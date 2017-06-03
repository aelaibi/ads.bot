package ma.labs.bot.rules;
/********************************************************
 *                                                      *
 *                    PIXITREND ROBOT                   *
 *                    BY labs GROUP                    *
 *                                                      *
 ********************************************************/

import com.google.gson.JsonParser;
import org.apache.commons.lang3.StringUtils;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by labs004 on 21/07/2016.
 */
public class RegExService {


    public static int countMatches(String regex, String input) {
        Matcher matcher = Pattern.compile(regex).matcher(input);
        int count = 0;
        while(matcher.find()) {
            count++;
        }
        return count;
    }
    public static List<String> getMatches(String regex, String input, int index) {
        Matcher matcher = Pattern.compile(regex).matcher(input);
        List<String> out = new ArrayList<>();
        while(matcher.find()) {
            out.add(matcher.group(index));
        }
        return out;
    }

    public static long getDurationInSecondes(String line){
        String found = findFirst("[D|d]uration:.((\\d|:|\\.)*)",line,false,1);
        if(!"".equals(found)){
            String[] timepieces = found.split(":|\\.");
            if (timepieces.length == 4){
                return Duration
                        .ofHours(Integer.valueOf(timepieces[0]))
                        .plusMinutes(Integer.valueOf(timepieces[1])).plusSeconds(Integer.valueOf(timepieces[2]))
                        .getSeconds();
            }
        }
        return -1;
    }
    private static String deleteRegex(String input, String regex){
        final String match = RegExService.findFirst(regex, input, true, 0);
        if(StringUtils.isBlank(match)){
            return input;
        }
        return input.replace(match, "");
    }
    public static String cleanFbAdFromLikes(String input){
        if(StringUtils.isBlank(input)){
            return input;
        }
        String out = deleteRegex(input, "\\n(\\d|\\s)*.*(aime|like).*\\.");
        out = deleteRegex(out, "^(\\w|\\s)*.*?(aime|like).*?\\.\\n");
        return out;
    }
    public static String cleanRightFbAdFromLikes(String input){
        if(StringUtils.isBlank(input)){
            return input;
        }
        String out = deleteRegex(input, "\\n(\\d|\\s)*.*(aime|like).*");
        out = deleteRegex(out, "\\n(\\d|\\s)*$");
        return out;
    }
    public static String findFirst(String regex, String input, boolean ignoreCase, int groupIndex) {
        Matcher matcher = Pattern.compile(regex,ignoreCase?Pattern.CASE_INSENSITIVE:0)
                .matcher(input);
        if (matcher.find()){//find first one
            return matcher.group(groupIndex);
        }
        return "";
    }

    public static String getManifestFromData(String respJson){
        try {
            JsonParser parser = new JsonParser();

            return parser.parse(respJson).getAsJsonObject()
                    .get("data").getAsJsonArray().get(1).getAsJsonObject()//indice 1 à monitorer
                    .get("properties").getAsJsonObject()
                    .get("manifest").toString();
        }catch (Exception e){
            return findFirst("\"manifest\":(\\[.*\"}\\])",respJson,false,1);
        }
    }

    public static String getGADestinationUrl(String input) {
        return findFirst("( |\"|')destination_url( |\"|'):( |\"|')(.*?)( |\"|')",input,false,4);
    }

    public static String getYoutubeIdFromUrl(String url) {
        //^(?:https?\:\/\/)?(?:www\.)?(?:youtu\.be\/|youtube\.com\/(?:embed\/|v\/|watch\?v\=))([\w-]{10,12})(?:$|\&|\?\#).*
        String vId = null;
        String reg = ".*(?:youtu.be\\/|v\\/|u\\/\\w\\/|embed\\/|e\\/|watch\\?v=|watch\\?.*v=)([^#&\\?]*).*";
        Pattern pattern = Pattern.compile(
                reg,
                Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(url);
        if (matcher.matches()){
            vId = matcher.group(1);
        }
        return vId;
    }
    public static String getAdUrlFromClickTag(String input){
        return findFirst("(.*)adurl(=|%3D)(.*)",input,false,3);
    }

    public static boolean isAdsenseHtml(String html){
        if(html==null)
            return false;
        return (html.contains("adsense/support")
                || html.contains("adsense/troubleshooter")
                || html.contains("gamned.com/en/privacy/")
                || html.contains("youradchoices.com")
                || html.contains("choices.truste.com/")
                || html.contains("adsrvr.org")
                || html.contains("youronlinechoices.com")
                || html.contains("criteo.com/privacy")
                || html.contains("aboutads.info")
                || html.contains("preferences-mgr.truste.com")
                || html.contains("info.evidon.com/more_info/")
                || html.contains("mediamath.com/privacy-policy/")
                || html.contains("adform.net")//jira 472
                || html.contains("via AppNexus")//JIRA 347
        );
    }

    public static boolean isAdsenseOriginalUrl(String originalURL) {
       return (originalURL!=null
               && (originalURL.contains("rubiconproject.com")
               || originalURL.contains("adnxs.com") //JIRA 347
               ||originalURL.contains("disqusads.com") //JIRA 341
               ||originalURL.contains("criteo.com") //JIRA 354
               || originalURL.contains("adroll.com") //JIRA 351
               || originalURL.contains("adform.net")//jira 472
               || originalURL.toLowerCase().contains("mediamath")//jira 471
                )
       );
   }

    public static boolean isAdsenseDestinationUrl(String urlDestination) {
        return  (urlDestination!=null
                && (urlDestination.contains("affiliaxe.com")
                || urlDestination.contains("adnxs.com"))//JIRA 347
        );
    }
}
