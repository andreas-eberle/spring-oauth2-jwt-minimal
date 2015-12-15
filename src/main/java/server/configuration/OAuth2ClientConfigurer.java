package server.configuration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.oauth2.resource.ResourceServerProperties;
import org.springframework.boot.autoconfigure.security.oauth2.resource.UserInfoTokenServices;
import org.springframework.boot.context.embedded.FilterRegistrationBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.client.OAuth2ClientContext;
import org.springframework.security.oauth2.client.OAuth2RestTemplate;
import org.springframework.security.oauth2.client.filter.OAuth2ClientAuthenticationProcessingFilter;
import org.springframework.security.oauth2.client.filter.OAuth2ClientContextFilter;
import org.springframework.security.oauth2.client.resource.OAuth2ProtectedResourceDetails;
import org.springframework.security.oauth2.client.token.grant.code.AuthorizationCodeResourceDetails;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableOAuth2Client;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.Http403ForbiddenEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import server.security.jwt.FacebookSuccessHandler;
import server.security.jwt.StatelessJwtAuthenticationFilter;

@EnableOAuth2Client
@Configuration
public class OAuth2ClientConfigurer extends WebSecurityConfigurerAdapter {

    @Autowired
    private OAuth2ClientContext oAuth2ClientContext;

    /**
     * Configure HttpSecurity. This includes:<br>
     * - resources requiring authorized <br>
     * - resources that are free to access <br>
     * - csrf token mapping <br>
     * - construction of the security filter chain
     *
     * @param httpSecurity
     * @throws Exception
     */
    @Override
    protected void configure(HttpSecurity httpSecurity) throws Exception {
        httpSecurity
                .csrf().disable()
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS).enableSessionUrlRewriting(false).and()
                .antMatcher("/**").authorizeRequests()
                .antMatchers("/login/**").permitAll()
                .anyRequest().authenticated().and()
                .exceptionHandling().authenticationEntryPoint(new Http403ForbiddenEntryPoint()).and()
                .addFilterBefore(statelessJwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(createSsoFilter(facebook(), facebookSuccessHandler(), "/login/facebook"), BasicAuthenticationFilter.class);
    }

    private OAuth2ClientAuthenticationProcessingFilter createSsoFilter(ClientResourceDetails clientDetails, AuthenticationSuccessHandler successHandler, String path) {
        OAuth2ClientAuthenticationProcessingFilter ssoFilter = new OAuth2ClientAuthenticationProcessingFilter(path);
        ssoFilter.setAllowSessionCreation(false);
        OAuth2RestTemplate restTemplate = new OAuth2RestTemplate(clientDetails.getClient(), oAuth2ClientContext);
        ssoFilter.setRestTemplate(restTemplate);
        ssoFilter.setTokenServices(new UserInfoTokenServices(clientDetails.getResource().getUserInfoUri(), clientDetails.getClient().getClientId()));
        ssoFilter.setAuthenticationSuccessHandler(successHandler);
        return ssoFilter;
    }

    @Bean
    protected StatelessJwtAuthenticationFilter statelessJwtAuthenticationFilter() {
        return new StatelessJwtAuthenticationFilter();
    }

    @Bean // handles the redirect to facebook
    public FilterRegistrationBean oAuth2ClientFilterRegistration(OAuth2ClientContextFilter filter) {
        FilterRegistrationBean registration = new FilterRegistrationBean();
        registration.setFilter(filter);
        registration.setOrder(-100);
        return registration;
    }

    @Bean
    protected AuthenticationSuccessHandler facebookSuccessHandler() {
        return new FacebookSuccessHandler();
    }

    @Bean
    @ConfigurationProperties("facebook")
    protected ClientResourceDetails facebook() {
        return new ClientResourceDetails();
    }

    public class ClientResourceDetails {

        private final OAuth2ProtectedResourceDetails client = new AuthorizationCodeResourceDetails();
        private final ResourceServerProperties resource = new ResourceServerProperties();

        public OAuth2ProtectedResourceDetails getClient() {
            return client;
        }

        public ResourceServerProperties getResource() {
            return resource;
        }
    }
}
