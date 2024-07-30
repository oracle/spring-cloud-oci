/*
 ** Copyright (c) 2023, Oracle and/or its affiliates.
 ** Licensed under the Universal Permissive License v 1.0 as shown at https://oss.oracle.com/licenses/upl/
 */

package com.oracle.cloud.spring.storage;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.oracle.bmc.auth.RegionProvider;
import com.oracle.bmc.objectstorage.ObjectStorageClient;
import com.oracle.cloud.spring.autoconfigure.core.CredentialsProvider;
import com.oracle.cloud.spring.core.compartment.CompartmentProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;

import java.util.Optional;

import static com.oracle.cloud.spring.autoconfigure.core.CredentialsProviderAutoConfiguration.credentialsProviderQualifier;
import static com.oracle.cloud.spring.autoconfigure.core.RegionProviderAutoConfiguration.regionProviderQualifier;

/**
 * Auto-configuration for initializing the OCI Storage component.
 * Depends on {@link com.oracle.cloud.spring.autoconfigure.core.CompartmentProviderAutoConfiguration},
 * {@link com.oracle.cloud.spring.autoconfigure.core.CredentialsProviderAutoConfiguration} and
 * {@link com.oracle.cloud.spring.autoconfigure.core.RegionProviderAutoConfiguration}
 * for loading the Authentication configuration
 *
 * @see com.oracle.cloud.spring.storage.Storage
 */
@ConditionalOnClass({OracleStorageProtocolResolver.class, ObjectStorageClient.class})
@AutoConfiguration
@Import(OracleStorageProtocolResolver.class)
@ConditionalOnProperty(name = "spring.cloud.oci.storage.enabled", havingValue = "true", matchIfMissing = true)
public class StorageAutoConfiguration {

    @Bean
    @RefreshScope
    @ConditionalOnMissingBean(Storage.class)
    @ConditionalOnBean(StorageObjectConverter.class)
    Storage storageActions(ObjectStorageClient osClient, StorageObjectConverter storageObjectConverter,
                           Optional<StorageContentTypeResolver> contentTypeResolver,
                           CompartmentProvider compartmentProvider) {
        return new StorageImpl(osClient, storageObjectConverter,
                contentTypeResolver.orElseGet(StorageContentTypeResolverImpl::new),
                compartmentProvider.getCompartmentOCID());
    }

    @Bean
    @RefreshScope
    @ConditionalOnMissingBean
    ObjectStorageClient objectStorageClient(@Qualifier(regionProviderQualifier) RegionProvider regionProvider,
                                            @Qualifier(credentialsProviderQualifier)
                                                    CredentialsProvider cp) {
        ObjectStorageClient osClient = ObjectStorageClient.builder().build(cp.getAuthenticationDetailsProvider());
        if (regionProvider.getRegion() != null) osClient.setRegion(regionProvider.getRegion());
        return osClient;
    }

    @AutoConfiguration
    @ConditionalOnClass(ObjectMapper.class)
    static class JacksonJSONStorageObjectConverterConfiguration {

        @ConditionalOnMissingBean
        @Bean
        StorageObjectConverter storageObjectConverter(Optional<ObjectMapper> objectMapper) {
            return new JacksonJSONStorageObjectConverter(objectMapper.orElseGet(ObjectMapper::new));
        }
    }

}
