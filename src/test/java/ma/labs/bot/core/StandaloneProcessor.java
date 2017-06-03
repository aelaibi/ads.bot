package ma.labs.bot.core;
/********************************************************
 *                                                      *
 *                    PIXITREND ROBOT                   *
 *                    BY labs GROUP                    *
 *                                                      *
 ********************************************************/

import ma.labs.bot.rules.FileManagerService;
import ma.labs.bot.utils.GUIHelper;
import ma.labs.bot.utils.TimeUtils;
import org.junit.runner.RunWith;
import org.openqa.selenium.*;
import org.openqa.selenium.phantomjs.PhantomJSDriver;
import org.openqa.selenium.phantomjs.PhantomJSDriverService;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by labs004 on 05/01/2017.
 */
@RunWith(SpringRunner.class)
@SpringBootTest
public class StandaloneProcessor {

    private static Logger logger = LoggerFactory.getLogger(StandaloneProcessor.class);

    @Autowired
    protected FileManagerService fileManagerService;
    @Autowired
    protected GUIHelper guiHelper;

    @org.junit.Test
    public  void run() throws MalformedURLException {
        logger.info("start");
        DesiredCapabilities capabilities = DesiredCapabilities.phantomjs();
        capabilities.setJavascriptEnabled(true);
        //capabilities.setCapability("takesScreenshot", false);
        capabilities.setCapability(
                PhantomJSDriverService.PHANTOMJS_EXECUTABLE_PATH_PROPERTY,
                "phantomJS/phantomjs.exe"
        );
        // Change "User-Agent" via page-object capabilities
        capabilities.setCapability(PhantomJSDriverService.PHANTOMJS_PAGE_SETTINGS_PREFIX + "userAgent",
                "Mozilla/5.0 (Windows NT 6.1; Win64; x64; rv:16.0) Gecko/20121026 Firefox/16.0");
        //  Disable "web-security", enable all possible "ssl-protocols" and "ignore-ssl-errors" for PhantomJSDriver
        capabilities.setCapability(PhantomJSDriverService.PHANTOMJS_CLI_ARGS, new String[] {
                "--web-security=false",
                "--ssl-protocol=any",
                "--ignore-ssl-errors=true",
                "--webdriver-loglevel=DEBUG"
        });
        //PhantomJsDriverManager.getInstance().setup();
        WebDriver driver = new PhantomJSDriver(capabilities);
        //new EADHtmlUnitDriver(BrowserVersion.FIREFOX_45, true);

        driver.navigate().to("https://s0.2mdn.net/4066784/1480958617918/index.html");
        TimeUtils.waitFor(5000);
        try {
            //window.location.pathname
            String file = fileManagerService.getFileContentAsString("js/relativeToAbsoluteUrls.js");
            ((JavascriptExecutor)driver)
                    .executeScript(file);
            System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>"+(String)((JavascriptExecutor)driver)
                    .executeScript("return window.location.pathname;"));
        }catch (Exception e){
            logger.error("",e);
        }



        guiHelper.captureScreenShot(driver,null,fileManagerService.getMediaPath()+"/aaaa.png");
        String ret = driver.getPageSource();
        //getUrlAllImages(driver, "//img");
        //System.out.println(ret);
    }



    public String[] getUrlAllImages(WebDriver driver,String specialXpathForImages) {
        try {
            List<WebElement> elements = driver.findElements(By.xpath(specialXpathForImages));
            List<String> result = new ArrayList<>();
            for(WebElement element : elements){
                final Dimension size = element.getSize();
                if(size.getHeight() == 1 && size.getWidth() == 1) {
                    continue;
                }
                String t = element.getAttribute("src");
                result.add(t);
                System.out.println(t);
            }
            List<String> tmp = result.stream().distinct().collect(Collectors.toList());
            logger.debug("got {} images", tmp.size());
            return tmp.toArray(new String[tmp.size()]);
        } catch (Exception e) {
            logger.error(e.getMessage());
        }
        return new String[0];
    }
}
