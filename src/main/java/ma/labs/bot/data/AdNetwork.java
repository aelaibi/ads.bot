package ma.labs.bot.data;
/********************************************************
 *                                                      *
 *                    PIXITREND ROBOT                   *
 *                    BY labs GROUP                    *
 *                                                      *
 ********************************************************/

/**
 * Created by labs004 on 26/09/2016.
 */
public enum AdNetwork {
    BID("4"),
    ADSENSE("3"),
    DIRECT("2"),
    YOUTUBE("6"),
    FACEBOOK("1");

    private final String code;

    AdNetwork(String s) {
        code=s;
    }

    public String getCode() {
        return code;
    }
}
