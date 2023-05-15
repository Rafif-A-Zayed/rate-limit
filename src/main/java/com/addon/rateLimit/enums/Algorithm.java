package com.addon.rateLimit.enums;

import lombok.Getter;

import java.util.Optional;

@Getter
public enum Algorithm {

    Fixed_Window("fixed-window");

    private final String code;

    Algorithm(String code) {
        this.code = code;
    }

    public static Optional<Algorithm> getAlgorithmByCode(String code) {
        for (Algorithm algorithm : Algorithm.values()) {
            if (algorithm.code.equals(code)) {
                return Optional.of(algorithm);
            }
        }
        return Optional.empty();
    }
}
