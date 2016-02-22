package io.kodokojo.user;

import io.kodokojo.commons.project.model.User;
import io.kodokojo.commons.project.model.UserService;
import io.kodokojo.commons.utils.RSAUtils;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import java.io.IOException;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;

public class RedisUserManagerIntTest {


    public static void main(String[] args) throws NoSuchAlgorithmException, IOException {
        //  TODO Remove
        KeyGenerator generator = KeyGenerator.getInstance("AES");
        generator.init(128);
        SecretKey key = generator.generateKey();

        KeyPair keyPair = RSAUtils.generateRsaKeyPair();

        UserManager userManager = new RedisUserManager(key, "192.168.99.100");

        String email = "jpthiery@xebia.fr";
        User jpthiery = new User("Jean-Pascal THIERY", "jpthiery", email, "jpascal", RSAUtils.encodePublicKey((RSAPublicKey) keyPair.getPublic(), email));

        userManager.addUser(jpthiery);

        User user = userManager.getUserByUsername("jpthiery");
        System.out.println(user);
        System.out.println(user.getSshPublicKey());


        UserService jenkins = new UserService(id, "jenkins", "jenkins", "jenkins", (RSAPrivateKey) keyPair.getPrivate(), (RSAPublicKey) keyPair.getPublic());
        userManager.addUserService(jenkins);

        UserService userService = userManager.getUserServiceByName("jenkins");
        System.out.println(userService);
    }

}