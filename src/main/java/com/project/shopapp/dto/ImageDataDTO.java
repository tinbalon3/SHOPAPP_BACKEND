package com.project.shopapp.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ImageDataDTO {
    private List<Long> imageIds;
    private List<Boolean> updateImages; // Đổi tên để khớp với JSON
}

