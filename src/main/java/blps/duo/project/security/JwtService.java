package blps.duo.project.security;

import blps.duo.project.exceptions.JwtAuthenticationException;
import blps.duo.project.model.Person;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

@Service
public class JwtService implements TokenProvider {

    @Value("${jwt.secret-key}")
    private String secretKey;
    @Value("${jwt.token-expiration-seconds}")
    private long tokenExpiration;

    String extractUsername(String jwt) {
        return extractClaim(jwt, Claims::getSubject);
    }

    String extractName(String jwt) {
        return extractClaim(jwt, claims -> (String) claims.get("name"));
    }

    List<String> extractRoles(String jwt) {
        return extractClaim(jwt, claims -> (List<String>) claims.get("roles"));
    }

    boolean extractALeader(String jwt) {
        return extractClaim(jwt, claims -> (Boolean) claims.get("aLeader"));
    }

    long extractPersonRaceId(String jwt) {
        return extractClaim(jwt, claims -> ((Integer) claims.get("personRaceId")).longValue());
    }

    @Override
    public String generateToken(Person person) {
        return generateToken(Map.of(), person);
    }

    boolean isTokenValid(String jwt) {
        return !isTokenExpired(jwt);
    }

    private boolean isTokenExpired(String jwt) {
        return extractClaim(jwt, Claims::getExpiration).before(new Date());
    }

    private <T> T extractClaim(String jwt, Function<Claims, T> claimResolver) {
        Claims claims = extractAllClaims(jwt);
        return claimResolver.apply(claims);
    }

    private Claims extractAllClaims(String jwt) {
        try {
            return Jwts.parser()
                    .verifyWith(getSecretKey())
                    .build()
                    .parseSignedClaims(jwt)
                    .getPayload();
        } catch (JwtException e) {
            throw new JwtAuthenticationException("Invalid JWT\n" + e.getMessage());
        }
    }

    private String generateToken(Map<String, Object> extraClaims, Person person) {
        var currentTime = System.currentTimeMillis();
        return Jwts.builder()
                .header().add("typ", "JWT")
                .and()
                .claims(extraClaims)
                .subject(person.getEmail())
                .claim("name", person.getName())
                .claim("personRaceId", person.getPersonRaceId())
                .claim("aLeader", person.isALeader())
                .claim("roles", person.getAuthorities().stream()
                        .map(GrantedAuthority::getAuthority)
                        .map(role -> role.substring("ROLE_".length()))
                        .toArray())
                .issuedAt(new Date(currentTime))
                .expiration(new Date(currentTime + tokenExpiration * 1000))
                .signWith(getSecretKey(), Jwts.SIG.HS256)
                .compact();
    }

    private SecretKey getSecretKey() {
        byte[] bytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(bytes);
    }
}
