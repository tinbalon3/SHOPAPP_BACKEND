package com.project.shopapp.IntegrationTest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.shopapp.dto.UserLoginDTO;
import com.project.shopapp.models.Provider;
import com.project.shopapp.models.Role;
import com.project.shopapp.models.User;
import com.project.shopapp.repositories.RoleRepository;
import com.project.shopapp.repositories.TokenRepository;
import com.project.shopapp.repositories.UserRepository;
import io.qameta.allure.AllureId;
import io.qameta.allure.Step;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.util.Optional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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
public class UserLoginIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private TokenRepository tokenRepository;
    @BeforeAll
    void setupRoles() {
        Role roleUser = new Role();
        roleUser.setId(1L);
        roleUser.setName("USER");
        roleRepository.save(roleUser);

        Role roleAdmin = new Role();
        roleAdmin.setId(2L);
        roleAdmin.setName("ADMIN");
        roleRepository.save(roleAdmin);
    }

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

        User admin = new User();
        admin.setFullName("admintest@dev.vna.com");
        admin.setPhoneNumber("");
        admin.setEmail("admintest@dev.vna.com");
        admin.setActive(true);
        admin.setEnabled(true);
        admin.setProvider(Provider.LOCAL);
        Optional<Role> role_2 = roleRepository.findById(2L);
        admin.setPassword(new BCryptPasswordEncoder().encode("admin")); // Mã hóa password
        admin.setRole(role_2.get());
        userRepository.save(admin);

        User userBlocked = new User();
        userBlocked.setFullName("userBlocked");
        userBlocked.setPhoneNumber("0111111111");
        userBlocked.setEmail("userblocked@gmail.com");
        userBlocked.setActive(true);
        userBlocked.setEnabled(false);
        userBlocked.setProvider(Provider.LOCAL);
        userBlocked.setPassword(new BCryptPasswordEncoder().encode("123456")); // Mã hóa password
        userBlocked.setRole(role.get());
        userRepository.save(userBlocked);
    }

    @AfterAll
    void cleanupDatabase() {
        userRepository.deleteAll();
        tokenRepository.deleteAll();
    }


    @Step("Kiểm tra kết quả login với email {0}")
    private void assertLogin(String email, ResultActions resultActions) throws Exception {
        resultActions.andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Đăng nhập thành công"))
                .andExpect(jsonPath("$.data.token").exists())
                .andExpect(jsonPath("$.data.user_name").value(email));
    }

    @Test
    @AllureId("101")
    @DisplayName("Người dùng đăng nhập thành công")
    void login_001() throws Exception {
        UserLoginDTO loginDTO = new UserLoginDTO();
        loginDTO.setUserName("mando150903@gmail.com");
        loginDTO.setPassword("123456");


        var resultActions = mockMvc.perform(post("/api/v1/users/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginDTO))
                .header("User-Agent", "mobile"));

        assertLogin("mando150903@gmail.com", resultActions);
    }

    @Test
    @AllureId("102")
    @DisplayName("Quản trị viên đăng nhập thành công")
    void login_002() throws Exception {
        UserLoginDTO loginDTO = new UserLoginDTO();
        loginDTO.setUserName("admintest@dev.vna.com");
        loginDTO.setPassword("admin");


        var resultActions = mockMvc.perform(post("/api/v1/users/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginDTO))
                .header("User-Agent", "mobile"));

        assertLogin("admintest@dev.vna.com", resultActions);

    }




    @Test
    @AllureId("103")
    @DisplayName("Kiểm tra đăng nhập với tài khoản bị khóa")
    void login_003() throws Exception {
        UserLoginDTO loginDTO = new UserLoginDTO();
        loginDTO.setUserName("userblocked@gmail.com");
        loginDTO.setPassword("123456");


        var resultActions = mockMvc.perform(post("/api/v1/users/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginDTO))
                .header("User-Agent", "mobile")
                .header("Accept-Language", "vi"));

        resultActions.andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.message").value("Tài khoản của bạn đã bị khóa"))
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.data").isEmpty());
    }

    @Test
    @AllureId("104")
    @DisplayName("Kiểm tra đăng nhập với mật khẩu không đúng")
    void login_004() throws Exception {
        UserLoginDTO loginDTO = new UserLoginDTO();
        loginDTO.setUserName("mando150903@gmail.com");
        loginDTO.setPassword("1234569");


        var resultActions = mockMvc.perform(post("/api/v1/users/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginDTO))
                .header("User-Agent", "mobile")
                .header("Accept-Language", "vi"));

        resultActions.andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.message").value("Số điện thoại hoặc mật khẩu không chính xác"))
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.data").isEmpty());
    }

    @Test
    @AllureId("105")
    @DisplayName("Kiểm tra đăng nhập với tên đăng nhập không đúng")
    void login_005() throws Exception {
        UserLoginDTO loginDTO = new UserLoginDTO();
        loginDTO.setUserName("mando150903");
        loginDTO.setPassword("123456");


        var resultActions = mockMvc.perform(post("/api/v1/users/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginDTO))
                .header("User-Agent", "mobile")
                .header("Accept-Language", "vi"));

        resultActions.andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.message").value("Tên đăng nhập hoặc mật khẩu không kết nối đến tài khoản nào. Tìm tài khoản của bạn và đăng nhập."))
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.data").isEmpty());
    }

    @Test
    @AllureId("106")
    @DisplayName("Kiểm tra đăng nhập với tên đăng nhập và mật khẩu trống")
    void login_006() throws Exception {
        UserLoginDTO loginDTO = new UserLoginDTO();
        loginDTO.setUserName("");
        loginDTO.setPassword("");


        var resultActions = mockMvc.perform(post("/api/v1/users/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginDTO))
                .header("User-Agent", "mobile")
                .header("Accept-Language", "vi"));

        resultActions.andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.message").value("Tên đăng nhập hoặc mật khẩu không kết nối đến tài khoản nào. Tìm tài khoản của bạn và đăng nhập."))
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.data").isEmpty());
    }


}