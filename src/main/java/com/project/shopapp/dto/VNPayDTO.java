package com.project.shopapp.dto;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
public class VNPayDTO implements Serializable {
    private Integer amount;
    private String reason;
}
