package ma.labs.bot.core;

import ma.labs.bot.connectors.AWSConnector;
import ma.labs.bot.connectors.DatabaseAPIConnector;
import ma.labs.bot.connectors.MediaHelperAPIConnector;
import ma.labs.bot.data.Robot;
import org.apache.commons.lang3.tuple.Pair;

import java.util.List;

/**
 * Created by Mohamed on 12/10/2016.
 */
public interface IScraper {

    int getTabsCount();

    void setRobotData(Robot robotBrowserInfo);

    void resetProfilesFolder();
    void goToURL(String url) throws ProblemLoadingPageException;
    void openBrowser();
    void closeBrowser();
    void extractAdsPage(String idExtraction, String idPage);
    void extractWallPaperAd(String idExtraction, String idPage, String selectorElement, String selectorIframe);
    void goToMainPageYoutube() throws ProblemLoadingPageException;
    void extractYoutubeMainPageAd(String idExtraction, String idPage);
    void goToRandomVideoFromMain() throws YoutubeNavigationException;
    void processYoutubeVideo(String idExtraction, String idPage) throws Exception;
    void goToRandomVideoFromSidebar() throws YoutubeNavigationException;
    void killFirefox();

    //for test
    void setDatabaseAPIConnector(DatabaseAPIConnector databaseAPIConnector);
    void setAwsConnector(AWSConnector awsConnector);
    void setMediaHelperAPIConnector(MediaHelperAPIConnector mediaHelperAPIConnector);

    void setFirefoxProfiles(String browserProfiles);

    Integer getFirefoxProcessID();
    String getCodeSourceOfCurrentFrame(boolean b);

    void setYoutubePages(String main, String sideBar);


    /**
     * Facebook
     */
    void extractFBAds(String idExtraction, String idPage, Pair<String, List<String>> possibleDepth);
    void extractFBRightColumn(String idExtraction, String idPage, String profileHref);
    boolean checkFbProfilConnected();
    String getFbProfileHref();
    List<String> getPossibleMedias();


}
