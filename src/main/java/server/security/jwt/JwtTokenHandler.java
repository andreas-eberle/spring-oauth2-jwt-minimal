package server.security.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Header;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import java.util.Date;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import server.security.UserAuthentication;

@Component
public class JwtTokenHandler {

    private static final int DAY_DURATION = 1000 * 60 * 60;
    private static final String CLAIM_AUTH_PROVIDER = "provider";
    private static final String CLAIM_EXTERNAL_ID = "ext-id";
    private static final String CLAIM_GIVEN_NAME = "name";
    private static final String CLAIM_SURNAME = "surname";
    private static final String CLAIM_EMAIL = "email";

    @Value("${api.jwt.secretKey}")
    private String secretKey;

    public UserAuthentication parseUserFromToken(String token) {
        Claims claims = Jwts.parser()
                .setSigningKey(secretKey)
                .parseClaimsJws(token)
                .getBody();

        String authProvider = (String) claims.get(CLAIM_AUTH_PROVIDER);
        String externalId = (String) claims.get(CLAIM_EXTERNAL_ID);
        String givenName = (String) claims.get(CLAIM_GIVEN_NAME);
        String surname = (String) claims.get(CLAIM_SURNAME);
        String email = (String) claims.get(CLAIM_EMAIL);

        return new UserAuthentication(authProvider, externalId, givenName, surname, email);
    }

    public String createTokenForUser(UserAuthentication user) {
        return Jwts.builder()
                .setSubject(user.getName())
                .setHeaderParam(Header.TYPE, Header.JWT_TYPE)
                .claim(CLAIM_AUTH_PROVIDER, user.getAuthProvider())
                .claim(CLAIM_EXTERNAL_ID, user.getExternalId())
                .claim(CLAIM_GIVEN_NAME, user.getGivenName())
                .claim(CLAIM_SURNAME, user.getSurname())
                .claim(CLAIM_EMAIL, user.getEmail())
                .setIssuedAt(new Date())
                .setExpiration(new Date(new Date().getTime() + DAY_DURATION))
                .signWith(SignatureAlgorithm.HS256, secretKey)
                .compact();
    }
}
