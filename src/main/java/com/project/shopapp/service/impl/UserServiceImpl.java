package com.project.shopapp.service.impl;

import com.project.shopapp.components.JwtTokenUtils;
import com.project.shopapp.components.LocalizationUtils;
import com.project.shopapp.dto.*;
import com.project.shopapp.exception.DataAlreadyExistsException;
import com.project.shopapp.exception.DataNotFoundException;
import com.project.shopapp.exception.ExpiredTokenException;
import com.project.shopapp.exception.InvalidRefreshToken;
import com.project.shopapp.models.Role;
import com.project.shopapp.models.Token;
import com.project.shopapp.models.User;
import com.project.shopapp.repositories.RoleRepository;
import com.project.shopapp.repositories.TokenRepository;
import com.project.shopapp.repositories.UserRepository;
import com.project.shopapp.service.IUserService;
import com.project.shopapp.untils.MessageKeys;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements IUserService {

    private final UserRepository userRepository;

    private final RoleRepository roleRepository;

    private final PasswordEncoder passwordEncoder;

    private final JwtTokenUtils jwtTokenUtils;

    private final TokenRepository tokenRepository;

    private final AuthenticationManager authenticationManager;

    private final LocalizationUtils localizationUtils;

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromAddress;

    @Value("${spring.mail.shopApp}")
    private String nameShopp;

    @Override
    @Transactional
    public User createUser(UserDTO userDTO) throws DataAlreadyExistsException,DataNotFoundException,  MessagingException, UnsupportedEncodingException {
        String phoneNumber = userDTO.getPhoneNumber();
        if(userRepository.existsByPhoneNumber(phoneNumber)){
            throw new DataAlreadyExistsException("Phone number already exists");
        };

        String email = userDTO.getEmail();
        if(userRepository.existsByEmail(email)){
            throw new DataAlreadyExistsException("Email already exists");
        };
//        User newUser = UserMapper.MAPPER.mapToUser(userDTO);
        User newUser = User.builder()
                .fullName(userDTO.getFullName())
                .password(userDTO.getPassword())
                .address(userDTO.getAddress())
                .phoneNumber(userDTO.getPhoneNumber())
                .email(userDTO.getEmail())
                .dateOfBirth(userDTO.getDateOfBirth())
                .facebookAccountId(userDTO.getFacebookAccountId())
                .googleAccountId(userDTO.getGoogleAccountId())
                .active(true)
                .build();
        Optional<Role> role = roleRepository.findById(userDTO.getRole());
        if(role.isEmpty()){
            throw new DataNotFoundException(localizationUtils.getLocalizeMessage(MessageKeys.ROLE_DOES_NOT_EXISTS));
        }
        newUser.setRole(role.get());
        if(userDTO.getFacebookAccountId() == 0 && userDTO.getGoogleAccountId() == 0){
            String password = userDTO.getPassword();
            String encodedPassword = passwordEncoder.encode(password);
            newUser.setPassword(encodedPassword);
        }
        String verificationCode = generateVerificationCode(6);
        newUser.setVerificationCode(verificationCode);
        newUser.setEnabled(false);
        sendVerificationEmail(newUser);
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
            String phoneNumber = jwtTokenUtils.extractPhoneNumber(token);
            User user = userRepository.findByPhoneNumber(phoneNumber).orElseThrow(
                    ()-> new DataNotFoundException(localizationUtils.getLocalizeMessage(MessageKeys.NOT_FOUND_USER))
            );
            return user;
    }
    @Override
    public String login(UserLoginDTO userLoginDTO) throws Exception {
       Optional<User> optionalUser = Optional.empty();
       String subject = null;
       if(userLoginDTO.getPhoneNumber() != null && !userLoginDTO.getPhoneNumber().isBlank()){
           optionalUser = userRepository.findByPhoneNumber(userLoginDTO.getPhoneNumber());
           subject = userLoginDTO.getPhoneNumber();
       }
       if(optionalUser.isEmpty() && userLoginDTO.getEmail() != null){
           optionalUser = userRepository.findByEmail(userLoginDTO.getEmail());
           subject = userLoginDTO.getEmail();
       }
       if(optionalUser.isEmpty()){
           throw new DataNotFoundException(localizationUtils.getLocalizeMessage(MessageKeys.WRONG_PHONE_PASSWORD));
       }
       User existingUser = optionalUser.get();
       //check password
        if(existingUser.getFacebookAccountId() == 0 && existingUser.getGoogleAccountId() == 0){
            if(!passwordEncoder.matches(userLoginDTO.getPassword(),existingUser.getPassword())){
                throw new BadCredentialsException(localizationUtils.getLocalizeMessage(MessageKeys.WRONG_PHONE_PASSWORD));
            }
        }
        Optional<Role> optionalRole = roleRepository.findById(userLoginDTO.getRoleId());
        if(optionalRole.isEmpty() || !userLoginDTO.getRoleId().equals(existingUser.getRole().getId())){
            throw new DataNotFoundException(localizationUtils.getLocalizeMessage(MessageKeys.ROLE_DOES_NOT_EXISTS));
        }
        if(!existingUser.isActive()){
            throw new DataNotFoundException(localizationUtils.getLocalizeMessage(MessageKeys.USER_IS_LOCKED));
        }
       //authenticate with java spring security
        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                subject,userLoginDTO.getPassword(),existingUser.getAuthorities()
        );

        authenticationManager.authenticate(authenticationToken);
//        SecurityContextHolder.getContext().setAuthentication(authenticationManager.authenticate(authenticationToken));
       return jwtTokenUtils.generateToken(existingUser);
    }

    @Override
    public void updateEmail(Long id, EmailDTO emailDTO) throws Exception {
        User existingUser = userRepository.findById(id).orElseThrow(
                () -> new DataNotFoundException(localizationUtils.getLocalizeMessage(MessageKeys.NOT_FOUND_USER))
        );
        if(emailDTO.getEmail() != null){
            existingUser.setEmail(emailDTO.getEmail());
        }
        userRepository.save(existingUser);
    }

    @Override
    public void updatePassword(Long id, PasswordDTO passwordDTO) throws Exception {
        User existingUser = userRepository.findById(id).orElseThrow(
                () -> new DataNotFoundException(localizationUtils.getLocalizeMessage(MessageKeys.NOT_FOUND_USER))
        );
        if(passwordDTO.getPassword() != null && !passwordDTO.getPassword().isEmpty()){
            if(!passwordDTO.getPassword().equals(passwordDTO.getRetypePassword())){
                throw new DataIntegrityViolationException(localizationUtils.getLocalizeMessage(MessageKeys.PASSWORD_RETYPE_PASSWORD));
            }
            String newPassword = passwordDTO.getPassword();
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

        return getUserDetailsFromToken(existingToken.getToken());
    }

    @Override
    public User getUserDetailsFromToken(String token) throws Exception {
        if(jwtTokenUtils.isTokenExpired(token)){
            throw new ExpiredTokenException("Token is expired");
        }
        String phoneNumber = jwtTokenUtils.extractPhoneNumber(token);
        Optional<User> user = userRepository.findByPhoneNumber(phoneNumber);

        if(user.isPresent()){
            return user.get();
        } else {
            throw new Exception(localizationUtils.getLocalizeMessage(MessageKeys.NOT_FOUND_USER));
        }
    }

    @Override
    @Transactional
    public void resetPassword(Long userId) throws DataNotFoundException, MessagingException, UnsupportedEncodingException {
        User existingUser = userRepository.findById(userId)
                .orElseThrow(() -> new DataNotFoundException(localizationUtils.getLocalizeMessage(MessageKeys.NOT_FOUND_USER,userId)));
        String newPassword = UUID.randomUUID().toString().substring(0,5);
        String encodePassword = passwordEncoder.encode(newPassword);
        existingUser.setPassword(encodePassword);
        userRepository.save(existingUser);

        // Xóa token sau khi đổi mật khẩu
        List<Token> tokens = tokenRepository.findByUser(existingUser);
        tokenRepository.deleteAll(tokens);

        String toAddress = existingUser.getEmail();
        String subject = "Mật khẩu của bạn đã được làm mới";
        String content = "Kính chào [[name]],<br>"
                + "Vui lòng nhấp vào đường dẫn bên dưới để đăng nhập với mật khẩu mới: [[newpassword]]<br>"
                + "<h3><a href=\"[[URL]]\" target=\"_self\">Đăng nhập</a></h3>"
                + "Cảm ơn,<br>"
                + "VNA Fruit.";

        content = content.replace("[[name]]", existingUser.getFullName())
                .replace("[[newpassword]]", newPassword);

        String verifyURL = "https://7fa0-42-116-205-114.ngrok-free.app/login";
        content = content.replace("[[URL]]", verifyURL);

        try{
            sendEmail(toAddress, subject, content, nameShopp);
        }catch (Exception e){
            throw new DataNotFoundException("Không tìm thấy email");
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
    public Optional<User> getUserById(Long userId) {
        return userRepository.findById(userId);
    }
    private void sendEmail(String toAddress, String subject, String content, String senderName)
            throws MessagingException, UnsupportedEncodingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        helper.setFrom(fromAddress, senderName);
        helper.setTo(toAddress);
        helper.setSubject(subject);
        helper.setText(content, true);

        mailSender.send(message);
    }
    @Override
    public void sendPasswordResetCodeEmail(User user) throws MessagingException, UnsupportedEncodingException {
        String toAddress = user.getEmail();
        String subject = "Xác nhận yêu cầu đổi mật khẩu của bạn";

        String content = "Kính chào [[name]],<br>"
                + "Chúng tôi nhận được yêu cầu đổi mật khẩu cho tài khoản của bạn.<br>"
                + "Vui lòng nhập mã xác nhận sau để tiếp tục quá trình đổi mật khẩu:<br>"
                + "<h2>[[code]]</h2><br>"
                + "Nếu bạn không yêu cầu thay đổi mật khẩu, vui lòng bỏ qua email này.<br>"
                + "Cảm ơn,<br>"
                + "VNA Fruit Team.";
        String code = generateVerificationCode(6);
        content = content.replace("[[name]]", user.getFullName())
        .replace("[[code]]",code);
        user.setVerificationCode(code);
        userRepository.save(user);
        sendEmail(toAddress, subject, content, nameShopp);

    }
    @Override
    public void sendVerificationEmail(User user) throws MessagingException, UnsupportedEncodingException {

            String toAddress = user.getEmail();
            String subject = "Vui lòng xác nhận đăng ký của bạn";

            // Nội dung email với mã xác nhận
            String content = "Kính chào [[name]],<br>"
                    + "Vui lòng nhập mã xác nhận sau để hoàn tất đăng ký của bạn:<br>"
                    + "<h3>Mã xác nhận: [[verificationCode]]</h3>"
                    + "Cảm ơn bạn,<br>"
                    + "VNA Fruit.";

            // Thay thế tên người dùng
            content = content.replace("[[name]]", user.getFullName());

            // Thay thế mã xác nhận
            String verificationCode = user.getVerificationCode();
            content = content.replace("[[verificationCode]]", verificationCode);

            // Gửi email
            sendEmail(toAddress, subject, content, nameShopp);

    }

    @Override
    public void sendChangeEmailCode(EmailDTO emailDTO,User user) throws MessagingException, UnsupportedEncodingException {
        String toAddress = emailDTO.getEmail();
        String subject = "Xác nhận yêu cầu đổi mật email của bạn";

        String content = "Kính chào [[name]],<br>"
                + "Chúng tôi nhận được yêu cầu đổi email cho tài khoản của bạn.<br>"
                + "Vui lòng nhập mã xác nhận sau để tiếp tục quá trình đổi email:<br>"
                + "<h2>[[code]]</h2><br>"
                + "Nếu bạn không yêu cầu thay đổi email, vui lòng bỏ qua email này.<br>"
                + "Cảm ơn,<br>"
                + "VNA Fruit Team.";
        String code = generateVerificationCode(6);
        content = content.replace("[[name]]", user.getFullName())
                .replace("[[code]]",code);
        user.setVerificationCode(code);
        userRepository.save(user);
        sendEmail(toAddress, subject, content, nameShopp);
    }


    public boolean verify(String verificationCode) {
        User user = userRepository.findByVerificationCode(verificationCode);

        if (user == null || !user.isEnabled()) {
            return false;
        } else {
            user.setVerificationCode(null);
            user.setEnabled(true);
            userRepository.save(user);

            return true;
        }

    }

    @Override
    public void saveUserToDB(User user) {
        userRepository.save(user);
    }
}
