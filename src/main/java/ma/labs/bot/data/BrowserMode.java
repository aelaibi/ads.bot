package ma.labs.bot.data;

/**
 * Created by Mohamed on 10/10/2016.
 */
public enum BrowserMode {
    DESKTOP("1"),
    MOBILE("2");

    private final String code;

    BrowserMode(String code){
        this.code = code;
    }

    public String getCode() {
        return code;
    }
}
