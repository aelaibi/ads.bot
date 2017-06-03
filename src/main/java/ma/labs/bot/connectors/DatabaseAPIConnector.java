/**
 * Created by admin on 21/06/2016.
 */
package ma.labs.bot.connectors;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import ma.labs.bot.core.IsNewMediaResponse;
import ma.labs.bot.data.util.MediaType;
import ma.labs.bot.data.util.RobotJsonDeserializer;
import ma.labs.bot.utils.Constants;
import ma.labs.bot.utils.TimeUtils;
import ma.labs.bot.utils.Utils;
import ma.labs.bot.data.*;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.NameValuePair;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.Point;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
public class DatabaseAPIConnector {


    private static final String ACTION = "action" ;
    private static Logger logger = LoggerFactory.getLogger(DatabaseAPIConnector.class);

    @Value("${mode:dev}")
    private String mode;

    @Value("${toBefixed.enabled}")
    protected boolean isToBeFixedEnabled;

    @Value("${db.api.url}")
    private String dbAPIUrl;

    private int timeout=20;

    Gson gson = null;


    CloseableHttpClient httpclient;
    protected int retryDelay = 10000;

    public DatabaseAPIConnector(){
        RequestConfig config = RequestConfig.custom()
                .setConnectTimeout(timeout * Constants.TIMEOUT_UNIT)
                .setConnectionRequestTimeout(timeout * Constants.TIMEOUT_UNIT)
                .setSocketTimeout(timeout * Constants.TIMEOUT_UNIT).build();
        httpclient = HttpClients.custom()
                .setDefaultRequestConfig(config)
                .build();
        GsonBuilder builder = new GsonBuilder();
        builder.registerTypeAdapter(Robot.class, new RobotJsonDeserializer());
        gson = builder.create();
    }

    public Pages getPages(String country) throws IOException {
        NameValuePair nv2 = new BasicNameValuePair("country",country);
        String respJson = callAPI("getPages",nv2);
        Pages pages = gson.fromJson(respJson, Pages.class);
        return pages;
    }

    public RobotInfo getRobotInfo(String idRobot) throws IOException{

        NameValuePair nv2 = new BasicNameValuePair("idRobot",idRobot);
        String respJson = callAPI("getRobotInfo",nv2);
        RobotInfo robotInfo = gson.fromJson(respJson, RobotInfo.class);
        return robotInfo;

    }
    //GET ID OF THE MAINPAGE OF YOUTUBE
    public String getYoutubeHomePageId() {
        return callActionAndGetId("getYoutubeHomePageId");
    }
    //GET ID OF THE SIDEBAR OF YOUTUBE
    public String getYoutubeVideoPageId() {
        return callActionAndGetId("getYoutubeVideoPageId");
    }
    private String callActionAndGetId(String ction) {
        try {
            String resp = callAPI(ction);
            String r = extarctAttributeFromData(resp,"id");
            if (r != null) {
                return r;
            }
        } catch (Exception e) {
            logger.error(e.getLocalizedMessage(),e);
        }
        return null;
    }

    //GET ID OF THE MAINPAGE OF FACEBOOK
    public String getFacebookPageId() {
        return callActionAndGetId("getFacebookPageId");
    }

    public String insertExtraction(String idPage, String idRobot) throws IOException {
        String id ="0";
        NameValuePair nv1 = new BasicNameValuePair("page_id",idPage);
        NameValuePair nv2 = new BasicNameValuePair("robot_id",idRobot);
        NameValuePair nv3 = new BasicNameValuePair("status","0");
        NameValuePair nv4 = new BasicNameValuePair("start_date", TimeUtils.now());
        String respJson = callAPI("insertExtraction",nv1,nv2,nv3,nv4);
        String ret = extarctDataFromResponse(respJson);
        if(ret!= null && !"0".equals(ret)){
            return ret;
        }else{
            logger.error("Can not insert Extraction with idPage {} and idRobot {}", idPage, idRobot);
        }
        return id;
    }

    public void endExtraction(String idExtraction) throws IOException{
        NameValuePair nv1 = new BasicNameValuePair("idExtraction",idExtraction);
        NameValuePair nv2 = new BasicNameValuePair("end_date", TimeUtils.now());
        callAPI("endExtraction",nv1, nv2);
    }

    //INSERT NEW MEDIA
    public String insertNewMedia(String name, String filename, String content, String originalURL,
                                 MediaType type, Dimension size, String duration, long fileSize) {
        /*logger.debug("insert new media with values {} {} {} {} {} {} {} {}",
                name, filename, content, originalURL, type, size, duration);*/
        try {
            List<NameValuePair> params = new ArrayList<>();
            if ( MediaType.VIDEO.equals(type)) {
                   params.add(new BasicNameValuePair("name", name));
                   params.add(new BasicNameValuePair("filename", filename));
                   params.add(new BasicNameValuePair("originalurl", originalURL));
                   params.add(new BasicNameValuePair("type", type.getCode()));
                   params.add(new BasicNameValuePair("duration", duration));
            } else {
                    params.add(new BasicNameValuePair("name", name));
                    params.add(new BasicNameValuePair("filename", filename));
                    params.add(new BasicNameValuePair("type", type.getCode()));
                    params.add(new BasicNameValuePair("originalurl", originalURL));
                    params.add(new BasicNameValuePair("height", "" + size.getHeight()));
                    params.add(new BasicNameValuePair("width", "" + size.getWidth()));
                    params.add(new BasicNameValuePair("content", Utils.escapeDataString(content)));
            }
            params.add(new BasicNameValuePair("size", String.valueOf(fileSize)));
            String respJson = callAPI("insertNewMedia", params.toArray(new BasicNameValuePair[params.size()]));
            Map<String, Long> map = gson.fromJson(respJson, new TypeToken<Map<String, Long>>(){}.getType());
            if(map != null && 0 !=map.get("data")){
                return Long.toString(map.get("data"));
            }
        } catch (Exception e) {
            logger.error(e.getLocalizedMessage(),e);
        }
        return null;
    }
    //INSERT NEW Visual
    public String insertNewVisual(String country, String destinationURL,
                                  String idMedia , MediaType type, String dateCreated) {
        return insertNewVisual(country, destinationURL, idMedia, type, dateCreated, null);
    }
    //INSERT NEW Visual
    public String insertNewVisual(String country, String destinationURL,
                                  String idMedia , MediaType type, String dateCreated, String zone) {
        /*logger.debug("insert new visual with values {} {} {} {}",
                country, destinationURL, idMedia, type);*/
        try {
            List<NameValuePair> params = new ArrayList<>();
            params.add(new BasicNameValuePair("date_created" , dateCreated));
            String idDestination = null;
            if (destinationURL != null && !"".equals(destinationURL)) {
                idDestination = insertDestination(destinationURL);
            }
            //IF FB
            if (MediaType.FACEBOOK.equals(type)) {
                params.add(new BasicNameValuePair("idMedia", idMedia));
                params.add(new BasicNameValuePair("country",country));
                if(!StringUtils.isBlank(zone)){
                    params.add(new BasicNameValuePair("zone_id", zone));
                }
                if (idDestination != null)
                    params.add(new BasicNameValuePair("destination_id", idDestination));
            } else {
                params.add(new BasicNameValuePair("idMedia", idMedia));
                params.add(new BasicNameValuePair("country", country));
                if (idDestination != null)
                    params.add(new BasicNameValuePair("destination_id", idDestination));
            }
            String respJson = callAPI("insertNewVisual", params.toArray(new BasicNameValuePair[params.size()])); // return lastInsertedID
            Map<String, Long> map = gson.fromJson(respJson, new TypeToken<Map<String, Long>>(){}.getType());
            if(map != null && 0 != map.get("data")){
                return Long.toString(map.get("data"));
            }
        } catch (Exception e) {
            logger.error(e.getLocalizedMessage(),e);
        }
        return null;
    }

    //GET ID OF THE MAINPAGE OF YOUTUBE
    public VisualResponse getVisual(String checksum){
        try {
            String respJson = callAPI("getVisual", new BasicNameValuePair("checksum", checksum));
            JsonParser parser = new JsonParser();
            JsonArray visuals = parser.parse(respJson).getAsJsonObject().get("data").getAsJsonArray();
            if(visuals!=null && visuals.size()>0){
                VisualResponse visualResponse = new VisualResponse();
                visualResponse.setChecksum(checksum);
                visualResponse.setIdMedia(visuals.get(0).getAsJsonObject().get("idMedia").getAsString());

                JsonElement toBeFixed = visuals.get(0).getAsJsonObject().get("toBeFixed");
                visualResponse.setToBeFixed((toBeFixed != null && !toBeFixed.isJsonNull()) ? toBeFixed.getAsString() : "0");
                visualResponse.setVisuals(new ArrayList<>());
                for(JsonElement vi: visuals){
                    String country     = vi.getAsJsonObject().get("country").getAsString();
                    String idVisual    = vi.getAsJsonObject().get("idVisual").getAsString();
                    JsonElement zoneElement = vi.getAsJsonObject().get("idZone");
                    String idZone      =  (zoneElement!= null && !zoneElement.isJsonNull()) ? vi.getAsJsonObject().get("idZone").getAsString() : null;
                    Visual visual      = new Visual(country, idZone, idVisual);
                    visualResponse.getVisuals().add(visual);
                }
                return visualResponse;
            }
        } catch (Exception e) {
            logger.error(e.getLocalizedMessage(),e);
        }
        return null;
    }

    //INSERT DESTINATION
    public String insertDestination(String url) {
        try {
            String pathScreenshot ="";
            String respJson = callAPI("getIdDestination", new BasicNameValuePair("url", url));
            String id = extarctAttributeFromData(respJson,"ID");
            if(id != null){
                return id;
            }
            String localType = "1";
            // If new destination
            if (url.contains("facebook.com") || url.contains("fb.com")) {
                localType = "3";
            } else if (url.contains("youtube.com") || url.contains("youtu.be")) {
                localType = "2";
            } else if (url.startsWith("mailto:")) {
                localType = "4";
            } else {
                localType = "1";
            }
            List<NameValuePair> params = new ArrayList<>();
            params.add(new BasicNameValuePair("type", localType));
            params.add(new BasicNameValuePair("url", url));
            params.add(new BasicNameValuePair("screenshotpath", pathScreenshot));
            String resp =callAPI("insertDestination", params.toArray(new BasicNameValuePair[params.size()]));
            return extarctDataFromResponse(resp);
        } catch (Exception e) {
            logger.error(e.getLocalizedMessage(),e);
        }
        return null;
    }

    //INSERT IMPRESSION
    public void insertImpression(Robot robot, String idExtraction, MediaType type /* of media */,
                                 String content, AdNetwork network, String filename, String originalURL,
                                 String destinationURL, String id_zone, String id_page,
                                 Dimension sizeScreen, Dimension size, Point location, String duration, IsNewMediaResponse isNew, long fileSize) {
        if(Utils.isDevMode(mode)){
            try {
                String fileContent = "robot: " + robot +"\n"
                        + "idExtraction : " + idExtraction +"\n"
                        + "type : " + type +"\n"
                        + "content : " + content +"\n"
                        + "network : " + network +"\n"
                        + "filename : " + filename +"\n"
                        + "originalURL : " + originalURL +"\n"
                        + "destinationURL : " + destinationURL +"\n"
                        + "id_zone : " + id_zone +"\n"
                        + "id_page : " + id_page +"\n"
                        + "sizeScreen : " + sizeScreen +"\n"
                        + "size : " + size +"\n"
                        + "location : " + location +"\n"
                        + "duration : " + duration +"\n"
                        + "isNew : " + isNew +"\n"
                        + "fileSize : " + fileSize +"\n";
                try (BufferedWriter writer = Files.newBufferedWriter(Paths.get("media",filename+".txt"), StandardOpenOption.CREATE)) {
                    writer.write(fileContent);
                }
            }catch (Exception exception){}

        }
        try {
            if (originalURL.contains("TemplateContainer")
                    || originalURL.contains("favico")
                    || (originalURL.contains("mqdefault.jpg") && originalURL.contains("ytimg.com")))
                return; //if google template or favico or youtube thumbnail
            String now = TimeUtils.now();

            String idMedia, idVisual = "";
            if (isNew.isNewMedia() ||  isNew.isNewVisual()) {//new media or new visual
                //IF NEW MEDIA, THEN NEW Visual ALSO
                if (isNew.isNewMedia()) {
                    idMedia = insertNewMedia("", filename, content, originalURL, type, size, duration,fileSize);
                    if(MediaType.FACEBOOK.equals(type)){
                        idVisual = insertNewVisual(robot.getCountry(), destinationURL, idMedia, type, now, id_zone);
                    } else {
                        idVisual = insertNewVisual(robot.getCountry(), destinationURL, idMedia, type, now);
                    }
                } else {//IF NOT NEW MEDIA, AND NEW Visual
                    idMedia = isNew.getIdMedia();
                    if(MediaType.FACEBOOK.equals(type)){
                        idVisual = insertNewVisual(robot.getCountry(), destinationURL, idMedia, type, now, id_zone);
                    } else {
                        idVisual = insertNewVisual(robot.getCountry(), destinationURL, idMedia, type, now);
                    }
                }
            } else {//existing media and existing visual
                idVisual = isNew.getIdVisual();
            }
            if(isNew.isToBeFixed() && isToBeFixedEnabled){
                callAPI("updateToBeFixed",
                        new BasicNameValuePair[]{ new BasicNameValuePair("media_id", isNew.getIdMedia()) });
            }

            List<NameValuePair> params = new ArrayList<>();

            params.add(new BasicNameValuePair("extraction_id", idExtraction));
            params.add(new BasicNameValuePair("page_id", id_page));
            params.add(new BasicNameValuePair("network", network.getCode()));
            params.add(new BasicNameValuePair("x", ""+location.getX()));
            params.add(new BasicNameValuePair("y", ""+location.getY()));
            params.add(new BasicNameValuePair("country", robot.getCountry()));
            params.add(new BasicNameValuePair("profile_id", Integer.toString(robot.getProfileID())));
            params.add(new BasicNameValuePair("device_id",  Integer.toString(robot.getDeviceId())));
            params.add(new BasicNameValuePair("visual_id" , idVisual ));
            params.add(new BasicNameValuePair("screenWidth" , Integer.toString(sizeScreen.getWidth())));
            params.add(new BasicNameValuePair("screenHeight" , Integer.toString(sizeScreen.getHeight())));
            params.add(new BasicNameValuePair("date_created" , now));

            if (MediaType.FACEBOOK.equals(type)) {
                params.add(new BasicNameValuePair("zone_id", id_zone));
            }
            callAPI("insertImpression", params.toArray(new BasicNameValuePair[params.size()]));
        } catch (Exception e) {
            logger.error(e.getLocalizedMessage(),e);
        }
    }


    public String callAPI(String action, NameValuePair ...params)  throws IOException{
        return  callAPI(action, true, params);
    }
    private String callAPI(String action, boolean retry, NameValuePair ...params)  throws IOException{
        logger.info("calling DB API action {} with params {}", action, params);
        String respJson;
        NameValuePair nvAction = new BasicNameValuePair(ACTION,action);
        HttpUriRequest request = RequestBuilder.post()
                .setUri(dbAPIUrl)
                .addHeader("Content-type", "application/x-www-form-urlencoded")
                .addParameter(nvAction)
                .addParameters(params)
                .build();
        CloseableHttpResponse response =null;
        try {
            response = httpclient.execute(request);
            if(response.getStatusLine().getStatusCode() == 200){
                respJson = EntityUtils.toString(response.getEntity(),"UTF-8");
                if(extarctCodeFromResponse(respJson)!=1){
                    throw new Exception("Calling API got resp "+respJson);
                }
                if(!"getPages".equals(action))// getPages has a big response
                    logger.info("{} got response {} ", action, respJson);
                EntityUtils.consume(response.getEntity());
                return respJson;
            }
            throw new Exception("Calling API got code "+response.getStatusLine().getStatusCode());
        }catch (Exception e){
            if(retry){
                logger.error("calling DB API action {} got error : {}",action, e.getMessage());
                TimeUtils.waitFor(retryDelay);
                return callAPI(action,false, params);
            }
        }
        finally {
            if(response!=null)
                response.close();
        }
        return null;
    }

    private String extarctAttributeFromData(String respJson, String attr){
        try {
            JsonParser parser = new JsonParser();

            if(parser.parse(respJson) != null
                    && null !=parser.parse(respJson).getAsJsonObject().get("data")
                    && null !=parser.parse(respJson).getAsJsonObject().get("data").getAsJsonObject().get(attr)){
                return parser.parse(respJson).getAsJsonObject().get("data").getAsJsonObject().get(attr).getAsString();
            }
        }catch (Exception e){
        }
        return null;
    }
    private String extarctDataFromResponse(String respJson){
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

    private int extarctCodeFromResponse(String respJson){
        try {
            JsonParser parser = new JsonParser();

            if(parser.parse(respJson) != null
                    && null !=parser.parse(respJson).getAsJsonObject().get("code")){
                return parser.parse(respJson).getAsJsonObject().get("code").getAsInt();
            }
        }catch (Exception e){
        }
        return 0;
    }

}
