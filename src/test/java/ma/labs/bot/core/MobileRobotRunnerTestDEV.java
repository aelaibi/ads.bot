/**
 * Created by admin on 21/06/2016.
 */
package ma.labs.bot.core;

import ma.labs.bot.data.Page;
import ma.labs.bot.data.Pages;
import ma.labs.bot.data.Robot;
import ma.labs.bot.data.RobotInfo;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;

import static org.mockito.BDDMockito.given;


public class MobileRobotRunnerTestDEV extends BaseRobotRunnerTestDEV {


    @Autowired
    @Qualifier("mobileScraper") private IScraper scraper;

    @Before
    public void setup() throws IOException{
        super.setup();
        scraper.setDatabaseAPIConnector(databaseAPIConnector);
        scraper.setAwsConnector(awsConnector);
        scraper.setMediaHelperAPIConnector(mediaHelperAPIConnector);

        // force to start with facebook!!
        ReflectionTestUtils.setField(robotRunner, "isLastVisitedYoutube", true);

        given(databaseAPIConnector.getFacebookPageId()).willReturn("123456");

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
        robotInfo.getData().setFirefoxFile("http://profiles.io/male_18_2.zip");

        //S5 config
        robotInfo.getData().setDeviceUserAgent("Mozilla/5.0 (Linux; Android 5.0; SM-G900P Build/LRX21T) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/48.0.2564.23 Mobile Safari/537.36");
        robotInfo.getData().setDeviceId(2);
        robotInfo.getData().setDeviceName("Samsung GALAXY S5");
        robotInfo.getData().setDeviceWidth(360);
        robotInfo.getData().setDeviceHeight(640);

        robotInfo.getData().setProfileID(11);

        given(databaseAPIConnector.getRobotInfo("5"))
                .willReturn(robotInfo);

        Pages pages = new Pages();
        pages.setData(new Page[]{
                new Page(1,"http://www.h24info.ma/lifestyle/high-tech-gaming/facebook-se-deploie-dans-la-video-en-direct/40255","body", null,null),

        });
        given(databaseAPIConnector.getPages("4"))
                .willReturn(pages);

        //when
        robotRunner.init();
        robotRunner.run();
        //then

    }




}
