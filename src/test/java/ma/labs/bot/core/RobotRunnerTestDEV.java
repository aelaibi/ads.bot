/**
 * Created by admin on 21/06/2016.
 */
package ma.labs.bot.core;

import ma.labs.bot.data.BrowserMode;
import ma.labs.bot.data.RobotInfo;
import ma.labs.bot.utils.Constants;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;

import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doNothing;


public class RobotRunnerTestDEV extends BaseRobotRunnerTestDEV {

    @Autowired @Qualifier("scraper") private IScraper scraper;




    @Test
    public void runOnePage() throws IOException {
        //given
        String idRobot = "44";
        int profile = Constants.ANONYMOUS_PROFILE;
        String useProxy = "";
        robotRunner.setIdRobot(idRobot);
        //robotRunner.setRunInLoop(true);
        RobotInfo robotInfo = prepareRobot(profile, BrowserMode.DESKTOP,useProxy);
        given(databaseAPIConnector.getRobotInfo(idRobot)).willReturn(robotInfo);

        pagesLink.add("http://www.voitureaumaroc.com/assurance.asp");
        mockPages();

        //when
        robotRunner.init();
        robotRunner.run();
        //then
    }





    @Before
    public void setup() throws IOException{
        super.setup();
        scraper.setMediaHelperAPIConnector(mediaHelperAPIConnector);
        scraper.setDatabaseAPIConnector(databaseAPIConnector);
        scraper.setAwsConnector(awsConnector);
        // force to start with facebook!!
        ReflectionTestUtils.setField(robotRunner, "isLastVisitedYoutube", true);

        given(databaseAPIConnector.getFacebookPageId()).willReturn("123456");
        given(mediaHelperAPIConnector.downloadByFileUrl(any()))
                .willReturn(new MediaHelperAPIResponse("byurl123456", 300, 19856));
        given(mediaHelperAPIConnector.downloadByYoutubeId(any()))
                .willReturn(new MediaHelperAPIResponse("byid123456", 400, 1855));

        doNothing().when(mediaHelperAPIConnector).uploadByYoutubeID(anyString());
        doNothing().when(mediaHelperAPIConnector).uploadByFileUrl(anyString());

    }
}
