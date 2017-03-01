package io.kodokojo.commons.service.repository;

import io.kodokojo.commons.service.repository.search.Criteria;
import io.kodokojo.commons.service.repository.search.SoftwareFactorySearchDto;
import javaslang.control.Option;

import java.util.List;

public interface SoftwareFactorySearcher {

    Option<List<SoftwareFactorySearchDto>> searchSoftwareFactoryByCriterion(Criteria... criterion);

    default Option<List<SoftwareFactorySearchDto>> searchSofwareFactoryByName(String name) {
        Criteria criteria = new Criteria("name", name);
        return searchSoftwareFactoryByCriterion(criteria);
    }
}
