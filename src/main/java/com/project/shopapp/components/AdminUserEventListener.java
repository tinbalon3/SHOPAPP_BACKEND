package com.project.shopapp.components;

import com.project.shopapp.models.Role;
import com.project.shopapp.repositories.RoleRepository;
import com.project.shopapp.service.IUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AdminUserEventListener {

    private final IUserService userService;
    private final RoleRepository roleRepository;



    @EventListener(ContextRefreshedEvent.class)
    public void onApplicationEvent(ContextRefreshedEvent event) {
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

