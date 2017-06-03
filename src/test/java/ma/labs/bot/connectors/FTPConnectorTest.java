package ma.labs.bot.connectors;

import org.apache.commons.net.ftp.FTPClient;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Paths;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

/********************************************************
 *                                                      *
 *                    PIXITREND ROBOT                   *
 *                    BY labs GROUP                    *
 *                                                      *
 ********************************************************/

/**
 * Created by labs004 on 30/12/2016.
 */
@RunWith(MockitoJUnitRunner.class)
public class FTPConnectorTest {

    @InjectMocks
    FTPConnector ftpConnector;

    URL url = this.getClass().getClassLoader().getResource("./media/imageFtp.png");


    @Test
    public void upload() throws Exception {
        assertNotNull(url);

        FTPClient ftpClientMock = mock(FTPClient.class);
        ftpConnector.ftpClient = ftpClientMock;
        ftpConnector.upload(Paths.get(url.toURI()).toFile());

    }

    @Test
    public void uploadInDevMode() throws Exception {
        assertNotNull(url);
        ftpConnector.mode = "dev";
        FTPClient ftpClientMock = mock(FTPClient.class);
        ftpConnector.ftpClient = ftpClientMock;
        ftpConnector.upload(Paths.get(url.toURI()).toFile());

        verifyFtpClientConnectCall(ftpClientMock,0);
    }

    private void verifyFtpClientConnectCall(FTPClient ftpClientMock, int times) throws IOException {
        verify(ftpClientMock, times(times)).connect(anyString());
    }

    @Test
    public void uploadFileNull() throws Exception {

        FTPClient ftpClientMock = mock(FTPClient.class);
        ftpConnector.ftpClient = ftpClientMock;
        ftpConnector.upload(null);
        verifyFtpClientConnectCall(ftpClientMock,0);

    }

    @Test
    public void uploadFileConnectionOKAndLoginKO() throws Exception {

        FTPClient ftpClientMock = mock(FTPClient.class);
        ftpConnector.ftpClient = ftpClientMock;
        when(ftpClientMock.login(anyString(),anyString())).thenReturn(false);

        ftpConnector.upload(Paths.get(url.toURI()).toFile());

        verifyFtpClientConnectCall(ftpClientMock,1);
        verify(ftpClientMock, times(1)).login(anyString(), anyString());
        verify(ftpClientMock, times(2)).logout();
    }

    @Test
    public void uploadFileConnectionOKAndLoginOK() throws Exception {

        FTPClient ftpClientMock = mock(FTPClient.class);
        ftpConnector.ftpClient = ftpClientMock;
        when(ftpClientMock.login(anyString(),anyString())).thenReturn(true);
        when(ftpClientMock.getReplyCode()).thenReturn(200);
        when(ftpClientMock.storeFile(any(),any())).thenReturn(true);
        File f = Paths.get(url.toURI()).toFile();

        ftpConnector.upload(f);

        verifyFtpClientConnectCall(ftpClientMock,1);
        verify(ftpClientMock, times(1)).login(anyString(), anyString());
        verify(ftpClientMock, times(1)).storeFile(contains(f.getName()),any(FileInputStream.class));
        verify(ftpClientMock, times(1)).logout();
    }

    @Test
    public void uploadFileConnectionOKAndLoginOKWithNegativeReply() throws Exception {

        FTPClient ftpClientMock = mock(FTPClient.class);
        ftpConnector.ftpClient = ftpClientMock;
        when(ftpClientMock.login(anyString(),anyString())).thenReturn(true);
        when(ftpClientMock.getReplyCode()).thenReturn(500);

        File f = Paths.get(url.toURI()).toFile();

        ftpConnector.upload(f);

        verifyFtpClientConnectCall(ftpClientMock,1);
        verify(ftpClientMock, times(1)).login(anyString(), anyString());
        verify(ftpClientMock, times(0)).storeFile(contains(f.getName()),any(FileInputStream.class));
        verify(ftpClientMock, times(1)).logout();
    }

}