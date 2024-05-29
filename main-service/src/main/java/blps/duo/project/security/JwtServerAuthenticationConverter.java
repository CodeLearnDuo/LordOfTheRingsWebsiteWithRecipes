package blps.duo.project.security;

import blps.duo.project.model.Person;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.server.authentication.ServerAuthenticationConverter;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class JwtServerAuthenticationConverter implements ServerAuthenticationConverter {

    private static final String BEARER = "Bearer ";
    private final JwtService jwtService;

    @Override
    public Mono<Authentication> convert(ServerWebExchange exchange) {
        return Mono.justOrEmpty(exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION))
                .filter(header -> header.startsWith(BEARER))
                .map(header -> header.substring(BEARER.length()))
                .map(jwt -> new JwtToken(jwt, createPerson(jwt)));
    }

    private Person createPerson(String jwt) {
        String email = jwtService.extractUsername(jwt);
        String name = jwtService.extractName(jwt);
        boolean aLeader = jwtService.extractALeader(jwt);
        long personRaceId = jwtService.extractPersonRaceId(jwt);
        return new Person(email, name, personRaceId, aLeader);
    }
}
