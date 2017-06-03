/**
 * Created by admin on 21/06/2016.
 */
package ma.labs.bot.connectors;
import com.google.gson.Gson;
import com.google.gson.JsonParser;
import ma.labs.bot.core.MediaHelperAPIResponse;
import ma.labs.bot.utils.Constants;
import ma.labs.bot.utils.Utils;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.io.IOException;


@Component
public class MediaHelperAPIConnector {
    private static Logger logger = LoggerFactory.getLogger(MediaHelperAPIConnector.class);

    @Value("${mode:dev}")
    private String mode;
    private static final long DELAY = 10000;

    private static final String ACTION = "action";
    @Value("${mediahelper.url}")
    private String apiUrl;

    private int timeout = 100;
    private Gson gson;

    private final CloseableHttpClient httpclient;

    public MediaHelperAPIConnector(){
        RequestConfig config = RequestConfig.custom()
                .setConnectTimeout(timeout * Constants.TIMEOUT_UNIT)
                .setConnectionRequestTimeout(timeout * Constants.TIMEOUT_UNIT)
                .setSocketTimeout(timeout * Constants.TIMEOUT_UNIT).build();
        httpclient = HttpClients.custom()
                .setDefaultRequestConfig(config)
                .build();
        gson = new Gson();
    }

    @Retryable(value = {IOException.class}, maxAttempts = 3, backoff = @Backoff(delay = DELAY))
    public MediaHelperAPIResponse downloadByFileUrl(String fileUrl) throws IOException {
        return this.download(fileUrl, null);
    }
    @Retryable(value = {IOException.class}, maxAttempts = 3, backoff = @Backoff(delay = DELAY))
    public MediaHelperAPIResponse downloadByYoutubeId(String youtubeId) throws IOException {
        return this.download(null, youtubeId);
    }

    private MediaHelperAPIResponse download(String fileUrl, String youtubeId) throws IOException{
        final CloseableHttpResponse response = this.callAPI("download", fileUrl, youtubeId);
        if(isAValidResponseByCode(response)){
            try {
                MediaHelperAPIResponse ret = formatResponse(response);
                if(ret!=null && 600<ret.getDuration()){//skip big videos
                    logger.debug("video duration ({}) is > 600 sec", ret.getDuration());
                    ret = null;
                }
                return ret;
            } catch (IOException e) {
                logger.debug("during formatResponse, details : {}", e);
                throw e;
            }
        }
        if(null != response){
            try {
                response.close();
                logger.debug("response closed");
            } catch (IOException e) {
                logger.error("cannot close response, error : {}", e);
                throw e;
            }
        }
        return null;
    }
    @Async
    public void uploadByFileUrl(String fileUrl){

        try {
            this.upload(fileUrl, null);
        } catch (IOException e) {
            logger.error("failed to download file, neted message is : {}", e);
        }
    }
    @Async
    public void uploadByYoutubeID(String youtubeId){
        try {
            this.upload(null, youtubeId);
        } catch (IOException e) {
            logger.error("failed to download file, neted message is : {}", e);
        }
    }
    private boolean upload(String fileUrl, String youtubeId) throws IOException {
        if(Utils.isDevMode(mode)){
            logger.debug("Trying to upload {} or {} via YOUTUBE API", fileUrl, youtubeId);
            return true;
        }
        final CloseableHttpResponse response = this.callAPI("upload", fileUrl, youtubeId);
        final boolean retour = this.isAValidResponseByCode(response);
        if(null != response){
            try {
                response.close();
                logger.debug("response closed");
            } catch (IOException e) {
                logger.error("cannot close response, error : {}", e);
                throw e;
            }
        }
        return retour;
    }

    private CloseableHttpResponse callAPI(String action, String fileUrl, String youtubeId) throws IOException {
        logger.info("calling media helper API with action '{}', fileurl : {}, youtubeId : {}", action, fileUrl, youtubeId);
        final HttpUriRequest request = buildRequest(action, fileUrl, youtubeId);
        if(null == request){
            logger.error("fileurl and youtubeid are both not null or both null");
            return null;
        }
        try {
            return httpclient.execute(request);
        } catch (IOException e) {
            logger.error("during callAPI :  httpclient.execute, got : {}", e);
            throw e;
        }
    }
    private HttpUriRequest buildRequest(String action, String fileUrl, String youtubeId) {
        BasicNameValuePair[] paramArray = new BasicNameValuePair[3];
        paramArray[0] = new BasicNameValuePair(ACTION, action);
        paramArray[1] = new BasicNameValuePair("url", fileUrl);
        paramArray[2] = new BasicNameValuePair("id", youtubeId);
        return RequestBuilder
                .post()
                .setUri(apiUrl)
                .addParameters(paramArray)
                .addHeader("Content-Type", "application/x-www-form-urlencoded")
                .build();
    }
    private String extractAttributeFromData(String respJson, String attr){
        try {
            JsonParser parser = new JsonParser();

            if(parser.parse(respJson) != null
                    && null !=parser.parse(respJson).getAsJsonObject().get(attr)){
                return parser.parse(respJson).getAsJsonObject().get(attr).getAsString();
            }
        }catch (Exception e){
        }
        return null;
    }
    private String extractDataFromResponse(String respJson){
        try {
            JsonParser parser = new JsonParser();

            if(parser.parse(respJson) != null
                    && null !=parser.parse(respJson).getAsJsonObject().get("data")){
                return parser.parse(respJson).getAsJsonObject().get("data").getAsString();
            }
        }catch (Exception e){
        }
        return null;
    }
    private MediaHelperAPIResponse formatResponse(CloseableHttpResponse response) throws IOException {
        final String stringResponse = EntityUtils.toString(response.getEntity());
        if(null == stringResponse){ return null;}
        final String dataStringResponse = this.extractDataFromResponse(stringResponse);
        if(null == dataStringResponse){ return null;}
        final String code = this.extractAttributeFromData(stringResponse, "code");
        if(!"1".equals(code)){
            throw new IOException("MediaAPIConnector returned codeAttribute <> 1.");
        }
        logger.info("got : {}", stringResponse);
        MediaHelperAPIResponse out = gson.fromJson(dataStringResponse, MediaHelperAPIResponse.class);
        if(out.isEmptyChecksum() ||0 == out.getDuration()){
            return null;
        }
        return out;
    }
    private boolean isAValidResponseByCode(CloseableHttpResponse response){
        return null != response && 200 == response.getStatusLine().getStatusCode();
    }
}
