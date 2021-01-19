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

public class AuroraTokenAuthorizationFilter extends BasicAuthenticationFilter {
    private final AuroraToken auroraToken;

    private static final Logger logger = LoggerFactory.getLogger(AuroraTokenAuthorizationFilter.class);

    public AuroraTokenAuthorizationFilter(
        AuthenticationManager authenticationManager,
        AuroraToken auroraToken) {
        super(authenticationManager);
        this.auroraToken = auroraToken;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
        throws IOException, ServletException {
        String header = request.getHeader("Authorization");
        boolean validToken = header != null && auroraToken.matchesScheme(header) && auroraToken.tokenMatches(header);
        if (validToken) {
            GrantedAuthority authority = () -> "aurora-token";
            SecurityContextHolder
                .getContext()
                .setAuthentication(new PreAuthenticatedAuthenticationToken("pre-auth-token", null, List.of(authority)));
        } else {
            logger.debug("Rejecting aurora authorization header: "+(header==null?"null":header.substring(0, Math.min(header.length(), 16)))+"....");
        }
        chain.doFilter(request, response);
    }
}
