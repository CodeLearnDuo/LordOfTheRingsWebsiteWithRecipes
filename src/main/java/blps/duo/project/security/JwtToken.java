package blps.duo.project.security;

import blps.duo.project.model.Person;
import lombok.Getter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.Authentication;

import java.util.Objects;


@Getter
public class JwtToken extends AbstractAuthenticationToken {

    private final String token;
    private final Person principal;

    public JwtToken(String token, Person principal) {
        super(principal.getAuthorities());
        this.token = token;
        this.principal = principal;
    }

    Authentication withAuthenticated(boolean isAuthenticated) {
        JwtToken cloned = new JwtToken(token, principal);
        cloned.setAuthenticated(isAuthenticated);
        return cloned;
    }

    @Override
    public Object getCredentials() {
        return null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof JwtToken jwtToken)) return false;
        if (!super.equals(o)) return false;
        return Objects.equals(token, jwtToken.token) && Objects.equals(principal, jwtToken.principal);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), token, principal);
    }
}
