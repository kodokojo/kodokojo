package io.kodokojo.bdd.stage;

import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
import com.tngtech.jgiven.Stage;
import com.tngtech.jgiven.annotation.ExpectedScenarioState;
import com.tngtech.jgiven.annotation.ProvidedScenarioState;
import com.tngtech.jgiven.annotation.Quoted;

import java.io.IOException;

import static org.assertj.core.api.Assertions.fail;

public class AccessRestWhen<SELF extends AccessRestWhen<?>> extends Stage<SELF> {

    private final OkHttpClient httpClient = new OkHttpClient();

    @ExpectedScenarioState
    String restEntryPointHost;

    @ExpectedScenarioState
    int restEntryPointPort;

    @ProvidedScenarioState
    int responseHttpStatusCode;

    @ProvidedScenarioState
    String responseHttpStatusBody;

    public SELF try_to_access_to_get_url_$(@Quoted String url) {
        return try_to_access_to_call_$_url_$("GET", url);
    }


    private SELF try_to_access_to_call_$_url_$(@Quoted String methodName, @Quoted String url) {

        Request request = new Request.Builder().get().url(getBaseUrl() + url).build();
        try {
            Response response = httpClient.newCall(request).execute();
            responseHttpStatusCode = response.code();
            responseHttpStatusBody = response.body().string();
            response.body().close();
        } catch (IOException e) {
            fail(e.getMessage());
        }
        return self();
    }

    private String getBaseUrl() {
        return "http://" + restEntryPointHost + ":" + restEntryPointPort;
    }
}
