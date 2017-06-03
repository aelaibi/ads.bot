/**
 * Created by admin on 21/06/2016.
 */
package ma.labs.bot.core;

import ma.labs.bot.connectors.DatabaseAPIConnector;
import ma.labs.bot.data.Page;
import ma.labs.bot.data.Pages;
import ma.labs.bot.data.Robot;
import ma.labs.bot.data.RobotInfo;
import ma.labs.bot.utils.TimeUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class RobotRunnerTest {

    @Mock
    DatabaseAPIConnector databaseAPIConnector;
    @Mock
    Scraper scraper;
    @InjectMocks
    RobotRunner robotRunner ;

    @Before
    public void setup() throws IOException{
        //robotRunner = new RobotRunner(databaseAPIConnector, scraper);
        robotRunner.setDatabaseAPIConnector(databaseAPIConnector);
        ReflectionTestUtils.setField(robotRunner, "runYoutubeAfter", 7);
        System.setProperty(TimeUtils.IGNORE_WAIT, "yes");//to ignore TimeUtils.waitfor

    }

    @Test
    public void testRobotInfoIsNullAndRobotIdIsOne() throws Exception {
        //given
        robotRunner.setIdRobot("1");
        given(databaseAPIConnector.getRobotInfo("1"))
                .willReturn(null);
        //when
        try {
            robotRunner.init();
        }catch (NullPointerException npe){
            //then
            assertNotNull(npe);
        }

    }



    @Test
    public void assertRobotRunnerGotRobotData() throws IOException {
        //given
        prepareRobot("5","0");
        //when
        robotRunner.init();
        //then
        assertNotNull(robotRunner.getRobotInfo());
    }

    @Test
    public void checkFailsOnProxyNotEnable() throws IOException {
        //given
        RobotInfo robotI = prepareRobot("5","0");
        robotI.getData().setFirefoxProxyEnable(0);
        //when
        try {
            robotRunner.init();
        }catch (IllegalArgumentException iae){
            //then
            assertNotNull(iae);
            assertEquals(" Robot must use proxy", iae.getMessage());
        }


    }
    @Test
    public void robotRunnerWillLaunchWebExtraction() throws Exception {
        //given
        RobotInfo robotI = prepareRobot("5","0");
        robotI.getData().setCountry("4");
        Pages pages = new Pages();
        pages.setData(new Page[]{
                new Page(1,"http://test.com",null, null,null),
                //new Page(2,"http://www.google.com",null, null,null)
        });
        given(databaseAPIConnector.getPages("4"))
                .willReturn(pages);
        //when
        robotRunner.init();
        robotRunner.run();
        //then
        assertNotNull(robotRunner.getRobotInfo());
        verify(scraper, times(1)).openBrowser();
        verify(scraper, times(1)).closeBrowser();
        verify(scraper, times(1)).goToURL("http://test.com");
    }
    @Test
    public void robotRunnerWillLaunchWebExtractionFor2Pages() throws Exception {
        //given
        RobotInfo robotI = prepareRobot("5","0");
        robotI.getData().setCountry("4");
        Pages pages = new Pages();
        pages.setData(new Page[]{
                new Page(1,"http://test.com",null, null,null),
                new Page(2,"http://test2.com",null, null,null)
        });
        given(databaseAPIConnector.getPages("4"))
                .willReturn(pages);
        //when
        robotRunner.init();
        robotRunner.run();
        //then
        assertNotNull(robotRunner.getRobotInfo());
        verify(scraper, times(1)).openBrowser();
        verify(scraper, times(1)).closeBrowser();
        verify(scraper, times(2)).goToURL(anyString());
        verify(databaseAPIConnector, times(2)).insertExtraction(anyString(),anyString());
    }
    @Test
    public void robotRunnerWillLaunchWebExtractionFor2PagesAndYoutube() throws Exception {
        //given
        //robotRunner.
        ReflectionTestUtils.setField(robotRunner, "runYoutubeAfter", 2);
        RobotInfo robotI = prepareRobot("5","0");
        robotI.getData().setCountry("4");
        Pages pages = new Pages();
        pages.setData(new Page[]{
                new Page(1,"http://test.com",null, null,null),
                new Page(2,"http://test2.com",null, null,null),
                new Page(3,"http://test3.com",null, null,null)
        });
        given(databaseAPIConnector.getPages("4"))
                .willReturn(pages);
        //when
        robotRunner.init();
        robotRunner.run();
        //then
        assertNotNull(robotRunner.getRobotInfo());
        verify(scraper, times(2)).openBrowser();
        verify(scraper, times(1)).closeBrowser();
        verify(scraper, times(1)).goToURL("http://test.com");
        verify(scraper, times(1)).goToURL("http://test2.com");
        verify(scraper, times(1)).goToURL("http://test3.com");
        verify(scraper, times(2)).goToMainPageYoutube();
        verify(scraper, never()).goToURL("https://www.facebook.com/");
        verify(databaseAPIConnector, times(3+10)).insertExtraction(anyString(),anyString());//12 for youtube
    }

    @Test
    public void robotRunnerWillLaunchWebExtractionFor2PagesThenYoutubeThenFb() throws Exception {
        //given
        //robotRunner.
        ReflectionTestUtils.setField(robotRunner, "runYoutubeAfter", 2);
        ReflectionTestUtils.setField(robotRunner, "isLastVisitedYoutube", true);

        RobotInfo robotI = prepareRobot("5","0");
        robotI.getData().setCountry("4");
        Pages pages = new Pages();
        pages.setData(new Page[]{
                new Page(1,"http://test.com",null, null,null),
                new Page(2,"http://test2.com",null, null,null),
                new Page(3,"http://test3.com",null, null,null),
                new Page(3,"http://test4.com",null, null,null),
                new Page(3,"http://test5.com",null, null,null)
        });
        given(databaseAPIConnector.getPages("4"))
                .willReturn(pages);
        //when
        robotRunner.init();
        robotRunner.run();
        //then
        assertNotNull(robotRunner.getRobotInfo());
        verify(scraper, times(2)).openBrowser();
        verify(scraper, times(1)).closeBrowser();
        verify(scraper, times(1)).goToURL("http://test.com");
        verify(scraper, times(1)).goToURL("http://test2.com");
        verify(scraper, times(1)).goToURL("http://test3.com");
        verify(scraper, times(1)).goToURL("http://test4.com");
        verify(scraper, times(1)).goToURL("http://test5.com");
        verify(scraper, times(2)).goToMainPageYoutube();
        verify(scraper, times(1)).goToURL("https://www.facebook.com/");
        verify(databaseAPIConnector, times(5+10)).insertExtraction(anyString(),anyString());//12 for youtube
    }

    @Test
    public void robotRunnerInitWillOverrideRunYoutubeAfterFromDB() throws IOException {
        //given
        //robotRunner.
        ReflectionTestUtils.setField(robotRunner, "runYoutubeAfter", 5);//comming from properties
        RobotInfo robotI = prepareRobot("5","0");
        robotI.getData().setCountry("4");
        robotI.getData().setRunYoutubeAfter(2);
        //when
        robotRunner.init();
        //then
        assertNotNull(robotRunner.getRobotInfo());
        assertEquals(2, robotRunner.getRunYoutubeAfter());
    }
    @Test
    public void robotRunnerInitWillNotOverrideRunYoutubeAfterFromDB() throws IOException {
        //given
        //robotRunner.
        ReflectionTestUtils.setField(robotRunner, "runYoutubeAfter", 5);//comming from properties
        RobotInfo robotI = prepareRobot("5","0");
        robotI.getData().setCountry("4");
        //when
        robotRunner.init();
        //then
        assertNotNull(robotRunner.getRobotInfo());
        assertEquals(5, robotRunner.getRunYoutubeAfter());
    }

    @Test
    public void assertNonFbRobotGotListPagesToVisit() throws IOException {
        //given

        robotRunner.setIdRobot("5");
        given(databaseAPIConnector.getYoutubeHomePageId())
                .willReturn("homeYoutubeID");
        given(databaseAPIConnector.getYoutubeVideoPageId())
                .willReturn("sideBarYoutubeID");
        //robotRunner.setRunInLoop(true);
        RobotInfo robotInfo = new RobotInfo();
        robotInfo.setData(new Robot());
        robotInfo.getData().setIsFB("0");
        robotInfo.getData().setCountry("4");
        robotInfo.getData().setName("bba omar bot");
        robotInfo.getData().setFirefoxProxyEnable(1);

        robotInfo.getData().setDeviceId(1);

        given(databaseAPIConnector.getRobotInfo("5"))
                .willReturn(robotInfo);
        Pages pages = new Pages();
        pages.setData(new Page[]{new Page(222,"http://www.lahamag.com/Details/56147",null, null,null),
                new Page(1111,"http://www.lahamag.com/Details/56147",null, null,null)});
        given(databaseAPIConnector.getPages("4"))
                .willReturn(pages);
        //when
        robotRunner.init();
        robotRunner.run();
        //then

    }

    private RobotInfo prepareRobot(String idRobot, String fbFlag) throws IOException {
        robotRunner.setIdRobot(idRobot);
        RobotInfo robotInfo = new RobotInfo();
        robotInfo.setData(new Robot());
        robotInfo.getData().setIsFB(fbFlag);

        robotInfo.getData().setDeviceId(1);
        robotInfo.getData().setFirefoxProxyEnable(1);

        given(databaseAPIConnector.getRobotInfo(idRobot))
                .willReturn(robotInfo);
        return robotInfo;
    }


    @Test
    public void testStartRobotVide() throws Exception {
        try {
            robotRunner.init();
        }catch (NullPointerException npe){
            assertNotNull(npe);
        }
    }
}
