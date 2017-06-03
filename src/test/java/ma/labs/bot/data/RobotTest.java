package ma.labs.bot.data;

import org.junit.Assert;
import org.junit.Test;

/********************************************************
 *                                                      *
 *                    PIXITREND ROBOT                   *
 *                    BY labs GROUP                    *
 *                                                      *
 ********************************************************/

/**
 * Created by labs004 on 18/07/2016.
 */
public class RobotTest {

    Robot robot = new Robot();



    @Test(expected = RuntimeException.class)
    public void defaultBrowsingModeThrowsException() throws Exception {
        robot.getBrowserMode();
    }

    @Test
    public void deviceIdOneShouldReturnDesktopBrowsingMode() throws Exception {
        robot.setDeviceId(1);
        Assert.assertEquals(BrowserMode.DESKTOP, robot.getBrowserMode());
    }
    @Test
    public void deviceIdOtherThanOneShouldReturnMobileBrowsingMode() throws Exception {
        robot.setDeviceId(2);
        Assert.assertEquals(BrowserMode.MOBILE, robot.getBrowserMode());
    }

}