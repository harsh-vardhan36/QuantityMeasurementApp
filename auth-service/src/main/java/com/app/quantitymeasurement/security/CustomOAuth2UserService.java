package com.app.quantitymeasurement.security;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.app.quantitymeasurement.entity.User;
import com.app.quantitymeasurement.enums.AuthProvider;
import com.app.quantitymeasurement.exception.OAuth2AuthenticationProcessingException;
import com.app.quantitymeasurement.repository.UserRepository;

/**
 * Service that processes an authenticated OAuth2 user.
 * It maps the OAuth2 attributes to our User entity and performs sync.
 */
@Service
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    @Autowired
    private UserRepository userRepository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest oAuth2UserRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(oAuth2UserRequest);

        try {
            return processOAuth2User(oAuth2UserRequest, oAuth2User);
        } catch (AuthenticationException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new InternalAuthenticationServiceException(ex.getMessage(), ex.getCause());
        }
    }

    private OAuth2User processOAuth2User(OAuth2UserRequest oAuth2UserRequest, OAuth2User oAuth2User) {
        String email = oAuth2User.getAttribute("email");
        if (!StringUtils.hasText(email)) {
            throw new OAuth2AuthenticationProcessingException("Email not found from OAuth2 provider");
        }

        Optional<User> userOptional = userRepository.findByEmail(email);
        User user;
        if (userOptional.isPresent()) {
            user = userOptional.get();
            if (!user.getProvider().equals(AuthProvider.valueOf(oAuth2UserRequest.getClientRegistration().getRegistrationId()))) {
                throw new OAuth2AuthenticationProcessingException("Looks like you're signed up with " +
                        user.getProvider() + " account. Please use your " + user.getProvider() +
                        " account to login.");
            }
            user = updateExistingUser(user, oAuth2User, oAuth2UserRequest);
        } else {
            user = registerNewUser(oAuth2UserRequest, oAuth2User);
        }

        return UserPrincipal.create(user, oAuth2User.getAttributes());
    }

    private User registerNewUser(OAuth2UserRequest oAuth2UserRequest, OAuth2User oAuth2User) {
        User user = new User();

        user.setProvider(AuthProvider.valueOf(oAuth2UserRequest.getClientRegistration().getRegistrationId()));
        user.setProviderId(oAuth2User.getAttribute("sub"));
        user.setFirstName(oAuth2User.getAttribute("given_name"));
        user.setLastName(oAuth2User.getAttribute("family_name"));
        user.setEmail(oAuth2User.getAttribute("email"));
        user.setImageUrl(oAuth2User.getAttribute("picture"));
        user.setEmailVerified(true);
        return userRepository.save(user);
    }

    private User updateExistingUser(User existingUser, OAuth2User oAuth2User, OAuth2UserRequest oAuth2UserRequest) {
        existingUser.setFirstName(oAuth2User.getAttribute("given_name"));
        existingUser.setLastName(oAuth2User.getAttribute("family_name"));
        existingUser.setImageUrl(oAuth2User.getAttribute("picture"));
        
        // Sync provider and providerId
        existingUser.setProvider(AuthProvider.valueOf(oAuth2UserRequest.getClientRegistration().getRegistrationId()));
        existingUser.setProviderId(oAuth2User.getAttribute("sub"));
        
        return userRepository.save(existingUser);
    }
}
