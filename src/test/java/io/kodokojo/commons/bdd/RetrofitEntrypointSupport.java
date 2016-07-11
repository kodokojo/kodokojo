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
package io.kodokojo.commons.bdd;

/*
 * #%L
 * commons-tests
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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import retrofit.RetrofitError;

public class RetrofitEntrypointSupport {

    private static final Logger LOGGER = LoggerFactory.getLogger(RetrofitEntrypointSupport.class);

    private RetrofitEntrypointSupport() {
        // Utility class.
    }

    public static <T> T retriveFromRestEntrypoint(RetrofitEntrypointCallback<T> callback, int timeout) {
        T res = null;
        long end = System.currentTimeMillis() + timeout;
        long begin = System.currentTimeMillis();
        long now;
        int nbTry =0;
        do {
            nbTry++;
            now = System.currentTimeMillis();
            try {
                res = callback.execute();
            } catch (RetrofitError retrofitError) {
                if (retrofitError.getResponse().getStatus() != 404) {
                    throw  retrofitError;
                }
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        } while ((end - now > 0) && res == null);
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Had following result after {} try {} millis : {}", nbTry, end - begin ,res);
        }
        return res;
    }

}
