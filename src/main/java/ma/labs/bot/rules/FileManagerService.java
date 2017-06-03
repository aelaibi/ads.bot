package ma.labs.bot.rules;
/********************************************************
 *                                                      *
 *                    PIXITREND ROBOT                   *
 *                    BY labs GROUP                    *
 *                                                      *
 ********************************************************/

import ma.labs.bot.core.ImageUrlFileName;
import ma.labs.bot.utils.Constants;
import ma.labs.bot.utils.Utils;
import ma.labs.bot.utils.cmd.CommandExecutor;
import ma.labs.bot.utils.nio.DefaultRedirectStrategyImpl;
import ma.labs.bot.utils.nio.DefaultTrustManager;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.client.CircularRedirectException;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.HttpCoreContext;
import org.apache.http.util.EntityUtils;
import org.apache.tika.Tika;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;

import javax.net.ssl.KeyManager;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.*;
import java.security.SecureRandom;
import java.util.*;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

/**
 * Created by labs004 on 19/07/2016.
 */
@Component
public class FileManagerService {


    private static final long DELAY = 10000;
    public static final String CHECK_IP_AMAZONAWS_COM = "http://checkip.amazonaws.com";
    private final Logger logger = LoggerFactory.getLogger(FileManagerService.class);
    @Autowired
    private ResourceLoader resourceLoader;

    public String getMediaPath() {
        return mediaPath;
    }

    @Value("${media.path}")
    private String mediaPath;
    @Value("${firefox.profiles.path}")
    private String profilesPath;

    @Value("${ffmpeg.path}")
    private String ffmpegPath;
    @Value("${ffprobe.path}")
    private String ffprobePath;
    @Value("${youtube.dl.path}")
    private String youtubeDlPath;

    @Value("${http.timeout:20}")
    protected int timeout;

    @Value("${mode:dev}")
    private String mode;

    public void setMode(String mode) {
        this.mode = mode;
    }

    public String getProfilesPath() {
        return profilesPath;
    }

    public void setProfilesPath(String profilesPath) {
        this.profilesPath = profilesPath;
    }

    public  String getFileContentAsString(String filename){
        try {
            String line ;
            StringBuilder responseData = new StringBuilder();
            Resource resource = resourceLoader.getResource(filename);
            BufferedReader in = new BufferedReader(new InputStreamReader(resource.getInputStream()));
            while((line = in.readLine()) != null) {
                responseData.append(line);
            }
            in.close();
            return responseData.toString();
        } catch (IOException e) {
            logger.error(e.getLocalizedMessage(),e);
        }
        return "";
    }

    public List<String> getFileContentAsLisString(String filename){
        String line ;
        List<String> out = new ArrayList<>();
        BufferedReader in;
        Resource resource = resourceLoader.getResource(filename);
        try {
            in = new BufferedReader(new InputStreamReader(resource.getInputStream()));
            while((line = in.readLine()) != null) {
                out.add(line);
            }
            in.close();
            return out;
        } catch (IOException e) {
            logger.error(e.getMessage(),e);
        }
        return null;
    }

    protected List<String> getPossibleMedias(){
        List<String> out = getFileContentAsLisString("xpaths/generic.data")
                .stream()
                .filter(line -> (!"".equals(line) && !line.startsWith("#")))
                .collect(Collectors.toList());

        return out;
    }
    public String getPossibleMediasAsString(){
        return String.join("|",getPossibleMedias());
    }

    //CALCULATE CHECKSUM OF A STRING
    public String getChecksumText(String text) {
        try {
            if ("".equals(text)) {
                return Constants.CHECKSUMEMPTY;
            } else {
                return DigestUtils.md5Hex(text);
            }
        } catch (Exception e) {
            logger.error(e.getLocalizedMessage());
            return Constants.CHECKSUMEMPTY;
        }
    }
    //CALCULATE CHECKSUM OF A file from media folder
    public String getChecksumFileinMedia(String file) {
        try {
            Path p = Paths.get(mediaPath,file);
            InputStream is = Files.newInputStream(p,StandardOpenOption.READ);
            String out = DigestUtils.md5Hex(is).replace("-","").toLowerCase();
            is.close();
            return out;
        }catch (Exception e) {
            logger.error(e.getLocalizedMessage(),e);
            return null;
        }
    }

    public File getFileFromMedia(String filename){
        Path p = Paths.get(mediaPath,filename);
        return p.toFile();
    }

    //copy a source file to target
    //both files are in media
    public void copyFileInMedia(String src, String target) {
        try {
            Path pSrc = Paths.get(mediaPath,src);
            Path pTarget = Paths.get(mediaPath,target);
            Files.copy(pSrc, pTarget, StandardCopyOption.REPLACE_EXISTING);
        } catch (Exception e) {
            logger.error(e.getLocalizedMessage(),e);
        }
    }
    public long getSizeOfFbMedia(String mediaID){
        /* media_id is the name of the media file, an html file with no extension
         * so, fb ad size = media file size [+ video size]
         * if the video file is present, he's in called 'mediaID-folder/video-file-checksum'
         */
        long out = this.getFileSizeInMedia(mediaID);
        final String adFolder = this.mediaPath+"/"+mediaID+"-folder";
        final File folder = new File(adFolder);
        if(folder.exists()){
            for(File file : folder.listFiles()){
                out += this.getFileSizeInFolderInMedia(adFolder, file.getName());
            }
        }
        return out;
    }
    public long getFileSizeInMedia(String filename){
        try {
            return Paths.get(mediaPath,filename).toFile().length();
        } catch (Exception e) {
            logger.error(e.getLocalizedMessage());
            return -1;
        }
    }

    private long getFileSizeInFolderInMedia(String folderName, String fileName){
        try {
            return Paths.get(folderName, fileName).toFile().length();
        } catch (Exception e) {
            logger.error(e.getLocalizedMessage());
            return -1;
        }
    }

    public  String getImageBase64FromFileInMedia(String fileName){
        logger.debug("converting file {} to base64", fileName);
        String imB64 = null;
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        InputStream stream = null;
        try {
            File file = Paths.get(mediaPath,fileName).toFile();
            stream = new FileInputStream(file);
            byte[] chunk = new byte[4096];
            int bytesRead;
            while ((bytesRead = stream.read(chunk)) > 0) {
                outputStream.write(chunk, 0, bytesRead);
            }
            Tika tika = new Tika();
            String mimeType = tika.detect(file);
            if("text/plain".equals(mimeType)){//some svg are considered as plain text
                //example : https://s0.2mdn.net/5859718/1479723426634/fz14239a%20Q4%20Lebanon%20Burst%204%20Sale%20Fares%20activity-LB/text-09.svg
                mimeType = "image/svg+xml";
            }
            imB64 = "data:" + mimeType + ";base64," + Base64.getEncoder().encodeToString(outputStream.toByteArray());
            stream.close();
        } catch (Exception e) {
            logger.error(e.getLocalizedMessage());
            try {
                if (stream!=null)
                    stream.close();
            } catch (IOException e1) {
            }
        }
        return imB64;

    }

    //DOWNLOAD FILE FROM URL INTO LOCAL MEDIA FOLDER
    @Retryable(value = {IOException.class}, maxAttempts = 3, backoff = @Backoff(delay = DELAY))
    public void downloadFile(String url, String destinationFilename) throws IOException {
        try {
            logger.debug("downloading file {} from {}",destinationFilename, url);
            createMediaFolderIfNotExisting();
            if (!url.startsWith("http") && !url.startsWith("file") && !url.startsWith("data")) {
                url = "http://" + url;
            } else if (url.startsWith("//")) {//case of urls starting with //
                url = "http:" + url;
            }
            if (!"".equals(url) && (url.startsWith("http") || url.startsWith("file"))) {
                HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
                connection.setConnectTimeout(timeout*Constants.TIMEOUT_UNIT);
                connection.setReadTimeout(timeout*Constants.TIMEOUT_UNIT);
                connection.setRequestProperty("Connection","close");
                connection.setRequestProperty("user-agent",
                        "Mozilla/5.0 (Windows NT 6.3; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/47.0.2526.106 Safari/537.36");
                connection.setRequestProperty("Accept",
                        "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
                connection.setRequestProperty("Accept-Language", "fr-FR,fr;q=0.8,en-US;q=0.6,en;q=0.4");
                //connection.setRequestProperty("Accept-Encoding", "gzip, deflate, sdch");
                connection.setRequestProperty("Cache-Control", "max-age=0");
                final int responseCode = connection.getResponseCode();

                // detect if the response was redirected to another url !!!!
                if (responseCode == HttpURLConnection.HTTP_MOVED_TEMP || responseCode == HttpURLConnection.HTTP_MOVED_PERM
                        || responseCode == HttpURLConnection.HTTP_SEE_OTHER){
                    final String newUrl = connection.getHeaderField("Location");
                    downloadFile(newUrl, destinationFilename);
                    return;
                }
                FileUtils.copyInputStreamToFile(connection.getInputStream(), Paths.get(mediaPath,destinationFilename).toFile());
            }else if(url.startsWith("data")){
                createFileInMedia(destinationFilename, url);
            }
        } catch (IOException e) {
            logger.error("{} : can not download file from {}",e.getMessage(),url);
            throw e;
        }
    }

    //CREATE a TEXT FILE IN MEDIA
    public void createFileInMedia(String filename, String fileContent) throws IOException {
        createMediaFolderIfNotExisting();
        try (BufferedWriter writer = Files.newBufferedWriter(Paths.get(mediaPath,filename), StandardOpenOption.CREATE)) {
            writer.write(fileContent);
        }
        fileContent=null;
    }

    protected void createMediaFolderIfNotExisting() {
        Path p = Paths.get(mediaPath);
        if(!p.toFile().exists())
            try {
                Files.createDirectories(p);
            } catch (IOException e) {
                logger.error("can not create {} : {}",mediaPath, e.getLocalizedMessage());
            }
    }
    //LOOP UNTIL FINDING THE FINAL URL
    public String getFinalURL(String urlStr) throws Exception {
        if(!Utils.checkValidURI(urlStr)){
            urlStr = Utils.gatValidUriString(urlStr);
        }
        // it 's a workaround , to avoid http check for site having bad cert
        SSLContext ctx = SSLContext.getInstance("TLS");
        ctx.init(new KeyManager[]{}, new TrustManager[]{new DefaultTrustManager() }, new SecureRandom());
        SSLContext.setDefault(ctx);
        RequestConfig config = RequestConfig.custom()
                .setConnectTimeout(timeout * Constants.TIMEOUT_UNIT)
                .setConnectionRequestTimeout(timeout * Constants.TIMEOUT_UNIT)
                .setSocketTimeout(timeout * Constants.TIMEOUT_UNIT).build();
        CloseableHttpClient httpclient = HttpClients
                .custom()
                .setDefaultRequestConfig(config)
                .setSSLContext(ctx)
                .setRedirectStrategy(new DefaultRedirectStrategyImpl())
                .build();

        HttpUriRequest request = RequestBuilder.get()
                .setUri(urlStr)
                .addHeader("Accept","text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
                .addHeader("User-Agent","Mozilla/5.0 (Windows NT 10.0; WOW64; rv:47.0) Gecko/20100101 Firefox/47.0")
                .addHeader("Accept-Language","fr,fr-FR;q=0.8,en-US;q=0.5,en;q=0.3")
                .addHeader("Accept-Encoding","gzip, deflate")
                .addHeader("Connection","keep-alive")
                .build();
        HttpContext context = new BasicHttpContext();
        String currentUrl ;
        try {
            httpclient.execute(request,context);
            HttpUriRequest currentReq = (HttpUriRequest) context.getAttribute(
                    HttpCoreContext.HTTP_REQUEST);
            HttpHost currentHost = (HttpHost)  context.getAttribute(
                    HttpCoreContext.HTTP_TARGET_HOST);
            currentUrl = (currentReq.getURI().isAbsolute()) ? currentReq.getURI().toString() : (currentHost.toURI() + currentReq.getURI());
            if(currentUrl.equals(urlStr)
                    || currentUrl.contains("pixel.mathtag.com")){//try to detect http-equiv=refresh (redirection inside html)
                String resp = EntityUtils.toString(((HttpResponse)context.getAttribute(HttpCoreContext.HTTP_RESPONSE)).getEntity(),"UTF-8");
                String metaHTTPEQUIV= RegExService.findFirst("<meta http-equiv=('|\")refresh('|\").*content=('|\")(.*?)('|\")", resp, false,4);
                if(metaHTTPEQUIV!= null && (metaHTTPEQUIV.contains("url")||metaHTTPEQUIV.contains("URL"))){
                    String target = RegExService.findFirst("(url|URL)=(.*?)$",metaHTTPEQUIV, false,2);
                    if(target!=null && !target.equals(""))
                        currentUrl= Utils.unescapeDataString(StringEscapeUtils.unescapeHtml4(target));
                }
            }
        }catch (ClientProtocolException ce){
            if(ce.getCause() instanceof CircularRedirectException){
                logger.warn("Circular redirect to {}", urlStr);
                currentUrl = ((HttpResponse)context.getAttribute(HttpCoreContext.HTTP_RESPONSE)).getFirstHeader("location").getValue();
            }else {
                throw ce;
            }
        }
        return currentUrl;
    }

    public void deleteFromMedia(String tmpFilename) {
        try {
            Files.delete(Paths.get(mediaPath,tmpFilename));
        } catch (IOException e) {
            logger.error("Can not delete file {} : {}",tmpFilename, e.getMessage());
        }
    }

    public void emptyMediaFolder(){
        try {
            FileUtils.deleteDirectory(Paths.get(mediaPath).toFile());
        } catch (IOException e) {
            logger.error("Can not empty folder {} : {}",mediaPath, e.getMessage());
        }
    }

    public void emptyProfilesFolder(){
        if(Utils.isDevMode(mode))
            return;
        try {
            Path p = Paths.get(profilesPath);
            FileUtils.deleteDirectory(p.toFile());
            Files.createDirectories(p);
        } catch (IOException e) {
            logger.error("Can not empty folder {} : {}",mediaPath, e.getMessage());
        }
    }

    public void convert2mp4InMedia(String filename) {

        try {
            String fullFilename = mediaPath+"/"+filename;
            CommandExecutor.execute(ffmpegPath, "-y", "-v", "error", "-i", fullFilename, fullFilename+".mp4");
        }catch (Exception e){
            logger.error(e.getMessage(),e);
        }
    }

    public void downloadYoutubeVideo(String youtubeVideoID) {
        try{
            String url = "https://www.youtube.com/watch?v=" + youtubeVideoID;
            String targetFile = mediaPath+"/"+youtubeVideoID+".mp4";
            //update DL
            CommandExecutor.execute(youtubeDlPath, "-U");
            //Download video
            CommandExecutor.execute(youtubeDlPath, url, "-f", "18", "-o", targetFile);
        }catch (Exception e){
            logger.error(e.getMessage(),e);
        }

    }

    //GET DURATION IN SECONDES (ROUNDED TO INT)
    public String getDurationVideoFile(String filename) {

        List<String> out = CommandExecutor.executeAndGetOutPut(ffmpegPath, "-i", mediaPath+"/"+filename);
        for(String line : out){
            long sec = RegExService.getDurationInSecondes(line);
            if(sec != -1){
                logger.debug("duration of {} is {}",filename,sec);
                return Long.toString(sec);
            }
        }
        return "0";
    }

    public void createDirectory(String path) {
        try {
            Files.createDirectories(Paths.get(path));
        } catch (IOException e) {
            logger.error("Can not create directory {}",path);

        }
    }

    public boolean existsInMedia(String path) {
        return Files.exists(Paths.get(mediaPath,path));
    }

    public boolean existsInFolder(String path, String folderPath) {
        return Files.exists(Paths.get(folderPath,path));
    }

    public boolean isProfileFolderExists(String profileZipFolder) {
        return Paths.get(profilesPath, profileZipFolder.replace(".zip", "")).toFile().exists();
    }

    private  void addToZipFile(File file,String fileName, ZipOutputStream zos)
            throws  IOException {
        FileInputStream fis = new FileInputStream(file);
        ZipEntry zipEntry = new ZipEntry(fileName);
        zos.putNextEntry(zipEntry);

        byte[] bytes = new byte[1024];
        int length;
        while ((length = fis.read(bytes)) >= 0) {
            zos.write(bytes, 0, length);
        }

        zos.closeEntry();
        fis.close();
    }
    public void createFbZipFileInMedia(String mediaFBFolderPath, String media_id) {
        File zip = Paths.get(mediaPath,mediaFBFolderPath + "/" + media_id + ".zip").toFile();
        try(ZipOutputStream zos =
                    new ZipOutputStream(new FileOutputStream(zip))) {

            addToZipFile(Paths.get(mediaPath,mediaFBFolderPath ,  media_id + ".html").toFile(), media_id + ".html", zos);
            //addToZipFile("app/banner.txt", zos);
            try (DirectoryStream<Path> stream = Files.newDirectoryStream(Paths.get(mediaPath,mediaFBFolderPath,"assets"))) {
                for (Path entry: stream) {
                    addToZipFile(entry.toFile(),"assets/"+entry.toFile().getName(),zos);
                }
            }
        } catch(IOException ioe) {
            logger.error(ioe.getMessage());
        }
    }

    public void downloadAndExtractProfile(String firefoxProfileUrl, String profileNameZip) {
        try {
            URLConnection connection = new URL(firefoxProfileUrl).openConnection();
            ReadableByteChannel rbc = Channels.newChannel(connection.getInputStream());
            FileChannel outFileChannel = FileChannel.open(Paths.get(profilesPath, profileNameZip)
                    , EnumSet.of(StandardOpenOption.CREATE, StandardOpenOption.WRITE));
            outFileChannel.transferFrom(rbc, 0, Integer.MAX_VALUE);
            outFileChannel.close();
            rbc.close();
            extractZipInFolder(profileNameZip, profilesPath);
        }catch (IOException ioe){
            logger.warn("I can not download the profile: {}",firefoxProfileUrl);
        }
    }

    public String downloadFileAsString(String url) {
        try {
            return IOUtils.toString(new URL(url));
        }catch (IOException ioe){
            logger.warn("I can not download  {}",url);
            return "";
        }

    }

    public void extractZipInFolder(String profileNameWithZipExt, String firefoxProfiles) {
        logger.debug("start unzip");
        String profileName = profileNameWithZipExt.replace(".zip", "");
        try {
            FileUtils.deleteDirectory(Paths.get(firefoxProfiles,profileName).toFile());
        } catch (IOException e) {
            logger.error("Can not delete folder {} : {}",profileName, e);
        }

        try{
            //create output directory is not exists
            String outputFolder = firefoxProfiles+"/"+profileName;
            File folder = new File(outputFolder);
            if(!folder.exists()){
                folder.mkdir();
            }

            ZipFile zipFile = new ZipFile(Paths.get(firefoxProfiles,profileNameWithZipExt).toFile());
            Enumeration<?> enu = zipFile.entries();
            //get the zipped file list entry
            while (enu.hasMoreElements()) {
                ZipEntry entry = (ZipEntry) enu.nextElement();
                String fileName = entry.getName();
                File newFile = new File(outputFolder + File.separator + fileName);
                //System.out.println("file unzip : "+ fileName);
                if (fileName.endsWith("/")) {
                    newFile.mkdirs();
                    continue;
                }
                new File(newFile.getParent()).mkdirs();
                InputStream is = zipFile.getInputStream(entry);
                FileOutputStream fos = new FileOutputStream(newFile);
                byte[] bytes = new byte[1024];
                int length;
                while ((length = is.read(bytes)) >= 0) {
                    fos.write(bytes, 0, length);
                }
                is.close();
                fos.close();
            }
            zipFile.close();
            logger.debug("unzip done");

        }catch(IOException ex){
            logger.error("can not unzip file",ex);
        }
    }
    public String getImageUrlFromFbSrc(String imgUrl){
        String imageUrl;

        try { imageUrl = getFinalURL(imgUrl); }
        catch (Exception e) { imageUrl = imgUrl; }

        if (imageUrl.contains("safe_image")) {
            if (imageUrl.contains("url=")) {
                try {
                    return Utils.unescapeDataString(
                            Utils.unescapeDataString(
                                    Utils.parseQueryString(imageUrl).get("url")));
                } catch (UnsupportedEncodingException e) {
                    logger.warn("UnsupportedEncodingException when unescapeDataString of url {}, getting parameter url.", imageUrl);
                    return imageUrl;
                }
            } else {
                return StringEscapeUtils.unescapeHtml4(imageUrl);
            }
        } else {
            return StringEscapeUtils.unescapeHtml4(imageUrl);
        }
    }

    public String replaceImagesWithBase64(String htmlCodeWholePage, List<ImageUrlFileName> images2Convert64) {
        String out = htmlCodeWholePage;
        try{
            for (ImageUrlFileName img : images2Convert64) {
                try {
                    out = out.replace(img.getImageUrl(), getImageBase64FromFileInMedia(img.getFileName()));
                } catch (NullPointerException e) {
                    logger.error("NullPointerException in replaceImagesWithBase64, exception={}", e);
                    continue;
                }
                logger.debug("replace {} with base64",img.getImageUrl());
                deleteFromMedia(img.getFileName());
            }
            return out;
        }catch (Exception e){
            logger.error("converting image to base64",e);
        }
        return htmlCodeWholePage;
    }

    public  String getPublicIpAdress(){
        try {
            URL whatismyip = new URL(CHECK_IP_AMAZONAWS_COM);
            BufferedReader in = new BufferedReader(new InputStreamReader(
                    whatismyip.openStream()));

            String ip = in.readLine(); //you get the IP as a String
            return ip;
        } catch (IOException e) {
            logger.error("error occured while trying to get public ip adress. ", e);
        }
        return "";
    }
}
