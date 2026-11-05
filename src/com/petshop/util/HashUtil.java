package com.petshop.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Tiện ích băm mật khẩu bảo mật chuẩn SHA-256 (Bộ chỉ thị kỹ thuật IUH - Điều 5).
 */
public final class HashUtil {

    private HashUtil() {
    }

    /**
     * Băm chuỗi văn bản bằng thuật toán SHA-256 và trả về chuỗi Hexadecimal viết thường.
     *
     * @param input Chuỗi văn bản cần băm (mật khẩu)
     * @return Chuỗi Hex 64 ký tự, hoặc rỗng nếu input null
     */
    public static String sha256(String input) {
        if (input == null) {
            return "";
        }
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] encodedhash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder(2 * encodedhash.length);
            for (byte b : encodedhash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Lỗi thuật toán mã hóa SHA-256: " + e.getMessage(), e);
        }
    }
}

