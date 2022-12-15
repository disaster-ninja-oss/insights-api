package io.kontur.insightsapi.configuration;

import com.nimbusds.jose.shaded.json.JSONArray;
import com.nimbusds.jose.shaded.json.JSONObject;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;

import java.util.Collection;
import java.util.List;


@RequiredArgsConstructor
@EnableWebSecurity
@Configuration
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class WebSecurityConfiguration {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .csrf()
                .disable()
                .authorizeRequests(auth -> auth
                        .antMatchers("/graphql/**", "/graphiql/**", "/vendor/graphiql/**", "/tiles/**",
                                "/cache/cleanUp", "/health/liveness", "/health/readiness", "/metrics",
                                "/v3/api-docs/**", "/swagger-ui/**").permitAll()
                        .anyRequest().authenticated()//TODO: secure other endpoints in future
                )
                .oauth2ResourceServer(resourceServerConfigurer -> resourceServerConfigurer
                        .jwt(jwtConfigurer -> jwtConfigurer.jwtAuthenticationConverter(
                                jwtAuthenticationConverter()))
                )
                .build();
    }

    @Bean
    public Converter<Jwt, AbstractAuthenticationToken> jwtAuthenticationConverter() {
        JwtAuthenticationConverter jwtAuthenticationConverter = new JwtAuthenticationConverter();
        jwtAuthenticationConverter.setJwtGrantedAuthoritiesConverter(
                jwtGrantedAuthoritiesConverter());
        return jwtAuthenticationConverter;
    }

    @Bean
    public Converter<Jwt, Collection<GrantedAuthority>> jwtGrantedAuthoritiesConverter() {
        JwtGrantedAuthoritiesConverter converter = new JwtGrantedAuthoritiesConverter();
        return new Converter<Jwt, Collection<GrantedAuthority>>() {
            @Override
            public Collection<GrantedAuthority> convert(Jwt jwt) {
                Collection<GrantedAuthority> grantedAuthorities = converter.convert(jwt);
                if (jwt.hasClaim(ClaimParams.RESOURCE_ACCESS)) {
                    JSONObject resourceAccess = jwt.getClaim(ClaimParams.RESOURCE_ACCESS);
                    if (resourceAccess.containsKey("insights-api")) {
                        JSONObject insightsApi = (JSONObject) resourceAccess.get(ClaimParams.INSIGHTS_API_CLIENT);
                        if (insightsApi.containsKey(ClaimParams.ROLES)) {
                            JSONArray roles = (JSONArray) insightsApi.get(ClaimParams.ROLES);
                            List<SimpleGrantedAuthority> keycloakAuthorities = roles.stream()
                                    .map(role -> new SimpleGrantedAuthority((String) role))
                                    .toList();
                            grantedAuthorities.addAll(keycloakAuthorities);
                        }
                    }
                }
                if (jwt.hasClaim(ClaimParams.USERNAME)) {
                    grantedAuthorities.add(new SimpleGrantedAuthority(
                            ClaimParams.USERNAME_PREFIX + jwt.getClaim(ClaimParams.USERNAME)));
                }
                return grantedAuthorities;
            }
        };
    }

    public static class ClaimParams {
        public static final String USERNAME_PREFIX = "USERNAME_";

        public static final String RESOURCE_ACCESS = "resource_access";

        public static final String INSIGHTS_API_CLIENT = "insights-api";
        public static final String ROLES = "roles";
        public static final String USERNAME = "username";
    }
}
