/**
 * Created by admin on 21/06/2016.
 */
package ma.labs.bot.core;

import ma.labs.bot.data.Page;
import ma.labs.bot.data.Pages;
import ma.labs.bot.data.Robot;
import ma.labs.bot.data.RobotInfo;
import ma.labs.bot.utils.Utils;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;
import java.util.Arrays;

import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doNothing;


public class FacebookRobotRunnerTestDEV extends BaseRobotRunnerTestDEV {
    @Autowired @Qualifier("scraper") private IScraper scraper;

    @Before
    public void setup() throws IOException{
        super.setup();
        scraper.setMediaHelperAPIConnector(mediaHelperAPIConnector);
        scraper.setDatabaseAPIConnector(databaseAPIConnector);
        scraper.setAwsConnector(awsConnector);
        // force to start with facebook!!

        given(databaseAPIConnector.getFacebookPageId()).willReturn("123456");
        given(mediaHelperAPIConnector.downloadByFileUrl(anyString()))
                .willReturn(new MediaHelperAPIResponse("by url", 300, 19856));
        given(mediaHelperAPIConnector.downloadByYoutubeId(anyString()))
                .willReturn(new MediaHelperAPIResponse("by id", 400, 1855));

        doNothing().when(mediaHelperAPIConnector).uploadByYoutubeID(anyString());
        doNothing().when(mediaHelperAPIConnector).uploadByFileUrl(anyString());

        ReflectionTestUtils.setField(robotRunner, "isLastVisitedYoutube", true);
    }
    String pagesLink =
            "http://www.gofgogle.com/,http://wwfgw.gogogle.com/,http://www.goofgffgle.com/"
            //"http://localhost/bug/index.html,"
            //"http://download.m5zn.com/Software/12585/TimeZones-Build.html,https://0.s3.envato.com/files/117872085/index.html,"
            //+"http://www.naharnet.com/,"
            //+"file:///C:/Users/labs004/Desktop/media-to-analyse/index.html,"
            //+"http://nvie.com/posts/a-successful-git-branching-model/,"
            //+"http://www.software-testing-tutorials-automation.com/2015/01/how-to-get-height-and-width-of-element.html,"
            //+"http://www.alyaoum24.com,"
            //+"http://www.aabbir.com/3763.html,"
            ;

    private void setMobileMode(RobotInfo robotInfo){
        robotInfo.getData().setDeviceUserAgent("Mozilla/5.0 (Linux; Android 5.0; SM-G900P Build/LRX21T) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/48.0.2564.23 Mobile Safari/537.36");
        robotInfo.getData().setDeviceId(2);
        robotInfo.getData().setDeviceName("Samsung GALAXY S5");
        robotInfo.getData().setDeviceWidth(360);
        robotInfo.getData().setDeviceHeight(640);
    }


    @Test
    public void runOnePage() throws IOException {
        //given
        robotRunner.setIdRobot("5");
        //robotRunner.setRunInLoop(true);
        RobotInfo robotInfo = new RobotInfo();
        robotInfo.setData(new Robot());
        robotInfo.getData().setIsFB("0");
        robotInfo.getData().setCountry("4");
        robotInfo.getData().setName("bba omar bot");
        robotInfo.getData().setFirefoxFile("http://api.pixitrend.com/profiles/male_18.zip");
//        robotInfo.getData().setFirefoxProxyEnable(1);
//        robotInfo.getData().setFirefoxProxy("collector-beirut-001.pixitrend.net:7788");

//        robotInfo.getData().setDeviceId(1);
        setMobileMode(robotInfo);
        given(databaseAPIConnector.getRobotInfo("5")).willReturn(robotInfo);

        Pages pages = new Pages();
        pages.setData(
                Arrays.stream(pagesLink.split(","))
                        .map(link -> new Page(Utils.getRandom(0,100),link,null, null,null))
                        .toArray(Page[]::new)
        );

        given(databaseAPIConnector.getPages("4"))
                .willReturn(pages);

        //when
        robotRunner.init();
        robotRunner.run();
        //then
    }
}
