package ma.labs.bot.core;
/********************************************************
 *                                                      *
 *                    PIXITREND ROBOT                   *
 *                    BY labs GROUP                    *
 *                                                      *
 ********************************************************/

import ma.labs.bot.data.AdNetwork;
import ma.labs.bot.data.util.MediaType;
import ma.labs.bot.rules.RegExService;
import ma.labs.bot.utils.AdobeEdgeHelper;
import ma.labs.bot.utils.Constants;
import ma.labs.bot.utils.TimeUtils;
import ma.labs.bot.utils.Utils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.openqa.selenium.*;
import org.openqa.selenium.interactions.Actions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by labs004 on 18/07/2016.
 */

@Component("scraper")
public class Scraper extends BasicScraper implements IScraper {

    private static Logger LOGGER = LoggerFactory.getLogger(Scraper.class);

    private String youtubeHomePageId;
    private String youtubeSideBarPageId;

    @Value("${toBefixed.enabled}")
    protected boolean isToBeFixedEnabled;

    public static final String FB_FOLDER_SUFFIX = "-folder";
    //FB vars
    private List<String> fbAdsTimeline    = new ArrayList<>();
    private List<String> fbAdsRightColumn = new ArrayList<>();
    protected List<Integer> processedAds  = new ArrayList<>();
    protected   int lastpositionYTimeline = 0;
    private  int lastpositionYRightColumn = 0;
    protected int xPosition = 0;

    @Override
    public void extractAdsPage(String idExtraction, String idPage){
        try {
            driver.switchTo().defaultContent();
            offsetX = 0;
            offsetY = 0;
        }catch (Exception e){
            LOGGER.error(e.getLocalizedMessage());
            try{
                driver.switchTo().alert().accept();
                driver.switchTo().defaultContent();
            }catch (Exception ex){
                LOGGER.error(ex.getLocalizedMessage());
            }
        }
        try {

            if(isScreenshotON){
                TimeUtils.waitFor(500);
                String screen = "screen-"+idExtraction+ Constants.JPG;
                File file = guiHelper.captureScreenShot(driver,null,fileManagerService.getMediaPath()+"/"+screen);
                ftpConnector.upload(file);
            }

            String hTMLCodeWholePage = getCodeSourceOfCurrentFrame(false);
            extractWebAds(idExtraction, idPage, hTMLCodeWholePage, true, null); //Extract first level ads

            for (WebElement iframe ://Extract from all iframes except the ones referenced (like plugins, ...)
                    driver.findElements(By.xpath(iFrameSelector))) {
                extractFromIframe(iframe, idExtraction, idPage, null);
                driver.switchTo().defaultContent();
                LOGGER.info("</iframe>");
                offsetX = 0;
                offsetY = 0;

            }
            fileNames.clear();

        } catch (Exception e) {
            fileNames.clear();
            LOGGER.error(e.getLocalizedMessage(),e);
        }
    }

    //EXTRACT ADS IN FROM CURRENT
    private void extractWebAds(String idExtraction, String idPage, String HTMLCodeWholePage, Boolean isFirstLevel, AdNetwork parentAdNetwork){
        LOGGER.info("extracting WebAds firstLevel = {}", isFirstLevel);
        String urlAdMedia ,
                fileAdMedia ,
                destinationURL , duration ;
        MediaType typeAdMedia ;
        AdNetwork network= AdNetwork.DIRECT;
        boolean uploadToS3 =true;
        long fileSize = 0;
        IsNewMediaResponse isNew ;
        List<WebElement> elements;
        try {
            elements = driver.findElements(By.xpath(possibleMedias));
        }catch (Exception e){
            LOGGER.error(e.getLocalizedMessage());
            TimeUtils.waitFor(5000);
            elements = driver.findElements(By.xpath(possibleMedias));
        }

        //If not first level, check if it can be HTML5
        Boolean considerAsHTML5 = false;
        if (!isFirstLevel) {
            int countBigElements = 0;
            countBigElements = getCountBigElements(elements, countBigElements);
            String body = driver.findElement(By.tagName("body")).getText();
            if (countBigElements > 1 || body.length() > 25) {
                LOGGER.debug("got countBigElements: {} and body :{}...", countBigElements, body.substring(0,24));
                considerAsHTML5 = true;
                extractHtml5StandardAd(idExtraction, idPage, HTMLCodeWholePage.replace("&amp;", "&").replace("&lt;", "<").replace("&gt;", ">"),"", parentAdNetwork);
            }
        }
        if (isFirstLevel || !considerAsHTML5) {
            for (WebElement originalElement : elements) {
                try {
                    WebElement element = originalElement;
                    if (!"script".equals(element.getTagName()) && (element.getSize().getHeight() > 3 && element.getSize().getWidth() > 3)) {
                        //Avoid tracking pixel trackers and icons
                        //Reset variables
                        urlAdMedia = fileAdMedia =  destinationURL = duration = "";
                        typeAdMedia = null;
                        uploadToS3 = true;
                        isNew = new IsNewMediaResponse( "1", "1", "1", "0", "0" );
                        Dimension eltSize = element.getSize();
                        Point eltLocation = element.getLocation();
                        //IFRAME VIDEO
                        if ("iframe".equals(element.getTagName())) {
                            LOGGER.info("{} extract video from iframe ", idExtraction);
                            typeAdMedia = MediaType.VIDEO;
                            network = AdNetwork.DIRECT;
                            urlAdMedia = element.getAttribute("src");
                            if (urlAdMedia == null || "".equals(urlAdMedia)) {
                                urlAdMedia = "";
                            }
                            String youtubeId ="";
                            LOGGER.debug("getting youtubeId from {}", urlAdMedia);
                            if(urlAdMedia.contains("youtube.com")){
                                youtubeId = RegExService.getYoutubeIdFromUrl(urlAdMedia);
                            }else if(urlAdMedia.contains("vid-v2")){
                               youtubeId = Utils.parseQueryString(urlAdMedia).get("vidId") ;
                            }
                            LOGGER.info("Youtube Id {}", youtubeId);
                            if(StringUtils.isBlank(youtubeId)){
                                continue;
                            }
                            MediaHelperAPIResponse mediaHelperAPIResponse = mediaHelperAPIConnector.downloadByYoutubeId(youtubeId);
                            if(mediaHelperAPIResponse != null){
                                fileAdMedia = mediaHelperAPIResponse.getChecksum();
                                isNew = isNewMedia(mediaHelperAPIResponse.getChecksum());
                                if (isNew.isNewMedia() || isNew.isNewVisual()) {
                                    if(isNew.isNewMedia()){
                                        fileSize = mediaHelperAPIResponse.getFileSize();
                                        duration = String.valueOf(mediaHelperAPIResponse.getDuration());
                                        mediaHelperAPIConnector.uploadByYoutubeID(youtubeId);
                                        uploadToS3 = false;
                                    }
                                    destinationURL = "";
                                }

                            }

                        }
                        //#IMAGES
                        if ("img".equals(element.getTagName())) {
                            LOGGER.info("{} extract image ", idExtraction);
                            if (element.getSize().getHeight() < 40 && element.getSize().getWidth() < 40){
                                LOGGER.debug("it's a small image, we don't need to process it");
                                continue;
                            }
                            typeAdMedia = MediaType.IMAGE;
                            urlAdMedia = element.getAttribute("src");
                            if (urlAdMedia == null || "".equals(urlAdMedia)) {
                                urlAdMedia = "";
                            }
                            ChecksumFileResponse checksum = getChecksumFile(urlAdMedia, true);
                            fileAdMedia = checksum.getChecksumFile();
                            fileSize = checksum.getFileSize();
                            isNew = new IsNewMediaResponse(checksum.getIsNewMedia(), checksum.getIsNewVisual(), checksum.getIdMedia(), checksum.getIdVisual(), checksum.getToBeFixed() );
                            WebElement parent = element.findElement(By.xpath(".."));
                            if (!"a".equals(parent.getTagName())) {
                                parent = parent.findElement(By.xpath(".."));
                            }
                            if ("a".equals(parent.getTagName())) {
                                String hrefImage = parent.getAttribute("href");
                                network = getNetwork(urlAdMedia,hrefImage, HTMLCodeWholePage, parentAdNetwork);
                                if (checksum.isNewMedia() || checksum.isNewVisual()) {
                                    if(checksum.isNewMedia()){
                                        urlAdMedia = getRealUrl(urlAdMedia);
                                    }
                                    destinationURL = getDestinationURL(hrefImage);
                                }
                            }
                        }
                        //END IMAGES
                        //#VIDEO
                        if ("video".equals(element.getTagName()) && isNotYoutube(idPage)) {
                            LOGGER.info("{} extract video ", idExtraction);
                            typeAdMedia = MediaType.VIDEO;
                            urlAdMedia = element.getAttribute("src");
                            if (urlAdMedia == null || "".equals(urlAdMedia)) {
                                urlAdMedia = "";
                            }
                            MediaHelperAPIResponse mediaHelperAPIResponse = mediaHelperAPIConnector.downloadByFileUrl(urlAdMedia);
                            if(mediaHelperAPIResponse == null){
                                continue;//skip this element
                            }
                            fileAdMedia = mediaHelperAPIResponse.getChecksum();
                            isNew = isNewMedia(mediaHelperAPIResponse.getChecksum());
                            WebElement parent = element.findElement(By.xpath(".."));
                            if (!"a".equals(parent.getTagName())) {
                                parent = parent.findElement(By.xpath(".."));
                            }
                            if ("a".equals(parent.getTagName())) {
                                String hrefImage = parent.getAttribute("href");
                                network = getNetwork(urlAdMedia, hrefImage, HTMLCodeWholePage, parentAdNetwork);
                                if (isNew.isNewMedia() || isNew.isNewVisual()) {
                                    if(isNew.isNewMedia()){
                                        urlAdMedia = getRealUrl(urlAdMedia);
                                        fileSize = mediaHelperAPIResponse.getFileSize();
                                        duration = String.valueOf(mediaHelperAPIResponse.getDuration());
                                        mediaHelperAPIConnector.uploadByFileUrl(urlAdMedia);
                                        uploadToS3 = false;
                                    }
                                    destinationURL = getDestinationURL(hrefImage);
                                }
                            }
                        }
                        //END VIDEO
                        //#FLASH
                        //FLASH WITH EMBED TAG
                        else if ("embed".equals(element.getTagName())) {
                            LOGGER.info("{} extract flash ", idExtraction);
                            typeAdMedia = MediaType.FLASH;
                            urlAdMedia = element.getAttribute("src");
                            ChecksumFileResponse checksum = getChecksumFile(urlAdMedia, true);
                            fileAdMedia = checksum.getChecksumFile();
                            fileSize = checksum.getFileSize();
                            isNew = new IsNewMediaResponse( checksum.getIsNewMedia(), checksum.getIsNewVisual(), checksum.getIdMedia(), checksum.getIdVisual(), checksum.getToBeFixed() );
                            String destinationFlashRaw = "";
                            if (urlAdMedia.toLowerCase().contains("clicktag")) {
                                destinationFlashRaw = Utils.unescapeDataString(RegExService.findFirst("(?i)(?<=clicktag[?=]).*",urlAdMedia,true,0));
                            } else {
                                destinationFlashRaw = element.getAttribute("flashvars");
                            }
                            network = getNetwork(urlAdMedia, destinationFlashRaw,HTMLCodeWholePage, parentAdNetwork);
                            if (checksum.isNewMedia()) {//not error and new visual
                                destinationURL = getDestinationURL(destinationFlashRaw);
                                urlAdMedia = getRealUrl(urlAdMedia);
                            } else  if (checksum.isNewVisual()) {//not error and new visual
                                destinationURL = getDestinationURL(destinationFlashRaw);
                            }

                        }
                        //FLASH WITH OBJET/PARAM TAG
                        else if ("object".equals(element.getTagName()) ||  "param".equals(element.getTagName())) { //object and others
                            LOGGER.info("{} extract falsh/object ", idExtraction);
                            typeAdMedia = MediaType.FLASH;
                            if ("param".equals(element.getTagName())) {
                                element = element.findElement(By.xpath(".."));
                            }
                            String flashVars = element.getAttribute("flashvars");
                            if (flashVars != null && !"".equals(flashVars)) { //object inline vars
                                urlAdMedia = element.getAttribute("data");
                                ChecksumFileResponse checksum = getChecksumFile(urlAdMedia, true);
                                fileSize = checksum.getFileSize();
                                isNew = new IsNewMediaResponse( checksum.getIsNewMedia(), checksum.getIsNewVisual(), checksum.getIdMedia(), checksum.getIdVisual(), checksum.getToBeFixed() );
                                fileAdMedia = checksum.getChecksumFile();
                                network = getNetwork(urlAdMedia, flashVars,HTMLCodeWholePage, parentAdNetwork);
                                if (checksum.isNewMedia()) {//not error and new media
                                    destinationURL = getDestinationURL(flashVars);
                                    urlAdMedia = getRealUrl(urlAdMedia);
                                }else if (checksum.isNewVisual()) {//not error and new visual
                                    destinationURL = getDestinationURL(flashVars);
                                }

                            } else {//object param vars
                                try {
                                    urlAdMedia = element.getAttribute("data");
                                    ChecksumFileResponse checksum;
                                    //IF IS VIDEO PLAYER
                                    if (urlAdMedia.contains("sas-video-player-")) {
                                        typeAdMedia = MediaType.VIDEO;
                                        flashVars = element.findElement(By.xpath(".//param[@name[contains(translate(., 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz'),'flashvars')]]")).getAttribute("value");
                                        urlAdMedia = RegExService.findFirst("(?i)(?<=videoFile[?=])(.*?)(?=&)",flashVars,false,0);
                                        checksum = getChecksumFile(urlAdMedia, true);
                                    } else if (urlAdMedia.contains("sascdn.com")
                                            && urlAdMedia.contains("standalone-player")) {
                                        typeAdMedia = MediaType.VIDEO;
                                        flashVars = element.findElement(By.xpath(".//param[@name[contains(translate(., 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz'),'flashvars')]]")).getAttribute("value");
                                        urlAdMedia = Utils.unescapeDataString(RegExService.findFirst("(?i)(?<=url[?=])(.*?)(?=&)",flashVars,false,0));
                                        checksum = getChecksumFile(urlAdMedia, true);
                                    } else {
                                        checksum = getChecksumFile(urlAdMedia, true);
                                    }
                                    fileSize = checksum.getFileSize();
                                    fileAdMedia = checksum.getChecksumFile();
                                    fileSize = checksum.getFileSize();
                                    isNew = new IsNewMediaResponse( checksum.getIsNewMedia(), checksum.getIsNewVisual(), checksum.getIdMedia(), checksum.getIdVisual(), checksum.getToBeFixed() );
                                    try {
                                        try {
                                            flashVars = element.findElement(By.xpath(".//param[@name[contains(translate(., 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz'),'flashvars')]]")).getAttribute("value");
                                        }
                                        catch (Exception e) { }
                                        network = getNetwork(urlAdMedia, flashVars,HTMLCodeWholePage, parentAdNetwork);
                                        if (checksum .isNewMedia() || checksum.isNewVisual()) {
                                            if(checksum.isNewMedia()){
                                                urlAdMedia = getRealUrl(urlAdMedia);
                                                if ("".equals(urlAdMedia)) {
                                                    try {
                                                        urlAdMedia = getRealUrl(element.findElement(By.xpath(".//param[@name='movie']")).getAttribute("value"));
                                                    } catch (Exception e) {
                                                        LOGGER.error(e.getMessage(),e);
                                                    }
                                                }
                                            }
                                            destinationURL = getDestinationURL(flashVars);
                                        }

                                    } catch (Exception e) {
                                        LOGGER.error(e.getMessage(),e);
                                    }

                                } catch (Exception e) {
                                    LOGGER.error(e.getMessage(),e);
                                }
                            }
                        }
                        //END FLASH
                        //#DIVS (BACKGROUND IMAGES)
                        else if ("div".equals(element.getTagName())) {
                            //Case ChoufTV Video
                            if("youtube".equals(element.getAttribute("class"))){
                                LOGGER.info("{} extract Video from ChoufTV DIV ", idExtraction);
                                typeAdMedia = MediaType.VIDEO;
                                network = AdNetwork.DIRECT;
                                urlAdMedia ="https://www.youtube.com/watch?v=";
                                String youtubeId =element.getAttribute("id");
                                LOGGER.debug("getting youtubeId from {}", urlAdMedia);

                                LOGGER.info("Youtube Id {}", youtubeId);
                                if(StringUtils.isBlank(youtubeId)){
                                    continue;
                                }
                                urlAdMedia +=youtubeId;
                                MediaHelperAPIResponse mediaHelperAPIResponse = mediaHelperAPIConnector.downloadByYoutubeId(youtubeId);
                                if(mediaHelperAPIResponse != null){
                                    fileAdMedia = mediaHelperAPIResponse.getChecksum();
                                    isNew = isNewMedia(mediaHelperAPIResponse.getChecksum());
                                    if (isNew.isNewMedia() || isNew.isNewVisual()) {
                                        if(isNew.isNewMedia()){
                                            fileSize = mediaHelperAPIResponse.getFileSize();
                                            duration = String.valueOf(mediaHelperAPIResponse.getDuration());
                                            mediaHelperAPIConnector.uploadByYoutubeID(youtubeId);
                                            uploadToS3 = false;
                                        }
                                        destinationURL = "";
                                    }
                                }

                            }else {
                                LOGGER.info("{} extract BACKGROUND IMAGES ", idExtraction);
                                typeAdMedia = MediaType.IMAGE;
                                String backgroundImage = element.getCssValue("background-image");
                                if (!"none".equals(backgroundImage)) {
                                    urlAdMedia = backgroundImage;
                                    if (urlAdMedia.startsWith("url")) {
                                        urlAdMedia = RegExService.findFirst("url\\(['|\"]?(.*?)['|\"]?\\)",urlAdMedia,false,1);
                                    }
                                    ChecksumFileResponse checksum = getChecksumFile(urlAdMedia, true);
                                    fileAdMedia = checksum.getChecksumFile();
                                    fileSize = checksum.getFileSize();
                                    isNew = new IsNewMediaResponse( checksum.getIsNewMedia(), checksum.getIsNewVisual(), checksum.getIdMedia(), checksum.getIdVisual(), checksum.getToBeFixed() );
                                    if (checksum.isNewMedia() || checksum.isNewVisual()) {
                                        if(checksum.isNewMedia()){
                                            urlAdMedia = getRealUrl(urlAdMedia);
                                        }
                                        destinationURL = getDestinationFromElement(element);
                                    }

                                }
                            }

                        }
                        //END DIVS (BACKGROUND IMAGES)


                        //IF #JOOMLA AD AGENCY
                        if (urlAdMedia.contains("stories/ad_agency")
                                && (destinationURL != null || !"".equals(destinationURL))) {
                            destinationURL = getDestinationURLJoomlaAdAgency(element);
                        }
                        if (null!=(typeAdMedia) && !"".equals(urlAdMedia)) {//if DIV, if no image, do not insert
                            fileNames.add(fileAdMedia);
                            databaseAPIConnector.insertImpression(getRobotData(),idExtraction, typeAdMedia, "", network,
                                    fileAdMedia, urlAdMedia, destinationURL, "null", idPage, sizeScreen,
                                    eltSize, new Point(eltLocation.getX() + offsetX, eltLocation.getY() + offsetY),
                                    duration, isNew, fileSize);
                            if(uploadToS3 && ((isNew.isNewMedia()|| (isNew.isToBeFixed() && isToBeFixedEnabled)))){
                                awsConnector.upload(fileAdMedia,typeAdMedia,true);
                            }
                        }

                    }

                }catch (Exception e) {
                   LOGGER.error(e.getLocalizedMessage(),e);
                }

            }
        }
    }

    private int getCountBigElements(List<WebElement> elements, int countBigElements) {
        for (WebElement element : elements) {
            Dimension size = element.getSize();
            if (size.getHeight() > 30 || size.getWidth() > 30) {
                countBigElements++;
                if(countBigElements>1)
                    break;
            }
        }
        return countBigElements;
    }

    private boolean isNotYoutube(String idPage) {
        boolean isYoutube = youtubeHomePageId.equals(idPage) || youtubeSideBarPageId.equals(idPage);
        return !isYoutube;
    }


    //EXTRACT ADS FROM IFRAME
    public void extractFromIframe(WebElement node, String idExtraction, String idPage, AdNetwork parentAdNetwork){
        LOGGER.info("<iframe>");
        // this bloc is for catching  StaleElementReferenceException
        try {
            node.getLocation();
        }catch (StaleElementReferenceException e){
            LOGGER.error("got a StaleElementReferenceException  {}", e.getLocalizedMessage());
        }
        Point location = node.getLocation();
        offsetX += location.getX();
        offsetY += location.getY();
        //LOGGER.debug("src : {}", node.getAttribute("src"));//OriginalUrl
        lastFrameSize = node.getSize();
        //TODO : find an other way to skip iframe for non ADS
        if (lastFrameSize.getHeight() < 5 || lastFrameSize.getWidth() < 5){
            framesUrl.push(node.getAttribute("src"));//to keep framesUrl coherent
            //select the frame, bacause we'll do gotoParent after
            driver.switchTo().frame(node);
            return; //Skip invisible iframes
        }


        framesUrl.push(node.getAttribute("src"));
        driver.switchTo().frame(node);
        if(Utils.isDevMode(mode)){
            TimeUtils.waitFor(3000);
        }
        String htmlCodeWholePage = getCodeSourceOfCurrentFrame(true);
        AdNetwork adNetwork = checkAdsenseByJS()?AdNetwork.ADSENSE:null;
        preProcessFrame();

        //check if the iframe contains an other iframe with big size,
        // that mean we're in the parent and we must ignore it and enter to children
        boolean bigChildFound = false;
        List<WebElement> children = driver.findElements(By.xpath(iFrameSelector));
        for (WebElement child : children) {
            double percent = 0.5;
            Dimension childSize = child.getSize();
            if(childSize.getWidth()>=(percent*lastFrameSize.getWidth())
                    && childSize.getHeight()>=(percent*lastFrameSize.getHeight())){
                bigChildFound=true;
                //check if the iframe contains adsense : for test
                if (adNetwork==null && RegExService.isAdsenseHtml(htmlCodeWholePage)){
                    LOGGER.info(">>>>> I'm an ADSENSE iframe");
                    adNetwork = AdNetwork.ADSENSE;
                }
                break;
            }
        }
        if (!bigChildFound){
                Boolean isHTML5 ;
            try {
                //IF NOT IN IGNORED IFRAMES (TODO)
                //THIS IS DONE IN YOUTUBE, BUT SHOULD BE DONE FOR OTHER HTML5 ADS
                htmlCodeWholePage = htmlCodeWholePage.replace("gwd_lib.js", "");
                //THIS REMOVE DUPLICATES ANITMATION WITHIN THE AD
                htmlCodeWholePage =Utils.replaceLast(htmlCodeWholePage,"</body>", "<script type='text/javascript'>function removeById(id){if (document.getElementById(id) != undefined) document.getElementById(id).outerHTML='';} function removeDuplicated(id){ var ids = document.querySelectorAll(id), len = ids.length, n; if(len < 2){return;} for(n = 0; n < len-1; n++){ if(ids[n]){ ids[n].parentElement.removeChild(ids[n]);}}} removeDuplicated('#adContent'); removeDuplicated(\"[id*='google_ads_iframe_']\"); removeById('abgb'); removeById('cbc'); </script> </body>");
                //TRY HTML5 FIRST
                isHTML5 = extractHTML5GoogleWebDesigner(idExtraction, idPage, htmlCodeWholePage, parentAdNetwork);
                if (!isHTML5)
                    isHTML5 = extractHTML5GoogleAds(idExtraction, idPage, htmlCodeWholePage, parentAdNetwork);
                if (!isHTML5)
                    isHTML5 = extractHTML5AdobeEdge(idExtraction, idPage, htmlCodeWholePage, parentAdNetwork);
                if (!isHTML5)
                    isHTML5 = extractHTML5AdButter(idExtraction, idPage, htmlCodeWholePage, parentAdNetwork);
                if (!isHTML5)
                    isHTML5 = extractHTML5ServingSys(idExtraction, idPage, htmlCodeWholePage, parentAdNetwork);
                if (!isHTML5)
                    isHTML5 = extractHTML5BannerFlow(idExtraction, idPage, htmlCodeWholePage, parentAdNetwork);
                if (!isHTML5)
                    isHTML5 = extractHTML5TUMULTHYPE(idExtraction, idPage, htmlCodeWholePage, parentAdNetwork);
                if (!isHTML5)
                    isHTML5 = extractHTML5CreateJs(idExtraction, idPage, parentAdNetwork);
                if (!isHTML5)
                    isHTML5 = extractHTML5GoogleHybrid(idExtraction, idPage, htmlCodeWholePage, parentAdNetwork);

                //ONLY EXTRACT ADS IF NOT HTML5
                if (!isHTML5) {
                    extractWebAds(idExtraction, idPage, htmlCodeWholePage, false, parentAdNetwork); //Extract ads before going deeper
                }
            } catch (Exception e) {
                LOGGER.error(e.getLocalizedMessage());
            }
        }
        for (WebElement child : children) {
            extractFromIframe(child, idExtraction, idPage, adNetwork);
            offsetX -= driver.manage().window().getPosition().getX();
            offsetY -= driver.manage().window().getPosition().getY();
            driver.switchTo().parentFrame();
            LOGGER.info("</iframe>", idExtraction, idPage);
            framesUrl.pop();
        }
    }

    private String swipedGalleryGWDAdsProcessing(String htmlCodeWholePage){
        final long nw = System.currentTimeMillis();
        if(StringUtils.isBlank(htmlCodeWholePage)){
            return htmlCodeWholePage;
        }
        final List<WebElement> gwdSwipeGallery = findElementsSafe(By.xpath("//gwd-swipegallery"), null);
        final String errorMessage = "there is no import of gwdswipegallery_min.js in this gwd ad. " +
                "i won't search it's images attribute and change it to base64 separated by # instead of , .";

        if(gwdSwipeGallery == null || gwdSwipeGallery.isEmpty()){
            LOGGER.debug("this gwd ad doesn't contain a gwd-swipegallery tag");
            return htmlCodeWholePage;
        }
        final String regexBySrcSwipeGallery = "<script.*gwdswipegallery_min\\.js.*?<\\/script>";
        String swipeGalleryJsInHtml = RegExService.findFirst(regexBySrcSwipeGallery, htmlCodeWholePage, true, 0);
        String out = htmlCodeWholePage;
        if(!StringUtils.isBlank(swipeGalleryJsInHtml)){
            try {
                swipeGalleryJsInHtml = swipeGalleryJsInHtml.substring(swipeGalleryJsInHtml.lastIndexOf("<script"),
                        swipeGalleryJsInHtml.length());
            } catch (StringIndexOutOfBoundsException e) {
                LOGGER.error("error occured : {}, so i will abort swipedGalleryGWDAdsProcessing.", e);
                swipeGalleryJsInHtml = "";
            }
            if(StringUtils.isBlank(swipeGalleryJsInHtml)){
                LOGGER.debug(errorMessage);
                return htmlCodeWholePage;
            }
            final String swipeGalleryJsSrc = RegExService.findFirst("src\\s*=\\s*['\"](.*?)['\"]",
                    swipeGalleryJsInHtml, true, 1);
            if(StringUtils.isBlank(swipeGalleryJsInHtml) || StringUtils.isBlank(swipeGalleryJsSrc)){
                LOGGER.debug(errorMessage);
                return htmlCodeWholePage;
            }
            InputStream in = null;
            try {
                in = new java.net.URL(swipeGalleryJsSrc).openStream();
                final String jsContent = IOUtils.toString(in, Charset.defaultCharset());
                if(StringUtils.isBlank(jsContent)){
                    LOGGER.error("i was unable to get jsContent from src : {}.", swipeGalleryJsSrc);
                    return htmlCodeWholePage;
                }
                final String replacement = "<script pixi=\"true\">" + jsContent.replaceFirst("split\\(\",\"\\)", "split(\"#\")") + "</script>";

                out = htmlCodeWholePage.replace(swipeGalleryJsInHtml, replacement);
            } catch (IOException ex){
                LOGGER.error("error during swipedGalleryGWDAdsProcessing, exception : {}", ex);
            } finally {
                IOUtils.closeQuietly(in);
            }
        } else {
            final String regexInSourceSwipeGallery = "(?s)<script.*?gwd-swipegallery(.*?)<\\/script>";
            String swipeGalleryJsInHtmlBySrc = RegExService.findFirst(regexInSourceSwipeGallery, htmlCodeWholePage, true, 1);
            if(StringUtils.isBlank(swipeGalleryJsInHtmlBySrc)){
                LOGGER.debug(errorMessage);
                return htmlCodeWholePage;
            } else {
                out = htmlCodeWholePage.replace(swipeGalleryJsInHtmlBySrc, swipeGalleryJsInHtmlBySrc.replaceFirst("split\\(\",\"\\)", "split(\"#\")"));
            }
        }
        final List<String> swipeGalleryTags = RegExService.getMatches("<gwd-swipegallery\\s*.*?images\\s*=['\"](.*?)['\"]", htmlCodeWholePage, 0);
        for(String gallery : swipeGalleryTags){
            final String imagesAttribute = RegExService.findFirst("images\\s*=['\"](.*?)['\"]", gallery, true, 1);
            if(!StringUtils.isBlank(imagesAttribute)){
                final List<String> images = Arrays.asList(imagesAttribute.split(","));
                final Iterator<String> imgs=images.iterator();
                String replac = "";

                while(imgs.hasNext()){
                    final String img = imgs.next();
                    try {
                        final String exec = executeJScriptFileCanThrowsException("js/pixiGwdSwpieGallery.js", img);
                        replac += (exec+ (imgs.hasNext()?"#":""));
                    } catch (Exception e) {
                        LOGGER.error("error during swipedGalleryGWDAdsProcessing, cannot get absoluteUrl for image {}. error is {}.", img, e);
                    }
                }
                out = out.replace(imagesAttribute,replac);
            }
        }
        final List<String> galleryNav = RegExService.getMatches("<gwd-gallerynavigation.*?(<div.*?)<\\/gwd-gallerynavigation>", out, 1);
        if(galleryNav != null && !galleryNav.isEmpty()){
            for(String nav : galleryNav){
                out = out.replace(nav, "");
            }
        }
        LOGGER.debug("took to swipedGalleryGWDAdsProcessing {} ms.", TimeUtils.countMSFrom(nw));
        return out;
    }
    //EXTRACT HTML5 GOOGLE WEB DESIGNER ADS
    public Boolean extractHTML5GoogleWebDesigner(String idExtraction, String idPage, String htmlCodeWholePage, AdNetwork parentAdNetwork) {
        Boolean isHTML5 = false;
        //HTML5 Google Web Designer
        //htmlCodeWholePage = deleteSrcAndStyleAttributesFromGwdAd(htmlCodeWholePage);
        try {
            if (htmlCodeWholePage.contains("<gwd-")) {
                LOGGER.info("I find a gwd ad ");
                isHTML5 = true;
                List<WebElement> elements = null;
                int nbrOfElements = 0;
                int count = 0;
                while (nbrOfElements < 1 && count < 5) {//TRY 5 TIMES (5 X 3 SECONDS = 15 SECONDS) BEFORE FAIL
                    elements = driver.findElements(By.xpath("//img[starts-with(@id,'gwd')]|//img[contains(@class,'gwd')]"));
                    nbrOfElements = elements.size();
                    if (nbrOfElements == 0)
                        TimeUtils.waitFor(2000); //ONLY WAIT IF NO ELEMENTS FOUND (AVOID WAITING FIRST TIME)
                    count++;
                }
                if (elements.size() > 0) {
                    String destinationURL = "";
                    GetChecksumOfAllImagesOnPageResp resp = getChecksumOfAllImagesOnPage("//img[starts-with(@id,'gwd')]|//img[contains(@class,'gwd')]|//img");
                    htmlCodeWholePage = swipedGalleryGWDAdsProcessing(htmlCodeWholePage);
                    String html5ElementChecksum = resp.getHtml5ElementChecksum();

                    AdNetwork network =getHTML5Network(getCurrentFrameUrl(), htmlCodeWholePage, parentAdNetwork);
                    MediaType typeAdMedia = MediaType.HTML5;
                    IsNewMediaResponse isNew = isNewMedia(html5ElementChecksum);
                    long fileSize =-1;
                    if ( isNew.isNewMedia() || isNew.isNewVisual()) {
                        if(isNew.isNewMedia() || (isNew.isToBeFixed() && isToBeFixedEnabled)){
                            htmlCodeWholePage = htmlCodeWholePage
                                    .replace("googbase_min","")
                                    .replace("paused !important;", "running !important");
                            htmlCodeWholePage = fileManagerService.replaceImagesWithBase64(htmlCodeWholePage, resp.getImages2Convert64());
                            fileManagerService.createFileInMedia(html5ElementChecksum, htmlCodeWholePage);
                            fileSize = fileManagerService.getFileSizeInMedia(html5ElementChecksum);
                            awsConnector.upload(html5ElementChecksum,typeAdMedia,true);
                        }
                        //DESTINATION URL
                        try {
                            destinationURL = getRealUrl(RegExService.findFirst("\"url\":\"(.*?)\"",htmlCodeWholePage,false,1)) ;
                            if ("".equals(destinationURL))
                                destinationURL = getRealUrl(RegExService.getGADestinationUrl(htmlCodeWholePage));
                            if ( "".equals(destinationURL))
                                destinationURL = findElementSafe(By.xpath("//gwd-exit"),null, false).getAttribute("url");
//                            if ( "".equals(destinationURL))
//                                destinationURL = getDestinationFromElement(driver.findElement(By.xpath("//gwd-taparea")));
                        } catch (Exception e) { }
                    }
                    databaseAPIConnector.insertImpression(getRobotData(),idExtraction, typeAdMedia
                            , "", network, html5ElementChecksum, getCurrentFrameUrl(), destinationURL, "null"
                            , idPage,sizeScreen, lastFrameSize, new Point(offsetX, offsetY), "", isNew, fileSize);
                    htmlCodeWholePage=null;
                }
            }
        } catch (Exception e) {
            LOGGER.error(e.getMessage(),e);
        }
        return isHTML5;
    }

    //EXTRACT HTML5 GOOGLE ADS (Swiffy and others)
    public Boolean extractHTML5GoogleAds(String idExtraction, String idPage, String htmlCodeWholePage, AdNetwork parentAdNetwork) {
        Boolean isHTML5 = false;
        //HTML5 Google SWIFFY
        try {
            String swiffyobject ;
            String adData ;
            String destinationUrl = "";
            String html5ElementChecksum ;
            GetChecksumOfAllImagesOnPageResp resp=null;
            try {
                //SWIFFY
                swiffyobject = executeJScriptCanThrowsException("return JSON.stringify(swiffyobject);");
                isHTML5 = true;
                LOGGER.info("I find a swiffyobject ad ");
                //MatchCollection HTML5Elements = Regex.Matches(swiffyobject, @"""data:image(.*?)"",|""text"":(.*?)"",");
                List<String> HTML5Elements = RegExService
                        .getMatches("\"data:image(.*?)\",|\"text\":(.*?)\",",swiffyobject,0);
                String concatChecksum =HTML5Elements.stream()
                        .map(e -> {
                            String tmp = e.substring(1, e.length() - 3);//REMOVE FIRST CHAR AND TWO LAST ONES
                            return fileManagerService.getChecksumText(tmp);})
                        .distinct()
                        .sorted()
                        .collect(Collectors.joining());

                html5ElementChecksum = fileManagerService.getChecksumText(concatChecksum);

                //JIRA : PIXITREND-98
                htmlCodeWholePage = Utils.replaceFirst(htmlCodeWholePage,"<head pixi=\"true\">",
                        "<head pixi=\"true\"><script>" + "document.evaluate('//canvas/..', document, null, XPathResult.ANY_TYPE, null).iterateNext().outerHTML  = '';" + "</script>");
                htmlCodeWholePage = htmlCodeWholePage.replace("&amp;", "&").replace("&lt;", "<").replace("&gt;", ">");

            } catch (Exception e) {
                //LOGGER.warn("NO swiffyobject Found");
                //GOOGLE ADS
                adData = executeJScriptCanThrowsException("return JSON.stringify(adData);");

                isHTML5 = true;
                LOGGER.info("I find a GOOGLE ADS");
                destinationUrl = RegExService.getGADestinationUrl(adData);
                resp = getHTML5SignatureFromImages();
                html5ElementChecksum = resp.getHtml5ElementChecksum();

            }
            if( !Constants.CHECKSUMEMPTY.equals(html5ElementChecksum)){
                AdNetwork network = getHTML5Network(getCurrentFrameUrl(), htmlCodeWholePage, parentAdNetwork);
                MediaType typeAdMedia = MediaType.HTML5;
                long fileSize =0;
                IsNewMediaResponse isNew = isNewMedia(html5ElementChecksum);
                if (isNew.isNewMedia() || (isNew.isToBeFixed() && isToBeFixedEnabled)) {
                    if(resp != null) {
                        htmlCodeWholePage = fileManagerService.replaceImagesWithBase64(htmlCodeWholePage, resp.getImages2Convert64());
                    }
                    fileManagerService.createFileInMedia(html5ElementChecksum,htmlCodeWholePage);
                    fileSize = fileManagerService.getFileSizeInMedia(html5ElementChecksum);
                    awsConnector.upload(html5ElementChecksum,typeAdMedia,true);
                }

                databaseAPIConnector.insertImpression(getRobotData(),idExtraction, typeAdMedia, "",
                        network, html5ElementChecksum, getCurrentFrameUrl(), destinationUrl, "null",
                        idPage, sizeScreen,lastFrameSize, new Point(offsetX, offsetY), "", isNew, fileSize);
            }
        } catch (Exception e) {
            //NO SWIFFY / HTML5 OF GOOGLE
            //LOGGER.warn("//NO SWIFFY / HTML5 OF GOOGLE");
        }

        return isHTML5;
    }


    //EXTRACT HTML5 REGULAR AD
    public void extractHtml5StandardAd(String idExtraction, String idPage, String htmlCodeWholePage, String html5ElementChecksum, AdNetwork parentAdNetwork ) {
        try {
            LOGGER.info("processing Html5StandardAd ");
            GetChecksumOfAllImagesOnPageResp resp = null;
            if ( "".equals(html5ElementChecksum)) {
                LOGGER.info("trying to get HTML5SignatureFromImages");
                resp = getHTML5SignatureFromImages();
                html5ElementChecksum = resp.getHtml5ElementChecksum();
            }
            if (!Constants.CHECKSUMEMPTY.equals(html5ElementChecksum) &&  !"".equals(html5ElementChecksum)) {
                String destinationURL = "";
                String originalURL = getCurrentFrameUrl();
                AdNetwork network = getHTML5Network(originalURL, htmlCodeWholePage, parentAdNetwork);

                MediaType typeAdMedia = MediaType.HTML5;
                long fileSize = 0;
                IsNewMediaResponse isNew = isNewMedia(html5ElementChecksum);
                if (isNew.isNewMedia() || isNew.isNewVisual()) {
                    //DESTINATION URL
                    try {
                        destinationURL = RegExService.findFirst("=\"(http(s|):\\/\\/www\\.googleadservices.com\\/pagead\\/aclk.*?)\"", htmlCodeWholePage, false, 1);
                        if(destinationURL!=null && !destinationURL.equals("")){
                            destinationURL = getDestinationURL(destinationURL);
                        }
                        if(StringUtils.isBlank(destinationURL)){
                            if(destinationURL ==null || "".equals(destinationURL) || Utils.isDestinationUrlcontainsGoogleCom(destinationURL)){
                                List<WebElement> images = driver.findElements(By.xpath("//img"));
                                if (images.size() > 0) {
                                    for (WebElement img:images) {
                                        Dimension s = img.getSize();
                                        if(s.getWidth()>1 && s.getHeight()>1){
                                            destinationURL = getDestinationFromElement(img);
                                            if(destinationURL!=null && !destinationURL.equals(""))
                                                break;
                                        }
                                    }
                                }
                                if (destinationURL ==null || "".equals(destinationURL)) {
                                    List<WebElement> divs = driver.findElements(By.xpath("//div"));
                                    destinationURL = getDestinationFromElement(divs.get(divs.size() / 2));
                                }
                            }
                        }
                    } catch (Exception e) { }
                    if(isNew.isNewMedia() || (isNew.isToBeFixed() && isToBeFixedEnabled)){
                        if(resp != null) {
                            htmlCodeWholePage = fileManagerService.replaceImagesWithBase64(htmlCodeWholePage, resp.getImages2Convert64());
                        }
                        fileManagerService.createFileInMedia(html5ElementChecksum,htmlCodeWholePage);
                        fileSize = fileManagerService.getFileSizeInMedia(html5ElementChecksum);
                        awsConnector.upload(html5ElementChecksum,typeAdMedia,true);
                    }
                }
                Dimension companionAdSize =  (lastFrameSize.getHeight()<5 ||lastFrameSize.getWidth()<5)?new Dimension(300,250):lastFrameSize;
                databaseAPIConnector.insertImpression(getRobotData(),idExtraction, typeAdMedia,
                        "", network, html5ElementChecksum, originalURL, destinationURL, "null",
                        idPage, sizeScreen, companionAdSize, new Point(offsetX, offsetY), "", isNew, fileSize);
                htmlCodeWholePage=null;
            }
        } catch (Exception e) {}
    }

    //EXTRACT HTML5 ADOBE EDGE ADS
    public Boolean extractHTML5AdobeEdge(String idExtraction, String idPage, String htmlCodeWholePage, AdNetwork parentAdNetwork) {
        Boolean isHTML5 = false;
        try {
            //TODO : TO BE TESTED, ANIMATION WILL BE BROKEN BECAUSE OF JS
            // https://s0.2mdn.net/4447585/1465890051320/728x90/728x90.html
            String adobeEdge = executeJScriptCanThrowsException("return AdobeEdge.version;");
            isHTML5 = true;
            LOGGER.info("processing HTML5AdobeEdge ");
            //CALCULATE CHECKSUM ONLY BY IMAGES AS A FIRST RELEASE (TODO : to be enhanced... if possible)
            //MERGE THESE TWO CALLS INTO ONE (Enhancement)

            GetChecksumOfAllImagesOnPageResp resp = getHTML5SignatureFromImages();
            String html5ElementChecksum = resp.getHtml5ElementChecksum();
            if( !Constants.CHECKSUMEMPTY.equals(html5ElementChecksum)){
                AdNetwork network = getHTML5Network(getCurrentFrameUrl(), htmlCodeWholePage, parentAdNetwork);
                MediaType typeAdMedia = MediaType.HTML5;
                IsNewMediaResponse isNew = isNewMedia(html5ElementChecksum);

                htmlCodeWholePage = getCodeSourceOfCurrentFrame(false);

                String tmpURL = framesUrl.peek();

                htmlCodeWholePage = AdobeEdgeHelper.execute(htmlCodeWholePage, tmpURL);
                long fileSize =0;
                String destinationURL="";
                if (isNew.isNewMedia() || isNew.isNewVisual()) {
                    //get destinationURL
                    String clickTag = executeJScriptCanThrowsException("return window.clickTag");
                    if (Utils.isDestinationUrlcontainsGoogleCom(clickTag) && htmlCodeWholePage.contains("destinationUrl")){
                        clickTag= RegExService.findFirst("destinationUrl:( |) ('|\")(.*?)('|\")",htmlCodeWholePage,false, 3);
                    }
                    if(clickTag!=null) {
                        if(clickTag.contains("adurl")){
                            clickTag=RegExService.getAdUrlFromClickTag(clickTag);
                            int i =0;
                            while (clickTag.contains("%") && i < 6) {
                                clickTag = Utils.unescapeDataString(clickTag);
                                i++;
                            }
                        }
                        destinationURL = getRealUrl(clickTag);
                    }else{
                        LOGGER.error("can not find window.clickTag in this page !!");
                    }
                    if(isNew.isNewMedia() || (isNew.isToBeFixed() && isToBeFixedEnabled)){
                        htmlCodeWholePage = fileManagerService.replaceImagesWithBase64(htmlCodeWholePage,resp.getImages2Convert64());
                        fileManagerService.createFileInMedia(html5ElementChecksum,htmlCodeWholePage);
                        fileSize = fileManagerService.getFileSizeInMedia(html5ElementChecksum);
                        awsConnector.upload(html5ElementChecksum,typeAdMedia,true);
                    }
                }


                databaseAPIConnector.insertImpression(getRobotData(),idExtraction, typeAdMedia, "",
                        network, html5ElementChecksum, getCurrentFrameUrl(), destinationURL, "null", idPage,
                        sizeScreen,lastFrameSize, new Point(offsetX,offsetY) , "", isNew, fileSize);
                htmlCodeWholePage=null;
            }

        } catch (Exception e) {
            logger.error("",e);
        }
        return isHTML5;
    }
    //EXTRACT HTML5 ADBUTTER
    public Boolean extractHTML5AdButter(String idExtraction, String idPage, String htmlCodeWholePage, AdNetwork parentAdNetwork) {
        Boolean isHTML5 = false;
        //HTML5 Google Web Designer
        try {
            if (htmlCodeWholePage.contains("adbutter.net")) {
                isHTML5 = true;
                LOGGER.info("processing HTML5AdButter ");
                List<WebElement> elements ;
                //TODO : ALSO BACKGROUNDS
                elements = driver.findElements(By.xpath("//img"));
                if (elements != null && elements.size() > 0) {
                    String destinationURL = "";
                    GetChecksumOfAllImagesOnPageResp resp = getHTML5SignatureFromImages();
                    String html5ElementChecksum = resp.getHtml5ElementChecksum();
                    if( !Constants.CHECKSUMEMPTY.equals(html5ElementChecksum)){
                        AdNetwork network = getHTML5Network(getCurrentFrameUrl(), htmlCodeWholePage, parentAdNetwork);
                        MediaType typeAdMedia = MediaType.HTML5;
                        IsNewMediaResponse isNew = isNewMedia(html5ElementChecksum);
                        long fileSize =0;

                        if (isNew.isNewMedia() || isNew.isNewVisual()) {
                            destinationURL = getDestinationFromElement(findElementSafe(By.xpath("//canvas"), null));
                            if( isNew.isNewMedia() || (isNew.isToBeFixed() && isToBeFixedEnabled)){
                                html5ElementChecksum = fileManagerService.replaceImagesWithBase64(html5ElementChecksum, resp.getImages2Convert64());
                                fileManagerService.createFileInMedia(html5ElementChecksum,htmlCodeWholePage);
                                fileSize = fileManagerService.getFileSizeInMedia(html5ElementChecksum);
                                awsConnector.upload(html5ElementChecksum,typeAdMedia,true);
                            }
                        }
                        databaseAPIConnector.insertImpression(getRobotData(),idExtraction, typeAdMedia,
                                "", network, html5ElementChecksum,
                                getCurrentFrameUrl(), destinationURL, "null", idPage, sizeScreen,lastFrameSize,
                                new Point(offsetX, offsetY), "", isNew, fileSize);
                        htmlCodeWholePage=null;
                    }
                }
            }
        } catch (Exception e) {
        }

        return isHTML5;
    }
    //EXTRACT HTML5 SERVING SYS
    public Boolean extractHTML5ServingSys(String idExtraction, String idPage, String htmlCodeWholePage, AdNetwork parentAdNetwork) {
        Boolean isHTML5 = false;
        try {
            if (htmlCodeWholePage.contains("isAdkitLoaded")
                    || htmlCodeWholePage.contains("adkit.js")) {
                isHTML5 = true;
                LOGGER.info("processing HTML5ServingSys");
                extractHtml5StandardAd(idExtraction, idPage, htmlCodeWholePage,"", parentAdNetwork);
            }
        } catch (Exception e) {
        }

        return isHTML5;
    }
    //EXTRACT HTML5 BANNERFLOW
    public Boolean extractHTML5BannerFlow(String idExtraction, String idPage, String htmlCodeWholePage, AdNetwork parentAdNetwork) {
        Boolean isHTML5 = false;
        try {
            if (htmlCodeWholePage.contains("bannerflow.com")) {
                isHTML5 = true;
                LOGGER.info("processing HTML5BannerFlow");
                extractHtml5StandardAd(idExtraction, idPage, htmlCodeWholePage,"", parentAdNetwork);
            }
        } catch (Exception e) {
        }

        return isHTML5;
    }
    //EXCTRACT HTML5 TUMULT HYPE
    public Boolean extractHTML5TUMULTHYPE(String idExtraction, String idPage, String htmlCodeWholePage, AdNetwork parentAdNetwork) {
        Boolean isHTML5 = false;
        try {
            if (htmlCodeWholePage.contains("HYPE_") || htmlCodeWholePage.contains("adkit.js")) {
                isHTML5 = true;
                LOGGER.info("processing HTML5TUMULTHYPE");
                extractHtml5StandardAd(idExtraction, idPage, htmlCodeWholePage,"", parentAdNetwork);
            }
        } catch (Exception e) {
        }

        return isHTML5;
    }
    //EXTRACT HTML5 CREATEJS (SERVING-SYS)
    public Boolean extractHTML5CreateJs(String idExtraction, String idPage, AdNetwork parentAdNetwork) {
        Boolean isHTML5 = false;
        try {
            String relative2AbsoluteJs = fileManagerService
                    .getFileContentAsString("js/createJsInsertHiddenImages.js");
            executeJScriptCanThrowsException(relative2AbsoluteJs+ " return JSON.stringify({'data': [createjs, lib]});");
            isHTML5 = true;
            LOGGER.info("processing HTML5CreateJs");

            String HTML5ElementChecksum = "";
            String scriptToInject = fileManagerService.getFileContentAsString("js/createJsScriptToInject.js");
            String htmlCodeWholePage = Utils.replaceFirst(getCodeSourceOfCurrentFrame(),"<head pixi=\"true\">", "<head pixi=\"true\">" + scriptToInject);
            extractHtml5StandardAd(idExtraction, idPage, htmlCodeWholePage, HTML5ElementChecksum, parentAdNetwork);

        } catch (Exception e) {}
        return isHTML5;
    }
    //EXTRACT HTML5 SERVING SYS
    public Boolean extractHTML5GoogleHybrid(String idExtraction, String idPage, String htmlCodeWholePage, AdNetwork parentAdNetwork) {
        Boolean isHTML5 = false;
        try {
            if (htmlCodeWholePage.contains("adunit")
                    && htmlCodeWholePage.contains("googlesyndication")) {
                isHTML5 = true;
                LOGGER.info("processing HTML5GoogleHybrid");
                extractHtml5StandardAd(idExtraction, idPage, htmlCodeWholePage,"", parentAdNetwork);
            }
        } catch (Exception e) {
        }
        return isHTML5;
    }
    //DETECT LANDING PAGE
    public String getDestinationURL(String parameter) {
        try {
            LOGGER.debug("getting dest of {}", parameter);
            if(parameter == null)
                return null;
            if(parameter.startsWith("javascript:"))
                return parameter;


            //FLASH (Normal case)
            if (RegExService.countMatches("clicktag=", parameter.toLowerCase()) == 1) {
                return getRealUrl(Utils.unescapeDataString( RegExService.findFirst("(?i)(?<=clicktag[?=]).*",parameter,true,0)));
            }
            //FLASH (Complicated cases)
            if (parameter.contains("keyValueDelim") && parameter.contains("renderingDomain")) //adtech special flash
            {
                return getRealUrl(executeJScriptCanThrowsException("return adtechAdConfig.clickthroughs.default.dest;"));
            }
            String parameterCopy = parameter;
            int i = 0;
            while (parameterCopy.contains("%") && i < 6) {
                parameterCopy = Utils.unescapeDataString(parameterCopy);
                i++;
            }
            //cleaning
            if (parameterCopy.toLowerCase().contains("[countgo]")) {
                parameterCopy = parameterCopy.replace("[countgo]", "");
            }
            if (parameterCopy.toLowerCase().contains("adurl")) {
                String tmp =RegExService.getAdUrlFromClickTag(parameterCopy);
                return getRealUrl(tmp);
            }
            if (parameterCopy.toLowerCase().contains(",url:")) {
                return getRealUrl(RegExService.findFirst("(?i)(?<=,url:)(.*?)(?=,)",parameterCopy,false,0));
            }
            if (parameterCopy.toLowerCase().contains(";link")) {
                return getRealUrl(RegExService.findFirst("(?i);link=(.*)",parameterCopy,false,1));
            }

            if (RegExService.countMatches("clicktag",parameterCopy.toLowerCase()) > 2) {
                return getRealUrl(RegExService.findFirst("(?i)(?<=clickTag[?=])(.*?)(?=&clickTag)",parameterCopy,false,0));
            }

            if (parameterCopy.toLowerCase().contains("zedo.com") && parameterCopy.toLowerCase().contains(";k=")) {
                return getRealUrl(RegExService.findFirst("(?i);k=(.*)[;]?",parameterCopy,false,0));
            }


        }catch (Exception e) {
            LOGGER.error(e.getMessage(),e);
        }

        return getRealUrl(parameter);
    }


    //GET DESTINATION LINK FROM JOOMLA AD AGENCY PLUGIN
    public String getDestinationURLJoomlaAdAgency(WebElement element) {
        try {
            return element.findElement(By.xpath("..//a[contains(@href,'click')]")).getAttribute("href");
        } catch (Exception e) {
            LOGGER.error(e.getMessage(),e);
        }
        return "";
    }
    //EXTRACT WALL PAPER AD (IN BACKGROUND-IMGE OF BODY)
    public void extractWallPaperAd(String idExtraction, String idPage, String selectorElement, String selectorIframe) {//habillage
        try {
            String urlAdMedia = "", destinationURL = "", fileAdMedia = "";
            IsNewMediaResponse isNew = new IsNewMediaResponse( "0", "0", "0", "0", "0" );
            WebElement img;
            //GO TO TOP
            driver.switchTo().defaultContent();
            //IF BACKGROUND IN IFRAME
            if (selectorIframe!=null
                    && !"/".equals(selectorIframe)
                    && !"".equals(selectorIframe)
                    && selectorIframe.contains("iframe")) {
                List<WebElement> iframes = driver.findElements(By.xpath(selectorIframe));
                if (iframes.size() > 0) {
                    driver.switchTo().frame(iframes.get(0));
                } else {
                    return; //NO BACKGROUND FOUND
                }
            }
            //IF CSS SELECTOR
            if (!selectorElement.contains("//")){
                img = driver.findElements(By.cssSelector(selectorElement)).get(0);
                urlAdMedia = img.getCssValue("background-image");
            } else {
                //XPATH SELECTOR
                img = driver.findElements(By.xpath(selectorElement)).get(0);
                try {
                    urlAdMedia = img.getAttribute("src");
                } catch (Exception e) {
                    urlAdMedia = img.getCssValue("background-image");
                }
            }
            if (img!=null &&
                    !(img.getSize().getWidth() > 55 && img.getSize().getHeight() > 55)) {
                LOGGER.warn("small image or null");
                return;
            }
            //IF FOUND
            if (!"none".equals(urlAdMedia) && !"".equals(urlAdMedia)) {
                LOGGER.info("extract image by cssSelector or xpathSelector ");
                urlAdMedia = urlAdMedia.replace("url('", "").replace("')", "")
                        .replace("url(\"", "").replace("\")", "")
                        .replace("url(", "").replace(")", "");

                ChecksumFileResponse checksum = getChecksumFile(urlAdMedia, true);
                long fileSize = checksum.getFileSize();
                if(checksum!=null){
                    isNew = new IsNewMediaResponse(checksum.getIsNewMedia(), checksum.getIsNewVisual(), checksum.getIdMedia(), checksum.getIdVisual(), checksum.getToBeFixed());
                    fileAdMedia = checksum.getChecksumFile();
                    if ( isNew.isNewMedia() || isNew.isNewVisual()) {

                        destinationURL = getDestinationFromElement(img);
                        if ( "".equals(destinationURL)) {//SMART AD SERVER
                            try {
                                destinationURL = getDestinationURL(executeJScriptCanThrowsException("return SmartBackgroundConfig.creative.clickUrl;"));
                            } catch (Exception e) {

                            }
                        }
                        if ("".equals(destinationURL)) {//GOOGLE AD NETWORK (http://www.goal.com/ar/news/14602/fifa-16?ICID=HP_TN_QL_1)
                            try {
                                String functionOnClick = executeJScriptCanThrowsException("return (window[(leftDiv.onclick + '').replace('function () {','').replace('();','').replace('}','').replace(/(\r\n|\n|\r)/gm,'')] + '').replace(/(\r\n|\n|\r)/gm,'');");
                                destinationURL = getRealUrl(RegExService.findFirst("(?i)link=\"(.*?)\"",functionOnClick,false,1));
                            } catch (Exception e) {

                            }
                        }
                        if ("".equals(destinationURL)) {
                            destinationURL = getDestinationFromElement(img);
                        }
                        if (isNew.isNewMedia()) {
                            awsConnector.upload(fileAdMedia,MediaType.IMAGE,true);
                        }
                    }
                    databaseAPIConnector.insertImpression(getRobotData(),idExtraction, MediaType.IMAGE, "", AdNetwork.DIRECT,
                            fileAdMedia, urlAdMedia, destinationURL, "null", idPage, sizeScreen,img.getSize(),
                            new Point(img.getLocation().getX() + offsetX, img.getLocation().getY()+ offsetY), "", isNew, fileSize);
                }
            }
        } catch (Exception e) {
            LOGGER.error("Exception or no background found", e);
        }
    }

    protected String getYoutubeVideoXpathSelectorForMain(){
        return "//*[contains(@class,'feed-item-container')]//li//a//img/..";
    }
    protected String getYoutubeVideoXpathSelectorForSideBar(){
        return "//span[@class='video-time']/preceding-sibling::img";
//        return "//div[@id='watch7-sidebar']//div[@id='watch7-sidebar-modules']//li[contains(@class,'video-list-item')]/div[@class='content-wrapper']/a";
    }


    //RANDOMLY CLICK ON VIDEO FROM THE MAIN PAGE OF YOUTUBE
    public void goToRandomVideoFromMain() throws YoutubeNavigationException {
        try {
            TimeUtils.waitFor(1000);
            List<WebElement> videos = driver.findElements(By.xpath(this.getYoutubeVideoXpathSelectorForMain()));
            WebElement videoToClick = videos.get((new Random().nextInt(videos.size() - 1)) + 1); //PICK A VIDEO RANDOMLY
            executeJScriptCanThrowsException("arguments[0].click();", videoToClick); //CLICK ON THE VIDEO
        } catch (Exception e) {
            LOGGER.error(e.getMessage(),e);
            throw new YoutubeNavigationException("Cannot go to random youtube video from main page.");
            //goToRandomVideoFromMain();
        }
    }

    //RANDOMLY CLICK ON VIDEO FROM RIGHT SIDE BAR
    public void goToRandomVideoFromSidebar() throws YoutubeNavigationException {
        try {
            TimeUtils.waitFor(1000);
            List<WebElement> videos = driver.findElements(By.xpath(this.getYoutubeVideoXpathSelectorForSideBar()));
            WebElement videoToClick = videos.get((new Random().nextInt(videos.size() - 1)) + 1); //PICK A VIDEO RANDOMLY
            executeJScriptCanThrowsException("arguments[0].click();", videoToClick); //CLICK ON THE VIDEO
        } catch (Exception e) {
            LOGGER.error(e.getMessage(),e);
            throw new YoutubeNavigationException("Cannot go to random youtube video from Sidebar.");
        }
    }

    @Override
    public void setYoutubePages(String main, String sideBar) {
        this.youtubeHomePageId = main;
        this.youtubeSideBarPageId = sideBar;
    }

    //RETURNS TRUE IF PREROLL VIDEO IS PRESENT
    public Boolean isPrerollVideo() {
        try {
            return driver.findElements(By.xpath("//div[contains(@class,'videoAdUiAdInfoPopupText')]")).size() != 0;
        } catch (Exception e) {
            LOGGER.error(e.getMessage(),e);
            return false;
        }
    }
    //GO TO YOUTUBE WEBSITE
    public void goToMainPageYoutube() throws ProblemLoadingPageException{
        goToURL("http://www.youtube.com");
    }
    //EXTRACT AD FROM YOUTUBE HOMEPAGE (HTML5)
    public void extractYoutubeMainPageAd(String idExtraction, String idPage) {
        try {
            if(isScreenshotON){
                TimeUtils.waitFor(500);
                String screen = "screen-youtube-main-"+idExtraction+ Constants.JPG;
                File file = guiHelper.captureScreenShot(driver,null,fileManagerService.getMediaPath()+"/"+screen);
                ftpConnector.upload(file);
            }

            String HTMLCodeWholePage = getCodeSourceOfCurrentFrame(false);

            if (HTMLCodeWholePage.contains("ytclosebutton")) {
                String concatChecksum = "";
                String html5ElementChecksum = "";
                String destinationURL = "";
                String checksumVideoIntro = "";
                WebElement firstFrame = driver.findElement(By.xpath("//iframe[@height>10 and contains(@id,'ad_creative_iframe')]"));
                Point location = firstFrame.getLocation();
                Dimension size = firstFrame.getSize();
                driver.switchTo().frame(firstFrame);
                WebElement secondFrame = driver.findElement(By.xpath("//iframe"));
                driver.switchTo().frame(secondFrame);

                int counter = 0;
                int countIntroVideo = 0;

                try {
                    WebElement introElement = driver.findElement(By.className("intro"));

                    checksumVideoIntro = fileManagerService.getChecksumText(RegExService.findFirst("video-url=\"(.*?)\"",HTMLCodeWholePage,false,1));
                    //Wait until introduction is done finished before taking the ad, timeout after 180 seconds (3 minutes)
                    while (introElement.getSize().getHeight() > 10 && counter < 60) {
                        TimeUtils.waitFor(3000);
                        counter++;
                    }
                    countIntroVideo = 1;
                } catch (Exception e) {

                }
                //Now we have the correct HTML code (The previous one contains only the introduction... maybe we can do something with it ?)
                HTMLCodeWholePage = getCodeSourceOfCurrentFrame().replace("gwd_lib.js","");

                //CALCULATE CHECKSUM ONLY BY IMAGES AS A FIRST RELEASE (TODO : to be enhanced... if possible)
                //MERGE THESE TWO CALLS INTO ONE (Enhancement)
                String backgroundImages = executeJScriptCanThrowsException("var all = document.getElementsByTagName('*'); var images = {'images': []}; var img; for (var i=0, max=all.length; i < max; i++) { img = all[i]; style = img.currentStyle || window.getComputedStyle(img, false);   bi = style.backgroundImage.slice(5, -2);  if (bi!='') { images['images'].push(bi); img.style.backgroundImage = style.backgroundImage; }} return JSON.stringify(images);");
                //Convert returned json string to array of strings
                List<String> backgroundImagesArray = Utils.convertJSONtoArray("images", backgroundImages);
                List<WebElement> imagesArray = driver.findElements(By.tagName("img"));

                //LEAVE LAST INDEX FOR VIDEO ID
                String[] checksumHTML5Elements = new String[backgroundImagesArray.size() + imagesArray.size() + countIntroVideo];
                List<ImageUrlFileName> images2Convert64 = new ArrayList<>();
                for (int i = 0; i < backgroundImagesArray.size(); i++) {
                    if (backgroundImagesArray.get(i).startsWith("http")) {
                        ChecksumFileResponse tmp = getChecksumFile(backgroundImagesArray.get(i), false);
                        checksumHTML5Elements[i] = tmp.getChecksumFile();
                        images2Convert64.add(new ImageUrlFileName(backgroundImagesArray.get(i),tmp.getTempFileName()));
                    } else if (backgroundImagesArray.get(i).startsWith("data:")) {
                        checksumHTML5Elements[i] = fileManagerService.getChecksumText(backgroundImagesArray.get(i));
                    }
                }

                String src = "";
                for (int i = 0; i < imagesArray.size(); i++) {
                    src = imagesArray.get(i).getAttribute("src");
                    if (src.startsWith("http")) {
                        ChecksumFileResponse tmp =  getChecksumFile(src, false);
                        checksumHTML5Elements[i] = tmp.getChecksumFile();
                        images2Convert64.add(new ImageUrlFileName(src,tmp.getTempFileName()));
                    } else if (src.startsWith("data:")) {
                        checksumHTML5Elements[i] = fileManagerService.getChecksumText(src);
                    }
                }
                //ADD ID OF VIDEO INTO SIGNATURE
                checksumHTML5Elements[backgroundImagesArray.size()] = checksumVideoIntro;

                concatChecksum =  Arrays.stream(checksumHTML5Elements)
                        .distinct()
                        .sorted()
                        .collect(Collectors.joining());

                if (countIntroVideo == 1) html5ElementChecksum = fileManagerService.getChecksumText(concatChecksum);

                AdNetwork network = AdNetwork.YOUTUBE;
                MediaType typeAdMedia = MediaType.HTML5;

                IsNewMediaResponse isNew = isNewMedia(html5ElementChecksum);
                long fileSize = 0;
                if (isNew.isNewMedia() || isNew.isNewVisual()) {
                    if(isNew.isNewMedia() || (isNew.isToBeFixed() && isToBeFixedEnabled)){
                        HTMLCodeWholePage = fileManagerService.replaceImagesWithBase64(HTMLCodeWholePage, images2Convert64);
                        fileManagerService.createFileInMedia(html5ElementChecksum,HTMLCodeWholePage);
                        fileSize= fileManagerService.getFileSizeInMedia(html5ElementChecksum);
                        awsConnector.upload(html5ElementChecksum,typeAdMedia,true);
                    }
                    destinationURL = getDestinationFromElement(driver.findElements(By.className("gwd-page-content")).get(0));
                }

                databaseAPIConnector.insertImpression(getRobotData(),idExtraction, typeAdMedia, "",
                        network, html5ElementChecksum, "", destinationURL, "null",
                        idPage,sizeScreen, size, location, "", isNew, fileSize);
                HTMLCodeWholePage=null;
            }
        } catch (Exception e) {
        }
    }
    public void pauseVideo() {
        try {
            executeJScriptCanThrowsException("document.getElementsByClassName('ytp-play-button')[0].click();");
        } catch (Exception e) {
            LOGGER.error("error occurred when attempting to pause youtube video, it's : {}", e);
            try {
                List<WebElement> elements = driver.findElements(By.xpath("//div[contains(@class,'ytp-button-pause')]"));
                if (elements.size() > 0) {
                    elements.get(0).click();
                }
            } catch (Exception ex) {
                LOGGER.error(e.getMessage(),ex);
            }
        }
    }

    //EXTRACT YOUTUBE ID FROM YOUTUBE URL
    protected String getYoutubeIdFromURL(String url) {
        try {
            return RegExService.getYoutubeIdFromUrl(url);
        } catch (Exception e) {
            LOGGER.error(e.getMessage(),e);
            return url;
        }
    }



    public String getIdPrerollVideo() {
        try {
            List<WebElement> elements = driver.findElements(By.xpath("//a[contains(@class,'html5-title-logo')]|//a[contains(@class,'ytp-title-link')]"));
            if (elements.size() > 0) {
                String idPrerollVideo = elements.get(0).getAttribute("href");
                //Get Youtube ID from URL if URL passed in parameters
                if(idPrerollVideo == null ){
                    return "";
                }
                if (idPrerollVideo.contains("youtu")) {
                    idPrerollVideo = getYoutubeIdFromURL(idPrerollVideo);
                }
                return idPrerollVideo;
            } else {
                return "";
            }
        } catch (Exception e) {
            LOGGER.error(e.getMessage(),e);
            return "";
        }
    }
    public String getUrlPrerollVideo() {
        try {
            return driver
                    .findElements(By.xpath("//div[@id='movie_player']//video[contains(@class,'html5-main-video')]"))
                    .get(0)
                    .getAttribute("src");
        } catch (Exception e) {
            LOGGER.error(e.getMessage(),e);
        }
        return "";
    }
    protected String getPrerollDestinationVideoXpathExpression(){
        return "//div[@class='videoAdUiVisitAdvertiserLinkText']";
    }
    public String getDestinationVideo() {
        try {
            return getDestinationFromElement(driver.findElements(By.xpath(this.getPrerollDestinationVideoXpathExpression())).get(0));
        } catch (Exception e) {
            LOGGER.error(e.getMessage(),e);
        }

        return "";
    }



    public void processYoutubeVideo(String idExtraction, String idPage) throws Exception{
        TimeUtils.waitFor(4000); //Wait 4 seconds before processing the pre-roll ad
        if(isScreenshotON){
            TimeUtils.waitFor(500);
            String screen = "screen-youtube-video-"+idExtraction+ Constants.JPG;
            File file = guiHelper.captureScreenShot(driver,null,fileManagerService.getMediaPath()+"/"+screen);
            ftpConnector.upload(file);
        }
        if (isPrerollVideo()) {
            pauseVideo();
            String idPrerollVideo = getIdPrerollVideo();
            //In Case pre-roll is a video file, now Youtube video
            if ("".equals(idPrerollVideo)) {
                idPrerollVideo = getUrlPrerollVideo();
            }
            //we propcess to pre-roll video only if it's detected and not empty
            if(!"".equals(idPrerollVideo)){
                LOGGER.info("preroll ID  {}",idPrerollVideo);

                String destinationUrl = "", originalURL = "", type = "mp4", duration = "";
                IsNewMediaResponse isNew;
                long fileSize =-1;

                MediaHelperAPIResponse mediaHelperAPIResponse;
                final String checksumFile;
                final boolean isYoutubeId = !idPrerollVideo.contains("http");

                try{
                    if(isYoutubeId){
                        originalURL = "https://www.youtube.com/watch?v=" + idPrerollVideo;
                        mediaHelperAPIResponse = this.mediaHelperAPIConnector.downloadByYoutubeId(idPrerollVideo);
                    } else {
                        originalURL = idPrerollVideo;
                        mediaHelperAPIResponse = this.mediaHelperAPIConnector.downloadByFileUrl(idPrerollVideo);
                    }
                } catch (IOException e){
                    mediaHelperAPIResponse = null;
                }
                if(null == mediaHelperAPIResponse){
                    LOGGER.error("MediaHelperAPIConnector returned null on preroll url : {}", idPrerollVideo);
                    extractAdsPage(idExtraction, idPage);
                    return;
                }

                checksumFile = mediaHelperAPIResponse.getChecksum();
                isNew = this.isNewMedia(checksumFile);
                if(isNew.isNewMedia() || isNew.isNewVisual()){
                    if(isNew.isNewMedia() || (isNew.isToBeFixed() && isToBeFixedEnabled)){
                        LOGGER.debug("==will=upload======>{}", mediaHelperAPIResponse.getChecksum());
                        if(isYoutubeId){
                            this.mediaHelperAPIConnector.uploadByYoutubeID(idPrerollVideo);// upload file (async)
                        } else {
                            this.mediaHelperAPIConnector.uploadByFileUrl(idPrerollVideo);// upload file (async)
                        }
                        fileSize = mediaHelperAPIResponse.getFileSize();
                        duration = String.valueOf(mediaHelperAPIResponse.getDuration());
                    }
                    destinationUrl = getDestinationVideo();
                }

                Dimension size = new Dimension(0, 0);
                Point location = new Point(0, 0);
                try {
                    WebElement video = driver.findElements(By.xpath("//div[@id='player-api']")).get(0);
                    size = video.getSize();
                    location = video.getLocation();
                } catch (Exception e) {
                }
                databaseAPIConnector.insertImpression(getRobotData(),idExtraction, MediaType.VIDEO, "", AdNetwork.YOUTUBE,
                        checksumFile, originalURL, destinationUrl, "null", idPage,sizeScreen, size,
                        location, duration, isNew, fileSize);
                skipVideoAd();
                TimeUtils.waitFor(2000);
                pauseVideo();
            } else {
                LOGGER.error("pre-roll video will be ignored, it's source cannot be defined by known methods.");
            }
        }
        extractAdsPage(idExtraction, idPage);
    }

    //SKIP AND WAIT 5 SECONDS (TO SEE OVERLAY)
    protected String getPrerollVideoSkipButtonXpathExpression(){
        return "//button[@class='videoAdUiSkipButton']";
    }
    public void skipVideoAd() {
        try {
            List<WebElement> elements = driver.findElements(By.xpath(this.getPrerollVideoSkipButtonXpathExpression()));
            if (elements.size() > 0) {
                elements.get(0).click();
                TimeUtils.waitFor(5000);
            }
        } catch (Exception e) {
            LOGGER.error(e.getLocalizedMessage());
        }
    }

    public GetChecksumOfAllImagesOnPageResp getHTML5SignatureFromImages() {
        return getChecksumOfAllImagesOnPage( "//img");
    }
    public GetChecksumOfAllImagesOnPageResp getChecksumOfAllImagesOnPage(String specialXpathForImages) {
        try {
            String[] images = ArrayUtils.addAll(getUrlAllBackgroundImages(), getUrlAllImages(specialXpathForImages));
            List<String> checksumHTML5Elements = new ArrayList<>();
            List<ImageUrlFileName> images2Convert64 = new ArrayList<>();
            if(images.length>60){//TODO to enhance : we consider it's not an ad and we must ignore it
                throw new Exception("too much images ! It's not an ad and we will ignore it");
            }
            final String currentUrl = driver.getCurrentUrl();
            for (int i = 0; i < images.length; i++) {
                if(!StringUtils.isBlank(currentUrl) && StringUtils.equals(currentUrl, images[i])){
                    logger.warn("found an image with src same as current frame url, {}", currentUrl);
                    continue;
                }
                if (!images[i].contains("mkz-infos.png")) {
                    if (images[i].startsWith("http")) {
                        ChecksumFileResponse tmp = null;
                        try {
                            tmp = getChecksumFile(images[i], false);
                        } catch (IOException e) {
                            LOGGER.error("Cannot get cheksum of image");
                            continue;
                        }
                        checksumHTML5Elements.add(tmp.getChecksumFile());
                        images2Convert64.add(new ImageUrlFileName(images[i],tmp.getTempFileName()));
                        //fileManagerService.deleteFromMedia(tmp.getTempFileName());
                    } else if (images[i].startsWith("data:")) {
                        checksumHTML5Elements.add(fileManagerService.getChecksumText(images[i]));
                    }
                }
            }
            List<String> tmp = checksumHTML5Elements.stream()
                    .distinct()
                    .sorted()
                    .collect(Collectors.toList());
            String concatChecksum = String.join("", tmp);
            String html5ElementChecksum = fileManagerService.getChecksumText(concatChecksum);
            if(Constants.CHECKSUMEMPTY.equals(html5ElementChecksum)){
                html5ElementChecksum = getChecksumAllSvgPaths();
            }
            return  new GetChecksumOfAllImagesOnPageResp(html5ElementChecksum, images2Convert64);
        } catch (Exception e) {
            LOGGER.error(e.getMessage());
        }
        return new GetChecksumOfAllImagesOnPageResp(Constants.CHECKSUMEMPTY, new ArrayList<>());
    }

    //GET ALL URLS OF IMAGES THAT ARE SET AS BACKGROUND OF ANY ELEMENT OF THE PAGE
    public String[] getUrlAllBackgroundImages() {
        try {
            String backgroundImages = executeJScriptFileCanThrowsException("js/getUrlAllBackgroundImages.js");
            //convert returned json string to array of strings
            String[] out = Utils.convertJSONtoArray("images", backgroundImages)
                    .stream()
                    .distinct()
                    .toArray(size -> new String[size]);
            LOGGER.debug("got {} BackgroundImages", out.length);
            return out;
        } catch (Exception e) {
            LOGGER.error(e.getMessage());
        }

        return new String[0];
    }
    //GET ALL URLS OF IMAGES OF THE PAGE (<img ....)
    public String[] getUrlAllImages(String specialXpathForImages) {
        try {
            List<WebElement> elements = driver.findElements(By.xpath(specialXpathForImages));
            List<String> result = new ArrayList<>();
            for(WebElement element : elements){
                final Dimension size = element.getSize();
                if(size.getHeight() == 1 && size.getWidth() == 1) {
                    continue;
                }
                result.add(element.getAttribute("src"));
            }
            List<String> tmp = result.stream().distinct().collect(Collectors.toList());
            LOGGER.debug("got {} images", tmp.size());
            return tmp.toArray(new String[tmp.size()]);
        } catch (Exception e) {
            LOGGER.error(e.getMessage());
        }
        return new String[0];
    }
    public String getChecksumAllSvgPaths() {
        try {
            String svgPathsData = executeJScriptFileCanThrowsException("js/getAllSvgPathsData.js");
            //convert returned json string to array of strings
            List<String> out = Utils.convertJSONtoArray("paths", svgPathsData)
                    .stream()
                    .map(path->Utils.sort(Utils.removeWhitSpacesTabsNewLines(path)))
                    .distinct()
                    .collect(Collectors.toList());

            LOGGER.debug("got {} SVG Paths", out.size());
            String concatChecksum = fileManagerService.getChecksumText(String.join("", out));
            return concatChecksum;
        } catch (Exception e) {
            LOGGER.error(e.getMessage());
        }

        return Constants.CHECKSUMEMPTY;
    }

    public AdNetwork getHTML5Network(String originalUrl, String HTMLCodeWholePage, AdNetwork parentAdNetwork) {
        try {
            if(parentAdNetwork!=null)
                return parentAdNetwork;
            String stringToAnalyze = "";
            if (RegExService.isAdsenseHtml(HTMLCodeWholePage)){
                return AdNetwork.ADSENSE;
            }
            if (HTMLCodeWholePage.contains("data-original-click-url")){
                stringToAnalyze = RegExService
                        .findFirst("data-original-click-url=(\"|')(.*?)(\"|')", HTMLCodeWholePage,false,2);
                //regex : data-original-click-url=("|')(.*?)("|')            -> group 2
            } else {
                if (framesUrl.size() >= 1) {
                    stringToAnalyze = framesUrl.elementAt(framesUrl.size() - 1);
                }
                if (framesUrl.size() >= 2) {
                    stringToAnalyze = framesUrl.elementAt(framesUrl.size() - 2);
                }

                stringToAnalyze = stringToAnalyze.replace("\\x3d", "=")
                        .replace("\\x3f", "?")
                        .replace("\\x26", "&")
                        .replace("\\x25", "%");
            }
            AdNetwork network = AdNetwork.DIRECT;
            if (AdNetwork.ADSENSE.equals(getNetwork(originalUrl, stringToAnalyze,HTMLCodeWholePage, parentAdNetwork))) { //If not premium, override it
                network = AdNetwork.ADSENSE;
            }

            return network;
        } catch (Exception e) {
            LOGGER.error(e.getMessage());
            return AdNetwork.DIRECT;
        }
    }
    private void processSingleFbAd(WebElement ad, int xPosition, boolean isSingle, String idExtraction, String idPage, String idZone, boolean scrollToElement){
        LOGGER.info("processing an ad");
        lastpositionYTimeline++;
        if(scrollToElement){
            try {
                scrollToElement(ad);
            } catch (IllegalArgumentException e) {
                LOGGER.info("Ad will be ignored [deleted], after 20 scroll it's still not shown.");
                /**
                 * This element should be deleted, so he won't selected another time
                 */
                executeJScriptCanThrowsException("return arguments[0].remove();", ad);
                LOGGER.debug("element was deleted.");
                return;
            }
        }
        cleanUpFbAdFromUnwantedItems(ad);

        final List<WebElement> additionalElementsToRemove = getAdditionalElementsToRemove(ad);

        String htmlToSaveIfNew = null;
        try {
            htmlToSaveIfNew = getHtmlOfWebElement(ad);
        } catch (IllegalStateException e) {
            LOGGER.error("A script is busy on this ad, it will be ignored. exception : {}.", e);
            processedAds.add(ad.hashCode());
            return;
        }
        removeElements(additionalElementsToRemove);

        processFbAdAndInsertImpression(ad, xPosition, isSingle, idExtraction, idPage, idZone, htmlToSaveIfNew);
        processedAds.add(ad.hashCode());
        LOGGER.info("end processing ad.");
    }

    private List<WebElement> getAdditionalElementsToRemove(WebElement ad) {
        final WebElement userContentWrapper = findElementSafe(By.xpath(".//div[contains(@class,'fbUserContent')]"), ad);

        final WebElement sponsoredText = findElementSafe(By.xpath(".//span/a[text()='Sponsorisé' or text()='Sponsored']/.."), userContentWrapper);
        final WebElement sponsoredAudience = findElementSafe(By.xpath(".//a[contains(@class,'fbPrivacyAudienceIndicator')]"), userContentWrapper);
        final WebElement sponsoredExtraDot = findElementSafe(By.xpath(".//span[@role='presentation']"), userContentWrapper);

        return Arrays.asList(sponsoredText, sponsoredAudience, sponsoredExtraDot);
    }

    protected String getHtmlOfWebElement(WebElement ad) throws IllegalStateException {
        if(ad == null){
            return "";
        }
        executeJScriptCanThrowsException(fileManagerService.getFileContentAsString("js/fbPIXIProcessFirst.js"), ad);

        int remainingAttempts = 5;
        String text = "";
        while(remainingAttempts >= 0){
            long startTime = System.currentTimeMillis();
            try{
                text = executeJScriptFileCanThrowsException("js/fbPIXIProcess.js", ad);
                LOGGER.info("PIXI--TEST , duration is : " + (System.currentTimeMillis() - startTime) );
                if (text.contains("A script on this page may be busy")
                        || text.contains("Un script sur cette page est peut-être occupé") || StringUtils.isBlank(text)) {
                    TimeUtils.waitFor(5000);
                    remainingAttempts--;
                } else {
                    break;
                }
            } catch (Exception e){
                LOGGER.info("PIXI--TEST-EXCEPTION-THROWN, duration is : " + (System.currentTimeMillis() - startTime) );
                TimeUtils.waitFor(5000);
                remainingAttempts--;
            }
        }
        if(text.contains("A script on this page may be busy")
                || text.contains("Un script sur cette page est peut-être occupé") || StringUtils.isBlank(text)){
            LOGGER.error("A script is busy, nested exception is");
            throw new IllegalStateException("A script on this page is busy busy");
        }
        if(StringUtils.isBlank(text)){
            LOGGER.error("fb Ad has an empty text!!");
        }
        return text;
    }

    //EXTRACT ADS FROM TIMELINE
    protected void extractFBTimelineAds(String idExtraction, String idPage, List<String> possibleMedias) {
        try {
            LOGGER.info("extrcting FB timeline ads for {} & page {}", idExtraction, idPage);
            String xPath = String.join("|",possibleMedias);

            List<WebElement> ads = driver.findElements(By.xpath(xPath));
            if (ads.size() > 0) {//if there is some ads
                for (WebElement element : ads) {
                    WebElement userContentWrapper = findElementSafe(By.xpath(".//div[contains(@class,'fbUserContent')][1]"), element);
                    if (userContentWrapper == null) {
                        continue;
                    }
                    final WebElement btnNext = findElementSafe(By.xpath(".//a[@data-testid='hscroll_pager_next']"), element);
                    if(btnNext != null){
                        final int y = btnNext.getLocation() != null && btnNext.getLocation().getY()>0 ?
                                btnNext.getLocation().getY() :
                                (element.getLocation()!=null && element.getLocation().getY()>0 ? element.getLocation().getY() : 0);
                        while(btnNext.getAttribute("tabindex") == null || !"-1".equals(btnNext.getAttribute("tabindex"))) {
                            executeJScript("arguments[0].click();", btnNext);
                            TimeUtils.waitFor(1000);
                        }

                        scrollToY(y);
                        final List<WebElement> adsInBlock = findElementsSafe(By.xpath(".//iframe[@class='fbEmuTracking']/.."), element);
                        if(adsInBlock!= null && !adsInBlock.isEmpty()){
                            LOGGER.info("found a block of ads, size = {}.", adsInBlock.size());
                            int xPosition = 0;
                            for(WebElement ad : adsInBlock){
                                ++xPosition;
                                processSingleFbAd(ad, xPosition, false, idExtraction, idPage, "1", false);
                            }
                            LOGGER.info("end block of ads");
                        }
                    } else {
                        if(!processedAds.contains(element.hashCode())){
                            LOGGER.info("found a new single ad");
                            processSingleFbAd(element, 1, true, idExtraction, idPage, "1", true);
                            LOGGER.info("end new single ad");
                        }
                    }
                }
            }
        }catch (Exception e){
            LOGGER.error(e.getMessage(),e);
        }
    }

    private String getInnerTextFromElement(WebElement ad){
        if(ad == null) {
            return "";
        }
        final String jsReturned = executeJScript("return arguments[0].innerText", ad);
        return StringUtils.isBlank(jsReturned) ? "" : jsReturned;
    }

    protected String getDescriptionFBXpathSelector(boolean isSingleAd){
        return isSingleAd ? ".//div[contains(@class,'userContent')]" : ".//div[contains(@class,'userContent')]";
    }

    protected void processFbAdAndInsertImpression(WebElement ad, int positionX, boolean isSingleAd, String extractionId, String pageId, String idZone, String text) {
        if(ad == null){
            return;
        }
        LOGGER.info("Processing fb ad. positionX : {}, isSingleAd : {}, extractionId : {}, pageId : {}", positionX, isSingleAd, extractionId, pageId);

        /**
         * getting ad content
         */
        String content = getInnerTextFromElement(ad);
        content = RegExService.cleanFbAdFromLikes(content);
        /**
         * calculate ad media_id = textChecksum + imagesChecksum[] + videoCheksum
         * textChecksum     : ad.getText() -> checksum or empty
         * imagesChecksum[] : all images   -> all cheksums
         * videoCheksum     : .//video checksum or empty
         */
        final WebElement descriptionElement           = findElementSafe(By.xpath(getDescriptionFBXpathSelector(isSingleAd)), ad);
        final List<WebElement> imgTags                = findElementsSafe(By.xpath(".//img"), ad);
        final List<WebElement> iWithBackgroundUrlTags = findElementsSafe(By.xpath(".//i[contains(@style, 'background-image:') or contains(@style, 'background:')]"), ad);
        final WebElement videoTag                     = findElementSafe(By.xpath(".//video"), ad);

        final List<String> imgsUrls = new ArrayList<>();
        final String videoUrl       = videoTag != null ? videoTag.getAttribute("src") : "";
        /**
         * We'll get images sources from IMG tags, it's the 'src' attribute
         */
        imgTags.forEach(t -> {
            imgsUrls.add(t.getAttribute("src"));
        });
        /**
         * in case when it's an 'i' tag, with bachgroung url
         * we'll filter not null
         * and get the attribute style then parse it with regex to obtain background or background-url
         */
        iWithBackgroundUrlTags.stream()
                .filter(t -> t!= null)
                .map(t -> t.getAttribute("style"))
                .forEach(styleAttr -> {
                    final String url = RegExService.findFirst("((background-image[ ]?:|background[ ]?:).*?url\\(['|\"]?(.*?)['|\"]?\\))", styleAttr, true, 3);
                    imgsUrls.add(url);
                });
        /**
         * Then we'll calculate the media_id from the pattern above
         */
        ChecksumFileResponse videoChecksum = null;
        try {
            if(videoTag != null && videoUrl != null) {
                videoChecksum = getChecksumFile(videoUrl, false);
            }
        } catch (IOException e) {
            LOGGER.error("orror occured during getChecksumFile of video {}, exception : {}", videoUrl, e);
            videoChecksum = null;
        }

        final String stringVideoChecksum = videoChecksum != null ? videoChecksum.getChecksumFile() : Constants.CHECKSUMEMPTY;
        final String descriptionChecksum = fileManagerService.getChecksumText(content);
        final GetChecksumOfAllImagesOnPageResp imagesChecksum = getImagesCheksum(imgsUrls);
        /**
         * Finally the media_id
         */
        final String mediaId = fileManagerService.getChecksumText(descriptionChecksum + imagesChecksum.getHtml5ElementChecksum() + stringVideoChecksum) ;//+ idZone;
        LOGGER.info("got mediaId : {}", mediaId);/**
         * detect destination url
         */
        final String hrefDestination = getAdDestionation(ad, isSingleAd, descriptionElement);
        LOGGER.debug("ad with media_id {} has destination : {}", mediaId, hrefDestination);
        final IsNewMediaResponse isNew = isNewFbMedia(mediaId, idZone);
        /**
         * In case when it's a new media, we need to process it and save it
         */
        if(isNew.isNewVisual()){
            try {
                saveAndUploadFbMedia(ad, mediaId, idZone, imagesChecksum.getImages2Convert64(), videoTag, videoChecksum, text);
            } catch (Exception e) {
                LOGGER.error("Cannot finalize fb ad save for mediaId : {}, exception is {}.", mediaId, e);
                return;
            }
        }

        /**
         * sizeofElement
         */
        Dimension adSize = ad.getSize();
        if(adSize != null && adSize.getWidth() == 0 && adSize.getHeight() == 0){
            if(!isSingleAd){
                adSize = new Dimension(476, 359);
            } else {
                LOGGER.error("element had 0x0 size and it was a single ad.");
            }

        }
        /**
         * fileSize
         */
        final long fileSize = fileManagerService.getSizeOfFbMedia(mediaId+idZone);
        /**
         * original url
         */
        final String originalUrl = getOriginalUrl(descriptionElement);
        /**
         * Insert the impression in the database
         */
        databaseAPIConnector.insertImpression(getRobotData(),extractionId, MediaType.FACEBOOK, content, AdNetwork.FACEBOOK, mediaId ,
                originalUrl, hrefDestination, idZone, pageId, sizeScreen, adSize,
                new Point(positionX, lastpositionYTimeline), "", isNew, fileSize);

    }

    protected String getOriginalUrl(WebElement descriptionElement){
        return "";
    }

    protected void scrollToY(int y){
        if(y<=0){
            LOGGER.error("trying to scroll to y position with invalid parameter y : {}", y);
            return;
        }
        try {
            executeJScriptCanThrowsException("window.scrollTo(0,Math.round(" +y + "))");
        } catch (Exception e) {
            LOGGER.error("cannot scroll to y position {}, because of exception {}", y, e);
        }
    }
    protected void scrollToElement(WebElement element) throws IllegalArgumentException {
        if(element == null) return;
        try {
            new Actions(driver).moveToElement(element).build().perform();
            executeJScriptCanThrowsException("window.scrollTo(0," + element.getLocation().getY() + ")");
            int scrolls = 0;
            while (element.getSize().equals(new Dimension(0,0))) {
                executeJScriptCanThrowsException("window.scrollBy(0,200);");
                scrolls++;
                if(scrolls == 20){
                    throw new IllegalArgumentException("Element is not visible even after 20 scroll by 300 each. it should be ignored.");
                }
            }
            executeJScriptCanThrowsException("window.scrollTo(0," + element.getLocation().getY() + ")");
            TimeUtils.waitFor(1000);
        } catch (Exception e) {
            if(e instanceof IllegalArgumentException){
                throw e;
            } else {
                LOGGER.error(e.getMessage());
            }
        }
    }

    protected WebElement getFriendsLikesElementForFBAd(WebElement userContentWrapper){
        final WebElement h6Based = findElementSafe(By.xpath(".//h6"), userContentWrapper);
        final WebElement friendsLikes = h6Based != null ? findElementSafe(By.xpath(".//h5"), userContentWrapper) : null; // friends' likes
        return friendsLikes;
    }

    protected WebElement getAdHeaderForFBAd(WebElement element){
        WebElement adHeader = findElementSafe(By.xpath(".//div[@class='_5g-l']"), element);
        return adHeader;
    }

    protected void removeElement(JavascriptExecutor js, WebElement element){
        if(js != null && element != null){
            try {
                executeJScript("arguments[0].remove();", element);
            } catch (StaleElementReferenceException e) {
                LOGGER.info("cannot delete an element because it is no longer attached to DOM.");
            }
        }
    }
    protected void removeElements(List<WebElement> elements){
        final JavascriptExecutor jse = (JavascriptExecutor)driver ;
        for(WebElement el : elements){
            removeElement(jse, el);
        }
    }
    protected void cleanUpFbAdFromUnwantedItems(WebElement element){
        final WebElement userContentWrapper = findElementSafe(By.xpath(".//div[contains(@class,'fbUserContent')]"), element);
        final JavascriptExecutor jse = (JavascriptExecutor)driver ;
        /**
         * first of all, in facebook desktop; there are some ads that are hidden
         */
        final WebElement adHeader = getAdHeaderForFBAd(userContentWrapper);
        removeElement(jse, adHeader);
        final WebElement friendsLikes = getFriendsLikesElementForFBAd(userContentWrapper);
        removeElement(jse, friendsLikes);
        final WebElement pageLikeButton = findElementSafe(By.xpath(".//button[contains(@class, 'PageLikeButton')]"), userContentWrapper); // Page like button
        removeElement(jse, pageLikeButton);
        final WebElement popUpToggleArrow = findElementSafe(By.xpath(".//a[@rel='toggle' and @aria-haspopup]"), userContentWrapper);
        removeElement(jse, popUpToggleArrow);
        final WebElement interactionBtns = findElementSafe(By.xpath(".//form"), userContentWrapper); // Like, comment and share Buttons
        removeElement(jse, interactionBtns);
        final List<WebElement> buttons = findElementsSafe(By.xpath(".//button | .//div[contains(@class,'rfloat')]//div//a[@role='button']"), element);
        if (buttons != null) {
            buttons.forEach(t -> removeElement(jse, t));
        }

        final WebElement seeMore = findElementSafe(By.xpath(".//a[@class='see_more_link']"), element);
        if(seeMore != null){
            executeJScript("var element=arguments[0];idOld=element.id;element.id='id1';document.getElementById('id1').click();element.id=idOld;", seeMore);
        }
        /**
         * In case it s an event,
         * we need to delete event time and pariticipants
         * event time => span with class eventTime
         */
        final WebElement eventTime = findElementSafe(By.xpath(".//span[@class='eventTime']/../.."), userContentWrapper);
        if(eventTime != null){
            LOGGER.debug("found an event, we will delete event time and participants number");
            final WebElement participantsNumber = findElementSafe(By.xpath("./following::div[1]"), eventTime);
            removeElement(jse, participantsNumber);
            removeElement(jse, eventTime);
        }
        final WebElement numberOfPageLikes = findElementSafe(By.xpath(".//div[@class='_5qg4' and (contains(text(), 'like') or contains(text(), 'aime') )]"), element);
        if(numberOfPageLikes != null ){
            LOGGER.info("i will delete number of page likes, text is {}", getInnerTextFromElement(numberOfPageLikes));
            removeElement(jse, numberOfPageLikes);
        }
    }

    public void extractFBRightColumn(String idExtraction, String idPage, String profileHref) {
        try {
            executeJScript("document.body.style.backgroundColor = 'white';");
            //treatement of ads of the right zone
            int scrollTime = 0;
            lastpositionYRightColumn = 0;
            fbAdsRightColumn.clear();
            final int maxScrolls = Utils.getRandom(20, 40);
            LOGGER.info("i will scroll {} times.", maxScrolls);
            while (scrollTime < maxScrolls) {
                // If "moreBlock" button, click on it to show more articles in the feed and process the scrolling
                try {
                    WebElement morePagesPrimary = driver.findElement(By.xpath(".//a[contains(@class, 'uiMorePagerPrimary')]"));
                    executeJScriptCanThrowsException("arguments[0].click();", morePagesPrimary);
                }
                catch (Exception e) { }
                scrollTime++;

                //Zone 3 processing
                processFBRightColumn(idExtraction, idPage, "3", profileHref);
                //Scroll & move mouse
                LOGGER.info("scrolling fb page RightCol for ads : {}",scrollTime);
                TimeUtils.waitFor(Utils.getRandom(2000, 5000));
                executeJScript("window.scrollBy(0,600)");
                guiHelper.moveMouse();
            }
        } catch (Exception e) {
            LOGGER.error(e.getMessage());
        }
    }

    public void extractFBAds(String idExtraction, String idPage, Pair<String, List<String>> possibleDepth) {
        try {
            int scrollTime = 0;
            lastpositionYTimeline = 0;
            lastpositionYRightColumn = 0;
            fbAdsTimeline.clear();
            fbAdsRightColumn.clear();
            processedAds.clear();
            xPosition = 0;
            final int maxScrolls = Utils.getRandom(20, 40);
            LOGGER.info("i will scroll {} times.", maxScrolls);
            while (scrollTime < maxScrolls) {
                scrollTime++;
                //Process right column ads
                // /!\ in mobile mode, this method does nothing.
                processFBRightColumn(idExtraction, idPage, "2", "");
                //Process timeline ads
                extractFBTimelineAds(idExtraction, idPage, possibleDepth.getRight());
                //Scroll & move mouse
                LOGGER.info("scrolling fb page for ads : {}",scrollTime);
                executeJScript("window.scrollBy(0,600)");
                guiHelper.moveMouse();
                TimeUtils.waitFor(Utils.getRandom(2000, 5000));
            }
        } catch (Exception e) {
            LOGGER.error(e.getMessage(),e);
        }
    }

    protected String getRealUrlFB(String fileUrl) {
        try {
            fileUrl= Utils.unescapeDataString(fileUrl);
            fileUrl = getRealUrl(fileUrl, false);
            final String is = RegExService.findFirst("[a-zA-Z0-9_]*\\.(jpg|png|gif|jpeg)",fileUrl,false,0);
            if(!StringUtils.isBlank(is))
                return is;
            return fileUrl;
        } catch (Exception e) {
            return fileUrl;
        }

    }

    private void cleanUpRightColFBAd(WebElement ad){
        final WebElement likeBtn = findElementSafe(By.xpath(".//a[@role='button' and contains(@class, 'uiIconText')]/parent::div/parent::div/parent::div"), ad);
        final WebElement joinBtn = findElementSafe(By.xpath(".//a[@role='button' and contains(@id, 'joinButton')]"), ad);
        final WebElement eventBtn = findElementSafe(By.xpath(".//a[@role='button' and contains(@class, 'emuEventfad')]/parent::div/parent::div/parent::div"), ad);
        final WebElement like = findElementSafe(By.xpath(".//div[contains(text(), 'aime') or contains(text(), 'like')]"), ad);
        final WebElement close = findElementSafe(By.xpath(".//a[contains(@class, 'uiCloseButton')]"), ad);
        final JavascriptExecutor jse = (JavascriptExecutor) this.driver;

        final WebElement playingPeople = findElementSafe(
                By.xpath("//div[(contains(text(), 'play') or contains(text(), 'jouent')) and preceding-sibling::span[text()=' · ']]"), ad);
        if(playingPeople != null){
            LOGGER.warn("check what im deleting : text is {}", getInnerTextFromElement(ad));
        }
        removeElement(jse, playingPeople);
        removeElement(jse, likeBtn);
        removeElement(jse, joinBtn);
        removeElement(jse, eventBtn);
        removeElement(jse, like);
        removeElement(jse, close);
        final List<WebElement> allButtons = findElementsSafe(By.xpath(".//a[@role='button' and (text()='Like Page' or text()='Aimer la page') ] "), ad);
        if(allButtons != null){
            for(WebElement elem : allButtons){
                LOGGER.warn("i deleted a link with role=button : text is {}", getInnerTextFromElement(ad));
                removeElement(jse, elem);
            }
        }
        final WebElement ratingStars = findElementSafe(By.xpath(".//div[contains(@class, 'uiStars')]"), ad);
        if(ratingStars != null){
            LOGGER.warn("i deleting the rating stars, text is {}", getInnerTextFromElement(ad));
            removeElement(jse, ratingStars);
        }
    }

    private String getRightColAdDestination(WebElement ad, String profileHref){
        final List<WebElement> links = findElementsSafe(By.xpath(".//a"), ad);
        String hrefDestination = "";
        for (WebElement destination : links) {
            if (destination != null && destination.getAttribute("class") != null
                    && !destination.getAttribute("class").contains("uiCloseButton")
                    && destination.getSize().getHeight() >= 50) {
                hrefDestination = destination.getAttribute("href");
                if(StringUtils.isBlank(hrefDestination)){
                    continue;
                }
                final boolean isProfileHref = hrefDestination.equals(profileHref + "#");
                if(!StringUtils.isBlank(profileHref)){
                    if(hrefDestination != null && isProfileHref){
                        hrefDestination = destination.getAttribute("ajaxify");
                    }
                }
                if(StringUtils.isBlank(hrefDestination)){
                    hrefDestination = "";
                    continue;
                }
                try {
                    final String finalUrl = fileManagerService.getFinalURL(hrefDestination);
                    hrefDestination = this.getFbAdsDestination(finalUrl);
                } catch (Exception e) {
                    hrefDestination = this.getFbAdsDestination(hrefDestination);
                }
                if(!StringUtils.isBlank(hrefDestination)){
                    return hrefDestination;
                }
            }
        }
        return hrefDestination;
    }

    protected void processFBRightColumn(String idExtraction, String idPage, String idZone, String profileHref) {
        try {
            LOGGER.debug("processing FB RightCol for extraction {}", idExtraction);
            final String rightColSelector = "//div[contains(@class,'ego_unit_container')]//div[contains(@class,'ego_unit') and @data-ego-fbid and .//*[contains(@data-gt,'ads_xout')]]";
            final List<WebElement> ads = findElementsSafe(By.xpath(rightColSelector), null);
            if(ads == null ||ads.isEmpty()){
                return;
            }
            LOGGER.info("right col ads processing, size is {}.", ads);
            for (WebElement ad : ads) {
                if(processedAds.contains(ads.hashCode())){
                    continue;
                }
                lastpositionYRightColumn++;
                LOGGER.debug("processing FB RightCol ad");
                try {
                    LOGGER.info("processing right col ad, zone = {}", idZone);
                     // cleaning ad from unwanted items
                    cleanUpRightColFBAd(ad);
                     // media id processing

                    final List<WebElement> imgTags                = findElementsSafe(By.xpath(".//img"), ad);
                    final List<WebElement> iWithBackgroundUrlTags = findElementsSafe(By.xpath(".//i[contains(@style, 'background-image:') or contains(@style, 'background:')]"), ad);
                    final WebElement videoTag                     = findElementSafe(By.xpath(".//video"), ad);

                    final List<String> imgsUrls = new ArrayList<>();
                    final String videoUrl       = videoTag != null ? videoTag.getAttribute("src") : "";
                    /**
                     * We'll get images sources from IMG tags, it's the 'src' attribute
                     */
                    imgTags.forEach(t -> {
                        imgsUrls.add(t.getAttribute("src"));
                    });
                    /**
                     * in case when it's an 'i' tag, with bachgroung url
                     * we'll filter not null
                     * and get the attribute style then parse it with regex to obtain background or background-url
                     */
                    iWithBackgroundUrlTags.stream()
                            .filter(t -> t!= null)
                            .map(t -> t.getAttribute("style"))
                            .forEach(styleAttr -> {
                                final String url = RegExService.findFirst("((background-image[ ]?:|background[ ]?:).*?url\\(['|\"]?(.*?)['|\"]?\\))", styleAttr, true, 3);
                                imgsUrls.add(url);
                            });
                    /**
                     * Then we'll calculate the media_id from the pattern above
                     */
                    ChecksumFileResponse videoChecksum = null;
                    try {
                        if(videoTag != null && videoUrl != null) {
                            videoChecksum = getChecksumFile(videoUrl, false);
                        }
                    } catch (IOException e) {
                        LOGGER.error("orror occured during getChecksumFile of video {}, exception : {}", videoUrl, e);
                        videoChecksum = null;
                    }
                    /**
                     * content
                     */
                    String initialContent = getInnerTextFromElement(ad);
                    String content = initialContent;
                    content = RegExService.cleanRightFbAdFromLikes(content);
                    if(!StringUtils.equals(initialContent, content)){
                        LOGGER.info("content changed for this ad by regex. initial was : {} , we'll use into cheksum : {}", initialContent, content);
                    }
                    final String stringVideoChecksum = videoChecksum != null ? videoChecksum.getChecksumFile() : Constants.CHECKSUMEMPTY;
                    final String descriptionChecksum = fileManagerService.getChecksumText(content);
                    final GetChecksumOfAllImagesOnPageResp imagesChecksum = getImagesCheksum(imgsUrls);
                    /**
                     * Finally the media_id
                     */
                    final String mediaId = fileManagerService.getChecksumText(descriptionChecksum + imagesChecksum.getHtml5ElementChecksum() + stringVideoChecksum);// + idZone;
                    LOGGER.info("got mediaId : {} for right col ad, zone is : {}", mediaId, idZone);
                    /**
                     * detect destination url
                     */
                    final String hrefDestination = getRightColAdDestination(ad, profileHref);
                    final IsNewMediaResponse isNew = isNewFbMedia(mediaId, idZone);
                    /**
                     * In case when it's a new media, we need to process it and save it
                     */
                    if(isNew.isNewVisual()){
                        try {
                            String text = null;
                            try {
                                text = getHtmlOfWebElement(ad);
                            } catch (IllegalStateException e) {
                                LOGGER.error("A script is busy on this ad, it will be ignored. exception : {}.", e);
                                processedAds.add(ad.hashCode());
                                continue;
                            }
                            saveAndUploadFbMedia(ad, mediaId, idZone, imagesChecksum.getImages2Convert64(), videoTag, videoChecksum, text);
                        } catch (Exception e) {
                            LOGGER.error("Cannot finalize fb ad save, exception is {}.", e);
                        }
                    }

                    /**
                     * sizeofElement
                     */
                    final Dimension adSize = ad.getSize();
                    /**
                     * fileSize
                     */
                    final long fileSize = fileManagerService.getSizeOfFbMedia(mediaId+idZone);
                    /**
                     * original url
                     */
                    final String originalUrl = "";
                    /**
                     * Insert the impression in the database
                     */
                    databaseAPIConnector.insertImpression(getRobotData(), idExtraction, MediaType.FACEBOOK, content, AdNetwork.FACEBOOK, mediaId ,
                            originalUrl, hrefDestination, idZone, idPage, sizeScreen, adSize,
                            new Point(1, lastpositionYRightColumn), "", isNew, fileSize);

                    processedAds.add(ad.hashCode());
                    LOGGER.info("end processing right col ad.");
                } catch (Exception e) {
                    LOGGER.error(e.getMessage());
                }
            }
            LOGGER.info("end right col ads processing.");
        } catch (Exception e) {
            LOGGER.error(e.getMessage(),e);
        }
    }

    public boolean checkFbProfilConnected() {
        final List<WebElement> tmp = findElementsSafe(By.xpath("//div[@data-referrer='pagelet_composer']"), null);
        if (tmp==null ||tmp.size() == 0) {
            LOGGER.error("Cannot connect to Facebook");
            return false;
        }
        return true;
    }

    protected String geFBProfilXpathSelector(){
        return "//div[contains(@data-referrer, 'pagelet_bluebar')]//a[contains(@title, 'Profil')]";
    }

    public String getFbProfileHref() {
        final WebElement profil = findElementSafe(By.xpath(geFBProfilXpathSelector()), null);
        if(profil != null && profil.getAttribute("href") != null){
            return profil.getAttribute("href");
        }
        return null;
    }

    @Override
    public List<String> getPossibleMedias() {
        return Arrays.asList("//div[contains(@data-testid,'fbfeed_story') and (.//a[contains(@href, '/ads/about')] or .//a[contains(text(),'Sponsorisé')] or .//a[contains(text(),'Sponsored')])]") ;
    }

    protected String getFbAdsDestination(String destinationFromLink){
        LOGGER.debug("fbAdsDestination got url : " + destinationFromLink);
        if(StringUtils.isBlank(destinationFromLink)){
            LOGGER.debug("fbAdsDestination will return empty string for entry : " + destinationFromLink);
            return "";
        }
        try {
            String tmp = destinationFromLink;
            if(StringUtils.isBlank(tmp)){
                LOGGER.debug("fbAdsDestination will return empty string for entry : " + destinationFromLink);
                return "";
            }
            if (tmp.contains("facebook.com/l.php?u=") || tmp.contains("facebook.com/a.php?u=")
                    || tmp.contains("facebook.com/flx/warn/?u=")) {
                tmp = Utils.parseQueryString(tmp).get("u");

            }
            if(tmp.contains("login/?next=")){
                tmp = Utils.parseQueryString(tmp).get("next");
                int iter=0;
                while((tmp.startsWith("http%") || tmp.startsWith("https%")) && !(tmp.startsWith("http//") || tmp.startsWith("https//")) || iter <5){
                    LOGGER.info("trying to unescape fb url undefinitly");
                    tmp = Utils.unescapeDataString(tmp);
                    iter++;
                }
                if(tmp.contains("l.facebook.com/") && !tmp.contains("l.facebook.com/?u=")){
                    tmp=tmp.substring(tmp.lastIndexOf("l.facebook.com/")+15, tmp.length()).replace("%3F", "?");
                } else if(tmp.contains("lm.facebook.com/") && !tmp.contains("lm.facebook.com/?u=")){
                    tmp=tmp.substring(tmp.lastIndexOf("lm.facebook.com/")+16, tmp.length()).replace("%3F", "?");
                }
            }
            if(!tmp.contains("http")){
                tmp = destinationFromLink;
            }
            final String decodedUrl = Utils.unescapeDataString(tmp);

            if(decodedUrl.contains("get.adobe.com/flashplayer")){
                LOGGER.debug("fbAdsDestination will return empty string for entry : " + destinationFromLink);
                return "";
            }
            if(decodedUrl.equalsIgnoreCase("https://www.facebook.com/#")
                    || decodedUrl.equalsIgnoreCase("https://www.facebook.com/")){
                LOGGER.info("fbAdsDestination will return empty string for entry : " + destinationFromLink);
                return "";
            }
            // to not try to optimise :-)
            if(decodedUrl.startsWith("http:/") || decodedUrl.startsWith("https:/")){
                return decodedUrl;
            } else if(decodedUrl.startsWith("http")) {
                return Utils.unescapeDataString(decodedUrl);
            } else if(decodedUrl.startsWith("//")) {
                return "https:" + decodedUrl;
            } else if(decodedUrl.startsWith("/")){
                return "https://www.facebook.com" + decodedUrl;
            } else {
                return decodedUrl;
            }
        } catch (UnsupportedEncodingException e) {
            LOGGER.debug("fbAdsDestination error occured : {}" + e);
            LOGGER.debug("fbAdsDestination will return empty string for entry : " + destinationFromLink);
            return "";
        }
    }

    protected GetChecksumOfAllImagesOnPageResp getImagesCheksum(List<String> imagesUrls) {
        try {
            final List<String> checksumHTML5Elements = new ArrayList<>();
            final List<ImageUrlFileName> images2Convert64 = new ArrayList<>();
            for(String imageUrl : imagesUrls){
                if (imageUrl != null) {
                    if (imageUrl.startsWith("http")) {
                        final String proceedingUrl = fileManagerService.getImageUrlFromFbSrc(imageUrl);

                        ChecksumFileResponse tmp = null;
                        try {
                            tmp = getChecksumFile(proceedingUrl, false);
                        } catch (IOException e) {
                            LOGGER.error("Cannot get cheksum of image, url : {} .", proceedingUrl);
                            continue;
                        }
                        final String realProceedingUrl = getRealUrlFB(proceedingUrl);
                        if(!StringUtils.isBlank(realProceedingUrl) && !realProceedingUrl.contains("http")){
                            checksumHTML5Elements.add(fileManagerService.getChecksumText(realProceedingUrl));
                        } else {
                            checksumHTML5Elements.add(tmp.getChecksumFile());
                        }
                        images2Convert64.add(new ImageUrlFileName(StringEscapeUtils.escapeHtml4(imageUrl),tmp.getTempFileName()));
                        //fileManagerService.deleteFromMedia(tmp.getTempFileName());
                    } else if (imageUrl.startsWith("data:")) {
                        checksumHTML5Elements.add(fileManagerService.getChecksumText(imageUrl));
                    }
                }
            }
            List<String> tmp = checksumHTML5Elements.stream()
                    .distinct()
                    .sorted()
                    .collect(Collectors.toList());
            String concatChecksum = String.join("", tmp);
            String html5ElementChecksum = fileManagerService.getChecksumText(concatChecksum);
            return  new GetChecksumOfAllImagesOnPageResp(html5ElementChecksum, images2Convert64);
        } catch (Exception e) {
            LOGGER.error(e.getMessage());
        }
        return new GetChecksumOfAllImagesOnPageResp(Constants.CHECKSUMEMPTY, new ArrayList<>());
    }

    protected void saveAndUploadFbMedia(WebElement ad, String mediaId, String zone, List<ImageUrlFileName> imagesToConvert, WebElement videoTag, ChecksumFileResponse videoChecksum, String text) throws Exception{
        /**
         * Saving and uploading media
         * RG :
         *      . if it contains a video : a folder will be created with name=media_id, the video with name 'its checksum' and format 'mp4' will be in this folder
         *      . all computed styles of the element will be grabbed to style attibute of each tag of the ad
         *      . images will be transformed to base64
         *
         *
         */
        LOGGER.info("i will save and upload a new visual/media for fb, checksum is {}, and zone is {}.", mediaId, zone);
        final String mediaFBFolderPath =  mediaId + zone + FB_FOLDER_SUFFIX;
        final String stringVideoChecksum = videoChecksum != null ? videoChecksum.getChecksumFile() : Constants.CHECKSUMEMPTY;
        String videoSrcInMedia = "";
        if(videoTag != null){
            /**
             * create folder for the ad, it will contains the video tag
             * Then upload it to s3
             */
            try {
                LOGGER.info("found a video tag, src is : " + videoTag.getAttribute("src"));
            } catch (Exception e) {}
            fileManagerService.createDirectory(fileManagerService.getMediaPath() +"/"+mediaFBFolderPath );
            videoSrcInMedia = mediaFBFolderPath + "/" + stringVideoChecksum;
            fileManagerService.copyFileInMedia(videoChecksum.getTempFileName(), videoSrcInMedia);
            awsConnector.upload(videoSrcInMedia, MediaType.VIDEO, true);
        }


        for (String found : RegExService.getMatches("(<iframe.*?><\\/iframe>)",text,0)) {
            text = text.replace(found, "");
        }
        String reg = "(data-.*?\".*?\")" +
                "|(ajaxify.*?\".*?\")" +
                "|(onmousedown=\".*?\")" +
                "|(href=\".*?\")" +
                "|(<iframe.*?><\\/iframe>)" +
                "|(target=\".*?\")" +
//                "|(role=\".*?\")" +
//                "|(class=\".*?\")" +
                "|(id=\".*?\")" +
                "|(onmouse.*?=\".*?\")" +
                "|(onclick.*?=\".*?\")";
        final List<String> toremove = RegExService.getMatches(reg,text,0);
        for (String found : toremove) {
            text = text.replace(found, "");
        }

        text = fileManagerService.replaceImagesWithBase64(text, imagesToConvert);
        text = text
                .replace("&quot;", "'")
                .replace("&amp;cfs=1", "")
                .replace("&amp;upscale=1", "")
                .replace("&amp;","&");
        if (videoTag != null) {
            final String videoReplacement;
            if(Utils.isDevMode(mode)){
                videoReplacement = "##PIXI##/" + stringVideoChecksum;
            } else {
                videoReplacement = "##PIXI_STORAGE##/" + stringVideoChecksum;
            }
            final String videoUrl       = videoTag.getAttribute("src");
            text = text.replace("muted=\"1\"", "")
                    .replace(StringEscapeUtils.unescapeHtml4(videoUrl), videoReplacement)
                    .replace("<video ", "<video controls ");
        }
        text = fileManagerService.getFileContentAsString(getFbAdHtmlFileName())
                .replace("##TEXT##",text);

        int adHeight = ad.getSize().getHeight() + 35;
        int adWidth  = ad.getSize().getWidth()  + 35;
        if(adHeight < 350){
            adHeight = getFbAdHeight(adHeight);
        }
        if(adWidth < 360){
            adWidth = getFbAdWidth();
        }

        text = text.replace("##AD_WIDTH##", String.valueOf(adWidth));
        text = text.replace("##AD_HEIGHT##", String.valueOf(adHeight));

        if(videoTag != null){
            final String jsToExecuteForPlayingVideos = fileManagerService.getFileContentAsString(getFbPlayVideoFileName());
            text = text.replace("##JS##", jsToExecuteForPlayingVideos);
        } else {
            text = text.replace("##JS##", "");
        }
        //UPLOAD FILE
        text = text.replace("##PIXI##", mediaFBFolderPath);

        final String mediaAndZone = mediaId+zone;

        try {
            fileManagerService.createFileInMedia(mediaAndZone, text);
            awsConnector.upload(mediaAndZone, MediaType.FACEBOOK, true);
        } catch (IOException e) {
            LOGGER.error("exception during create file in media or upload to aws, exception is {}.", e);
        }
    }
    protected int getFbAdWidth(){
        return 530;
    }
    protected int getFbAdHeight(int adHeight){
        return 700;
    }
    protected String getFbPlayVideoFileName(){
        return "js/fb-play-video.js";
    }
    protected String getFbAdHtmlFileName(){
        return "html/fb-ad.html";
    }

    protected String getAdDestionation(WebElement ad, boolean isSingleAd, WebElement descriptionElement) {
        String hrefDestination = "";
        try {
            final WebElement userContentWrapper = findElementSafe(By.xpath(".//div[contains(@class,'fbUserContent')]"), ad);
            final WebElement userContent        = userContentWrapper != null ? findElementSafe(By.xpath(".//div[contains(@class,'userContent')]"), userContentWrapper) : null;
            final WebElement last                 = userContent == null ? null : findElementSafe(By.xpath("./following-sibling::div"), userContent);

            if (last != null && last.getSize().getHeight() > 0 && last.getSize().getWidth() > 0) {
                final List<WebElement> destinations = findElementsSafe(By.xpath(".//div[contains(@class,'mtm')]//a"), last);
                if (destinations != null) {
                    for(WebElement element : destinations){
                        final String destinationFromElement = element.getAttribute("href");
                        try {
                            final String finalUrl = fileManagerService.getFinalURL(destinationFromElement);
                            hrefDestination = this.getFbAdsDestination(finalUrl);
                        } catch (Exception e) {
                            hrefDestination = this.getFbAdsDestination(destinationFromElement);
                        }
                        if(!StringUtils.isBlank(hrefDestination)){
                            LOGGER.debug("destination from element is {} .", destinationFromElement);
                            break;
                        }
                    }
                }
            } else {
                final WebElement toSearchInto = userContentWrapper != null ? userContentWrapper : ad;
                final List<WebElement> possible = findElementsSafe(By.xpath(".//a"), toSearchInto);
                for (WebElement destination : possible) {
                    if (destination.getSize().getHeight() >= 100) {
                        final String destinationFromElement = destination.getAttribute("href");
                        try {
                            final String finalUrl = fileManagerService.getFinalURL(destinationFromElement);
                            hrefDestination = this.getFbAdsDestination(finalUrl);
                        } catch (Exception e) {
                            hrefDestination = this.getFbAdsDestination(destinationFromElement);
                        }
                        LOGGER.debug("destination from element is {} .", destinationFromElement);
                        break;
                    }
                }
            }
            return hrefDestination;
        } catch (StaleElementReferenceException e) {
            LOGGER.error("cannot detect destination, element is no longer attached to DOM, exception is : {}", e);
            return "";
        }
    }


    private String extractVideoIdFromShahidUrl(String url) {
        return RegExService.findFirst("\\/series\\/(\\d*+)\\/", url, true, 1);
    }
    protected String goToUrlAndGetSource(String url){
        try {
            goToURL(url);
            return driver.getPageSource();
        } catch (ProblemLoadingPageException e) {
            LOGGER.error("Cannot go to url {}, error", url, e);
            return "";
        }
    }


    protected Dimension getShahidPlayerDimensions(){
        return new Dimension(960, 540);
    }
    protected Point getShahidPlayerLocation(){
        return new Point(312, 85);
    }
}
