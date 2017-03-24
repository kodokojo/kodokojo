package io.kodokojo.commons.service.elasticsearch;

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
import java.util.List;
import java.util.Properties;

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
    public Option<List<OrganisationSearchDto>> searchOrganisationByCriterion(Criteria... criterionArray) {
        return search(OrganisationSearchDto.class, ORGANISATION_INDEX, criterionArray);
    }

    @Override
    public Option<List<UserSearchDto>> searchUserByCriterion(Criteria... criterion) {
        return search(UserSearchDto.class, USER_INDEX, criterion);
    }

    @Override
    public Option<List<ProjectConfigurationSearchDto>> searchProjectConfigurationByCriterion(Criteria... criterion) {
        return search(ProjectConfigurationSearchDto.class, SOFTWARE_FACTORY_INDEX, criterion);
    }

    @Override
    protected String generateQuery(List<Criteria> criterion) {
        VelocityEngine ve = new VelocityEngine();
        ve.init(VE_PROPERTIES);

        Template template = ve.getTemplate("marathon/" + brickConfiguration.getName().toLowerCase() + ".json.vm");

        VelocityContext context = new VelocityContext();
        context.put("ID", "id");
        StringWriter sw = new StringWriter();
        template.merge(context, sw);
        return super.generateQuery(criterion);
    }

    protected static final String ORGANISATION_INDEX = "organisation";

    protected static final String USER_INDEX = "user";

    protected static final String SOFTWARE_FACTORY_INDEX = "softwarefactory";

}
