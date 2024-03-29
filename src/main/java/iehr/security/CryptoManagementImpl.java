package iehr.security;

import com.fasterxml.jackson.core.JsonProcessingException;
import eu.interopehrate.security_commons.encryptedCommunication.EncryptedCommunicationFactory;
import eu.interopehrate.security_commons.services.ca.CAServiceFactory;
import eu.interopehrate.security_commons.services.ca.api.CAService;
import eu.interopehrate.security_commons.encryptedCommunication.api.EncryptedCommunication;
import iehr.security.api.CryptoManagement;

import javax.crypto.KeyAgreement;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.io.InputStream;
import java.security.*;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPublicKey;
import java.util.Base64;
import java.util.concurrent.ExecutionException;

import static java.nio.charset.StandardCharsets.UTF_8;

public class CryptoManagementImpl implements CryptoManagement {

    private static final String KEYSTORE_PASSWORD = "interop";


    private final CAService ca;
    private final EncryptedCommunication encryptedCommunication;
    private final String keystorePath;

    public CryptoManagementImpl(String caUrl, String keystorePath) {
        ca = CAServiceFactory.create(caUrl);
        encryptedCommunication = EncryptedCommunicationFactory.create();
        this.keystorePath = keystorePath;
    }

    @Override
    public PrivateKey getPrivateKey(String alias) throws KeyStoreException, IOException, CertificateException, NoSuchAlgorithmException, UnrecoverableKeyException {
        char[] password = KEYSTORE_PASSWORD.toCharArray();
        KeyStore keyStore=KeyStore.getInstance("PKCS12");
        InputStream stream = this.getClass().getClassLoader().getResourceAsStream(keystorePath);
        keyStore.load(stream,password);
        PrivateKey key = (PrivateKey)keyStore.getKey(alias, password);
        return (PrivateKey) key;
    }

    @Override
    public RSAPublicKey getPublicKey(String alias) throws KeyStoreException, IOException, CertificateException, NoSuchAlgorithmException {
        char[] password = KEYSTORE_PASSWORD.toCharArray();

        // Reload the keystore
        KeyStore keyStore = KeyStore.getInstance("PKCS12");
        InputStream stream = this.getClass().getClassLoader().getResourceAsStream(keystorePath);
        keyStore.load(stream,password);
        java.security.cert.Certificate cert = keyStore.getCertificate(alias);
        RSAPublicKey pkey = (RSAPublicKey)cert.getPublicKey();
        return pkey;
    }

    @Override
    public String signPayload(String payload, PrivateKey privateKey) throws InvalidKeyException, SignatureException {
            Signature privateSignature = null;
            try {
                privateSignature = Signature.getInstance("SHA256withRSA");
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            }
            privateSignature.initSign(privateKey);
            privateSignature.update(payload.getBytes(UTF_8));
            byte[] signature = privateSignature.sign();

            return Base64.getEncoder().encodeToString(signature);
    }

    @Override
    public boolean verifyPayload(RSAPublicKey publicKey, byte[] payload, byte[] sign) throws NoSuchAlgorithmException, InvalidKeyException, SignatureException {
            byte[] signedPayloadContent = Base64.getDecoder().decode(sign);
            Signature signature = Signature.getInstance("SHA256withRSA");
            signature.initVerify(publicKey);
            signature.update(payload);
            boolean result = signature.verify(signedPayloadContent);
            System.out.println("MSSG verifySignature -> " + String.valueOf(result));
            return result;
    }

    @Override
    public byte[] getUserCertificate(String userAlias) throws IOException, ExecutionException, InterruptedException {
        return ca.getUserCertificate(userAlias);
    }

    @Override
    public Boolean validateUserCertificate(byte[] certificateData) throws IOException, ExecutionException, InterruptedException {
        return ca.validateUserCertificate(certificateData);
    }

    @Override
    public X509Certificate toX509Certificate(byte[] certificateData) throws CertificateException {
        return ca.toX509Certificate(certificateData);
    }

    @Override
    public String decrypt(String encryptedPayload, String symKey) throws Exception {
        return encryptedCommunication.decrypt(encryptedPayload, symKey);
    }

    @Override
    public String decryptb(byte[] encryptedPayload, String symKey) throws Exception {
        return encryptedCommunication.decryptb(encryptedPayload, symKey);
    }

    @Override
    public String encrypt(String payload, String symKey) throws Exception {
        return encryptedCommunication.encrypt(payload,symKey);
    }

    @Override
    public byte[] encryptb(String payload, String symKey) throws Exception {
        return encryptedCommunication.encryptb(payload, symKey);
    }

    @Override
    public String generateSymmtericKey() throws NoSuchAlgorithmException {
        return encryptedCommunication.generateSymmtericKey();
    }

    @Override
    public KeyPair aliceInitKeyPair() throws Exception {
        return encryptedCommunication.aliceInitKeyPair();
    }

    @Override
    public KeyAgreement aliceKeyAgreement(KeyPair aliceKpair) throws Exception {
        return encryptedCommunication.aliceKeyAgreement(aliceKpair);
    }

    @Override
    public byte[] alicePubKeyEnc(KeyPair aliceKpair) throws Exception {
        return encryptedCommunication.alicePubKeyEnc(aliceKpair);
    }

    @Override
    public KeyPair bobInitKeyPair(byte[] alicePubKeyEnc) throws Exception {
        return encryptedCommunication.bobInitKeyPair(alicePubKeyEnc);
    }

    @Override
    public KeyAgreement bobKeyAgreement(KeyPair bobKpair) throws Exception {
        return encryptedCommunication.bobKeyAgreement(bobKpair);
    }

    @Override
    public byte[] bobPubKeyEnc(KeyPair bobKpair) throws Exception {
        return encryptedCommunication.bobPubKeyEnc(bobKpair);
    }

    @Override
    public KeyAgreement aliceKeyAgreementFin(byte[] bobPubKeyEnc, KeyAgreement aliceKeyAgree) throws Exception {
        return encryptedCommunication.aliceKeyAgreementFin(bobPubKeyEnc,aliceKeyAgree);
    }

    @Override
    public KeyAgreement bobKeyAgreementFin(byte[] alicePubKeyEnc, KeyAgreement bobKeyAgree) throws Exception {
        return encryptedCommunication.bobKeyAgreementFin(alicePubKeyEnc, bobKeyAgree);
    }

    @Override
    public SecretKeySpec generateSymmtericKey(byte[] sharedSecret, int size) {
        return encryptedCommunication.generateSymmtericKey(sharedSecret, size);
    }

    @Override
    public String createDetachedJws(byte[] certificateData, String signed) throws JsonProcessingException {
        return ca.createDetachedJws(certificateData, signed);
    }

    @Override
    public Boolean verifyDetachedJws(String jwsToken, String payload) throws CertificateException, JsonProcessingException, NoSuchAlgorithmException, InvalidKeyException, SignatureException {
        RSAPublicKey rsaPublicKey = (RSAPublicKey)ca.getPublicKeyFromJws(jwsToken);
        String signed = ca.getSignatureFromJws(jwsToken);
        return verifyPayload(rsaPublicKey,payload.getBytes(), signed.getBytes());
    }

}
