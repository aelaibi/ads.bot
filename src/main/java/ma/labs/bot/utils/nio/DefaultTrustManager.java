package ma.labs.bot.utils.nio;
/********************************************************
 *                                                      *
 *                    PIXITREND ROBOT                   *
 *                    BY labs GROUP                    *
 *                                                      *
 ********************************************************/

import javax.net.ssl.X509TrustManager;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

/**
 * Created by labs004 on 08/08/2016.
 */
public class DefaultTrustManager implements X509TrustManager {

    @Override
    public void checkClientTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {

    }

    @Override
    public void checkServerTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {

    }

    @Override
    public X509Certificate[] getAcceptedIssuers() {
        return null;
    }
}
