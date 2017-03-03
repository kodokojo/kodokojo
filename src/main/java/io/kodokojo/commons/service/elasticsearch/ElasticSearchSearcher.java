package io.kodokojo.commons.service.elasticsearch;

import io.kodokojo.commons.config.ElasticSearchConfig;
import io.kodokojo.commons.service.repository.OrganisationSearcher;
import io.kodokojo.commons.service.repository.ProjectSearcher;
import io.kodokojo.commons.service.repository.UserSearcher;
import io.kodokojo.commons.service.repository.search.Criteria;
import io.kodokojo.commons.service.repository.search.OrganisationSearchDto;
import io.kodokojo.commons.service.repository.search.ProjectSearchDto;
import io.kodokojo.commons.service.repository.search.UserSearchDto;
import javaslang.control.Option;
import okhttp3.OkHttpClient;

import javax.inject.Inject;
import java.util.List;

public class ElasticSearchSearcher extends ElasticSearchEngine implements OrganisationSearcher, UserSearcher, ProjectSearcher {

    @Inject
    public ElasticSearchSearcher(ElasticSearchConfig elasticSearchConfig, OkHttpClient httpClient) {
        super(elasticSearchConfig, httpClient);
    }

    @Override
    public Option<List<OrganisationSearchDto>> searchOrganisationByCriterion(Criteria... criterionArray) {
        return search(OrganisationSearchDto.class, ORAGNISATION_INDEX, criterionArray);
    }

    @Override
    public Option<List<UserSearchDto>> searchUserByCriterion(Criteria... criterion) {
        return search(UserSearchDto.class, USER_INDEX, criterion);
    }

    @Override
    public Option<List<ProjectSearchDto>> searchSoftwareFactoryByCriterion(Criteria... criterion) {
        return search(ProjectSearchDto.class, SOFTWAREFACTORY_INDEX, criterion);
    }

    protected static final String ORAGNISATION_INDEX = "organisation";

    protected static final String USER_INDEX = "user";

    protected static final String SOFTWAREFACTORY_INDEX = "softwarefactory";

}
