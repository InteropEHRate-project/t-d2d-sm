package iehr.security;

import iehr.security.api.CryptoManagement;

public class CryptoManagementFactory {
    private CryptoManagementFactory() {}

    /**
     * Factory method for creating an instance of CryptoManagementFactory
     *
     * @return
     */
    public static CryptoManagement create(final String caUrl, final String keystorePath) {
        return new CryptoManagementImpl(caUrl, keystorePath);
    }
}
