/**
 * Kodo Kojo - Software factory done right
 * Copyright © 2017 Kodo Kojo (infos@kodokojo.io)
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
package io.kodokojo.commons.config.module;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import io.kodokojo.commons.config.ElasticSearchConfig;
import io.kodokojo.commons.config.RedisConfig;
import io.kodokojo.commons.service.elasticsearch.ElasticSearchConfigurationSearcher;
import io.kodokojo.commons.service.lifecycle.ApplicationLifeCycleManager;
import io.kodokojo.commons.service.redis.RedisOrganisationStore;
import io.kodokojo.commons.service.redis.RedisProjectStore;
import io.kodokojo.commons.service.redis.RedisUserRepository;
import io.kodokojo.commons.service.repository.*;
import io.kodokojo.commons.service.repository.store.OrganisationStore;
import io.kodokojo.commons.service.repository.store.ProjectStore;
import okhttp3.OkHttpClient;

import javax.crypto.SecretKey;
import javax.inject.Named;

public class DatabaseModule extends AbstractModule {

    @Override
    protected void configure() {
        //  Nothing to do.
    }

    @Provides
    @Singleton
    Repository provideRepository(OrganisationStore organisationStore, ProjectStore projectStore, @Named("securityKey") SecretKey secretKey, RedisConfig redisConfig, ElasticSearchConfigurationSearcher elasticSearchSearcher, ApplicationLifeCycleManager applicationLifeCycleManager) {
        RedisUserRepository redisUserManager = new RedisUserRepository(secretKey, redisConfig.host(), redisConfig.port(), redisConfig.password());
        applicationLifeCycleManager.addService(redisUserManager);
        return new Repository(redisUserManager, redisUserManager, organisationStore, projectStore, elasticSearchSearcher);
    }

    @Provides
    @Singleton
    UserRepository provideUserRepository(Repository repository) {
        return repository;
    }

    @Provides
    @Singleton
    UserFetcher provideUserFetcher(UserRepository userRepository) {
        return userRepository;
    }

    @Provides
    @Singleton
    OrganisationRepository provideEntityRepository(Repository repository) {
        return repository;
    }

    @Provides
    @Singleton
    ProjectRepository provideProjectRepository(Repository repository) {
        return repository;
    }

    @Provides
    @Singleton
    ProjectFetcher provideProjectFetcher(Repository repository) {
        return repository;
    }


    @Provides
    @Singleton
    OrganisationStore provideEntityStore(@Named("securityKey") SecretKey key, RedisConfig redisConfig, ApplicationLifeCycleManager applicationLifeCycleManager) {
        RedisOrganisationStore entityStore = new RedisOrganisationStore(key, redisConfig.host(), redisConfig.port(), redisConfig.password());
        applicationLifeCycleManager.addService(entityStore);
        return entityStore;
    }

    @Provides
    @Singleton
    ProjectStore provideProjectStore(@Named("securityKey") SecretKey key, RedisConfig redisConfig, ApplicationLifeCycleManager applicationLifeCycleManager) {
        RedisProjectStore redisProjectStore = new RedisProjectStore(key, redisConfig.host(), redisConfig.port(), redisConfig.password());
        applicationLifeCycleManager.addService(redisProjectStore);
        return redisProjectStore;
    }

    @Provides
    @Singleton
    ElasticSearchConfigurationSearcher provideElasticSearchSearcher(ElasticSearchConfig elasticSearchConfig, OkHttpClient httpClient) {
        return new ElasticSearchConfigurationSearcher(elasticSearchConfig, httpClient);
    }

    @Provides
    @Singleton
    OrganisationSearcher provideOrganisationSearcher(ElasticSearchConfigurationSearcher elasticSearchSearcher) {
        return elasticSearchSearcher;
    }

    @Provides
    @Singleton
    UserSearcher provideUserSearcher(ElasticSearchConfigurationSearcher elasticSearchSearcher) {
        return elasticSearchSearcher;
    }

    @Provides
    @Singleton
    ProjectConfigurationSearcher proSoftwareFactorySearcher(ElasticSearchConfigurationSearcher elasticSearchSearcher) {
        return elasticSearchSearcher;
    }

}
