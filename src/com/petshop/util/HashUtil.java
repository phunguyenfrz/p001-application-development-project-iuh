package com.petshop.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Tiện ích mã hóa bảo mật cho hệ thống Happy Tails.
 * Tuân thủ Điều 5 Bộ chỉ thị kỹ thuật IUH: Băm mật khẩu bằng thuật toán SHA-256.
 */
public final class HashUtil {

    private HashUtil() {
    }

    /**
     * Băm chuỗi đầu vào bằng thuật toán SHA-256 ra chuỗi Hex 64 ký tự.
     *
     * @param input Chuỗi văn bản thuần (mật khẩu)
     * @return Chuỗi băm SHA-256 dạng hex thường (64 ký tự), hoặc chuỗi rỗng nếu input null
     */
    public static String sha256(String input) {
        if (input == null) {
            return "";
        }
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] encodedHash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder(2 * encodedHash.length);
            for (byte b : encodedHash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("Hệ thống không hỗ trợ thuật toán SHA-256: " + e.getMessage(), e);
        }
    }
}
