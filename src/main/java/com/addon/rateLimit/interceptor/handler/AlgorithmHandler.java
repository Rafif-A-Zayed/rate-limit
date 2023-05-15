package com.addon.rateLimit.interceptor.handler;


import com.addon.rateLimit.config.RateLimitProp;
import com.addon.rateLimit.enums.Algorithm;

public interface AlgorithmHandler {

    void handle(RateLimitProp.Client client, String api, String user) ;
    void handleRollback(RateLimitProp.Client client, String api, String user) ;
    Algorithm getAlgorithm();
}
