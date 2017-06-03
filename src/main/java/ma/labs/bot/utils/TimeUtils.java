package ma.labs.bot.utils;
/********************************************************
 *                                                      *
 *                    PIXITREND ROBOT                   *
 *                    BY labs GROUP                    *
 *                                                      *
 ********************************************************/

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by labs004 on 26/09/2016.
 */
public abstract class TimeUtils {
    private TimeUtils(){}

    private static final Logger logger = LoggerFactory.getLogger(TimeUtils.class);

    public static final String IGNORE_WAIT = "IGNORE_WAIT";

    public static void waitFor(int wait) {
        if(System.getProperty(IGNORE_WAIT)!=null)
            return;
        try {
            Thread.sleep(wait);
        } catch (InterruptedException e) {
            logger.error(e.getMessage());
            throw new Error(e);
        }
    }

    public static long countSecondsFrom(long begin) {
        return (System.currentTimeMillis() - begin)/1000;
    }
    public static long countMSFrom(long begin) {
        return (System.currentTimeMillis() - begin);
    }

    public static String now(){
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date now = Calendar.getInstance().getTime();
        String reportDate = df.format(now);
        return reportDate;
    }
}
