package io.kodokojo.commons.service.elasticsearch;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.kodokojo.commons.DataBuilder;
import io.kodokojo.commons.config.ElasticSearchConfig;
import io.kodokojo.commons.service.repository.search.Criteria;
import io.kodokojo.commons.service.repository.search.OrganisationSearchDto;
import io.kodokojo.commons.service.repository.search.UserSearchDto;
import javaslang.control.Option;
import okhttp3.OkHttpClient;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.util.List;

@Ignore
public class ElasticSearchSearcherIntTest implements DataBuilder {

    private ElasticSearchConfig elasticSearchConfig;

    private Gson gson;

    private ElasticSearchConfigurationSearcher elasticSearchSearcher;

    @Test
    public void acceptance() {
        //  insert data.
        insertData();

        OrganisationSearchDto data = anOrganisationSearchDto();
        if (elasticSearchSearcher.addOrUpdate(data)) {
            System.out.println("Data update or insert");
        }
        Option<List<OrganisationSearchDto>> organisationResult = elasticSearchSearcher.searchOrganisationByCriterion(null,
                new Criteria("name", "xebia")
        );
        organisationResult.forEach(organisationSearchDtos -> {
            System.out.println(organisationSearchDtos);
        });


        Option<List<UserSearchDto>> emailOption = elasticSearchSearcher.searchUserByCriterion(null, new Criteria("email", Criteria.CriteriaOperator.COULD_BE, "durand.pierre"));
        emailOption.get().forEach(System.out::println);
    }

    private void insertData() {
        elasticSearchSearcher.addOrUpdate( anUserSearchDto());
        elasticSearchSearcher.addOrUpdate(aSecondeUserSearchDto());
        elasticSearchSearcher.addOrUpdate( aThirdUserSearchDto());
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
        elasticSearchSearcher = new ElasticSearchConfigurationSearcher(elasticSearchConfig, new OkHttpClient());
    }

}