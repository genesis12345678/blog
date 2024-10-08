package com.spring.blog.service.oauth;

import com.spring.blog.common.converters.ProviderUserRequest;
import com.spring.blog.common.enums.SocialType;
import com.spring.blog.domain.User;
import com.spring.blog.model.ProviderUser;
import com.spring.blog.repository.UserRepository;
import com.spring.blog.service.UserService;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.function.Function;

@Service
@Getter
@RequiredArgsConstructor
public abstract class AbstractOAuth2UserService {

    private final UserService userService;
    private final UserRepository userRepository;
    private final Function<ProviderUserRequest, ProviderUser> providerUserConverter;

    protected ProviderUser getProviderUser(ProviderUserRequest providerUserRequest) {
        return providerUserConverter.apply(providerUserRequest);
    }

    protected void save(ProviderUser providerUser, OAuth2UserRequest oAuth2UserRequest) {

        Optional<User> user = userRepository.findByEmail(providerUser.getEmail());
        String registrationId = oAuth2UserRequest.getClientRegistration().getRegistrationId().toUpperCase();

        if (user.isEmpty()) {
            SocialType socialType = SocialType.valueOf(registrationId);
            userService.save(providerUser, socialType);
            return;
        }

        if (!user.get().getRegistrationId().getSocialName().toUpperCase().equals(registrationId)) {
            throw new OAuth2AuthenticationException("Duplicate User");
        }
    }
}