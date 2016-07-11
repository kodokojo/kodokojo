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
package io.kodokojo.commons.utils.servicelocator.consul;


import com.google.gson.Gson;
import retrofit.RestAdapter;
import retrofit.converter.GsonConverter;

public class ConsulRestFactory {

    private ConsulRestFactory() {
        // Util class.
    }

    public static ConsulRest build(String baseUrl, Gson gson) {
        if (baseUrl == null) {
            throw new IllegalArgumentException("baseUrl must be defined.");
        }
        if (gson == null) {
            throw new IllegalArgumentException("gson must be defined.");
        }
        RestAdapter restAdapter = new RestAdapter.Builder().setEndpoint(baseUrl).setConverter(new GsonConverter(gson)).build();
        return restAdapter.create(ConsulRest.class);
    }


}
