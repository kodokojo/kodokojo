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
package io.kodokojo.commons.config.properties.provider.kv;

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


import io.kodokojo.commons.config.properties.provider.AbstarctStringPropertyValueProvider;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.Stat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.io.IOException;

import static org.apache.commons.lang.StringUtils.isBlank;

public class ZookeeperPropertyValueProvider extends AbstarctStringPropertyValueProvider implements Closeable {

    private static final Logger LOGGER = LoggerFactory.getLogger(ZookeeperPropertyValueProvider.class);

    private final KeyToZookeeperPathConverter keyToZookeeperPathConverter;

    private final ZooKeeper client;

    private final String zookeeperUrl;

    public ZookeeperPropertyValueProvider(String zookeeperUrl, KeyToZookeeperPathConverter keyToZookeeperPathConverter) {
        if (isBlank(zookeeperUrl)) {
            throw new IllegalArgumentException("zookeeperUrl must be defined.");
        }
        this.zookeeperUrl = zookeeperUrl;
        try {
            client = new ZooKeeper(zookeeperUrl, 1000, (event -> {
            }), true);
        } catch (IOException e) {
            throw new IllegalStateException("Unable to create a Zookeeper client with url " + zookeeperUrl, e);
        }
        this.keyToZookeeperPathConverter = keyToZookeeperPathConverter;
    }

    public ZookeeperPropertyValueProvider(String zookeeperUrl) {
        this(zookeeperUrl, null);
    }

    @Override
    protected String provideValue(String key) {
        if (isBlank(key)) {
            throw new IllegalArgumentException("key must be defined.");
        }
        if (keyToZookeeperPathConverter != null) {
            key = keyToZookeeperPathConverter.convert(key);
        }
        try {
            Stat exists = client.exists(key, false);
            if (exists != null) {
                byte[] data = client.getData(key, false, exists);
                return new String(data);
            } else if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Path " + key + " not exist.");
            }
        } catch (KeeperException | InterruptedException e) {
            LOGGER.error("Unable to request Zookeeper instance url " + zookeeperUrl, e);
        }

        return null;
    }


    @Override
    public void close() throws IOException {
        if (client != null) {
            try {
                client.close();
            } catch (InterruptedException e) {
                LOGGER.error("Unable to close Zookeeper client.", e);
            }
        }
    }
}
