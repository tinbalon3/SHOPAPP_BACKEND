package com.project.shopapp.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.shopapp.components.JwtTokenUtils;
import com.project.shopapp.components.LocalizationUtils;

import com.project.shopapp.components.converters.RegisterObjectMessageConverter;
import com.project.shopapp.dto.*;
import com.project.shopapp.exceptions.*;
import com.project.shopapp.models.*;
import com.project.shopapp.repositories.RoleRepository;
import com.project.shopapp.repositories.TokenRepository;
import com.project.shopapp.repositories.UserRepository;
import com.project.shopapp.request.UpdatePasswordRequest;
import com.project.shopapp.service.IUserService;
import com.project.shopapp.untils.MessageKeys;
import com.project.shopapp.untils.ValidationUtils;

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.RandomStringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class UserServiceImpl extends BaseRedisServiceImpl implements IUserService {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private RoleRepository roleRepository;
    @Autowired
    private JwtTokenUtils jwtTokenUtils;
    @Autowired
    private TokenRepository tokenRepository;
    @Autowired
    private AuthenticationManager authenticationManager;
    @Autowired
    private LocalizationUtils localizationUtils;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;
    private static final Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);
    private final static int TIME_WINDOW = 1;
    private final static int LIMIT = 10;




    @Override
    @Transactional
    public User createUser(UserDTO userDTO) throws  JsonProcessingException, InvalidDataRegisterException {

        Optional<Role> role = roleRepository.findById(userDTO.getRole());
        if(userDTO.getAuthProvider().equals("LOCAL")) {
            if(!ValidationUtils.isValidName(userDTO.getFullName())) {
                throw new InvalidDataRegisterException("Họ và tên không hợp lệ.");
            }
            if(!userDTO.isAccepted()) {
                throw new InvalidDataRegisterException("Bạn phải đồng ý với Điều khoản & Điều kiện để đăng ký.");
            }
            if(!ValidationUtils.validatePhoneNumber(userDTO.getPhoneNumber())) {
                throw new InvalidDataRegisterException("Số điện thoại không hợp lệ.");
            }
            if(!ValidationUtils.validateEmail(userDTO.getEmail())) {
                throw new InvalidDataRegisterException("Email không hợp lệ.");
            }
            if(!ValidationUtils.isAdult(userDTO.getDateOfBirth())){
                throw new InvalidDataRegisterException("Bạn phải đủ 18 tuổi để đăng ký.");
            }
            if(userRepository.existsUserWithPhoneNumberAndRoleNotAdmin(userDTO.getPhoneNumber())){
                throw new InvalidDataRegisterException("Số điện thoại đã được đăng kí.");
            };
            if(userRepository.existsByEmail(userDTO.getEmail())){
                throw new InvalidDataRegisterException("Email đã được đăng kí.");
            };
            if(!userDTO.getPassword().equals(userDTO.getRetypePassword())){
                throw new InvalidDataRegisterException(localizationUtils.getLocalizeMessage(MessageKeys.PASSWORD_NOT_MATCH));
            }
            if(role.isEmpty()){
                throw new InvalidDataRegisterException(localizationUtils.getLocalizeMessage(MessageKeys.ROLE_DOES_NOT_EXISTS));
            }
        }



        User newUser = User.builder()
                .fullName(userDTO.getFullName())
                .password(userDTO.getPassword())
                .address(userDTO.getAddress())
                .phoneNumber(userDTO.getPhoneNumber())
                .email(userDTO.getEmail())
                .provider(Provider.valueOf(userDTO.getAuthProvider()))
                .dateOfBirth(userDTO.getDateOfBirth())
                .enabled(false)
                .active(true)
                .build();

        newUser.setRole(role.get());

        if (newUser.getProvider() == Provider.LOCAL) {

            String encodedPassword = passwordEncoder.encode(userDTO.getPassword());
            newUser.setPassword(encodedPassword);

            String verificationCode = generateVerificationCode(6);
            RegisterObject registerObject = RegisterObject.builder()
                    .email(newUser.getEmail())
                    .verificationCode(verificationCode)
                    .build();

            this.kafkaTemplate.setMessageConverter(new RegisterObjectMessageConverter());
            this.kafkaTemplate.send("user_register_otp_email_topic", registerObject);

//            Lưu mã OTP vào redis
            String user_OTP_Key = "User_OTP_register:" + newUser.getEmail();
            Map<String, String> userOTP = new HashMap<>();
            userOTP.put(user_OTP_Key,verificationCode);
            saveMap(user_OTP_Key,userOTP);
            setTimeToLive(user_OTP_Key,60);

//            Lưu thông tin user vào redis
            String user_INFO_Key = "User_INFO_register:" + newUser.getEmail();
            Map<String, User> userINFO = new HashMap<>();
            userINFO.put(user_INFO_Key,newUser);
            saveMap(user_INFO_Key,userINFO);
            setTimeToLive(user_INFO_Key,60);

        }
        else {
            newUser.setEnabled(true);
        }

        return newUser;
    }

    @Override
    public User createAdmin(String adminName, String password_admin, Role role) {

        User newUser = User.builder()
                .fullName("admin")
                .password(password_admin)
                .address("")
                .phoneNumber("")
                .email(adminName)
                .provider(Provider.LOCAL)
                .dateOfBirth(new Date())
                .active(true)
                .build();


        newUser.setRole(role);

        String password = password_admin;
        String encodedPassword = passwordEncoder.encode(password);
        newUser.setPassword(encodedPassword);


        newUser.setEnabled(true);


        return userRepository.save(newUser);
    }



    private String generateVerificationCode(int longCode){
        String randomCode = RandomStringUtils.randomNumeric(longCode);
        return randomCode;
    }
    @Override
    public Page<User> getAllUser(String keyword, Pageable pageable) {
        return userRepository.findAll(keyword,pageable);
    }



    @Override
    public User getUserDetails(String token) throws DataNotFoundException {
            String email = jwtTokenUtils.extractEmail(token);
            User user = userRepository.findByEmail(email).orElseThrow(
                    ()-> new DataNotFoundException(localizationUtils.getLocalizeMessage(MessageKeys.NOT_FOUND_USER))
            );
            return user;
    }
    @Override
    @Transactional
    public String login(UserLoginDTO userLoginDTO) throws EmailNotRegisterException, DataNotFoundException, InvalidPasswordException, UserErrorException {
        User user = validateUserLogin(userLoginDTO);
        checkUserStatus(user);
        authenticateUser(userLoginDTO, user);
        return jwtTokenUtils.generateToken(user);
    }

    private User validateUserLogin(UserLoginDTO userLoginDTO) throws  UserErrorException {
        if (userLoginDTO.getUserName().isEmpty() || userLoginDTO.getPassword().isEmpty()) {
            throw new UserErrorException(localizationUtils.getLocalizeMessage(MessageKeys.LOGIN_NULL_DATA));
        }

        User user = null;

        // Kiểm tra nếu là tài khoản admin, xử lý riêng biệt
        if (isAdminLogin(userLoginDTO)) {
            user = findAdmin(userLoginDTO);
        } else {
            // Kiểm tra nếu là người dùng thông thường, tìm theo email
            if (ValidationUtils.validateEmail(userLoginDTO.getUserName())) {
                Optional<User> optionalUser = userRepository.findByEmail(userLoginDTO.getUserName());
                if (optionalUser.isPresent()) {
                    user = optionalUser.get();
                }
            }
        }

        if (user == null) {
            throw new UserErrorException(localizationUtils.getLocalizeMessage(MessageKeys.LOGIN_NULL_DATA));
        }

        return user;
    }

    private boolean isAdminLogin(UserLoginDTO userLoginDTO) {
        // Kiểm tra nếu email của người dùng khớp với định dạng admin@dev.vna.com
        return userLoginDTO.getUserName() != null && userLoginDTO.getUserName().equalsIgnoreCase("@dev.vna.com");
    }


    private User findAdmin(UserLoginDTO userLoginDTO) throws UserErrorException {
        // Tìm tài khoản admin theo email (hoặc cách khác tùy vào cách bạn lưu thông tin admin)
        Optional<User> adminOptional = userRepository.findByEmail(userLoginDTO.getUserName());
        if (adminOptional.isPresent()) {
            User admin = adminOptional.get();
            if (admin.getRole().getId() != 2L) { // Kiểm tra xem role của tài khoản có phải là admin không
                throw new UserErrorException("Không có quyền truy cập");
            }
            return admin;
        }
        throw new UserErrorException(localizationUtils.getLocalizeMessage(MessageKeys.LOGIN_NULL_DATA));
    }


    private void authenticateUser(UserLoginDTO userLoginDTO, User user) throws  UserErrorException {
        // Kiểm tra nếu người dùng đăng nhập qua phương thức LOCAL
        if (user.getProvider().equals(Provider.LOCAL)) {
            if (!passwordEncoder.matches(userLoginDTO.getPassword(), user.getPassword())) {
                throw new UserErrorException(localizationUtils.getLocalizeMessage(MessageKeys.WRONG_PHONE_PASSWORD));
            }
        } else {
            throw new UserErrorException(localizationUtils.getLocalizeMessage(MessageKeys.WRONG_PHONE_PASSWORD));
        }

        // Xác thực với Spring Security
        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                user.getEmail(), userLoginDTO.getPassword(), user.getAuthorities());
        authenticationManager.authenticate(authenticationToken);
    }



    private void checkUserStatus(User user) throws UserErrorException {
        if (!user.isActive()) {
            throw new UserErrorException(localizationUtils.getLocalizeMessage(MessageKeys.USER_IS_NOT_ACTIVE));
        }
        if (!user.isEnabled()) {
            throw new UserErrorException(localizationUtils.getLocalizeMessage(MessageKeys.USER_IS_LOCKED));
        }
    }


    @Override
    @Transactional
    public void updateEmail(EmailDTO emailDTO) throws Exception {
        User existingUser = userRepository.findByEmail(emailDTO.getEmail()).orElseThrow(
                () -> new DataNotFoundException(localizationUtils.getLocalizeMessage(MessageKeys.NOT_FOUND_USER))
        );
        if(emailDTO.getEmail_new() != null){
            existingUser.setEmail(emailDTO.getEmail_new());
        }
        userRepository.save(existingUser);
    }

    @Override
    @Transactional
    public void updatePassword(String email, UpdatePasswordRequest updatePasswordRequest) throws Exception {
        User existingUser = userRepository.findByEmail(email).orElseThrow(
                () -> new DataNotFoundException(localizationUtils.getLocalizeMessage(MessageKeys.NOT_FOUND_USER,email))
        );
        if(updatePasswordRequest.getPassword() != null && !updatePasswordRequest.getPassword().isEmpty()){
            if(!updatePasswordRequest.getPassword().equals(updatePasswordRequest.getRetypePassword())){
                throw new DataIntegrityViolationException(localizationUtils.getLocalizeMessage(MessageKeys.PASSWORD_RETYPE_PASSWORD));
            }
            String newPassword = updatePasswordRequest.getPassword();
            String encodePassword = passwordEncoder.encode(newPassword);
            existingUser.setPassword(encodePassword);
        }
        userRepository.save(existingUser);
    }

    @Override
    @Transactional
    public User updateUser(Long id, UpdateUserDTO updateUserDTO) throws Exception {
        User existingUser = userRepository.findById(id).orElseThrow(
                () -> new DataNotFoundException(localizationUtils.getLocalizeMessage(MessageKeys.NOT_FOUND_USER))
        );

        if(updateUserDTO.getFullName() != null)
            existingUser.setFullName(updateUserDTO.getFullName());
        if(updateUserDTO.getAddress() != null)
            existingUser.setAddress(updateUserDTO.getAddress());
        if(updateUserDTO.getDateOfBirth() != null)
            existingUser.setDateOfBirth(updateUserDTO.getDateOfBirth());
        if(updateUserDTO.getPhoneNumber() != null)
            existingUser.setPhoneNumber(updateUserDTO.getPhoneNumber());
//        if(updateUserDTO.getGoogleAccountId() > 0)
//            existingUser.setGoogleAccountId(updateUserDTO.getGoogleAccountId());
//        if(updateUserDTO.getFacebookAccountId() > 0)
//            existingUser.setFacebookAccountId(updateUserDTO.getFacebookAccountId());
//
//        if(updateUserDTO.getPassword() != null && !updateUserDTO.getPassword().isEmpty()){
//            if(!updateUserDTO.getPassword().equals(updateUserDTO.getRetypePassword())){
//                throw new DataIntegrityViolationException(localizationUtils.getLocalizeMessage(MessageKeys.PASSWORD_RETYPE_PASSWORD));
//            }
//            String newPassword = updateUserDTO.getPassword();
//            String encodePassword = passwordEncoder.encode(newPassword);
//            existingUser.setPassword(encodePassword);
//        }
        return userRepository.save(existingUser);

    }

    @Override
    public User getUserDetailsFromRefreshToken(String refreshToken) throws Exception {
        Token existingToken = tokenRepository.findByRefreshToken(refreshToken).orElseThrow(
                () -> new InvalidRefreshToken("Invalid RefreshToken"));

        return getUserDetailsFromToken(existingToken.getUser().getEmail());
    }

    @Override
    public User getUserDetailsFromToken(String phoneNumber) throws Exception {
//        if(jwtTokenUtils.isTokenExpired(token)){
//            throw new ExpiredTokenException("Token is expired");
//        }
//        String phoneNumber = jwtTokenUtils.extractEmail(token);
        Optional<User> user = userRepository.findByEmail(phoneNumber);

        if(user.isPresent()){
            return user.get();
        } else {
            throw new Exception(localizationUtils.getLocalizeMessage(MessageKeys.NOT_FOUND_USER));
        }
    }





    @Override
    @Transactional
    public void blockOrEnable(Long userId, Boolean active) throws DataNotFoundException {
        User existingUser = userRepository.findById(userId)
                .orElseThrow(() -> new DataNotFoundException(localizationUtils.getLocalizeMessage(MessageKeys.NOT_FOUND_USER,userId)));
        existingUser.setActive(active);
        userRepository.save(existingUser);


    }

    @Override
    public Optional<User> getUserById(Long userId)  {
        return userRepository.findById(userId);
    }






    @Override
    public boolean isAllowed(String userId) {
        String key = "rate:limit:" + userId;
        Long currentCount = increment(key);
        logger.info("Current count: {}", currentCount);
        if (currentCount == 1) {
            // Đặt thời gian hết hạn của khóa thành 1 giây
            setExpired(key, TIME_WINDOW);
        }

        // vượt quá giới hạn
        // check controller forgot
        return currentCount <= LIMIT;
    }

    @Override
    public Optional<User> getUserByEmail(String email) throws DataNotFoundException {
        return userRepository.findByEmail(email);
    }

    @Override
    public boolean adminExists() {
        boolean role = userRepository.existsByRoleId(Double.parseDouble("2"));
        return role;
    }

}
