package com.steven.server.util;

public enum CacheKey {

    MODEL_KEY("federated_v1_model_hash"),
    PARAM_KEY("federated_v1_param_hash");

    private final String key;

    CacheKey(String key) {
        this.key = key;
    }

    public String getKey() {
        return key;
    }
}
