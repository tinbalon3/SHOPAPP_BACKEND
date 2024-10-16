package com.project.shopapp.untils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ValidationUtils {
    public static boolean validateEmail(String email) {
        // Regex kiểm tra định dạng email
        String emailRegex = "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$";

        // Tạo pattern từ regex
        Pattern pattern = Pattern.compile(emailRegex);

        // Nếu email là null, trả về false
        if (email == null) {
            return false;
        }

        // Sử dụng matcher để kiểm tra tính hợp lệ của email
        Matcher matcher = pattern.matcher(email);
        return  matcher.matches();
    }
    public static boolean validatePhoneNumber(String phoneNumber) {
        // Regex kiểm tra số điện thoại bắt đầu bằng 0 và có 10 chữ số
        String phoneRegex = "^0\\d{9}$";
        Pattern pattern = Pattern.compile(phoneRegex);

        if (phoneNumber == null) {
            return false;
        }

        Matcher matcher = pattern.matcher(phoneNumber);
        return matcher.matches();
    }
}
