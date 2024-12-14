package com.project.shopapp.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Data
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ProductRequest {
    @NotBlank(message = "Tên sản phẩm không được để trống")
    @Size(min=3,max = 200,message = "Tên sản phẩm phải từ 3 đến 200 kí tự")
    private String name;

    @Min(value=0,message = "Giá phải lớn hơn hoặc bằng 0")
    @Max(value=1000000,message = "Giá phải nhỏ hơn hoặc bằng 10.000.000")
    private float price;
    @Min(value=0,message = "Số lượng sản phẩm phải lớn hơn hoặc bằng 0")
    private int stock;
    private String description;
    @JsonProperty("category_id")
    private long categoryId;
}
