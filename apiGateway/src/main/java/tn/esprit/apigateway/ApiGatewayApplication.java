package tn.esprit.apigateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class ApiGatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(ApiGatewayApplication.class, args);
    }

    @Bean
    public RouteLocator dynamicRoutes(RouteLocatorBuilder builder,
                                      DiscoveryClient discoveryClient) {

        RouteLocatorBuilder.Builder routes = builder.routes();

        discoveryClient.getServices().forEach(serviceId -> {
            System.out.println("Gateway route exposed for service: " + serviceId);

            routes.route(serviceId, r -> r
                    .path("/apiGateway/" + serviceId + "/**") // prefix with /apiGateway
                    .filters(f -> f.stripPrefix(2))          // remove /apiGateway/serviceId from path before forwarding
                    .uri("lb://" + serviceId));
        });

        return routes.build();
    }

}
