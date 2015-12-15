package server.security.jwt;

import server.security.UserAuthentication;
import java.io.IOException;
import java.util.HashMap;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

@Component
public class FacebookSuccessHandler implements AuthenticationSuccessHandler {

    @Autowired
    private JwtAuthenticationService tokenService;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        HashMap<String, String> userDetailsMap = (HashMap<String, String>) ((OAuth2Authentication) authentication).getUserAuthentication().getDetails();

        UserAuthentication user = getUserAuthentication(userDetailsMap);

        String jwtToken = tokenService.getAuthenticationToken(user);

        response.setHeader("Authorization", jwtToken);
        response.setStatus(HttpStatus.NO_CONTENT.value());
    }

    private UserAuthentication getUserAuthentication(HashMap<String, String> userDetailsMap) {
        String externalId = userDetailsMap.get("id");
        String email = userDetailsMap.get("email");
        String givenName = userDetailsMap.get("first_name");
        String surname = userDetailsMap.get("last_name");

        return new UserAuthentication("facebook", externalId, givenName, surname, email);
    }
}
