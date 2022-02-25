package iehr.security.dummy;

import com.google.gson.Gson;
import iehr.security.CryptoManagementFactory;
import iehr.security.api.CryptoManagement;

import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.SignatureException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPublicKey;
import java.util.Base64;
import java.util.concurrent.ExecutionException;

public class Main {

    public static final String CA_URL = "http://interoperate-ejbca-service.euprojects.net";
    public static final String USER_ALIAS = "healthorganization";


    public static void main(String[] args) throws Exception {
        CryptoManagement cryptoManagement = CryptoManagementFactory.create(CA_URL);

        //sign
        PrivateKey privateKey = cryptoManagement.getPrivateKey(USER_ALIAS);
        String payload = "payload";
        String signed = cryptoManagement.signPayload(payload,privateKey);
        System.out.println("Sign " + signed);

        //validate
        byte[] certificateData = cryptoManagement.getUserCertificate(USER_ALIAS);
        Boolean isValid = cryptoManagement.validateUserCertificate(certificateData);
        System.out.println("Check if user certificate is valid: "+ isValid);

        //verify
        X509Certificate certificate = cryptoManagement.toX509Certificate(certificateData);
        RSAPublicKey rsaPublicKey = (RSAPublicKey)certificate.getPublicKey();
        Boolean verify = cryptoManagement.verifyPayload(rsaPublicKey,payload.getBytes(), signed.getBytes());
        System.out.println("Verify Payload: " + verify);
    }
}