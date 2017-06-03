package ma.labs.bot.rules;

import org.junit.Assert;
import org.junit.Test;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertFalse;
import static junit.framework.TestCase.assertTrue;

/********************************************************
 *                                                      *
 *                    PIXITREND ROBOT                   *
 *                    BY labs GROUP                    *
 *                                                      *
 ********************************************************/

/**
 * Created by labs004 on 29/07/2016.
 */
public class RegExServiceTest {
    @Test
    public void getYoutubeIdFromUrl() throws Exception {
        //String id = RegExService.getYoutubeIdFromUrl("http://www.youtube.com/embed/Woq5iX9XQhA?html5=1");
        String id ;
        id = RegExService.getYoutubeIdFromUrl("http://www.youtube.com/watch?v=0zM4nApSvMg&feature=feedrec_grec_index");
        Assert.assertEquals("0zM4nApSvMg", id);

        id = RegExService.getYoutubeIdFromUrl("http://www.youtube.com/embed/Woq5iX9XQhA?html5=1");
        Assert.assertEquals("Woq5iX9XQhA", id);

        id = RegExService.getYoutubeIdFromUrl("http://youtu.be/0zM4nApSvMg");
        Assert.assertEquals("0zM4nApSvMg", id);

        id = RegExService.getYoutubeIdFromUrl("http://www.youtube.com/v/384IUU43bfQ?fs=1&amp;hl=en_US&amp;rel=0");
        Assert.assertEquals("384IUU43bfQ", id);

        id = RegExService.getYoutubeIdFromUrl("https://www.youtube.com/v/884IUU43bfQ?fs=1&amp;hl=en_US&amp;rel=0");
        Assert.assertEquals("884IUU43bfQ", id);
    }

    @Test
    public void testgetGADestinationUrl(){
        String input = "{\"google_width\":160,\"google_height\":600,\"google_click_url\":\"https://www.googleadservices.com/pagead/aclk?sa=L&ai=C1bWdvJ61V76lE7TgzAbjupnoDKX-9ZtG9YK9uuYBrf2xkO0CEAEgsMPUFGD5u--E5C-gAaO6x8QDyAEJqQK6UaVbL_yyPuACAKgDAaoEvwFP0LpoeNhacYMpLRFdYWrzTrh0haQsgyjvcnNJQBJENyRn972MItZy0M1ZIChJFtw2F-AV2PwLDVdxwrsFIBwsxiIJXyzuJo1Hj5_Y5I9e4wuNLWFhYS6rMMwY7Dj2UNG-yWy4hHVdutMvrJeBw0UNCifJK8h54016Vu0cspLaYO4KYmsvDLy11j_4-DPChNJI0RYtHEOHIEh3_LImqTWWfgn6Bl17MaaGG5HGAJzYcUv8ByYd_DZiq-wnArdB6eAEAYgGAaAGLoAHxcW4O6gHgcYbqAemvhvYBwDYEww&num=1&cid=CAASEuRo3n0TcE6tBqwEkiJ9ISK--Q&sig=AOD64_1-XjUl3YqIGZwcSmo1TMa-9bYUHw&client=ca-pub-2996542930718127&adurl=\",\"google_ait_url\":\"https://googleads.g.doubleclick.net/pagead/conversion/?ai=C1bWdvJ61V76lE7TgzAbjupnoDKX-9ZtG9YK9uuYBrf2xkO0CEAEgsMPUFGD5u--E5C-gAaO6x8QDyAEJqQK6UaVbL_yyPuACAKgDAaoEvwFP0LpoeNhacYMpLRFdYWrzTrh0haQsgyjvcnNJQBJENyRn972MItZy0M1ZIChJFtw2F-AV2PwLDVdxwrsFIBwsxiIJXyzuJo1Hj5_Y5I9e4wuNLWFhYS6rMMwY7Dj2UNG-yWy4hHVdutMvrJeBw0UNCifJK8h54016Vu0cspLaYO4KYmsvDLy11j_4-DPChNJI0RYtHEOHIEh3_LImqTWWfgn6Bl17MaaGG5HGAJzYcUv8ByYd_DZiq-wnArdB6eAEAYgGAaAGLoAHxcW4O6gHgcYbqAemvhvYBwDYEww&sigh=7IhC90lBhcc&label=_AITNAME_&value=_AITVALUE_\",\"redirect_url\":\"https://www.googleadservices.com/pagead/aclk?sa=L&ai=C1bWdvJ61V76lE7TgzAbjupnoDKX-9ZtG9YK9uuYBrf2xkO0CEAEgsMPUFGD5u--E5C-gAaO6x8QDyAEJqQK6UaVbL_yyPuACAKgDAaoEvwFP0LpoeNhacYMpLRFdYWrzTrh0haQsgyjvcnNJQBJENyRn972MItZy0M1ZIChJFtw2F-AV2PwLDVdxwrsFIBwsxiIJXyzuJo1Hj5_Y5I9e4wuNLWFhYS6rMMwY7Dj2UNG-yWy4hHVdutMvrJeBw0UNCifJK8h54016Vu0cspLaYO4KYmsvDLy11j_4-DPChNJI0RYtHEOHIEh3_LImqTWWfgn6Bl17MaaGG5HGAJzYcUv8ByYd_DZiq-wnArdB6eAEAYgGAaAGLoAHxcW4O6gHgcYbqAemvhvYBwDYEww&num=1&cid=CAASEuRo3n0TcE6tBqwEkiJ9ISK--Q&sig=AOD64_1-XjUl3YqIGZwcSmo1TMa-9bYUHw&client=ca-pub-2996542930718127&adurl=http://www.toyota.co.ma/yaris\",\"visible_url\":\"www.toyota.co.ma\",\"destination_url\":\"http://www.toyota.co.ma/yaris\",\"link_target\":\"_top\",\"google_template_data\":{\"rendering_settings\":{\"format\":\"160x600\",\"rtl\":\"true\"},\"adData\":[{\"layout\":\"Custom\",\"creationContext\":\"AUTHORING_TOOL:GOOGLE_WEB_DESIGNER\",\"preU2Urls\":\"true\",\"displayUrl\":\"www.toyota.co.ma\",\"destinationUrl\":\"http://www.toyota.co.ma/yaris\",\"FLAG_client_side_flag_overrides\":\"[{\\\"name\\\" : \\\"uses_octagon_sdk\\\", \\\"value\\\" : true}]\",\"Custom_layout\":\"https://tpc.googlesyndication.com/sadbundle/18374714691496760605/Ban-toyota-biTon-160x600/index.html?csp=er3\",\"versionInfo\":\"7.3.2\"}]}}";
        String destURL = RegExService.getGADestinationUrl(input);
        Assert.assertEquals("http://www.toyota.co.ma/yaris", destURL);
    }

    @Test
    public void getDurationRegex(){
        long duration = RegExService
                .getDurationInSecondes("  Duration: 00:00:30.00, start: 0.000000, bitrate: 628 kb/s");
        Assert.assertEquals(30, duration);

        duration = RegExService
                .getDurationInSecondes("  Duration: 00:01:30.00, start: 0.000000, bitrate: 628 kb/s");
        Assert.assertEquals(90, duration);

        duration = RegExService
                .getDurationInSecondes("  Duration: 00:03:39.70, start: 0.000000, bitrate: 628 kb/s");
        Assert.assertEquals(219, duration);

    }

    @Test
    public void getAdUrlFromClickTag(){
        String in = "https://adclick.g.doubleclick.net/pcs/click?xai=AKAOjstsdSYtw8fRCkICI_UjE97sLc-GTVMP7hQuDGziv_HXH8Bgx9cwCa43odJ9ua4-cnLVvGYkmRPpALXnytqPd259CZdfXuUz_DfPItXyEWRc6OaRrXj2JVC4rq7FaTHfiCnIrwsIxzgtIC1Vb9xpLCNf2WZGZhWv8c3-USAoSfPpiHz4KkZMTwK0TsoHlgNP-m_OpR59vNB5orCB-_yQqdGCKuh-v3HNuUtwlATK-NGrYQ&sig=Cg0ArKJSzNL8SDV41o7FEAE&urlfix=1&adurl=https://adclick.g.doubleclick.net/pcs/click?xai=AKAOjstbGzI-K4ggnF8cG0fNsyDVH7FPOn_sklCFMqTUVMy6oJHzadf2xyMXGS4EgWIVrbslokhtkFWIdT1kSkXV2e-zBI3hu74NHnSLVau8j4dApUzH_ZvVuuM_&sig=Cg0ArKJSzDXoLbckTvw_EAE&urlfix=1&rm_eid=3187698&adurl=http%3A%2F%2Fwww.mobily.com.sa%2Fportalu%2Fwps%2Fportal%2Fpersonal%2Fdiscover%2Fall%2F10gb-voucher%3Futm_source%3DAlKhalijeyyah_RON%26utm_medium%3DDisplay%26utm_campaign%3D10GB_Unlimited_Data";
        Assert.assertEquals("http%3A%2F%2Fwww.mobily.com.sa%2Fportalu%2Fwps%2Fportal%2Fpersonal%2Fdiscover%2Fall%2F10gb-voucher%3Futm_source%3DAlKhalijeyyah_RON%26utm_medium%3DDisplay%26utm_campaign%3D10GB_Unlimited_Data",
                RegExService.getAdUrlFromClickTag(in));
        in = "https://adclick.g.doubleclick.net/pcs/click?xai=AKAOjsuQAtECuOIQN2X5w2VfDNPI_fu8MCZXYf4mIqWOk6EtcIGcTp9zPdd_XQjWxz3Z9-lWVx74YHLjNaX2THVLzZDmq5AaN32GM75v7WgJbK8XT5HWCBGwtDLFHtSEFU5pV14&sig=Cg0ArKJSzJjLZCjz8wb-EAE&urlfix=1&rm_eid=3187698&adurl=https://adclick.g.doubleclick.net/pcs/click%3Fxai%3DAKAOjsthAHjCgRpPA0TmVguATZFNJeZMGd3AheJ6ZlpnKlckzMeGhyAw0tejpGuxSnOlq69gEgC0eGgv7AVSJp0TKQO_nptyBt_zNmRElHYge3LyqPh2T3aiiRQ71C8GDBpjidx2vu13sSYhWJLM8KpWBRsQ4cuQl5YBTOx5PpNLSv2WLlk1a0e4QXD-je0HgIt7_jwrFd_1kgO-2SP22vaRFqp0-_45lX_Hv8apoPAz%26sig%3DCg0ArKJSzMcBUgzNpp5BEAE%26urlfix%3D1%26adurl%3Dhttp%3A%2F%2Fwww.mobily.com.sa%2Fportalu%2Fwps%2Fportal%2Fpersonal%2Fdiscover%2Fall%2F10gb-voucher%3Futm_source%3DAlMadina%26utm_medium%3DMobile%26utm_campaign%3D10GB_Unlimited_Data&toto=11";
        Assert.assertEquals("http%3A%2F%2Fwww.mobily.com.sa%2Fportalu%2Fwps%2Fportal%2Fpersonal%2Fdiscover%2Fall%2F10gb-voucher%3Futm_source%3DAlMadina%26utm_medium%3DMobile%26utm_campaign%3D10GB_Unlimited_Data&toto=11",
                RegExService.getAdUrlFromClickTag(in));
    }

    @Test
    public void isAdsenseHtmlCases(){
        //
        Assert.assertEquals(false, RegExService.isAdsenseHtml(null));

        //
        String html = "<html><head>\n" +
                "    <meta charset=\"UTF-8\">\n" +
                "    <title>SafeFrame Container</title>\n" +
                "  </head>\n" +
                "  <body marginwidth=\"0\" marginheight=\"0\" leftmargin=\"0\" topmargin=\"0\" style=\"background:transparent\" onload=\"rptcsi('loadimgad')\">\n" +
                "    <script>\n......" +

                "</script><div id=\"google_image_div\" style=\"height: 600px; width: 300px; overflow:hidden; position:absolute\"><input type=\"hidden\" id=\"csi\" value=\"1\"><a id=\"aw0\" target=\"_top\" href=\"https://www.googleadservices.com/pagead/aclk?sa=L&amp;ai=CIb_lr5gkWO-DLbDatgeE_ZvYDLGng55HgsvVpuADjay34f0DEAEg3cSlHGD5u--E5C-gAfWckf8DyAEC4AIAqAMByAOZBKoElAJP0AADczzsDwclhe2Hpp3dYA3vPwsVfcmyOpaCNIMnnGBYYoVnnvzeRyzxdl66UjzmDRKpcLeA5ueDtxfnRpcrY83RrBbr6vJ5-zL5qndvsSflw8lEJNAum4FhJjhCqnexZ2Ccq3t_hoS_xcCDW9JPkUJYpxVsx4_EuNDGi6YlOm8Sdo60uiEYLlXVzKR5Tb4j2grJoskWelhLrj09eONjv7KbspPvAd4nVQ5Zvc37AqpLMEqAAyv4iivogJskb30NiQKCCVr8l39d928GbaanwHxF2xsPo-87c3PNuuWEOw8TbX3ph7xoUqkZDzm8DOymftcsfeyzKrBXn1U-DI2tGSbdocfm9piKF3aDSbJFpy2DqL3gBAGIBgGgBgKAB_PibqgHpr4b2AcB0ggFCIBhEAE&amp;num=1&amp;cid=CAASEuRojFJerJLyKc6m068sSbFZaw&amp;sig=AOD64_1eBA5HBgbtJE4AoU-a8L-_oZ79QA&amp;client=ca-pub-9611340043830472&amp;adurl=http://welcome.linode.com/bundled-pricing-4gb/\" data-original-click-url=\"https://www.googleadservices.com/pagead/aclk?sa=L&amp;ai=CIb_lr5gkWO-DLbDatgeE_ZvYDLGng55HgsvVpuADjay34f0DEAEg3cSlHGD5u--E5C-gAfWckf8DyAEC4AIAqAMByAOZBKoElAJP0AADczzsDwclhe2Hpp3dYA3vPwsVfcmyOpaCNIMnnGBYYoVnnvzeRyzxdl66UjzmDRKpcLeA5ueDtxfnRpcrY83RrBbr6vJ5-zL5qndvsSflw8lEJNAum4FhJjhCqnexZ2Ccq3t_hoS_xcCDW9JPkUJYpxVsx4_EuNDGi6YlOm8Sdo60uiEYLlXVzKR5Tb4j2grJoskWelhLrj09eONjv7KbspPvAd4nVQ5Zvc37AqpLMEqAAyv4iivogJskb30NiQKCCVr8l39d928GbaanwHxF2xsPo-87c3PNuuWEOw8TbX3ph7xoUqkZDzm8DOymftcsfeyzKrBXn1U-DI2tGSbdocfm9piKF3aDSbJFpy2DqL3gBAGIBgGgBgKAB_PibqgHpr4b2AcB0ggFCIBhEAE&amp;num=1&amp;cid=CAASEuRojFJerJLyKc6m068sSbFZaw&amp;sig=AOD64_1eBA5HBgbtJE4AoU-a8L-_oZ79QA&amp;client=ca-pub-9611340043830472&amp;adurl=http://welcome.linode.com/bundled-pricing-4gb/\"><img src=\"https://tpc.googlesyndication.com/simgad/15754566051314415043\" border=\"0\" width=\"300\" alt=\"\" class=\"img_ad\" onload=\"tick('1ad');\"></a><style>div,ul,li{margin:0;padding:0;}.abgc{height:15px;position:absolute;right:0px;top:0px;text-rendering:geometricPrecision;width:15px;z-index:9020;}.abgb{height:15px;width:15px;}.abgc img{display:block;}.abgc svg{display:block;}.abgs{display:none;height:100%;}.abgl{text-decoration:none;}.abgi{fill-opacity:1.0;fill:#00aecd;stroke:none;}.abgbg{fill-opacity:1.0;fill:#cdcccc;stroke:none;}.abgtxt{fill:black;font-family:'Arial';font-size:100px;overflow:visible;stroke:none;}</style><div id=\"abgc\" class=\"abgc\" dir=\"ltr\" style=\"width: 15px; height: 15px;\"><div id=\"abgb\" class=\"abgb\" style=\"display: block;\"><svg width=\"100%\" height=\"100%\"><rect class=\"abgbg\" width=\"100%\" height=\"100%\"></rect><svg class=\"abgi\" x=\"0px\"><path d=\"M7.5,1.5a6,6,0,1,0,0,12a6,6,0,1,0,0,-12m0,1a5,5,0,1,1,0,10a5,5,0,1,1,0,-10ZM6.625,11l1.75,0l0,-4.5l-1.75,0ZM7.5,3.75a1,1,0,1,0,0,2a1,1,0,1,0,0,-2Z\"></path></svg></svg></div><div id=\"abgs\" class=\"abgs\" style=\"display: none;\"><a id=\"abgl\" class=\"abgl\" href=\"https://www.google.com/url?ct=abg&amp;q=https://www.google.com/adsense/support/bin/request.py%3Fcontact%3Dabg_afc%26url%3Dhttp://mtv.com.lb/News/Cuts/540952/%2525D8%2525A7%2525D9%252584%2525D8%2525B6%2525D8%2525A7%2525D8%2525AD%2525D9%25258A%2525D8%2525A9_%2525D8%2525A5%2525D8%2525B1%2525D9%252587%2525D8%2525A7%2525D8%2525A8%2525D9%25258A%2525D9%252588_%2525D9%252581%2525D9%25258A%2525D9%25258A%2525D9%252586%2525D8%2525A7!%26gl%3DMA%26hl%3Dar%26client%3Dca-pub-9611340043830472%26ai0%3DCIb_lr5gkWO-DLbDatgeE_ZvYDLGng55HgsvVpuADjay34f0DEAEg3cSlHGD5u--E5C-gAfWckf8DyAEC4AIAqAMByAOZBKoElAJP0AADczzsDwclhe2Hpp3dYA3vPwsVfcmyOpaCNIMnnGBYYoVnnvzeRyzxdl66UjzmDRKpcLeA5ueDtxfnRpcrY83RrBbr6vJ5-zL5qndvsSflw8lEJNAum4FhJjhCqnexZ2Ccq3t_hoS_xcCDW9JPkUJYpxVsx4_EuNDGi6YlOm8Sdo60uiEYLlXVzKR5Tb4j2grJoskWelhLrj09eONjv7KbspPvAd4nVQ5Zvc37AqpLMEqAAyv4iivogJskb30NiQKCCVr8l39d928GbaanwHxF2xsPo-87c3PNuuWEOw8TbX3ph7xoUqkZDzm8DOymftcsfeyzKrBXn1U-DI2tGSbdocfm9piKF3aDSbJFpy2DqL3gBAGIBgGgBgKAB_PibqgHpr4b2AcB0ggFCIBhEAE&amp;usg=AFQjCNF1h1Fo9iJz9vTIL99xHjnJGPCTdA\" target=\"_blank\"><svg width=\"100%\" height=\"100%\"><path class=\"abgbg\" d=\"M0,0L100,0L100,15L4,15s-4,0,-4,-4z\"></path><svg class=\"abgtxt\" x=\"5px\" y=\"11px\" width=\"78px\"><text transform=\"scale(0.14386167146974063)\">حول هذا الإعلان</text></svg><svg class=\"abgi\" x=\"85px\"><path d=\"M7.5,1.5a6,6,0,1,0,0,12a6,6,0,1,0,0,-12m0,1a5,5,0,1,1,0,10a5,5,0,1,1,0,-10ZM6.625,11l1.75,0l0,-4.5l-1.75,0ZM7.5,3.75a1,1,0,1,0,0,2a1,1,0,1,0,0,-2Z\"></path></svg></svg></a></div></div><script>var abgp={elp:document.getElementById('abgcp'),el:document.getElementById('abgc'),ael:document.getElementById('abgs'),iel:document.getElementById('abgb'),hw:15,sw:100,hh:15,sh:15,himg:'https://tpc.googlesyndication.com'+'/pagead/images/abg/icon.png',simg:'https://tpc.googlesyndication.com/pagead/images/ata/ar.png',alt:'حول هذا الإعلان',t:'حول هذا الإعلان',tw:78,t2:'',t2w:0,tbo:0,att:'adsbygoogle',ff:'',halign:'right',fe:false,iba:false,lttp:true,umd:false,uic:false,uit:false,ict:document.getElementById('cbb'),icd:undefined,uaal:true,opi: false};</script><script src=\"https://tpc.googlesyndication.com/pagead/js/r20161031/r20110914/abg.js\"></script><iframe frameborder=\"0\" height=\"0\" width=\"0\" src=\"https://googleads.g.doubleclick.net/pagead/drt/re?v=r20150723\" style=\"position:absolute\"></iframe></div><script src=\"https://tpc.googlesyndication.com/pagead/js/r20161031/r20110914/client/ext/m_js_controller.js\">" +
                "</body></html>";
        Assert.assertEquals(true, RegExService.isAdsenseHtml(html));



    }

    @Test
    public void isAdsenseOriginalUrlFalseForNull(){
        assertFalse(RegExService.isAdsenseOriginalUrl(null));
    }
    @Test
    public void isAdsenseOriginalUrlTrueForRUBICONPROJECT(){
        assertTrue(RegExService.isAdsenseOriginalUrl("https://secure-assets.rubiconproject.com/static/psa/casala/CASALA_leaderboard_ad.gif"));
    }
    @Test
    public void isAdsenseOriginalUrlTrueForADNXS(){
        assertTrue(RegExService.isAdsenseOriginalUrl("http://cdn.adnxs.com/p/e3/f2/5f/23/e3f25f23ae87eea1cf621f22fd28c88a.jpg"));
    }

    @Test
    public void isAdsenseOriginalUrlTrueForADROLL(){
        assertTrue(RegExService.isAdsenseOriginalUrl("https://s.adroll.com/a/LFR/7RD/LFR7RDOAFFCFXA6GXUUWKK.jpg"));
    }

    @Test
    public void isAdsenseOriginalUrlTrueForDISQUSADS(){
        assertTrue(RegExService.isAdsenseOriginalUrl("http://disqusads.com/ads-iframe/taboola/?category=news&service=dynamic&safetylevel=20&video_allowed=0&variant=fallthrough&experiment=network_default&forum_pk=1064855&provider=adsnative&position=top&shortname=dailystarleb&forum_shortname=dailystarleb&anchorColor=%23434c55&colorScheme=light&sourceUrl=http%3A%2F%2Fwww.dailystar.com.lb%2FBusiness%2FLocal%2F2016%2FJan-06%2F330405-tobacco-smuggling-declines-on-regie-crackdown.ashx&typeface=sans-serif&canonicalUrl=http%3A%2F%2Fwww.dailystar.com.lb%2F%2FBusiness%2FLocal%2F2016%2FJan-06%2F330405-tobacco-smuggling-declines-on-regie-crackdown.ashx&disqus_version=985d0e8"));
    }

    @Test
    public void isAdsenseOriginalUrlTrueForCRITEO(){
        assertTrue(RegExService.isAdsenseOriginalUrl("https://cas.fr.eu.criteo.com/delivery/r/afr.php?did=57bdeb03bca08def7533c1398e094460&z=V73rBAAA2YwK23WiAAKe2woH6AEuldwHKrlhLA&u=%7CdjA7%2B%2FdE6tETHtAbMbur7DnusEU1APUUoguBr5EYcJo%3D%7C&c1=4z_1vBnVXyU3s5S1ODdcxBEhMh_1PE6dCt88-QtmDLFZoesv54i95JtaNWn21qj781jSzj2qNG0TWytCF1hbuCwUml86ELl1WcDSBf9ot3sn7cOoHnhHYTml2ifONyQQ5_39yQpCMcRk9N47G4KliUs1M6yRqhbqRbbhxdNtbbGfjkWmN-Pv-QE5ma4KGJe1Qr5cggkaBacQBeXlh6MOU7_PHRbFWgQ1Uyl-gRRVokmgpsLQXf-azFR_Br8YYKFPkVmWP3DBqznTIkJi1NyFf092xnd_xZ8c&ct0=https://adclick.g.doubleclick.net/aclk%3Fsa%3DL%26ai%3DC_8DGBOu9V4yzA6Lr7QbbvYqACPeX8JRG2YKs8pACwI23ARABIABgkQaCARdjYS1wdWItOTM2NjA0NzE2OTczMjk2NsgBCagDAaoEiAFP0Bl4yKu81uBm3yB0zzJdtbfRsKkamQeUZRmnZKxSu30lCrJWs9AMMRicEdeT4Y-yw5mGOaHtgQ7PudxGLGT-lRoFARpH1Kqiyky1VuKE3qD5WyHvmNWrW6rHVZGRqBAGrNcGBIoNclM6WC41bvJIcjqtRYE1ZqtLeecqafFj_97qsoiI6MwDgAbhycjT6Nq-pvMBoAYhqAemvhvYBwA%26num%3D1%26sig%3DAOD64_0cvRlXbqfcYyJROryB-w8IK0BahQ%26client%3Dca-pub-9366047169732966%26adurl%3D"));
    }



    @Test
    public void isAdsenseDestinationUrlFalseForNull(){
        assertFalse(RegExService.isAdsenseDestinationUrl(null));
    }
    @Test
    public void isAdsenseDestinationUrlTrueForADNXS(){
        assertTrue(RegExService.isAdsenseDestinationUrl("https://secure-sin.adnxs.com/click?AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA__________8AAAAAAAAAAAAAAAAAAAAA5QAAAAIAAACqvSACAAAAAAAAAAAAAAAAAAAAACwB-gAAAAAAAAAAAgEAAAAAAAAAkwvo-wAAAAA./clickenc=http://www.deletebloodcancer.org/?utm_source=AppNexus"));
    }

    @Test
    public void isAdsenseDestinationUrlTrueForAFFILIAXE(){
        assertTrue(RegExService.isAdsenseDestinationUrl("http://performance.affiliaxe.com/aff_c?offer_id=22647&aff_id=87676&url_id=10373&source=disqus-7887bc6e-609d-430d-a49c-0ef566c7bbe1"));
    }

    @Test
    public void testCleanFbAdLastAndFirstLikes(){
        final String contentToClean = "Sanaa Lawti, Lamiaa Idrissi et 3 autres amis aiment ça.\n" +
                "اقوال كبور التاريخية\n" +
                "اقوال كبور التاريخية\n" +
                "اقوال كبور التاريخية\n" +
                "Magazine\n" +
                "2 015 616 personnes aiment ça.";

        final String cleanedContent = RegExService.cleanFbAdFromLikes(contentToClean);

        Assert.assertFalse(cleanedContent.contains("2 015 616 personnes aiment ça."));
        Assert.assertEquals(cleanedContent,
                "اقوال كبور التاريخية\n" +
                "اقوال كبور التاريخية\n" +
                "اقوال كبور التاريخية\n" +
                "Magazine");
    }
    @Test
    public void testCleanFbAdLastLikes(){
        final String contentToClean = " Prepare Ton Exam\n" +
                "ستافدوا من نصائح الكوتش ناهد رشاد باش تنجحو في إمتحاناتكم Profite des conseils gratuits...\n" +
                "Prepare Ton Exam\n" +
                "Communauté\n" +
                "45 717 personnes aiment ça.";

        final String cleanedContent = RegExService.cleanFbAdFromLikes(contentToClean);

        Assert.assertFalse(cleanedContent.contains("2 015 616 personnes aiment ça."));
        Assert.assertEquals(cleanedContent, " Prepare Ton Exam\n" +
                "ستافدوا من نصائح الكوتش ناهد رشاد باش تنجحو في إمتحاناتكم Profite des conseils gratuits...\n" +
                "Prepare Ton Exam\n" +
                "Communauté");
    }
    @Test
    public void testCleanRightFbAdLastLikes(){
        final String contentToClean = "Build Better Ad Campaigns\n" +
                "Uncover Your Competitor's Display Strategy with WhatRunsWhere. Start Today!\n" +
                "2 720 personnes aiment";

        final String cleanedContent = RegExService.cleanRightFbAdFromLikes(contentToClean);

        Assert.assertFalse(cleanedContent.contains("2 720 personnes aiment"));
        Assert.assertEquals(cleanedContent, "Build Better Ad Campaigns\n" +
                "Uncover Your Competitor's Display Strategy with WhatRunsWhere. Start Today!");
    }
    @Test
    public void testCleanRightFbAdfromStatsData(){
        final String contentToClean = "Build Better Ad Campaigns\n" +
                "Uncover Your Competitor's Display Strategy with WhatRunsWhere. Start Today!\n" +
                "65465464654";

        final String cleanedContent = RegExService.cleanRightFbAdFromLikes(contentToClean);

        Assert.assertFalse(cleanedContent.contains("65465464654"));
        Assert.assertEquals(cleanedContent, "Build Better Ad Campaigns\n" +
                "Uncover Your Competitor's Display Strategy with WhatRunsWhere. Start Today!");
    }

    @Test
    public void getManifestForCreateJS(){
        String in = "{\"data\":[{\"EaselJS\":{\"version\":\"0.7.0\",\"buildDate\":\"Tue, 01 Oct 2013 16:02:38 GMT\"},\"TweenJS\":{\"version\":\"0.5.0\",\"buildDate\":\"Wed, 25 Sep 2013 17:09:35 GMT\"},\"PreloadJS\":{\"version\":\"0.4.0\",\"buildDate\":\"Wed, 25 Sep 2013 17:09:35 GMT\"}},{\"properties\":{\"width\":728,\"height\":90,\"fps\":30,\"color\":\"#FFFFFF\",\"manifest\":[{\"src\":\"https://tpc.googlesyndication.com/sadbundle/17282057955997869545/icn_2_min.png\",\"id\":\"icn_2_min\",\"ext\":\"png\",\"type\":\"image\",\"tag\":{}},{\"src\":\"https://tpc.googlesyndication.com/sadbundle/17282057955997869545/logo_min.png\",\"id\":\"logo_min\",\"ext\":\"png\",\"type\":\"image\",\"tag\":{}},{\"src\":\"https://tpc.googlesyndication.com/sadbundle/17282057955997869545/pattern.png\",\"id\":\"pattern\",\"ext\":\"png\",\"type\":\"image\",\"tag\":{}}]}}]}";

        assertEquals("[{\"src\":\"https://tpc.googlesyndication.com/sadbundle/17282057955997869545/icn_2_min.png\",\"id\":\"icn_2_min\",\"ext\":\"png\",\"type\":\"image\",\"tag\":{}},{\"src\":\"https://tpc.googlesyndication.com/sadbundle/17282057955997869545/logo_min.png\",\"id\":\"logo_min\",\"ext\":\"png\",\"type\":\"image\",\"tag\":{}},{\"src\":\"https://tpc.googlesyndication.com/sadbundle/17282057955997869545/pattern.png\",\"id\":\"pattern\",\"ext\":\"png\",\"type\":\"image\",\"tag\":{}}]",
                RegExService.getManifestFromData(in));
    }

}