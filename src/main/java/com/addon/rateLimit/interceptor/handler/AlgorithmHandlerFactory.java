package com.addon.rateLimit.interceptor.handler;

import com.addon.rateLimit.enums.Algorithm;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Component
public class AlgorithmHandlerFactory {

    private final FixedWindowHandler fixedWindowHandler;
    private static final Map<Algorithm, AlgorithmHandler> handlerHashMap = new EnumMap<>(Algorithm.class);

    @Autowired
    private AlgorithmHandlerFactory(List<AlgorithmHandler> services, FixedWindowHandler fixedWindowHandler) {
        services.forEach((service) -> handlerHashMap.put(service.getAlgorithm(), service));
        this.fixedWindowHandler = fixedWindowHandler;
    }

    public AlgorithmHandler getHandler(Algorithm algorithm) {
        AlgorithmHandler handler = handlerHashMap.get(algorithm);
        // default handler fixedWindowHandler
        return handler == null? fixedWindowHandler: handler;
    }
}


