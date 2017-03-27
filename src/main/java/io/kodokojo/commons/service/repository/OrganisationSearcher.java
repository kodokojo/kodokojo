package io.kodokojo.commons.service.repository;

import io.kodokojo.commons.service.repository.search.Criteria;
import io.kodokojo.commons.service.repository.search.OrganisationSearchDto;
import javaslang.control.Option;

import java.util.List;
import java.util.Set;

public interface OrganisationSearcher {

    Option<List<OrganisationSearchDto>> searchOrganisationByCriterion(Set<String> organisationIds,Criteria... criterion);

    default Option<List<OrganisationSearchDto>> searchOrganisationByName(Set<String> organisationIds, String name) {
        Criteria criteria = new Criteria("name", name);
        return searchOrganisationByCriterion(organisationIds, criteria);
    }

}
