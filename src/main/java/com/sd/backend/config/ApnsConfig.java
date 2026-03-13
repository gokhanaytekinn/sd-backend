package com.sd.backend.config;

import com.eatthepath.pushy.apns.ApnsClient;
import com.eatthepath.pushy.apns.ApnsClientBuilder;
import com.eatthepath.pushy.apns.auth.ApnsSigningKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import java.io.InputStream;

@Configuration
public class ApnsConfig {

    @Value("${apns.key-id:PLACEHOLDER_KEY_ID}")
    private String keyId;

    @Value("${apns.team-id:U5R64N99GG}")
    private String teamId;

    @Value("${apns.key-file:apns_key.p8}")
    private String keyFile;

    @Value("${apns.sandbox:false}")
    private boolean isSandbox;

    @Bean
    public ApnsClient apnsClient() throws Exception {
        if ("PLACEHOLDER_KEY_ID".equals(keyId)) {
            return null;
        }

        try (InputStream keyInputStream = new ClassPathResource(keyFile).getInputStream()) {
            ApnsSigningKey signingKey = ApnsSigningKey.loadFromInputStream(keyInputStream, teamId, keyId);

            String host = isSandbox ? ApnsClientBuilder.DEVELOPMENT_APNS_HOST : ApnsClientBuilder.PRODUCTION_APNS_HOST;
            
            System.out.println("DEBUG: Initializing APNs Client - Sandbox: " + isSandbox + ", Host: " + host + ", BundleId: " + System.getProperty("apns.bundle-id"));

            return new ApnsClientBuilder()
                    .setSigningKey(signingKey)
                    .setApnsServer(host)
                    .build();
        }
    }
}
