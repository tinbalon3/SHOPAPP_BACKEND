package com.project.shopapp.IntegrationTest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.shopapp.models.Provider;
import com.project.shopapp.models.Role;
import com.project.shopapp.models.User;
import com.project.shopapp.repositories.RoleRepository;
import com.project.shopapp.repositories.TokenRepository;
import com.project.shopapp.repositories.UserRepository;
import io.qameta.allure.AllureId;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestPropertySource(properties = {
        "spring.flyway.enabled=false",
        "spring.datasource.url=jdbc:h2:mem:testdb",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class UserRegisterIntegrationTest {
    @Autowired
    private MockMvc mockMvc;



    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;



    @BeforeAll
    void setupUser() {
        // Thêm user vào database H2 để test
        User user = new User();
        user.setFullName("Man Do");
        user.setPhoneNumber("0123456789");
        user.setEmail("mando150903@gmail.com");
        user.setActive(true);
        user.setEnabled(true);
        user.setProvider(Provider.LOCAL);
        Optional<Role> role = roleRepository.findById(1L);
        user.setPassword(new BCryptPasswordEncoder().encode("123456")); // Mã hóa password
        user.setRole(role.get());
        userRepository.save(user);
    }
    @Test

    @DisplayName("Email không tồn tại nhưng vẫn pass và tiến hành gửi otp ")
    public void testRegister_001() throws Exception {
        String requestBody = "{\n" +
                "  \"fullname\": \"Dang Ngan Dong\",\n" +
                "  \"phone_number\": \"0258963155\",\n" +
                "  \"address\": \"Nha A ngo~ B\",\n" +
                "  \"password\": \"123456\",\n" +
                "  \"email\": \"example123@gmail.com\",\n" +
                "  \"retype_password\": \"123456\",\n" +
                "  \"date_of_birth\": \"2003-05-13\",\n" +
                "  \"role_id\": 1,\n" +
                "  \"auth_provider\": \"LOCAL\",\n" +
                "  \"isAccepted\": true\n" +
                "}";
        var resultActions = mockMvc.perform(post("/api/v1/users/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody)
                .header("User-Agent", "mobile")
                .header("Accept-Language", "vi"));

        resultActions.andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Tạm thời tạo User thành công."))
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.data").isEmpty());
    }
    @Test
    @DisplayName("Xác nhận mã OTP được gửi về testcase 003")
    public void testRegister_002() throws Exception {
        
        String requestBody = "{\n" +
                "  \"email\": \"dangngandong2603@gmail.com\",\n" +
                "  \"code\": \"123456\"" + "}";
        var resultActions = mockMvc.perform(put("/api/v1/users/register/verify")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody)
                .header("Accept-Language", "vi"));

        resultActions.andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Xác thực mã OTP thành công."))
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.data").isEmpty());
    }
    @Test

    @DisplayName("Người dùng nhập thông tin đăng kí hợp lệ")
    public void testRegister_003() throws Exception {
        String requestBody = "{\n" +
                "  \"fullname\": \"Dang Ngan Dong\",\n" +
                "  \"phone_number\": \"0258963155\",\n" +
                "  \"address\": \"Nha A ngo~ B\",\n" +
                "  \"password\": \"123456\",\n" +
                "  \"email\": \"dangngandong2603@gmail.com\",\n" +
                "  \"retype_password\": \"123456\",\n" +
                "  \"date_of_birth\": \"2003-05-13\",\n" +
                "  \"role_id\": 1,\n" +
                "  \"auth_provider\": \"LOCAL\",\n" +
                "  \"isAccepted\": true\n" +
                "}";
        var resultActions = mockMvc.perform(post("/api/v1/users/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody)
                .header("User-Agent", "mobile")
                .header("Accept-Language", "vi"));

        resultActions.andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Tạm thời tạo User thành công."))
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.data").isEmpty());
    }
    @Test
    @DisplayName("Nhập mã OTP không đúng")
    public void testRegister_004() throws Exception {
        String requestBody = "{\n" +
                "  \"email\": \"dangngandong2603@gmail.com\",\n" +
                "  \"code\": \"123456\"" + "}";
        var resultActions = mockMvc.perform(put("/api/v1/users/register/verify")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody)
                .header("Accept-Language", "vi"));

        resultActions.andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Xác thực mã OTP không thành công."))
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.data").isEmpty());
    }
    @Test
    @DisplayName("Đăng kí không thành công vì ngày sinh dưới 18 tuổi")
    public void testRegister_005() throws Exception {
        String requestBody = "{\n" +
                "  \"fullname\": \"Dang Ngan Dong\",\n" +
                "  \"phone_number\": \"0258963155\",\n" +
                "  \"address\": \"Nha A ngo~ B\",\n" +
                "  \"password\": \"123456\",\n" +
                "  \"email\": \"dangngandong2603@gmail.com\",\n" +
                "  \"retype_password\": \"123456\",\n" +
                "  \"date_of_birth\": \"2010-05-13\",\n" +
                "  \"role_id\": 1,\n" +
                "  \"auth_provider\": \"LOCAL\",\n" +
                "  \"isAccepted\": true\n" +
                "}";
        var resultActions = mockMvc.perform(post("/api/v1/users/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody)
                .header("User-Agent", "mobile")
                .header("Accept-Language", "vi"));

        resultActions.andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.message").value("Bạn phải đủ 18 tuổi để đăng ký."))
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.data").isEmpty());
    }
    @Test
    @DisplayName("Đăng kí không thành công vì mật khẩu ít hơn 6 kí tự")
    public void testRegister_006() throws Exception {
        String requestBody = "{\n" +
                "  \"fullname\": \"Dang Ngan Dong\",\n" +
                "  \"phone_number\": \"0258963155\",\n" +
                "  \"address\": \"Nha A ngo~ B\",\n" +
                "  \"password\": \"12345\",\n" +
                "  \"email\": \"dangngandong2603@gmail.com\",\n" +
                "  \"retype_password\": \"123456\",\n" +
                "  \"date_of_birth\": \"2005-05-13\",\n" +
                "  \"role_id\": 1,\n" +
                "  \"auth_provider\": \"LOCAL\",\n" +
                "  \"isAccepted\": true\n" +
                "}";
        var resultActions = mockMvc.perform(post("/api/v1/users/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody)
                .header("User-Agent", "mobile")
                .header("Accept-Language", "vi"));

        resultActions.andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.message").value("Mật khẩu phải có độ dài từ 6 đến 50 kí tự"))
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.data").isEmpty());
    }

    @Test
    @DisplayName("Đăng kí không thành công vì mật khẩu dài hơn 50 kí tự")
    public void testRegister_007() throws Exception {
        String requestBody = "{\n" +
                "  \"fullname\": \"Dang Ngan Dong\",\n" +
                "  \"phone_number\": \"0258963155\",\n" +
                "  \"address\": \"Nha A ngo~ B\",\n" +
                "  \"password\": \"11111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111\",\n" +
                "  \"email\": \"dangngandong2603@gmail.com\",\n" +
                "  \"retype_password\": \"123456\",\n" +
                "  \"date_of_birth\": \"2005-05-13\",\n" +
                "  \"role_id\": 1,\n" +
                "  \"auth_provider\": \"LOCAL\",\n" +
                "  \"isAccepted\": true\n" +
                "}";
        var resultActions = mockMvc.perform(post("/api/v1/users/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody)
                .header("User-Agent", "mobile")
                .header("Accept-Language", "vi"));

        resultActions.andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.message").value("Mật khẩu phải có độ dài từ 6 đến 50 kí tự"))
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.data").isEmpty());
    }

    @Test
    @DisplayName("Đăng ký không thành công vì mật khẩu nhập lại không trùng khớp")
    public void testRegister_008() throws Exception {
        String requestBody = "{\n" +
                "  \"fullname\": \"Dang Ngan Dong\",\n" +
                "  \"phone_number\": \"0258963155\",\n" +
                "  \"address\": \"Nha A ngo~ B\",\n" +
                "  \"password\": \"123457\",\n" +
                "  \"email\": \"dangngandong2603@gmail.com\",\n" +
                "  \"retype_password\": \"123456\",\n" +
                "  \"date_of_birth\": \"2005-05-13\",\n" +
                "  \"role_id\": 1,\n" +
                "  \"auth_provider\": \"LOCAL\",\n" +
                "  \"isAccepted\": true\n" +
                "}";
        var resultActions = mockMvc.perform(post("/api/v1/users/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody)
                .header("User-Agent", "mobile")
                .header("Accept-Language", "vi"));

        resultActions.andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.message").value("Mật khẩu nhập lại không chính xác"))
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.data").isEmpty());
    }

    @Test
    @DisplayName("Đăng ký không thành công vì điều khoản chưa được chấp nhận")
    public void testRegister_009() throws Exception {
        String requestBody = "{\n" +
                "  \"fullname\": \"Dang Ngan Dong\",\n" +
                "  \"phone_number\": \"0258963155\",\n" +
                "  \"address\": \"Nha A ngo~ B\",\n" +
                "  \"password\": \"123456\",\n" +
                "  \"email\": \"dangngandong2603@gmail.com\",\n" +
                "  \"retype_password\": \"123456\",\n" +
                "  \"date_of_birth\": \"2005-05-13\",\n" +
                "  \"role_id\": 1,\n" +
                "  \"auth_provider\": \"LOCAL\",\n" +
                "  \"isAccepted\": false\n" +
                "}";
        var resultActions = mockMvc.perform(post("/api/v1/users/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody)
                .header("User-Agent", "mobile")
                .header("Accept-Language", "vi"));

        resultActions.andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.message").value("Bạn phải đồng ý với Điều khoản & Điều kiện để đăng ký."))
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.data").isEmpty());
    }

    @Test
    @DisplayName("Đăng ký không thành công vì họ và tên không hợp lệ")
    public void testRegister_010() throws Exception {
        String requestBody = "{\n" +
                "  \"fullname\": \"123\",\n" +
                "  \"phone_number\": \"0258963155\",\n" +
                "  \"address\": \"Nha A ngo~ B\",\n" +
                "  \"password\": \"123456\",\n" +
                "  \"email\": \"dangngandong2603@gmail.com\",\n" +
                "  \"retype_password\": \"123456\",\n" +
                "  \"date_of_birth\": \"2005-05-13\",\n" +
                "  \"role_id\": 1,\n" +
                "  \"auth_provider\": \"LOCAL\",\n" +
                "  \"isAccepted\": false\n" +
                "}";
        var resultActions = mockMvc.perform(post("/api/v1/users/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody)
                .header("User-Agent", "mobile")
                .header("Accept-Language", "vi"));

        resultActions.andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.message").value("Họ và tên không hợp lệ."))
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.data").isEmpty());
    }


    @Test
    @DisplayName("Đăng ký không thành công vì số điện thoại ngắn hơn 10 kí tự")
    public void testRegister_011() throws Exception {
        String requestBody = "{\n" +
                "  \"fullname\": \"Van Sinl\",\n" +
                "  \"phone_number\": \"02589\",\n" +
                "  \"address\": \"Nha A ngo~ B\",\n" +
                "  \"password\": \"123456\",\n" +
                "  \"email\": \"dangngandong2603@gmail.com\",\n" +
                "  \"retype_password\": \"123456\",\n" +
                "  \"date_of_birth\": \"2005-05-13\",\n" +
                "  \"role_id\": 1,\n" +
                "  \"auth_provider\": \"LOCAL\",\n" +
                "  \"isAccepted\": true\n" +
                "}";
        var resultActions = mockMvc.perform(post("/api/v1/users/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody)
                .header("User-Agent", "mobile")
                .header("Accept-Language", "vi"));

        resultActions.andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.message").value("Số điện thoại không hợp lệ."))
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.data").isEmpty());
    }

    @Test
    @DisplayName("Đăng ký không thành công vì email không hợp lệ")
    public void testRegister_012() throws Exception {
        String requestBody = "{\n" +
                "  \"fullname\": \"Van Sinl\",\n" +
                "  \"phone_number\": \"0779808678\",\n" +
                "  \"address\": \"Nha A ngo~ B\",\n" +
                "  \"password\": \"123456\",\n" +
                "  \"email\": \"dangngandong2603@\",\n" +
                "  \"retype_password\": \"123456\",\n" +
                "  \"date_of_birth\": \"2005-05-13\",\n" +
                "  \"role_id\": 1,\n" +
                "  \"auth_provider\": \"LOCAL\",\n" +
                "  \"isAccepted\": true\n" +
                "}";
        var resultActions = mockMvc.perform(post("/api/v1/users/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody)
                .header("User-Agent", "mobile")
                .header("Accept-Language", "vi"));

        resultActions.andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.message").value("Email không hợp lệ."))
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.data").isEmpty());
    }

    @Test
    @DisplayName("Đăng ký không thành công vì email đã đăng kí trong hệ thống")
    public void testRegister_013() throws Exception {
        String requestBody = "{\n" +
                "  \"fullname\": \"Van Sinl\",\n" +
                "  \"phone_number\": \"0779808678\",\n" +
                "  \"address\": \"Nha A ngo~ B\",\n" +
                "  \"password\": \"123456\",\n" +
                "  \"email\": \"mando150903@gmail.com\",\n" +
                "  \"retype_password\": \"123456\",\n" +
                "  \"date_of_birth\": \"2005-05-13\",\n" +
                "  \"role_id\": 1,\n" +
                "  \"auth_provider\": \"LOCAL\",\n" +
                "  \"isAccepted\": true\n" +
                "}";
        var resultActions = mockMvc.perform(post("/api/v1/users/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody)
                .header("User-Agent", "mobile")
                .header("Accept-Language", "vi"));

        resultActions.andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.message").value("Email đã được đăng kí."))
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.data").isEmpty());
    }

    @Test
    @DisplayName("Đăng ký không thành công vì số điện thoại đã đăng kí trong hệ thống")
    public void testRegister_014() throws Exception {
        String requestBody = "{\n" +
                "  \"fullname\": \"Van Sinl\",\n" +
                "  \"phone_number\": \"0123456789\",\n" +
                "  \"address\": \"Nha A ngo~ B\",\n" +
                "  \"password\": \"123456\",\n" +
                "  \"email\": \"dangngandong2603@gmail.com\",\n" +
                "  \"retype_password\": \"123456\",\n" +
                "  \"date_of_birth\": \"2005-05-13\",\n" +
                "  \"role_id\": 1,\n" +
                "  \"auth_provider\": \"LOCAL\",\n" +
                "  \"isAccepted\": true\n" +
                "}";
        var resultActions = mockMvc.perform(post("/api/v1/users/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody)
                .header("User-Agent", "mobile")
                .header("Accept-Language", "vi"));

        resultActions.andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.message").value("Số điện thoại đã được đăng kí."))
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.data").isEmpty());
    }
}
