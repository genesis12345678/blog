package com.spring.blog.service.unit;

import com.spring.blog.common.enums.SocialType;
import com.spring.blog.domain.User;
import com.spring.blog.mapper.UserMapper;
import com.spring.blog.repository.UserRepository;
import com.spring.blog.service.UserService;
import com.spring.blog.service.dto.request.FormAddUserServiceRequest;
import com.spring.blog.service.dto.request.OAuthAddUserServiceRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.when;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@DisplayName("UserService 단위 테스트")
@ExtendWith(MockitoExtension.class)
public class UserServiceUnitTest {

    @Mock private UserRepository userRepository;
    @Mock private UserMapper userMapper;

    @InjectMocks private UserService userService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(userService, "defaultImageUrl", "imageUrl");
    }

    @DisplayName("save() 테스트")
    @Test
    void save() {
        // given
        String email = "email@test.com";
        String nickname = "testNickname";
        String password = "password12@";
        String phoneNumber = "01011223344";
        String imageUrl = "imageUrl";

        User user = User.builder()
                .email(email)
                .nickname(nickname)
                .password(password)
                .phoneNumber(phoneNumber)
                .profileImageUrl(imageUrl)
                .registrationId(SocialType.NONE)
                .build();

        when(userMapper.toEntity(any(FormAddUserServiceRequest.class), any(SocialType.class), anyString()))
                .thenReturn(user);
        when(userRepository.save(any(User.class))).thenReturn(user);

        // when
        String result = userService.save(new FormAddUserServiceRequest(email, password, nickname, phoneNumber));

        // then
        assertThat(result).isEqualTo(nickname);
        verify(userMapper, times(1)).toEntity(any(FormAddUserServiceRequest.class), any(SocialType.class), anyString());
        verify(userRepository, times(1)).save(any(User.class));
    }

    @DisplayName("updateOAuthUser() 테스트")
    @Test
    void updateOAuthUser() {
        // given
        String email = "email@test.com";

        User user = mock(User.class);

        String updateNickname = "updateNickname";
        String updatePhoneNumber = "01013572468";

        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(user));
        doNothing().when(user).updateNickname(anyString());
        doNothing().when(user).updatePhoneNumber(anyString());

        when(user.getNickname()).thenReturn(updateNickname);

        // when
        String result = userService.updateOAuthUser(
                new OAuthAddUserServiceRequest(updatePhoneNumber, updateNickname), email);

        // then
        assertThat(result).isEqualTo(updateNickname);
        verify(userRepository, times(1)).findByEmail(anyString());
        verify(user, times(1)).updatePhoneNumber(anyString());
        verify(user, times(1)).updateNickname(anyString());
    }
}
