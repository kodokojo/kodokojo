package io.kodokojo.commons;

import io.kodokojo.commons.service.elasticsearch.DataIdProvider;
import io.kodokojo.commons.service.repository.search.OrganisationSearchDto;
import io.kodokojo.commons.service.repository.search.UserSearchDto;

public interface DataBuilder {

    default OrganisationSearchDto anOrganisationSearchDto() {
        OrganisationSearchDto res = new OrganisationSearchDto();
        res.setName("xebia");
        res.setIdentifier("1234");
        return res;
    }

    default DataIdProvider anUserSearchDto() {
        UserSearchDto res = new UserSearchDto();
        res.setFirstName("Jean-Paul");
        res.setLastName("Martinez");
        res.setEmail("jeanpaul+martinez@zouzou.org");
        res.setIdentifier("01011");
        return res;
    }

    default DataIdProvider aSecondeUserSearchDto() {
        UserSearchDto res = new UserSearchDto();
        res.setFirstName("Marcel");
        res.setLastName("Flag");
        res.setEmail("marcel+flag@zouzou.org");
        res.setIdentifier("12131415");
        return res;
    }

    default DataIdProvider aThirdUserSearchDto() {
        UserSearchDto res = new UserSearchDto();
        res.setFirstName("Pierre");
        res.setLastName("Durand");
        res.setEmail("durand.pierre@ralalalesjeunes.fr");
        res.setIdentifier("16171819");
        return res;
    }

}
