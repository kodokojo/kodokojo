package io.kodokojo.commons.service.repository;

import io.kodokojo.commons.service.repository.search.Criteria;
import io.kodokojo.commons.service.repository.search.ProjectConfigurationSearchDto;
import javaslang.control.Option;

import java.util.List;

public interface ProjectConfigurationSearcher {

    Option<List<ProjectConfigurationSearchDto>> searchProjectConfigurationByCriterion(List<String> organisationIds,Criteria... criterion);

    default Option<List<ProjectConfigurationSearchDto>> searchSofwareFactoryByName(List<String> organisationIds,String name) {
        Criteria criteria = new Criteria("name", name);
        return searchProjectConfigurationByCriterion(organisationIds, criteria);
    }
}
