package ma.labs.bot.core;

import ma.labs.bot.data.AdNetwork;
import ma.labs.bot.data.util.MediaType;
import ma.labs.bot.utils.Constants;
import ma.labs.bot.utils.TimeUtils;
import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxProfile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created by Mohamed on 12/10/2016.
 */

/**
 * Please increment this counter each time a modification is done in FACEBOOK xpath's
 * 1. add sponsored in audience fb selector
 * 2. change check on connected profil for desktop, from test on userContentWraper to the pagelet
 * 3. Sponsored text written in timeline ads needs to be removed from content : new xpath .//span/a[text()='Sponsorisé' or text()='Sponsored']/..
 */
@Component("mobileScraper")
public class MobileScraper extends Scraper implements IScraper {
    private static Logger logger = LoggerFactory.getLogger(MobileScraper.class);

    /*@Override
    public void checkProfileConnected() {
        WebElement listMails = null;
        try {
            goToURL("https://mail.google.com/mail/mu/mp/409/#tl/priority/%5Esmartlabel_personal");
            logger.debug("trying to find mails list");
            TimeUtils.waitFor(8000);
            listMails =findElementSafe(By.xpath("//div[contains(@role,'listitem')]"), null, true);
        } catch (Exception e) {}
        if(listMails == null){
            throw new RuntimeException("Profile not connected, Robot will shutdown ");
        }
        logger.info("first mail found :  {}", listMails.getText());
    }*/

    @Override
    public String getYoutubeVideoXpathSelectorForMain(){
        return "//a[div][contains(@href,'/watch?v=') and count(div)=1]";
    }
    @Override
    public String getYoutubeVideoXpathSelectorForSideBar(){
        return "//a[div][contains(@href,'/watch?v=') and count(div)=1]";
    }
    @Override
    protected String getPrerollDestinationVideoXpathExpression(){
        return "//div[@class='videoAdUiLearnMore']";
    }
    @Override
    protected String getPrerollVideoSkipButtonXpathExpression(){
        return "//button[contains(@class,'videoAdUiSkipButton')]";
    }
    @Override
    protected void setPreferences(FirefoxProfile profile) {
        super.setPreferences(profile);
        profile.setPreference("general.useragent.override", robotData.getDeviceUserAgent());
        //set mediasource.enabled to false, to get youtube video pre-rolls in url mode
        profile.setPreference("media.mediasource.enabled", false);
        profile.setPreference("media.mediasource.webm.enabled", false);
        logger.debug("set both preferences media.mediasource.enabled and media.mediasource.webm.enabled values to false");
    }
    @Override
    protected void resizeWindow(){
        driver.manage().window().setSize(new Dimension(robotData.getDeviceWidth(), robotData.getDeviceHeight()));
    }
    @Override
    public String getIdPrerollVideo() {
        try {
            List<WebElement> elements = driver.findElements(By.xpath("//video"));
            if(!elements.isEmpty()){
                return elements.get(0).getAttribute("src");
            } else {
                return "";
            }
        } catch (Exception e) {
            logger.error(e.getMessage(),e);
            return "";
        }
    }
    @Override
    public void processYoutubeVideo(String idExtraction, String idPage) throws Exception{

        TimeUtils.waitFor(4000); //Wait 4 seconds before processing the pre-roll ad
        if(isScreenshotON){
            TimeUtils.waitFor(500);
            String screen = "screen-proc-"+idExtraction+ Constants.JPG;
            File file = guiHelper.captureScreenShot(driver,null,fileManagerService.getMediaPath()+"/"+screen);
            ftpConnector.upload(file);
        }
        if (isPrerollVideo()) {
            pauseVideo();
            String idPrerollVideo = getIdPrerollVideo();
            //we propcess to pre-roll video only if it's detected and not empty
            if(!"".equals(idPrerollVideo)){
                logger.debug("preroll ID (idPrerollVideo) {}",idPrerollVideo);

                String destinationUrl = "",  duration = "";
                IsNewMediaResponse isNew;
                long fileSize = -1;

                MediaHelperAPIResponse mediaHelperAPIResponse;
                try {
                    mediaHelperAPIResponse = this.mediaHelperAPIConnector.downloadByFileUrl(idPrerollVideo);
                } catch (IOException e) {
                    mediaHelperAPIResponse = null;
                }
                if(null == mediaHelperAPIResponse){
                    logger.error("MediaHelperAPIConnector returned null on preroll url : {}", idPrerollVideo);
                    logger.info("processYoutubeVideo will stop processing for preroll : {}", idPrerollVideo);
                    extractAdsPage(idExtraction, idPage);
                    return;
                }

                final String checksumFile = mediaHelperAPIResponse.getChecksum();
                isNew = this.isNewMedia(checksumFile);

                if(isNew.isNewMedia() || isNew.isNewVisual()){
                    if(isNew.isNewMedia()){
                        logger.debug("==will=upload======>{}", mediaHelperAPIResponse.getChecksum());
                        this.mediaHelperAPIConnector.uploadByFileUrl(idPrerollVideo); // upload file (async)
                        fileSize = mediaHelperAPIResponse.getFileSize();
                        duration = String.valueOf(mediaHelperAPIResponse.getDuration());
                    }
                    destinationUrl = getDestinationVideo();
                }

                Dimension size = new Dimension(0, 0);
                Point location = new Point(0, 0);
                try {
                    WebElement video = driver.findElements(By.xpath("//div[@id='player']")).get(0);
                    size = video.getSize();
                    location = video.getLocation();
                } catch (Exception e) {
                    logger.error("error occured when trying to get video player size and location, details : {}", e.getMessage());
                }
                databaseAPIConnector.insertImpression(getRobotData(),idExtraction, MediaType.VIDEO, "", AdNetwork.YOUTUBE,
                        checksumFile, idPrerollVideo, destinationUrl, "null", idPage,sizeScreen, size,
                        location, duration, isNew, fileSize);
                skipVideoAd();
                TimeUtils.waitFor(2000);
                pauseVideo();
            } else {
                logger.error("pre-roll video will be ignored, it's source cannot be defined by known methods.");
            }
        }
        extractAdsPage(idExtraction, idPage);
    }
    @Override
    public List<String> getPossibleMedias() {
        return Arrays.asList("//iframe[@class='fbEmuTracking']/..") ;
    }
    @Override
    protected String geFBProfilXpathSelector(){
        return "//form[@id= 'mbasic_inline_feed_composer']//i[@class='img profpic']/..";
    }
    @Override
    public boolean checkFbProfilConnected() {
        TimeUtils.waitFor(1000);
        final List<WebElement> tmp = findElementsSafe(By.xpath("//form[@id= 'mbasic_inline_feed_composer']"), null);
        if (tmp==null || tmp.isEmpty()) {
            logger.error("Cannot connect to Facebook mobile");
            return false;
        }
        return true;
    }
    //
    /**
     * saves for xpath
     *  //iframe[@class='fbEmuTracking']/parent::article//div[@data-sigil='m-feed-story-attachments-element']
     * //article[child::iframe[@class='fbEmuTracking']]/div[1]
     */
    static final String FB_SINGLE_AD_XPATH_SELECTOR = "//article[child::iframe[@class='fbEmuTracking'] and (//span[contains(text(),'Sponsorisé')] or //span[contains(text(),'Sponsored')])]/div[1]";
    static final String FB_BLOCK_OF_ADS_XPATH_SELECTOR = "//div[contains(@class, 'scrollAreaBody')]//div[@data-sigil='story-div'] | //div[@data-sigil=\"story-div\"and (//*[contains(text(),'Sponsorisé')] or //*[contains(text(),'Sponsored')])]";

    protected void extractFBTimelineAds(String idExtraction, String idPage, List<String> possibleMedias) {
        /**
         * ad element
         *    sequence :
         *      single ads selector : //iframe[@class='fbEmuTracking']/..//div[@data-sigil='m-feed-story-attachments-element']  -> will return a set of single ads
         *      block of ads selector : //div[contains(@class, 'scrollAreaBody')]//div[@data-sigil='story-div']                 -> will return a set of single ads that was in a block of ads
         *      /!\   : ads inside block of ads don't have the same structure as single ads, so there processing won't be the same
         */
        final List<WebElement> singleAds = findElementsSafe(By.xpath(FB_SINGLE_AD_XPATH_SELECTOR), null);
        if(singleAds != null && !singleAds.isEmpty()){
            /**
             * before proceeding to element, some ads contain another  article which contains div[@data-sigil='m-feed-story-attachments-element']
             * and we need to remove the ads already processed by their hashcode
             */
            final List<WebElement> elementsToRemove = new ArrayList<>();
            for(WebElement element : singleAds){
                final List<WebElement> childFakeAds = findElementsSafe(By.xpath(".//article//div[@data-sigil='m-feed-story-attachments-element']"), element);
                if (childFakeAds != null && !childFakeAds.isEmpty()) {
                    elementsToRemove.addAll(childFakeAds);
                }
                if(processedAds.contains(element.hashCode())){
                    elementsToRemove.add(element);
                }
            }
            singleAds.removeAll(elementsToRemove);
            if(singleAds!=null && !singleAds.isEmpty()){
                logger.info("Found single ads of size : " + singleAds.size());
                singleAds.forEach(ad -> {
                    if(ad == null){
                        return;
                    }
                    /**
                     * scroll to element and clean up ad from closing pop-up and like button
                     */
                    try {
                        scrollToElement(ad);
                    } catch (IllegalArgumentException e) {
                        logger.error("Ad will be ignored, after 20 scroll it's still not shown. exception is : {}", e);
                        return;
                    }
                    cleanUpSingleAd(ad);
                    final WebElement sponsored = findElementSafe(By.xpath(getAudienceIconFBXpathSelector(true)), ad);

                    String text = null;
                    try {
                        text = getHtmlOfWebElement(ad);
                    } catch (IllegalStateException e) {
                        logger.error("A script is busy on this ad, it will be ignored. exception : {}.", e);
                        processedAds.add(ad.hashCode());
                        return;
                    }
                    if(sponsored != null){
                        executeJScript("arguments[0].remove();", sponsored);
                        logger.info("removed sponsored row");
                    }
                    /**
                     * then process element
                     * but first, increment the lastpositionYTimeline
                     */
                    lastpositionYTimeline++;
                    processFbAdAndInsertImpression(ad, 1, true, idExtraction, idPage, "1", text);
                    processedAds.add(ad.hashCode());
                    executeJScript("arguments[0].removeAttribute('data-sigil');", ad);
                });
            }

        }
        final List<WebElement> blockOfAds = findElementsSafe(By.xpath(FB_BLOCK_OF_ADS_XPATH_SELECTOR), null);
        if(blockOfAds != null && !blockOfAds.isEmpty()){
            final List<WebElement> tobeDeleted = blockOfAds.stream().filter(ad -> processedAds.contains(ad.hashCode())).collect(Collectors.toList());
            if(tobeDeleted != null && !tobeDeleted.isEmpty()){
                blockOfAds.removeAll(tobeDeleted);
            }
            if(blockOfAds!= null && !blockOfAds.isEmpty()){
                logger.info("Found a new block of ads of size : " + blockOfAds.size());
                /**
                 * restore xPosition to zero
                 * lastpositionYTimeline should be the same for all the block of ads
                 * so we'll increment it once, before the forEach loop
                 */
                xPosition = 0;
                lastpositionYTimeline++;
                blockOfAds.forEach(ad -> {
                    if(ad == null){
                        return;
                    }
                    xPosition++;
                    /**
                     * scroll to element and clean up ad from header(likes + closing pop-up) and like button
                     */
                    try {
                        scrollToElement(ad);
                    } catch (IllegalArgumentException e) {
                        logger.error("Ad will be ignored, after 20 scroll it's still not shown. exception is : {}", e);
                        return;
                    }

                    cleanUpBlockOfAds(ad);
                    final WebElement sponsored = findElementSafe(By.xpath(getAudienceIconFBXpathSelector(false)), ad);

                    String text = null;
                    try {
                        text = getHtmlOfWebElement(ad);
                    } catch (IllegalStateException e) {
                        logger.error("A script is busy on this ad, it will be ignored. exception : {}.", e);
                        processedAds.add(ad.hashCode());
                        return;
                    }
                    if(sponsored != null){
                        executeJScript("arguments[0].remove();", sponsored);
                        logger.info("removed sponsored row");
                    }
                    /**
                     * then process element
                     */
                    processFbAdAndInsertImpression(ad, xPosition, false, idExtraction, idPage, "1", text);
                    processedAds.add(ad.hashCode());
                });
            }
        }

    }
    //region Clean-up ads from unwanted elements
    /**
     * These two xpath selectors should be executed on an ad element, else, they will return a bunch of elements for every post on timeline
     */
    static final String FB_POP_UP_SINGLE_AD_CLOSING_XPATH_SELECTOR = ".//i[contains(@data-sigil, 'story-popup-context')]/../..";
    static final String FB_SINGLE_AD_FAN_LIKE_XPATH_SELECTOR = ".//div[contains(@data-sigil, 'm-fan-action') or contains(@id, 'event_action')]";

    private void cleanUpSingleAd(WebElement ad) {
        /**
         * These elements were detected by xpath : //iframe[@class='fbEmuTracking']/..//div[@data-sigil='m-feed-story-attachments-element']
         * To clean up this element, we need to clean the following :
         *      -> pop-up closing -> xpath : ad//i_with_data-sigil="story-popup-context" ===>
         *                  => --ad--//i[contains(@data-sigil, 'story-popup-context')]/../..
         *      -> like button, if it exists : ad/header_tag/second_div/div/div/second_div ==> if don't exist, pop-up cloging will be second
         *                  => --ad--//div[contains(@data-sigil, 'm-fan-action')]/../..
         */
        List<WebElement> closingPopUp = findElementsSafe(By.xpath(FB_POP_UP_SINGLE_AD_CLOSING_XPATH_SELECTOR), ad);
        List<WebElement> likeButton = findElementsSafe(By.xpath(FB_SINGLE_AD_FAN_LIKE_XPATH_SELECTOR), ad);
        /**
         * To concat the two lists, we need to check first on their nullity !!
         */
        closingPopUp = closingPopUp == null ? new ArrayList<>() : closingPopUp;
        likeButton = likeButton == null ? new ArrayList<>() : likeButton;
        logger.info("cleaning single ad, closingPopUp.size = {} , likeButton.size = {} .", closingPopUp.size(), likeButton.size());
        /**
         * now we are null safe, then we can merge the two lists and remove the elements that contains
         * please note than every list contains one element in most cases
         * but we need to proceed by ad.findElements rather than findElement, findElement will return only the first element
         * And we don't know if there are any hidden item with same selector
         */
        /**
         * before proceeding to elements deletion, if there is a see more , click it
         */
        final WebElement seeMore = findElementSafe(By.xpath(".//span[@data-sigil='more']"), ad);
        if(seeMore != null){
            executeJScript("arguments[0].click();", seeMore);
            logger.info("clicked on seeMore");
        }
        /**
         * Then, in case of events
         * we need to delete dynamic ares
         */
        final WebElement eventCalendar = findElementSafe(By.xpath(".//div[@data-sigil='shareAngoraAttachmentMedia']/parent::div/div[2]/header/h1[2]"), ad);
        final WebElement eventParticipants = findElementSafe(By.xpath(".//div[@data-sigil='shareAngoraAttachmentMedia']/parent::div/div[2]/header/following::div[1]"), ad);
        if(eventCalendar != null){
            executeJScript("arguments[0].remove();", eventCalendar);
            logger.info("removed event calendar scheduling.");
        }
        if(eventParticipants != null){
            executeJScript("arguments[0].remove();", eventParticipants);
            logger.info("removed event participants.");
        }
        Stream.concat(closingPopUp.stream(), likeButton.stream()).forEach(element -> {
            /**
             * here we'll clean the ad from closing pop-up and like button
             */
            executeJScript("arguments[0].remove();", element);
            logger.info("removed like button and/or closing pop-up element");
        });
        /**
         * deleting buttons from element
         */
        final List<WebElement> buttons = findElementsSafe(By.xpath(".//button"), ad);
        if(buttons != null){
            buttons.forEach(t -> {
                executeJScript("arguments[0].remove();", t);
                logger.info("removed button from ad");
            });
        }
        /**
         * In case of games, it may show the number of friends playing the game
         * .//header/header/following::div
         */
        final List<WebElement> friendsOrPeopleUsingOrPlaying = findElementsSafe(By.xpath(".//header/header/following::div"), ad);
        if(friendsOrPeopleUsingOrPlaying != null && !friendsOrPeopleUsingOrPlaying.isEmpty()){
            friendsOrPeopleUsingOrPlaying.forEach(t -> {
                final String content;
                try {
                    content = t.getAttribute("innerText");
                } catch (StaleElementReferenceException e) {
                    return;
                }
                if(!StringUtils.isBlank(content) && StringUtils.indexOfAny(content.toLowerCase(), "amis", "friends", "personnes", "people") > -1){
                    executeJScript("arguments[0].remove();", t);
                    logger.info("removed number of friends playing.");
                }
            });
        }
        /**
         * The last thing to do, in ads with video cases
         * we need to fulfill a logic to get the video as a part of the element.
         */
        final WebElement videoTag = findElementSafe(By.xpath(".//div[@data-sigil='inlineVideo']"), ad);
        if(videoTag != null){
            logger.info("This ad contains a video.");
            executeJScript(fileManagerService.getFileContentAsString("js/PIXIFbMobileVideo.js"), videoTag);
        }
        /**
         * In some cases, the footer of comments and likes persist, we need so to delete it.
         */
        final WebElement footer = findElementSafe(By.xpath(".//footer"), ad);
        if(footer != null){
            executeJScript("arguments[0].remove();", footer);
            logger.info("removed footer.");
        }
        final WebElement peopleLiking = findElementSafe(By.xpath(".//div[(contains(@class,'_22x1') or contains(@class,'_5qg4')) and (contains(text(), 'people like')or contains(text(), 'personnes aiment'))]"), ad);
        if(peopleLiking != null){
            logger.info("will delete number of people liking");
            executeJScript("arguments[0].remove();", peopleLiking);
        }
        logger.info("cleaning single ad - end.");
    }
    static final String FB_BLOCK_OF_ADS_FRIENDS_OR_PAGE_LIKES_XPATH_SELECTOR = "./div[1]";
    static final String FB_BLOCK_OF_ADS_FAN_ACTION_XPATH_SELECTOR = ".//div[contains(@data-sigil, 'm-fan-action') or contains(@data-sigil, 'event')]";
    static final String FB_BLOCK_OF_ADS_FANS_NUMBER_XPATH_SELECTOR = FB_BLOCK_OF_ADS_FAN_ACTION_XPATH_SELECTOR+"/preceding::div[1]";
    private void cleanUpBlockOfAds(WebElement ad) {
        /**
         * These elements were detected by xpath : //div[contains(@class, 'scrollAreaBody')]//div[@data-sigil='story-div']
         * To clean up those elements, all we need it to remove the header part that contains the friends likes or total number of likes of the page
         * the like button is located at the bottom of the ad, it s selector is : //div[contains(@data-sigil, 'm-fan-action')]
         *
         * The header element is usually the first div in the detected ad,
         * but we have to be aware that facebook may not generate that header sometimes, so we'll select the first div
         * then we'll check if it contaisn a link (tag <a></a>) if it's the case, we have to ignore it
         *
         */
        logger.info("cleaning blcok of ads.");
        final List<WebElement> friendsOrPageLikes = findElementsSafe(By.xpath(FB_BLOCK_OF_ADS_FRIENDS_OR_PAGE_LIKES_XPATH_SELECTOR), ad);
        final List<WebElement> fanAction = findElementsSafe(By.xpath(FB_BLOCK_OF_ADS_FAN_ACTION_XPATH_SELECTOR), ad);
        /**
         * proceeding to other elements
         */
        if(friendsOrPageLikes != null && !friendsOrPageLikes.isEmpty()){
            logger.info("cleaning ad of a block of ads, friendsOrPageLikes.size = {} .", friendsOrPageLikes.size());
            /**
             * proceed first by the header, its selector is /div[1]
             */
            friendsOrPageLikes.forEach(element -> {
                if(element == null){
                    return;
                }
                final WebElement linkTagToVerify;
                try {
                    /**
                     * if this element is not null, this mean that the first div of the ad contains the page link, so no friends or page likes
                     */
                    linkTagToVerify = element.findElement(By.xpath(".//a[@data-gt]"));
                    if(linkTagToVerify != null){
                        return;
                    }
                } catch (Exception e) {
                    logger.info("could not retrieve element linkTagToVerify with xpath .//a[@data-gt] from element inside blockofAds.");
                }
                /**
                 * else, it has to be an element that should be removed
                 */
                executeJScript("arguments[0].remove();", element);
                logger.info("removed friends likes or page likes and/or closing pop-up element");
            });
        }
        /**
         * before deleting page fan action, delete page fans action :
         * FB_BLOCK_OF_ADS_FANS_NUMBER_XPATH_SELECTOR
         */
        final WebElement pageFansNumber = findElementSafe(By.xpath(FB_BLOCK_OF_ADS_FANS_NUMBER_XPATH_SELECTOR), ad);
        if(pageFansNumber != null){
            executeJScript("arguments[0].remove();", pageFansNumber);
            logger.info("removed page fans number element");
        }
        if(fanAction != null && !fanAction.isEmpty()){
            logger.info("cleaning ad of a block of ads, fanAction.size = {} .", fanAction.size());
            /**
             * then, clean up the like button
             */
            fanAction.forEach(element -> {
                if(element == null){
                    return;
                }
                /**
                 * here we'll clean the ad from closing pop-up and like button
                 */
                executeJScript("arguments[0].remove();", element);
                logger.info("removed fan action element");
            });

        }
        /**
         * deleting buttons from element
         */
        final List<WebElement> buttons = findElementsSafe(By.xpath(".//button"), ad);
        if(buttons != null){
            buttons.forEach(t -> {
                executeJScript("arguments[0].remove();", t);
                logger.info("removed button from ad");
            });
        }
        logger.info("cleaning blcok of ads - END.");
    }

    protected String getAudienceIconFBXpathSelector(boolean isSingleAd){
        return isSingleAd ? ".//span[contains(text(),'Sponsorisé') or contains(text(),'Sponsored')]/.." : ".//div[contains(text(),'Sponsorisé') or contains(text(),'Sponsored')]";
    }
    //endregion
    @Override
    protected String getDescriptionFBXpathSelector(boolean isSingleAd){
        return isSingleAd ? "./*[2]" : "./a";
    }

    @Override
    protected String getOriginalUrl(WebElement descriptionElement){
        if(descriptionElement == null ){
            return "";
        }
        final List<WebElement> allLinks = findElementsSafe(By.xpath(".//a"), descriptionElement);
        WebElement lastElement = (allLinks != null && !allLinks.isEmpty()) ? allLinks.get(allLinks.size()-1) : null;
        return lastElement == null ? "" : this.getFbAdsDestination(lastElement.getAttribute("href"));
    }

    @Override
    protected String getAdDestionation(WebElement ad, boolean isSingleAd, WebElement descriptionElement) {
        if(isSingleAd){
            /**
             * if it is singleAd, the selector is : //a[contains(@data-sigil, 'MLinkshim')]
             * it may return two elements, but the same final url
             */
            final List<WebElement> destElements = findElementsSafe(By.xpath(".//a[contains(@data-sigil, 'MLinkshim')]"), ad);
            if(destElements != null && !destElements.isEmpty()){
                /**
                 * we'll take the last element that matches the selector
                 */
                final WebElement destElement = destElements.get(destElements.size()-1);
                if(destElement != null){
                    final String href = destElement.getAttribute("href");
                    logger.debug("trying to get ad destination for url : {}.", href);
                    if(href != null){
                        try {
                            final String finalUrl = fileManagerService.getFinalURL(href);
                            return this.getFbAdsDestination(finalUrl);
                        } catch (Exception e) {
                            return this.getFbAdsDestination(href);
                        }
                    } else {
                        return "";
                    }
                }
            } else {
                /**
                 * we'll take the url of the ad
                 * for single ad, description element is always an a by its selector
                 */
                if(descriptionElement != null){
                    final String href = descriptionElement.getAttribute("href");
                    logger.debug("trying to get ad destination for url : {}.", href);
                    if(href != null){
                        try {
                            final String finalUrl = fileManagerService.getFinalURL(href);
                            return this.getFbAdsDestination(finalUrl);
                        } catch (Exception e) {
                            return this.getFbAdsDestination(href);
                        }
                    } else {
                        return "";
                    }
                }
            }
        } else {
            final List<WebElement> destElements = findElementsSafe(By.xpath(".//a[@data-gt]"), ad);
            if(destElements != null && !destElements.isEmpty()){
                /**
                 * we'll take the last element that matches the selector
                 */
                final WebElement destElement = destElements.get(destElements.size()-1);
                if(destElement != null){
                    final String href = destElement.getAttribute("href");
                    logger.debug("trying to get ad destination for url : {}.", href);
                    if(href != null){
                        try {
                            final String finalUrl = fileManagerService.getFinalURL(href);
                            return this.getFbAdsDestination(finalUrl);
                        } catch (Exception e) {
                            return this.getFbAdsDestination(href);
                        }
                    } else {
                        return "";
                    }
                }
            } else {
                return "";
            }
        }
        return "";
    }
    @Override
    protected String getFbAdHtmlFileName(){
        return "html/fb-mobile-ad.html";
    }
    @Override
    protected String getFbPlayVideoFileName(){
        return "js/fb-mobile-play-video.js";
    }
    @Override
    protected void processFBRightColumn(String idExtraction, String idPage, String idZone, String profileHref) {
        logger.debug("In mobile mode, there is no right column processing.");
    }
    @Override
    protected int getFbAdWidth(){
        return 400;
    }
    @Override
    protected int getFbAdHeight(int adHeight){
        return adHeight;
    }

    protected Dimension getShahidPlayerDimensions(){
        return new Dimension(960, 540);
    }
    protected Point getShahidPlayerLocation(){
        return new Point(24, 85);
    }
}
