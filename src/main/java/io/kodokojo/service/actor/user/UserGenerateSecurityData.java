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
package io.kodokojo.service.actor.user;

import akka.actor.AbstractActor;
import akka.actor.Props;
import akka.japi.pf.ReceiveBuilder;
import io.kodokojo.service.RSAUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigInteger;
import java.security.KeyPair;
import java.security.SecureRandom;

/**
 * Generate SSH keys and Secret password.
 */
public class UserGenerateSecurityData extends AbstractActor {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserGenerateSecurityData.class);

    public static Props PROPS() {
        return Props.create(UserGenerateSecurityData.class);
    }

    public UserGenerateSecurityData() {
        receive(ReceiveBuilder.match(GenerateSecurityMsg.class, msg -> {
            LOGGER.debug("Receive a request to generate security data of user.");
            String password = new BigInteger(130, new SecureRandom()).toString(msg.nbDecimal);
            KeyPair keyPair = RSAUtils.generateRsaKeyPair();
            sender().tell(new UserSecurityDataMsg(password, keyPair), self());
        }).matchAny(this::unhandled).build());
    }

    public static class GenerateSecurityMsg {

        protected final int nbDecimal;

        public GenerateSecurityMsg(int nbDecimal) {
            this.nbDecimal = nbDecimal;
        }

        public GenerateSecurityMsg() {
            this(32);
        }

    }

    public static class UserSecurityDataMsg {

        private final String password;

        private final KeyPair keyPair;

        public UserSecurityDataMsg(String password, KeyPair keyPair) {
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
