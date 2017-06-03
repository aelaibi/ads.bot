package ma.labs.bot.data;
/********************************************************
 *                                                      *
 *                    PIXITREND ROBOT                   *
 *                    BY labs GROUP                    *
 *                                                      *
 ********************************************************/

import com.google.gson.annotations.SerializedName;

/**
 * Created by labs004 on 03/08/2016.
 */
public class ProfileInfo {

    @SerializedName("ZIPNAME")
    private String ZIPNAME;
    @SerializedName("URL")
    private String URL;
    @SerializedName("ID")
    private String ID;

    public ProfileInfo() {
    }

    public ProfileInfo(String ZIPNAME, String URL, String ID) {
        this.ZIPNAME = ZIPNAME;
        this.URL = URL;
        this.ID = ID;
    }

    public String getZIPNAME() {
        return ZIPNAME;
    }

    public void setZIPNAME(String ZIPNAME) {
        this.ZIPNAME = ZIPNAME;
    }

    public String getURL() {
        return URL;
    }

    public void setURL(String URL) {
        this.URL = URL;
    }

    public String getID() {
        return ID;
    }

    public void setID(String ID) {
        this.ID = ID;
    }
}
