package ma.labs.bot.connectors;
/********************************************************
 *                                                      *
 *                    PIXITREND ROBOT                   *
 *                    BY labs GROUP                    *
 *                                                      *
 ********************************************************/

import ma.labs.bot.utils.Utils;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPReply;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileInputStream;

/**
 * Created by labs004 on 20/07/2016.
 */
@Component
public class FTPConnector {
    private static Logger logger = LoggerFactory.getLogger(FTPConnector.class);

    FTPClient ftpClient = new FTPClient();

    @Value("${ftp.host}")
    private String sftpHost;
    @Value("${ftp.port}")
    private int sftpPort;
    @Value("${ftp.user}")
    private String sftpUser;
    @Value("${ftp.pass}")
    private String sftpPass;
    @Value("${ftp.remote.folder}")
    private String sftpRemoteFolder;

    @Value("${mode:dev}")
     String mode;


    @Async
    public void upload(File file)  {

        if(Utils.isDevMode(mode)){
            logger.info("I'm a mock, the real connector will upload {} to FTP", file);
            return;
        }
        if(file==null){
            logger.error("I can not upload null to FTP !");
            return;
        }
        //FileInputStream fis=null;
        try (FileInputStream fis = new FileInputStream(file)) {
            logger.debug("trying to upload {} to FTP", file.getName());

            ftpClient.connect(sftpHost);
            if(!ftpClient.login(sftpUser, sftpPass)) {
                ftpClient.logout();
                return ;
            }
            int reply = ftpClient.getReplyCode();
            if (!FTPReply.isPositiveCompletion(reply)) {
                throw new Exception("FTP Reply is negative !");
            }
            ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
            ftpClient.enterLocalPassiveMode();
            ftpClient.changeWorkingDirectory(sftpRemoteFolder);
            if (ftpClient.storeFile(file.getName(), fis)) {
                logger.debug("{} is uploaded successfully.",file.getName());
            }


        } catch (Exception e) {
            logger.error(e.getMessage());
        }finally {
            try{
                if(ftpClient != null){
                    ftpClient.logout();
                    ftpClient.disconnect();
                }
            }catch (Exception ex){
                logger.error(ex.getMessage());
            }

        }

    }
}
