/**
 * Created by admin on 23/06/2016.
 */
package ma.labs.bot.connectors;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import ma.labs.bot.core.MediaHelperAPIResponse;
import ma.labs.bot.utils.Utils;
import org.junit.*;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.Assert.*;

@RunWith(SpringRunner.class)
@SpringBootTest
public class MediaHelperAPIConnectorTest {

    protected static WireMockServer wireMockServer;


    @BeforeClass
    public static void setupServer() {

        wireMockServer = new WireMockServer(9999);
        wireMockServer.start();

        WireMock.configureFor(wireMockServer.port());
    }


    @AfterClass
    public static void serverShutdown() {
        wireMockServer.stop();
    }
    @Before
    public void init() throws InterruptedException {
        WireMock.resetToDefault();
    }
    private void mockWith(String reqBody, int status, String respBody) {
        stubFor(post(urlEqualTo("/api/youtube.php"))
                .withHeader("Content-Type", equalTo("application/x-www-form-urlencoded"))
                .withRequestBody(equalTo(reqBody))
                .willReturn(aResponse()
                        .withStatus(status)
                        .withHeader("Content-Type", "text/xml")
                        .withBody(respBody)));
    }
    @Autowired
    private MediaHelperAPIConnector mediaHelperAPIConnector;

    @Test
    public void testNullFileUrl() throws IOException {
        final MediaHelperAPIResponse response = this.mediaHelperAPIConnector.downloadByFileUrl(null);
        final MediaHelperAPIResponse response2 = this.mediaHelperAPIConnector.downloadByYoutubeId(null);
        Assert.assertNull(response);
        Assert.assertNull(response2);
    }
    @Test
    public void testAPIUrlNotValid() throws IOException {
        final MediaHelperAPIResponse response = this.mediaHelperAPIConnector.downloadByFileUrl("");
        final MediaHelperAPIResponse response2 = this.mediaHelperAPIConnector.downloadByYoutubeId("");
        assertNull(response);
        assertNull(response2);
    }
    @Test
    public void testUrlFileNotFound() throws IOException {
        final String fileUrl = "http://www.fb.com/file.php?index=145239764561";
        final String reqBody = "action=download&url="+ Utils.escapeDataString(fileUrl)+"&id";
        final String resp = "{'data':\"{'checksum':'8e0a79e99fee40512de26ae1476d5793', 'duration':300, 'size':27547500}\", 'code':1}";
        mockWith(reqBody, 500, resp);
        final MediaHelperAPIResponse response = this.mediaHelperAPIConnector.downloadByFileUrl(fileUrl);
        assertNull(response);
    }
    @Test
    public void testValidFileExpectedChecksumAndDuration() throws IOException {
        final MediaHelperAPIResponse expectedResponse = new MediaHelperAPIResponse();
        expectedResponse.setChecksum("06b261e94c5d6b37998158943a2ed99b");
        expectedResponse.setDuration(16);
        expectedResponse.setFileSize(1058324);
        final String fileUrl = "https://r2---sn-p5qlsn6l.c.2mdn.net/videoplayback/id/8f65f3c9d38ce049/itag/45/source/doubleclick_dmm/ratebypass/yes/acao/yes/ip/173.193.210.189/ipbits/0/expire/3619966326/sparams/acao,expire,id,ip,ipbits,itag,mm,mn,ms,mv,nh,pl,ratebypass,source/signature/809950F29865FFD6A18363F99EDC0CA9F3130CC4.35FB6551554023135CC0A1F7284150DB63EE15AB/key/cms1/cms_redirect/yes/mm/30/mn/sn-p5qlsn6l/ms/nxu/mt/1476786721/mv/u/nh/IgpwcjAzLmlhZDA3KgkxMjcuMC4wLjE/pl/20?c=MWEB&cver=1.20161013&file=file.webm";
        final String reqBody = "action=download&url="+ Utils.escapeDataString(fileUrl)+"&id";
        final String resp = "{'data':\"{'checksum':'06b261e94c5d6b37998158943a2ed99b', 'duration':16, 'size':1058324}\", 'code':1}";
        mockWith(reqBody,200, resp);
        final MediaHelperAPIResponse response = this.mediaHelperAPIConnector.downloadByFileUrl(fileUrl);
        assertEquals(expectedResponse, response);
    }
    @Test
    public void testYoutubeID() throws IOException {
        final MediaHelperAPIResponse expectedResponse = new MediaHelperAPIResponse();
        expectedResponse.setChecksum("8e0a79e99fee40512de26ae1476d5793");
        expectedResponse.setDuration(300);
        expectedResponse.setFileSize(27547500);
        final String youtubeId = "1w7OgIMMRc4";
        final String reqBody = "action=download&url&id="+ youtubeId;
        final String resp = "{'data':\"{'checksum':'8e0a79e99fee40512de26ae1476d5793', 'duration':300, 'size':27547500}\", 'code':1}";
        mockWith(reqBody,200, resp);
        final MediaHelperAPIResponse response = this.mediaHelperAPIConnector.downloadByYoutubeId(youtubeId);
        assertEquals(expectedResponse, response);
    }

    @Test
    public void downloadByYoutubeIdIsNullIfDuration601() throws IOException {
        String youtubeId = "1w7OgIMMRc4";
        String reqBody = "action=download&url&id="+ youtubeId;
        String resp = "{'data':\"{'checksum':'8e0a79e99fee40512de26ae1476d5793', 'duration':601, 'size':27547500}\", 'code':1}";
        mockWith(reqBody,200, resp);
        MediaHelperAPIResponse response = this.mediaHelperAPIConnector.downloadByYoutubeId(youtubeId);
        assertNull(response);
    }
    @Test
    public void testNotValidYoutubeID() throws IOException {
        final String youtubeId = "OPf0Ysddm0";
        final String reqBody = "action=download&url&id="+ youtubeId+"&url";
        final String resp = "{'data':\"{'checksum':'8e0a79e99fee40512de26ae1476d5793', 'duration':300, 'size':27547500}\", 'code':1}";
        mockWith(reqBody,500, resp);
        final MediaHelperAPIResponse response = this.mediaHelperAPIConnector.downloadByYoutubeId(youtubeId);
        assertNull(response);
    }
    @Test
    public void testEqualsMethodOnMediaHelperAPIResponse(){
        final MediaHelperAPIResponse obj1 = new MediaHelperAPIResponse(null, 0, 0);
        Assert.assertFalse(obj1.equals(null));// null entry
        Assert.assertFalse(obj1.equals("test"));// not same class
        final MediaHelperAPIResponse obj2 = new MediaHelperAPIResponse(null, 1, 0);
        Assert.assertFalse(obj1.equals(obj2));// duration not equal
        obj2.setDuration(0);
        Assert.assertTrue(obj1.equals(obj2));// same duration and same checksum
        obj2.setChecksum("check1");
        Assert.assertFalse(obj1.equals(obj2));// one of checksums is null and the other not
        obj1.setChecksum("check1");
        Assert.assertTrue(obj1.equals(obj2));
        obj2.setChecksum("check2");
        Assert.assertFalse(obj1.equals(obj2));
        obj2.setDuration(1);
        Assert.assertFalse(obj1.equals(obj2));
        obj2.setFileSize(1);
        assertFalse(obj1.equals(obj2));
    }
}
