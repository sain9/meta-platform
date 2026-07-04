package com.hussain.gateway_api.filter;

import java.util.Collection;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

import reactor.core.publisher.Mono;

@Component
public class JwtLoggingFilter implements GlobalFilter, Ordered {

    private static final Logger log = LoggerFactory.getLogger(JwtLoggingFilter.class);

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {

        return exchange.getPrincipal()
                .cast(Authentication.class)
                .doOnNext(authentication -> {

                    if (authentication.getPrincipal() instanceof Jwt jwt) {

                        String username = jwt.getClaimAsString("preferred_username");
                        String email = jwt.getClaimAsString("email");
                        String userId = jwt.getSubject();

                        log.info("=================================================");
                        log.info("Authenticated User");
                        log.info("User ID  : {}", userId);
                        log.info("Username : {}", username);
                        log.info("Email    : {}", email);

                        Map<String, Object> realmAccess = jwt.getClaim("realm_access");

                        if (realmAccess != null) {
                            Collection<?> roles = (Collection<?>) realmAccess.get("roles");
                            log.info("Realm Roles : {}", roles);
                        }

                        Map<String, Object> resourceAccess = jwt.getClaim("resource_access");
                        log.info("Client Roles : {}", resourceAccess);

                        log.info("=================================================");
                    }

                })
                .then(chain.filter(exchange));
    }

    @Override
    public int getOrder() {
        return -1;
    }
}