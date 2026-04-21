package com.smartcampus.security;

import com.smartcampus.enums.UserRole;
import com.smartcampus.model.User;
import com.smartcampus.repository.UserRepository;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Handles generating the JWT and passing it back to the React UI when Google
 * authenticates a user. Auto-registers new users on first login.
 */
@Component
public class OAuth2AuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    private static final Logger logger = LoggerFactory.getLogger(OAuth2AuthenticationSuccessHandler.class);

    private final JwtTokenProvider tokenProvider;
    private final UserRepository userRepository;

    private final String frontendUrl = "http://localhost:5173/oauth2/callback";

    public OAuth2AuthenticationSuccessHandler(JwtTokenProvider tokenProvider, UserRepository userRepository) {
        this.tokenProvider = tokenProvider;
        this.userRepository = userRepository;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
            Authentication authentication) throws IOException, ServletException {
        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
        String oauthId = oAuth2User.getAttribute("sub");
        String email = oAuth2User.getAttribute("email");
        String name = oAuth2User.getAttribute("name");
        String picture = oAuth2User.getAttribute("picture");

        // Find existing user or auto-register on first login
        User user = userRepository.findByOauthId(oauthId)
                .orElseGet(() -> {
                    logger.info("New user login — auto-registering: {} ({})", name, email);
                    User newUser = User.builder()
                            .oauthId(oauthId)
                            .email(email)
                            .name(name)
                            .profileImageUrl(picture)
                            .role(UserRole.USER)
                            .isActive(true)
                            .build();
                    return userRepository.save(newUser);
                });

        // Update profile info on each login in case Google profile changed
        user.setName(name);
        user.setEmail(email);
        user.setProfileImageUrl(picture);
        userRepository.save(user);

        String token = tokenProvider.generateToken(user);
        logger.info("OAuth2 login successful for user: {} (role: {})", email, user.getRole());

        response.sendRedirect(frontendUrl + "?token=" + token);
    }
}
