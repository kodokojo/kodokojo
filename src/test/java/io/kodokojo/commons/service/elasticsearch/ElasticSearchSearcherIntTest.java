package io.kodokojo.commons.service.elasticsearch;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.kodokojo.commons.DataBuilder;
import io.kodokojo.commons.config.ElasticSearchConfig;
import io.kodokojo.commons.service.repository.search.Criteria;
import io.kodokojo.commons.service.repository.search.OrganisationSearchDto;
import io.kodokojo.commons.service.repository.search.UserSearchDto;
import javaslang.control.Option;
import javaslang.control.Try;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@Ignore
public class ElasticSearchSearcherIntTest implements DataBuilder {

    private ElasticSearchConfig elasticSearchConfig;

    private Gson gson;

    private ElasticSearchSearcher elasticSearchSearcher;

    @Test
    public void acceptance() {
        //  insert data.
        insertData();

        OrganisationSearchDto data = anOrganisationSearchDto();
        if (elasticSearchSearcher.addOrUpdate(ElasticSearchSearcher.ORAGNISATION_INDEX, data)) {
            System.out.println("Data update or insert");
        }
        Option<List<OrganisationSearchDto>> organisationResult = elasticSearchSearcher.searchOrganisationByCriterion(
                new Criteria("name","xebia")
        );
        organisationResult.forEach(organisationSearchDtos -> {
            System.out.println(organisationSearchDtos);
        });


        Option<List<UserSearchDto>> emailOption = elasticSearchSearcher.searchUserByCriterion(new Criteria("email", Criteria.CriteriaOperator.COULD_BE, "durand.pierre"));
        emailOption.get().forEach(System.out::println);
    }

    private void insertData() {
        elasticSearchSearcher.addOrUpdate(ElasticSearchSearcher.USER_INDEX, anUserSearchDto());
        elasticSearchSearcher.addOrUpdate(ElasticSearchSearcher.USER_INDEX, aSecondeUserSearchDto());
        elasticSearchSearcher.addOrUpdate(ElasticSearchSearcher.USER_INDEX, aThirdUserSearchDto());
    }


    @Before
    public void setup() {
        elasticSearchConfig = new ElasticSearchConfig() {
            @Override
            public String url() {
                return "http://localhost:9200";
            }

            @Override
            public String indexName() {
                return "kodokojo";
            }
        };
        gson = new GsonBuilder().create();
        elasticSearchSearcher = new ElasticSearchSearcher(elasticSearchConfig, new OkHttpClient());
    }

}