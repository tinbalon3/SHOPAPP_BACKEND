package com.project.shopapp.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.project.shopapp.components.LocalizationUtils;
import com.project.shopapp.dto.EmailDTO;
import com.project.shopapp.dto.OrderDTO;
import com.project.shopapp.exceptions.DataNotFoundException;
import com.project.shopapp.exceptions.EmailNotRegisterException;
import com.project.shopapp.models.Provider;
import com.project.shopapp.models.Token;
import com.project.shopapp.models.User;
import com.project.shopapp.repositories.TokenRepository;
import com.project.shopapp.repositories.UserRepository;
import com.project.shopapp.service.ISendEmailService;
import com.project.shopapp.untils.MessageKeys;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class SendEmailServiceImpl extends BaseRedisServiceImpl implements ISendEmailService {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private JavaMailSender mailSender;
    @Value("${spring.mail.shopApp}")
    private String nameShopp;
    @Value("${spring.mail.username}")
    private String fromAddress;
    @Autowired
    private LocalizationUtils localizationUtils;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private TokenRepository tokenRepository;


    @Override
    @Transactional
    public void sendPasswordResetEmailCode(User user) throws MessagingException, UnsupportedEncodingException, JsonProcessingException {
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
        String userKey = "User_OTP_reset_password:" + toAddress;
        Map<String, String> map = new HashMap<>();
        map.put(userKey,code);
        saveMap(userKey,map);
        setTimeToLive(userKey,60);

        sendEmail(toAddress, subject, content, nameShopp);
    }

    @Override
    public void sendVerificationEmailCode(String email, String VerificationCode) throws MessagingException, UnsupportedEncodingException, JsonProcessingException {
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
        String userKey = "User_OTP_register:" + email;
        Map<String, String> map = new HashMap<>();
        map.put(userKey,VerificationCode);
        saveMap(userKey,map);
        setTimeToLive(userKey,60);
        // Gửi email
        sendEmail(toAddress, subject, content, nameShopp);
    }

    @Override
    public void sendChangeEmailCode(EmailDTO emailDTO, User user) throws MessagingException, UnsupportedEncodingException, JsonProcessingException {
        String toAddress = emailDTO.getEmail();
        String subject = "Xác nhận yêu cầu đổi email của bạn";

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
        Map<String, String> map = new HashMap<>();
        map.put(userKey,code);
        saveMap(userKey,map);
        setTimeToLive(userKey,60);

        sendEmail(toAddress, subject, content, nameShopp);
    }

    @Override
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
    public void resetPassword(String email) throws DataNotFoundException, MessagingException, UnsupportedEncodingException, EmailNotRegisterException {
        User existingUser = userRepository.findByEmail(email)
                .orElseThrow(() -> new DataNotFoundException("Email không tồn tại trong hệ thống."));
        if(existingUser.getProvider() == Provider.LOCAL) {
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

            sendEmail(toAddress, subject, content, nameShopp);

        }
        else{
            throw new EmailNotRegisterException("Tài khoản này đã đăng nhập qua Google. Không thể lấy lại mật khẩu qua hệ thống");
        }
    }
    private String generateVerificationCode(int longCode){
        String randomCode = RandomStringUtils.randomNumeric(longCode);
        return randomCode;
    }
    private void sendEmail(String toAddress, String subject, String content, String senderName)
            throws MessagingException, UnsupportedEncodingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        helper.setFrom(fromAddress, senderName);
        helper.setTo(toAddress);
        helper.setSubject(subject);
        helper.setText(content, true);
       try {
           mailSender.send(message);
       }
        catch (Exception e){
           throw new MessagingException("Email không tồn tại.");
        }
    }
}
