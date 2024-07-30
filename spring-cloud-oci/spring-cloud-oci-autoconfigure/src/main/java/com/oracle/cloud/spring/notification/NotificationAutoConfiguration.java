/*
 ** Copyright (c) 2023, Oracle and/or its affiliates.
 ** Licensed under the Universal Permissive License v 1.0 as shown at https://oss.oracle.com/licenses/upl/
 */

package com.oracle.cloud.spring.notification;

import com.oracle.bmc.auth.RegionProvider;
import com.oracle.bmc.ons.NotificationControlPlane;
import com.oracle.bmc.ons.NotificationControlPlaneClient;
import com.oracle.bmc.ons.NotificationDataPlane;
import com.oracle.bmc.ons.NotificationDataPlaneClient;
import com.oracle.cloud.spring.autoconfigure.core.CredentialsProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;

import static com.oracle.cloud.spring.autoconfigure.core.CredentialsProviderAutoConfiguration.credentialsProviderQualifier;
import static com.oracle.cloud.spring.autoconfigure.core.RegionProviderAutoConfiguration.regionProviderQualifier;

/**
 * Auto-configuration for initializing the OCI Notification component.
 * Depends on {@link com.oracle.cloud.spring.autoconfigure.core.CredentialsProviderAutoConfiguration} and
 * {@link com.oracle.cloud.spring.autoconfigure.core.RegionProviderAutoConfiguration}
 * for loading the Authentication configuration
 *
 * @see com.oracle.cloud.spring.notification.Notification
 */
@AutoConfiguration
@ConditionalOnClass({Notification.class})
@ConditionalOnProperty(name = "spring.cloud.oci.notification.enabled", havingValue = "true", matchIfMissing = true)
public class NotificationAutoConfiguration {

    @Bean
    @RefreshScope
    @ConditionalOnMissingBean(Notification.class)
    Notification getNotificationImpl(NotificationDataPlane notificationDataPlane, NotificationControlPlane notificationControlPlane ) {
        return new NotificationImpl(notificationDataPlane, notificationControlPlane);
    }

    @Bean
    @RefreshScope
    @ConditionalOnMissingBean
    NotificationDataPlane notificationDataPlaneClient(@Qualifier(regionProviderQualifier) RegionProvider regionProvider,
                                                      @Qualifier(credentialsProviderQualifier)
                                                              CredentialsProvider cp) {
        NotificationDataPlane notificationDataPlaneClient = NotificationDataPlaneClient.builder().build(cp.getAuthenticationDetailsProvider());
        if (regionProvider.getRegion() != null) notificationDataPlaneClient.setRegion(regionProvider.getRegion());
        return notificationDataPlaneClient;
    }

    @Bean
    @RefreshScope
    @ConditionalOnMissingBean
    NotificationControlPlane notificationControlPlaneClient(@Qualifier(regionProviderQualifier) RegionProvider regionProvider,
                                                            @Qualifier(credentialsProviderQualifier)
                                                                    CredentialsProvider cp) {
        NotificationControlPlane notificationControlPlaneClient = NotificationControlPlaneClient.builder().build(cp.getAuthenticationDetailsProvider());
        if (regionProvider.getRegion() != null) notificationControlPlaneClient.setRegion(regionProvider.getRegion());
        return notificationControlPlaneClient;
    }
}
