package com.project.shopapp.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.project.shopapp.components.JwtTokenUtils;
import com.project.shopapp.dto.*;
import com.project.shopapp.exceptions.DataNotFoundException;
import com.project.shopapp.mapper.UserMapper;
import com.project.shopapp.models.Token;
import com.project.shopapp.models.User;
import com.project.shopapp.repositories.RoleRepository;
import com.project.shopapp.response.ResponseObject;
import com.project.shopapp.response.user.*;
import com.project.shopapp.service.ITokenService;
import com.project.shopapp.service.IUserService;
import com.project.shopapp.components.LocalizationUtils;
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
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;

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
    private final LocalizationUtils localizationUtils;
    private final ITokenService tokenService;

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
                            .status(HttpStatus.OK)
                            .build());

    }

    @PostMapping("/register")
    public ResponseEntity<ResponseObject> createUser(@Valid @RequestBody UserDTO userDTO,
                                                       BindingResult result) throws Exception {

            if(result.hasErrors()) {
                List<String> errorMessage = result.getFieldErrors()
                        .stream()
                        .map(fieldError -> fieldError.getDefaultMessage()).toList();
                logger.info("Các lỗi khi đăng kí: " + errorMessage.toString());
                return ResponseEntity.badRequest().body(ResponseObject.builder()
                                .message(localizationUtils.getLocalizeMessage(MessageKeys.WRONG_DATA_REGISTER))
                                .status(HttpStatus.BAD_REQUEST)
                        .build());
            }
            if(!userDTO.getPassword().equals(userDTO.getRetypePassword())){
                return ResponseEntity.badRequest().body( ResponseObject.builder()
                                .message(localizationUtils.getLocalizeMessage(MessageKeys.PASSWORD_NOT_MATCH))
                                .status(HttpStatus.BAD_REQUEST)
                        .build());
            }
            if(userDTO.getEmail() == null || userDTO.getEmail().trim().isBlank()){
                if(userDTO.getPhoneNumber() == null || userDTO.getPhoneNumber().isBlank()){
                    return ResponseEntity.badRequest().body(ResponseObject.builder()

                                    .message("At least email or phone number is required")
                                    .status(HttpStatus.BAD_REQUEST)
                            .build());
                }
                else{
                    if(!ValidationUtils.validatePhoneNumber(userDTO.getPhoneNumber())) {
                        throw new Exception("Invalid phone number");
                    }
                }
            } else {
                if(!ValidationUtils.validateEmail(userDTO.getEmail())){
                    throw new Exception("Invalid email format");
                }
            }
            User user = userService.createUser(userDTO);

            return ResponseEntity.ok(ResponseObject.builder()
                            .status(HttpStatus.OK)
                            .message("Tạo user thành công")
                            .data(user)
                            .build());

    }

    @GetMapping("/verify")
    public ResponseEntity<ResponseObject> verifyUser(@RequestParam("code") String code,@RequestParam("email") String email) throws DataNotFoundException, JsonProcessingException {
        if (userService.verify(code,email)) {
            return ResponseEntity.ok(ResponseObject.builder()
                    .status(HttpStatus.OK)
                    .message("Xác thực mã OTP thành công.")
                    .build());
        }else {
            return ResponseEntity.ok(ResponseObject.builder()
                    .status(HttpStatus.BAD_REQUEST)
                    .message("Xác thực mã OTP không thành công.")
                    .build());
        }
    }



    private boolean isMobileDevice(String userAgent){
        //Kiểm tra User-Agent header để xác định thiết bị di động
        //Ví dụ đơn giản:
        return userAgent.toLowerCase().contains("mobile");
    }
    @PostMapping("/revoke-token")
    public ResponseEntity<ResponseObject> revokeToken(@RequestBody RefreshTokenDTO refreshTokenDTO) throws DataNotFoundException {
            tokenService.revokeToken(refreshTokenDTO.getRefreshToken());
            return ResponseEntity.ok(ResponseObject.builder()
                            .message("Revoke token thành công")
                            .status(HttpStatus.OK)
                    .build());



    }
    @GetMapping("/auth/callback")
    public ResponseEntity<ResponseObject> callback(@RequestParam("code") String code,HttpServletRequest request) throws Exception {

            if (code == null || code.isEmpty()) {
                return ResponseEntity.badRequest().body(ResponseObject.builder()
                        .message("Lỗi lấy code từ request.")
                        .status(HttpStatus.BAD_REQUEST)
                        .build());
            }
            String accessToken = googleUtils.getToken(code);
            String userAgent = request.getHeader("User-Agent");
            GooglePojo googlePojo = googleUtils.getUserInfo(accessToken);
            User user = googleUtils.buildUser(googlePojo);

            if (!user.isActive()) {
                throw new DataNotFoundException(localizationUtils.getLocalizeMessage(MessageKeys.USER_IS_LOCKED));
            }

            String token = jwtTokenUtils.generateToken(user);
            Token jwtToken = tokenService.addToken(user, token, isMobileDevice(userAgent));
            LoginResponse loginResponse = LoginResponse.builder()
                    .message(localizationUtils.getLocalizeMessage(MessageKeys.LOGIN_SUCCESSFULLY))
                    .token(jwtToken.getToken())
                    .tokenType(jwtToken.getTokenType())
                    .refreshToken(jwtToken.getRefreshToken())
                    .userName(user.getFullName())
                    .roles(user.getAuthorities().stream().map(item -> item.getAuthority()).toList())
                    .id(user.getId())
                    .build();

            return ResponseEntity.ok(ResponseObject.builder()
                    .data(loginResponse)
                    .message("Đăng nhập bằng google thành công")
                    .status(HttpStatus.OK)
                    .build());
    }


    @PostMapping("/login")
    public ResponseEntity<ResponseObject> login(@Valid @RequestBody UserLoginDTO userLoginDTO, HttpServletRequest request) throws Exception {
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
                    .userName(userDetail.getUsername())
                    .roles(userDetail.getAuthorities().stream().map(item -> item.getAuthority()).toList())
                    .id(userDetail.getId())
                    .build();
            return ResponseEntity.ok(ResponseObject.builder()
                            .data(loginResponse)
                            .status(HttpStatus.OK)
                            .message("Đăng nhập thành công")
                    .build());

    }
    @GetMapping("/send-verification-code/{id}")
    public ResponseEntity<?> sendVerificationCode(@PathVariable Long id) throws DataNotFoundException, MessagingException, UnsupportedEncodingException {

        Optional<User> user = userService.getUserById(id);
        userService.sendPasswordResetCodeEmail(user.get());
        return ResponseEntity.ok(ResponseObject.builder()
                .status(HttpStatus.OK)
                .message("Xác minh mã OTP thành công.")
                .build());


    }
    @PreAuthorize("hasRole('ROLE_USER')")
    @PostMapping("/send-verification-email-code/{id}")
    public ResponseEntity<?> sendChangeEmailCode(@PathVariable Long id,@RequestBody EmailDTO emailDTO) throws DataNotFoundException, MessagingException, UnsupportedEncodingException, JsonProcessingException {

            User user = userService.getUserById(id).orElseThrow(
                    () ->  new DataNotFoundException(localizationUtils.getLocalizeMessage(MessageKeys.NOT_FOUND_USER)));
            userService.sendChangeEmailCode(emailDTO,user);
            return ResponseEntity.ok().build();


    }
    @PostMapping("/refreshToken")
    public ResponseEntity<ResponseObject> refreshToken(@RequestBody RefreshTokenDTO refreshTokenDTO) throws Exception {

            User userDetail = userService.getUserDetailsFromRefreshToken(refreshTokenDTO.getRefreshToken());
            Token jwtToken = tokenService.refreshToken(refreshTokenDTO.getRefreshToken(),userDetail);
            LoginResponse loginResponse = LoginResponse.builder()
                    .message("Refresh token successfully")
                    .token(jwtToken.getToken())
                    .tokenType(jwtToken.getTokenType())
                    .refreshToken(jwtToken.getRefreshToken())
                    .userName(userDetail.getUsername())
                    .roles(userDetail.getAuthorities().stream().map(item -> item.getAuthority()).toList())
                    .id(userDetail.getId())
                    .build();
            return ResponseEntity.ok(ResponseObject.builder()
                    .data(loginResponse)
                    .message("Refresh token thành công")
                    .status(HttpStatus.OK)
                    .build());


    }
    @PreAuthorize("hasRole('ROLE_USER') or hasRole('ROLE_ADMIN')")
    @GetMapping("/details")
    public ResponseEntity<ResponseObject> getUserDetails(@RequestHeader("Authorization") String authorizationHeader) throws DataNotFoundException {


            String extractToken = authorizationHeader.substring(7);
            User user = userService.getUserDetails(extractToken);
            UserDetailResponse userResponse = UserMapper.MAPPER.mapToUserDetailResponse(user);
            return ResponseEntity.ok(ResponseObject.builder()
                            .data(userResponse)
                            .message("Lấy thông tin user thành công")
                            .status(HttpStatus.OK)
                    .build());


    }
    @PreAuthorize("hasRole('ROLE_USER')")
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
            .status(HttpStatus.OK)
            .build());
    }
    @PreAuthorize("hasRole('ROLE_USER')")
    @PutMapping("/update_email/{user_id}")
    public ResponseEntity<?> updateEmail(@PathVariable("user_id") Long id,
                                            @RequestHeader("Authorization") String authorizationHeader,
                                            @RequestBody EmailDTO emailDTO) throws Exception {

            String extractToken = authorizationHeader.substring(7);
            User userDetails = userService.getUserDetails(extractToken);
            if(!userDetails.getId().equals(id)){
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
            userService.updateEmail(id,emailDTO);
            return ResponseEntity.ok().build();

    }
    @PreAuthorize("hasRole('ROLE_USER')")
    @PutMapping("/update_password/{user_id}")
    public ResponseEntity<?> updatePassword(@PathVariable("user_id") Long id,
                                            @RequestHeader("Authorization") String authorizationHeader,
                                            @RequestBody PasswordDTO passwordDTO) throws Exception {

            String extractToken = authorizationHeader.substring(7);
            User userDetails = userService.getUserDetails(extractToken);
            if(!userDetails.getId().equals(id)){
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
             userService.updatePassword(id,passwordDTO);
            return ResponseEntity.ok().build();

    }
    @PutMapping("/reset-password/{userId}")
    public ResponseEntity<ResponseObject> resetPassword(@PathVariable Long userId) throws DataNotFoundException, MessagingException, UnsupportedEncodingException {

            userService.resetPassword(userId);
            return ResponseEntity.ok().body(ResponseObject.builder()
                    .message("Làm mới mật khẩu thành công")
                    .status(HttpStatus.OK)
                    .build());

    }

    @PutMapping("/blockOrEnable/{userId}/{active}")
    public ResponseEntity<ResponseObject> blockOrEnable(@PathVariable Long userId,@PathVariable boolean active) throws DataNotFoundException {
            userService.blockOrEnable(userId,active);
            String message = active == true ? "Successfully enabled the user." : "Successfully blocked the user.";
            return ResponseEntity.ok().body(ResponseObject.builder()
                    .message(message)
                    .status(HttpStatus.OK)
                    .build());

    }

    @GetMapping("/forgot_password")
    public ResponseEntity<ResponseObject> forgotPassword(@RequestParam String username){
        boolean allowed = userService.isAllowed(username);
        if(allowed) {
            logger.info("Request allowed for user: {}",username);
            return ResponseEntity.ok(ResponseObject.builder()
                            .message("Request allowed for user: " + username)
                            .status(HttpStatus.OK)
                    .build());
        } else {
            logger.info("Request limit exceed for user: {}", username);
            return ResponseEntity.ok(ResponseObject.builder()
                            .message("Request limit exceed for user: " + username)
                            .status(HttpStatus.TOO_MANY_REQUESTS)
                    .build());
        }
    }

}
