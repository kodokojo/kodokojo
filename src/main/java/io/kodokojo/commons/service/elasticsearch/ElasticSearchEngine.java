package io.kodokojo.commons.service.elasticsearch;

import com.google.gson.*;
import io.kodokojo.commons.config.ElasticSearchConfig;
import io.kodokojo.commons.service.repository.search.Criteria;
import javaslang.control.Option;
import javaslang.control.Try;
import okhttp3.*;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static java.util.Objects.requireNonNull;
import static org.apache.commons.lang.StringUtils.isBlank;
import static org.apache.commons.lang.StringUtils.isNotBlank;

public class ElasticSearchEngine {

    private static final Logger LOGGER = LoggerFactory.getLogger(ElasticSearchEngine.class);

    private static final String IDENTIFIER = "identifier";

    protected final ElasticSearchConfig elasticSearchConfig;

    protected final OkHttpClient httpClient;

    protected final ThreadLocal<Gson> gson = ThreadLocal.withInitial(() -> new GsonBuilder().create());

    @Inject
    public ElasticSearchEngine(ElasticSearchConfig elasticSearchConfig, OkHttpClient httpClient) {
        requireNonNull(elasticSearchConfig, "elasticSearchConfig must be defined.");
        requireNonNull(httpClient, "httpClient must be defined.");
        this.elasticSearchConfig = elasticSearchConfig;
        this.httpClient = httpClient;
    }

    public boolean isConnected() {
        Request.Builder builder = new Request.Builder();
        Request request = builder.get()
                .url(elasticSearchConfig.url())
                .build();
        Try<HttpResponse> httpResponses = executeRequest(request);
        return httpResponses.map(h -> h.code).getOrElse(-1) == 200;
    }

    public boolean addOrUpdate(String type, DataIdProvider data) {
        return addOrUpdate(type, data, data.getId());
    }

    public boolean addOrUpdate(String type, Serializable data, String identifier) {
        if (isBlank(type)) {
            throw new IllegalArgumentException("type must be defined.");
        }
        requireNonNull(data, "data must be defined.");
        if (isBlank(identifier)) {
            throw new IllegalArgumentException("identifier must be defined.");
        }
        RequestBody body = RequestBody.create(JSON_MINETYPE, gson.get().toJson(data));
        if (isAlreadyInserted(type, identifier)) {
            JsonParser parser = new JsonParser();
            Request request = computeLookupRequest(type, identifier);
            Try<HttpResponse> httpResponses = executeRequest(request);
            return httpResponses.map(httpResponse -> {
                String bodyResponse = httpResponse.getBody();
                JsonObject json = (JsonObject) parser.parse(bodyResponse);
                JsonObject hits = json.getAsJsonObject(HITS_ATTRIBUTES);
                if (hits.getAsJsonPrimitive("total").getAsInt() == 0) {
                    return false;
                }
                JsonObject element = (JsonObject) hits.getAsJsonArray(HITS_ATTRIBUTES).get(0);
                return element.getAsJsonPrimitive(ID_ATTRIBUTE).getAsString();
            }).map(id -> {
                String url = computeUrl(type) + id;
                Request.Builder builder = new Request.Builder();
                Request updateRequest = builder.put(body).url(url).build();
                LOGGER.debug("Updating following data with _id '{}': {}", id, data);
                Try<HttpResponse> updateResponse = executeRequest(updateRequest);
                return updateResponse.map(httpResponse -> {
                    JsonObject json = (JsonObject) parser.parse(httpResponse.getBody());
                    return json.getAsJsonPrimitive("created").getAsString().equals("false");
                }).getOrElse(false);
            }).getOrElse(false);

        } else {
            Request.Builder builder = new Request.Builder();
            Request request = builder.url(computeUrl(type)).post(body).build();
            LOGGER.debug("Inserting data: {}", data);
            Try<HttpResponse> httpResponses = executeRequest(request);
            return httpResponses.map(httpResponse -> {
                if (httpResponse.code != 201) {
                    return false;
                }
                JsonParser parser = new JsonParser();
                JsonObject json = (JsonObject) parser.parse(httpResponse.getBody());

                return json.getAsJsonPrimitive("result").getAsString().equals("created");
            }).getOrElse(false);
        }
    }

    protected boolean isAlreadyInserted(String type, String id) {
        if (isBlank(type)) {
            throw new IllegalArgumentException("type must be defined.");
        }
        if (isBlank(id)) {
            throw new IllegalArgumentException("id must be defined.");
        }
        JsonParser parser = new JsonParser();
        Request request = computeLookupRequest(type, id);
        Try<HttpResponse> httpResponses = executeRequest(request);
        Try<Boolean> exist = httpResponses.map(httpResponse -> {
            String body = httpResponse.getBody();
            JsonObject json = (JsonObject) parser.parse(body);
            return json.getAsJsonObject(HITS_ATTRIBUTES).getAsJsonPrimitive("total").getAsInt() > 0;
        });
        return exist.getOrElse(Boolean.FALSE);
    }

    private Request computeLookupRequest(String type, String id) {
        Request.Builder builder = new Request.Builder();
        String url = computeUrl(type, SEARCH_ACTION);
        List<Criteria> criterion = new ArrayList<>();
        criterion.add(new Criteria(provideIdAttribute(type), id));
        String query = generateQuery(criterion);
        Request request = builder.url(url)
                .post(RequestBody.create(JSON_MINETYPE, query))
                .build();
        return request;
    }


    protected Try<HttpResponse> executeRequest(Request request) {
        Response response = null;
        try {
            response = httpClient.newCall(request).execute();
            return Try.success(new HttpResponse(response.code(), response.body().string()));
        } catch (IOException e) {
            LOGGER.error("An error occur while trying to {} on url {}.", request.method(), request.url().toString());
            return Try.failure(e);
        } finally {
            if (response != null) {
                IOUtils.closeQuietly(response.body());
            }
        }
    }

    protected static String generateQuery(List<Criteria> criterion) {
        assert criterion != null : "criterion must be defined";

        StringBuilder res = new StringBuilder("{\"query\": { \"bool\": { ");
        StringBuilder must = new StringBuilder("\"must\": [");
        StringBuilder should = new StringBuilder("\"should\": [");
        StringBuilder mustNot = new StringBuilder("\"must_not\": [");
        boolean isFirstMustCriteria = true;
        boolean isFirstShouldCriteria = true;
        boolean isFirstMustNotCriteria = true;
        for (Criteria criteria : criterion) {
            StringBuilder sb = should;
            if (isFirstMustCriteria) {
                isFirstMustCriteria = false;
            } else {
                must.append(",");
            }
            if (isFirstShouldCriteria) {
                isFirstShouldCriteria = false;
            } else {
                should.append(",");
            }
            if (isFirstMustNotCriteria) {
                isFirstMustNotCriteria = false;
            } else {
                mustNot.append(",");
            }
            switch (criteria.getOperator()) {
                case MATCH:
                case COULD_BE:
                    sb = should;
                    break;
                case MUST_BE:
                    sb = must;
                    break;
                case EXCLUDE:
                    sb = mustNot;
                    break;
            }
            sb.append("{ \"match\": { \"").append(criteria.getField()).append("\": \"").append(criteria.getValue()).append("\" }}");
        }
        must.append("]");
        mustNot.append("]");
        should.append("]");
        res.append(must.toString()).append(",");
        res.append(mustNot.toString()).append(",");
        res.append(should.toString());
        res.append("}}}");

        return res.toString();
    }

    protected <T> Option<List<T>> search(Class<T> classe, String esType, Criteria... criterionArray) {
        requireNonNull(criterionArray, "criterion must be defined.");
        List<Criteria> criterion = Arrays.asList(criterionArray);
        JsonParser parser = new JsonParser();

        Request.Builder builder = new Request.Builder();
        String url = computeUrl(esType, SEARCH_ACTION);
        String query = generateQuery(criterion);
        if (LOGGER.isTraceEnabled()) {
            JsonElement jsonEl = parser.parse(query);
            Gson prettyGson = new GsonBuilder().setPrettyPrinting().create();
            String json = prettyGson.toJson(jsonEl);
            LOGGER.trace("Sending following query on url '{}':\n{}", url, json);
        }
        Request request = builder.url(url)
                .post(RequestBody.create(JSON_MINETYPE, query))
                .build();
        Try<HttpResponse> httpResponses = executeRequest(request);
        Try<List<T>> res = httpResponses.map(httpResponse -> {
            String body = httpResponse.getBody();

            JsonObject json = (JsonObject) parser.parse(body);

            JsonArray hits = json.getAsJsonObject(HITS_ATTRIBUTES).getAsJsonArray(HITS_ATTRIBUTES);
            Gson gson = this.gson.get();
            List<T> dtos = new ArrayList<>();
            for (JsonElement el : hits) {
                JsonObject element = (JsonObject) el;
                T dto = gson.fromJson(element.getAsJsonObject(SOURCE_ATTRIBUTE), classe);
                dtos.add(dto);
            }
            return dtos;
        });
        return res.getOption();

    }

    protected String provideIdAttribute(String index) {
        return IDENTIFIER;
    }

    protected String computeUrl(String type, String action) {
        String url = endWithSeparator(elasticSearchConfig.url());
        StringBuilder sb = new StringBuilder(url)
                .append(endWithSeparator(elasticSearchConfig.indexName()))
                .append(endWithSeparator(type));

        if (isNotBlank(action)) {
            sb.append(action);
        }
        return sb.toString();
    }

    protected String computeUrl(String type) {
        return computeUrl(type, null);
    }

    private String endWithSeparator(String str) {
        if (!str.endsWith("/")) {
            str = str + SPERATOR;
        }
        return str;
    }

    protected static class HttpResponse {

        private final int code;

        private final String body;

        private HttpResponse(int code, String body) {
            this.code = code;
            this.body = body;
        }

        public int getCode() {
            return code;
        }

        public String getBody() {
            return body;
        }
    }

    protected static final String ID_ATTRIBUTE = "_id";

    protected static final String SOURCE_ATTRIBUTE = "_source";

    protected static final String FOUND_ATTRIBUTE = "found";

    protected static final String HITS_ATTRIBUTES = "hits";

    protected static final MediaType JSON_MINETYPE = MediaType.parse("application/json");

    protected static final String UPDATE_ACTION = "_update";

    protected static final String SEARCH_ACTION = "_search";

    protected static final char SPERATOR = '/';

}
