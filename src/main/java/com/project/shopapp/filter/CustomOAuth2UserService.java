package com.project.shopapp.filter;

import com.project.shopapp.models.CustomOAuth2User;
import com.project.shopapp.models.User;
import com.project.shopapp.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService  {

	private final UserRepository userRepository;
	@Override
	public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
		OAuth2User oAuth2User =  super.loadUser(userRequest);
		String email = oAuth2User.getAttribute("email");
		Optional<User> user = userRepository.findByEmail(email);

		// Tạo UsernamePasswordAuthenticationToken mà không cần mật khẩu
		UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
				user.get().getEmail(),
				null, // Bỏ qua mật khẩu
				user.get().getAuthorities()
		);

		// Đặt vào SecurityContext
		SecurityContextHolder.getContext().setAuthentication(authenticationToken);

		return oAuth2User; // Trả về OAuth2User

	}

}
