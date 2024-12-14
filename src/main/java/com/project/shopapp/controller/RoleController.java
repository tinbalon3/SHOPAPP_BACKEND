package com.project.shopapp.controller;

import com.project.shopapp.models.Role;
import com.project.shopapp.response.ResponseObject;
import com.project.shopapp.service.IRoleService;
import com.project.shopapp.service.impl.RoleServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("${api.prefix}/roles")
@RequiredArgsConstructor
public class RoleController {
    private final IRoleService roleService;

    @PreAuthorize("true")
    @GetMapping("")
    public ResponseEntity<ResponseObject> getAllRoles(){
        List<Role> roles = roleService.getAllRoles();
        return ResponseEntity.ok(ResponseObject.builder()
                        .message("Lấy danh sách role thành công")
                        .data(roles)
                        .status(HttpStatus.OK.value())
                .build());

    }
}
