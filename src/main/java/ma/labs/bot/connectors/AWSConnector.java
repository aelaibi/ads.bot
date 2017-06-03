package ma.labs.bot.connectors;
/********************************************************
 *                                                      *
 *                    PIXITREND ROBOT                   *
 *                    BY labs GROUP                    *
 *                                                      *
 ********************************************************/

import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.PutObjectResult;
import ma.labs.bot.data.util.MediaType;
import ma.labs.bot.rules.FileManagerService;
import ma.labs.bot.utils.TimeUtils;
import ma.labs.bot.utils.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.File;

/**
 * Created by labs004 on 20/07/2016.
 */
@Component
public class AWSConnector {
    private static Logger logger = LoggerFactory.getLogger(AWSConnector.class);

    String VISUAL_BUCKET = "pixitrend-prd";

    @Value("${aws.access.key}") String accessKey;
    @Value("${aws.secret.key}") String secretKey;



    @Value("${mode:dev}")
     String mode;

    @Autowired
    FileManagerService fileManagerService;
    @Autowired
    FTPConnector ftpConnector;




    @PostConstruct
    void init(){
        //init types
    }

    /**
     * upload from MEDIA FOLDER : @"media"
     * @param filename
     * @param type
     * @param retry
     */
    @Async
    public void upload(String filename, MediaType type, boolean retry){
        if(null == filename){
            logger.error("the filename is null");
            return;
        }
        if(Utils.isDevMode(mode)){
            logger.info("I'm a mock, the real connector will upload {} to s3", filename);
            return;
        }
        logger.info("I'm  uploading {} to s3", filename);
        //if(type_ == null)
            //type_ = "binary/octet-stream";
        try {
            AmazonS3 s3client = new AmazonS3Client(new BasicAWSCredentials(accessKey, secretKey));
            s3client.setRegion(Region.getRegion(Regions.US_EAST_1));
            File file = fileManagerService.getFileFromMedia(filename);
            if(!file.exists()){
                logger.error("file {} does not exist", filename);
                return;
            }

            ftpConnector.upload(file);

            PutObjectRequest putObjectRequest = new PutObjectRequest(VISUAL_BUCKET, filename, file);
            putObjectRequest.setCannedAcl(CannedAccessControlList.PublicRead);

            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentType(type.getMimeType());
            putObjectRequest.setMetadata(metadata);

            PutObjectResult ret = s3client.putObject(putObjectRequest);
            logger.info("uploading {} to s3 with success", filename);
        } catch (Exception e) {
            logger.error("upload to s3 error ",e);
            if (retry) {
                logger.info("retry upload to s3 ");
                TimeUtils.waitFor(5000);
                upload(filename, type, false);
            }
        }
    }

}
