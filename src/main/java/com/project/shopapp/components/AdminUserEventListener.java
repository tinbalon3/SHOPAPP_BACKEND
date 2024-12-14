package com.project.shopapp.components;

import com.project.shopapp.models.Provider;
import com.project.shopapp.models.Role;
import com.project.shopapp.models.User;
import com.project.shopapp.repositories.RoleRepository;
import com.project.shopapp.repositories.UserRepository;
import com.project.shopapp.service.IUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class AdminUserEventListener {

    private final IUserService userService;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;



    @EventListener(ContextRefreshedEvent.class)
    public void onApplicationEvent(ContextRefreshedEvent event) {
//        Optional<Role> role = roleRepository.findById(1L);
//        User userBlocked = new User();
//        userBlocked.setFullName("userBlocked");
//        userBlocked.setPhoneNumber("0111111111");
//        userBlocked.setEmail("userblocked@gmail.com");
//        userBlocked.setActive(true);
//        userBlocked.setEnabled(false);
//        userBlocked.setProvider(Provider.LOCAL);
//        userBlocked.setPassword(new BCryptPasswordEncoder().encode("123456")); // Mã hóa password
//        userBlocked.setRole(role.get());
//        userRepository.save(userBlocked);

//                Optional<Role> role = roleRepository.findById(1L);
//        User userBlocked = new User();
//        userBlocked.setFullName("userCanAccess");
//        userBlocked.setPhoneNumber("0111111112");
//        userBlocked.setEmail("userenabled@gmail.com");
//        userBlocked.setActive(true);
//        userBlocked.setEnabled(false);
//        userBlocked.setProvider(Provider.LOCAL);
//        userBlocked.setPassword(new BCryptPasswordEncoder().encode("123456")); // Mã hóa password
//        userBlocked.setRole(role.get());
//        userRepository.save(userBlocked);
        // Kiểm tra và tạo admin user
        if (!userService.adminExists()) {

            Role adminRole = roleRepository.findByName("ADMIN")
                    .orElseGet(() -> roleRepository.save(new Role(Role.ADMIN)));
            userService.createAdmin("admin", "admin", adminRole);
            System.out.println("Admin user đã được tạo qua ContextRefreshedEvent.");
        } else {
            System.out.println("Admin user đã tồn tại qua ContextRefreshedEvent, không cần tạo thêm.");
        }
    }
}

