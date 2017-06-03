package ma.labs.bot.core;

import ma.labs.bot.connectors.AWSConnector;
import ma.labs.bot.connectors.DatabaseAPIConnector;
import ma.labs.bot.data.Robot;
import ma.labs.bot.data.Visual;
import ma.labs.bot.data.VisualResponse;
import ma.labs.bot.rules.FileManagerService;
import ma.labs.bot.utils.Constants;
import ma.labs.bot.utils.TimeUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxProfile;
import org.openqa.selenium.htmlunit.HtmlUnitWebElement;
import org.openqa.selenium.remote.DesiredCapabilities;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;
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
public class ScraperTest {



    @Mock
    protected WebDriverWrapper webDriverWrapper;
    @Mock
    protected FileManagerService fileManagerService;
    @Mock
    protected DatabaseAPIConnector databaseAPIConnector;
    @Mock
    protected AWSConnector awsConnector;

    @InjectMocks
    Scraper scraper;

    @Before
    public void setup() throws IOException {
        System.setProperty(TimeUtils.IGNORE_WAIT, "yes");//to ignore TimeUtils.waitfor
    }


    @Test
    public void openBrowserProxyWellConfigured() throws Throwable{
        //given
        final FirefoxProfile[] profile = new FirefoxProfile[1];
        final DesiredCapabilities[] capabilities = new DesiredCapabilities[1];
        FirefoxDriver driverMock = mockDriver(profile, capabilities);

        Robot robot = getRobotWithProxy();
        scraper.setRobotData(robot);
        mockProxyChek(driverMock);

        //when
        scraper.openBrowser();
        //then
        assertProfileAndCapabilities(profile[0], capabilities[0]);
    }

    @Test
    public void openBrowserProxyWellConfiguredForNonAnonymousAndConnected() throws Throwable{
        //given
        final FirefoxProfile[] profile = new FirefoxProfile[1];
        final DesiredCapabilities[] capabilities = new DesiredCapabilities[1];
        FirefoxDriver driverMock = mockDriver(profile, capabilities);
        List<WebElement> out= new ArrayList<>();
        WebElement link = mock(WebElement.class);
        out.add(link);
        By inborOrContact = By.xpath("//a[contains(text(),'Inbox') or contains(text(), 'Contacts')]");
        when(driverMock.findElements(inborOrContact)).thenReturn(out);

        Robot robot = getRobotWithProxy();
        robot.setProfileID(15);//not anonymous
        scraper.setRobotData(robot);

        mockProxyChek(driverMock);
        when(driverMock.getPageSource()).thenReturn("123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890");
        //when
        scraper.openBrowser();
        //then
        assertProfileAndCapabilities(profile[0], capabilities[0]);
        verify(driverMock, times(1)).findElements(inborOrContact);
        verify(driverMock, times(0)).findElements(By.xpath("//input[contains(@class,'maia-button-secondary')]"));
    }
    @Test
    public void openBrowserProxyWellConfiguredForNonAnonymousAndConnectedFirstTime() throws Throwable{
        //given
        final FirefoxProfile[] profile = new FirefoxProfile[1];
        final DesiredCapabilities[] capabilities = new DesiredCapabilities[1];
        FirefoxDriver driverMock = mockDriver(profile, capabilities);
        List<WebElement> out= new ArrayList<>();
        WebElement link = mock(WebElement.class);
        out.add(link);
        By basicHtml = By.xpath("//input[contains(@class,'maia-button-secondary')]");
        when(driverMock.findElements(basicHtml)).thenReturn(out);
        when(driverMock.getPageSource()).thenReturn("123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890");
        mockProxyChek(driverMock);

        Robot robot = getRobotWithProxy();
        robot.setProfileID(15);//not anonymous
        scraper.setRobotData(robot);

        //when
        scraper.openBrowser();
        //then
        assertProfileAndCapabilities(profile[0], capabilities[0]);
        verify(driverMock, times(1)).findElements(By.xpath("//a[contains(text(),'Inbox') or contains(text(), 'Contacts')]"));
        verify(driverMock, times(1)).findElements(basicHtml);
    }

    @Test
    public void openBrowserProxyPacIsnotWorking() throws Throwable{
        //given
        final FirefoxProfile[] profile = new FirefoxProfile[1];
        final DesiredCapabilities[] capabilities = new DesiredCapabilities[1];
        FirefoxDriver driverMock = mockDriver(profile, capabilities);
        List<WebElement> out= new ArrayList<>();
        WebElement link = mock(WebElement.class);
        out.add(link);
        By basicHtml = By.xpath("//input[contains(@class,'maia-button-secondary')]");
        when(driverMock.findElements(basicHtml)).thenReturn(out);

        //the same public IP
        when(fileManagerService.getPublicIpAdress()).thenReturn("10.10.10.10");
        WebElement body = mock(WebElement.class);
        when(body.getText()).thenReturn("10.10.10.10");
        when(driverMock.findElement(By.tagName("body"))).thenReturn(body);

        Robot robot = getRobotWithProxy();
        robot.setProfileID(15);//not anonymous
        scraper.setRobotData(robot);

        //when
        try {
            scraper.openBrowser();
        }catch (IllegalArgumentException iae){
            //then
            assertNotNull(iae);
            assertEquals("Firefox proxy is not ready, robot is shutting down !!", iae.getMessage());
        }

        verify(driverMock, times(1)).findElements(basicHtml);
    }

    private void mockProxyChek(FirefoxDriver driverMock) {
        when(fileManagerService.getPublicIpAdress()).thenReturn("10.10.10.10");
        WebElement body = mock(WebElement.class);
        when(body.getText()).thenReturn("55.55.55.66");
        when(driverMock.findElement(By.tagName("body"))).thenReturn(body);
    }

    @Test(expected = RuntimeException.class)
    public void openBrowserProxyWellConfiguredForNonAnonymousAndNotConnected() throws Throwable{
        //given
        final FirefoxProfile[] profile = new FirefoxProfile[1];
        final DesiredCapabilities[] capabilities = new DesiredCapabilities[1];
        FirefoxDriver driverMock = mockDriver(profile, capabilities);


        Robot robot = getRobotWithProxy();
        robot.setProfileID(15);//not anonymous
        scraper.setRobotData(robot);

        //when
        scraper.openBrowser();

    }

    private Robot getRobotWithProxy() {
        when(fileManagerService.downloadFileAsString(contains("proxy=collector-sharjah-003.pixitrend.net&port=7788")))
                .thenReturn(".... FindProxyForURL ....");

        return prepareRobot("collector-sharjah-003.pixitrend.net:7788");
    }

    private void assertProfileAndCapabilities(FirefoxProfile object, DesiredCapabilities capability) {
        assertNull(scraper.firefoxProcessId);
        assertNotNull(object);
        assertNotNull(capability);
        assertTrue(capability.isJavascriptEnabled());
        assertFalse((Boolean) capability.getCapability("marionette"));
        assertEquals("http://api.pixitrend.com/pac.php?proxy=collector-sharjah-003.pixitrend.net&port=7788"
                , object.getStringPreference("network.proxy.autoconfig_url",""));
        assertEquals(2, object.getIntegerPreference("network.proxy.type",0));
    }

    @Test
    public void openBrowserProxyNotEnabled() throws Throwable{
        //given
        final FirefoxProfile[] profile = new FirefoxProfile[1];
        final DesiredCapabilities[] capabilities = new DesiredCapabilities[1];
        mockDriver(profile, capabilities);


        Robot robot = prepareRobot(null);
        scraper.setRobotData(robot);

        //when
        scraper.openBrowser();
        //then
        assertNull(scraper.firefoxProcessId);
        assertNotNull(profile[0]);
        assertNotNull(capabilities[0]);
        assertTrue(capabilities[0].isJavascriptEnabled());
        assertFalse((Boolean) capabilities[0].getCapability("marionette"));
        assertEquals(""
                , profile[0].getStringPreference("network.proxy.autoconfig_url",""));
        assertEquals(0, profile[0].getIntegerPreference("network.proxy.type",0));
    }

    @Test
    public void openBrowserProxyNonConfigured() throws Throwable{
        //given
        final FirefoxProfile[] profile = new FirefoxProfile[1];
        final DesiredCapabilities[] capabilities = new DesiredCapabilities[1];
        mockDriver(profile, capabilities);

        when(fileManagerService.downloadFileAsString(contains("proxy=collector-sharjah-003.pixitrend.net&port=7788")))
                .thenReturn(".... ....");

        Robot robot = prepareRobot("collector-sharjah-003.pixitrend.net:7788");
        scraper.setRobotData(robot);
        //when
        try {
            scraper.openBrowser();
        }catch (IllegalArgumentException iae){
            assertNotNull(iae);
            assertEquals("Robot proxy not configured, stop the robot !!", iae.getMessage());
        }
    }

    @Test
    public void findElementsSafeOnRoot() throws Throwable{
        //given
        final FirefoxProfile[] profile = new FirefoxProfile[1];
        final DesiredCapabilities[] capabilities = new DesiredCapabilities[1];
        FirefoxDriver driverMock = mockDriver(profile, capabilities);
        By xpath = By.xpath("//div");
        List<WebElement> out= new ArrayList<>();
        out.add(new HtmlUnitWebElement(null, null));
        when(driverMock.findElements(xpath)).thenReturn(out);
        scraper.driver = driverMock;
         //when
        List<WebElement> ret = scraper.findElementsSafe(xpath, null);
        assertNotNull(ret);
        assertEquals(1,ret.size());
    }

    @Test
    public void getCodeSourceOfCurrentFrame() throws Throwable{
        //given
        final FirefoxProfile[] profile = new FirefoxProfile[1];
        final DesiredCapabilities[] capabilities = new DesiredCapabilities[1];
        FirefoxDriver driverMock = mockDriver(profile, capabilities);

        String out = "<html><head><title>Title &amp; tags</title></head><body><h1>labs</h1></body></html>";
        when(driverMock.getPageSource()).thenReturn(out);
        scraper.driver = driverMock;
        //when
        String ret = scraper.getCodeSourceOfCurrentFrame();
        assertNotNull(ret);
        assertEquals("<html><head><title>Title & tags</title></head><body><h1>labs</h1></body></html>",ret);
    }
    private void prepareScraper(){
        //given
        final FirefoxProfile[] profile = new FirefoxProfile[1];
        final DesiredCapabilities[] capabilities = new DesiredCapabilities[1];
        FirefoxDriver driverMock = mockDriver(profile, capabilities);

        when(driverMock.getPageSource()).thenReturn("123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890");

        scraper.driver = driverMock;
    }
    @Test
    public void goToUrlWithoutProxy() throws Exception {
        //given
        prepareScraper();
        Robot robot = prepareRobot(null);
        when(scraper.executeJScriptFile("js/getIp.js"))
                .thenReturn("123.456.789.012");
        when(fileManagerService.getPublicIpAdress())
                .thenReturn("123.456.789.012");
        scraper.setRobotData(robot);
        //then
        scraper.goToURL("https://www.google.com");
    }
    @Test(expected = ProblemLoadingPageException.class)
    public void goToUrlWithProxyButError() throws Exception {
        //given
        prepareScraper();
        Robot robot = getRobotWithProxy();
        when(scraper.executeJScriptFile("js/getIp.js"))
                .thenReturn("123.456.789.012");
        when(fileManagerService.getPublicIpAdress())
                .thenReturn("123.456.789.012");
        scraper.setRobotData(robot);
        //then
        scraper.goToURL("https://www.google.com");
    }
    @Test
    public void goToUrlWithProxyAndITsOK() throws Exception {
        //given
        prepareScraper();
        Robot robot = getRobotWithProxy();
        when(scraper.executeJScriptFile("js/getIp.js"))
                .thenReturn("123.456.789.012");
        when(fileManagerService.getPublicIpAdress())
                .thenReturn("1.2.3.4");
        scraper.setRobotData(robot);
        //then
        scraper.goToURL("https://www.google.com");
    }

    @Test
    public void goToURLNotConnected() throws Throwable{
        //given
        final FirefoxProfile[] profile = new FirefoxProfile[1];
        final DesiredCapabilities[] capabilities = new DesiredCapabilities[1];
        FirefoxDriver driverMock = mockDriver(profile, capabilities);
        when(driverMock.getTitle()).thenReturn("Problem loading page");
        scraper.driver = driverMock;
        //when
        try {
            scraper.goToURL("https://evernote.com/");
        }catch (ProblemLoadingPageException e){
            assertEquals("Problem loading page https://evernote.com/" , e.getMessage());
        }


    }

    private Robot prepareRobot(String proxy) {
        Robot robot = new Robot();
        robot.setIsFB("0");
        robot.setCountry("4");
        robot.setName("bba omar bot");
        robot.setFirefoxFile("http://api.pixitrend.com/profiles/male_18.zip");
        robot.setFirefoxProxyEnable((proxy!=null)?1:0);
        robot.setFirefoxProxy(proxy);
        robot.setProfileID(Constants.ANONYMOUS_PROFILE);
        return robot;
    }

    private FirefoxDriver mockDriver(FirefoxProfile[] profile, DesiredCapabilities[] capabilities) {
        FirefoxDriver driverMock = mock(FirefoxDriver.class);
        doAnswer( invocation -> {
            profile[0] = (FirefoxProfile) invocation.getArguments()[1];
            capabilities[0] = (DesiredCapabilities) invocation.getArguments()[2];
            return driverMock;
        }).when(webDriverWrapper).createFireFoxDriver(any(),any(),any());
        WebDriver.Options manageMock = mock(WebDriver.Options.class);
        when(driverMock.manage()).thenReturn(manageMock);
        WebDriver.Window windowMock = mock(WebDriver.Window.class);
        when(manageMock.window()).thenReturn(windowMock);
        return driverMock;
    }


    @Test
    public void getChecksumFileDataAndUploadTrueAndFoundVisual() throws Exception {
        String country = "66";
        Robot robot = new Robot();
        robot.setCountry(country);
        scraper.setRobotData(robot);
        String url = "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAABQAAAAUCAIAAAAC64paAAAAA";
        given(fileManagerService.getChecksumText(Matchers.startsWith(url)))
                .willReturn("azert123");
        given(fileManagerService.getChecksumText(url))
                .willReturn("checksumurlazerty");
        VisualResponse vis = new VisualResponse();

        vis.setChecksum("checksumurlazerty");
        vis.setIdMedia("55");
        vis.setVisuals(new ArrayList<>());
        vis.getVisuals().add(new Visual(country, null, "8"));
        given(databaseAPIConnector.getVisual("checksumurlazerty"))
                .willReturn(vis);


        ChecksumFileResponse out = scraper.getChecksumFile(url, true);

        assertNotNull(out);
        assertEquals("checksumurlazerty",out.getChecksumFile());
        assertEquals("0",out.getIsNewMedia());
        assertEquals("0",out.getIsNewVisual());
        assertEquals("55",out.getIdMedia());
        assertEquals("8",out.getIdVisual());
        Mockito.verify(fileManagerService, never()).getChecksumFileinMedia(anyString());
        Mockito.verify(fileManagerService, times(1)).copyFileInMedia(anyString(),anyString());
        Mockito.verify(databaseAPIConnector, times(1)).getVisual(anyString());

    }

    @Test
    public void getChecksumFileDataAndUploadFalse() throws Exception {
        String url = "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAABQAAAAUCAIAAAAC64paAAAAA";
        given(fileManagerService.getChecksumText(Matchers.startsWith(url)))
                .willReturn("azert123");
        given(fileManagerService.getChecksumText(url))
                .willReturn("checksumurlazerty");


        ChecksumFileResponse out = scraper.getChecksumFile(url, false);

        assertNotNull(out);
        assertEquals("checksumurlazerty",out.getChecksumFile());
        assertEquals("0",out.getIsNewMedia());
        assertEquals("0",out.getIsNewVisual());
        assertEquals("0",out.getIdMedia());
        assertEquals("0",out.getIdVisual());
        Mockito.verify(fileManagerService, never()).getChecksumFileinMedia(anyString());
        Mockito.verify(fileManagerService, never()).copyFileInMedia(anyString(),anyString());
        Mockito.verify(databaseAPIConnector, never()).getVisual(anyString());

    }
    @Test
    public void getChecksumFileDataAndUploadTrue() throws Exception {
        String url = "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAABQAAAAUCAIAAAAC64paAAAAA";
        given(fileManagerService.getChecksumText(Matchers.startsWith(url)))
                .willReturn("azert123");
        given(fileManagerService.getChecksumText(url))
                .willReturn("checksumurlazerty");


        ChecksumFileResponse out = scraper.getChecksumFile(url, true);

        assertNotNull(out);
        assertEquals("checksumurlazerty",out.getChecksumFile());
        assertEquals("1",out.getIsNewMedia());
        assertEquals("1",out.getIsNewVisual());
        assertEquals("0",out.getIdMedia());
        assertEquals("0",out.getIdVisual());
        Mockito.verify(fileManagerService, never()).getChecksumFileinMedia(anyString());
        Mockito.verify(fileManagerService, times(1)).copyFileInMedia(anyString(),anyString());
        Mockito.verify(databaseAPIConnector, times(1)).getVisual(anyString());

    }

    @Test
    public void getChecksumFileHttpAndUploadFalse() throws Exception {
        String url = "http://www.site.com/image.png";
        given(fileManagerService.getChecksumText(Matchers.startsWith(url)))
                .willReturn("azert123");
        given(fileManagerService.getChecksumFileinMedia(Matchers.contains("azert123")))
                .willReturn("checksumazert123");

        ChecksumFileResponse out = scraper.getChecksumFile(url, false);

        assertNotNull(out);
        assertEquals("checksumazert123",out.getChecksumFile());
        Mockito.verify(fileManagerService, never()).copyFileInMedia(anyString(),anyString());
        Mockito.verify(databaseAPIConnector, never()).getVisual(anyString());

    }
    @Test
    public void getChecksumFileHttpAndUploadTrue() throws Exception {
        String url = "http://www.site.com/image.png";
        given(fileManagerService.getChecksumText(Matchers.startsWith(url)))
                .willReturn("azert123");
        given(fileManagerService.getChecksumFileinMedia(Matchers.contains("azert123")))
                .willReturn("checksumazert123");

        ChecksumFileResponse out = scraper.getChecksumFile(url, true);

        assertNotNull(out);
        assertEquals("checksumazert123",out.getChecksumFile());
        Mockito.verify(fileManagerService, times(1)).copyFileInMedia(anyString(),anyString());
        Mockito.verify(databaseAPIConnector, times(1)).getVisual(anyString());

    }

    @Test
    public void getDestinationURLShouldReturnAdurl() throws Exception {
        String expected = "http://promo.weezchat.ma/pwzchatma/lp_weez_wall/go/?ext_code=Google-c-c---96356441077-www.mulhak.com";
        String href = "https://googleads.g.doubleclick.net/aclk?sa=l&ai=CghF71CThV42AJceebpyui-gDoK7m_ET1xtaL5wKtyv3fBRABIMSqtB9g-bvvhOQvoAGzi4e7A8gBAqkCP6LxmzXFsj6oAwHIA8EEqgTEAk_Q9viR8ABwl33MZpktyCoGoDdbhJ6k7fqcUSd5rRjA8ipSRt_eZVeSgyCY7EP21qEUJ2abNjZM4CTL6nSb0S5FonWzhooHQN1gDus5fatqSsepMyRWDgsRsx7stuT-Rd0IICzm4UeTndIBnCY0fE3lhkY2n6lV2j8Rj0_qPhFkJPR36VEvbGt_C6RwKfveBOm1_tii9JejLl7dVpVurSpYuao31y2DAyKTaXMwySUp9YRfqI1VrG1sOaAZBIgXOIIgPIBC4MCotFUIvCU3WYUDRTt1612tEtq-OQo5LEsP7fhaRDSWaDfipdyQyY7CV8hmaKrDhkWvUkM2FJY-cFDZ-9v3EHgnqdwF2d41WVFQBrR1Pfng8oXekcKSvQq39AQIVWcXsyWqxn8jFhucnW0AnHJjR42Odr8oYDmmMNO8oSckZKAGAoAHtfT4RKgHgcYbqAemvhvYBwHSCAUIgGEQAdgTDA&num=1&sig=AOD64_29S7Ee81OfxxXS3GFbH8l_UQ7lOg&client=ca-pub-4638717473313198&adurl=http://promo.weezchat.ma/pwzchatma/lp_weez_wall/go/%3Fext_code%3DGoogle-c-c---96356441077-www.mulhak.com";
        given(fileManagerService.getFinalURL(expected))
                .willReturn(expected);

        String dest = scraper.getDestinationURL(href);

        assertEquals(expected, dest);
        verify(fileManagerService, times(1)).getFinalURL(expected);
    }

    @Test
    public void getDestinationURLShouldReturnJavascript() throws Exception {
        String href = "javascript:void(0);";

        String dest = scraper.getDestinationURL(href);

        assertEquals(href, dest);
        verify(fileManagerService, times(0)).getFinalURL(anyString());
    }

    @Test
    public void isNewMediaShouldReturnTrueWhenNull(){
        IsNewMediaResponse resp = scraper.isNewMedia("checksum123");
        assertEquals(new IsNewMediaResponse( "1", "1", "0", "0", "0" ), resp);
    }

    @Test
    public void isNewMediaShouldReturnTrueWhenNotNullAndVisualNotFound(){
        //given
        VisualResponse visualResponse = new VisualResponse();
        visualResponse.setChecksum("checksum123");
        visualResponse.setIdMedia("55");
        visualResponse.setVisuals(new ArrayList<>());
//        visualResponse.setCountries(new HashSet<>());
        given(databaseAPIConnector.getVisual("checksum123")).willReturn(visualResponse);
        //when
        IsNewMediaResponse resp = scraper.isNewMedia("checksum123");
        //then
        assertEquals(new IsNewMediaResponse( "0", "1", "55", "0", "0" ), resp);
    }
    @Test
    public void isNewMediaShouldReturnTrueWhenNotNullAndVisualNotFoundInThisCountry(){
        //given
        String country = "6";
        Robot robot = new Robot();
        robot.setCountry(country);
        scraper.setRobotData(robot);
        VisualResponse visualResponse = new VisualResponse();
        visualResponse.setChecksum("checksum123");
        visualResponse.setIdMedia("55");
        visualResponse.setVisuals(new ArrayList<>());
        visualResponse.getVisuals().add(new Visual("8", null, "2"));
        visualResponse.getVisuals().add(new Visual("4", null, "1"));
        given(databaseAPIConnector.getVisual("checksum123")).willReturn(visualResponse);
        //when
        IsNewMediaResponse resp = scraper.isNewMedia("checksum123");
        //then
        assertEquals(new IsNewMediaResponse( "0", "1", "55", "0", "0" ), resp);
    }
    @Test
    public void isNewMediaShouldReturnTrueWhenNotNullAndVisualFoundInThisCountry(){
        //given
        String country = "6";
        Robot robot = new Robot();
        robot.setCountry(country);
        scraper.setRobotData(robot);
        VisualResponse visualResponse = new VisualResponse();
        visualResponse.setChecksum("checksum123");
        visualResponse.setIdMedia("55");
        visualResponse.setVisuals(new ArrayList<>());
        visualResponse.getVisuals().add(new Visual("8", null, "2"));
        visualResponse.getVisuals().add(new Visual("6", null, "99"));
        visualResponse.getVisuals().add(new Visual("4", null, "1"));
        given(databaseAPIConnector.getVisual("checksum123")).willReturn(visualResponse);
        //when
        IsNewMediaResponse resp = scraper.isNewMedia("checksum123");
        //then
        assertEquals(new IsNewMediaResponse( "0", "0", "55", "99", "0" ), resp);
    }

    @Test
    public void isNewMediaShouldRe(){
        //given
        String country = "1";
        Robot robot = new Robot();
        robot.setCountry(country);
        scraper.setRobotData(robot);
        VisualResponse visualResponse = new VisualResponse();
        String checksum = "32813a478f4903e33bd42bf3d7f14df0";
        visualResponse.setChecksum(checksum);
        visualResponse.setIdMedia("1076108");
        visualResponse.setVisuals(new ArrayList<>());
        visualResponse.getVisuals().add(new Visual("1", null, "1223145"));
        given(databaseAPIConnector.getVisual(checksum)).willReturn(visualResponse);
        //when
        IsNewMediaResponse resp = scraper.isNewMedia(checksum);
        //then
        assertEquals(new IsNewMediaResponse( "0", "0", "1076108", "1223145", "0" ), resp);
    }

    @Test
    public void isNewMediaWontBeFixed(){
        //given
        String country = "1";
        Robot robot = new Robot();
        robot.setCountry(country);
        scraper.setRobotData(robot);
        VisualResponse visualResponse = new VisualResponse();
        String checksum = "123";
        visualResponse.setChecksum(checksum);
        visualResponse.setIdMedia("100");
        visualResponse.setVisuals(new ArrayList<>());
        visualResponse.setToBeFixed("0");
        visualResponse.getVisuals().add(new Visual("1", null, "101"));
        given(databaseAPIConnector.getVisual(checksum)).willReturn(visualResponse);
        //when
        IsNewMediaResponse resp = scraper.isNewMedia(checksum);
        //then
        assertEquals(new IsNewMediaResponse( "0", "0", "100", "101", "0"), resp);
        assertFalse(resp.isNewMedia());
        assertFalse(resp.isNewVisual());
        assertEquals("0", resp.getToBeFixed());
    }
    @Test
    public void isNewMediaHaveToBeFixed(){
        //given
        String country = "1";
        Robot robot = new Robot();
        robot.setCountry(country);
        scraper.setRobotData(robot);
        VisualResponse visualResponse = new VisualResponse();
        String checksum = "123";
        visualResponse.setChecksum(checksum);
        visualResponse.setIdMedia("100");
        visualResponse.setVisuals(new ArrayList<>());
        visualResponse.setToBeFixed("1");
        visualResponse.getVisuals().add(new Visual("1", null, "101"));
        given(databaseAPIConnector.getVisual(checksum)).willReturn(visualResponse);
        //when
        IsNewMediaResponse resp = scraper.isNewMedia(checksum);
        //then
        assertEquals(new IsNewMediaResponse( "0", "0", "100", "101", "1"), resp);
        assertFalse(resp.isNewMedia());
        assertFalse(resp.isNewVisual());
        assertEquals("1", resp.getToBeFixed());
    }
    @Test
    public void isNewMediaNewMedia(){
        IsNewMediaResponse resp = scraper.isNewMedia("checksum123");
        assertTrue(resp.isNewMedia());
        assertTrue(resp.isNewVisual());
        assertEquals("0", resp.getToBeFixed());
    }
}