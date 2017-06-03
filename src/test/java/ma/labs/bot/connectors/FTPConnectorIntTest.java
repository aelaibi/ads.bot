package ma.labs.bot.connectors;

import ma.labs.bot.utils.TimeUtils;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.net.URL;
import java.nio.file.Paths;

import static org.junit.Assert.assertNotNull;

/********************************************************
 *                                                      *
 *                    PIXITREND ROBOT                   *
 *                    BY labs GROUP                    *
 *                                                      *
 ********************************************************/

/**
 * Created by labs004 on 15/09/2016.
 */
@RunWith(SpringRunner.class)
@SpringBootTest
public class FTPConnectorIntTest {

    @Autowired
    FTPConnector ftpConnector;

    @Test
    public void uploadNull() throws Exception {
        ftpConnector.upload(null);
        TimeUtils.waitFor(200);

    }

    @Test
    @Ignore
    public void uploadFile() throws Exception {
        URL url = this.getClass().getClassLoader().getResource("./media/imageFtp.png");
        assertNotNull(url);
        //ftpConnector.setMode("prod");
        ftpConnector.upload(Paths.get(url.toURI()).toFile());
        TimeUtils.waitFor(30000);

    }

}