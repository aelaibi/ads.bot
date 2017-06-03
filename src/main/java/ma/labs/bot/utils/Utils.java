package ma.labs.bot.utils;
/********************************************************
 *                                                      *
 *                    PIXITREND ROBOT                   *
 *                    BY labs GROUP                    *
 *                                                      *
 ********************************************************/

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.UnsupportedEncodingException;
import java.net.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Created by labs004 on 18/07/2016.
 */
public class Utils {

    private static final Logger logger = LoggerFactory.getLogger(Utils.class);

    public static int getRandom(int min, int max){
        Random random = new Random();
        return random.nextInt(max - min) + min;

    }

    public static String unescapeDataString(String href) throws UnsupportedEncodingException {
        return URLDecoder.decode(href, "UTF-8");
    }

    public static String escapeDataString(String href) throws UnsupportedEncodingException {
        return URLEncoder.encode(href, "UTF-8");
    }

    public static boolean checkValidURI(String urlStr) {
        final URI u;
        try {
            u = URI.create(urlStr);
        }catch (IllegalArgumentException e){
            return false;
        }
        return  "http".equals(u.getScheme());
    }
    public static String gatValidUriString(String urlStr) {
        try {
            URL url = new URL(urlStr);
            URI uri = new URI(url.getProtocol(), url.getUserInfo(),
                    url.getHost(), url.getPort(), url.getPath(), url.getQuery(), url.getRef());
            urlStr =uri.toURL().toString();
        }catch (Exception e){}
        return urlStr;
    }

    public static Map<String, String> parseQueryString(String hrefDestination) {
        try {
            logger.debug("parsing url {}",hrefDestination);
            URL url = new URL(hrefDestination);
            return splitQuery(url);
        } catch (MalformedURLException e) {
            logger.error(e.getMessage());
        }


        return Collections.emptyMap();
    }
     private static Pair<String, String> splitQueryParameter(String it) {
        final int idx = it.indexOf("=");
        final String key = idx > 0 ? it.substring(0, idx) : it;
        final String value = idx > 0 && it.length() > idx + 1 ? it.substring(idx + 1) : "";
        return new ImmutablePair<>(key, value) ;
    }
    private static Map<String, String> splitQuery(URL url) {
        if (url.getQuery()==null || url.getQuery().trim().length()==0) {
            return Collections.emptyMap();
        }
        return Arrays.stream(url.getQuery().split("&"))
                .map(e -> splitQueryParameter(e))
                .collect(Collectors.toMap(Pair::getLeft,Pair::getRight));
    }

    public static String getGroupValue(Matcher matcher, int index) {
        return matcher.group(index)!=null?matcher.group(index):"";
    }

    public static List<String> convertJSONtoArray(String firstIndex, String jsonStr) {

        JsonParser parser = new JsonParser();
        JsonArray array = parser.parse(jsonStr).getAsJsonObject().get(firstIndex).getAsJsonArray();
        List<String> out = new ArrayList<>();
        for (JsonElement e: array){
            out.add(e.getAsString());
        }
        return out;
    }




    //Default 0
    public static String getYoutubeDurationInSecs(String tmp) {
        if(tmp == null || "".equals(tmp))
            return "0";
        try {
            String[] parts = tmp.split(":");
            int d =0;
            if(parts.length==2){
                d = (Integer.valueOf(parts[0])*60) + Integer.valueOf(parts[1]);
            }
            if(parts.length==3){
                d = (Integer.valueOf(parts[0])*3600)+ (Integer.valueOf(parts[1])*60) + Integer.valueOf(parts[2]);
            }
            return String.valueOf(d+1); // we add one (1) to duration because youtube starts from 0, and so, the complete duration is the length+1
        }catch (Exception e){
            return "0";
        }

    }
    public static String replaceLast(String string, String toReplace, String replacement) {
        int pos = string.lastIndexOf(toReplace);
        if (pos > -1) {
            return string.substring(0, pos)
                    + replacement
                    + string.substring(pos + toReplace.length(), string.length());
        } else {
            return string;
        }
    }
    public static String replaceFirst(String input, CharSequence target, CharSequence replacement) {
        return Pattern.compile(target.toString(), Pattern.LITERAL).matcher(
                input).replaceFirst(Matcher.quoteReplacement(replacement.toString()));
    }

    public static boolean isProdMode(String mode) {
        return ("prod".equals(mode) || "production".equals(mode));
    }
    public static boolean isDevMode(String mode) {
        return ("dev".equals(mode) || "development".equals(mode));
    }

    public static boolean isDestinationUrlcontainsGoogleCom(String destUrl){
        String google = "google.com";
        if(destUrl==null || destUrl.trim().equals(""))
            return false;
        if(destUrl.length()>30){
            return destUrl.substring(0,30).contains(google);
        }else{
            return destUrl.contains(google);
        }
    }

    public  static String sort(String in){
        if(in ==null)return in;
        return in.chars()
                .sorted()
                .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
                .toString();
    }

    public  static String removeWhitSpacesTabsNewLines(String in){
        if(in ==null)return in;
        return in.replaceAll("\t", "").replaceAll("\n", "").replaceAll(" ", "");
    }
}