package io.kodokojo.entrypoint;

/*
 * #%L
 * project-manager
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

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.kodokojo.commons.project.model.User;
import io.kodokojo.commons.utils.RSAUtils;
import io.kodokojo.user.SimpleCredential;
import io.kodokojo.user.UserAuthenticator;
import io.kodokojo.user.UserCreationDto;
import io.kodokojo.user.UserManager;
import io.kodokojo.user.redis.RedisUserManager;
import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spark.Request;
import spark.Response;
import spark.ResponseTransformer;
import spark.Spark;

import javax.crypto.*;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.math.BigInteger;
import java.security.*;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;

import static spark.Spark.*;

public class RestEntrypoint {

    private static final Logger LOGGER = LoggerFactory.getLogger(RestEntrypoint.class);

    private static final String JSON_CONTENT_TYPE = "application/json";

    private static final String TEXT_CONTENT_TYPE = "text/plain";

    private static final String API_VERSION = "v1";

    private static final String BASE_API = "/api/" + API_VERSION;

    private final int port;

    private final UserManager userManager;

    private final UserAuthenticator<SimpleCredential> userAuthenticator;

    private final ResponseTransformer jsonResponseTransformer;

    public RestEntrypoint(int port, UserManager userManager, UserAuthenticator<SimpleCredential> userAuthenticator) {
        this.port = port;
        this.userManager = userManager;
        this.userAuthenticator = userAuthenticator;
        jsonResponseTransformer = new JsonTransformer();
    }

    public void start() {

        Spark.port(port);

        staticFileLocation("webapp");

        before((request, response) -> {
            boolean authenticationRequired = true;
            // White list of url which not require to have an identifier.
            if (requestMatch("POST", BASE_API + "/user", request) ||
                    requestMatch("GET", BASE_API, request) ||
                    requestMatch("GET", BASE_API + "/doc/.*", request) ||
                    requestMatch("PUT", BASE_API + "/user/[^/]*", request)) {
                authenticationRequired = false;
            }

            if (authenticationRequired) {
                Authenticator authenticator = new Authenticator();
                authenticator.handle(request, response);
                if (authenticator.isProvideCredentials()) {
                    User user = userAuthenticator.authenticate(new SimpleCredential(authenticator.getUsername(), authenticator.getPassword()));
                    if (user == null) {
                        authorizationRequiered(response);
                    }

                } else {
                    authorizationRequiered(response);
                }
            }
        });

        get(BASE_API, JSON_CONTENT_TYPE, (request, response) -> {
            response.type(JSON_CONTENT_TYPE);
            return "{\"version\":\"1.0.0\"}";
        });

        post(BASE_API + "/user", JSON_CONTENT_TYPE, (request, response) -> {
            String res = userManager.generateId();
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Generate id : {}", res);
            }
            return res;
        });

        put(BASE_API + "/user/:id", JSON_CONTENT_TYPE, ((request, response) -> {
            String identifier = request.params(":id");
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Try to create user with id {}", identifier);
            }
            if (userManager.identifierExpectedNewUser(identifier)) {
                JsonParser parser = new JsonParser();
                JsonObject json = (JsonObject) parser.parse(request.body());
                String email = json.getAsJsonPrimitive("email").getAsString();
                String username = email.substring(0, email.lastIndexOf("@"));
                User userByUsername = userManager.getUserByUsername(username);
                if (userByUsername != null) {
                    if (LOGGER.isDebugEnabled()) {
                        LOGGER.debug("Trying to create user {} from email '{}' who already exist.", username, email);
                    }
                    halt(409);
                    return "";
                }

                String password = new BigInteger(130, new SecureRandom()).toString(32);
                KeyPair keyPair = RSAUtils.generateRsaKeyPair();
                RSAPrivateKey privateKey = (RSAPrivateKey) keyPair.getPrivate();
                User user = new User(identifier, username, username, email, password, RSAUtils.encodePublicKey((RSAPublicKey) keyPair.getPublic(), email));
                if (userManager.addUser(user)) {
                    response.status(201);
                    StringWriter sw = new StringWriter();
                    RSAUtils.writeRsaPrivateKey(privateKey, sw);
                    return new UserCreationDto(user, sw.toString());
                }

                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("The UserManager not abel to add following user {}.", user.toString());
                }
                halt(428);
                return "";
            } else {
                halt(412);
                return "";
            }
        }), jsonResponseTransformer);

        get(BASE_API + "/user/:id", JSON_CONTENT_TYPE, (request, response) -> {
            SimpleCredential credential = extractCredential(request);
            if (credential != null) {
                String identifier = request.params(":id");
                User user = userManager.getUserByIdentifier(identifier);
                if (user.getUsername().equals(credential.getUsername())) {
                    return user;
                } else {
                    return new User(user.getIdentifier(), user.getName(), user.getUsername(), "", "", "");
                }
            }
            halt(500);
            return "";
        }, jsonResponseTransformer);

        Spark.awaitInitialization();

    }

    public void stop() {
        Spark.stop();
    }

    private static void authorizationRequiered(Response response) {
        response.header("WWW-Authenticate", "Basic realm=\"Kodokojo\"");
        halt(401);
    }

    private static boolean requestMatch(String methodName, String regexpPath, Request request) {
        boolean matchMethod = methodName.equals(request.requestMethod());
        boolean pathMatch = request.pathInfo().matches(regexpPath);
        return matchMethod && pathMatch;
    }

    private static SimpleCredential extractCredential(Request request) {
        Authenticator authenticator = new Authenticator();
        try {
            authenticator.handle(request, null);
            if (authenticator.isProvideCredentials()) {
                return new SimpleCredential(authenticator.getUsername(), authenticator.getPassword());
            }
        } catch (Exception e) {
            LOGGER.debug("Unable to retrieve credentials", e);
        }
        return null;
    }


    //  TODO Move following code in Test runer class.
    public static void main(String[] args) throws NoSuchAlgorithmException, DecoderException, IOException {

        KeyGenerator generator = KeyGenerator.getInstance("AES");
        generator.init(128);
        SecretKey aesKey = generator.generateKey();


        String encodedString = Hex.encodeHexString(aesKey.getEncoded());
        System.out.println(encodedString);

        byte[] byteKey = Hex.decodeHex(encodedString.toCharArray());

        System.out.println(new File("").getAbsolutePath());
        byte[] encodeKeyByteArray = FileUtils.readFileToByteArray(new File("temp.key"));
        SecretKeySpec keySpec = new SecretKeySpec(encodeKeyByteArray, "AES");
/*
        String data = "Coucou !";

        byte[] encrypt = encrypt(data, keySpec);

        FileOutputStream outputStream = new FileOutputStream("content_crypted.txt");
        outputStream.write(encrypt);
        outputStream.flush();
        outputStream.close();
*/
        byte[] readFileToByteArray = FileUtils.readFileToByteArray(new File("content_crypted.txt"));

        String decrypt = decrypt(readFileToByteArray, keySpec);
        System.out.println(decrypt);

        /*

        RedisUserManager redisUserManager = new RedisUserManager(aesKey, "192.168.99.100", 6379);
        redisUserManager.addUser(new User(redisUserManager.generateId(), "Jean-Pascal THIERY", "jpthiery", "jpthiery@xebia.fr", "jpascal", "SSHPublic key"));
        System.out.println(redisUserManager.getUserByUsername("jpthiery"));
        RestEntrypoint restEntrypoint = new RestEntrypoint(80, redisUserManager, redisUserManager);
        restEntrypoint.start();
        */
    }

    private static byte[] encrypt(String data, SecretKey key) {
        try {
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.ENCRYPT_MODE, key);
            return cipher.doFinal(data.getBytes());
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | BadPaddingException | IllegalBlockSizeException e) {
            throw new RuntimeException("Unable to create Cipher", e);
        }
    }

    private static String decrypt(byte[] encrypted, SecretKey key) {
        try {
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.DECRYPT_MODE, key);
            return new String(cipher.doFinal(encrypted));
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | BadPaddingException | IllegalBlockSizeException e) {
            throw new RuntimeException("Unable to create Cipher", e);
        }
    }


}
