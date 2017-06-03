package ma.labs.bot.core;
/********************************************************
 *                                                      *
 *                    PIXITREND ROBOT                   *
 *                    BY labs GROUP                    *
 *                                                      *
 ********************************************************/

import java.util.List;

/**
 * Created by labs004 on 02/09/2016.
 */
public class GetChecksumOfAllImagesOnPageResp {
    private String html5ElementChecksum;
    private List<ImageUrlFileName> images2Convert64;

    public GetChecksumOfAllImagesOnPageResp() {
    }

    public GetChecksumOfAllImagesOnPageResp(String html5ElementChecksum, List<ImageUrlFileName> images2Convert64) {
        this.html5ElementChecksum = html5ElementChecksum;
        this.images2Convert64 = images2Convert64;
    }

    public String getHtml5ElementChecksum() {
        return html5ElementChecksum;
    }

    public void setHtml5ElementChecksum(String html5ElementChecksum) {
        this.html5ElementChecksum = html5ElementChecksum;
    }

    public List<ImageUrlFileName> getImages2Convert64() {
        return images2Convert64;
    }

    public void setImages2Convert64(List<ImageUrlFileName> images2Convert64) {
        this.images2Convert64 = images2Convert64;
    }
}
