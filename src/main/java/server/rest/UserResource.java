package server.rest;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import server.security.UserAuthentication;

@RestController
@RequestMapping("users")
public class UserResource {

    @RequestMapping(path = "/current", method = RequestMethod.GET)
    public UserAuthentication getCurrentUser() {
        return (UserAuthentication) SecurityContextHolder.getContext().getAuthentication();
    }
}
