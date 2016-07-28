package io.kodokojo.service.actor.user;

import akka.actor.AbstractActor;
import akka.actor.Props;
import akka.japi.pf.ReceiveBuilder;
import io.kodokojo.service.RSAUtils;

import java.math.BigInteger;
import java.security.KeyPair;
import java.security.SecureRandom;

public class UserGenerateSecurityData extends AbstractActor {

    public static Props PROPS() {
        return Props.create(UserGenerateSecurityData.class);
    }

    public UserGenerateSecurityData() {
        receive(ReceiveBuilder.match(GenerateSecurityMessage.class, msg -> {
            String password = new BigInteger(130, new SecureRandom()).toString(msg.nbDecimal);
            KeyPair keyPair = RSAUtils.generateRsaKeyPair();
            sender().tell(new UserSecurityDataMessage(password, keyPair), self());
        }).matchAny(this::unhandled).build());
    }

    public static class GenerateSecurityMessage {

        protected final int nbDecimal;

        public GenerateSecurityMessage(int nbDecimal) {
            this.nbDecimal = nbDecimal;
        }

    }

    public static class UserSecurityDataMessage {

        private final String password;

        private final KeyPair keyPair;

        public UserSecurityDataMessage(String password, KeyPair keyPair) {
            this.password = password;
            this.keyPair = keyPair;
        }

        public String getPassword() {
            return password;
        }

        public KeyPair getKeyPair() {
            return keyPair;
        }
    }

}
