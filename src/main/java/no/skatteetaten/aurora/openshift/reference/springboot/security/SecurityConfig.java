package no.skatteetaten.aurora.openshift.reference.springboot.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;

import no.skatteetaten.aurora.openshift.reference.springboot.psa.AuroraToken;
import no.skatteetaten.aurora.openshift.reference.springboot.psa.AuroraTokenAuthorizationFilter;
import no.skatteetaten.aurora.openshift.reference.springboot.psa.OpenShiftTokenReviewService;
import no.skatteetaten.aurora.openshift.reference.springboot.psa.PsatAuthorizationFilter;

@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final OpenShiftTokenReviewService tokenReviewService;

    private String auroraPath;

    public SecurityConfig(
        OpenShiftTokenReviewService tokenReviewService) {
        this.tokenReviewService = tokenReviewService;
    }

    // TODO Change or remove default location, more correct default: /u01/secrets/app/aurora-token
    @Value("${aurora.token.location:/tmp/aurora.txt}")
    public void setAuroraPath(String auroraPath) {
        this.auroraPath = auroraPath;
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.cors().and().csrf().disable()
            // this disables session creation on Spring Security
            .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            .and()
            .authorizeRequests()
            .antMatchers(HttpMethod.POST, "/**").permitAll()
            // Most specific rules should go first
            .antMatchers("/keepalive/server").authenticated()
            // Normally you probably want denyAll as default
            .anyRequest().permitAll()
            .and()
            .addFilter(new PsatAuthorizationFilter(authenticationManager(), tokenReviewService))
            .addFilter(new AuroraTokenAuthorizationFilter(authenticationManager(), new AuroraToken(auroraPath)))
            ;
   }
}