package com.project.shopapp.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.project.shopapp.components.JwtTokenUtils;
import com.project.shopapp.components.LocalizationUtils;

import com.project.shopapp.components.converters.RegisterObjectMessageConverter;
import com.project.shopapp.dto.*;
import com.project.shopapp.exceptions.DataAlreadyExistsException;
import com.project.shopapp.exceptions.DataNotFoundException;
import com.project.shopapp.exceptions.ExpiredTokenException;
import com.project.shopapp.exceptions.InvalidRefreshToken;
import com.project.shopapp.models.*;
import com.project.shopapp.repositories.RoleRepository;
import com.project.shopapp.repositories.TokenRepository;
import com.project.shopapp.repositories.UserRepository;
import com.project.shopapp.request.CartItemRequest;
import com.project.shopapp.service.IUserService;
import com.project.shopapp.untils.MessageKeys;
import com.project.shopapp.untils.ValidationUtils;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

import jakarta.persistence.Id;
import org.apache.commons.lang3.RandomStringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.*;

@Service

public class UserServiceImpl extends BaseRedisServiceImpl implements IUserService {
    @Autowired
    private  UserRepository userRepository;
    @Autowired
    private  RoleRepository roleRepository;
    @Autowired
    private  PasswordEncoder passwordEncoder;
    @Autowired
    private  JwtTokenUtils jwtTokenUtils;
    @Autowired
    private  TokenRepository tokenRepository;
    @Autowired
    private  AuthenticationManager authenticationManager;
    @Autowired
    private  LocalizationUtils localizationUtils;
    @Autowired
    private  JavaMailSender mailSender;
    @Autowired
    private  KafkaTemplate<String, Object> kafkaTemplate;
    private static final Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);
    private final static int TIME_WINDOW = 1;
    private final static int LIMIT = 10;


    @Value("${spring.mail.username}")
    private String fromAddress;

    @Value("${spring.mail.shopApp}")
    private String nameShopp;

    public UserServiceImpl(RedisTemplate<String, Object> redisTemplate) {
        super(redisTemplate);
    }

    @Override
    @Transactional
    public User createUser(UserDTO userDTO) throws DataAlreadyExistsException, DataNotFoundException, MessagingException, UnsupportedEncodingException, JsonProcessingException {
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
                .provider(Provider.valueOf(userDTO.getAuthProvider()))
                .dateOfBirth(userDTO.getDateOfBirth())
                .active(true)
                .build();

        Optional<Role> role = roleRepository.findById(userDTO.getRole());
        if(role.isEmpty()){
            throw new DataNotFoundException(localizationUtils.getLocalizeMessage(MessageKeys.ROLE_DOES_NOT_EXISTS));
        }
        newUser.setRole(role.get());
        if(userDTO.getAuthProvider().equals("LOCAL")){
            String password = userDTO.getPassword();
            String encodedPassword = passwordEncoder.encode(password);
            newUser.setPassword(encodedPassword);
        }

        //Nếu đã đăng nhập bằng google thì không cần xác minh qua gmail nữa
        if (newUser.getProvider() != Provider.GOOGLE) {
            String verificationCode = generateVerificationCode(6);

            RegisterObject registerObject = RegisterObject.builder()
                    .email(newUser.getEmail())
                    .verificationCode(verificationCode)
                    .build();
            this.kafkaTemplate.setMessageConverter(new RegisterObjectMessageConverter());
            this.kafkaTemplate.send("user_register_otp_email_topic", registerObject);
//            Lưu mã OTP vào redis
            String user_OTP_Key = "User_OTP:" + newUser.getEmail();
            Map<String, String> userOTP = new HashMap<>();
            userOTP.put(user_OTP_Key,verificationCode);
            saveMap(user_OTP_Key,userOTP);
            setTimeToLive(user_OTP_Key,60);
//            Lưu thông tin user vào redis
            String user_INFO_Key = "User_INFO:" + newUser.getEmail();
            Map<String, User> userINFO = new HashMap<>();
            userINFO.put(user_INFO_Key,newUser);
            saveMap(user_INFO_Key,userINFO);
            setTimeToLive(user_INFO_Key,60);
            newUser.setEnabled(false);
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

    @Override
    public boolean adminExists() {
        return userRepository.existsByRoleId(Long.parseLong("2"));
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
            String phoneNumber = jwtTokenUtils.extractEmail(token);
            User user = userRepository.findByEmail(phoneNumber).orElseThrow(
                    ()-> new DataNotFoundException(localizationUtils.getLocalizeMessage(MessageKeys.NOT_FOUND_USER))
            );
            return user;
    }
    @Override
    @Transactional
    public String login(UserLoginDTO userLoginDTO) throws Exception {
       Optional<User> optionalUser = Optional.empty();
       String subject = null;
        //kiểm tra tên đăng nhập có rỗng hay không
       if(optionalUser.isEmpty() && userLoginDTO.getUser_name() != null){
           //kiểm tra người dùng sử dụng email hay số điện thoại để tiến hành đăng nhập
           if(ValidationUtils.validateEmail(userLoginDTO.getUser_name())) {
               optionalUser = userRepository.findByEmail(userLoginDTO.getUser_name());
               subject = userLoginDTO.getUser_name();
           }
           else if(ValidationUtils.validatePhoneNumber(userLoginDTO.getUser_name())) {
               optionalUser = userRepository.findByPhoneNumber(userLoginDTO.getUser_name());
               subject = userLoginDTO.getUser_name();
           }

       }
       if(optionalUser.isEmpty()){
           throw new DataNotFoundException(localizationUtils.getLocalizeMessage(MessageKeys.WRONG_PHONE_PASSWORD));
       }
       User existingUser = optionalUser.get();

        if(existingUser.getProvider().equals(Provider.LOCAL)){
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
    @Transactional
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
    @Transactional
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

        return getUserDetailsFromToken(existingToken.getToken());
    }

    @Override
    public User getUserDetailsFromToken(String token) throws Exception {
        if(jwtTokenUtils.isTokenExpired(token)){
            throw new ExpiredTokenException("Token is expired");
        }
        String phoneNumber = jwtTokenUtils.extractEmail(token);
        Optional<User> user = userRepository.findByPhoneNumber(phoneNumber);

        if(user.isPresent()){
            return user.get();
        } else {
            throw new Exception(localizationUtils.getLocalizeMessage(MessageKeys.NOT_FOUND_USER));
        }
    }

    @Override
    @Transactional
    public void resetPassword(Long userId) throws DataNotFoundException {
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
                + "Vui lòng đăng nhập với mật khẩu mới: [[newpassword]]<br>"
                + "Cảm ơn,<br>"
                + "VNA Fruit.";

        content = content.replace("[[name]]", existingUser.getFullName())
                .replace("[[newpassword]]", newPassword);

        try{
            sendEmail(toAddress, subject, content, nameShopp);
        }catch (Exception e){
            throw new DataNotFoundException("Không tìm thấy email");
        }


    }
    @Override
    @Transactional
    public void sendMailOrderSuccessfully(OrderDTO order) throws DataNotFoundException {


        String toAddress = order.getEmail();

        String subject = "Đơn hàng của bạn đã được thanh toán thành công!";
        User user = userRepository.findById(order.getUser_id()).orElseThrow(
                () -> new DataNotFoundException(localizationUtils.getLocalizeMessage(MessageKeys.NOT_FOUND_USER))
        );
        // Định dạng ngày tháng
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm");
        String formattedOrderDate = dateFormat.format(order.getOrderDate()); // Đảm bảo order.getOrderDate() là kiểu Date

        // Sử dụng StringBuilder để tạo nội dung email
        StringBuilder contentBuilder = new StringBuilder();
        contentBuilder.append("Kính chào ").append(user.getFullName()).append(",<br><br>")
                .append("Chúng tôi xin thông báo rằng đơn hàng của bạn với mã theo dõi <strong>")
                .append(order.getTrackingNumber()).append("</strong> đã được thanh toán thành công vào ngày <strong>")
                .append(formattedOrderDate).append("</strong>.<br>")
                .append("Cảm ơn bạn đã mua hàng từ chúng tôi! Chúng tôi rất vui khi được phục vụ bạn.<br><br>")
                .append("Nếu bạn có bất kỳ câu hỏi nào về đơn hàng, vui lòng liên hệ với chúng tôi qua email này hoặc truy cập trang hỗ trợ của chúng tôi.<br><br>")
                .append("Trân trọng,<br>")
                .append("Đội ngũ VNA Fruit");

        String content = contentBuilder.toString();

        try {
            sendEmail(toAddress, subject, content, nameShopp);
        } catch (Exception e) {
            throw new DataNotFoundException("Không tìm thấy email của người dùng hoặc đã xảy ra lỗi khi gửi email.");
        }
    }
    @Override
    @Transactional
    public void sendErrorMailOnInvalidEmail(OrderDTO order) throws DataNotFoundException {
        // Địa chỉ email người nhận (ví dụ: email hỗ trợ)
        String toAddress = order.getEmail(); // Gửi đến email hỗ trợ để kiểm tra vấn đề
        String subject = "Lỗi khi gửi email thông báo thanh toán cho đơn hàng";
        User user = userRepository.findById(order.getUser_id()).orElseThrow(
                () -> new DataNotFoundException(localizationUtils.getLocalizeMessage(MessageKeys.NOT_FOUND_USER))
        );
        // Tạo nội dung email thông báo lỗi
        StringBuilder contentBuilder = new StringBuilder();
        contentBuilder.append("Kính chào Đội ngũ hỗ trợ,<br><br>")
                .append("Đơn hàng với mã theo dõi <strong>")
                .append(order.getTrackingNumber()).append("</strong> không thể gửi email thông báo thanh toán thành công đến người dùng. ")
                .append("Lý do có thể là email của người dùng không hợp lệ: <strong>").append(order.getEmail()).append("</strong>.<br><br>")
                .append("Vui lòng kiểm tra và hỗ trợ người dùng nếu cần thiết. Dưới đây là thông tin chi tiết về đơn hàng:<br><br>")
                .append("<strong>Mã theo dõi:</strong> ").append(order.getTrackingNumber()).append("<br>")
                .append("<strong>Tên người dùng:</strong> ").append(user.getFullName()).append("<br>")
                .append("<strong>Địa chỉ email của người dùng:</strong> ").append(order.getEmail()).append("<br><br>")
                .append("Trân trọng,<br>")
                .append("Đội ngũ VNA Fruit");

        String content = contentBuilder.toString();

        // Gửi email thông báo lỗi về việc email sai
        try {
            sendEmail(toAddress, subject, content, nameShopp);
        } catch (Exception e) {
            throw new DataNotFoundException("Không thể gửi email thông báo lỗi về địa chỉ email sai. Vui lòng thử lại.");
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
    public Optional<User> getUserById(Long userId) throws DataNotFoundException {
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
    @Transactional
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
//        user.setVerificationCode(code);
        userRepository.save(user);
        sendEmail(toAddress, subject, content, nameShopp);

    }
    @Override
    @Transactional
    public void sendVerificationEmail(String email,String VerificationCode) throws MessagingException, UnsupportedEncodingException {

            String toAddress = email;
            String subject = "Vui lòng xác nhận đăng ký của bạn";

            // Nội dung email với mã xác nhận
            String content = "Kính chào [[name]],<br>"
                    + "Vui lòng nhập mã xác nhận sau để hoàn tất đăng ký của bạn:<br>"
                    + "<h3>Mã xác nhận: [[verificationCode]]</h3>"
                    + "Cảm ơn bạn,<br>"
                    + "VNA Fruit.";

            // Thay thế tên người dùng
            content = content.replace("[[name]]", "khách hàng thân mến");

            // Thay thế mã xác nhận
            String verificationCode = VerificationCode;
            content = content.replace("[[verificationCode]]", verificationCode);

            // Gửi email
            sendEmail(toAddress, subject, content, nameShopp);

    }

    @Override
    @Transactional
    public void sendChangeEmailCode(EmailDTO emailDTO,User user) throws MessagingException, UnsupportedEncodingException, JsonProcessingException {
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
        String userKey = "User_OTP:" + user.getEmail();
//        user.setVerificationCode(code);
        Map<String, String> map = new HashMap<>();
        map.put(userKey,code);
        saveMap(userKey,map);
        setTimeToLive(userKey,60);
        userRepository.save(user);

        sendEmail(toAddress, subject, content, nameShopp);
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
    public boolean verify(String verificationCode,String email) throws JsonProcessingException, DataNotFoundException {
        String user_OTP_Key = "User_OTP:" + email;
        String user_INFO_Key = "User_INFO:" + email;

        Map<String, String> user_OTP = (Map<String, String>) getMap(user_OTP_Key, String.class, String.class);
        Map<String, User> user_INFO = (Map<String, User>) getMap(user_INFO_Key, String.class, User.class);

        if (user_OTP.isEmpty() || user_INFO.get(user_INFO_Key) == null || !user_INFO.get(user_INFO_Key).isEnabled()) {
            return false;
        }
        else {
           String code = user_OTP.get(user_OTP_Key);
           if(code.equals(verificationCode)) {
               User user = user_INFO.get(user_INFO_Key);
               user.setEnabled(true);
               userRepository.save(user);
               return true;
           }
           return false;

        }

    }


}
