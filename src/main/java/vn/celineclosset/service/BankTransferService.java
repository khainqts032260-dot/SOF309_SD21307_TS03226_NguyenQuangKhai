package vn.celineclosset.service;

import vn.celineclosset.util.AppConfig;

import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Locale;

/** Tạo VietQR Quick Link có sẵn tài khoản, số tiền và mã đơn. */
public class BankTransferService {
    public String paymentCode(int orderId) {
        String prefix = AppConfig.get("payment.codePrefix", "DH").replaceAll("[^A-Za-z]", "");
        if (prefix.isBlank()) prefix = "DH";
        int digits = Math.max(1, Math.min(10, AppConfig.getInt("payment.codeDigits", 5)));
        String numericPart = Integer.toString(Math.max(0, orderId));
        if (numericPart.length() < digits) {
            numericPart = "0".repeat(digits - numericPart.length()) + numericPart;
        }
        return prefix.toUpperCase(Locale.ROOT) + numericPart;
    }

    public boolean isBankConfigured() {
        return !AppConfig.get("shop.bankCode", "970423").isBlank()
                && !normalizedAccount().isBlank()
                && !AppConfig.get("shop.bankOwner").isBlank();
    }

    public String qrImageUrl(BigDecimal amount, String paymentCode) {
        if (!isBankConfigured() || amount == null || amount.signum() <= 0) return "";
        String base = AppConfig.get("vietqr.quickLinkBase", "https://img.vietqr.io/image");
        String bankCode = AppConfig.get("shop.bankCode", "970423").replaceAll("[^A-Za-z0-9]", "");
        String template = AppConfig.get("vietqr.template", "compact2").replaceAll("[^A-Za-z0-9_-]", "");
        String amountText = amount.setScale(0, java.math.RoundingMode.HALF_UP).toPlainString();
        return base + "/" + bankCode + "-" + normalizedAccount() + "-" + template + ".png"
                + "?amount=" + encode(amountText)
                + "&addInfo=" + encode(paymentCode)
                + "&accountName=" + encode(AppConfig.get("shop.bankOwner"));
    }

    private String normalizedAccount() {
        return AppConfig.get("shop.bankAccount").replaceAll("[^A-Za-z0-9]", "");
    }

    private String encode(String value) {
        return URLEncoder.encode(value == null ? "" : value, StandardCharsets.UTF_8);
    }
}
