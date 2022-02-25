package iehr.security;

import iehr.security.api.CryptoManagement;
import org.junit.Test;

import javax.crypto.KeyAgreement;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.security.*;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPublicKey;
import java.util.Base64;
import java.util.concurrent.ExecutionException;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class CryptoManagementTest {

    public static final String CA_URL = "http://212.101.173.84:8071";

    @Test
    public void testCA() {
    }

}
