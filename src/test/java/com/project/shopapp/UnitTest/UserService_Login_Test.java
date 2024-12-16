package com.project.shopapp.UnitTest;

import com.project.shopapp.UnitTest.UserServiceTestConfig.UserServiceTestConfig;
import com.project.shopapp.components.JwtTokenUtils;
import com.project.shopapp.components.LocalizationUtils;
import com.project.shopapp.dto.UserLoginDTO;
import com.project.shopapp.exceptions.DataNotFoundException;
import com.project.shopapp.exceptions.EmailNotRegisterException;
import com.project.shopapp.exceptions.InvalidPasswordException;
import com.project.shopapp.exceptions.UserErrorException;
import com.project.shopapp.models.Provider;
import com.project.shopapp.models.Role;
import com.project.shopapp.models.Token;
import com.project.shopapp.models.User;
import com.project.shopapp.repositories.RoleRepository;
import com.project.shopapp.repositories.UserRepository;
import com.project.shopapp.service.ITokenService;
import com.project.shopapp.service.impl.UserServiceImpl;
import com.project.shopapp.untils.MessageKeys;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import java.util.Optional;

import static org.hibernate.validator.internal.util.Contracts.assertNotNull;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@AutoConfigureMockMvc
@ContextConfiguration(classes = {UserServiceTestConfig.class})
@ActiveProfiles("test")
@TestPropertySource(properties = {
        "spring.flyway.enabled=false",
        "spring.datasource.url=jdbc:h2:mem:testdb",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class UserService_Login_Test {

    @InjectMocks
    private UserServiceImpl userService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private JwtTokenUtils jwtTokenUtils;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private LocalizationUtils localizationUtils;

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @Mock
    private HashOperations<String, String, Object> hashOperations;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private ITokenService tokenService;
    @Test
    @DisplayName("Người dùng đăng nhập thành công")
    void login_001() throws Exception {
        // Arrange
        UserLoginDTO userLoginDTO = new UserLoginDTO();
        userLoginDTO.setUserName("mando150903@gmail.com");
        userLoginDTO.setPassword("123456");

        // Mock dữ liệu Role
        Role role = new Role();
        role.setId(1L);
        role.setName("USER");

        // Mock dữ liệu người dùng
        User user = new User();
        user.setFullName("Man Do");
        user.setPhoneNumber("0123456789");
        user.setEmail("mando150903@gmail.com");
        user.setActive(true);
        user.setEnabled(true);
        user.setProvider(Provider.LOCAL);
        user.setPassword("123456"); // Mã hóa password
        user.setRole(role);

        // Khi tìm email của người dùng, trả về dữ liệu đã mock
        when(userRepository.findByEmail("mando150903@gmail.com")).thenReturn(Optional.of(user));

        // Mock hành vi khi mã hóa mật khẩu
        when(passwordEncoder.matches("123456", user.getPassword())).thenReturn(true);

        // Mock hành vi tạo token JWT
        when(jwtTokenUtils.generateToken(user)).thenReturn("mockToken");

        // Mock hành vi của extractEmail
        when(jwtTokenUtils.extractEmail("mockToken")).thenReturn("mando150903@gmail.com");

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(null); // Hoặc trả về đối tượng Authentication giả nếu cần

        // Mock hành vi của addToken
        Token mockToken = new Token();
        mockToken.setToken("mockToken"); // Giả sử class Token có phương thức setToken()
        when(tokenService.addToken(any(User.class), anyString(), anyBoolean())).thenReturn(mockToken);

        // Act
        String token = userService.login(userLoginDTO);

        User userDetail = userService.getUserDetails(token);  // Gọi phương thức getUserDetails

        String userAgent = "mobile";

        Token jwtToken = tokenService.addToken(userDetail, token, isMobileDevice(userAgent));

        // Assert
        assertNotNull(token);
        assertEquals("mockToken", token);
        assertNotNull(jwtToken);  // Kiểm tra jwtToken không phải null
        assertEquals("mockToken", jwtToken.getToken());  // Kiểm tra token trong jwtToken là "mockToken"

        // Verify các phương thức đã được gọi đúng
        verify(userRepository, times(2)).findByEmail("mando150903@gmail.com");
        verify(jwtTokenUtils).generateToken(user);
        verify(passwordEncoder).matches("123456", user.getPassword()); // Kiểm tra password có được so sánh đúng không
        verify(tokenService).addToken(any(User.class), anyString(), anyBoolean());  // Kiểm tra addToken đã được gọi đúng
    }
    @Test
    @DisplayName("Quản trị viên đăng nhập thành công")
    void login_admin_Success() throws Exception {
        // Arrange
        UserLoginDTO userLoginDTO = new UserLoginDTO();
        userLoginDTO.setUserName("admin@dev.vna.com");
        userLoginDTO.setPassword("admin");

        // Mock dữ liệu Role
        Role role = new Role();
        role.setId(2L);
        role.setName("ADMIN");

        // Mock dữ liệu người dùng
        User user = new User();
        user.setFullName("ADMIN");
        user.setPhoneNumber("0123456789");
        user.setEmail("admin@dev.vna.com");
        user.setActive(true);
        user.setEnabled(true);
        user.setProvider(Provider.LOCAL);
        user.setPassword("admin"); // Mã hóa password
        user.setRole(role);

        // Khi tìm email của người dùng, trả về dữ liệu đã mock
        when(userRepository.findByEmail("admin@dev.vna.com")).thenReturn(Optional.of(user));

        // Mock hành vi khi mã hóa mật khẩu
        when(passwordEncoder.matches("admin", user.getPassword())).thenReturn(true);

        // Mock hành vi tạo token JWT
        when(jwtTokenUtils.generateToken(user)).thenReturn("mockToken");

        // Mock hành vi của extractEmail
        when(jwtTokenUtils.extractEmail("mockToken")).thenReturn("admin@dev.vna.com");

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(null); // Hoặc trả về đối tượng Authentication giả nếu cần

        // Mock hành vi của addToken
        Token mockToken = new Token();
        mockToken.setToken("mockToken"); // Giả sử class Token có phương thức setToken()
        when(tokenService.addToken(any(User.class), anyString(), anyBoolean())).thenReturn(mockToken);

        // Act
        String token = userService.login(userLoginDTO);

        User userDetail = userService.getUserDetails(token);  // Gọi phương thức getUserDetails

        String userAgent = "mobile";

        Token jwtToken = tokenService.addToken(userDetail, token, isMobileDevice(userAgent));

        // Assert
        assertNotNull(token);
        assertEquals("mockToken", token);
        assertNotNull(jwtToken);  // Kiểm tra jwtToken không phải null
        assertEquals("mockToken", jwtToken.getToken());  // Kiểm tra token trong jwtToken là "mockToken"

        // Verify các phương thức đã được gọi đúng
        verify(userRepository, times(2)).findByEmail("admin@dev.vna.com");
        verify(jwtTokenUtils).generateToken(user);
        verify(passwordEncoder).matches("admin", user.getPassword()); // Kiểm tra password có được so sánh đúng không
        verify(tokenService).addToken(any(User.class), anyString(), anyBoolean());  // Kiểm tra addToken đã được gọi đúng
    }
    private boolean isMobileDevice(String userAgent){
        //Kiểm tra User-Agent header để xác định thiết bị di động
        //Ví dụ đơn giản:
        return userAgent.toLowerCase().contains("mobile");
    }
    @Test
    @DisplayName("Người dùng đăng nhập thất bại với mật khẩu sai")
    void login_002() throws Exception {
        // Arrange
        UserLoginDTO userLoginDTO = new UserLoginDTO();
        userLoginDTO.setUserName("mando150903@gmail.com");
        userLoginDTO.setPassword("wrongpassword"); // Sử dụng mật khẩu sai

        // Mock dữ liệu Role
        Role role = new Role();
        role.setId(1L);
        role.setName("USER");

        // Mock dữ liệu người dùng
        User user = new User();
        user.setFullName("Man Do");
        user.setPhoneNumber("0123456789");
        user.setEmail("mando150903@gmail.com");
        user.setActive(true);
        user.setEnabled(true);
        user.setProvider(Provider.LOCAL);
        user.setPassword("123456"); // Mật khẩu chính xác trong hệ thống
        user.setRole(role);

        // Khi tìm email của người dùng, trả về dữ liệu đã mock
        when(userRepository.findByEmail("mando150903@gmail.com")).thenReturn(Optional.of(user));

        // Mock hành vi khi mã hóa mật khẩu (so sánh mật khẩu sai)
        when(passwordEncoder.matches("wrongpassword", user.getPassword())).thenReturn(false);

        // Act & Assert: Kiểm tra nếu sai mật khẩu sẽ ném ra exception
        assertThrows(UserErrorException.class, () -> {
            userService.login(userLoginDTO);
        });

        // Verify các phương thức đã được gọi đúng
        verify(userRepository).findByEmail("mando150903@gmail.com");
        verify(passwordEncoder).matches("wrongpassword", user.getPassword()); // Kiểm tra password có được so sánh đúng không
    }

    @Test
    @DisplayName("Người dùng đăng nhập thất bại với tên người dùng không tồn tại")
    void login_003()  {
        // Arrange
        UserLoginDTO userLoginDTO = new UserLoginDTO();
        userLoginDTO.setUserName("nonexistentuser@gmail.com"); // Email không tồn tại
        userLoginDTO.setPassword("anyPassword");

        // Khi tìm email của người dùng, trả về Optional.empty() để giả lập trường hợp không tồn tại người dùng
        when(userRepository.findByEmail("nonexistentuser@gmail.com")).thenReturn(Optional.empty());

        when(localizationUtils.getLocalizeMessage(MessageKeys.LOGIN_NULL_DATA))
                .thenReturn("Tên đăng nhập hoặc mật khẩu không kết nối đến tài khoản nào. Tìm tài khoản của bạn và đăng nhập.");
        // Act & Assert: Kiểm tra nếu không tìm thấy người dùng sẽ ném ra exception
        UserErrorException thrown = assertThrows(UserErrorException.class, () -> {
            userService.login(userLoginDTO); // Nên ném ra exception vì không tìm thấy người dùng
        });
        assertEquals("Tên đăng nhập hoặc mật khẩu không kết nối đến tài khoản nào. Tìm tài khoản của bạn và đăng nhập.", thrown.getMessage()); // Kiểm tra thông điệp lỗi
        // Verify: Kiểm tra phương thức `findByEmail` có được gọi đúng với email không tồn tại
        verify(userRepository).findByEmail("nonexistentuser@gmail.com");
    }
    @Test
    @DisplayName("Người dùng đăng nhập thất bại do tài khoản bị khóa")
    void login_004() throws UserErrorException, EmailNotRegisterException, DataNotFoundException, InvalidPasswordException {
        // Arrange
        UserLoginDTO userLoginDTO = new UserLoginDTO();
        userLoginDTO.setUserName("mando150903@gmail.com");
        userLoginDTO.setPassword("123456");

        // Mock dữ liệu Role
        Role role = new Role();
        role.setId(1L);
        role.setName("USER");

        // Mock dữ liệu người dùng
        User user = new User();
        user.setFullName("Man Do");
        user.setPhoneNumber("0123456789");
        user.setEmail("mando150903@gmail.com");
        user.setActive(true);
        user.setEnabled(true);
        user.setProvider(Provider.LOCAL);
        user.setPassword("123456"); // Mật khẩu chính xác trong hệ thống
        user.setRole(role);
        user.setEnabled(false);  // Đặt trạng thái là bị khóa

        // Khi tìm email của người dùng, trả về dữ liệu đã mock
        when(userRepository.findByEmail("mando150903@gmail.com")).thenReturn(Optional.of(user));

        // Act & Assert: Kiểm tra nếu người dùng bị khóa sẽ ném ra exception
        UserErrorException thrown = assertThrows(UserErrorException.class, () -> {
            userService.login(userLoginDTO); // Nên ném ra exception vì tài khoản bị khóa
        });



        // Verify các phương thức đã được gọi đúng
        verify(userRepository).findByEmail("mando150903@gmail.com");

    }
    @Test
    @DisplayName("Lấy thông tin chi tiết người dùng không tồn tại")
    void login_005() throws UserErrorException, EmailNotRegisterException, DataNotFoundException, InvalidPasswordException {
        // Arrange
        UserLoginDTO userLoginDTO = new UserLoginDTO();
        userLoginDTO.setUserName("mando150903@gmail.com");
        userLoginDTO.setPassword("123456");

        // Mock dữ liệu Role
        Role role = new Role();
        role.setId(1L);
        role.setName("USER");

        // Mock dữ liệu người dùng
        User user = new User();
        user.setFullName("Man Do");
        user.setPhoneNumber("0123456789");
        user.setEmail("mando150903@gmail.com");
        user.setActive(true);
        user.setEnabled(true);
        user.setProvider(Provider.LOCAL);
        user.setPassword("123456"); // Mã hóa password
        user.setRole(role);

        // Khi tìm email của người dùng, trả về dữ liệu đã mock
        when(userRepository.findByEmail("mando150903@gmail.com")).thenReturn(Optional.of(user));

        // Mock hành vi khi mã hóa mật khẩu
        when(passwordEncoder.matches("123456", user.getPassword())).thenReturn(true);

        // Mock hành vi tạo token JWT
        when(jwtTokenUtils.generateToken(user)).thenReturn("mockToken");

        // Mock hành vi của extractEmail
        when(jwtTokenUtils.extractEmail("mockToken")).thenReturn("mando150903@gmail.com");

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(null); // Hoặc trả về đối tượng Authentication giả nếu cần

        // Mock hành vi của addToken
        // Mock hành vi của addToken
        Token mockToken = new Token();
        mockToken.setToken("mockToken"); // Giả sử class Token có phương thức setToken()


        // Act
        String token = userService.login(userLoginDTO);
        when(jwtTokenUtils.extractEmail(token)).thenReturn("khongtontai@gmail.com");
        assertThrows(DataNotFoundException.class, () -> {
            userService.getUserDetails(token);
        });


    }


}

