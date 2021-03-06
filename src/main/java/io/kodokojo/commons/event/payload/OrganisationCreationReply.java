package io.kodokojo.commons.event.payload;

import java.io.Serializable;

public class OrganisationCreationReply implements Serializable{

    private String identifier;

    private boolean alreadyExist;

    public OrganisationCreationReply(String identifier, boolean alreadyExist) {
        this.identifier = identifier;
        this.alreadyExist = alreadyExist;
    }

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public boolean isAlreadyExist() {
        return alreadyExist;
    }

    public void setAlreadyExist(boolean alreadyExist) {
        this.alreadyExist = alreadyExist;
    }

    @Override
    public String toString() {
        return "OrganisationCreationReply{" +
                "identifier='" + identifier + '\'' +
                ", alreadyExist=" + alreadyExist +
                '}';
    }
}
