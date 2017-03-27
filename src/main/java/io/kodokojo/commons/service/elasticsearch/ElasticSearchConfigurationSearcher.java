package io.kodokojo.commons.service.elasticsearch;

import com.google.gson.GsonBuilder;
import io.kodokojo.commons.config.ElasticSearchConfig;
import io.kodokojo.commons.service.repository.OrganisationSearcher;
import io.kodokojo.commons.service.repository.ProjectConfigurationSearcher;
import io.kodokojo.commons.service.repository.UserSearcher;
import io.kodokojo.commons.service.repository.search.Criteria;
import io.kodokojo.commons.service.repository.search.OrganisationSearchDto;
import io.kodokojo.commons.service.repository.search.ProjectConfigurationSearchDto;
import io.kodokojo.commons.service.repository.search.UserSearchDto;
import javaslang.control.Option;
import okhttp3.OkHttpClient;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.runtime.RuntimeConstants;
import org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader;

import javax.inject.Inject;
import java.io.StringWriter;
import java.util.*;
import java.util.stream.Collectors;

import static org.apache.commons.collections4.CollectionUtils.isNotEmpty;

public class ElasticSearchConfigurationSearcher extends ElasticSearchEngine implements OrganisationSearcher, UserSearcher, ProjectConfigurationSearcher {

    private static final Properties VE_PROPERTIES = new Properties();

    static {
        VE_PROPERTIES.setProperty(RuntimeConstants.RESOURCE_LOADER, "classpath");
        VE_PROPERTIES.setProperty("classpath.resource.loader.class", ClasspathResourceLoader.class.getName());
        VE_PROPERTIES.setProperty("runtime.log.logsystem.class", "org.apache.velocity.runtime.log.NullLogChute");
    }

    @Inject
    public ElasticSearchConfigurationSearcher(ElasticSearchConfig elasticSearchConfig, OkHttpClient httpClient) {
        super(elasticSearchConfig, httpClient);
    }

    @Override
    public Option<List<OrganisationSearchDto>> searchOrganisationByCriterion(Set<String> organisationIds, Criteria... criterionArray) {
        return search(OrganisationSearchDto.class, ORGANISATION_INDEX, organisationIds, criterionArray);
    }

    @Override
    public Option<List<UserSearchDto>> searchUserByCriterion(Set<String> organisationIds, Criteria... criterion) {
        return search(UserSearchDto.class, USER_INDEX, this::generateUserQuery, organisationIds, criterion);
    }

    @Override
    public Option<List<ProjectConfigurationSearchDto>> searchProjectConfigurationByCriterion(List<String> organisationIds, Criteria... criterion) {
        return search(ProjectConfigurationSearchDto.class, SOFTWARE_FACTORY_INDEX, organisationIds, criterion);
    }

    protected String generateUserQuery(Collection<String> organisationIds, Criteria... criterionArray) {
        List<Criteria> criterion = Arrays.asList(criterionArray);
        VelocityContext context = new VelocityContext();
        List<Criteria> userCriterion = criterion.stream()
                .filter(criteria -> !criteria.getField().equals("organisationIds") && !criteria.getField().equals("global"))
                .collect(Collectors.toList());

        List<Criteria> mustBeCriteria = userCriterion.stream()
                .filter(criteria -> criteria.getOperator().equals(Criteria.CriteriaOperator.MUST_BE)).collect(Collectors.toList());

        List<Criteria> shouldBeCriteria = userCriterion.stream()
                .filter(criteria -> criteria.getOperator().equals(Criteria.CriteriaOperator.COULD_BE)).collect(Collectors.toList());

        criterion.stream()
                .filter(criteria -> criteria.getField().equals("global")).findFirst().ifPresent(criteria -> {
            shouldBeCriteria.add(new Criteria("firstName", Criteria.CriteriaOperator.COULD_BE, criteria.getValue()));
            shouldBeCriteria.add(new Criteria("lastName", Criteria.CriteriaOperator.COULD_BE, criteria.getValue()));
            shouldBeCriteria.add(new Criteria("username", Criteria.CriteriaOperator.COULD_BE, criteria.getValue()));
            shouldBeCriteria.add(new Criteria("email", Criteria.CriteriaOperator.COULD_BE, criteria.getValue()));
        });

        VelocityEngine ve = new VelocityEngine();
        ve.init(VE_PROPERTIES);

        Template template = ve.getTemplate("es/query_user.json.vm");
        if (isNotEmpty(organisationIds)) {
            context.put("organisationIds", organisationIds);
        }
        context.put("must", mustBeCriteria);
        context.put("should", shouldBeCriteria);
        StringWriter sw = new StringWriter();
        template.merge(context, sw);
        return sw.toString();
    }


    protected static final String ORGANISATION_INDEX = "organisation";

    protected static final String USER_INDEX = "user";

    protected static final String SOFTWARE_FACTORY_INDEX = "softwarefactory";

}
