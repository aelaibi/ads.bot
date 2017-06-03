package ma.labs.bot.rules;

import org.apache.commons.io.FileUtils;
import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.SocketTimeoutException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.List;

import static org.junit.Assert.*;

/********************************************************
 *                                                      *
 *                    PIXITREND ROBOT                   *
 *                    BY labs GROUP                    *
 *                                                      *
 ********************************************************/

/**
 * Created by labs004 on 19/07/2016.
 */
@RunWith(SpringRunner.class)
@SpringBootTest
public class FileManagerServiceTest {

    @Autowired
    FileManagerService fileManagerService;



    @Test
    public void assertGetFileContentAsStringIsWorking(){
        assertEquals("alert('hello labs');", fileManagerService.getFileContentAsString("js/test.js"));
    }

    @Test
    public void assertGetFileContentAsLisStringIsWorking(){

        //when
        List<String> out = fileManagerService.getFileContentAsLisString("xpaths/generic.data");
        //then
        assertNotNull(out);
        assertEquals("#GOOGLE", out.get(0));
    }

    @Test
    public void assertGetPossibleMediasIsWorking(){
        //when
        List<String> out = fileManagerService.getPossibleMedias();
        //then
        assertNotNull(out);
        assertEquals("//a[contains(@href,'doubleclick.net')]//img", out.get(0));
    }
    @Test
    public void assertGetPossibleMediasAsStringIsWorking(){
        //when
        String out = fileManagerService.getPossibleMediasAsString();
        //then
        assertNotNull(out);
        assertTrue(165<out.split("\\|").length);
    }

    @Test
    public void testGetChecksumTextWithEmptyText(){
        //when
        String out = fileManagerService.getChecksumText("");
        //then
        assertNotNull(out);
        assertEquals("d41d8cd98f00b204e9800998ecf8427e",out);
    }

    @Test
    public void testDownloadFile() throws IOException {
        //given
        clearMedia();
        //when
        fileManagerService.downloadFile("https://a.slack-edge.com/66f9/img/tour/tools.png","tools.png");
        //then
        Path p = Paths.get(fileManagerService.getMediaPath(), "tools.png");
        assertNotNull(p);
        assertTrue(p.toFile().exists());
    }

    //@Test
    public void testDownloadFileWithRetry() {
        //given
        fileManagerService.timeout=1;
        boolean exception=false;
        clearMedia();
        String badUrl="https://a.slack-edge.com/66f9/img/tour/tools8.png";
        //when
        long start = System.currentTimeMillis();
        long delay = 0;
        try {
            fileManagerService.downloadFile(badUrl,"tools.png");
        } catch (IOException e) {
            delay = System.currentTimeMillis()-start;
            exception = true;
        }
        assertTrue(exception);

        //then : more than 2*10000 millisecondes
        // ( 3 times with 10000 delay)
        System.out.println(delay);
        assertTrue(delay>(2*10000));
    }
    @Test
    public void testDownloadFileStartingwithData() throws IOException {
        //given
        clearMedia();
        String url = "data:image/gif;base64," +
                "R0lGODlhEAAQAMQAAORHHOVSKudfOulrSOp3WOyDZu6QdvCchPGolfO0o/XBs/fNwfjZ0frl3/zy7////wAAAAAAAAAAAAAAAAAA" +
                "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAACH5BAkAABAALAAAAAAQABAAAAVVICSOZGlCQAosJ6mu7fiyZeKqNKTo" +
                "QGDsM8hBADgUXoGAiqhSvp5QAnQKGIgUhwFUYLCVDFCrKUE1lBavAViFIDlTImbKC5Gm2hB0SlBCBMQiB0UjIQA7";
        //when
        fileManagerService.downloadFile(url,"star.png");
        //then
        Path p = Paths.get(fileManagerService.getMediaPath(), "star.png");
        assertNotNull(p);
        assertTrue(p.toFile().exists());
    }

    private void clearMedia()  {
        try {
            FileUtils.cleanDirectory(Paths.get(fileManagerService.getMediaPath()).toFile());
        }catch (Exception e){

        }

    }

    @Test
    public void testGetChecksumFileinMedia() throws IOException {
        //given
        String url = "data:image/gif;base64," +
                "R0lGODlhEAAQAMQAAORHHOVSKudfOulrSOp3WOyDZu6QdvCchPGolfO0o/XBs/fNwfjZ0frl3/zy7////wAAAAAAAAAAAAAAAAAA" +
                "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAACH5BAkAABAALAAAAAAQABAAAAVVICSOZGlCQAosJ6mu7fiyZeKqNKTo" +
                "QGDsM8hBADgUXoGAiqhSvp5QAnQKGIgUhwFUYLCVDFCrKUE1lBavAViFIDlTImbKC5Gm2hB0SlBCBMQiB0UjIQA7";

        byte[] image = Base64.getDecoder().decode(url.split(",")[1]);
        Files.write(Paths.get(fileManagerService.getMediaPath(),"star.png")
                        , image);
        //when
        String chk = fileManagerService.getChecksumFileinMedia("star.png");

        //then
        assertNotNull(chk);
        assertEquals("4c7d225b43162f89d93ca41e691ff537",chk);
    }

    @Test
    public void testGetChecksumFileinMedia2() throws IOException, URISyntaxException {
        //given
        URL url = this.getClass().getClassLoader().getResource("./media/64d0d19515485fb6eae73332cff30be9");
        assertNotNull(url);

        fileManagerService.createMediaFolderIfNotExisting();
        Files.copy(Paths.get(url.toURI()),Paths.get("media","64d0d19515485fb6eae73332cff30be9"));
        //when
        String chk = fileManagerService.getChecksumFileinMedia("64d0d19515485fb6eae73332cff30be9");

        //then
        assertNotNull(chk);
        assertEquals("64d0d19515485fb6eae73332cff30be9",chk);
    }


    @Test
    public void getPublicIpAdressShouldReturn(){
        String ip = fileManagerService.getPublicIpAdress();
        assertNotNull(ip);
        assertTrue(ip.length()>5);
        assertNotEquals("",ip);//can change
    }

    @Test
    @Ignore
    public void testGetFinalURLPixelMathtag() throws Exception {
        //when
        String in ="https://adclick.g.doubleclick.net/pcs/click?xai=AKAOjsubf7nPmKAzK2uq39cljL4xPiG0lk3a0-zgzuImNYr0UcG7xTP2G0CXYANN3nUI2gb9Ezxk6tv4gat0b5cA2zF8lda0kQvVCgc0ZkJMfnX3INz3DlE0cg&sig=Cg0ArKJSzNDA6ge78UOiEAE&urlfix=1&adurl=http://pixel.mathtag.com/click/img%3Fmt_aid%3D2059345716211239659%26mt_id%3D2103479%26mt_adid%3D144286%26mt_sid%3D1198645%26mt_exid%3D5%26mt_inapp%3D0%26mt_uuid%3D159d57bb-3e92-4c00-b982-c4c2d42805a7%26mt_3pck%3Dhttp%253A//ox-d.menadex.com/w/1.0/rc%253Fts%253D1fHJpZD03ZDRiNjVjOS1kYmM2LTRhY2MtODJjMy1hOGQ4OGUwYTFhY2N8cnQ9MTQ3MTkwNzMwN3xhdWlkPTUzODIxMjAyOXxhdW09RE1JRC5XRUJ8c3NpZD01MzcwNjY0MTN8c2lkPTUzNzIzNDE3M3xwdWI9NTM3MTI0MzA2fHBjPVVTRHxyYWlkPWQyZTVlYWU0LTRjNjktNDgzZS1iZGFiLTczYzU2YzhlYjU4ZnxhaWQ9NTM3MjcwNTMyfHQ9MTJ8YXM9MzAweDI1MHxsaWQ9NTM3MTkxMjAzfG9pZD01MzcxNDM4OTZ8cD0yOTk5fHByPTI5OTl8YXRiPTMyMDAwfGFkdj01MzcwNzMzOTl8YWM9VVNEfHBtPVBSSUNJTkcuQ1BNfG09MXxhaT0xNTJkMDdiMS1kYTQxLTRjNDAtYWQxOC1mZTc0YzI2OTUzNGF8bWM9R0JQfG1yPTB8cGk9MjI5M3xtdWk9Nzk1Nzk1MGQtYmFiNy00YTIyLWQyZDQtY2YyYjM5ODk2YmY4fG1hPTY1ODVjOWJhLTYwZDktNGNlNy04NTg3LTk0ZmY0Y2RjNTM0OHxtcnQ9MTQ3MTkwNzMwN3xtcmM9U1JUX1dPTnxtd2E9NTM3MDczMzk5fGNrPTF8bXdiaT0yNjQ2Mnxtd2I9MjQ0NjR8bWFwPTI5OTl8ZWxnPTF8bW9jPVVTRHxtb3I9MHxtcGM9R0JQfG1wcj0yMjkzfG1wZj01MHxtbWY9Mzh8bXBuZj01MHxtbW5mPTM4fHBjdj0yMDE2MDYyMHxtbz1PWC1HQnxlYz0yMTAzNDc5fG1wdT0yOTk5fG1jcD0yOTk5fGFxdD1ydGJ8aWM9ZjU2MzAyZTgtN2ExNy00ZGNhLTgzZDAtYmNkYzZlZGU3NDg2fHNhcz0zMDF4MjUxfGN0PTF8bXdjPTUzNzE0Mzg5Nnxtd3A9NTM3MTkxMjAzfG13Y3I9NTM3MjcwNTMyfG13cGY9MzAwMXxtd3BvcGY9MjI5NHxtd3BuZj0zMDAxfG13cG9wbmY9MjI5NHxtd2lzPTF8bXdwdD1veF9wcm90b3xwZGlkPTUzNjg3NDI3MXxwcGlkPTUzNjg4MzA3MHxwZHQ9MnxwZGRpZD1PWC1hZHYtWHhGZWpTfHBkcD0yMjkzfHVyPTNjRHJGbDRmbGN8bGQ9cm95YWxhaXJtYXJvYy5jb20%2526r%253D%26redirect%3Dhttp://www.royalairmaroc.com/ma-fr/Reservations-Promotions/Nos-meilleures-offres/Vol-Casablanca-Venise%253Futm_source%253Dadw%2526utm_medium%253Ddisplay%2526utm_campaign%253Dadw-maroc-france";
        String finalURL = fileManagerService.getFinalURL(in);
        //then
        assertNotNull(finalURL);
        assertEquals("http://www.royalairmaroc.com/ma-fr/Reservations-Promotions/Nos-meilleures-offres/Vol-Casablanca-Venise?utm_source=adw&utm_medium=display&utm_campaign=adw-maroc-france",finalURL);

    }

    @Test
    public void testGetFinalURLMenara() throws Exception {
        //when
        String finalURL = fileManagerService.getFinalURL("http://menara.ma");
        //then 
        assertNotNull(finalURL);
        assertEquals("http://www.menara.ma/ar",finalURL);

    }
    @Test
    public void testGetFinalURLGOOShortURL() throws Exception {
        //when
        String in = "https://goo.gl/TROC8v";
        //String in = "http://adserver.adtech.de/?adlink|3.0|1143|4957408|1|16|AdId=15243611;BnId=1;link=http://www.gbp.ma/EspaceCommunication/Pages/Actualite.aspx?IdActu=856";
        String finalURL = fileManagerService.getFinalURL(in);
        //then
        assertNotNull(finalURL);
        assertEquals("http://www.seleniumhq.org/",finalURL);
    }
    @Test
    public void testGetFinalURLCloudfront() throws Exception {
        //when
        String in = "http://cloudfront-labs.amazonaws.com/x.png";
        String finalURL = fileManagerService.getFinalURL(in);
        //then
        assertNotNull(finalURL);
        Assert.assertThat(finalURL, Matchers.endsWith("cloudfront.net/test.png"));
    }


    @Test
    public void testGetFinalURWithTimeout() throws Exception {
        //given
        ServerSocket serverSocket = new ServerSocket(0, 1);
        int port = serverSocket.getLocalPort();
        fileManagerService.timeout = 1;

        //when
        String in = "http://localhost:" +port+ "";
        try {
            fileManagerService.getFinalURL(in);
        }catch (SocketTimeoutException stx) {
            Assert.assertEquals(stx.getMessage(), "Read timed out"); //that's what are we waiting for
        }
                //some cleanup
        if (serverSocket != null && !serverSocket.isClosed()) {
            serverSocket.close();
        }
    }




    @Test
    public void convertAvi2Mp4(){
        //fileManagerService.convert2mp4InMedia("ads-meditel.avi");
    }

    @Test
    public void downloadYoutubeVideo(){
        //fileManagerService.downloadYoutubeVideo("ybZgyVHMqDE");
    }

    @Test
    @Ignore
    public void getDurationVideoFileMustBe30sec(){
        fileManagerService.downloadYoutubeVideo("ybZgyVHMqDE");
        assertEquals("30", fileManagerService.getDurationVideoFile("ybZgyVHMqDE.mp4"));
    }

    @Test
    public void testExistsInMediaOK() throws IOException {
        String path = "file.txt";
        assertFalse(fileManagerService.existsInMedia(path));

        fileManagerService.createFileInMedia(path, "hello labs !!");
        assertTrue(fileManagerService.existsInMedia(path));

        fileManagerService.deleteFromMedia(path);
    }

    @Test
    public void testcreateFbZipFileInMedia() throws IOException {

        //given
        String media = "fbMedia";
        fileManagerService.createDirectory(fileManagerService.getMediaPath()+"/fbMedia-folder/assets");
        fileManagerService.createFileInMedia("fbMedia-folder/fbMedia.html", "hello labs9999 !!");
        fileManagerService.createFileInMedia("fbMedia-folder/assets/azert", "hello labs !!");
        fileManagerService.createFileInMedia("fbMedia-folder/assets/1aq2s3eed5", "hello labs !!");
        //when
        fileManagerService.createFbZipFileInMedia("fbMedia-folder",media);

        //then
        assertTrue(fileManagerService.existsInMedia("fbMedia-folder/fbMedia.zip"));
        //clear
        FileUtils.deleteDirectory(Paths.get(fileManagerService.getMediaPath(), "fbMedia-folder").toFile());
    }

    @Test
    public void testgetImageBase64FromFileInMedia() throws IOException, URISyntaxException {
        URL url = this.getClass().getClassLoader().getResource("./media/mpu_bar.txt");
        assertNotNull(url);
        fileManagerService.createMediaFolderIfNotExisting();
        Files.copy(Paths.get(url.toURI()),Paths.get("media","2f357da0c47f7bbc"));
        String out = fileManagerService.getImageBase64FromFileInMedia("2f357da0c47f7bbc");
        assertNotNull(out);
        assertThat(out, Matchers.startsWith("data:image/svg+xml;base64,"));
//        assertEquals(2254, out.length());
    }
    @Test
    public void getImageBase64FromFileInMedia2ndTest() throws IOException, URISyntaxException {
        URL url = this.getClass().getClassLoader().getResource("./media/svgexample123456789");
        assertNotNull(url);
        fileManagerService.createMediaFolderIfNotExisting();
        Files.copy(Paths.get(url.toURI()),Paths.get("media","svgexample123456789"));
        String out = fileManagerService.getImageBase64FromFileInMedia("svgexample123456789");
        System.out.println(out);
        assertNotNull(out);
        assertThat(out, Matchers.startsWith("data:image/svg+xml;base64,"));
//        assertEquals(6002, out.length());
    }

    @Test
    public void getImageBase64FromWeb() throws IOException, URISyntaxException {

        fileManagerService.downloadFile("https://upload.wikimedia.org/wikipedia/commons/9/93/Number-line.svg","20160919061354945");
        String out = fileManagerService.getImageBase64FromFileInMedia("20160919061354945");
        System.out.println(out);
        assertNotNull(out);
        assertThat(out, Matchers.startsWith("data:image/svg+xml;base64,"));
    }

    @Test
    public void getChecksumFileinMediaFromWeb() throws IOException, URISyntaxException {

        fileManagerService.downloadFile("https://upload.wikimedia.org/wikipedia/commons/1/13/Redbloodcells.jpg","cbae026d098fc9042af5339799475f70");
        String out = fileManagerService.getChecksumFileinMedia("cbae026d098fc9042af5339799475f70");
        System.out.println(out);
        assertNotNull(out);

    }

    //@Test
    public void getChecksumFileinYOUTUBE() throws IOException, URISyntaxException {
        fileManagerService.downloadFile("https://r8---sn-p5h-jhoe.googlevideo.com/videoplayback?itag=18&ratebypass=yes&ipbits=0&initcwndbps=472500&mime=video%2Fmp4&key=yt6&mt=1483980091&gir=yes&sparams=clen%2Cdur%2Cei%2Cgir%2Cid%2Cinitcwndbps%2Cip%2Cipbits%2Citag%2Clmt%2Cmime%2Cmm%2Cmn%2Cms%2Cmv%2Cpl%2Cratebypass%2Crequiressl%2Csource%2Cupn%2Cexpire&expire=1484001890&upn=wLTiBa9khbs&lmt=1483246610984899&ip=41.250.201.215&pl=21&dur=7.105&mv=m&source=youtube&ms=au&ei=Ar5zWLeQAuT1iQbujpDoAQ&mn=sn-p5h-jhoe&mm=31&id=o-AIBZNVfJu_aloGeI0CU6xXAZTgyAxN7jZPntKTvKQJ56&signature=3B0318E812F422DE05FA9C20682579BC3A67B965.A0C46D4BB6DCFC61B0059D9AA75594FD1E0CB91B&requiressl=yes&clen=538417"
                ,"file1.mp4");
        String out = fileManagerService.getChecksumFileinMedia("file1.mp4");
        System.out.println(out);

        fileManagerService.downloadFile("https://r10---sn-25g7sn7k.googlevideo.com/videoplayback?requiressl=yes&nh=IgpwcjAxLnBhcjAxKg0xOTUuMTU0LjMuMjI3&usequic=no&gir=yes&dur=7.105&initcwndbps=3740000&pl=23&ratebypass=yes&source=youtube&ipbits=0&lmt=1483246610984899&itag=18&expire=1484002334&mime=video%2Fmp4&key=yt6&mm=31&mn=sn-25g7sn7k&upn=2tS3Wv8guRk&mt=1483980394&mv=m&ms=au&signature=79708E99979BE0B678E6B246250D4B601E6A0362.12B2319D91AF6600DA79EC848A77BCA6E00B7A4D&ip=163.172.91.211&sparams=clen%2Cdur%2Cei%2Cgir%2Cid%2Cinitcwndbps%2Cip%2Cipbits%2Citag%2Clmt%2Cmime%2Cmm%2Cmn%2Cms%2Cmv%2Cnh%2Cpl%2Cratebypass%2Crequiressl%2Csource%2Cupn%2Cusequic%2Cexpire&ei=vr9zWK_2EMaocdyPpMgD&beids=%5B9452307%5D&id=o-AGiJzOHNHeCSAwZcnUjZmWyG3zpQJ72tkxSRzXxKLnAd&clen=538417"
                ,"file2.mp4");
        String out2 = fileManagerService.getChecksumFileinMedia("file2.mp4");
        System.out.println(out2);


    }

    @Test
    public void testExtractZipFile() throws IOException,URISyntaxException {
        //given
        URL url = this.getClass().getClassLoader().getResource("./ael.zip");
        assertNotNull(url);
        String folder = "test";
        Files.createDirectories(Paths.get(folder));
        Files.copy(Paths.get(url.toURI()),Paths.get(folder,"ael.zip"));
        //when
        fileManagerService.extractZipInFolder("ael.zip",folder);

        //then
        assertTrue(Paths.get(folder,"ael/fbMedia.html").toFile().exists());
        assertTrue(Paths.get(folder,"ael/assets/1aq2s3eed5").toFile().exists());
        //clean
        FileUtils.deleteDirectory(Paths.get(folder).toFile());
    }

    @Test
    public void testDeleteProfilesFolder() throws IOException {
        String profilesPath = "firefoxProfiles";

        try {
            fileManagerService.setProfilesPath(profilesPath);
            fileManagerService.setMode("prod");
            Files.createDirectories(Paths.get(profilesPath, "test"));
            Files.createDirectories(Paths.get(profilesPath, "test2"));
            Files.createFile(Paths.get(profilesPath, "test/empty.txt"));
            File[] files = Paths.get(profilesPath).toFile().listFiles();
            assertNotNull(files);
            assertEquals(2,files.length);

            fileManagerService.emptyProfilesFolder();

            assertTrue(Paths.get(profilesPath).toFile().exists());
            assertEquals(0,Paths.get(profilesPath).toFile().listFiles().length);


        }catch (IOException e){}
        finally {
            FileUtils.deleteDirectory(Paths.get(profilesPath).toFile());
        }

    }

    @Test

    public void testRedirectingUrl() throws IOException, Exception {

        //http://www.google.com/images/branding/googlelogo/2x/googlelogo_color_120x44dp.png
        final String url = "https://goo.gl/5zJb6S";

        final String destFile = "googlelogo_color_120x44dp.png";
        Path path = Paths.get("media",destFile);
        assertFalse(path.toFile().exists());

        fileManagerService.downloadFile(url, destFile);

        assertTrue(path.toFile().exists());
        path.toFile().delete();
    }

}