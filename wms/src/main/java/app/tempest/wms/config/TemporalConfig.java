package app.tempest.wms.config;

import io.grpc.Metadata;
import io.grpc.stub.MetadataUtils;
import io.temporal.client.WorkflowClient;
import io.temporal.client.WorkflowClientOptions;
import io.temporal.serviceclient.SimpleSslContextBuilder;
import io.temporal.serviceclient.WorkflowServiceStubs;
import io.temporal.serviceclient.WorkflowServiceStubsOptions;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

import javax.net.ssl.SSLException;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;

/**
 * Temporal client configuration.
 * Supports three authentication modes:
 * 1. Local development (no auth) - default
 * 2. API Key authentication (for Temporal Cloud)
 * 3. mTLS certificate authentication (for Temporal Cloud)
 */
@Configuration
public class TemporalConfig {

     private static final Logger log = LoggerFactory.getLogger(TemporalConfig.class);

     // Temporal Cloud authorization header for API key
     private static final Metadata.Key<String> AUTHORIZATION_HEADER = Metadata.Key.of("Authorization",
               Metadata.ASCII_STRING_MARSHALLER);

     @Value("${temporal.address:localhost:7233}")
     private String temporalAddress;

     @Value("${temporal.namespace:default}")
     private String temporalNamespace;

     @Value("${temporal.api-key:}")
     private String temporalApiKey;

     @Value("${temporal.client.cert:}")
     private String clientCert;

     @Value("${temporal.client.key:}")
     private String clientKey;

     @Bean
     public WorkflowServiceStubs workflowServiceStubs() {
          WorkflowServiceStubsOptions.Builder optionsBuilder = WorkflowServiceStubsOptions.newBuilder()
                    .setTarget(temporalAddress);

          // Check for API Key authentication first (preferred for Temporal Cloud)
          if (StringUtils.hasText(temporalApiKey)) {
               log.info("Configuring Temporal with API Key authentication");
               configureApiKeyAuth(optionsBuilder);
          }
          // Fall back to mTLS certificate authentication
          else if (StringUtils.hasText(clientCert) && StringUtils.hasText(clientKey)) {
               log.info("Configuring Temporal with mTLS certificate authentication");
               configureMtlsAuth(optionsBuilder);
          }
          // Local development - no authentication
          else {
               log.info("Configuring Temporal for local development (no authentication)");
          }

          return WorkflowServiceStubs.newServiceStubs(optionsBuilder.build());
     }

     /**
      * Configure API Key authentication for Temporal Cloud.
      * Uses Bearer token in Authorization header.
      */
     private void configureApiKeyAuth(WorkflowServiceStubsOptions.Builder optionsBuilder) {
          // Enable TLS for Temporal Cloud
          try {
               optionsBuilder.setSslContext(SimpleSslContextBuilder.noKeyOrCertChain().build());
          } catch (SSLException e) {
               log.error("Failed to configure TLS for Temporal", e);
               throw new IllegalStateException("Failed to configure TLS for Temporal", e);
          }

          // Add API key as Bearer token
          Metadata metadata = new Metadata();
          metadata.put(AUTHORIZATION_HEADER, "Bearer " + temporalApiKey);

          optionsBuilder.setChannelInitializer(
                    channel -> channel.intercept(MetadataUtils.newAttachHeadersInterceptor(metadata)));
     }

     /**
      * Configure mTLS certificate authentication for Temporal Cloud.
      */
     private void configureMtlsAuth(WorkflowServiceStubsOptions.Builder optionsBuilder) {
          try {
               optionsBuilder.setSslContext(SimpleSslContextBuilder.forPKCS8(
                         new ByteArrayInputStream(clientCert.getBytes(StandardCharsets.UTF_8)),
                         new ByteArrayInputStream(clientKey.getBytes(StandardCharsets.UTF_8))).build());
          } catch (SSLException e) {
               throw new IllegalStateException("Failed to configure mTLS for Temporal", e);
          }
     }

     @Bean
     public WorkflowClient workflowClient(WorkflowServiceStubs serviceStubs) {
          return WorkflowClient.newInstance(serviceStubs,
                    WorkflowClientOptions.newBuilder()
                              .setNamespace(temporalNamespace)
                              .build());
     }
}
