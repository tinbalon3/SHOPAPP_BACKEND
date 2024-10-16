package com.project.shopapp.controller;

import com.project.shopapp.dto.*;
import com.project.shopapp.exception.DataNotFoundException;
import com.project.shopapp.mapper.UserMapper;
import com.project.shopapp.models.Token;
import com.project.shopapp.models.User;
import com.project.shopapp.response.user.*;
import com.project.shopapp.service.IEmailService;
import com.project.shopapp.service.ITokenService;
import com.project.shopapp.service.IUserService;
import com.project.shopapp.components.LocalizationUtils;
import com.project.shopapp.untils.MessageKeys;
import com.project.shopapp.untils.ValidationUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("${api.prefix}/users")
@RequiredArgsConstructor
public class UserController {

    private final IUserService userService;
    private final LocalizationUtils localizationUtils;
    private final ITokenService tokenService;
    private final IEmailService emailService;
    private static final Logger logger = LoggerFactory.getLogger(User.class);

    @GetMapping("")
    public ResponseEntity<?> getAllUser(
            @RequestParam(defaultValue = "",required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int limit
    ){
        try {
            PageRequest pageRequest = PageRequest.of(
                    page,limit, Sort.by("id").ascending()
            );
            Page<User> userPage = userService.getAllUser(keyword,pageRequest);
            List<User> users = userPage.getContent();
            int totalPages = userPage.getTotalPages();
            List<UserResponse> userResponses = users.stream().map(user ->
              UserMapper.MAPPER.mapToUserResponse(user)).collect(Collectors.toList());
            return ResponseEntity.ok(UserListResponse
                    .builder()
                            .userResponses(userResponses)
                            .totalPages(totalPages)
                            .build()
                    );
        }
        catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/register")
    public ResponseEntity<RegisterResponse> createUser(@Valid @RequestBody UserDTO userDTO,
                                                       HttpServletRequest request,
                                                       BindingResult result){
        try {
            if(result.hasErrors()) {
                List<String> errorMessage = result.getFieldErrors()
                        .stream()
                        .map(fieldError -> fieldError.getDefaultMessage()).toList();
                logger.info("Các lỗi khi đăng kí: " + errorMessage.toString());
                return ResponseEntity.badRequest().body(RegisterResponse.builder()
                                .message(localizationUtils.getLocalizeMessage(MessageKeys.WRONG_DATA_REGISTER))
                                .user(null)
                        .build());
            }
            if(!userDTO.getPassword().equals(userDTO.getRetypePassword())){
                return ResponseEntity.badRequest().body( RegisterResponse.builder()
                                .message(localizationUtils.getLocalizeMessage(MessageKeys.PASSWORD_NOT_MATCH))
                                .user(null)
                        .build());
            }
            if(userDTO.getEmail() == null || userDTO.getEmail().trim().isBlank()){
                if(userDTO.getPhoneNumber() == null || userDTO.getPhoneNumber().isBlank()){
                    return ResponseEntity.badRequest().body(RegisterResponse.builder()
                                    .user(null)
                                    .message("At least email or phone number is required")
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
            return ResponseEntity.ok(
                    RegisterResponse.builder()
                            .message(localizationUtils.getLocalizeMessage(MessageKeys.REGISTER_SUCCESSFULLY))
                            .user(user)
                            .build());
        }catch(Exception e) {
            return ResponseEntity.badRequest().body(
                    RegisterResponse.builder()
                            .message(localizationUtils.getLocalizeMessage(MessageKeys.REGISTER_FAILED, e.getMessage()))
                            .user(null)
                            .build());
        }
    }

    @GetMapping("/verify")
    public ResponseEntity<?> verifyUser(@RequestParam("code") String code) {
        if (userService.verify(code)) {
            return ResponseEntity.ok().build();
        } else {
            return ResponseEntity.badRequest().build();
        }
    }

    private String getSiteURL(HttpServletRequest request) {
        String siteURL = request.getRequestURL().toString();
        return siteURL.replace(request.getServletPath(), "");
    }

    private boolean isMobileDevice(String userAgent){
        //Kiểm tra User-Agent header để xác định thiết bị di động
        //Ví dụ đơn giản:
        return userAgent.toLowerCase().contains("mobile");
    }
    @PostMapping("/revoke-token")
    public ResponseEntity<?> revokeToken(@RequestBody RefreshTokenDTO revokeToken){

        try {
            tokenService.revokeToken(revokeToken.getRefreshToken());
            return ResponseEntity.ok().build();
        } catch (DataNotFoundException e) {
           return ResponseEntity.badRequest().body(e.getMessage());
        }


    }
    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody UserLoginDTO userLoginDTO, HttpServletRequest request) {
        //kiem tra thong tin dang nhap va sinh token
        try {
            String token = userService.login(userLoginDTO);
            String userAgent = request.getHeader("User-Agent");
            User userDetail = userService.getUserDetails(token);
            Token jwtToken = tokenService.addToken(userDetail,token,isMobileDevice(userAgent));
            return ResponseEntity.ok(LoginResponse.builder()
                            .message(localizationUtils.getLocalizeMessage(MessageKeys.LOGIN_SUCCESSFULLY))
                            .token(jwtToken.getToken())
                            .tokenType(jwtToken.getTokenType())
                            .refreshToken(jwtToken.getRefreshToken())
                            .userName(userDetail.getUsername())
                            .roles(userDetail.getAuthorities().stream().map(item -> item.getAuthority()).toList())
                            .id(userDetail.getId())
                    .build());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    @GetMapping("/send-verification-code/{id}")
    public ResponseEntity<?> sendVerificationCode(@PathVariable Long id){
        try{
            User user = userService.getUserById(id).orElseThrow(
                    () ->  new DataNotFoundException(localizationUtils.getLocalizeMessage(MessageKeys.NOT_FOUND_USER)));
            userService.sendPasswordResetCodeEmail(user);
            return ResponseEntity.ok().build();
        }catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }

    }
    @PreAuthorize("hasRole('ROLE_USER')")
    @PostMapping("/send-verification-email-code/{id}")
    public ResponseEntity<?> sendChangeEmailCode(@PathVariable Long id,@RequestBody EmailDTO emailDTO){
        try{
            User user = userService.getUserById(id).orElseThrow(
                    () ->  new DataNotFoundException(localizationUtils.getLocalizeMessage(MessageKeys.NOT_FOUND_USER)));
            userService.sendChangeEmailCode(emailDTO,user);
            return ResponseEntity.ok().build();
        }catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }

    }
    @PostMapping("/refreshToken")
    public ResponseEntity<?> refreshToken(@RequestBody RefreshTokenDTO refreshTokenDTO){
        try {
            User userDetail = userService.getUserDetailsFromRefreshToken(refreshTokenDTO.getRefreshToken());
            Token jwtToken = tokenService.refreshToken(refreshTokenDTO.getRefreshToken(),userDetail);
            return ResponseEntity.ok(LoginResponse.builder()
                    .message("Refresh token successfully")
                    .token(jwtToken.getToken())
                    .tokenType(jwtToken.getTokenType())
                    .refreshToken(jwtToken.getRefreshToken())
                    .userName(userDetail.getUsername())
                    .roles(userDetail.getAuthorities().stream().map(item -> item.getAuthority()).toList())
                    .id(userDetail.getId())
                    .build());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }

    }
    @PreAuthorize("hasRole('ROLE_USER') or hasRole('ROLE_ADMIN')")
    @GetMapping("/details")
    public ResponseEntity<?> getUserDetails(@RequestHeader("Authorization") String authorizationHeader) {

        try {
            String extractToken = authorizationHeader.substring(7);
            User user = userService.getUserDetails(extractToken);
            UserDetailResponse userResponse = UserMapper.MAPPER.mapToUserDetailResponse(user);
            return ResponseEntity.ok(userResponse);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }

    }
    @PreAuthorize("hasRole('ROLE_USER')")
    @PutMapping("/details/{user_id}")
    public ResponseEntity<?> updateUser(@PathVariable("user_id") Long id,
                                        @RequestBody UpdateUserDTO updateUserDTO,
                                        @RequestHeader("Authorization") String authorizationHeader){

        try {
            String extractToken = authorizationHeader.substring(7);
            User userDetails = userService.getUserDetails(extractToken);
            if(!userDetails.getId().equals(id)){
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
            User updateUser = userService.updateUser(id,updateUserDTO);
            return ResponseEntity.ok().body(UserMapper.MAPPER.mapToUserResponse(updateUser));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }

    }
    @PreAuthorize("hasRole('ROLE_USER')")
    @PutMapping("/update_email/{user_id}")
    public ResponseEntity<?> updateEmail(@PathVariable("user_id") Long id,
                                            @RequestHeader("Authorization") String authorizationHeader,
                                            @RequestBody EmailDTO emailDTO){
        try {
            String extractToken = authorizationHeader.substring(7);
            User userDetails = userService.getUserDetails(extractToken);
            if(!userDetails.getId().equals(id)){
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
            userService.updateEmail(id,emailDTO);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    @PreAuthorize("hasRole('ROLE_USER')")
    @PutMapping("/update_password/{user_id}")
    public ResponseEntity<?> updatePassword(@PathVariable("user_id") Long id,
                                            @RequestHeader("Authorization") String authorizationHeader,
                                            @RequestBody PasswordDTO passwordDTO){
        try {
            String extractToken = authorizationHeader.substring(7);
            User userDetails = userService.getUserDetails(extractToken);
            if(!userDetails.getId().equals(id)){
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
             userService.updatePassword(id,passwordDTO);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    @PutMapping("/reset-password/{userId}")
    public ResponseEntity<?> resetPassword(@PathVariable Long userId)  {
        try {

            userService.resetPassword(userId);
            Map<String, String> response = new HashMap<>();
            response.put("message", "Làm mới mật khẩu thành công");
            return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(response);
        }

        catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }

    }

    @PutMapping("/blockOrEnable/{userId}/{active}")
    public ResponseEntity<?> blockOrEnable(@PathVariable Long userId,@PathVariable boolean active)  {
        try {

            userService.blockOrEnable(userId,active);
            String message = active == true ? "Successfully enabled the user." : "Successfully blocked the user.";
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("message", message);
            return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(errorResponse);
        }

        catch (Exception e){
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }

    }

}
