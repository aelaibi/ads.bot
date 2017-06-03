package ma.labs.bot.data;
/********************************************************
 *                                                      *
 *                    PIXITREND ROBOT                   *
 *                    BY labs GROUP                    *
 *                                                      *
 ********************************************************/

import org.apache.commons.lang3.builder.ReflectionToStringBuilder;

/**
 * Created by labs004 on 18/07/2016.
 */
public class Robot {


    private String name;
    private String city;
    private String country;
    private String isFB;

    private int firefoxProxyEnable;
    private String firefoxProxy;


    private int profileID;
    private String firefoxFile;
    private int runYoutubeAfter;
    private int deviceId;//default is desktop
    private String deviceName;
    private int deviceWidth;
    private int deviceHeight;
    private String deviceUserAgent;


    public BrowserMode getBrowserMode(){
        if(this.deviceId==0)
            throw new RuntimeException("device ID must not be 0");
        return   (this.deviceId == 1)?BrowserMode.DESKTOP :BrowserMode.MOBILE;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getIsFB() {
        return isFB;
    }

    public void setIsFB(String isFB) {
        this.isFB = isFB;
    }


    public String getFirefoxFile() {
        return firefoxFile;
    }

    public void setFirefoxFile(String firefoxFile) {
        this.firefoxFile = firefoxFile;
    }


    public int getRunYoutubeAfter() {
        return runYoutubeAfter;
    }

    public void setRunYoutubeAfter(int runYoutubeAfter) {
        this.runYoutubeAfter = runYoutubeAfter;
    }

    public int getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(int deviceId) {
        this.deviceId = deviceId;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    public int getDeviceWidth() {
        return deviceWidth;
    }

    public void setDeviceWidth(int deviceWidth) {
        this.deviceWidth = deviceWidth;
    }

    public int getDeviceHeight() {
        return deviceHeight;
    }

    public void setDeviceHeight(int deviceHeight) {
        this.deviceHeight = deviceHeight;
    }

    public String getDeviceUserAgent() {
        return deviceUserAgent;
    }

    public void setDeviceUserAgent(String deviceUserAgentValue) {
        this.deviceUserAgent = deviceUserAgentValue;
    }

    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this);
    }



    public boolean isFirefoxProxyEnable() {
        return 1==firefoxProxyEnable;
    }

    public void setFirefoxProxyEnable(int firefoxProxyEnable) {
        this.firefoxProxyEnable = firefoxProxyEnable;
    }

    public String getFirefoxProxy() {
        return firefoxProxy;
    }

    public void setFirefoxProxy(String firefoxProxy) {
        this.firefoxProxy = firefoxProxy;
    }

    public int getProfileID() {
        return profileID;
    }

    public void setProfileID(int profileID) {
        this.profileID = profileID;
    }

}
