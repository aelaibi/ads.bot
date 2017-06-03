package ma.labs.bot.utils;

import org.junit.Test;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/********************************************************
 *                                                      *
 *                    PIXITREND ROBOT                   *
 *                    BY labs GROUP                    *
 *                                                      *
 ********************************************************/

/**
 * Created by labs004 on 23/11/2016.
 */
public class VersionReaderTest {
    @Test
    public void getVersion() throws Exception {
        String version = VersionReader.getVersion();
        assertNotNull(version);
        assertTrue(version.contains("BuildDate : [20"));
        assertTrue(version.contains("Version(SCM) ["));
    }

}