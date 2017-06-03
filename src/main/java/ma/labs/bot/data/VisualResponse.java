package ma.labs.bot.data;
/********************************************************
 *                                                      *
 *                    PIXITREND ROBOT                   *
 *                    BY labs GROUP                    *
 *                                                      *
 ********************************************************/

import org.apache.commons.lang3.builder.ReflectionToStringBuilder;

import java.util.List;
/**
 * Created by labs004 on 20/07/2016.
 */

public class VisualResponse {

    private String checksum;

    private String idMedia;

    private String toBeFixed;

    private List<Visual> visuals;

    public String getChecksum() {
        return checksum;
    }

    public void setChecksum(String checksum) {
        this.checksum = checksum;
    }

    public String getIdMedia() {
        return idMedia;
    }

    public void setIdMedia(String idMedia) {
        this.idMedia = idMedia;
    }

    public List<Visual> getVisuals() {
        return visuals;
    }

    public void setVisuals(List<Visual> visuals) {
        this.visuals = visuals;
    }

    public String getToBeFixed() {
        return toBeFixed;
    }

    public void setToBeFixed(String toBeFixed) {
        this.toBeFixed = toBeFixed;
    }

    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this);
    }
}