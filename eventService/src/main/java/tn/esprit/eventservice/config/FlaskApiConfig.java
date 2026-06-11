package tn.esprit.eventservice.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "flask.api")
public class FlaskApiConfig {

    private String url;
    private String token;

    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }

}
