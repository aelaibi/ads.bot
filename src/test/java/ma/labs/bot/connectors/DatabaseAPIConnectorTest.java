/**
 * Created by admin on 23/06/2016.
 */
package ma.labs.bot.connectors;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import ma.labs.bot.data.*;
import ma.labs.bot.data.util.MediaType;
import ma.labs.bot.utils.TimeUtils;
import ma.labs.bot.utils.Utils;
import org.junit.*;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.Assert.*;

@RunWith(SpringRunner.class)
@SpringBootTest
public class DatabaseAPIConnectorTest {


    protected static WireMockServer wireMockServer;

    @Autowired
    private DatabaseAPIConnector databaseAPIConnector;


    @BeforeClass
    public static void setupServer() {

        wireMockServer = new WireMockServer(8089);
        wireMockServer.start();

        WireMock.configureFor(wireMockServer.port());
    }


    @AfterClass
    public static void serverShutdown() {
        wireMockServer.stop();
    }
    @Before
    public void init() throws InterruptedException {
        WireMock.resetToDefault();
        databaseAPIConnector.retryDelay = 10;
    }




    @Test
    public void testGetPageNonVide() throws IOException {
        // given
        String reqBody = "action=getPages&country=3";
        int status = 200;
        String respBody = "{'data':" +
                "[{'ID':'4306','URL':'http://www.lahamag.com/Details/56147'," +
                " 'SELECTORCSS':'', 'SELECTORXPATH':'', 'XPATHLANDINGPAGE':''}," +
                " {'ID':'36223','URL':'http://forum.el-wlid.com/f37.html'," +
                " 'SELECTORCSS':'', 'SELECTORXPATH':'', 'XPATHLANDINGPAGE':''}," +
                " {'ID':'10261','URL':'http://www.zawya.com/middle-east/tourism/'," +
                " 'SELECTORCSS':'', 'SELECTORXPATH':'', 'XPATHLANDINGPAGE':''}," +
                " {'ID':'30064','URL':'http://financecareers.about.com'," +
                " 'SELECTORCSS':'', 'SELECTORXPATH':'', 'XPATHLANDINGPAGE':''}]," +
                " 'code':1}";
        mockWith(reqBody, status, respBody);
        //when
        Pages pages = databaseAPIConnector.getPages("3");
        //then
        assertNotNull(pages);
        Assert.assertEquals(pages.getCode(),1);
        Assert.assertEquals(pages.getData().length,4);
        Assert.assertEquals(pages.getData()[0].getUrl(),"http://www.lahamag.com/Details/56147");
        Assert.assertEquals(pages.getData()[0].getId(),4306);
    }

    private void mockWith(String reqBody, int status, String respBody) {
        stubFor(post(urlEqualTo("/pixitrendRobotAPIDEV.php"))
                .withHeader("Content-Type", equalTo("application/x-www-form-urlencoded"))
                .withRequestBody(equalTo(reqBody))
                .willReturn(aResponse()
                        .withStatus(status)
                        .withHeader("Content-Type", "text/xml")
                        .withBody(respBody)));
    }

    @Test
    public void testGetPageVide() throws IOException {
        // given
        mockWith("action=getPages&country=56", 200, "{'data':[],'code':1}");
        //when
        Pages pages = databaseAPIConnector.getPages("56");

        assertNotNull(pages);
        Assert.assertEquals(pages.getCode(),1);
        Assert.assertEquals(pages.getData().length,0);
    }
    @Test
    public void testGetPagesWithFunctionnalError() throws IOException {
        // given
        mockWith("action=getPages&country=aa", 200, "{'data':[],'code':0}");

        //when
        Pages pages = databaseAPIConnector.getPages("aa");

        Assert.assertNull(pages);
    }
    @Test
    public void testGetPagesWithHTTPError() throws IOException {
        // given
        mockWith("action=getPages&country=aa", 500, "");

        //when
        Pages pages = databaseAPIConnector.getPages("aa");

        Assert.assertNull(pages);
    }

    @Test
    public void testGetRobotInfoWithBrowserModeMOBILE() throws IOException {
        // given
        String respBody ="{'data':" +
                "{'name':'UAE-COLLECTOR-3', 'city':'Sharjah', 'country':'3'," +
                "'isFB':'1'," +
                "'deviceId':'2', deviceName : 'S5', 'deviceUserAgent':'Test...'," +
                "'deviceWidth':360, 'deviceHeight':640}" +
                ", 'code':1}";
        mockWith("action=getRobotInfo&idRobot=3", 200, respBody);

        //when
        RobotInfo robot = databaseAPIConnector.getRobotInfo("3");

        assertNotNull(robot);
        Assert.assertEquals(robot.getCode(),1);
        assertNotNull(robot.getData());
        final Robot robotBrowserInfo = robot.getData();
        Assert.assertEquals(robotBrowserInfo.getBrowserMode(), BrowserMode.MOBILE);
        Assert.assertEquals(robotBrowserInfo.getDeviceWidth(), 360);
        Assert.assertEquals(robotBrowserInfo.getDeviceHeight(), 640);
        Assert.assertEquals(robotBrowserInfo.getDeviceName(), "S5");
        Assert.assertEquals(robotBrowserInfo.getDeviceUserAgent(), "Test...");
        Assert.assertEquals(robot.getData().getName(), "UAE-COLLECTOR-3");
    }
    @Test
    public void testGetRobotInfoWithBrowserModeDEFAULT() throws IOException {
        // given
        String respBody ="{'data':" +
                "{'name':'UAE-COLLECTOR-3', 'city':'Sharjah', 'country':'3'," +
                "'isFB':'1'," +
                "'deviceId':'1', deviceName : '', 'deviceUserAgent':''," +
                "'deviceWidth':0, 'deviceHeight':0}" +
                ", 'code':1}";
        mockWith("action=getRobotInfo&idRobot=3", 200, respBody);

        //when
        RobotInfo robot = databaseAPIConnector.getRobotInfo("3");

        assertNotNull(robot);
        Assert.assertEquals(robot.getCode(),1);
        assertNotNull(robot.getData());
        final Robot robotBrowserInfo = robot.getData();
        Assert.assertEquals(robotBrowserInfo.getBrowserMode(), BrowserMode.DESKTOP);
        Assert.assertEquals(robotBrowserInfo.getDeviceWidth(), 0);
        Assert.assertEquals(robotBrowserInfo.getDeviceHeight(), 0);
        Assert.assertEquals(robotBrowserInfo.getDeviceName(), "");
        Assert.assertEquals(robotBrowserInfo.getDeviceUserAgent(), "");
        Assert.assertEquals(robot.getData().getName(), "UAE-COLLECTOR-3");
    }
    @Test
    public void testGetRobotInfoExistant() throws IOException {
        // given
        String respBody ="{'data':" +
                "{'name':'UAE-COLLECTOR-3', 'city':'Sharjah', 'country':'3'," +
                "'isFB':'1', 'HOSTname':'collector-sharjah-003.pixitrend.net'," +
                "'PORT':'4444', 'LOGINSSH':'web3'," +
                "'PASSWORDSSH':'root'}, 'code':1}";
        mockWith("action=getRobotInfo&idRobot=3", 200, respBody);

        //when
        RobotInfo robot = databaseAPIConnector.getRobotInfo("3");

        assertNotNull(robot);
        Assert.assertEquals(robot.getCode(),1);
        assertNotNull(robot.getData());
        Assert.assertEquals(robot.getData().getName(), "UAE-COLLECTOR-3");

    }
    @Test
    public void testGetRobotInfoNonExistant() throws IOException {
        // given
        String respBody = "{'data': false, 'code':1}";
        mockWith("action=getRobotInfo&idRobot=700", 200, respBody);

        //when
        RobotInfo robot = databaseAPIConnector.getRobotInfo("700");

        assertNotNull(robot);
        Assert.assertEquals(robot.getCode(),1);
        Assert.assertNull(robot.getData());

    }

    @Test
    public void testGetRobotInfoContainsFireFoxProfile() throws IOException {
        // given
        String respBody ="{\"data\":{\"name\":\"UAE-COLLECTOR-1\",\"city\":\"Sharjah\",\"country\":\"3\",\"isFB\":\"0\",\"id\":\"1\",\"firefoxFile\":\"http://ff.io/ael.zip\"},\"code\":1}";
        mockWith("action=getRobotInfo&idRobot=1", 200, respBody);

        //when
        RobotInfo robot = databaseAPIConnector.getRobotInfo("1");

        assertNotNull(robot);
        Assert.assertEquals(robot.getCode(),1);
        assertNotNull(robot.getData());
        Assert.assertEquals(robot.getData().getName(), "UAE-COLLECTOR-1");
        Assert.assertEquals(robot.getData().getFirefoxFile(), "http://ff.io/ael.zip");

    }

    @Test
    public void testGetRobotInfoContainsFireFoxProxyDisabled() throws IOException {
        // given
        String respBody ="{\"data\":{\"name\":\"UAE-COLLECTOR-1\",\"city\":\"Sharjah\",\"country\":\"3\",\"isFB\":\"0\",\"id\":\"1\",\"firefoxFile\":\"http://ff.io/ael.zip\"},\"code\":1}";
        mockWith("action=getRobotInfo&idRobot=1", 200, respBody);

        //when
        RobotInfo robot = databaseAPIConnector.getRobotInfo("1");

        assertNotNull(robot);
        Assert.assertEquals(robot.getCode(),1);
        assertFalse(robot.getData().isFirefoxProxyEnable());
    }

    @Test
    public void testGetRobotInfoContainsFireFoxProxyEnabled() throws IOException {
        // given
        String respBody ="{\"data\":{\"name\":\"UAE-COLLECTOR-1\",\"city\":\"Sharjah\"," +
                "\"country\":\"3\"," +
                "\"isFB\":\"0\"" +
                ",\"firefoxProxyEnable\":1" +
                ",\"firefoxProxy\":\"http://localhost:8888\"" +
                ",\"id\":\"1\"," +
                "\"firefoxFile\":\"http://ff.io/ael.zip\"}," +
                "\"code\":1}";
        mockWith("action=getRobotInfo&idRobot=1", 200, respBody);

        //when
        RobotInfo robot = databaseAPIConnector.getRobotInfo("1");

        assertNotNull(robot);
        Assert.assertEquals(robot.getCode(),1);
        assertTrue(robot.getData().isFirefoxProxyEnable());
        assertEquals("http://localhost:8888",robot.getData().getFirefoxProxy());
    }

    @Test
    public void testGetRobotInfoContainsRunYoutubeAfter() throws IOException {
        // given
        String respBody ="{\"data\":{\"name\":\"UAE-COLLECTOR-1\",\"city\":\"Sharjah\",\"country\":\"3\",\"isFB\":\"0\",\"id\":\"1\",\"firefoxFile\":\"http://ff.io/ael.zip\",\"runYoutubeAfter\":8},\"code\":1}";
        mockWith("action=getRobotInfo&idRobot=1", 200, respBody);

        //when
        RobotInfo robot = databaseAPIConnector.getRobotInfo("1");

        assertNotNull(robot);
        Assert.assertEquals(robot.getCode(),1);
        assertNotNull(robot.getData());
        Assert.assertEquals(robot.getData().getName(), "UAE-COLLECTOR-1");
        Assert.assertEquals(8,robot.getData().getRunYoutubeAfter());

    }
    @Test
    public void testGetRobotInfoWithoutRunYoutubeAfter() throws IOException {
        // given
        String respBody ="{\"data\":{\"name\":\"UAE-COLLECTOR-1\",\"city\":\"Sharjah\",\"country\":\"3\",\"isFB\":\"0\",\"profileID\":\"78\",\"firefoxFile\":\"http://ff.io/ael.zip\"},\"code\":1}";
        mockWith("action=getRobotInfo&idRobot=1", 200, respBody);

        //when
        RobotInfo robot = databaseAPIConnector.getRobotInfo("1");

        assertNotNull(robot);
        Assert.assertEquals(robot.getCode(),1);
        assertNotNull(robot.getData());
        //if not present runYoutubeAfter will contains default value 0 (int)
        Assert.assertEquals(0,robot.getData().getRunYoutubeAfter());
        Assert.assertEquals(78,robot.getData().getProfileID());

    }
    @Test
    public void testInsertExtractionWithIdPageEmptyShouldReturn0() throws IOException {
        // given
        String respBody = "{'data': '0', 'code':'1'}";
        mockWith("action=insertExtraction&page_id&robot_id=3&status=0", 200, respBody);

        //when
        String idInsertion = databaseAPIConnector.insertExtraction(null,"3");
        //then
        Assert.assertEquals("0",idInsertion);

    }

    @Test
    public void testInsertExtractionWithIdRobotEmptyShouldReturn0() throws IOException {
        // given
        String respBody = "{'data': '0', 'code':'1'}";
        mockWith("action=insertExtraction&page_id=22&robot_id&status=0", 200, respBody);

        //when
        String idInsertion = databaseAPIConnector.insertExtraction("22",null);
        //then
        Assert.assertEquals("0",idInsertion);

    }

    @Test
    public void testInsertExtractionShouldReturnId() throws IOException {
        // given
        String respBody = "{'data': '843681', 'code':'1'}";
        stubFor(post(urlEqualTo("/pixitrendRobotAPIDEV.php"))
                .withHeader("Content-Type", equalTo("application/x-www-form-urlencoded"))
                .withRequestBody(matching("action=insertExtraction&page_id=22&robot_id=3&status=0&start_date=(.*)"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "text/xml")
                        .withBody(respBody)));
        //mockWith("action=insertExtraction&page_id=22&robot_id=3&status=0&start_date=(.*)", 200, respBody);

        //when
        String idInsertion = databaseAPIConnector.insertExtraction("22","3");
        //then
        Assert.assertEquals("843681",idInsertion);

    }
    @Test
    public void testEndExtractionShouldWork() throws IOException {
        // given
        String respBody = "{'data': '1', 'code':'1'}";
        mockWith("action=endExtraction&idExtraction=22", 200, respBody);

        //when
        databaseAPIConnector.endExtraction("22");
        //then
        //no errors
    }

    @Test
    public void testinsertDestinationWhenItexistsshouldReturnTheID() throws UnsupportedEncodingException {
        // given
        String respBody = "{\"data\":{\"ID\":\"6\"},\"code\":1}";
        mockWith("action=getIdDestination&url="
                + Utils.escapeDataString("http://www.virginmegastore.com.sa/english/home"), 200, respBody);
        //when
        String ret =databaseAPIConnector.insertDestination( "http://www.virginmegastore.com.sa/english/home");
        //then
        assertNotNull(ret);
        Assert.assertEquals("6", ret);

    }
    @Test
    public void testinsertDestinationNotFoundShouldCreateITAndReturnTheID() throws UnsupportedEncodingException {
        // given
        String respBody = "{\"data\":false,\"code\":1}";
        mockWith("action=getIdDestination&url="
                + Utils.escapeDataString("http://www.virginmegastore.com.sa/english/home"), 200, respBody);
        String respInsertBody = "{\"data\":77,\"code\":1}";
        mockWith("action=insertDestination&type=1&url="
                + Utils.escapeDataString("http://www.virginmegastore.com.sa/english/home")+"&screenshotpath=", 200, respInsertBody);
        //when
        String ret =databaseAPIConnector.insertDestination("http://www.virginmegastore.com.sa/english/home");
        //then
        assertNotNull(ret);
        Assert.assertEquals("77", ret);

    }

    @Test
    public void getFacebookPageIdIsOk(){
        // given
        String respBody = "{\"data\":{\"id\":\"17100\",\"parent_id\":null,\"name\":\"Facebook\"" +
                ",\"url\":\"http:\\/\\/www.facebook.com\"" +
                ",\"selectorcss\":\"\"" +
                ",\"selectorxpath\":\"\"" +
                ",\"xpathlandingpage\":\"\"" +
                ",\"langue\":\"ar,en,fr\",\"status\":\"1\",\"xpath\":\"\",\"visits\":\"1\"},\"code\":1}";
        mockWith("action=getFacebookPageId", 200, respBody);

        //when
        String ret =databaseAPIConnector.getFacebookPageId();
        //then
        assertNotNull(ret);
        Assert.assertEquals("17100", ret);
    }


    @Test
    public void getYoutubeHomePageId(){
        // given
        String respBody = "{\"data\":" +
                "{\"id\":\"17101\",\"parent_id\":null,\"name\":\"Youtube\"," +
                "\"url\":\"http:\\/\\/www.youtube.com\",\"selectorcss\":\"\"," +
                "\"selectorxpath\":\"\",\"xpathlandingpage\":\"\",\"langue\":\"ar,en,fr\",\"status\":\"1\"," +
                "\"xpath\":\"\",\"visits\":\"1\"},\"code\":1}";
        mockWith("action=getYoutubeHomePageId", 200, respBody);

        //when
        String ret = databaseAPIConnector.getYoutubeHomePageId();
        //then
        assertNotNull(ret);
        Assert.assertEquals("17101", ret);
    }

    @Test
    public void getYoutubeHomePageIdThrowException(){
        // given
        String respBody = "{\"data\":" +
                "{\"id\":\"17101\",\"parent_id\":null,\"name\":\"Youtube\"," +
                "\"url\":\"http:\\/\\/www.youtube.com\",\"selectorcss\":\"\"," +
                "\"selectorxpath\":\"\",\"xpathlandingpage\":\"\",\"langue\":\"ar,en,fr\",\"status\":\"1\"," +
                "\"xpath\":\"\",\"visits\":\"1\"},\"code\":1}";
        //mockWith("action=getYoutubeHomePageId", 200, respBody);
        stubFor(post(urlEqualTo("/pixitrendRobotAPIDEV.php"))
                .withHeader("Content-Type", equalTo("application/x-www-form-urlencoded"))
                .withRequestBody(equalTo("action=getYoutubeHomePageId"))
                .willReturn(aResponse()
                        .withStatus(500)));

        //when
        String ret = databaseAPIConnector.getYoutubeHomePageId();
        //then
        assertNull(ret);
    }

    @Test
    public void get1Visual(){
        // given
        String respBody = "{\"data\":[{\"country\":\"2\",\"idVisual\":\"1\",\"idMedia\":\"1\",\"idZone\":null}],\"code\":1}";
        mockWith("action=getVisual&checksum=c869988dd2c68fa55f9c73522e464040", 200, respBody);

        //when
        VisualResponse ret =databaseAPIConnector.getVisual("c869988dd2c68fa55f9c73522e464040");
        //then
        assertNotNull(ret);
        Assert.assertEquals("1",ret.getIdMedia());
        Assert.assertEquals(1,ret.getVisuals().size());
        Assert.assertEquals("1", ret.getVisuals().get(0).getIdVisual());
        Assert.assertEquals("2", ret.getVisuals().get(0).getCountry());
        assertNull(ret.getVisuals().get(0).getZone());
    }
    @Test
    public void get1VisualCase2(){
        // given
        String respBody = "{\"data\":[{\"country\":\"1\",\"idVisual\":\"1223145\",\"idMedia\":\"1076108\",\"idZone\":null}],\"code\":1}";
        mockWith("action=getVisual&checksum=32813a478f4903e33bd42bf3d7f14df0", 200, respBody);

        //when
        VisualResponse ret =databaseAPIConnector.getVisual("32813a478f4903e33bd42bf3d7f14df0");
        //then
        assertNotNull(ret);
        assertEquals("1076108",ret.getIdMedia());
        Assert.assertEquals(1,ret.getVisuals().size());
        Assert.assertEquals("1223145", ret.getVisuals().get(0).getIdVisual());
        Assert.assertEquals("1", ret.getVisuals().get(0).getCountry());
        assertNull(ret.getVisuals().get(0).getZone());
    }
    @Test
    public void get2Visual(){
        // given
        String respBody = "{\"data\":[{\"country\":\"1\",\"idVisual\":\"1\",\"idMedia\":\"1\",\"idZone\":null},{\"country\":\"5\",\"idVisual\":\"2\",\"idMedia\":\"1\",\"idZone\":null}],\"code\":1}";
        mockWith("action=getVisual&checksum=c869988dd2c68fa55f9c73522e464042", 200, respBody);

        //when
        VisualResponse ret =databaseAPIConnector.getVisual("c869988dd2c68fa55f9c73522e464042");
        //then
        assertNotNull(ret);
        Assert.assertEquals("1",ret.getIdMedia());
        Assert.assertEquals(2,ret.getVisuals().size());
        Assert.assertEquals("1", ret.getVisuals().get(0).getIdVisual());
        Assert.assertEquals("2", ret.getVisuals().get(1).getIdVisual());
        assertNull(ret.getVisuals().get(0).getZone());
        assertNull(ret.getVisuals().get(1).getZone());
        Assert.assertEquals("1", ret.getVisuals().get(0).getCountry());
        Assert.assertEquals("5", ret.getVisuals().get(1).getCountry());
    }
    @Test
    public void get0Visual(){
        // given
        String respBody = "{\"data\":[],\"code\":1}";
        mockWith("action=getVisual&checksum=c869988dd2c68fa55f9c73522e464042", 200, respBody);

        //when
        VisualResponse ret =databaseAPIConnector.getVisual("c869988dd2c68fa55f9c73522e464042");
        //then
        Assert.assertNull(ret);
    }

    @Test
    public void testGetVisualsHavingZoneIdForFb(){
        // given
        String respBody = "{\"data\":[{\"country\":\"1\",\"idVisual\":\"1\",\"idMedia\":\"1\",\"idZone\":null},{\"country\":\"5\",\"idVisual\":\"2\",\"idMedia\":\"1\",\"idZone\":\"3\"}],\"code\":1}";
        mockWith("action=getVisual&checksum=c869988dd2c68fa55f9c73522e464042", 200, respBody);
        //when
        VisualResponse ret =databaseAPIConnector.getVisual("c869988dd2c68fa55f9c73522e464042");
        //then
        assertNotNull(ret);
        Assert.assertEquals("1",ret.getIdMedia());
        Assert.assertEquals(2, ret.getVisuals().size());
        assertNull(ret.getVisuals().get(0).getZone());
        Assert.assertEquals("3", ret.getVisuals().get(1).getZone());
    }

    @Test
    public void testInsertVisualWithoutZone() throws IOException {
        String respBody = "{\"data\":{\"ID\":\"2\"},\"code\":1}";
        mockWith("action=getIdDestination&url=2", 200, respBody);
        final String now = TimeUtils.now();
        final String escapedNow = Utils.escapeDataString(now);
        // without zone
        mockWith("action=insertNewVisual&date_created="+escapedNow+"&idMedia=1&country=56&destination_id=2", 200, "{'data':'12','code':1}");
        final String newVisualId = databaseAPIConnector.insertNewVisual("56", "2", "1", MediaType.HTML5, now);
        assertEquals(newVisualId, "12");
    }
    @Test
    public void testInsertVisualWithZone() throws IOException {
        String respBody = "{\"data\":{\"ID\":\"2\"},\"code\":1}";
        mockWith("action=getIdDestination&url=2", 200, respBody);
        final String now = TimeUtils.now();
        final String escapedNow = Utils.escapeDataString(now);
        // with zone
        mockWith("action=insertNewVisual&date_created="+escapedNow+"&idMedia=1&country=56&zone_id=3&destination_id=2", 200, "{'data':'15','code':1}");
        final String newFbVisualId = databaseAPIConnector.insertNewVisual("56", "2", "1", MediaType.FACEBOOK, now, "3");
        assertEquals(newFbVisualId, "15");
    }
    @Test
    public void testInsertVisualReturnsNull() throws IOException {
        String respBody = "{\"data\":{\"ID\":\"2\"},\"code\":1}";
        mockWith("action=getIdDestination&url=2", 200, respBody);
        final String now = TimeUtils.now();
        final String escapedNow = Utils.escapeDataString(now);
        // null response
        mockWith("action=insertNewVisual&date_created="+escapedNow+"&idMedia=1&country=56&destination_id=2", 200, "{'data':'0','code':1}");
        final String nullVisualId = databaseAPIConnector.insertNewVisual("56", "2", "1", MediaType.FACEBOOK, now);
        assertNull(nullVisualId);
    }
    @Test
    public void testGetVisualsWithToBeFixedValueEquals1(){
        // given
        String respBody = "{\"data\":[{\"country\":\"1\",\"idVisual\":\"1\",\"idMedia\":\"1\",\"toBeFixed\":\"1\"},{\"country\":\"5\",\"idVisual\":\"2\",\"idMedia\":\"1\",\"toBeFixed\":\"1\"}],\"code\":1}";
        mockWith("action=getVisual&checksum=c869988dd2c68fa55f9c73522e464042", 200, respBody);
        //when
        VisualResponse ret =databaseAPIConnector.getVisual("c869988dd2c68fa55f9c73522e464042");
        //then
        assertNotNull(ret);
        Assert.assertEquals("1",ret.getIdMedia());
        Assert.assertEquals(2, ret.getVisuals().size());
        assertEquals("1", ret.getToBeFixed());
    }

}
