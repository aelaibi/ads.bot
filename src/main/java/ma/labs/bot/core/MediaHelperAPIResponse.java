package ma.labs.bot.core;

import com.google.gson.annotations.SerializedName;
import ma.labs.bot.utils.Constants;

/**
 * Created by Mohamed on 13/10/2016.
 */
public class MediaHelperAPIResponse {
    private String checksum;
    private int duration;//in seconds
    @SerializedName("size")
    private long fileSize;

    public MediaHelperAPIResponse(String checksum, int duration, long fileSize) {
        this.checksum = checksum;
        this.duration = duration;
        this.fileSize = fileSize;
    }

    public MediaHelperAPIResponse() {
    }

    public String getChecksum() {
        return checksum;
    }

    public void setChecksum(String checksum) {
        this.checksum = checksum;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public long getFileSize() {
        return fileSize;
    }

    public void setFileSize(long fileSize) {
        this.fileSize = fileSize;
    }
    public boolean isEmptyChecksum() {
        return Constants.CHECKSUMEMPTY.equals(this.getChecksum());
    }
    public boolean equals(Object obj){
        if(null == obj) {   return false ;   }
        if(MediaHelperAPIResponse.class != obj.getClass()) {  return false;   }
        final MediaHelperAPIResponse sameClassObj = (MediaHelperAPIResponse) obj;
        if(null == sameClassObj.getChecksum() && null != this.getChecksum() || null == this.getChecksum() && null != sameClassObj.getChecksum()){
            return false;
        } else {
            if(null == this.getChecksum() && null == sameClassObj.getChecksum()){
                return this.getDuration() == sameClassObj.getDuration() && this.getFileSize() == sameClassObj.getFileSize();
            } else {
                return this.getChecksum().equals(sameClassObj.getChecksum()) && this.getDuration() == sameClassObj.getDuration() && this.getFileSize() == sameClassObj.getFileSize();
            }
        }
    }
    @Override
    public int hashCode() {
        int result = checksum != null ? checksum.hashCode() : 0;
        result = 31 * result +  duration;
        result = 31 * result + new Long(fileSize).intValue();
        return result;
    }
}
