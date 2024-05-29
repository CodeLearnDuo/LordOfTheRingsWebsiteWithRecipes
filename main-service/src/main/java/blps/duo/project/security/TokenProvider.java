package blps.duo.project.security;

import blps.duo.project.model.Person;

public interface TokenProvider {

    String generateToken(Person person);
}
