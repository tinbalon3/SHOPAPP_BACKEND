package com.project.shopapp.untils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.shopapp.dto.UserDTO;
import com.project.shopapp.models.Provider;
import com.project.shopapp.models.User;
import com.project.shopapp.repositories.UserRepository;
import com.project.shopapp.service.IUserService;
import org.apache.http.client.ClientProtocolException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.Optional;

import org.apache.http.client.fluent.Request;
import org.apache.http.client.fluent.Form;



@Component
public class GoogleUtils {
    @Value("${google.app.id}")
    private String clientId;

    @Value("${google.app.secret}")
    private String clientSecret;

    @Value("${google.redirect.uri}")
    private String redirectUri;

    @Value("${google.link.get.token}")
    private String tokenUrl;

    @Value("${google.link.get.user_info}")
    private String userInfoUrl;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private IUserService userService;
    public String getToken(final String code) throws IOException {
        String response = Request.Post(tokenUrl)
                .bodyForm(Form.form()
                        .add("client_id", clientId)
                        .add("client_secret", clientSecret)
                        .add("redirect_uri", redirectUri)
                        .add("code", code)
                        .add("grant_type", "authorization_code").build())
                .execute()
                .returnContent()
                .asString();

        ObjectMapper mapper = new ObjectMapper();
        JsonNode node = mapper.readTree(response).get("access_token");
        return node.textValue();
    }

    public GooglePojo getUserInfo(final String accessToken) throws ClientProtocolException, IOException {
        String link = userInfoUrl + accessToken;
        String response = Request.Get(link).execute().returnContent().asString();
        ObjectMapper mapper = new ObjectMapper();
        GooglePojo googlePojo = mapper.readValue(response, GooglePojo.class);
        System.out.println(googlePojo);
        return googlePojo;
    }

    public User buildUser(GooglePojo googlePojo) throws Exception {
        Optional<User> userDetail = Optional.empty();
        userDetail = userRepository.findByEmail(googlePojo.getEmail());
        if(!userDetail.isPresent()) {
            UserDTO userDTO = UserDTO.builder()
                    .fullName(googlePojo.getName())
                    .googleAccountId(0)
                    .authProvider(String.valueOf(Provider.GOOGLE))
                    .address("")
                    .email(googlePojo.getEmail())
                    .role(Long.parseLong("1"))
                    .facebookAccountId(0)
                    .dateOfBirth(calculateDefaultDateOfBirth())
                    .phoneNumber("")
                    .retypePassword("")
                    .password("")
                    .build();
            userDetail = Optional.ofNullable(userService.createUser(userDTO));
        }

        if(userDetail.isPresent())
            return this.userRepository.save(userDetail.get());
        return null;
    }

    private Date calculateDefaultDateOfBirth() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, calendar.get(Calendar.YEAR) - 18); // Trừ 18 năm
        calendar.set(Calendar.MONTH, Calendar.JANUARY); // Thay đổi tháng nếu cần (có thể đặt tháng cụ thể)
        calendar.set(Calendar.DAY_OF_MONTH, 1); // Thay đổi ngày nếu cần (có thể đặt ngày cụ thể)
        return calendar.getTime();
    }
}
