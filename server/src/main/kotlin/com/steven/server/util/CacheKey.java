package com.steven.server.util;

public enum CacheKey {

    MODEL_KEY("federated_v1_model_hash_"),
    PARAM_KEY("federated_v1_param_hash_");

    private final String key;

    CacheKey(String key) {
        this.key = key;
    }

    public String getKey() {
        return key;
    }
}
