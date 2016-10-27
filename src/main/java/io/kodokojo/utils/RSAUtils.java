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
package io.kodokojo.utils;

import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.util.io.pem.PemObject;
import org.bouncycastle.util.io.pem.PemReader;
import org.bouncycastle.util.io.pem.PemWriter;

import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.security.*;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.util.Objects.requireNonNull;
import static org.apache.commons.lang.StringUtils.isBlank;

public class RSAUtils {

    private static final String SSH_RSA = "ssh-rsa";

    private static final String RSA = "RSA";

    private static final String SHA_1_PRNG = "SHA1PRNG";

    private static final int KEY_SIZE = 2048;

    private static final String PUBLIC_KEY_OUTPUT = "%s %s %s";

    private static final String AES = "AES";

    private static final String AES_ECB_NO_PADDING = "AES";

    public static final String AES_ECB_PKCS5_PADDING = "AES/ECB/PKCS5Padding";

    private static final Pattern PRIVATE_PATTERN = Pattern.compile("(-----BEGIN RSA PRIVATE KEY-----\n.*\n-----END RSA PRIVATE KEY-----?)", Pattern.DOTALL);

    private static final Pattern PUBLIC_PATTERN = Pattern.compile("(-----BEGIN CERTIFICATE-----\n.*\n-----END CERTIFICATE-----)", Pattern.DOTALL);

    private static final byte[] IV_PARAMETERSPEC_BYTES = "0102030405060708".getBytes();//Todo Change this by another. (16 char required)
    public static final String AES_CBC_PKCS5_PADDING = "AES/CBC/PKCS5Padding";

    private RSAUtils() {
        // Utility Class
    }

    public static KeyPair generateRsaKeyPair() throws NoSuchAlgorithmException {
        Security.addProvider(new BouncyCastleProvider());
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance(RSA);
        keyPairGenerator.initialize(KEY_SIZE, SecureRandom.getInstance(SHA_1_PRNG));
        return keyPairGenerator.generateKeyPair();
    }

    public static byte[] wrap(Key aesKey, Key keyToEncrypt) {
        if (aesKey == null) {
            throw new IllegalArgumentException("aesKey must be defined.");
        }
        if (!AES.equals(aesKey.getAlgorithm())) {
            throw new IllegalArgumentException("aesKey must be an AES key instead of " + aesKey.getAlgorithm() + ".");
        }
        if (keyToEncrypt == null) {
            throw new IllegalArgumentException("keyToEncrypt must be defined.");
        }
        try {
            Cipher cipher = Cipher.getInstance(AES_ECB_NO_PADDING);
            cipher.init(Cipher.WRAP_MODE, aesKey);
            return cipher.wrap(keyToEncrypt);
        } catch (NoSuchAlgorithmException | IllegalBlockSizeException | InvalidKeyException | NoSuchPaddingException e) {
            throw new RuntimeException("Unable to wrap key", e);
        }
    }

    public static Key unwrap(Key aesKey, byte[] envelop) {
        if (aesKey == null) {
            throw new IllegalArgumentException("aesKey must be defined.");
        }
        if (!AES.equals(aesKey.getAlgorithm())) {
            throw new IllegalArgumentException("aesKey must be an AES key.");
        }

        try {
            Cipher cipher = Cipher.getInstance(AES_ECB_NO_PADDING);
            cipher.init(Cipher.UNWRAP_MODE, aesKey);
            return cipher.unwrap(envelop, AES, Cipher.SECRET_KEY);
        } catch (NoSuchAlgorithmException | InvalidKeyException | NoSuchPaddingException e) {
            throw new RuntimeException("Unable to unwrap private key.", e);
        }
    }

    public static RSAPrivateKey unwrapPrivateRsaKey(Key aesKey, byte[] envelop) {
        if (aesKey == null) {
            throw new IllegalArgumentException("aesKey must be defined.");
        }
        if (!AES.equals(aesKey.getAlgorithm())) {
            throw new IllegalArgumentException("aesKey must be an AES key.");
        }
        Key unwrap = unwrap(aesKey, envelop);
        try {
            KeyFactory keyFactory = KeyFactory.getInstance(RSA);
            return (RSAPrivateKey) keyFactory.generatePrivate(new PKCS8EncodedKeySpec(unwrap.getEncoded()));
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new RuntimeException("Unable to unwrap private key.", e);
        }
    }

    public static RSAPublicKey unwrapPublicRsaKey(Key aesKey, byte[] envelop) {
        if (aesKey == null) {
            throw new IllegalArgumentException("aesKey must be defined.");
        }
        if (!AES.equals(aesKey.getAlgorithm())) {
            throw new IllegalArgumentException("aesKey must be an AES key.");
        }
        Key unwrap = unwrap(aesKey, envelop);
        try {
            KeyFactory keyFactory = KeyFactory.getInstance(RSA);
            return (RSAPublicKey) keyFactory.generatePublic(new X509EncodedKeySpec(unwrap.getEncoded()));
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new RuntimeException("Unable to unwrap public key.", e);
        }
    }

    public static void writeRsaPrivateKey(RSAPrivateKey privateKey, Writer sw) {
        try {
            PemWriter writer = new PemWriter(sw);
            writer.writeObject(new PemObject("RSA PRIVATE KEY", privateKey.getEncoded()));
            writer.flush();
            //outputStream.write(pkcs8EncodedKeySpec.getEncoded());

        } catch (IOException e) {
            throw new RuntimeException("Unable to write a private rsa key in output stream", e);
        }
    }

    public static RSAPrivateKey readRsaPrivateKey(Reader reader) {
        Security.addProvider(new BouncyCastleProvider());
        try {
            KeyFactory factory = KeyFactory.getInstance("RSA", "BC");

            PemReader pemReader = new PemReader(reader);
            PemObject privatePem = pemReader.readPemObject();
            PKCS8EncodedKeySpec privateSpec = new PKCS8EncodedKeySpec(privatePem.getContent());
            RSAPrivateKey privateKey = (RSAPrivateKey) factory.generatePrivate(privateSpec);
            return privateKey;
        } catch (IOException | NoSuchAlgorithmException | InvalidKeySpecException | NoSuchProviderException e) {
            throw new RuntimeException("Unable to extract private RAS Key .", e);
        }
    }

    public static X509Certificate readRsaPublicKey(Reader reader) {
        Security.addProvider(new BouncyCastleProvider());
        try {
            PEMParser pemParser = new PEMParser(reader);
            X509CertificateHolder cert = (X509CertificateHolder) pemParser.readObject();
            JcaX509CertificateConverter certificateConverter = new JcaX509CertificateConverter();
            return certificateConverter.getCertificate(cert);
        } catch (IOException | CertificateException e) {
            throw new RuntimeException("Unable to extract public RAS Key .", e);
        }
    }

    public static String encodePublicKey(RSAPublicKey rsaPublicKey, String userEmail) {
        if (rsaPublicKey == null) {
            throw new IllegalArgumentException("rsaPublicKey must be defined.");
        }
        if (isBlank(userEmail)) {
            throw new IllegalArgumentException("userEmail must be defined.");
        }
        ByteArrayOutputStream byteOs = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(byteOs);
        try {
            dos.writeInt(SSH_RSA.getBytes().length);
            dos.write(SSH_RSA.getBytes());
            dos.writeInt(rsaPublicKey.getPublicExponent().toByteArray().length);
            dos.write(rsaPublicKey.getPublicExponent().toByteArray());
            dos.writeInt(rsaPublicKey.getModulus().toByteArray().length);
            dos.write(rsaPublicKey.getModulus().toByteArray());
            String publicKeyEncoded = new String(Base64.getEncoder().encode(byteOs.toByteArray()));
            return String.format(PUBLIC_KEY_OUTPUT, SSH_RSA, publicKeyEncoded, userEmail);
        } catch (IOException e) {
            throw new RuntimeException("Unable to write un a memory DataOutputStream.", e);
        }
    }

    public static String encodedPrivateKey(PrivateKey key) {
        requireNonNull(key, "key must be defined.");
        String encodedKey = Base64.getEncoder().encodeToString(key.getEncoded());
        encodedKey = encodedKey.replaceAll("(.{64})", "$1\n");
        String privateKeyContent = String.format("-----BEGIN RSA PRIVATE KEY-----\n%s\n-----END RSA PRIVATE KEY-----", encodedKey);
        return privateKeyContent;
    }


    public static String extractPrivateKey(String content) {
        if (content == null) {
            throw new IllegalArgumentException("content must be defined.");
        }
        Matcher matcher = PRIVATE_PATTERN.matcher(content);
        return matcher.find() ? matcher.group(1) : null;
    }

    public static String extractPublic(String content) {
        if (content == null) {
            throw new IllegalArgumentException("content must be defined.");
        }
        Matcher matcher = PUBLIC_PATTERN.matcher(content);
        return matcher.find() ? matcher.group(1) : null;
    }


    public static byte[] encryptWithAES(Key key, String data) {
        try {
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.ENCRYPT_MODE, key);
            return cipher.doFinal(data.getBytes());
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | BadPaddingException | IllegalBlockSizeException e) {
            throw new RuntimeException("Unable to create Cipher", e);
        }
    }

    public static String decryptWithAES(Key key, byte[] encrypted) {
        try {
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.DECRYPT_MODE, key);
            return new String(cipher.doFinal(encrypted));
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | BadPaddingException | IllegalBlockSizeException e) {
            throw new RuntimeException("Unable to create Cipher", e);
        }
    }

    public static byte[] encryptObjectWithAES(Key key, Serializable data) {
        requireNonNull(key, "key must be defined.");
        if (!AES.equals(key.getAlgorithm())) {
            throw new IllegalArgumentException("key must be an AES key, not a  " + key.getAlgorithm() + " .");
        }
        requireNonNull(data, "data must be defined.");
        try {
            IvParameterSpec iv = new IvParameterSpec(IV_PARAMETERSPEC_BYTES);
            Cipher cipher = Cipher.getInstance(AES_CBC_PKCS5_PADDING);
            cipher.init(Cipher.ENCRYPT_MODE, key, iv);
            try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
                SealedObject sealedObject = new SealedObject(data, cipher);
                CipherOutputStream cipherOutputStream = new CipherOutputStream(out, cipher);
                ObjectOutputStream objectOutputStream = new ObjectOutputStream(cipherOutputStream);
                objectOutputStream.writeObject(sealedObject);
                objectOutputStream.close();
                return out.toByteArray();
            }
        } catch (InvalidAlgorithmParameterException | NoSuchPaddingException | InvalidKeyException | IllegalBlockSizeException | IOException | NoSuchAlgorithmException e) {
            throw new RuntimeException("Unable to create Cipher", e);
        }
    }

    public static Serializable decryptObjectWithAES(Key key, byte[] encrypted) {
        requireNonNull(key, "key must be defined.");
        if (!key.getAlgorithm().equals("AES")) {
            throw new IllegalArgumentException("key must be an AES key, not a  " + key.getAlgorithm() + " .");
        }
        requireNonNull(encrypted, "encrypted must be defined.");
        try {
            IvParameterSpec iv = new IvParameterSpec(IV_PARAMETERSPEC_BYTES);
            SecretKeySpec spec = new SecretKeySpec(key.getEncoded(), AES);
            Cipher cipher = Cipher.getInstance(AES_CBC_PKCS5_PADDING);
            cipher.init(Cipher.DECRYPT_MODE, spec, iv);
            ByteArrayInputStream in = new ByteArrayInputStream(encrypted);
            CipherInputStream cipherInputStream = new CipherInputStream(in, cipher);
            ObjectInputStream objectInputStream = new ObjectInputStream(cipherInputStream);
            SealedObject sealedObject = (SealedObject) objectInputStream.readObject();
            return (Serializable) sealedObject.getObject(cipher);
        } catch (InvalidAlgorithmParameterException | NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | BadPaddingException | IllegalBlockSizeException | IOException e) {
            throw new RuntimeException("Unable to create Cipher", e);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Unable to found SealedObject class", e);
        }
    }


}
