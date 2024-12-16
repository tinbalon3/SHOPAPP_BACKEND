package com.project.shopapp.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.project.shopapp.components.JwtTokenUtils;
import com.project.shopapp.dto.*;
import com.project.shopapp.exceptions.*;
import com.project.shopapp.mapper.UserMapper;
import com.project.shopapp.models.Provider;
import com.project.shopapp.models.Token;
import com.project.shopapp.models.User;
import com.project.shopapp.request.ForgotPasswordRequest;
import com.project.shopapp.request.UpdatePasswordRequest;
import com.project.shopapp.response.ResponseObject;
import com.project.shopapp.response.user.*;
import com.project.shopapp.service.ISendEmailService;
import com.project.shopapp.service.ITokenService;
import com.project.shopapp.service.IUserService;
import com.project.shopapp.components.LocalizationUtils;
import com.project.shopapp.service.IVerifyService;
import com.project.shopapp.untils.GooglePojo;
import com.project.shopapp.untils.GoogleUtils;
import com.project.shopapp.untils.MessageKeys;
import com.project.shopapp.untils.ValidationUtils;
import jakarta.mail.MessagingException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;

import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;


import java.io.UnsupportedEncodingException;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("${api.prefix}/users")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:4200")
public class UserController {

    private final IUserService userService;
    private final ISendEmailService emailService;
    private final IVerifyService verifyService;
    private final LocalizationUtils localizationUtils;
    private final ITokenService tokenService;
    private final ClientRegistrationRepository clientRegistrationRepository;
    private static final Logger logger = LoggerFactory.getLogger(User.class);
    private final GoogleUtils googleUtils;

    private final JwtTokenUtils jwtTokenUtils;
    @GetMapping("")
    public ResponseEntity<ResponseObject> getAllUser(
            @RequestParam(defaultValue = "",required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int limit){

        PageRequest pageRequest = PageRequest.of(page,limit, Sort.by("id").ascending());
        Page<User> userPage = userService.getAllUser(keyword,pageRequest);
        List<User> users = userPage.getContent();
        int totalPages = userPage.getTotalPages();
        List<UserResponse> userResponses = users.stream().map(user -> UserMapper.MAPPER.mapToUserResponse(user)).collect(Collectors.toList());
        UserListResponse userListResponse = UserListResponse.builder().userResponses(userResponses).totalPages(totalPages).build();
        return ResponseEntity.ok(ResponseObject.builder()
                            .data(userListResponse)
                            .message("Lấy danh sách user thành công")
                            .status(HttpStatus.OK.value())
                            .build());

    }
    @PutMapping("/logout")
    public ResponseEntity<ResponseObject> revokeToken(@RequestBody RefreshTokenDTO refreshTokenDTO) throws DataNotFoundException {
        tokenService.revokeToken(refreshTokenDTO.getRefreshToken());
        return ResponseEntity.ok(ResponseObject.builder()
                .message("Revoke token thành công")
                .status(HttpStatus.OK.value())
                .build());
    }
    @PostMapping("/register")
    public ResponseEntity<ResponseObject> register(@Valid @RequestBody UserDTO userDTO,
                                                   BindingResult result) throws DataNotFoundException, DataAlreadyExistsException,  JsonProcessingException, InvalidDataRegisterException {

        if(result.hasErrors()) {
            String errorMessage = result.getFieldErrors()
                    .stream()
                    .map(fieldError -> fieldError.getDefaultMessage())
                    .collect(Collectors.joining(", "));
           throw new InvalidDataRegisterException(errorMessage);
        }

        userService.createUser(userDTO);
        return ResponseEntity.ok(ResponseObject.builder()
                .status(HttpStatus.OK.value())
                .message("Tạm thời tạo User thành công.")
                .build());
    }

    @PutMapping("/email/verify")
    public ResponseEntity<ResponseObject> verifyCodeForgotPassword(@RequestBody VerifyCodeDTO verifyCodeDTO) throws  JsonProcessingException, OTPExpiredException {
        if (verifyService.verifyEmailCodeToDo(verifyCodeDTO.getCode(),verifyCodeDTO.getEmail())) {
            return ResponseEntity.ok(ResponseObject.builder()
                    .status(HttpStatus.OK.value())
                    .message("Xác thực mã OTP thành công.")
                    .build());
        }else {
            return ResponseEntity.ok(ResponseObject.builder()
                    .status(HttpStatus.BAD_REQUEST.value())
                    .message("Xác thực mã OTP không thành công.")
                    .build());
        }
    }
    @PutMapping("/register/verify")
    public ResponseEntity<ResponseObject> verifyRegisterUser(@RequestBody VerifyCodeDTO verifyCodeDTO) throws DataNotFoundException, JsonProcessingException {
        if (verifyService.verifyRegisterCode(verifyCodeDTO.getCode(),verifyCodeDTO.getEmail())) {
            return ResponseEntity.ok(ResponseObject.builder()
                    .status(HttpStatus.OK.value())
                    .message("Xác thực mã OTP thành công.")
                    .build());
        }else {
            return ResponseEntity.ok(ResponseObject.builder()
                    .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .message("Xác thực mã OTP không thành công.")
                    .build());
        }
    }



    private boolean isMobileDevice(String userAgent){
        //Kiểm tra User-Agent header để xác định thiết bị di động
        //Ví dụ đơn giản:
        return userAgent.toLowerCase().contains("mobile");
    }
    @GetMapping("/auth/googleLogin")
    public ResponseEntity<ResponseObject> loginGoogle()  {
        ClientRegistration registration = clientRegistrationRepository.findByRegistrationId("google");
        String authorizationUri = registration.getProviderDetails().getAuthorizationUri();
        String clientId = registration.getClientId();
        String redirectUri = registration.getRedirectUri();

        String oauth2Url = authorizationUri +
                "?response_type=code" +
                "&client_id=" + clientId +
                "&redirect_uri=" + redirectUri +
                "&scope=" + String.join(" ", registration.getScopes()) +
                "&state=custom_state"; // Thêm state tùy chỉnh nếu cần
        return ResponseEntity.ok(ResponseObject.builder()
                        .message("Ok")
                        .status(HttpStatus.OK.value())
                        .data(oauth2Url)
                .build());
    }

    @GetMapping("/auth/callback")
    public ResponseEntity<ResponseObject> callback(@RequestParam("code") String code,HttpServletRequest request) throws Exception {

            if (code == null || code.isEmpty()) {
                return ResponseEntity.badRequest().body(ResponseObject.builder()
                        .message("Xác thực Google thất bại. Vui lòng thử lại.")
                        .status(HttpStatus.BAD_REQUEST.value())
                        .build());
            }
            String accessToken = googleUtils.getToken(code);
            String userAgent = request.getHeader("User-Agent");
            GooglePojo googlePojo = googleUtils.getUserInfo(accessToken);
            Optional<User> user = null;
            user = userService.getUserByEmail(googlePojo.getEmail());
            if(user.isEmpty()) {
            user = Optional.ofNullable(googleUtils.buildUser(googlePojo));
            }
            if(user.get().getProvider() != Provider.GOOGLE) {
                throw new  DataAlreadyExistsException("Email đã đăng kí trong hệ thống, không thể đăng nhập với Google.");
            }

            if (!user.get().isActive()) {
                throw new DataNotFoundException(localizationUtils.getLocalizeMessage(MessageKeys.USER_IS_NOT_ACTIVE));
            }
            if (!user.get().isEnabled()) {
            throw new DataNotFoundException(localizationUtils.getLocalizeMessage(MessageKeys.USER_IS_LOCKED));
            }
            String token = jwtTokenUtils.generateToken(user.get());
            Token jwtToken = tokenService.addToken(user.get(), token, isMobileDevice(userAgent));

            LoginResponse loginResponse = LoginResponse.builder()
                    .message(localizationUtils.getLocalizeMessage(MessageKeys.LOGIN_SUCCESSFULLY))
                    .token(jwtToken.getToken())
                    .tokenType(jwtToken.getTokenType())
                    .refreshToken(jwtToken.getRefreshToken())
                    .refreshTokenExpired(jwtToken.getRefreshExpirationDate())
                    .userName(user.get().getFullName())
                    .roles(user.get().getAuthorities().stream().map(item -> item.getAuthority()).toList())
                    .id(user.get().getId())
                    .build();

            return ResponseEntity.ok(ResponseObject.builder()
                    .data(loginResponse)
                    .message("Đăng nhập bằng google thành công")
                    .status(HttpStatus.OK.value())
                    .build());
    }

    @PostMapping("/auth/login")
    public ResponseEntity<ResponseObject> login(@RequestBody UserLoginDTO userLoginDTO, HttpServletRequest request) throws EmailNotRegisterException, UserErrorException, DataNotFoundException, InvalidPasswordException {
        //kiem tra thong tin dang nhap va sinh token

            String token = userService.login(userLoginDTO);
            String userAgent = request.getHeader("User-Agent");
            User userDetail = userService.getUserDetails(token);
            Token jwtToken = tokenService.addToken(userDetail,token,isMobileDevice(userAgent));
            LoginResponse loginResponse = LoginResponse.builder()
                    .message(localizationUtils.getLocalizeMessage(MessageKeys.LOGIN_SUCCESSFULLY))
                    .token(jwtToken.getToken())
                    .tokenType(jwtToken.getTokenType())
                    .refreshToken(jwtToken.getRefreshToken())
                    .refreshTokenExpired(jwtToken.getRefreshExpirationDate())
                    .userName(userDetail.getUsername())
                    .roles(userDetail.getAuthorities().stream().map(item -> item.getAuthority()).toList())
                    .id(userDetail.getId())
                    .build();
            logger.info(""+jwtToken.getRefreshExpirationDate());
            return ResponseEntity.ok(ResponseObject.builder()
                            .data(loginResponse)
                            .status(HttpStatus.OK.value())
                            .message("Đăng nhập thành công")
                    .build());

    }
    @PostMapping("/reset-password/send-verification-code")
    public ResponseEntity<ResponseObject> sendVerificationCode(@RequestBody EmailDTO email) throws DataNotFoundException, MessagingException, UnsupportedEncodingException, JsonProcessingException {

        Optional<User> user = userService.getUserByEmail(email.getEmail());
        if(user.isEmpty()) {
            throw new DataNotFoundException("Không tìm thấy tài khoản người dùng");
        }
        emailService.sendPasswordResetEmailCode(user.get());
        return ResponseEntity.ok(ResponseObject.builder()
                .status(HttpStatus.OK.value())
                .message("Đã gửi mã OTP về email.")
                .build());


    }

    @GetMapping("/reset-password/check-email-exist")
    public ResponseEntity<ResponseObject> emailIsExists(@RequestParam("email") String email) throws DataNotFoundException, DataAlreadyExistsException {
        Optional<User> user = userService.getUserByEmail(email);
        if(user.isEmpty()) {
            throw new DataNotFoundException("Không tìm thấy tài khoản người dùng");
        }
        if(user.get().getProvider() == Provider.GOOGLE){
           throw new DataAlreadyExistsException("Tài khoản này đã đăng nhập qua Google. Không thể lấy lại mật khẩu qua hệ thống");
        }
        return ResponseEntity.ok(
                ResponseObject.builder()
                        .status(HttpStatus.OK.value())
                        .message("Tài khoản hợp lệ.")
                        .build()
        );
    }
    @PostMapping("/change-email/send-verification-email-code")
    public ResponseEntity<ResponseObject> sendChangeEmailCode(@RequestBody EmailDTO emailDTO) throws DataNotFoundException, MessagingException, UnsupportedEncodingException, JsonProcessingException {

            Optional<User> user = userService.getUserByEmail(emailDTO.getEmail());
            if(user.isEmpty()) {
            throw new DataNotFoundException("Không tìm thấy tài khoản người dùng");
            }
            emailService.sendChangeEmailCode(emailDTO,user.get());
            return ResponseEntity.ok(ResponseObject.builder()
                            .status(HttpStatus.OK.value())
                            .message("Gửi code thành công")
                    .build());


    }


    @GetMapping("/details")
    public ResponseEntity<ResponseObject> getUserDetails(@RequestHeader("Authorization") String authorizationHeader) throws DataNotFoundException {


            String extractToken = authorizationHeader.substring(7);
            User user = userService.getUserDetails(extractToken);
            UserDetailResponse userResponse = UserMapper.MAPPER.mapToUserDetailResponse(user);
            return ResponseEntity.ok(ResponseObject.builder()
                            .data(userResponse)
                            .message("Lấy thông tin user thành công")
                            .status(HttpStatus.OK.value())
                    .build());


    }

    @PutMapping("/details/{user_id}")
    public ResponseEntity<ResponseObject> updateUser(@PathVariable("user_id") Long id,
                                        @RequestBody UpdateUserDTO updateUserDTO,
                                        @RequestHeader("Authorization") String authorizationHeader) throws Exception {
            String extractToken = authorizationHeader.substring(7);
            User userDetails = userService.getUserDetails(extractToken);
            if(!userDetails.getId().equals(id)){
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
            User updateUser = userService.updateUser(id,updateUserDTO);
            UserResponse userResponse = UserMapper.MAPPER.mapToUserResponse(updateUser);
            return ResponseEntity.ok(ResponseObject.builder()
            .message("Update user thành công")
            .data(userResponse)
            .status(HttpStatus.OK.value())
            .build());
    }

    @PutMapping("/update-email")
    public ResponseEntity<ResponseObject> updateEmail(@RequestHeader("Authorization") String authorizationHeader,
                                         @RequestBody EmailDTO emailDTO) throws Exception {

            String extractToken = authorizationHeader.substring(7);
            User userDetails = userService.getUserDetails(extractToken);
            if(!userDetails.getEmail().equals(emailDTO.getEmail())){
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
            userService.updateEmail(emailDTO);
            return ResponseEntity.ok(ResponseObject.builder()
                            .status(HttpStatus.OK.value())
                            .message("Thay đổi email thành công.")
                    .build());

    }

    @PutMapping("/update-password")
    public ResponseEntity<?> updatePassword(@RequestBody UpdatePasswordRequest updatePasswordRequest) throws Exception {


            userService.updatePassword(updatePasswordRequest.getEmail(),updatePasswordRequest);
            return ResponseEntity.ok().build();

    }
    @PutMapping("/reset-password/change-pass")
    public ResponseEntity<ResponseObject> resetPassword(@RequestBody ForgotPasswordRequest forgotPasswordRequest) throws DataNotFoundException, MessagingException, UnsupportedEncodingException, EmailNotRegisterException {
        boolean allowed = userService.isAllowed(forgotPasswordRequest.getEmail());

        if(allowed) {
            emailService.resetPassword(forgotPasswordRequest.getEmail());
            return ResponseEntity.ok(ResponseObject.builder()
                    .message("Đã gửi mật khẩu mới về email của bạn.")
                    .status(HttpStatus.OK.value())
                    .build());
        }
        else {
            return ResponseEntity.ok(ResponseObject.builder()
                    .message("Request limit exceed for user: " + forgotPasswordRequest.getEmail())
                    .status(HttpStatus.TOO_MANY_REQUESTS.value())
                    .build());
        }

    }

    @PutMapping("/blockOrEnable/{userId}/{active}")
    public ResponseEntity<ResponseObject> blockOrEnable(@PathVariable Long userId,@PathVariable boolean active) throws DataNotFoundException {
            userService.blockOrEnable(userId,active);
            String message = active == true ? "Successfully enabled the user." : "Successfully blocked the user.";
            return ResponseEntity.ok().body(ResponseObject.builder()
                    .message(message)
                    .status(HttpStatus.OK.value())
                    .build());

    }

    @GetMapping("/forgot_password")
    public ResponseEntity<ResponseObject> forgotPassword(@RequestParam String username){
        boolean allowed = userService.isAllowed(username);
        if(allowed) {
            logger.info("Request allowed for user: {}",username);
            return ResponseEntity.ok(ResponseObject.builder()
                            .message("Request allowed for user: " + username)
                            .status(HttpStatus.OK.value())
                    .build());
        } else {
            logger.info("Request limit exceed for user: {}", username);
            return ResponseEntity.ok(ResponseObject.builder()
                            .message("Request limit exceed for user: " + username)
                            .status(HttpStatus.TOO_MANY_REQUESTS.value())
                    .build());
        }
    }

}
