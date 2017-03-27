package io.kodokojo.commons.service.repository;

import io.kodokojo.commons.service.repository.search.Criteria;
import io.kodokojo.commons.service.repository.search.UserSearchDto;
import javaslang.control.Option;

import java.util.List;
import java.util.Set;

public interface UserSearcher {

    Option<List<UserSearchDto>> searchUserByCriterion(Set<String> organisationIds, Criteria... criterion);

    default Option<List<UserSearchDto>> searchUserByUsername(Set<String> organisationIds, String name) {
        Criteria criteria = new Criteria("username", name);
        return searchUserByCriterion(organisationIds, criteria);
    }

    default Option<List<UserSearchDto>> searchUserByEmail(Set<String> organisationIds, String email) {
        Criteria criteria = new Criteria("email", email);
        return searchUserByCriterion(organisationIds, criteria);
    }

}
