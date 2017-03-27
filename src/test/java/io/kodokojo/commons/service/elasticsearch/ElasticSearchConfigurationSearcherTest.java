package io.kodokojo.commons.service.elasticsearch;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.kodokojo.commons.config.ElasticSearchConfig;
import io.kodokojo.commons.service.repository.search.Criteria;
import okhttp3.OkHttpClient;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ElasticSearchConfigurationSearcherTest {

    @Test
    public void search_user_in_all_organisation_ES_query_test() {
        ElasticSearchConfigurationSearcher elasticSearchConfigurationSearcher = new ElasticSearchConfigurationSearcher(elasticSearchConfig, new OkHttpClient());
        List<Criteria> criterion = new ArrayList<>();
        criterion.add(new Criteria("global", "dupont"));
        criterion.add(new Criteria("username", Criteria.CriteriaOperator.MUST_BE, "durand"));

        String query = elasticSearchConfigurationSearcher.generateUserQuery(null, criterion.toArray(new Criteria[0]));
        System.out.println(query);
        JsonParser parser = new JsonParser();
        JsonObject json = (JsonObject) parser.parse(query);

    }

    @Test
    public void search_user_from_current_organisation_ES_query_test() {
        ElasticSearchConfigurationSearcher elasticSearchConfigurationSearcher = new ElasticSearchConfigurationSearcher(elasticSearchConfig, new OkHttpClient());
        List<Criteria> criterion = new ArrayList<>();
        criterion.add(new Criteria("global", "dupont"));
        criterion.add(new Criteria("username", Criteria.CriteriaOperator.MUST_BE, "durand"));

        List<String> organisationIds = new ArrayList<>();
        organisationIds.add("1234");
        organisationIds.add("5678");
        String query = elasticSearchConfigurationSearcher.generateUserQuery(organisationIds, criterion.toArray(new Criteria[0]));
        System.out.println(query);
        JsonParser parser = new JsonParser();
        JsonObject json = (JsonObject) parser.parse(query);

    }

    private ElasticSearchConfig elasticSearchConfig = new ElasticSearchConfig() {
        @Override
        public String url() {
            return "http://localhost:9200";
        }

        @Override
        public String indexName() {
            return "kodokojo";
        }
    };

}