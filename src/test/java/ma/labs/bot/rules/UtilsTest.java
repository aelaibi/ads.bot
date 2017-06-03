package ma.labs.bot.rules;

import ma.labs.bot.utils.Utils;
import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Test;

import java.net.URI;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

/********************************************************
 *                                                      *
 *                    PIXITREND ROBOT                   *
 *                    BY labs GROUP                    *
 *                                                      *
 ********************************************************/

/**
 * Created by labs004 on 25/07/2016.
 */
public class UtilsTest {


    @Test
    public void getRandom() throws Exception {
        Assert.assertEquals(1, Utils.getRandom(1,2));

        Assert.assertThat(Utils.getRandom(1,5),Matchers.isOneOf(1,2,3,4));

    }
    @Test
    public void checkValidURI() throws Exception {
        Assert.assertEquals(false, Utils.checkValidURI("www.evernote.com/ space /Home.action#"));

    }


    @Test
    public void parseQueryString() throws Exception {
        Map<String, String> ret = Utils.parseQueryString("http://www.evernote.com/Home.action?u=one&name=tow");
        Assert.assertNotNull(ret);
        Assert.assertEquals(2,ret.size());
        Assert.assertEquals("one",ret.get("u"));
    }
    @Test
    public void parseQueryStringRealCase() throws Exception {
        String href= "https://external-mad1-1.xx.fbcdn.net/safe_image.php?d=AQDTx72wqoHulidQ&w=476&h=249&url=https%3A%2F%2Fwww.facebook.com%2Fads%2Fimage%2F%3Fd%3DAQKeNS1xglk9jmHR9HYebWFet4UKSLlvr-JWVDQeOf91IMnZeN3q0cNnnhCGYBGN8ItPsmmzoecYiG0gRu2w5166N7BbqJrO3juKot_L41xmf32XPWl8gLake0e72hOxPIc3h5xc3Z_E7xDAgF6IZuQ_&cfs=1&upscale=1&sx=0&sy=8&sw=650&sh=340&l";
        Map<String, String> ret = Utils.parseQueryString(href);
        Assert.assertNotNull(ret);
        Assert.assertEquals(11,ret.size());
        Assert.assertEquals("https%3A%2F%2Fwww.facebook.com%2Fads%2Fimage%2F%3Fd%3DAQKeNS1xglk9jmHR9HYebWFet4UKSLlvr-JWVDQeOf91IMnZeN3q0cNnnhCGYBGN8ItPsmmzoecYiG0gRu2w5166N7BbqJrO3juKot_L41xmf32XPWl8gLake0e72hOxPIc3h5xc3Z_E7xDAgF6IZuQ_",ret.get("url"));
    }



    @Test
    public void unescapeDataString() throws Exception {
        Assert.assertEquals("https://www.evernote.com/Home.action#n=0",
                Utils.unescapeDataString("https://www.evernote.com/Home.action#n=0"));

    }

    @Test
    public void escapeDataString() throws Exception {
        Assert.assertEquals("https%3A%2F%2Fwww.evernote.com%2FHome.action%23n%3D0"
                ,Utils.escapeDataString("https://www.evernote.com/Home.action#n=0"));
    }

    @Test
    public void greateURI() throws Exception{
        System.out.println(new URI("http://www.yahoo.com").resolve("wel%20come.html").toASCIIString());
    }



    @Test
    public void convertJSONtoArray(){
        String json="{images:[\"http://s1.hespress.com/themes/hespress/img/new_nav_bg.gif\", \"http://s1.hespress.com/themes/hespress/img/menu_sep.gif\", \"http://s1.hespress.com/themes/hespress/img/menu_sep.gif\" ]}";
        List<String> ret =Utils.convertJSONtoArray("images",json);
        Assert.assertNotNull(ret);
        Assert.assertEquals(3, ret.size());
        Assert.assertEquals("http://s1.hespress.com/themes/hespress/img/new_nav_bg.gif", ret.get(0));
    }

    @Test
    public  void replaceFirst(){
        String in = "I'm run in dev mode, dev mode is for test only";
        String out = Utils.replaceFirst(in, "dev", "prod");
        Assert.assertNotNull(out);
        Assert.assertEquals("I'm run in prod mode, dev mode is for test only", out);
    }
    @Test
    public  void replaceLast(){
        String in = "I'm run in dev mode, dev mode is for test only";
        String out = Utils.replaceLast(in, "dev", "prod");
        Assert.assertNotNull(out);
        Assert.assertEquals("I'm run in dev mode, prod mode is for test only", out);
    }

    @Test
    public  void replaceLastHTML(){
        String in = "<!DOCTYPE html>\n" +
                "<html xmlns=\"http://www.w3.org/1999/xhtml\">\n" +
                "\n" +
                "<head>\n" +
                "    <script async=\"\" src=\"https://www.google-analytics.com/analytics.js\"></script>\n" +
                "</head>\n" +
                "<body>\n" +
                "    <div dir=\"ltr\" class=\"abgc\" id=\"abgc\">\n" +
                "        <div class=\"abgb\" id=\"abgb\">\n" +
                "            <svg xmlns=\"http://www.w3.org/2000/svg\" width=\"100%\" height=\"100%\">\n" +
                "                <rect class=\"abgbg\" width=\"100%\" height=\"100%\" />\n" +
                "                <svg class=\"abgi\" x=\"0px\">\n" +
                "                    <path d=\"M7.5,1.5a6,6,0,1,0,0,12a6,6,0,1,0,0,-12m0,1a5,5,0,1,1,0,10a5,5,0,1,1,0,-10ZM6.625,11l1.75,0l0,-4.5l-1.75,0ZM7.5,3.75a1,1,0,1,0,0,2a1,1,0,1,0,0,-2Z\" />\n" +
                "                </svg>\n" +
                "            </svg>\n" +
                "        </div>\n" +
                "    </div>\n" +
                "    <script>\n" +
                "        if (window.top && window.top.postMessage) {\n" +
                "            window.top.postMessage('{\"googMsgType\":\"adpnt\"}', '*');\n" +
                "        }\n" +
                "    </script>\n" +
                "</body>\n" +
                "\n" +
                "</html>";
        String out = Utils.replaceLast(in,"</body>", "<script type='text/javascript'>function removeById(id){if (document.getElementById(id) != undefined) document.getElementById(id).outerHTML='';} function removeDuplicated(id){ var ids = document.querySelectorAll(id), len = ids.length, n; if(len < 2){return;} for(n = 0; n < len-1; n++){ if(ids[n]){ ids[n].parentElement.removeChild(ids[n]);}}} removeDuplicated('#adContent'); removeDuplicated(\"[id*='google_ads_iframe_']\"); removeById('abgb'); removeById('cbc'); </script> </body>");;
        Assert.assertNotNull(out);
        System.out.println(out);
        String expected = "<!DOCTYPE html>\n" +
                "<html xmlns=\"http://www.w3.org/1999/xhtml\">\n" +
                "\n" +
                "<head>\n" +
                "    <script async=\"\" src=\"https://www.google-analytics.com/analytics.js\"></script>\n" +
                "</head>\n" +
                "<body>\n" +
                "    <div dir=\"ltr\" class=\"abgc\" id=\"abgc\">\n" +
                "        <div class=\"abgb\" id=\"abgb\">\n" +
                "            <svg xmlns=\"http://www.w3.org/2000/svg\" width=\"100%\" height=\"100%\">\n" +
                "                <rect class=\"abgbg\" width=\"100%\" height=\"100%\" />\n" +
                "                <svg class=\"abgi\" x=\"0px\">\n" +
                "                    <path d=\"M7.5,1.5a6,6,0,1,0,0,12a6,6,0,1,0,0,-12m0,1a5,5,0,1,1,0,10a5,5,0,1,1,0,-10ZM6.625,11l1.75,0l0,-4.5l-1.75,0ZM7.5,3.75a1,1,0,1,0,0,2a1,1,0,1,0,0,-2Z\" />\n" +
                "                </svg>\n" +
                "            </svg>\n" +
                "        </div>\n" +
                "    </div>\n" +
                "    <script>\n" +
                "        if (window.top && window.top.postMessage) {\n" +
                "            window.top.postMessage('{\"googMsgType\":\"adpnt\"}', '*');\n" +
                "        }\n" +
                "    </script>\n" +
                "<script type='text/javascript'>function removeById(id){if (document.getElementById(id) != undefined) document.getElementById(id).outerHTML='';} function removeDuplicated(id){ var ids = document.querySelectorAll(id), len = ids.length, n; if(len < 2){return;} for(n = 0; n < len-1; n++){ if(ids[n]){ ids[n].parentElement.removeChild(ids[n]);}}} removeDuplicated('#adContent'); removeDuplicated(\"[id*='google_ads_iframe_']\"); removeById('abgb'); removeById('cbc'); </script> </body>\n" +
                "\n" +
                "</html>";
        Assert.assertEquals(expected, out);
    }

    @Test
    public  void isDestinationUrlcontainsGoogleComWhenNull(){
        Assert.assertFalse(Utils.isDestinationUrlcontainsGoogleCom(null));
    }
    @Test
    public  void isDestinationUrlcontainsGoogleComWhenEmpty(){
        Assert.assertFalse(Utils.isDestinationUrlcontainsGoogleCom(""));
    }
    @Test
    public  void isDestinationUrlcontainsGoogleComNotFound(){
        Assert.assertFalse(Utils.isDestinationUrlcontainsGoogleCom("https://adclick.g.doubleclick.net/pcs/click?xai=AKAOjsuQAtECuOIQN2X5w2VfDNPI_fu8MCZXYf4mIqWOk6EtcIGcTp9zPdd_XQjWx"));
    }
    @Test
    public  void isDestinationUrlcontainsGoogleComFoundFirst(){
        Assert.assertTrue(Utils.isDestinationUrlcontainsGoogleCom("www.google.com"));
    }
    @Test
    public  void isDestinationUrlcontainsGoogleComFound(){
        Assert.assertTrue(Utils.isDestinationUrlcontainsGoogleCom("https://www.google.com/url?ct=abg&q=https://www.google.com/adsense/support/bin/request.py%3Fcontact%3Dabg_afc"));
    }
    @Test
    public  void isDestinationUrlcontainsGoogleComFoundLast(){
        Assert.assertFalse(Utils.isDestinationUrlcontainsGoogleCom("https://adclick.g.doubleclick.net/pcs/click?xai=https://www.google.com/url?ct=abg&q=https://www.google.com/adsense/support/bin/request.py%3Fcontact%3Dabg_afc"));
    }
    @Test
    public void testCalculDurationInSecondsNullEntry(){
        final String tagContent = null;
        Assert.assertEquals(Utils.getYoutubeDurationInSecs(tagContent),"0");
    }
    @Test
    public void testCalculDurationInSecondsEmptyEntry(){
        final String tagContent = "";
        Assert.assertEquals(Utils.getYoutubeDurationInSecs(tagContent),"0");
    }
    @Test
    public void testCalculDurationInSeconds(){
        final String tagContent = "5:10";
        Assert.assertEquals(Utils.getYoutubeDurationInSecs(tagContent),"311"); // pay attention, the tag doesn't contain the duration, but the length
        // duration = length+1 (youtube start from zero : 0)
    }

    @Test
    public void removeWhitSpacesTabsNewLinesNull(){
        final String str = "";
        assertNull(Utils.removeWhitSpacesTabsNewLines(null));
    }
    @Test
    public void removeWhitSpacesTabsNewLines(){
        final String str = "\tun\n" +
                "deux\n" +
                "t rois";
        String out = Utils.removeWhitSpacesTabsNewLines(str);
        assertNotNull(out);
        assertEquals("undeuxtrois",out);
    }

    @Test
    public void sortNull(){
        final String str = "";
        assertNull(Utils.sort(null));
    }

    @Test
    public void sort(){
        final String str = "azer2ty";
        String out = Utils.sort(str);
        assertNotNull(out);
        assertEquals("2aertyz",out);
    }

}