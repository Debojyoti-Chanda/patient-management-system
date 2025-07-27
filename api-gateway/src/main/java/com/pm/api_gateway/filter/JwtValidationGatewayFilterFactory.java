package com.pm.api_gateway.filter;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

@Component
public class JwtValidationGatewayFilterFactory extends AbstractGatewayFilterFactory<Object> {

    private final WebClient webClient;

    public JwtValidationGatewayFilterFactory(WebClient.Builder webClientBuilder,
                                             @Value("${auth.service.url}") String authServiceUrl ){
        this.webClient = webClientBuilder.baseUrl(authServiceUrl).build();
    }

    @Override
    public GatewayFilter apply(Object config) {
        //  When Spring Cloud Gateway processes a route that uses this filter, this method is called to create an instance of GatewayFilter.
        // This returns a lambda expression that implements the GatewayFilter interface. This lambda defines the actual filtering logic that will be applied to each request.
        // exchange: Represents the current server exchange, providing access to the request and response.
        // chain: Represents the GatewayFilterChain, allowing you to delegate to the next filter in the chain.

        return (exchange, chain) -> {
            String token = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
            if(token == null ||  !token.startsWith("Bearer ")){
                 exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                 return exchange.getResponse().setComplete();
            }
            return webClient.get()
                    .uri("/validate")
                    .header(HttpHeaders.AUTHORIZATION,token)
                    .retrieve()  // Initiates the retrieval of the response from the authentication service.
                    .onStatus(HttpStatusCode::is4xxClientError, clientResponse -> {
                        // If auth service returns 4xx, consider it an unauthorized access
                        // You could log the specific status here for debugging
                        // e.g., log.warn("Auth service returned 4xx: {}", clientResponse.statusCode());
                        return Mono.error(new WebClientResponseException(
                                clientResponse.statusCode().value(),
                                "Unauthorized by auth service",
                                clientResponse.headers().asHttpHeaders(),
                                null, // body, if needed
                                null // charset, if needed
                        ));
                    })
                    .onStatus(HttpStatusCode::is5xxServerError, clientResponse -> {
                        // If auth service returns 5xx, consider it an internal error of the auth service
                        // You might want to map this to a 500 or just re-throw
                        // log.error("Auth service returned 5xx: {}", clientResponse.statusCode());
                        return Mono.error(new RuntimeException("Authentication service internal error"));
                    })
                    .toBodilessEntity()
                    .then(chain.filter(exchange))
                    .onErrorResume(WebClientResponseException.class, ex -> {
                        // This block handles 4xx errors specifically from the WebClient call
                        if (ex.getStatusCode() == HttpStatus.UNAUTHORIZED || ex.getStatusCode() == HttpStatus.FORBIDDEN) {
                            exchange.getResponse().setStatusCode(ex.getStatusCode());
                        } else {
                            // For other 4xx errors (e.g., Bad Request from auth service) or general client errors
                            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED); // Or HttpStatus.BAD_GATEWAY
                        }
                        return exchange.getResponse().setComplete();
                    })
                    .onErrorResume(RuntimeException.class, ex -> {
                        // This catches any other RuntimeExceptions, including our custom one for 5xx from auth service
                        // This could also catch network errors, etc.
                        // Log the error for debugging purposes
                        // e.error("Error calling authentication service: {}", ex.getMessage());
                        exchange.getResponse().setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR);
                        return exchange.getResponse().setComplete();
                    });
        };
    }
}
