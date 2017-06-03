package ma.labs.bot.data;

import com.google.gson.annotations.SerializedName;

/**
 * Created by labs004 on 11/07/2016.
 */
public class Page {
    @SerializedName("ID")
    private int id;
    @SerializedName("URL")
    private String url;
    @SerializedName("SELECTORCSS")
    private String selectorCss;
    @SerializedName("SELECTORXPATH")
    private String selectorXPath;
    @SerializedName("XPATHLANDINGPAGE")
    private String xPathLandingPage;


    public Page() {
    }

    public Page(int id, String url, String selectorCss, String selectorXPath, String xPathLandingPage) {
        this.id = id;
        this.url = url;
        this.selectorCss = selectorCss;
        this.selectorXPath = selectorXPath;
        this.xPathLandingPage = xPathLandingPage;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getSelectorCss() {
        return selectorCss;
    }

    public void setSelectorCss(String selectorCss) {
        this.selectorCss = selectorCss;
    }

    public String getSelectorXPath() {
        return selectorXPath;
    }

    public void setSelectorXPath(String selectorXPath) {
        this.selectorXPath = selectorXPath;
    }

    public String getxPathLandingPage() {
        return xPathLandingPage;
    }

    public void setxPathLandingPage(String xPathLandingPage) {
        this.xPathLandingPage = xPathLandingPage;
    }
}
