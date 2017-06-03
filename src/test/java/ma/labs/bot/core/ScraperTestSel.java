package ma.labs.bot.core;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import ma.labs.bot.utils.TimeUtils;
import org.junit.*;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import static com.github.tomakehurst.wiremock.client.WireMock.*;

/********************************************************
 *                                                      *
 *                    PIXITREND ROBOT                   *
 *                    BY labs GROUP                    *
 *                                                      *
 ********************************************************/

/**
 * Created by labs004 on 18/07/2016.
 */
@RunWith(SpringRunner.class)
@SpringBootTest
public class ScraperTestSel {

    @Autowired
    @Qualifier("scraper") private IScraper scraper;
    protected static WireMockServer wireMockServer;
    @BeforeClass
    public static void setupServer() {

        wireMockServer = new WireMockServer(9999);
        wireMockServer.start();

        WireMock.configureFor(wireMockServer.port());
    }

    @Before
    public void setup(){
        WireMock.resetToDefault();
        stubFor(get(urlEqualTo("/favicon.ico"))
                .willReturn(aResponse()
                        .withStatus(200)));
        scraper.setFirefoxProfiles("BrowserProfiles");
        scraper.openBrowser();
        TimeUtils.waitFor(200);
    }
    @AfterClass
    public static void serverShutdown() {
        wireMockServer.stop();
    }

    @After
    public void fin(){
        scraper.closeBrowser();
    }

    @Test
    public void testFirefoxProcessIDNotNull(){
        Assert.assertNotNull(scraper.getFirefoxProcessID());
    }



    @Test
    public void assertBrowserIsOpenedWhenOpenning(){
        //empty
    }

    @Test
    public void testGetCodeSourceOfCurrentFrameIsWorking()throws Exception{
        //given
        stubFor(get(urlEqualTo("/test.html"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "text/html")
                        .withBody("<html><head><title>Title</title></head><body><h1>labs</h1></body></html>")));
        //when
        scraper.goToURL("http://localhost:9999/test.html");
        String html = scraper.getCodeSourceOfCurrentFrame(false);
        //then
        Assert.assertNotNull(html);
        Assert.assertEquals("<html xmlns=\"http://www.w3.org/1999/xhtml\" webdriver=\"true\">" +
                "<head><title>Title</title></head><body><h1>labs</h1></body></html>",html);
    }

    @Test
    public void testMoveMouse()throws Exception{
        stubFor(get(urlEqualTo("/test.html"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "text/html")
                        .withBody("<html><head><title>Title</title></head><body><h1>labs</h1></body></html>")));

        scraper.goToURL("http://localhost:9999/test.html");
        //GUIHelper.moveMouse(true);
        TimeUtils.waitFor(5000);
    }


}