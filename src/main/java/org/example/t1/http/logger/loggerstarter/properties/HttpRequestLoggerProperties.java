package org.example.t1.http.logger.loggerstarter.properties;

import lombok.Data;
import org.example.t1.http.logger.loggerstarter.model.LogLevel;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "t1.http.request.logger")
@Data
public class HttpRequestLoggerProperties {
    private boolean enabled = true;
    private LogLevel logLevel = LogLevel.DEBUG;
}