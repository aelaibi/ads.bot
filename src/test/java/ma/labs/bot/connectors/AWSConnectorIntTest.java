package ma.labs.bot.connectors;

import ma.labs.bot.data.util.MediaType;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;

import static junit.framework.TestCase.assertNotNull;

/********************************************************
 *                                                      *
 *                    PIXITREND ROBOT                   *
 *                    BY labs GROUP                    *
 *                                                      *
 ********************************************************/

/**
 * Created by labs004 on 26/07/2016.
 */
@RunWith(SpringRunner.class)
@SpringBootTest
@Ignore
public class AWSConnectorIntTest {

    @Autowired
    private  AWSConnector awsConnector;




    @Test
    public void emptyTest(){
    }


    @Test
    public void upload() throws Exception {

        URL url = this.getClass().getClassLoader().getResource("./media/bb51113fa2c341fa2f357da0c47f7bbc");
        assertNotNull(url);
        //awsConnector, "mode", "prod");
        Files.copy(Paths.get(url.toURI()), Paths.get("media","testtesttest1200"));
        awsConnector.upload("testtesttest1200", MediaType.IMAGE,false);

        Thread.sleep(5000);
    }

}