package com.app.quantitymeasurement.security;

import java.io.IOException;
import java.net.URI;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import com.app.quantitymeasurement.entity.User;
import com.app.quantitymeasurement.entity.RefreshToken;
import com.app.quantitymeasurement.exception.BadRequestException;
import com.app.quantitymeasurement.repository.UserRepository;
import com.app.quantitymeasurement.service.RefreshTokenService;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Handler for successful OAuth2 login.
 * Generates a JWT access token + refresh token and redirects back to the frontend.
 */
@Component
public class OAuth2AuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    @Autowired
    private JwtTokenProvider tokenProvider;

    @Autowired
    private RefreshTokenService refreshTokenService;

    @Autowired
    private UserRepository userRepository;

    @Value("${app.oauth2.redirectUri}")
    private String authorizedRedirectUri;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        String targetUrl = determineTargetUrl(request, response, authentication);

        if (response.isCommitted()) {
            logger.debug("Response has already been committed. Unable to redirect to " + targetUrl);
            return;
        }

        clearAuthenticationAttributes(request, response);
        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }

    protected String determineTargetUrl(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
        String targetUrl = authorizedRedirectUri;

        if (!isAuthorizedRedirectUri(targetUrl)) {
            throw new BadRequestException("Unauthorized Redirect URI");
        }

        String accessToken = tokenProvider.generateToken(authentication);

        // Generate a refresh token for the OAuth2 user
        String refreshTokenValue = null;
        try {
            UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
            RefreshToken refreshToken = refreshTokenService.createRefreshToken(userPrincipal.getId());
            refreshTokenValue = refreshToken.getToken();
        } catch (Exception e) {
            logger.warn("Could not create refresh token for OAuth2 user: " + e.getMessage());
        }

        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(targetUrl)
                .queryParam("token", accessToken);

        if (refreshTokenValue != null) {
            builder.queryParam("refreshToken", refreshTokenValue);
        }

        return builder.build().toUriString();
    }

    protected void clearAuthenticationAttributes(HttpServletRequest request, HttpServletResponse response) {
        super.clearAuthenticationAttributes(request);
    }

    private boolean isAuthorizedRedirectUri(String uri) {
        URI clientRedirectUri = URI.create(uri);
        URI authorizedUri = URI.create(authorizedRedirectUri);

        return authorizedUri.getHost().equalsIgnoreCase(clientRedirectUri.getHost())
                && authorizedUri.getPort() == clientRedirectUri.getPort();
    }
}
