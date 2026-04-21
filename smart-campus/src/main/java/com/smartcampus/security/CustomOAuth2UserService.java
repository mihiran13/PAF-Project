package com.smartcampus.security;

import com.smartcampus.enums.UserRole;
import com.smartcampus.model.User;
import com.smartcampus.repository.UserRepository;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * Service to handle the user info fetch post-Google OAuth success.
 * Syncs the Google Profile data into our local DB user table.
 */
@Service
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;

    public CustomOAuth2UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);

        String oauthId = oAuth2User.getAttribute("sub");
        String email = oAuth2User.getAttribute("email");
        String name = oAuth2User.getAttribute("name");
        String picture = oAuth2User.getAttribute("picture");

        Optional<User> userOptional = userRepository.findByOauthId(oauthId);
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            boolean updateNeeded = false;

            if (name != null && !name.equals(user.getName())) {
                user.setName(name);
                updateNeeded = true;
            }
            if (picture != null && !picture.equals(user.getProfileImageUrl())) {
                user.setProfileImageUrl(picture);
                updateNeeded = true;
            }
            if (updateNeeded) {
                userRepository.save(user);
            }
        } else {
            User newUser = User.builder()
                    .oauthId(oauthId)
                    .email(email)
                    .name(name)
                    .profileImageUrl(picture)
                    .role(UserRole.USER) // Enforced by business rules
                    .isActive(true)
                    .build();
            userRepository.save(newUser);
        }

        return oAuth2User;
    }
}
