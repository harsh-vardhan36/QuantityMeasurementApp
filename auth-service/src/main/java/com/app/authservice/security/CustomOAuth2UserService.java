package com.app.authservice.security;

import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.oauth2.client.userinfo.*;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.app.authservice.entity.User;
import com.app.authservice.enums.AuthProvider;
import com.app.authservice.repository.UserRepository;

@Service
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    @Autowired
    private UserRepository userRepository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest oAuth2UserRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(oAuth2UserRequest);
        try {
            return processOAuth2User(oAuth2UserRequest, oAuth2User);
        } catch (Exception ex) {
            throw new InternalAuthenticationServiceException(ex.getMessage(), ex.getCause());
        }
    }

    private OAuth2User processOAuth2User(OAuth2UserRequest request, OAuth2User oAuth2User) {
        Map<String, Object> attributes = oAuth2User.getAttributes();

        String email = (String) attributes.get("email");
        if (!StringUtils.hasText(email)) {
            throw new IllegalArgumentException("Email not found from OAuth2 provider");
        }

        Optional<User> userOptional = userRepository.findByEmail(email);
        User user = userOptional.isPresent()
                ? updateExistingUser(userOptional.get(), attributes)
                : registerNewUser(request, attributes, email);

        return UserPrincipal.create(user, attributes);
    }

    private User registerNewUser(OAuth2UserRequest request, Map<String, Object> attributes, String email) {
        String fullName  = (String) attributes.get("name");
        String[] parts   = fullName != null ? fullName.split(" ", 2) : new String[]{"User", ""};

        User user = User.builder()
                .provider(AuthProvider.google)
                .providerId((String) attributes.get("sub"))
                .firstName(parts[0])
                .lastName(parts.length > 1 ? parts[1] : "")
                .email(email)
                .imageUrl((String) attributes.get("picture"))
                .emailVerified(Boolean.TRUE.equals(attributes.get("email_verified")))
                .build();
        return userRepository.save(user);
    }

    private User updateExistingUser(User existingUser, Map<String, Object> attributes) {
        String fullName = (String) attributes.get("name");
        String[] parts  = fullName != null ? fullName.split(" ", 2) : new String[]{"User", ""};
        existingUser.setFirstName(parts[0]);
        existingUser.setLastName(parts.length > 1 ? parts[1] : "");
        existingUser.setImageUrl((String) attributes.get("picture"));
        return userRepository.save(existingUser);
    }
}
