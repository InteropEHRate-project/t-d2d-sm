package iehr.security.dummy;


import iehr.security.CryptoManagementFactory;
import iehr.security.api.CryptoManagement;
import java.security.*;
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPublicKey;

public class Main {

    public static final String CA_URL = "http://interoperate-ejbca-service.euprojects.net";
    public static final String USER_ALIAS = "healthorganization";
    private static final String KEYSTORE_PATH = "keystore.p12";


    public static void main(String[] args) throws Exception {
        String payload = "{\"resourceType\":\"DiagnosticReport\",\"meta\":{\"profile\":[\"http://interopehrate.eu/fhir/StructureDefinition/DiagnosticReport-LaboratoryReport-IEHR\"]},\"language\":\"it\",\"extension\":[{\"url\":\"http://interopehrate.eu/fhir/StructureDefinition/ProvenanceExtension-IEHR\",\"valueReference\":{\"reference\":\"Provenance/09bcb9b6-4b35-4085-b4ce-fd4a08699e5f\"}}],\"category\":[{\"coding\":[{\"system\":\"http://terminology.hl7.org/CodeSystem/v2-0074\",\"code\":\"LAB\"}]}],\"code\":{\"coding\":[{\"system\":\"http://loinc.org\",\"code\":\"30954-2\",\"display\":\"Testdiagnosticirilevantie/odatidilaboratorio\"}]},\"subject\":{\"reference\":\"urn:uuid:da282632-e037-4b19-82b2-de2c73072cdd\"},\"effectiveDateTime\":\"2019-06-18T09:00:00+01:00\"}";

        CryptoManagement cryptoManagement = CryptoManagementFactory.create(CA_URL, KEYSTORE_PATH);

        //sign
        PrivateKey privateKey = cryptoManagement.getPrivateKey(USER_ALIAS);
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

        //create and verify JWS
        String jwsToken = cryptoManagement.createDetachedJws(certificateData, signed);
        System.out.println(jwsToken);
        Boolean verifyJws = cryptoManagement.verifyDetachedJws(jwsToken, payload);
        System.out.println("Verify Payload with Jws: " + verifyJws);

    }

}