package io.kontur.insightsapi.service.auth;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import static io.kontur.insightsapi.configuration.WebSecurityConfiguration.ClaimParams.USERNAME_PREFIX;

@Service
@RequiredArgsConstructor
public class AuthService {

    private static List<String> getTokenClaims() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            return List.of();
        }

        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
        if (authorities == null) {
            return List.of();
        }

        return authorities.stream().map(GrantedAuthority::getAuthority)
            .collect(Collectors.toList());
    }

    public Optional<String> getCurrentUsername() {
        List<String> tokenClaims = getTokenClaims();
        return getCurrentUsername(tokenClaims);
    }

    private Optional<String> getCurrentUsername(List<String> tokenClaims) {
        return tokenClaims.stream()
            .filter(Objects::nonNull)
            .filter(it -> it.startsWith(USERNAME_PREFIX))
            .findAny()
            .map(this::removePrefixFromUsernameClaim);
    }

    private String removePrefixFromUsernameClaim(String claim) {
        return claim.substring(USERNAME_PREFIX.length());
    }
}
