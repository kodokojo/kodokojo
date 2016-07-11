/**
 * Kodo Kojo - Software factory done right
 * Copyright © 2016 Kodo Kojo (infos@kodokojo.io)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package io.kodokojo.commons.utils.properties.provider.kv;

/*
 * #%L
 * commons-commons
 * %%
 * Copyright (C) 2016 Kodo-kojo
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */


import com.google.gson.*;
import io.kodokojo.commons.utils.properties.provider.AbstarctStringPropertyValueProvider;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.converter.GsonConverter;
import retrofit.http.GET;
import retrofit.http.Path;

import java.util.Base64;

import static org.apache.commons.lang.StringUtils.isBlank;

//  TODO Add tests
public class ConsulKvPropertyValueProvider extends AbstarctStringPropertyValueProvider {

    private final ConsulKvRest consulKvRest;

    public ConsulKvPropertyValueProvider(String baseUrl) {
        if (isBlank(baseUrl)) {
            throw new IllegalArgumentException("baseUrl must be defined.");
        }
        Gson gson = new GsonBuilder().create();
        RestAdapter restAdapter = new RestAdapter.Builder().setEndpoint(baseUrl).setConverter(new GsonConverter(gson)).build();
        consulKvRest = restAdapter.create(ConsulKvRest.class);
    }

    @Override
    protected String provideValue(String key) {
        if (isBlank(key)) {
            throw new IllegalArgumentException("key must be defined.");
        }
        try {
            JsonArray values = consulKvRest.getValueResult(key);
            JsonObject json = values.get(0).getAsJsonObject();

            String res = null;
            if (json != null) {
                JsonPrimitive primitive = json.getAsJsonPrimitive("Value");
                if (primitive != null) {
                    String value = primitive.getAsString();
                    res = new String(Base64.getDecoder().decode(value));
                }
            }
            return res;
        } catch (RetrofitError e) {
            if ("404 Not Found".equals(e.getMessage())) {
                return null;
            }
            throw e;
        }
    }

    interface ConsulKvRest {
        @GET("/v1/kv/{key}")
        JsonArray getValueResult(@Path("key") String key);
    }

}
