package com.addon.rateLimit.interceptor;


import com.addon.rateLimit.annotation.RateLimit;
import com.addon.rateLimit.config.RateLimitProp;
import com.addon.rateLimit.config.RateLimitProp.Client;
import com.addon.rateLimit.enums.Algorithm;
import com.addon.rateLimit.exception.RateLimitException;
import com.addon.rateLimit.identifiers.ClientIdentifier;
import com.addon.rateLimit.identifiers.UserIdentifier;
import com.addon.rateLimit.interceptor.handler.AlgorithmHandler;
import com.addon.rateLimit.interceptor.handler.AlgorithmHandlerFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

import java.util.List;
import java.util.Optional;


@Aspect
@Component
@Slf4j
@RequiredArgsConstructor
public class RateLimitInterceptor {

    public static final String FAILED_TO_GET_USER_INFO = "Failed to get user info";
    private final UserIdentifier userIdentifier;
    private final ClientIdentifier clientIdentifier;
    private final RateLimitProp rateLimitProp;
    private final AlgorithmHandlerFactory algorithmHandlerFactory;

    @Before(value = "@annotation(com.addon.rateLimit.annotation.RateLimit)")
    public void handleRateLimit(JoinPoint joinPoint) {

        if (this.rateLimitProp.isEnabled()) {
            MethodSignature signature = (MethodSignature)joinPoint.getSignature();
            Method method = signature.getMethod();
            RateLimit rateLimit = method.getAnnotation(RateLimit.class);
            RateLimitProp.Client client = this.getClient(this.rateLimitProp.getClients());
            if (null == client || client.isEnabled()) {
                boolean perUser = null == client ? rateLimit.perUser() : client.isPerUser();
                if (perUser) {
                    this.handlePerUser(client, false);
                } else {
                    this.handlePerApi(client, method.getName(), false);
                }
            }
        }

    }

    @AfterThrowing(value = "@annotation(com.addon.rateLimit.annotation.RateLimit)", throwing = "error")
    public void errorInterceptor(JoinPoint joinPoint, Exception error) {
        if (this.rateLimitProp.isEnabled()) {
            MethodSignature signature = (MethodSignature)joinPoint.getSignature();
            Method method = signature.getMethod();
            log.info("Rollback rate limit because API call has error {} for API {}", error.getMessage(), method.getName());
            RateLimit rateLimit = method.getAnnotation(RateLimit.class);
            RateLimitProp.Client client = this.getClient(this.rateLimitProp.getClients());
            if (null == client || client.isEnabled()) {
                boolean perUser = null == client ? rateLimit.perUser() : client.isPerUser();
                if (perUser) {
                    this.handlePerUser(client, true);
                } else {
                    this.handlePerApi(client, method.getName(), true);
                }
            }
        }



    }


    private Client getClient(List<Client> clients) {
        Optional<String> clientId = clientIdentifier.getClientIdentifier();

        if (clientId.isEmpty())
            return null;

        for (Client client : clients) {
            if (clientId.get().equals(client.getId()))
                return client;
        }
        return null;

    }


    private void handlePerUser(Client client, boolean rollback) {
        AlgorithmHandler algorithmHandler = getAlgorithmHandler(client);
        Optional<String> userId = userIdentifier.getUserIdentifier();
        if (userId.isEmpty()) {
            throw new RateLimitException(FAILED_TO_GET_USER_INFO, HttpStatus.FORBIDDEN);
        }
        if (rollback) {
            algorithmHandler.handleRollback(client,null, userId.get());
        } else {
            algorithmHandler.handle(client, null, userId.get());
        }
    }

    private void handlePerApi(Client client, String api, boolean rollback) {
        AlgorithmHandler algorithmHandler = getAlgorithmHandler(client);
        if (rollback) {
            algorithmHandler.handleRollback(client,api, null);
        } else {
            algorithmHandler.handle(client, api, null);
        }
    }


    private AlgorithmHandler getAlgorithmHandler(Client client) {
        String handler = null == client ? rateLimitProp.getDefaults().getAlgorithm() : client.getAlgorithm();
        // default algorithm Fixed_Window
        Algorithm algorithm = Algorithm.getAlgorithmByCode(handler).orElse(Algorithm.Fixed_Window);
        return algorithmHandlerFactory.getHandler(algorithm);
    }

}
