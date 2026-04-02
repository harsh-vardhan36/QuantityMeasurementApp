package com.app.quantitymeasurement.security;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Handles the final step in the OAuth2 login flow for the REST API.
 *
 * <p>After Google authenticates the user and the {@link CustomOAuth2UserService}
 * saves their record, this handler creates a signed JWT and redirects the client
 * to the configured frontend URL with the token as a query parameter,
 * e.g. {@code http://localhost:3000/oauth2/redirect?token=<JWT>}.</p>
 */
@Component
public class OAuth2AuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private static final Logger logger = LoggerFactory.getLogger(OAuth2AuthenticationSuccessHandler.class);

    @Autowired
    private JwtTokenProvider tokenProvider;

    /** The frontend URL to redirect to with the generated token. */
    @Value("${app.oauth2.redirectUri:http://localhost:3000/oauth2/redirect}")
    private String redirectUri;

    /**
     * Called upon a successful OAuth2 authentication. Generates a JWT for the user
     * and redirects the client to the configurable frontend redirect URI.
     *
     * @param request        the incoming HTTP request
     * @param response       the HTTP response to write the redirect to
     * @param authentication the successful authentication object
     */
    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {
        String targetUrl = determineTargetUrl(authentication);

        if (response.isCommitted()) {
            logger.debug("Response has already been committed. Skipping redirect to {}", targetUrl);
            return;
        }

        clearAuthenticationAttributes(request);
        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }

    /**
     * Builds the redirect URL containing the JWT, appended as a query parameter.
     *
     * @param authentication the successful authentication
     * @return the full redirect URL including the token
     */
    protected String determineTargetUrl(Authentication authentication) {
        String token = tokenProvider.generateToken(authentication);

        return UriComponentsBuilder.fromUriString(redirectUri)
                .queryParam("token", token)
                .build().toUriString();
    }
}
