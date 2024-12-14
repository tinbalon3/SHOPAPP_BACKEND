package com.project.shopapp.controller;

import com.project.shopapp.dto.RefreshTokenDTO;
import com.project.shopapp.exceptions.DataNotFoundException;
import com.project.shopapp.models.Token;
import com.project.shopapp.models.User;
import com.project.shopapp.response.ResponseObject;
import com.project.shopapp.response.user.LoginResponse;
import com.project.shopapp.service.ITokenService;
import com.project.shopapp.service.IUserService;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("${api.prefix}/token")
@RequiredArgsConstructor
public class TokenController {

    private final ITokenService tokenService;
    private final IUserService userService;
    @GetMapping("/refreshExpirationDate/{refreshToken}")
    public ResponseEntity<?> getRefreshExpirationDate(@PathVariable String refreshToken){
            return ResponseEntity.ok(tokenService.getRefreshExpirationDate(refreshToken));

    }


    @PostMapping("/refreshToken")
    public ResponseEntity<ResponseObject> refreshToken(@RequestBody RefreshTokenDTO refreshTokenDTO) throws Exception {

        User userDetail = userService.getUserDetailsFromRefreshToken(refreshTokenDTO.getRefreshToken());
        Token jwtToken = tokenService.refreshToken(refreshTokenDTO.getRefreshToken(),userDetail);
        LoginResponse loginResponse = LoginResponse.builder()
                .message("Refresh token successfully")
                .token(jwtToken.getToken())
                .tokenType(jwtToken.getTokenType())
                .refreshToken(jwtToken.getRefreshToken())
                .refreshTokenExpired(jwtToken.getRefreshExpirationDate())
                .userName(userDetail.getUsername())
                .roles(userDetail.getAuthorities().stream().map(item -> item.getAuthority()).toList())
                .id(userDetail.getId())
                .build();
        return ResponseEntity.ok(ResponseObject.builder()
                .data(loginResponse)
                .message("Refresh token thành công")
                .status(HttpStatus.OK.value())
                .build());


    }

}
