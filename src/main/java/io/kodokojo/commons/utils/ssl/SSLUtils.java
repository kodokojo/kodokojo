/**
 * Kodo Kojo - Software factory done right
 * Copyright © 2016 Kodo Kojo (infos@kodokojo.io)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package io.kodokojo.commons.utils.ssl;

/*
 * #%L
 * kodokojo-commons
 * %%
 * Copyright (C) 2016 Kodo-kojo
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */

import io.kodokojo.commons.utils.RSAUtils;
import org.bouncycastle.asn1.ASN1EncodableVector;
import org.bouncycastle.asn1.DERSequence;
import org.bouncycastle.asn1.x509.BasicConstraints;
import org.bouncycastle.asn1.x509.Extension;
import org.bouncycastle.asn1.x509.KeyPurposeId;
import org.bouncycastle.asn1.x509.KeyUsage;
import org.bouncycastle.cert.CertIOException;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cert.jcajce.JcaX509ExtensionUtils;
import org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.bouncycastle.util.io.pem.PemObject;
import org.bouncycastle.util.io.pem.PemWriter;

import java.io.IOException;
import java.io.Writer;
import java.math.BigInteger;
import java.security.*;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static org.bouncycastle.asn1.x509.Extension.basicConstraints;
import static org.bouncycastle.asn1.x509.Extension.subjectKeyIdentifier;
import static org.bouncycastle.asn1.x509.KeyUsage.cRLSign;
import static org.bouncycastle.asn1.x509.KeyUsage.dataEncipherment;
import static org.bouncycastle.asn1.x509.KeyUsage.digitalSignature;
import static org.bouncycastle.asn1.x509.KeyUsage.keyCertSign;
import static org.bouncycastle.asn1.x509.KeyUsage.keyEncipherment;

public class SSLUtils {

    private static final String PROVIDER_NAME = BouncyCastleProvider.PROVIDER_NAME;

    static {
        Security.addProvider(new BouncyCastleProvider());
    }

    private static final String SIGNATURE_ALGORITHM = "SHA256WithRSAEncryption";

    private static final long DEFAULT_CERTIFICATE_DURATION_VALIDITY = TimeUnit.DAYS.toMillis(3 * 31);

    private static final String COMMON_NAME_ENTRY = "CN=";

    private SSLUtils() {
        //  Nothing to do.
    }

    public static SSLKeyPair createSSLKeyPair(String commonsName, PrivateKey caPrivateKey, PublicKey caPublicKey, X509Certificate[] issuerCertificateChain, long duration, boolean isCaCertificate) {

        try {
            KeyPair keyPair = RSAUtils.generateRsaKeyPair();
            RSAPrivateKey privateKey = (RSAPrivateKey) keyPair.getPrivate();
            RSAPublicKey publicKey = (RSAPublicKey) keyPair.getPublic();

            JcaX509v3CertificateBuilder certificateBuilder = addJcaX509Extension(commonsName, publicKey, issuerCertificateChain[0], duration, isCaCertificate);

            if (isCaCertificate) {
                addASN1AndKeyUsageExtensions(certificateBuilder);
            }

            X509Certificate cert = verifyCertificate(caPrivateKey, caPublicKey, certificateBuilder);

            List<X509Certificate> x509Certificates = new ArrayList<>(Arrays.asList(issuerCertificateChain));
            x509Certificates.add(0, cert);
            return new SSLKeyPair(privateKey, publicKey, x509Certificates.toArray(new X509Certificate[x509Certificates.size()]));

        } catch (NoSuchAlgorithmException | CertIOException | CertificateException | InvalidKeyException | OperatorCreationException | SignatureException | NoSuchProviderException e) {
            throw new RuntimeException("Unable to generate SSL certificate for " + commonsName, e);
        }
    }

    private static JcaX509v3CertificateBuilder addJcaX509Extension(String commonsName, RSAPublicKey publicKey, X509Certificate issuerCertificate, long duration, boolean isCaCertificate) throws NoSuchAlgorithmException, CertIOException {
        long end = System.currentTimeMillis() + duration;

        BigInteger serial = BigInteger.valueOf(new SecureRandom(publicKey.getEncoded()).nextLong());

        JcaX509v3CertificateBuilder certificateBuilder = new JcaX509v3CertificateBuilder(new org.bouncycastle.asn1.x500.X500Name(issuerCertificate.getSubjectDN().getName()), serial, new Date(), new Date(end), new org.bouncycastle.asn1.x500.X500Name(COMMON_NAME_ENTRY + commonsName), publicKey);
        JcaX509ExtensionUtils jcaX509ExtensionUtils = new JcaX509ExtensionUtils();
        certificateBuilder.addExtension(subjectKeyIdentifier, false, jcaX509ExtensionUtils.createSubjectKeyIdentifier(publicKey));
        certificateBuilder.addExtension(basicConstraints, isCaCertificate, new BasicConstraints(isCaCertificate));

        return certificateBuilder;
    }

    public static SSLKeyPair createSelfSignedSSLKeyPair(String commonsName, RSAPrivateKey caPrivateKey, RSAPublicKey caPublicKey) {

        try {
            BigInteger serial = BigInteger.valueOf(new Random().nextInt());
            long end = System.currentTimeMillis() + DEFAULT_CERTIFICATE_DURATION_VALIDITY;

            org.bouncycastle.asn1.x500.X500Name commonsX500Name = new org.bouncycastle.asn1.x500.X500Name(COMMON_NAME_ENTRY + commonsName);
            JcaX509v3CertificateBuilder certificateBuilder = new JcaX509v3CertificateBuilder(commonsX500Name, serial, new Date(), new Date(end), commonsX500Name, caPublicKey);
            JcaX509ExtensionUtils jcaX509ExtensionUtils = new JcaX509ExtensionUtils();
            certificateBuilder.addExtension(subjectKeyIdentifier, false, jcaX509ExtensionUtils.createSubjectKeyIdentifier(caPublicKey));

            certificateBuilder.addExtension(basicConstraints, true, new BasicConstraints(true));

            addASN1AndKeyUsageExtensions(certificateBuilder);

            X509Certificate cert = verifyCertificate(caPrivateKey, caPublicKey, certificateBuilder);

            return new SSLKeyPair(caPrivateKey, caPublicKey, new X509Certificate[]{cert});

        } catch (NoSuchAlgorithmException | CertIOException | CertificateException | InvalidKeyException | OperatorCreationException | SignatureException | NoSuchProviderException e) {
            throw new RuntimeException("Unable to generate SSL certificate for " + commonsName, e);
        }
    }

    private static void addASN1AndKeyUsageExtensions(JcaX509v3CertificateBuilder certificateBuilder) throws CertIOException {
        ASN1EncodableVector purposes = new ASN1EncodableVector();
        purposes.add(KeyPurposeId.id_kp_serverAuth);
        purposes.add(KeyPurposeId.id_kp_clientAuth);
        purposes.add(KeyPurposeId.anyExtendedKeyUsage);
        certificateBuilder.addExtension(Extension.extendedKeyUsage, false, new DERSequence(purposes));

        KeyUsage keyUsage = new KeyUsage(keyCertSign | digitalSignature | keyEncipherment | dataEncipherment | cRLSign);
        certificateBuilder.addExtension(Extension.keyUsage, false, keyUsage);
    }

    private static X509Certificate verifyCertificate(PrivateKey caPrivateKey, PublicKey caPublicKey, JcaX509v3CertificateBuilder certificateBuilder) throws OperatorCreationException, CertificateException, NoSuchAlgorithmException, InvalidKeyException, NoSuchProviderException, SignatureException {
        ContentSigner signer = new JcaContentSignerBuilder(SIGNATURE_ALGORITHM).setProvider(PROVIDER_NAME).build(caPrivateKey);
        X509Certificate cert = new JcaX509CertificateConverter().setProvider(PROVIDER_NAME).getCertificate(certificateBuilder.build(signer));
        cert.checkValidity(new Date());
        cert.verify(caPublicKey);
        return cert;
    }

    public static SSLKeyPair createSSLKeyPair(String commonsName, PrivateKey caPrivateKey, PublicKey caPublicKey, X509Certificate[] issuerCertificateChain) {
        return createSSLKeyPair(commonsName, caPrivateKey, caPublicKey, issuerCertificateChain, DEFAULT_CERTIFICATE_DURATION_VALIDITY, false);
    }

    public static void writeSSLKeyPairPem(SSLKeyPair sslKeyPair, Writer writer) throws IOException {
        for (X509Certificate certificate : sslKeyPair.getCertificates()) {
            writeX509Certificate(certificate, writer);
        }
        RSAUtils.writeRsaPrivateKey(sslKeyPair.getPrivateKey(), writer);
        writer.flush();
    }

    private static void writeX509Certificate(X509Certificate certificate, Writer sw) {
        if (certificate == null) {
            throw new IllegalArgumentException("certificate must be defined.");
        }
        if (sw == null) {
            throw new IllegalArgumentException("sw must be defined.");
        }
        try {
            PemWriter writer = new PemWriter(sw);
            writer.writeObject(new PemObject("CERTIFICATE", certificate.getEncoded()));
            writer.flush();
        } catch (CertificateEncodingException | IOException e) {
            throw new RuntimeException("Unable to write a certificate in output stream", e);
        }
    }

}
