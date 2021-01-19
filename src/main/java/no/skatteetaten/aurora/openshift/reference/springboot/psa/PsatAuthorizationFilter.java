package no.skatteetaten.aurora.openshift.reference.springboot.psa;

import java.io.IOException;
import java.util.List;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

public class PsatAuthorizationFilter extends BasicAuthenticationFilter {
    private static final Logger logger = LoggerFactory.getLogger(PsatAuthorizationFilter.class);

    private final OpenShiftTokenReviewService tokenReviewService;

    public PsatAuthorizationFilter(
        AuthenticationManager authenticationManager,
        OpenShiftTokenReviewService tokenReviewService) {
        super(authenticationManager);
        this.tokenReviewService = tokenReviewService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
        throws IOException, ServletException {
        String header = request.getHeader("Authorization");
        boolean validHeader = tokenReviewService.matchesScheme(header) && tokenReviewService.isValid(header);
        if ( !validHeader) {
            logger.debug("Rejecting authorization header: "+(header==null?"null":header.substring(0, Math.min(header.length(), 16)))+"....");
            chain.doFilter(request, response);
            return;
        }
        // This is where the token gets evaluated
        GrantedAuthority authority = () -> "psat-token";
        SecurityContextHolder
            .getContext()
            .setAuthentication(new PreAuthenticatedAuthenticationToken("pre-auth-token", null, List.of(authority)));
        chain.doFilter(request, response);
    }
}
