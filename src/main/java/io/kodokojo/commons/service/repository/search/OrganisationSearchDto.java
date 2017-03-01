package io.kodokojo.commons.service.repository.search;

import io.kodokojo.commons.model.Organisation;
import io.kodokojo.commons.service.elasticsearch.DataIdProvider;

import java.io.Serializable;
import java.util.function.Function;

import static java.util.Objects.requireNonNull;

public class OrganisationSearchDto implements DataIdProvider {

    private String identifier;

    private String name;

    public OrganisationSearchDto() {
        super();
    }

    public static OrganisationSearchDto convert(Organisation organisation) {
        requireNonNull(organisation, "organisation must be defined.");
        return converter().apply(organisation);
    }

    public static Function<Organisation, OrganisationSearchDto> converter() {
        return organisation -> {
            OrganisationSearchDto res = new OrganisationSearchDto();
            res.setIdentifier(organisation.getIdentifier());
            res.setName(organisation.getName());
            return res;
        };
    }

    @Override
    public String getId() {
        return identifier;
    }

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }


    @Override
    public String toString() {
        return "OrganisationSearchDto{" +
                "identifier='" + identifier + '\'' +
                ", name='" + name + '\'' +
                '}';
    }

}
