package ma.labs.bot.utils.nio;
/********************************************************
 *                                                      *
 *                    PIXITREND ROBOT                   *
 *                    BY labs GROUP                    *
 *                                                      *
 ********************************************************/

import ma.labs.bot.utils.Utils;
import org.apache.http.ProtocolException;
import org.apache.http.impl.client.DefaultRedirectStrategy;

import java.net.URI;

/**
 * Created by labs004 on 08/08/2016.
 */
public class DefaultRedirectStrategyImpl extends DefaultRedirectStrategy {
    @Override
    protected URI createLocationURI(String location) throws ProtocolException {
        location = Utils.gatValidUriString(location);

        return super.createLocationURI(location);
    }
}
