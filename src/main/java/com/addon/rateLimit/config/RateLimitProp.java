package com.addon.rateLimit.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@ConfigurationProperties(prefix = "rate-limit")
@EnableConfigurationProperties
@Configuration
@Getter
@Setter
public class RateLimitProp {


    private boolean enabled = true;
    private Default defaults;
    private List<Client> clients;

    @Setter
    @Getter
    public static class Default {
        private String algorithm;
        private FixedWindow fixedWindow;
    }

    @Setter
    @Getter
    public static class Client {

        private String id;
        private boolean enabled = true;
        private boolean perUser;
        private String algorithm;
        private FixedWindow fixedWindow;
    }

    @Setter
    @Getter
    public static class FixedWindow {
        private int limit;
        private long per;
    }



}
