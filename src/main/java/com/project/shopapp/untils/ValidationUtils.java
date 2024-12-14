package com.project.shopapp.untils;

import java.time.LocalDate;
import java.time.Period;
import java.util.Date;
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
    public static boolean isValidName(String name) {
        if (name == null || name.isEmpty()) {
            return false; // Trả về false nếu chuỗi rỗng hoặc null
        }

        // Biểu thức chính quy kiểm tra tên chỉ chứa chữ cái và khoảng trắng
        String nameRegex = "^[a-zA-ZàáảãạăắằẳẵặâấầẩẫậéèẻẽẹêếềểễệíìỉĩịóòỏõọôốồổỗộơớờởỡợúùủũụưứừửữựýỳỷỹỵđĐ\\s]+$";

        // Kiểm tra tên có hợp lệ không
        Pattern pattern = Pattern.compile(nameRegex);
        return pattern.matcher(name).matches(); // Trả về true nếu tên hợp lệ
    }
    // Hàm kiểm tra tuổi
    public static boolean isAdult(Date birthDate) {
        if(birthDate == null ){
            return false;
        }
        // Lấy ngày hiện tại
        Date today = new Date();

        // Kiểm tra sự khác biệt giữa ngày sinh và ngày hiện tại
        long difference = today.getTime() - birthDate.getTime();

        // Chuyển đổi từ mili giây sang năm
        long years = difference / (1000L * 60 * 60 * 24 * 365);

        // Kiểm tra nếu tuổi lớn hơn hoặc bằng 18
        return years >= 18;
    }
}
