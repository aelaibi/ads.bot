package ma.labs.bot.core;
/********************************************************
 *                                                      *
 *                    PIXITREND ROBOT                   *
 *                    BY labs GROUP                    *
 *                                                      *
 ********************************************************/

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.firefox.FirefoxBinary;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxProfile;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.springframework.stereotype.Component;

/**
 * Created by labs004 on 29/12/2016.
 */
@Component
public class WebDriverWrapper {
    public WebDriver createFireFoxDriver(FirefoxBinary binary, FirefoxProfile profile, DesiredCapabilities capabilities) {
        return new FirefoxDriver(binary, profile, capabilities);
    }
}
