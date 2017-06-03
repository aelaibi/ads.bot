/**
 * Created by admin on 21/06/2016.
 */
package ma.labs.bot.core;

import ma.labs.bot.connectors.DatabaseAPIConnector;
import ma.labs.bot.data.*;
import ma.labs.bot.utils.TimeUtils;
import ma.labs.bot.utils.Utils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import java.io.IOException;
import java.util.List;

@Component
public class RobotRunner {

    private static Logger logger = LoggerFactory.getLogger(RobotRunner.class);

    @Autowired @Qualifier("scraper") private IScraper defaultScraper;
    @Autowired @Qualifier("mobileScraper") private IScraper mobileScraper;

    @Value("${robot.id}")
    private String idRobot ;
    @Value("${run.in.loop:false}")
    private boolean runInLoop;


    @Value("${run.youtube.after:1}")
    private int runYoutubeAfter;

    @Value("${mode:dev}")
    protected String mode;



    @Autowired
    private DatabaseAPIConnector databaseAPIConnector ;

    private IScraper scraper;

    private RobotInfo robotInfo = null;
    private String youtubeHomePageId;
    private String youtubeSideBarPageId;
    private String facebookPageId ;
    private boolean isMobileMode;

    private boolean isLastVisitedYoutube = false;

    public void init(){
        try {
            MDC.put("ROBOT.ID", idRobot);//used by the logger
            this.robotInfo = databaseAPIConnector.getRobotInfo(idRobot);
            logger.info("robot ID : {}", idRobot);
            logger.info("robot Info : {}", robotInfo);
            youtubeHomePageId = databaseAPIConnector.getYoutubeHomePageId();
            youtubeSideBarPageId = databaseAPIConnector.getYoutubeVideoPageId();

            facebookPageId = databaseAPIConnector.getFacebookPageId();

            doCheks();
            MDC.put("COUNTRY", robotInfo.getData().getCountry());

            final Robot robotBrowserInfo = this.robotInfo.getData();
            if(BrowserMode.MOBILE == robotBrowserInfo.getBrowserMode()){
                this.scraper = this.mobileScraper;
                isMobileMode = true;
            } else {
                // desktop scraper
                this.scraper = this.defaultScraper;
                isMobileMode = false;
            }
            this.scraper.setRobotData(robotBrowserInfo);
            this.scraper.setYoutubePages(youtubeHomePageId, youtubeSideBarPageId);

            if(this.robotInfo.getData().getRunYoutubeAfter()>0){
                this.runYoutubeAfter = this.robotInfo.getData().getRunYoutubeAfter();
            }
        } catch (IOException e) {
            logger.error(e.getLocalizedMessage());
        }
    }

    //CHECKING INIT IS DONE CORRECTLY
    private void doCheks() {
        Assert.notNull(robotInfo.getData(), "robot info must be not null");
        Assert.isTrue(robotInfo.getData().getDeviceId()>0, "Robot must have  device ID");
        if (Utils.isProdMode(mode)) {
            Assert.isTrue(robotInfo.getData().isFirefoxProxyEnable()," Robot must use proxy");
        }
    }

    public void run()  {
        do {
            logger.info("{} : I'm running ... ",robotInfo.getData().getName());
            scraper.resetProfilesFolder();

            launchWebExtraction();
            TimeUtils.waitFor(4000);
        }while (runInLoop);
        logger.info("End Running .....!!!");
    }
    //LAUNCH EXTRACTIONS
    private void launchWebExtraction() {
        String idExtraction = "";
        try {
            Pages resp = databaseAPIConnector.getPages(robotInfo.getData().getCountry());
            if (resp != null && resp.getData() != null){
                Page[] pages = resp.getData();
                logger.info("I will process {} pages", pages.length);
                scraper.openBrowser();
                int i = 1;
                for (Page page:pages) {
                    logger.info("{}:Extracting page ID {} ", i,page.getId());

                    int tabsCount = scraper.getTabsCount();
                    if(tabsCount > 1){ //to clean all tab we don't  need
                        logger.warn("I handle {} tabs !", tabsCount);
                        TimeUtils.waitFor(3000);
                        scraper.closeBrowser();
                        TimeUtils.waitFor(3000);
                        scraper.openBrowser();
                    }

                    try {
                        scraper.goToURL(page.getUrl());
                        idExtraction = databaseAPIConnector.insertExtraction(Integer.toString(page.getId()),this.idRobot);
                        logger.info("<page extr='{}' page='{}'>", idExtraction,page.getId());
                        long begin = System.currentTimeMillis();
                        scraper.extractAdsPage(idExtraction, Integer.toString(page.getId()));
                        if (StringUtils.isNotEmpty(page.getSelectorCss())
                                ||  StringUtils.isNotEmpty(page.getSelectorXPath())) {
                            scraper.extractWallPaperAd(idExtraction, Integer.toString(page.getId()), page.getSelectorCss(), page.getSelectorXPath());
                        }
                        databaseAPIConnector.endExtraction(idExtraction);
                        logger.info("{}:End extraction page ID {} after {} seconds" ,i, page.getId()
                                , TimeUtils.countSecondsFrom(begin) );
                        logger.info("<duration value='{}' />", TimeUtils.countSecondsFrom(begin));
                        logger.info("</page>");
                    } catch (ProblemLoadingPageException except){
                        logger.error(except.getMessage());
                    }
                    /**
                    * After X times, the robot will go to browse either youtube or facebook.
                    * if !isLastVisitedYoutube go to youtube and change isLastVisitedYoutube to true
                    * else go to facebook and change isLastVisitedYoutube to false
                    */
                    if(i % runYoutubeAfter ==0){
                        if(this.isLastVisitedYoutube){
                            this.isLastVisitedYoutube = false;
                            goToFacebook();
                        } else {
                            this.isLastVisitedYoutube = true;
                            goToYoutube();
                        }
                    }
                    i++;
                }
                scraper.closeBrowser();
            }else {
                logger.error("No Pages found for this robot {} ", robotInfo.getData());
            }

        } catch (Exception e) {
            logger.error(e.getLocalizedMessage(),e);
            TimeUtils.waitFor(6000);
            scraper.closeBrowser();
        }
    }

    private void goToYoutube(){
        try {
            for (int j = 0; j <= 1; j++) {
                logger.info("Extracting Youtube page ID {}", youtubeHomePageId);
                scraper.goToMainPageYoutube();

                String idExtraction = databaseAPIConnector.insertExtraction(youtubeHomePageId, idRobot);
                long begin = System.currentTimeMillis();
                scraper.extractYoutubeMainPageAd(idExtraction, youtubeHomePageId);
                databaseAPIConnector.endExtraction(idExtraction);
                logger.info("End extraction Youtube page ID {} after {} seconds" , youtubeHomePageId,
                        TimeUtils.countSecondsFrom(begin));

                try {
                    logger.info("Extracting Youtube page ID {}", youtubeSideBarPageId);
                    begin = System.currentTimeMillis();
                    scraper.goToRandomVideoFromMain();
                    idExtraction = databaseAPIConnector.insertExtraction(youtubeSideBarPageId, idRobot);
                    scraper.processYoutubeVideo(idExtraction, youtubeSideBarPageId);
                    databaseAPIConnector.endExtraction(idExtraction);
                    logger.info("End extraction Youtube page ID {} after {} seconds" , youtubeSideBarPageId,
                            TimeUtils.countSecondsFrom(begin));

                    for (int k = 0; k <= 2; k++) {
                        try {
                            logger.info("Extracting Youtube page ID {}", youtubeSideBarPageId);
                            scraper.goToRandomVideoFromSidebar();

                            idExtraction = databaseAPIConnector.insertExtraction(youtubeSideBarPageId, idRobot);
                            begin = System.currentTimeMillis();

                            scraper.processYoutubeVideo(idExtraction, youtubeSideBarPageId);
                            databaseAPIConnector.endExtraction(idExtraction);
                            logger.info("End extraction Youtube page ID {} after {} seconds" , youtubeSideBarPageId,
                                    TimeUtils.countSecondsFrom(begin));
                            TimeUtils.waitFor(Utils.getRandom(5000,10000));
                        } catch (YoutubeNavigationException e) {
                            logger.error("Error during youtube sidebar processing.", e.getMessage());
                        }
                    }
                } catch (YoutubeNavigationException e) {
                    logger.error("Error during youtube main page processing.", e.getMessage());
                }
            }
        } catch (Exception e) {
            logger.error(e.getMessage(),e);
        }

        //killfirefox and restart it to clean memory
        scraper.killFirefox();
        TimeUtils.waitFor(5000);
        scraper.openBrowser();
    }
    private void goToFacebook() {
        String idExtraction = "";
        //   //div[contains(@data-ft,'ei') and (@data-xt)]
        //   //div[contains(@data-testid,'fbfeed_story') and .//a[contains(@href, '/about/ads')]]
        final List<String> possibleMedias = scraper.getPossibleMedias();
        final Pair<String, List<String>> possibleDepths = new ImmutablePair<>("//", possibleMedias);
        try {
            long begin;
            try {
                logger.info("I will process Fb.");
                scraper.goToURL("https://www.facebook.com/");

                final boolean isConnected = scraper.checkFbProfilConnected();

                if(!isConnected){
                    return;
                }
                //Extract ads from timeline and rightBar
                logger.info("Extracting Facebook - Main page");

                idExtraction = databaseAPIConnector.insertExtraction(facebookPageId,idRobot);
                begin = System.currentTimeMillis();

                scraper.extractFBAds(idExtraction, facebookPageId, possibleDepths);

                databaseAPIConnector.endExtraction(idExtraction);
                logger.info("End extraction Facebook - Main page after {} seconds", TimeUtils.countSecondsFrom(begin) );

                if(!isMobileMode){
                    //Extract ads from secondary rightBar
                    logger.info("Extracting Facebook - Profile page");
                    final String profileHref = scraper.getFbProfileHref();
                    logger.debug("Facebook profile href is : " + profileHref);
                    scraper.goToURL(profileHref);

                    idExtraction = databaseAPIConnector.insertExtraction(facebookPageId,idRobot);
                    begin = System.currentTimeMillis();

                    scraper.extractFBRightColumn(idExtraction, facebookPageId, profileHref);

                    databaseAPIConnector.endExtraction(idExtraction);
                    logger.info("End extraction Facebook - Profile page after {} seconds", TimeUtils.countSecondsFrom(begin));
                }
            } catch (Exception e) {
                logger.error(e.getMessage(),e);
            }
            scraper.closeBrowser();
            TimeUtils.waitFor(5000);
            scraper.openBrowser();
        } catch (Exception e) {
            logger.error(e.getMessage(),e);
        }
    }

    public DatabaseAPIConnector getDatabaseAPIConnector() {
        return databaseAPIConnector;
    }

    public void setDatabaseAPIConnector(DatabaseAPIConnector databaseAPIConnector) {
        this.databaseAPIConnector = databaseAPIConnector;
    }

    public IScraper getScraper() {
        return scraper;
    }

    public void setScraper(IScraper scraper) {
        this.scraper = scraper;
    }

    public RobotInfo getRobotInfo() {
        return robotInfo;
    }

    public void setRobotInfo(RobotInfo robotInfo) {
        this.robotInfo = robotInfo;
    }

    public String getIdRobot() {
        return idRobot;
    }

    public void setIdRobot(String idRobot) {
        this.idRobot = idRobot;
    }

    public int getRunYoutubeAfter() {
        return runYoutubeAfter;
    }

    public void setRunYoutubeAfter(int runYoutubeAfter) {
        this.runYoutubeAfter = runYoutubeAfter;
    }

    public boolean isRunInLoop() {
        return runInLoop;
    }

    public void setRunInLoop(boolean runInLoop) {
        this.runInLoop = runInLoop;
    }
}
