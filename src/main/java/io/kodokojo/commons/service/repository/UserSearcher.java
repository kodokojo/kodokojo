package io.kodokojo.commons.service.repository;

import io.kodokojo.commons.service.repository.search.Criteria;
import io.kodokojo.commons.service.repository.search.UserSearchDto;
import javaslang.control.Option;

import java.util.List;

public interface UserSearcher {

    Option<List<UserSearchDto>> searchUserByCriterion(Criteria... criterion);

    default Option<List<UserSearchDto>> searchUserByUsername(String name) {
        Criteria criteria = new Criteria("username", name);
        return searchUserByCriterion(criteria);
    }

    default Option<List<UserSearchDto>> searchUserByEmail(String email) {
        Criteria criteria = new Criteria("email", email);
        return searchUserByCriterion(criteria);
    }

}
