package ma.labs.bot.utils;

import ma.labs.bot.data.AdNetwork;
import ma.labs.bot.data.Page;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;

/**
 * Created by Mohamed on 06/03/2017.
 */
public class ShahidUtilsTest {
    @Test
    public void testShahidPageOK(){
        Page page = new Page(1, "https://shahid.mbc.net/ar/series/100482/%D8%A3%D9%85%D9%86%D8%A7-%D8%B1%D9%88%D9%8A%D8%AD%D8%A9-%D8%A7%D9%84%D8%AC%D9%86%D8%A9.html",
                "", "", "");
        Assert.assertTrue(ShahidUtils.isShahidPage(page));
    }
    @Test
    public void testShahidPageKO(){
        Page page = new Page(1, "https://www.google.com",
                "", "", "");
        Assert.assertFalse(ShahidUtils.isShahidPage(page));
        Assert.assertFalse(ShahidUtils.isShahidPage(null));
    }
    @Test
    public void testRemoveCdataFromInputWillRemove(){
        final String input = "<![CDATA[Holla]]>";
        Assert.assertEquals("Holla",ShahidUtils.removeCDataFromInput(input));
    }
    @Test
    public void testRemoveCdataFromInputNothingToRemove(){
        final String input = "<![CDATA[Pixitrend";
        Assert.assertEquals("<![CDATA[Pixitrend", ShahidUtils.removeCDataFromInput(input));
    }
    @Test
    public void testRemoveCdataFromInputNothingToRemove2(){
        final String input = "Pixitrend";
        Assert.assertEquals("Pixitrend",ShahidUtils.removeCDataFromInput(input));
    }
    @Test
    public void testRemoveCdataFromInputNothingToRemoveBlankInput(){
        Assert.assertEquals("",ShahidUtils.removeCDataFromInput(""));
        Assert.assertNull(ShahidUtils.removeCDataFromInput(null));
    }
    @Test
    public void testGetAdSystem(){
        Assert.assertEquals("DCM", ShahidUtils.getAdSystem("blah blah ...<AdSystem>DCM</AdSystem>... another blah"));
    }
    @Test
    public void testGetAdSystemContainsCdata(){
        Assert.assertEquals("DCM", ShahidUtils.getAdSystem("blah blah ...<AdSystem><![CDATA[DCM]]></AdSystem>... another blah"));
    }
    @Test
    public void testGetAdDest(){
        Assert.assertEquals("https://www.googleadservices.com/pagead/aclk?sa=L&ai=CiRHwon-9WJvcOoO9btmCh7gJ4Ob5jUizjM6w2gT2qr64NxABIIe_3h9g-bvvhOQvoAG8y4j_A8gBBakCtBj-kdPDsT7gAgCoAwGYBACqBNcCT9B2AMqL9soVBNokm59iKn0I_YwPSh2-443YgG1E7UA5Um6Nz8_XcODfAy2I6kJzpn7DqOeUrSByClU1Afn4pRGcoYOEX9MRGrmLJv07zkCoVnqUqQF4kdLRem6bkcSmYTsbkLMRq_sQ9G6-ERRkPy6gmtvuL5jMwCiTk_rzZU15RYB-pQn9zMcyOiCuiLl21yiM4rgPVloofIcmpmwFq4ZoEH01JMWlJBy_UbVrVDjUJ4QsbmBj5diw0id4x4hyLaZ3xIVgcdmXy4MdMsVSxxerN5uMAAnEt5mheSgpEeJP6cUdW8NSdyou0Qr9rbKUj6qFQh410r8A90HCkKoaepx2TqcKSciYmxoFNc1ekNvQNccm0C987xpxAF-yyLJsP_fTQQiD1mTEfwWbN4BuSo9MPnKdpdB3yGAFrFZY23xsWftB-52OVjy2_kqs4PQYnKM33PB5eeAEAaAGHIAHrLR3qAemvhvYBwHSCAUIgGEQAbgTMdgTCA&num=1&cid=CAASEuRo4U3TKI3S5iUjy5agzLscCg&sig=AOD64_3yhuRdWSTyDPj5U8JrxVugCBZWVg&client=ca-video-pub-9361771523514298&adurl=http://www.amoursucre.com/demo%3Fr%3D3319",
                ShahidUtils.getAdDestination("<VideoClicks>\n" +
                        "<ClickThrough>\n" +
                        "<![CDATA[\n" +
                        "https://www.googleadservices.com/pagead/aclk?sa=L&ai=CiRHwon-9WJvcOoO9btmCh7gJ4Ob5jUizjM6w2gT2qr64NxABIIe_3h9g-bvvhOQvoAG8y4j_A8gBBakCtBj-kdPDsT7gAgCoAwGYBACqBNcCT9B2AMqL9soVBNokm59iKn0I_YwPSh2-443YgG1E7UA5Um6Nz8_XcODfAy2I6kJzpn7DqOeUrSByClU1Afn4pRGcoYOEX9MRGrmLJv07zkCoVnqUqQF4kdLRem6bkcSmYTsbkLMRq_sQ9G6-ERRkPy6gmtvuL5jMwCiTk_rzZU15RYB-pQn9zMcyOiCuiLl21yiM4rgPVloofIcmpmwFq4ZoEH01JMWlJBy_UbVrVDjUJ4QsbmBj5diw0id4x4hyLaZ3xIVgcdmXy4MdMsVSxxerN5uMAAnEt5mheSgpEeJP6cUdW8NSdyou0Qr9rbKUj6qFQh410r8A90HCkKoaepx2TqcKSciYmxoFNc1ekNvQNccm0C987xpxAF-yyLJsP_fTQQiD1mTEfwWbN4BuSo9MPnKdpdB3yGAFrFZY23xsWftB-52OVjy2_kqs4PQYnKM33PB5eeAEAaAGHIAHrLR3qAemvhvYBwHSCAUIgGEQAbgTMdgTCA&num=1&cid=CAASEuRo4U3TKI3S5iUjy5agzLscCg&sig=AOD64_3yhuRdWSTyDPj5U8JrxVugCBZWVg&client=ca-video-pub-9361771523514298&adurl=http://www.amoursucre.com/demo%3Fr%3D3319\n" +
                        "]]>\n" +
                        "</ClickThrough>\n" +
                        "<ClickTracking id=\"\">"));
    }
    @Test
    public void testGetAdDestNoDest(){
        Assert.assertEquals("",
                ShahidUtils.getAdDestination("there is no destination"));
    }
    @Test
    public void testGetAdDef(){
        Assert.assertEquals("http://optimized-by.rubiconproject.com/a/api/vast.xml?account_id=14138&site_id=83000&zone_id=390246&size_id=201&tg_c.language=ar&width=1280&height=720",
                ShahidUtils.getAdDefinitionUrl("<Wrapper>\n" +
                        "<AdSystem>GDFP</AdSystem>\n" +
                        "<VASTAdTagURI>\n" +
                        "<![CDATA[\n" +
                        "http://optimized-by.rubiconproject.com/a/api/vast.xml?account_id=14138&site_id=83000&zone_id=390246&size_id=201&tg_c.language=ar&width=1280&height=720\n" +
                        "]]>\n" +
                        "</VASTAdTagURI>\n" +
                        "<Error>\n" +
                        "<![CDATA[\n" +
                        "https://pu"));
    }
    @Test
    public void testhashGetAdDefReturnsNothing(){
        Assert.assertEquals("",
                ShahidUtils.getAdDefinitionUrl("<Wrapper>\n" +
                        "<AdSystem>GDFP</AdSystem>\n" +
                        "<VASTAdTddagURI>\n" +
                        "<![CDATA[\n" +
                        "http://optimized-by.rubiconproject.com/a/api/vast.xml?account_id=14138&site_id=83000&zone_id=390246&size_id=201&tg_c.language=ar&width=1280&height=720\n" +
                        "]]>\n" +
                        "</VASTAdTagURI>\n" +
                        "<Error>\n" +
                        "<![CDATA[\n" +
                        "https://pu"));
    }
    @Test
    public void testResultContainsAdsOK(){
        Assert.assertTrue(ShahidUtils.resultContainsAds("<VAST xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:noNamespaceSchemaLocation=\"vast.xsd\" version=\"2.0\">\n<Ad id=\"908483471\">\n<Wrapper>\n<AdSystem>GD"));
    }
    @Test
    public void testResultContainsAdsKO(){
        Assert.assertFalse(ShahidUtils.resultContainsAds("<VAST xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:noNamespaceSchemaLocation=\"vast.xsd\" version=\"2.0\">\n<Add id=\"908483471\">\n<Wrapper>\n<AdSystem>GD"));
        Assert.assertFalse(ShahidUtils.resultContainsAds(null));
    }
    @Test
    public void testGetAdsFromInputSize3(){
        final List<String> ads = ShahidUtils.getAdsFromResult("<VAST xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:noNamespaceSchemaLocation=\"vast.xsd\" version=\"2.0\">\n" +
                "<Ad id=\"908483471\">\n" +
                "<Wrapper>\n" +
                "<AdSystem>GDFP</AdSystem>\n" +
                "<VASTAdTagURI>\n" +
                "</Ad>\n" +
                "<Ad id=\"qsdf\">\n" +
                "<Wrapper>\n" +
                "<AdSystem>GDFP</AdSystem>\n" +
                "<VASTAdTagURI>\n" +
                "</Ad>\n" +
                "<Ad id=\"dfghdfgh\">\n" +
                "<Wrapper>\n" +
                "<AdSystem>GDFP</AdSystem>\n" +
                "<VASTAdTagURI>\n" +
                "</Ad>\n" +
                "</VAST>");
        Assert.assertEquals(3, ads.size());
    }
    @Test
    public void testGetAdsFromInputSize0(){
        final List<String> ads = ShahidUtils.getAdsFromResult("<VAST xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:noNamespaceSchemaLocation=\"vast.xsd\" version=\"2.0\">\n" +
                "<Add id=\"908483471\">\n" +
                "<Wrapper>\n" +
                "<VASTAdTagURI>\n" +
                "</Ad>\n" +
                "</VAST>");
        Assert.assertEquals(0, ads.size());
    }

    @Test
    public void testGetAdNetworkDirect(){
        Assert.assertEquals(AdNetwork.DIRECT, ShahidUtils.getAdNetwork("extr", ""));
        Assert.assertEquals(AdNetwork.DIRECT, ShahidUtils.getAdNetwork("extr", "whatever different from others ;)"));
    }
    @Test
    public void testGetAdNetworkAdsence(){
        Assert.assertEquals(AdNetwork.ADSENSE, ShahidUtils.getAdNetwork("extr", "adsence"));
        Assert.assertEquals(AdNetwork.ADSENSE, ShahidUtils.getAdNetwork("extr", "adnxs"));
    }
    @Test
    public void testGetMp4FilesSizeMoreThanZero(){
        final String text = "<![CDATA[\n" +
                "https://servedby.flashtalking.com/click/4/74543;2392100;1607395;211;0/?random=1067985962&ft_width=1&ft_height=1&ft_impID=335696359C0351&ft_custom=&url=https://rto.microsoft.com/IMW/DEC?event=CLK&as.id=4&as.cid=74543&as.sid=7704&as.pid=2392100&as.crid=1607395&as.cb=1489160595&as.cookie=3206323B3EE9E6&rto.iid=FT_IMPRESSIONID&ocid=Edge_dis_pmc_pma_11087212146109_11087212368981_11087212185266&ct=https%3A%2F%2Fad.atdmt.com%2Fc%2Fgo%3Bp%3D11087212368981%3Ba%3D11087212185266%3Bev.a%3D1%3Bidfa%3D%3Baaid%3D%3Bidfa_lat%3D%3Baaid_lat%3D%3Bcache%3D&cti=0&wcs.tr=0\n" +
                "]]>\n" +
                "</ClickThrough>\n" +
                "</VideoClicks>\n" +
                "<MediaFiles>\n" +
                "<MediaFile id=\"2\" delivery=\"progressive\" type=\"video/mp4\" bitrate=\"2329\" width=\"1920\" height=\"1080\">\n" +
                "<![CDATA[\n" +
                "https://cdn.flashtalking.com/66461/130110_EDGE_FY17-Performance-Learn27Sec-EN-Nov-02_US_0x0_NAT_1.mp4\n" +
                "]]>\n" +
                "</MediaFile>\n" +
                "<MediaFile id=\"3\" delivery=\"progressive\" type=\"video/x-flv\" bitrate=\"2329\" width=\"1920\" height=\"1080\">\n" +
                "<![CDATA[\n" +
                "https://cdn.flashtalking.com/66461/130110_EDGE_FY17-Performance-Learn27Sec-EN-Nov-02_US_0x0_NAT_1.flv\n" +
                "]]>\n" +
                "</MediaFile>\n" +
                "<MediaFile id=\"4\" delivery=\"progressive\" type=\"video/webm\" bitrate=\"2329\" width=\"1920\" height=\"1080\">\n" +
                "<![CDATA[\n" +
                "https://cdn.flashtalking.com/66461/130110_EDGE_FY17-Performance-Learn27Sec-EN-Nov-02_US_0x0_NAT_1.webm\n" +
                "]]>";
        Assert.assertEquals(1, ShahidUtils.getAdMp4MediaFiles(text).size());
    }
    @Test
    public void testGetMp4FilesSizeZero(){
        Assert.assertEquals(0, ShahidUtils.getAdMp4MediaFiles(null).size());
    }
}
