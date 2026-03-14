package com.sd.backend.dto;

import lombok.Data;
import java.util.List;

@Data
public class ApplePublicKeyResponse {
    private List<AppleJWK> keys;

    @Data
    public static class AppleJWK {
        private String kty;
        private String kid;
        private String use;
        private String alg;
        private String n;
        private String e;
    }
}
