package ma.labs.bot.connectors;

import ma.labs.bot.data.util.MediaType;
import ma.labs.bot.rules.FileManagerService;
import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.File;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;

import static junit.framework.TestCase.assertNotNull;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.times;

/********************************************************
 *                                                      *
 *                    PIXITREND ROBOT                   *
 *                    BY labs GROUP                    *
 *                                                      *
 ********************************************************/

/**
 * Created by labs004 on 26/07/2016.
 */
@RunWith(MockitoJUnitRunner.class)
public class AWSConnectorTest {

    @InjectMocks
    private  AWSConnector awsConnector;

    @Mock
    FileManagerService fileManagerServiceMock;
    @Mock
    FTPConnector ftpConnectorMock;




    @Before
    public    void init() throws Exception {
        awsConnector.fileManagerService = fileManagerServiceMock;
        awsConnector.ftpConnector = ftpConnectorMock;
    }


    @Test
    public void upload() throws Exception {
        //given
        setup();

        URL url = this.getClass().getClassLoader().getResource("./media/bb51113fa2c341fa2f357da0c47f7bbc");
        assertNotNull(url);
        String fileName = "ael-test2";
        String mediaFolder = "media";
        FileUtils.deleteDirectory(Paths.get(mediaFolder).toFile());
        Files.createDirectory(Paths.get(mediaFolder));
        Files.copy(Paths.get(url.toURI()), Paths.get(mediaFolder, fileName));
        given(fileManagerServiceMock.getFileFromMedia(fileName)).willReturn(Paths.get(mediaFolder, fileName).toFile());
        //when
        awsConnector.upload(fileName, MediaType.IMAGE,false);
        //then
        then(fileManagerServiceMock).should(times(1)).getFileFromMedia(fileName);
        then(ftpConnectorMock).should(times(1)).upload(any(File.class));

        //clean
        FileUtils.deleteDirectory(Paths.get(mediaFolder).toFile());
    }

    @Test
    public void uploadNullFile() throws Exception {
        //given

        setup();
        //when
        awsConnector.upload(null,MediaType.IMAGE,false);
        //then
        then(fileManagerServiceMock).should(times(0)).getFileFromMedia(anyString());
    }

    @Test
    public void uploadFileNotExists() throws Exception {
        //given

        setup();
        String noFile = "noFile";
        given(fileManagerServiceMock.getFileFromMedia(noFile)).willReturn(Paths.get("", noFile).toFile());
        //when
        awsConnector.upload(noFile,MediaType.IMAGE,false);
        //then
        then(ftpConnectorMock).should(times(0)).upload(any(File.class));
    }

    private void setup() {
        awsConnector.secretKey="test";
        awsConnector.accessKey="test";
    }

}