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
public class ChecksumFileResponse extends IsNewMediaResponse{
    // IS_NEW_MEDIA , IS_NEW_VISUAL , ID_MEDIA_IF_NOT_NEW_MEDIA , , CHECKSUM_FILE , TEMPORARY_FILENAME_IN_MEDIA_FOLDER
    private String checksumFile;
    private String  tempFileName;

    private long fileSize;

    public ChecksumFileResponse() {
    }

    public ChecksumFileResponse(String isNewMedia, String isNewVisual, String idMedia, String x, String checksumFile, String tempFileName, long fileSize, String toBeFixed) {
        super(isNewMedia, isNewVisual, idMedia, x,toBeFixed);
        this.checksumFile = checksumFile;
        this.tempFileName = tempFileName;
        this.fileSize = fileSize;
    }

    public ChecksumFileResponse(IsNewMediaResponse isNew, String checksumFile, String tempFileName, long fileSize) {
        super(isNew.getIsNewMedia(), isNew.getIsNewVisual(), isNew.getIdMedia(), isNew.getIdVisual(), isNew.getToBeFixed());
        this.checksumFile = checksumFile;
        this.tempFileName = tempFileName;
        this.fileSize = fileSize;
    }

    public String getChecksumFile() {
        return checksumFile;
    }

    public void setChecksumFile(String checksumFile) {
        this.checksumFile = checksumFile;
    }

    public String getTempFileName() {
        return tempFileName;
    }

    public void setTempFileName(String tempFileName) {
        this.tempFileName = tempFileName;
    }


    public long getFileSize() {
        return fileSize;
    }

    public void setFileSize(long fileSize) {
        this.fileSize = fileSize;
    }

    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this);
    }
}
