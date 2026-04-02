package com.app.quantitymeasurement.security;

import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.app.quantitymeasurement.entity.User;
import com.app.quantitymeasurement.enums.AuthProvider;
import com.app.quantitymeasurement.repository.UserRepository;

/**
 * Custom OAuth2 user service that integrates with Google's OAuth2 provider.
 *
 * <p>After a user authenticates via Google, this service loads the user's
 * information, then creates or updates their record in the local database.
 * It returns a {@link UserPrincipal} which is stored in the security context.</p>
 */
@Service
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    @Autowired
    private UserRepository userRepository;

    /**
     * Loads the OAuth2 user details after a successful Google login, then
     * persists or updates the user in the database.
     *
     * @param oAuth2UserRequest the OAuth2 user request from the provider
     * @return a {@link UserPrincipal} representing the authenticated user
     * @throws OAuth2AuthenticationException if authentication fails
     */
    @Override
    public OAuth2User loadUser(OAuth2UserRequest oAuth2UserRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(oAuth2UserRequest);

        try {
            return processOAuth2User(oAuth2UserRequest, oAuth2User);
        } catch (Exception ex) {
            throw new InternalAuthenticationServiceException(ex.getMessage(), ex.getCause());
        }
    }

    /**
     * Processes the returned OAuth2 user attributes and syncs the user record
     * in the local database — creating a new one if they've never logged in before,
     * or updating their details if they have.
     */
    private OAuth2User processOAuth2User(OAuth2UserRequest oAuth2UserRequest, OAuth2User oAuth2User) {
        Map<String, Object> attributes = oAuth2User.getAttributes();

        String email = (String) attributes.get("email");
        if (!StringUtils.hasText(email)) {
            throw new IllegalArgumentException("Email not found from OAuth2 provider");
        }

        Optional<User> userOptional = userRepository.findByEmail(email);
        User user;

        if (userOptional.isPresent()) {
            user = userOptional.get();
            // Update existing user's profile details on each login
            user = updateExistingUser(user, attributes);
        } else {
            // Register the new user
            user = registerNewUser(oAuth2UserRequest, attributes, email);
        }

        return UserPrincipal.create(user, attributes);
    }

    /** Creates and saves a brand-new User from the Google OAuth2 attributes. */
    private User registerNewUser(OAuth2UserRequest request,
                                 Map<String, Object> attributes,
                                 String email) {
        String fullName = (String) attributes.get("name");
        String[] nameParts = fullName != null ? fullName.split(" ", 2) : new String[]{"User", ""};
        String firstName = nameParts[0];
        String lastName = nameParts.length > 1 ? nameParts[1] : "";

        User user = User.builder()
                .provider(AuthProvider.google)
                .providerId((String) attributes.get("sub"))
                .firstName(firstName)
                .lastName(lastName)
                .email(email)
                .imageUrl((String) attributes.get("picture"))
                .emailVerified(Boolean.TRUE.equals(attributes.get("email_verified")))
                .build();
        return userRepository.save(user);
    }

    /** Updates a returning user's profile (name, picture) from OAuth2 attributes. */
    private User updateExistingUser(User existingUser, Map<String, Object> attributes) {
        String fullName = (String) attributes.get("name");
        String[] nameParts = fullName != null ? fullName.split(" ", 2) : new String[]{"User", ""};
        existingUser.setFirstName(nameParts[0]);
        existingUser.setLastName(nameParts.length > 1 ? nameParts[1] : "");
        existingUser.setImageUrl((String) attributes.get("picture"));
        return userRepository.save(existingUser);
    }
}
