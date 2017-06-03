package ma.labs.bot.utils;
/********************************************************
 *                                                      *
 *                    PIXITREND ROBOT                   *
 *                    BY labs GROUP                    *
 *                                                      *
 ********************************************************/



import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Created by labs004 on 30/08/2016.
 */
public class VersionReader {

    private static final Properties properties;


    /** Use a static initializer to read from file. */
    static {
        InputStream inputStream = VersionReader.class.getResourceAsStream("/version.properties");
        properties = new Properties();
        try {
            properties.load(inputStream);

        } catch (IOException e) {
            throw new RuntimeException("Failed to read properties file", e);
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    // Ignore
                }
            }
        }
    }

    /** Hide default constructor. */
    private VersionReader() {}

    /**
     * Gets the Git SHA-1.
     * @return A {@code String} with the Git SHA-1.
     */
    public static String getVersion() {
        return properties.getProperty("version");
    }
}
