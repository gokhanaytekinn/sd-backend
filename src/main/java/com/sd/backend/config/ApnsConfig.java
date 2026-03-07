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

    @Bean
    public ApnsClient productionApnsClient() throws Exception {
        return createClient(ApnsClientBuilder.PRODUCTION_APNS_HOST);
    }

    @Bean
    public ApnsClient sandboxApnsClient() throws Exception {
        return createClient(ApnsClientBuilder.DEVELOPMENT_APNS_HOST);
    }

    private ApnsClient createClient(String host) throws Exception {
        if ("PLACEHOLDER_KEY_ID".equals(keyId)) {
            // Return a dummy client or handle gracefully during initial setup
            return null;
        }

        try (InputStream keyInputStream = new ClassPathResource(keyFile).getInputStream()) {
            ApnsSigningKey signingKey = ApnsSigningKey.loadFromInputStream(keyInputStream, teamId, keyId);

            return new ApnsClientBuilder()
                    .setSigningKey(signingKey)
                    .setApnsServer(host)
                    .build();
        }
    }
}
