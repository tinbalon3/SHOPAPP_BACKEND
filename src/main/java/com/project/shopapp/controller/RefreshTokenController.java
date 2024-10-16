package com.project.shopapp.controller;

import com.project.shopapp.dto.RefreshTokenDTO;
import com.project.shopapp.service.ITokenService;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("${api.prefix}/refreshToken")
@RequiredArgsConstructor
public class RefreshTokenController {

    private final ITokenService iTokenService;
    @GetMapping("/refreshExpirationDate/{refreshToken}")
    public ResponseEntity<?> getRefreshExpirationDate(@PathVariable String refreshToken){
            return ResponseEntity.ok(iTokenService.getRefreshExpirationDate(refreshToken));

    }

}
