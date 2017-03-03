package io.kodokojo.commons.service.repository;

import io.kodokojo.commons.service.repository.search.Criteria;
import io.kodokojo.commons.service.repository.search.ProjectSearchDto;
import javaslang.control.Option;

import java.util.List;

public interface ProjectSearcher {

    Option<List<ProjectSearchDto>> searchSoftwareFactoryByCriterion(Criteria... criterion);

    default Option<List<ProjectSearchDto>> searchSofwareFactoryByName(String name) {
        Criteria criteria = new Criteria("name", name);
        return searchSoftwareFactoryByCriterion(criteria);
    }
}
