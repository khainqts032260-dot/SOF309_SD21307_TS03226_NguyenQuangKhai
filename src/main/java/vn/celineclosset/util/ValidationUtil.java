package vn.celineclosset.util;

/** Các kiểm tra đầu vào cơ bản dùng chung cho tài khoản. */
public final class ValidationUtil {
    private static final String SPECIAL_CHARACTERS = "!@#$%^&*";

    private ValidationUtil() {
    }

    public static String normalizeEmail(String email) {
        return email == null ? "" : email.trim().toLowerCase();
    }

    public static boolean isValidEmail(String email) {
        String value = normalizeEmail(email);
        if (value.isEmpty() || value.contains(" ")) {
            return false;
        }

        int atPosition = value.indexOf('@');
        int lastAtPosition = value.lastIndexOf('@');
        int dotPosition = value.lastIndexOf('.');

        if (atPosition <= 0 || atPosition != lastAtPosition) {
            return false;
        }
        if (dotPosition < atPosition + 2 || dotPosition == value.length() - 1) {
            return false;
        }

        for (int i = 0; i < value.length(); i++) {
            char ch = value.charAt(i);
            if (Character.isLetterOrDigit(ch) || ch == '@' || ch == '.' || ch == '_'
                    || ch == '%' || ch == '+' || ch == '-') {
                continue;
            }
            return false;
        }
        return true;
    }

    public static boolean isValidVietnamPhone(String phone) {
        if (phone == null) return false;
        String value = phone.trim();
        return value.matches("0\\d{9}");
    }

    public static boolean isValidAddress(String address) {
        if (address == null) return false;
        String value = address.trim();
        return value.length() >= 10 && value.matches(".*[A-Za-zÀ-ỹ].*") && value.matches(".*\\d.*");
    }

    public static String phoneRuleMessage() {
        return "Số điện thoại phải gồm đúng 10 chữ số và bắt đầu bằng số 0.";
    }

    public static String addressRuleMessage() {
        return "Địa chỉ phải có ít nhất 10 ký tự, gồm số nhà và tên đường/khu vực.";
    }

    public static boolean isValidPassword(String password) {
        if (password == null || password.length() < 8 || password.length() > 32) {
            return false;
        }

        boolean hasLowercase = false;
        boolean hasUppercase = false;
        boolean hasNumber = false;
        boolean hasSpecialCharacter = false;

        for (int i = 0; i < password.length(); i++) {
            char ch = password.charAt(i);
            if (Character.isLowerCase(ch)) {
                hasLowercase = true;
            } else if (Character.isUpperCase(ch)) {
                hasUppercase = true;
            } else if (Character.isDigit(ch)) {
                hasNumber = true;
            } else if (SPECIAL_CHARACTERS.indexOf(ch) >= 0) {
                hasSpecialCharacter = true;
            } else {
                return false;
            }
        }

        return hasLowercase && hasUppercase && hasNumber && hasSpecialCharacter;
    }

    public static String passwordRuleMessage() {
        return "Mật khẩu phải từ 8-32 ký tự, có chữ hoa, chữ thường, số và ký tự đặc biệt !@#$%^&*.";
    }
}
