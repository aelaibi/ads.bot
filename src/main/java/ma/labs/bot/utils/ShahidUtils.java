package ma.labs.bot.utils;

import ma.labs.bot.data.AdNetwork;
import ma.labs.bot.data.Page;
import ma.labs.bot.rules.RegExService;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Mohamed on 23/02/2017.
 */
public abstract class ShahidUtils {
    /**
     * private constructor (sonar)
     */
    private ShahidUtils(){}

    private static Logger logger = LoggerFactory.getLogger(ShahidUtils.class);

    public static final String SHAHID_PREROLL_ADS_URL = "https://pubads.g.doubleclick.net/gampad/ads?ad_rule=0&adk=4272121948&ciu_szs=234x60&cmsid=4511&dt={0}&env=vp&flash=24.0.0.221&frm=0&gdfp_req=1&ged=ve4_td5_tt2_pd5_la1000_er85.312.625.1272_vi0.0.296.1583_vp39_ts1_eb23147&hl=ar&impl=s&kfa=0&max_ad_duration=90000&min_ad_duration=0&osd=6&output=xml_vast2&pod=1&ppos=1&sdki=d&sdkv=3.254.3&sdr=1&slotname=%2F7229%2FMBCShahed&sz=1280x720&tfcd=0&u_ah=860&u_asa=1&u_aw=1600&u_cd=24&u_h=900&u_his=4&u_java=false&u_nmime=7&u_nplug=5&u_tz=60&u_w=1600&unviewed_position_start=1&vad_type=linear&video_doc_id={1}&vpos=preroll&url={2}";

    public static boolean isShahidPage(Page page){
        return (page != null && !StringUtils.isBlank(page.getUrl()) ) ? page.getUrl().contains("shahid.mbc.net") : false;
    }

    public static String removeCDataFromInput(String input){
        if(StringUtils.isBlank(input) || (!input.startsWith("<![CDATA[") || !input.endsWith("]]>"))){
            return input;
        } else {
            return RegExService.findFirst("(?s)<!\\[CDATA\\[(.*?)]]>", input, true, 1);
        }
    }
    public static String getAdSystem(final String ad){
        return removeCDataFromInput(RegExService.findFirst("(?s)<AdSystem>(.*?)<\\/AdSystem>", ad, true, 1));
    }
    public static List<String> getAdMp4MediaFiles(final String ad){
        if(StringUtils.isBlank(ad)){
            return new ArrayList<>();
        }
        return RegExService.getMatches("(?s)<MediaFile.*?type=\"video/mp4\".*?<!\\[CDATA\\[(.*?)]]>.*?<\\/MediaFile>", ad, 1);
    }
    public static String getAdDestination(final String ad){
        return RegExService.findFirst("(?s)<ClickThrough>.*?<!\\[CDATA\\[(.*?)]]>.*?<\\/ClickThrough>", ad, true, 1).trim();
    }
    public static String getAdDefinitionUrl(final String ad){
        return RegExService.findFirst("(?s)<VASTAdTagURI>.*?<!\\[CDATA\\[(.*?)\\]\\]>.*?<\\/VASTAdTagURI>", ad, true, 1).trim();
    }
    public static boolean resultContainsAds(final String result){
        return StringUtils.isBlank(result) ? false : result.contains("<Ad id=\"");
    }
    public static List<String> getAdsFromResult(final String result){
        return RegExService.getMatches("(?s)<Ad id=\"(.*?)<\\/Ad>", result, 0);
    }
    public static AdNetwork getAdNetwork(String idExtraction, final String adSystem){
        if(StringUtils.isBlank(adSystem)){
            logger.warn("shahid ad network could not be detected, (will return DIRECT), because ad system '{}' is blank. extraction is {}", adSystem, idExtraction);
            return AdNetwork.DIRECT;
        }
        return StringUtils.containsAny(adSystem.toLowerCase(), "adsence", "adnxs") ? AdNetwork.ADSENSE : AdNetwork.DIRECT;
    }
}
