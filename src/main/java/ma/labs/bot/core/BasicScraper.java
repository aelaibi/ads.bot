package ma.labs.bot.core;
/********************************************************
 *                                                      *
 *                    PIXITREND ROBOT                   *
 *                    BY labs GROUP                    *
 *                                                      *
 ********************************************************/

import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.WinNT;
import ma.labs.bot.connectors.AWSConnector;
import ma.labs.bot.connectors.DatabaseAPIConnector;
import ma.labs.bot.connectors.FTPConnector;
import ma.labs.bot.connectors.MediaHelperAPIConnector;
import ma.labs.bot.data.AdNetwork;
import ma.labs.bot.data.Robot;
import ma.labs.bot.data.Visual;
import ma.labs.bot.data.VisualResponse;
import ma.labs.bot.rules.FileManagerService;
import ma.labs.bot.rules.RegExService;
import ma.labs.bot.utils.Constants;
import ma.labs.bot.utils.GUIHelper;
import ma.labs.bot.utils.TimeUtils;
import ma.labs.bot.utils.Utils;
import ma.labs.bot.utils.cmd.CommandExecutor;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.SystemUtils;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxBinary;
import org.openqa.selenium.firefox.FirefoxProfile;
import org.openqa.selenium.os.Kernel32;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.util.Assert;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import static org.openqa.selenium.Platform.WINDOWS;

/**
 * Created by labs004 on 15/08/2016.
 */

public  class BasicScraper implements DisposableBean {

    protected static Logger logger = LoggerFactory.getLogger(BasicScraper.class);

    static {
        System.setProperty("java.awt.headless", "false");
    }

    @Autowired
    protected WebDriverWrapper webDriverWrapper;
    @Autowired
    protected FileManagerService fileManagerService;
    @Autowired
    protected DatabaseAPIConnector databaseAPIConnector;
    @Autowired
    protected AWSConnector awsConnector;
    @Autowired
    protected FTPConnector ftpConnector;
    @Autowired
    protected GUIHelper guiHelper;
    @Autowired
    protected MediaHelperAPIConnector mediaHelperAPIConnector;


    protected WebDriver driver;
    protected Dimension sizeScreen;
    protected String possibleMedias =null;
    protected Robot robotData;
    protected Integer firefoxProcessId;


    private String pacUrl ="http://api.pixitrend.com/pac.php?proxy=%s&port=%d";

    @Value("${mode:dev}")
    protected String mode;
    @Value("${firefox.profiles.path}")
    protected String firefoxProfiles ;
    @Value("${screenshot.active:false}")
    protected boolean isScreenshotON;

    Stack<String> framesUrl = new Stack<>();
    List<String> fileNames = new ArrayList<>();
    int offsetX = 0;
    int offsetY = 0;
    Dimension lastFrameSize = new Dimension(0, 0);

    String iFrameSelector = "//iframe[" +
            "not(" +
            "contains(@src,'s-static.ak.facebook.com') or " +           //FACEBOOK
            "contains(@src,'facebook.com/plugins') or " +               //FACEBOOK
            "contains(@src,'facebook.com/widgets') or " +               //FACEBOOK
            "contains(@src,'plus.google.com') or " +                    // GOOGLE
            "@id='history-iframe' or " +                    // GOOGLE
            "@style='display: none;' or " +                    // GOOGLE
            "contains(@src,'facebook.com/v') or " +                     //FACEBOOK
            "contains(@src,'facebook.com/connect') or " +               //FACEBOOK
            "contains(@src,'staticxx.facebook.com') or " +               //FACEBOOK
            "contains(@src,'platform.twitter.com') or " +               //TWITTER
            "contains(@src,'www.youtube.com/embed') or " +               //YOUTUBE
            "contains(@src,'vid-v2.html') or " +                        //http://banners.hespress.com/2015/11/09/vid-v2.html?vidId=6ldoyoqZf7E
            "contains(@src,'apis.google.com/') or " +                   //GOOGLE+
            "contains(@src,'addthis.com') or " +                        //ADDTHIS
            "contains(@src,'sharethis.com') or " +                      //SHARETHIS
            "contains(@src,'disqus.com/embed/comments') or " +          //DISQUS AUDIENCE BLOCK COMENTS
            "contains(@src,'effectivemeasure.net') or " +               //EFFECTIVEMEASURE
            "contains(@name,'__hidden__') or " +                            //HIDDEN GOOGLE FRAMES
            "contains(@id,'ReadMoreRightSide') or " +                            //.alaraby.co.uk
            "contains(@src,'www.google.com/recaptcha') or " +                            //google recaptcha
            "contains(@src,'habous.gov.ma/horaire') or " +              //habous.gov.ma horaire
            "contains(@src,'rma-api.gravity.com/') or " +               //huffpostmaghreb.com most read
            "contains(@id,'twitter-widget')" +                            //twitter iframe
            ")" +
            "]";

    @PostConstruct
    private void init(){
        possibleMedias = fileManagerService.getPossibleMediasAsString();
        fileManagerService.emptyMediaFolder();

    }




    protected Integer getFirefoxPid(FirefoxBinary binary){
        try {
            final Field fieldCmdProcess = FirefoxBinary.class.getDeclaredField("process");
            fieldCmdProcess.setAccessible(true);
            final Object ObjCmdProcess = fieldCmdProcess.get(binary);

            final Field fieldInnerProcess = ObjCmdProcess.getClass().getDeclaredField("process");
            fieldInnerProcess.setAccessible(true);
            final Object objInnerProcess = fieldInnerProcess.get(ObjCmdProcess);

            final Field fieldWatchDog = objInnerProcess.getClass().getDeclaredField("executeWatchdog");
            fieldWatchDog.setAccessible(true);
            final Object objWatchDog = fieldWatchDog.get(objInnerProcess);

            final Field fieldReelProcess = objWatchDog.getClass().getDeclaredField("process");
            fieldReelProcess.setAccessible(true);
            final Process process = (Process) fieldReelProcess.get(objWatchDog);

            final Integer pid;

            if (Platform.getCurrent().is(WINDOWS)) {
                final Field f = process.getClass().getDeclaredField("handle");
                f.setAccessible(true);
                long hndl = f.getLong(process);

                final Kernel32 kernel = Kernel32.INSTANCE;
                final WinNT.HANDLE handle = new WinNT.HANDLE();
                handle.setPointer(Pointer.createConstant(hndl));
                pid = kernel.GetProcessId(handle);

            } else {
                final Field f = process.getClass().getDeclaredField("pid");
                f.setAccessible(true);
                pid = (Integer) f.get(process);
            }
            logger.info("firefox process id : " + pid + " on plateform : " + Platform.getCurrent());
            return pid;
        } catch (Exception e) {
            logger.error("Cannot get firefox process id, exception is : {}", e.getLocalizedMessage());
        }
        return null;
    }

    public void openBrowser(){
        String firefoxProfileUrl = robotData.getFirefoxFile();
        FirefoxBinary binary = new FirefoxBinary();
        File firefoxProfileFolder = null;
        FirefoxProfile profile=new FirefoxProfile();
        if(firefoxProfileUrl != null){
            try {
                String profileZipFolder = firefoxProfileUrl.substring((firefoxProfileUrl.lastIndexOf("/")+1));
                if(Utils.isProdMode(mode)) {
                    if (!fileManagerService.isProfileFolderExists(profileZipFolder)) {
                        fileManagerService.downloadAndExtractProfile(firefoxProfileUrl, profileZipFolder);
                    }
                }
                firefoxProfileFolder =  Paths.get(firefoxProfiles,  profileZipFolder.replace(".zip", "")).toFile();
            } catch (Exception e) {
                logger.warn("FirefoxProfiles folder is not found : {}",firefoxProfileUrl,e);
            }
            if(firefoxProfileFolder!=null && firefoxProfileFolder.exists()){
                profile = new FirefoxProfile(firefoxProfileFolder);
            }
        }
        profile.setAcceptUntrustedCertificates(true);
        addPlugins(profile);
        setPreferences(profile);
        DesiredCapabilities capabilities = DesiredCapabilities.firefox();
        capabilities.setJavascriptEnabled(true);
        capabilities.setCapability("marionette", false);
        if(robotData.isFirefoxProxyEnable()){
            setUpProxy(profile);
        }

        logger.debug("open firefox");
        driver = webDriverWrapper.createFireFoxDriver(binary, profile, capabilities);
        logger.debug("firefox opened");

        this.resizeWindow();
        this.firefoxProcessId = getFirefoxPid(binary);
        if(this.firefoxProcessId == null){
            logger.error("Firefox process id is null !!");
        }
        TimeUtils.waitFor(1500);
        sizeScreen = driver.manage().window().getSize();
        if(!isAnonymous()){
            checkProfileConnected();
        }
        if(robotData.isFirefoxProxyEnable()){
            checkProxyIsOk();
        }
    }

    private void checkProxyIsOk() {
        String ip = fileManagerService.getPublicIpAdress();
        String ipFromFireFox = "";
        try {
            driver.get(fileManagerService.CHECK_IP_AMAZONAWS_COM);
            ipFromFireFox = driver.findElement(By.tagName("body")).getText();
        } catch (Exception e) {
            logger.error("somthing is wrong!", e);
        }

        Assert.isTrue(!ip.equals(ipFromFireFox),
                "Firefox proxy is not ready, robot is shutting down !!");
    }

    private void setUpProxy(FirefoxProfile profile) {
        String[] tokens = robotData.getFirefoxProxy().split(":");
        if(tokens!=null && tokens.length == 2){
            String host = tokens[0];
            int port = Integer.parseInt(tokens[1]);
            String pac = String.format(pacUrl, host, port);
            String pacFileContent = fileManagerService.downloadFileAsString(pac);
            Assert.isTrue(pacFileContent.contains("FindProxyForURL"),
                    "Robot proxy not configured, stop the robot !!");

            profile.setPreference("network.proxy.autoconfig_url", pac);
            profile.setPreference("network.proxy.type", 2);
        }

    }

    private boolean isAnonymous() {
        return Constants.ANONYMOUS_PROFILE == robotData.getProfileID();
    }


    protected void checkProfileConnected() {
        WebElement inboxLink = null;
        try {
            goToURL("https://mail.google.com/mail/?ui=html&zy=h");
            logger.debug("trying to find Inbox link ");
            inboxLink =findElementSafe(By.xpath("//a[contains(text(),'Inbox') or contains(text(), 'Contacts')]"), null, true);
        } catch (Exception e) {}
        if(inboxLink == null){
            WebElement basicBut = findElementSafe(By.xpath("//input[contains(@class,'maia-button-secondary')]"), null, true);
            if(basicBut != null){
                basicBut.click();
                logger.debug("Connected to HTML gmail -first time");
                return;
            }
            TimeUtils.waitFor(1500);
            throw new RuntimeException("Profile not connected, Robot will shutdown ");
        }
        logger.info("Inbox link {} is shown", inboxLink.getText());
    }



    private void addPlugins(FirefoxProfile profile) {
        if(Utils.isProdMode(mode)) {
            File file = Paths.get("ADDONS", "youtube_high_definition-50.0-fx+sm.xpi").toFile();
            profile.addExtension(file);
            profile.setPreference("extensions.youtubehighdefinition.currentversion", "50.0");
            profile.setPreference("extensions.youtubehighdefinition.currentvideoquality", "medium");
            profile.setPreference("extensions.youtubehighdefinition.currentvideosize", "default");
            profile.setPreference("extensions.youtubehighdefinition.enableautoexpanddescription", false);
            profile.setPreference("extensions.youtubehighdefinition.enablesuggestedautoplay", false);
            profile.setPreference("extensions.youtubehighdefinition.tbplaced", true);

            File killspinners = Paths.get("ADDONS", "killspinners-1.3.1-fx.xpi").toFile();
            profile.addExtension(killspinners);
            profile.setPreference("extensions.killspinners.timeout", 60);
        }
        logger.debug("end addons install");
    }



    protected void setPreferences(FirefoxProfile profile) {
        //network.prefetch-next :: Fetch only pages that you click
        profile.setPreference("network.prefetch-next", false); //default true
        profile.setPreference("network.http.response.timeout", 10); //default 300
        profile.setPreference("dom.max_script_run_time", 120); //default; in firefox : 10, in selenium : 30
        profile.setPreference("dom.max_chrome_script_run_time", 120); //default is 20
    }

    protected void resizeWindow(){
        this.driver.manage().window().maximize();
    }

    public void closeBrowser() {
        logger.debug("quit the browser");
        //see https://github.com/seleniumhq/selenium-google-code-issue-archive/issues/1402#issuecomment-191452880
        if(driver!=null){
            try {
                driver.quit();//close()
            }catch (Exception e){
                logger.error("cannot quit driver, nested exceotion is : {}", e);
                killFirefox();
            }
        }

    }
    @Retryable(value = {Exception.class}, maxAttempts = 2, backoff = @Backoff(delay = 2000))
    public void killFirefox() {
        logger.debug("killall firefox");
        try{
            if(this.firefoxProcessId != null) {
                logger.debug("firefox will be killed through process pid.");
                if(SystemUtils.IS_OS_WINDOWS){
                    CommandExecutor.execute("taskkill ","/F", "/PID", String.valueOf(this.firefoxProcessId));
                    //CommandExecutor.execute("taskkill ","/F", "/IM", "firefox.exe");
                }else{
                    CommandExecutor.execute("kill", "-9", String.valueOf(this.firefoxProcessId));
                    //CommandExecutor.execute("pkill","firefox");
                }
            } else {
                logger.error("Cannot terminate firefox by process id, process id is null!");
            }
        }catch (Exception ex){
            logger.error("can not kill firefox !");
            throw ex;
        }

    }




    public void goToURL(String url) throws ProblemLoadingPageException {
        if(driver!=null){
            long start = System.currentTimeMillis();
            driver.get("about:blank");
            driver.get(url);
            if("Problem loading page".equals(driver.getTitle())){
                throw new ProblemLoadingPageException("Problem loading page " +url);
            }else if ("404 Not Found".equals(driver.getTitle())){
                throw new ProblemLoadingPageException("Page not found " +url);
            }
            if(StringUtils.length(driver.getPageSource()) < 80){
                throw new ProblemLoadingPageException("Page doesn't contains much, probably it was not fully loaded or proxy server is down. " +url);
            }
            logger.info("loading page take {} ms", TimeUtils.countMSFrom(start));
            preProcessPage();
        }
    }

    private void preProcessPage() throws ProblemLoadingPageException{
        if(robotData.isFirefoxProxyEnable()){
            final String ipFromContext = fileManagerService.getPublicIpAdress();
            final String ipFromBrowser = executeJScriptFile("js/getIp.js");
            if(StringUtils.equals(ipFromBrowser, ipFromContext)){
                logger.error("Proxy;Page cannot be processed, reason : Firefox is not using proxy! ipFromContext : {}, ipFromBrowser: {}.",
                        ipFromContext, ipFromBrowser);
                throw new ProblemLoadingPageException("Proxy is not used.");
            } else if(StringUtils.equals("response_error", ipFromBrowser)){
                logger.error("Proxy;Cannot get ip, reason : Response returned from service is not OK (200).");
            } else if(StringUtils.startsWith(ipFromBrowser,"call_error")){
                logger.error("Proxy;Cannot get ip, reason : Page doesn't allow script to communicate with service. "+ipFromBrowser);
            }
        }
        executeJScriptFile("js/scroll2TeadsVideo.js");
    }

    public void preProcessFrame() {
        executeJScriptFile("js/cleanPage.js");
    }
    public boolean checkAdsenseByJS() {
        return Boolean.parseBoolean(executeJScriptFile("js/cleanPage.js"));
    }


    public String getCodeSourceOfCurrentFrame( ){
        return getCodeSourceOfCurrentFrame(true);
    }

    public String getCodeSourceOfCurrentFrame(boolean processRelativeToAbsoluteUrls ){
        if (processRelativeToAbsoluteUrls )
            relativeToAbsoluteUrls(true);
        String out="";
        tagSourceWithPixi();
        try {
            out = driver.getPageSource();
        }catch (Exception ex){
            TimeUtils.waitFor(2000);
            String get = "var generatedSource = new XMLSerializer().serializeToString(document); return generatedSource;";
            out = executeJScriptCanThrowsException(get);
        }
        out = out.replace("&amp;","&")
                .replace("&lt;","<")
                .replace("&gt;",">")
                .replace("&quote;","'")
                .replace("<a0:svg","<svg")
                .replace("</a0:svg","<svg")
                .replace("<a0:path","<path")
                .replace("</a0:path","</path")
                .replace("<a0:polyline","<polyline")
                .replace("</a0:polyline","</polyline")
                .replace("xmlns:a0=","xmlns:=")
                .replace("window.goog||{}","{}");
        return out;

    }

    private void tagSourceWithPixi() {
        executeJScriptFile("js/addPixiTag.js");
    }

    private void relativeToAbsoluteUrls(boolean retry ){
        try {
            executeJScriptFileCanThrowsException("js/relativeToAbsoluteUrls.js");
        }catch (Exception e){
            logger.error(e.getMessage(),e);
            if(retry){
                TimeUtils.waitFor(5000);
                relativeToAbsoluteUrls(false);
            }
        }
    }

    protected AdNetwork getNetwork(String originalURL, String urlDestination, String htmlCodeWholePage, AdNetwork parentAdNetwork) {
        try{
            if(parentAdNetwork == AdNetwork.ADSENSE) {
                return AdNetwork.ADSENSE;
            }

            if(RegExService.isAdsenseOriginalUrl(originalURL)){//JIRA 287
                return AdNetwork.ADSENSE;
            }

            if (RegExService.isAdsenseHtml(htmlCodeWholePage)){
                return AdNetwork.ADSENSE;
            }
            if(urlDestination == null)
                return AdNetwork.DIRECT;
            if (!urlDestination.contains("google_xml_addata")) {
                int i = 0;
                while (urlDestination.contains("%") && i < 5) {
                    urlDestination = Utils.unescapeDataString(urlDestination);
                    i++;
                }
            }
            if(RegExService.isAdsenseDestinationUrl(urlDestination)){
                return AdNetwork.ADSENSE;
            }
            if (urlDestination.contains("adroll") || urlDestination.contains("MEDIAMATH")
                    || urlDestination.contains("mathtag") || urlDestination.contains("solocpm") || urlDestination.contains("adbutter.net")) {
                return AdNetwork.BID; //bid
            }

            if ((urlDestination.contains("num=1") || urlDestination.contains("nx=")) && !urlDestination.contains("num=0")) {
                return (driver.getCurrentUrl().contains("bid.g.doubleclick.net")) ? AdNetwork.BID : AdNetwork.ADSENSE; //bid, else adsense
            }
            if (urlDestination.contains("num=0")) {
                return (!driver.getCurrentUrl().contains("bid.g.doubleclick.net") && !driver.getCurrentUrl().contains("utm_medium")) ? AdNetwork.DIRECT : AdNetwork.BID; //direct, else bid
            }
            return AdNetwork.DIRECT; //direct
        } catch (Exception e) {
            logger.error(e.getMessage(),e);
            logger.debug("get network for href {}", urlDestination);
            return AdNetwork.DIRECT; //direct
        }
    }

    //GET FINAL URL AFTER REDIRECTIONS
    public String getRealUrl(String url) {
        return getRealUrl(url,false);
    }

    public String getRealUrl(String url, boolean retry) {
        logger.debug("getting the real url of {}",url);
        try {
            if (url == null || "".equals(url)) {
                return "";
            }
            //DO NOT GET REAL URL OF BASE64 IMAGES
            if (url.startsWith("data:")) {
                return url;
            }
            if (!url.startsWith("http")) {
                if(url.startsWith("//")){
                    url = "http:" + url;
                }else{
                    url = "http://" + url;
                }

            }
            return  fileManagerService.getFinalURL( url);
        } catch (Exception e) {
            logger.error("Error getting final url {}",url,e);
            if (retry) {
                TimeUtils.waitFor(2000);
                url = getRealUrl(url,false);
            }
            return url;
        }
    }

    //SAFE WAY TO GET AN ELEMENT FROM IWEBELEMENT
    protected WebElement findElementSafe(By by, WebElement parent) {
        return   findElementSafe(by,  parent , false) ;
    }

    protected WebElement findElementSafe(By by, WebElement parent, boolean retry) {
        try {
            List<WebElement> elements = findElementsSafe(by, parent);

            if (elements!= null && elements.size() > 0) {
                return elements.get(0);
            }
            return null;
        } catch (Exception e) {
            logger.error(e.getMessage(),e);
            if (retry) {
                findElementSafe(by, parent, false);
            }
            return null;
        }
    }

    //SAFE WAY TO GET A COLLECTION OF ELEMENTS FROM IWEBELEMENT
    protected List<WebElement> findElementsSafe(By by, WebElement parent ) {
        try {
            if (parent != null) {
                return parent.findElements(by);
            } else {
                return driver.findElements(by);
            }
        } catch (Exception e) {
            logger.error(e.getMessage(),e);
            return null;
        }
    }

    //GET WALLPAPER LANDING PAGE
    public String getDestinationFromElement(WebElement backgroundElement) {
        String destinationUrl = "";
        try {
            WebElement parent = backgroundElement.findElement(By.xpath(".."));
            if ("a".equals(parent.getTagName()) && parent.getAttribute("href")!= null && !parent.getAttribute("href").toLowerCase().contains("javascript:")) {
                destinationUrl = getRealUrl(parent.getAttribute("href"));
            } else {
                destinationUrl = executeJScriptFileCanThrowsException("js/getDestinationFromElement.js" , backgroundElement);

                if (StringUtils.isBlank(destinationUrl)) {
                    try {
                        backgroundElement.click();
                    } catch (Exception e) {
                        try {
                            executeJScriptCanThrowsException("arguments[0].click();", backgroundElement);
                        } catch (Exception e1) {
                            logger.error("Cannot click on background element, nested exception : {}", e1);
                        }
                    }
                    destinationUrl = executeJScriptCanThrowsException("return eUrl;", backgroundElement);
                }
            }
            if(destinationUrl!=null){
                return getRealUrl(destinationUrl);
            }
            return "";
        } catch (Exception e) {
            logger.error(e.getMessage());
            return destinationUrl;
        }
    }

    //GET URL OF THE CURRENT IFRAME
    public String getCurrentFrameUrl() {
        return executeJScriptCanThrowsException("return window.location.href");
    }

    //EXECUTE JAVASCRIPT
    public String executeJScriptFile(String file, Object... parameters) {
        try {
            return executeJScriptFileCanThrowsException(file, parameters);
        } catch (Exception e) {
            logger.error("Can not run js file {}", file);
        }
        return "";
    }
    public String executeJScriptFileCanThrowsException(String file, Object... parameters) throws Exception{
        return executeJScript(fileManagerService.getFileContentAsString(file), parameters);
    }
    public String executeJScriptCanThrowsException(String script, Object... parameters) {
        return (String)((JavascriptExecutor)driver).executeScript(script, parameters);
    }
    public String executeJScript(String script, Object... parameters) {
        try {
            return executeJScriptCanThrowsException(script, parameters);
        } catch (Exception e) {
            logger.error("Can not run script {}", script);
        }
        return "";
    }



    public void setRobotData(Robot robotData) {
        this.robotData = robotData;
    }
    public Robot getRobotData() {
        return robotData;
    }
    public Integer getFirefoxProcessID() {
        return firefoxProcessId;
    }

    public String getDeviceId() {
        return robotData.getDeviceId()+"";
    }


    public void setFirefoxProfiles(String firefoxProfiles) {
        this.firefoxProfiles = firefoxProfiles;
    }


    protected String getCountry(){
        return robotData.getCountry();
    }

    public int getProfileID() {
        return robotData.getProfileID();
    }


    public void resetProfilesFolder(){
        fileManagerService.emptyProfilesFolder();
    }

    public void setMediaHelperAPIConnector(MediaHelperAPIConnector mediaHelperAPIConnector){
        this.mediaHelperAPIConnector = mediaHelperAPIConnector;
    }


    public FileManagerService getFileManagerService() {
        return fileManagerService;
    }

    public void setFileManagerService(FileManagerService fileManagerService) {
        this.fileManagerService = fileManagerService;
    }

    public DatabaseAPIConnector getDatabaseAPIConnector() {
        return databaseAPIConnector;
    }

    public void setDatabaseAPIConnector(DatabaseAPIConnector databaseAPIConnector) {
        this.databaseAPIConnector = databaseAPIConnector;
    }

    public AWSConnector getAwsConnector() {
        return awsConnector;
    }

    public void setAwsConnector(AWSConnector awsConnector) {
        this.awsConnector = awsConnector;
    }



    @Override
    public void destroy() throws Exception {
        logger.info("killing firefox");
        closeBrowser();
    }

    public int getTabsCount() {
        return driver.getWindowHandles().size();
    }


    //GET THE CHECKSUM OF A REMOTE FILE + FEW INFORMATION
    //RETURNS { IS_NEW_MEDIA , IS_NEW_VISUAL , ID_MEDIA_IF_NOT_NEW_MEDIA , , CHECKSUM_FILE , TEMPORARY_FILENAME_IN_MEDIA_FOLDER }
    public ChecksumFileResponse getChecksumFile(String url, boolean uploadIfNew) throws IOException {
        try {
            String tmpFilename = "tmp-"+fileManagerService.getChecksumText(url + (System.currentTimeMillis()/1000));
            String checksumFile = "";
            fileManagerService.downloadFile(url, tmpFilename);
            if (!url.startsWith("data")) {
                checksumFile = fileManagerService.getChecksumFileinMedia(tmpFilename);
            } else {
                checksumFile = fileManagerService.getChecksumText(url);
            }
            logger.debug("{} has the checksum value {} ", tmpFilename, checksumFile);
            long fileSize = fileManagerService.getFileSizeInMedia(tmpFilename);
            //CHECK IF MEDIA/VISUAL IS NEW AND UPLOAD IT
            if (uploadIfNew) {
                IsNewMediaResponse isNew = isNewMedia(checksumFile);
                fileManagerService.copyFileInMedia(tmpFilename,checksumFile);
                return new ChecksumFileResponse(isNew, checksumFile, tmpFilename, fileSize);
            } else {
                return new ChecksumFileResponse("0", "0", "0", "0", checksumFile, tmpFilename , fileSize, "0");
            }
        } catch (Exception e) {
            logger.error(e.getLocalizedMessage());
            throw e;
            //return new ChecksumFileResponse("0", "0", "0", "0", Utils.CHECKSUMEMPTY, "0");
        }
    }


    //CHECK IF A VISUAL EXISTS IN NOSQL DATABASE
    //RETURNS { IS_NEW_MEDIA , IS_NEW_VISUAL , ID_MEDIA_IF_NOT_NEW_MEDIA , ??? }
    public IsNewMediaResponse isNewMedia(String filename/*, String zone = ""*/) {
        try {
            VisualResponse visualResponse = databaseAPIConnector.getVisual(filename);

            if (visualResponse == null) {
                //New media and new visual
                return new IsNewMediaResponse( "1", "1", "0", "0", "0" );
            } else {
                Visual visual = visualResponse.getVisuals()
                        .stream()
                        .filter(v -> getCountry().equals(v.getCountry()) )
                        .findFirst()
                        .orElse(null);
                if (visual != null) {
                    //Visual and media exist
                    return  new IsNewMediaResponse( "0", "0", visualResponse.getIdMedia(), visual.getIdVisual(), visualResponse.getToBeFixed() );
                }
                //New visual only
                return new IsNewMediaResponse( "0", "1", visualResponse.getIdMedia(), "0", visualResponse.getToBeFixed() );
            }
        }catch (Exception e) {
            logger.error(e.getMessage(),e);
            return null;
        }
    }
    public IsNewMediaResponse isNewFbMedia(String filename, String idZone) {
        try {
            VisualResponse visualResponse = databaseAPIConnector.getVisual(filename);

            if (visualResponse == null) {
                //New media and new visual
                return new IsNewMediaResponse( "1", "1", "0", "0", "0");
            } else {
                Visual visual = visualResponse.getVisuals()
                        .stream()
                        .filter(v -> getCountry().equals(v.getCountry()) && idZone.equals(v.getZone()) )
                        .findFirst()
                        .orElse(null);
                if (visual != null) {
                    //Visual and media exist
                    return  new IsNewMediaResponse( "0", "0", visualResponse.getIdMedia(), visual.getIdVisual(), visualResponse.getToBeFixed() );
                }
                //New visual only
                return new IsNewMediaResponse( "0", "1", visualResponse.getIdMedia(), "0", visualResponse.getToBeFixed() );
            }
        }catch (Exception e) {
            logger.error(e.getMessage(),e);
            return null;
        }
    }
}
