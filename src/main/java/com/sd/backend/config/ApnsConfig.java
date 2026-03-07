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
    public ApnsClient apnsClient() throws Exception {
        InputStream keyInputStream = new ClassPathResource(keyFile).getInputStream();
        ApnsSigningKey signingKey = ApnsSigningKey.loadFromInputStream(keyInputStream, teamId, keyId);

        return new ApnsClientBuilder()
                .setSigningKey(signingKey)
                .setApnsServer(ApnsClientBuilder.PRODUCTION_APNS_HOST)
                .build();
    }
}
