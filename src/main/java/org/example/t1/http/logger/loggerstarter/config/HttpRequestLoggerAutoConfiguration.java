package org.example.t1.http.logger.loggerstarter.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.t1.http.logger.loggerstarter.aspect.HttpRequestResponseLoggingAspect;
import org.example.t1.http.logger.loggerstarter.properties.HttpRequestLoggerProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(HttpRequestLoggerProperties.class)
@ConditionalOnProperty(prefix = "t1.http.request.logger", name = "enabled", havingValue = "true", matchIfMissing = true)
public class HttpRequestLoggerAutoConfiguration {

    @Bean
    public HttpRequestResponseLoggingAspect httpRequestResponseLoggingAspect(HttpRequestLoggerProperties httpLoggingProperties, ObjectMapper objectMapper) {
        return new HttpRequestResponseLoggingAspect(httpLoggingProperties, objectMapper);
    }

    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }

}
