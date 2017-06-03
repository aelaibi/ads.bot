package ma.labs.bot.core;

import ma.labs.bot.connectors.AWSConnector;
import ma.labs.bot.connectors.DatabaseAPIConnector;
import ma.labs.bot.rules.FileManagerService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.openqa.selenium.*;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

/********************************************************
 *                                                      *
 *                    PIXITREND ROBOT                   *
 *                    BY labs GROUP                    *
 *                                                      *
 ********************************************************/

/**
 * Created by labs004 on 16/08/2016.
 */
@RunWith(MockitoJUnitRunner.class)
public class MobileScraperTest {



    @Mock
    protected FileManagerService fileManagerService;
    @Mock
    protected DatabaseAPIConnector databaseAPIConnector;
    @Mock
    protected AWSConnector awsConnector;
    @Mock
    protected WebDriver driver;

    @Mock
    MobileScraper scraper;

    @Mock
    WebElement webElement;

    @Before
    public void setup(){
        scraper.driver = driver;
    }

    @Test
    public void noCallToSeveralMethodsWhenNoPrerollDetected() throws Exception {
        //driver.findElements(By.xpath("//div[contains(@class,'videoAdUiAdInfoPopupText')]")).size() == 0)
        List<WebElement> weblements = new ArrayList<WebElement>();
        given(driver.findElements(By.xpath("//div[contains(@class,'videoAdUiAdInfoPopupText')]"))).willReturn(weblements);
//        given(driver.findElements(By.xpath("///div[contains(@class,'videoAdUiAdInfoPopupText')]"))).willReturn(weblements);
//        given(scraper.getUrlPrerollVideo()).willReturn("");
//        given(driver.findElements(By.xpath("//div[@id='movie_player']//video[contains(@class,'html5-main-video')]"))).willReturn(weblements);
        scraper.processYoutubeVideo("12","33");
//        driver.get
        Mockito.verify(driver, never()).findElement(By.xpath(anyString()));
        Mockito.verify(scraper, never()).getIdPrerollVideo();
    }
    @Test
    public void testPreRollVideoUrlIsFound() throws Exception {
        List<WebElement> weblements = new ArrayList<WebElement>();
        weblements.add(webElement);
        given(driver.findElements(By.xpath("//div[contains(@class,'videoAdUiAdInfoPopupText')]"))).willReturn(weblements);
        given(webElement.getAttribute("src")).willReturn("google.com");

        assertEquals(webElement.getAttribute("src"), "google.com");
        scraper.processYoutubeVideo(anyString(), anyString());
//        given(driver.findElements(By.xpath("//div[contains(@class,'videoAdUiAdInfoPopupText')]")).get(0).getAttribute("src")).willReturn("google.com");
//        Assert.assertTrue(scraper.isPrerollVideo());
    }

}