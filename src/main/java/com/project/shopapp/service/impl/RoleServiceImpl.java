package com.project.shopapp.service.impl;

import com.project.shopapp.models.Role;
import com.project.shopapp.repositories.RoleRepository;
import com.project.shopapp.service.IRoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
@Service
@RequiredArgsConstructor
public class RoleServiceImpl implements IRoleService {
    private final RoleRepository roleRepository;
    public List<Role> getAllRoles(){
        return roleRepository.findAll();
    }
}
