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
