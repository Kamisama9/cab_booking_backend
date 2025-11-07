package com.cts.api_gateway.filter;

import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.List;

@Component
public class RoleAuthorizationFilter extends AbstractGatewayFilterFactory<RoleAuthorizationFilter.Config> {

    public RoleAuthorizationFilter() {
        super(Config.class);
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            String userRole = exchange.getRequest().getHeaders().getFirst("X-User-Role");

            if (userRole == null) {
                return onError(exchange, "Role not found in token", HttpStatus.FORBIDDEN);
            }

            // Support multiple allowed roles
            List<String> allowedRoles = config.getAllowedRoles();
            boolean hasPermission = allowedRoles.stream()
                    .anyMatch(role -> role.equalsIgnoreCase(userRole));

            if (!hasPermission) {
                return onError(exchange,
                        "Access denied. Required roles: " + allowedRoles +
                                ", but you have: " + userRole,
                        HttpStatus.FORBIDDEN);
            }

            return chain.filter(exchange);
        };
    }

    private Mono<Void> onError(ServerWebExchange exchange, String error, HttpStatus status) {
        exchange.getResponse().setStatusCode(status);
        exchange.getResponse().getHeaders().add("X-Error-Message", error);
        return exchange.getResponse().setComplete();
    }

    public static class Config {
        private List<String> allowedRoles;

        public List<String> getAllowedRoles() {
            return allowedRoles;
        }

        public void setAllowedRoles(List<String> allowedRoles) {
            this.allowedRoles = allowedRoles;
        }

        // Convenience method for single role
        public void setRequiredRole(String role) {
            this.allowedRoles = Arrays.asList(role);
        }
    }
}