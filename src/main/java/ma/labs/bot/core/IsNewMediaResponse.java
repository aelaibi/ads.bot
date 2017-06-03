package ma.labs.bot.core;
/********************************************************
 *                                                      *
 *                    PIXITREND ROBOT                   *
 *                    BY labs GROUP                    *
 *                                                      *
 ********************************************************/

import org.apache.commons.lang3.builder.ReflectionToStringBuilder;

/**
 * Created by labs004 on 15/08/2016.
 */
public class IsNewMediaResponse {

    private String isNewMedia;
    private String isNewVisual;
    private String idMedia;
    private String idVisual;
    private String toBeFixed;

    public IsNewMediaResponse() {

    }

    public IsNewMediaResponse(String isNewMedia, String isNewVisual, String idMedia, String idVisual, String toBeFixed) {
        this.isNewMedia = isNewMedia;
        this.isNewVisual = isNewVisual;
        this.idMedia = idMedia;
        this.idVisual = idVisual;
        this.setToBeFixed(toBeFixed);
    }

    public String getIsNewMedia() {
        return isNewMedia;
    }

    public void setIsNewMedia(String isNewMedia) {
        this.isNewMedia = isNewMedia;
    }

    public String getIsNewVisual() {
        return isNewVisual;
    }

    public void setIsNewVisual(String isNewVisual) {
        this.isNewVisual = isNewVisual;
    }

    public String getIdMedia() {
        return idMedia;
    }

    public void setIdMedia(String idMedia) {
        this.idMedia = idMedia;
    }

    public String getIdVisual() {
        return idVisual;
    }

    public void setIdVisual(String idVisual) {
        this.idVisual = idVisual;
    }

    public String getToBeFixed() {
        return toBeFixed;
    }

    public boolean isToBeFixed(){
        return "1".equals(this.getToBeFixed());
    }

    public void setToBeFixed(String toBeFixed) {
        this.toBeFixed = toBeFixed;
    }

    public boolean isNewMedia(){
        return "1".equals(this.getIsNewMedia());
    }

    public boolean isNewVisual(){
        return "1".equals(this.getIsNewVisual());
    }

    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        IsNewMediaResponse that = (IsNewMediaResponse) o;

        if (getIsNewMedia() != null ? !getIsNewMedia().equals(that.getIsNewMedia()) : that.getIsNewMedia() != null)
            return false;
        if (getIsNewVisual() != null ? !getIsNewVisual().equals(that.getIsNewVisual()) : that.getIsNewVisual() != null)
            return false;
        if (getIdMedia() != null ? !getIdMedia().equals(that.getIdMedia()) : that.getIdMedia() != null) return false;
        return getIdVisual() != null ? getIdVisual().equals(that.getIdVisual()) : that.getIdVisual() == null;

    }

    @Override
    public int hashCode() {
        int result = getIsNewMedia() != null ? getIsNewMedia().hashCode() : 0;
        result = 31 * result + (getIsNewVisual() != null ? getIsNewVisual().hashCode() : 0);
        result = 31 * result + (getIdMedia() != null ? getIdMedia().hashCode() : 0);
        result = 31 * result + (getIdVisual() != null ? getIdVisual().hashCode() : 0);
        return result;
    }
}
