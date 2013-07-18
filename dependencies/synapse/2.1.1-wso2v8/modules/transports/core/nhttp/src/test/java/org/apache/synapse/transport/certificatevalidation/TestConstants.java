package org.apache.synapse.transport.certificatevalidation;

public interface TestConstants {

    //Validity period of a fake certificate made. 1 day (in milliseconds)
    final static int VALIDITY_PERIOD = 24 * 60 * 60 * 1000;
    //Next update for OCSPResponse or X509CRL will be after Now + NEXT_UPDATE_PERIOD
    final static int NEXT_UPDATE_PERIOD = 1000000;

    /**
     * The certificates in the resources folder will contain the certificates in the certificate chain from
     * https://www.github.com
     * These certificates are chosen because the certificate issuers support both CRL and OCSP. Read the certificates for
     * more details.
     *
     * CAUTION: Replace the certificates if they expire or are marked as revoked by their issuers. At the moment they are
     * valid. The expiry dates of the certificates are as follows:
     *
     * github.com                    : 09/02/2015
     * DigiCertHighAssuranceEVCA-1   : 11/10/2021
     * DigiCertHighAssuranceEVRootCA : 11/10/2031
     */
    final static String REAL_PEER_CERT = "/org/apache/synapse/transport/certificatevalidation" +
            "/certificates/github/github.com";
    final static String INTERMEDIATE_CERT = "/org/apache/synapse/transport/certificatevalidation" +
            "/certificates/github/DigiCertHighAssuranceEVCA-1";
    final static String ROOT_CERT = "/org/apache/synapse/transport/certificatevalidation" +
            "/certificates/github/DigiCertHighAssuranceEVRootCA";
}
