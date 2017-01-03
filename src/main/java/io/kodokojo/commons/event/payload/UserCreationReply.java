/**
 * Kodo Kojo - Software factory done right
 * Copyright © 2017 Kodo Kojo (infos@kodokojo.io)
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
package io.kodokojo.commons.event.payload;

import io.kodokojo.commons.RSAUtils;

import java.io.Serializable;
import java.security.KeyPair;
import java.security.interfaces.RSAPublicKey;

public class UserCreationReply implements Serializable {

    private final String userId;

    private final String privateKey;

    private final String publicKey;

    private final boolean userInWaitingList;

    private final boolean usernameEligible;

    public UserCreationReply(String userId, KeyPair keyPair, String email, boolean userInWaitingList, boolean usernameEligible) {
        this.userId = userId;
        if (keyPair != null) {
            this.privateKey = RSAUtils.encodedPrivateKey(keyPair.getPrivate());
            this.publicKey = RSAUtils.encodePublicKey((RSAPublicKey) keyPair.getPublic(), email);
        } else {
            this.privateKey = "";
            this.publicKey = "";
        }
        this.userInWaitingList = userInWaitingList;
        this.usernameEligible = usernameEligible;
    }

    public String getUserId() {
        return userId;
    }

    public String getPrivateKey() {
        return privateKey;
    }

    public String getPublicKey() {
        return publicKey;
    }

    public boolean isUserInWaitingList() {
        return userInWaitingList;
    }

    public boolean isUsernameEligible() {
        return usernameEligible;
    }
}
