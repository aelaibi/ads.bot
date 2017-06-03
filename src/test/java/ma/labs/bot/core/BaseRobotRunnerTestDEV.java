package ma.labs.bot.core;
/********************************************************
 *                                                      *
 *                    PIXITREND ROBOT                   *
 *                    BY labs GROUP                    *
 *                                                      *
 ********************************************************/

import ma.labs.bot.connectors.AWSConnector;
import ma.labs.bot.connectors.DatabaseAPIConnector;
import ma.labs.bot.connectors.MediaHelperAPIConnector;
import ma.labs.bot.data.*;
import ma.labs.bot.rules.FileManagerService;
import ma.labs.bot.utils.Utils;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.stubbing.Answer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.StringUtils;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;

/**
 * Created by labs004 on 11/08/2016.
 */
@RunWith(SpringRunner.class)
@SpringBootTest
public abstract class BaseRobotRunnerTestDEV {

    private static Logger logger = LoggerFactory.getLogger(BaseRobotRunnerTestDEV.class);

    @Mock
    DatabaseAPIConnector databaseAPIConnector;
    @Mock
    MediaHelperAPIConnector mediaHelperAPIConnector;
    @Mock
    AWSConnector awsConnector;

    IScraper scraper;
    @Autowired
    RobotRunner robotRunner ;
    @Autowired
    FileManagerService fileManagerService;

    List<String> pagesLink = new ArrayList<>();

    @Before
    public void setup()  throws IOException{
        MockitoAnnotations.initMocks(this);
        robotRunner.setDatabaseAPIConnector(databaseAPIConnector);

        fileManagerService.emptyMediaFolder();

        doAnswer(traceImpression())
                .when(databaseAPIConnector)
                .insertImpression(any(),any(),any(),any(),any(),any(),any(),any(),any(),any(),any(),any(),any(),any(),any(),anyLong());

        doAnswer(invocation -> {
                    return Integer.toString(Utils.getRandom(1256,9999));
                }
        ).when(databaseAPIConnector).insertExtraction(anyString(),anyString());

        given(mediaHelperAPIConnector.downloadByFileUrl(anyString()))
                .willReturn(new MediaHelperAPIResponse("by url", 300, 19856));
        given(mediaHelperAPIConnector.downloadByYoutubeId(anyString()))
                .willReturn(new MediaHelperAPIResponse("by id", 400, 1855));

        doNothing().when(mediaHelperAPIConnector).uploadByYoutubeID(anyString());
        doNothing().when(mediaHelperAPIConnector).uploadByFileUrl(anyString());
        doReturn(null).when(databaseAPIConnector).getVisual(anyString());
    }

    private Answer traceImpression() {
        return invocation -> {
                    String filename = (String) invocation.getArguments()[5];
                    String fileContent = invocation.toString();
                    try (BufferedWriter writer = Files.newBufferedWriter(Paths.get("media",filename+".txt"), StandardOpenOption.CREATE)) {
                        writer.write(fileContent);
                    }
            logger.info("<impression robotName='" + ((Robot)invocation.getArguments()[0]).getName()+"'"
                    + " idExtraction='" + invocation.getArguments()[1]+"'"
                    + " type='" + invocation.getArguments()[2]+"'"
                    + " content='" + invocation.getArguments()[3]+"'"
                    + " network='" + invocation.getArguments()[4]+"'"
                    + " filename='" + filename+"'"
                    + " originalURL='" + invocation.getArguments()[6]+"'"
                    + " destinationURL='" + invocation.getArguments()[7]+"'"
                    + " id_zone='" + invocation.getArguments()[8]+"'"
                    + " id_page='" + invocation.getArguments()[9]+"'"
                    + " sizeScreen='" + invocation.getArguments()[10]+"'"
                    + " size='" + invocation.getArguments()[11]+"'"
                    + " location='" + invocation.getArguments()[12]+"'"
                    + " duration='" + invocation.getArguments()[13]+"'"
                    + " isNewMedia='" + ((IsNewMediaResponse)invocation.getArguments()[14]).isNewMedia()+"'"
                    + " isNewVisual='" + ((IsNewMediaResponse)invocation.getArguments()[14]).isNewVisual()+"'"
                    + " fileSize='" + invocation.getArguments()[15] +"' />");
                    //final String savesDirectory = "C:\\media_saves\\";
                    //new File(savesDirectory).mkdirs();
                    //FileUtils.copyFile(Paths.get("media",filename).toFile(), new File(savesDirectory+filename+"__"+System.currentTimeMillis()));
                    //System.out.println(invocation);
                    return null;
                };
    }

    void mockPages() throws IOException {
        Pages pages = new Pages();
        pages.setData(
                pagesLink.stream()
                        .map(link -> new Page(Utils.getRandom(0,100),link,"body", null,null))
                        .toArray(Page[]::new)
        );
        given(databaseAPIConnector.getPages("4"))
                .willReturn(pages);
    }

    RobotInfo prepareRobot(int profile, BrowserMode device, String proxy) {
        RobotInfo robotInfo = new RobotInfo();
        robotInfo.setData(new Robot());
        robotInfo.getData().setIsFB("0");
        robotInfo.getData().setCountry("4");
        robotInfo.getData().setName("bba omar bot");
        robotInfo.getData().setFirefoxFile("http://api.pixitrend.com/profiles/male_18.zip");
        robotInfo.getData().setFirefoxProxyEnable((StringUtils.isEmpty(proxy))?0:1);
        robotInfo.getData().setFirefoxProxy("collector-"+proxy+"-001.pixitrend.net:7788");
        robotInfo.getData().setProfileID(profile);

        robotInfo.getData().setDeviceId(Integer.valueOf(device.getCode()));
        if(device==BrowserMode.MOBILE){
            robotInfo.getData().setDeviceUserAgent("Mozilla/5.0 (Linux; Android 5.0; SM-G900P Build/LRX21T) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/48.0.2564.23 Mobile Safari/537.36");
            robotInfo.getData().setDeviceName("Samsung GALAXY S5");
            robotInfo.getData().setDeviceWidth(360);
            robotInfo.getData().setDeviceHeight(640);
        }
        return robotInfo;
    }
}
