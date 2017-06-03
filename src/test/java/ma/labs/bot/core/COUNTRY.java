package ma.labs.bot.core;
/********************************************************
 *                                                      *
 *                    PIXITREND ROBOT                   *
 *                    BY labs GROUP                    *
 *                                                      *
 ********************************************************/

/**
 * Created by labs004 on 06/03/2017.
 */
public enum COUNTRY {
    MA("casablanca"), // ID 1
    LEB("beirut"), // ID 2
    UAE("sharjah"), // ID 3
    KSA("jeddah"), // ID 4
    KWT("hawalli"), // ID 5
    JOR("amman"), // ID 7
    EGY("cairo") // ID 8
    ;
    String city;

    COUNTRY(String city) {
        this.city = city;
    }
}
