package io.kodokojo.commons.service.repository;

import io.kodokojo.commons.service.repository.search.Criteria;
import io.kodokojo.commons.service.repository.search.OrganisationSearchDto;
import javaslang.control.Option;

import java.util.List;

public interface OrganisationSearcher {

    Option<List<OrganisationSearchDto>> searchOrganisationByCriterion(Criteria... criterion);

    default Option<List<OrganisationSearchDto>> searchOrganisationByName(String name) {
        Criteria criteria = new Criteria("name", name);
        return searchOrganisationByCriterion(criteria);
    }

}
