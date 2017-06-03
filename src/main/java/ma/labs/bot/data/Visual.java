package ma.labs.bot.data;

/**
 * Created by Mohamed on 26/12/2016.
 */
public class Visual {
    private String country;
    private String zone;
    private String idVisual;

    public Visual(String country, String zone, String idVisual) {
        this.country = country;
        this.zone = zone;
        this.idVisual = idVisual;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getZone() {
        return zone;
    }

    public void setZone(String zone) {
        this.zone = zone;
    }

    public String getIdVisual() {
        return idVisual;
    }

    public void setIdVisual(String idVisual) {
        this.idVisual = idVisual;
    }
}
