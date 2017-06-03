package ma.labs.bot.data.util;
/********************************************************
 *                                                      *
 *                    PIXITREND ROBOT                   *
 *                    BY labs GROUP                    *
 *                                                      *
 ********************************************************/

/**
 * Created by labs004 on 26/09/2016.
 */
public enum MediaType {
    IMAGE("1","image/png"),
    FLASH("2","application/x-shockwave-flash"),
    VIDEO("3","video/mp4"),
    FACEBOOK("4","text/html"),
    //5 n'existe pas :)
    HTML5("6","text/html");

    private final String code;



    private final String mimeType;

    MediaType(String c, String mime) {
        code=c;
        mimeType = mime;
    }

    public String getCode() {
        return code;
    }

    public String getMimeType() {
        return mimeType;
    }
}
