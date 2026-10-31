package com.petshop.service;

import com.petshop.util.DatabaseManager;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

/**
 * Dịch vụ quản lý phân quyền chức năng chi tiết cho từng nhân viên/tài khoản.
 * Lưu trữ đồng bộ vào CSDL (bảng PhanQuyenTaiKhoan) và bộ nhớ đệm tệp (data/phan_quyen.properties)
 * để đảm bảo quyền hạn được áp dụng chính xác ngay lập tức khi đăng nhập.
 */
public final class PhanQuyenService {

    private static final Path PATH_FILE = Paths.get("data", "phan_quyen.properties");
    private static final Map<String, Set<String>> BO_NHO_TAM = new HashMap<>();
    private static boolean daKhoiTaoBang = false;

    private PhanQuyenService() {}

    /**
     * Khởi tạo bảng PhanQuyenTaiKhoan trong CSDL nếu chưa tồn tại.
     */
    private static synchronized void damBaoBangTonTai() {
        if (daKhoiTaoBang) return;
        String sql = """
                IF NOT EXISTS (SELECT 1 FROM sysobjects WHERE name='PhanQuyenTaiKhoan' AND xtype='U')
                BEGIN
                    CREATE TABLE PhanQuyenTaiKhoan (
                        maNhanVien VARCHAR(20) PRIMARY KEY,
                        danhSachQuyen NVARCHAR(MAX),
                        ngayCapNhat DATETIME DEFAULT GETDATE()
                    );
                END
                """;
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.execute();
            daKhoiTaoBang = true;
        } catch (Exception ignored) {
            // Tiếp tục sử dụng file dự phòng nếu DB không cho phép tạo bảng
        }
    }

    /**
     * Lấy danh sách mã chức năng được cấp quyền cho nhân viên.
     * @param maNhanVien Mã nhân viên (VD: NV-20261030-000003)
     * @return Set mã chức năng được cấp quyền, hoặc null nếu nhân viên này chưa từng tùy chỉnh quyền riêng (dùng mặc định theo vai trò).
     */
    public static synchronized Set<String> layDanhSachQuyen(String maNhanVien) {
        if (maNhanVien == null || maNhanVien.isBlank()) return null;

        // 1. Kiểm tra trong bộ nhớ đệm
        if (BO_NHO_TAM.containsKey(maNhanVien)) {
            return BO_NHO_TAM.get(maNhanVien);
        }

        // 2. Kiểm tra trong CSDL
        damBaoBangTonTai();
        String sql = "SELECT danhSachQuyen FROM PhanQuyenTaiKhoan WHERE maNhanVien = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, maNhanVien.trim());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String chuoi = rs.getString("danhSachQuyen");
                    Set<String> set = giaiMaChuoiQuyen(chuoi);
                    BO_NHO_TAM.put(maNhanVien, set);
                    ghiFileDuPhong(maNhanVien, chuoi);
                    return set;
                }
            }
        } catch (Exception ignored) {}

        // 3. Kiểm tra trong tệp dự phòng cục bộ
        Properties props = docFileDuPhong();
        if (props.containsKey(maNhanVien)) {
            String chuoi = props.getProperty(maNhanVien);
            Set<String> set = giaiMaChuoiQuyen(chuoi);
            BO_NHO_TAM.put(maNhanVien, set);
            return set;
        }

        return null;
    }

    /**
     * Lưu danh sách mã chức năng được cấp quyền cho nhân viên.
     */
    public static synchronized void luuDanhSachQuyen(String maNhanVien, Set<String> dsQuyen) {
        if (maNhanVien == null || maNhanVien.isBlank()) return;

        Set<String> tapHop = (dsQuyen != null) ? new HashSet<>(dsQuyen) : new HashSet<>();
        BO_NHO_TAM.put(maNhanVien, tapHop);

        String chuoi = maHoaChuoiQuyen(tapHop);

        // 1. Ghi vào CSDL
        damBaoBangTonTai();
        String sql = """
                IF EXISTS (SELECT 1 FROM PhanQuyenTaiKhoan WHERE maNhanVien = ?)
                    UPDATE PhanQuyenTaiKhoan SET danhSachQuyen = ?, ngayCapNhat = GETDATE() WHERE maNhanVien = ?
                ELSE
                    INSERT INTO PhanQuyenTaiKhoan (maNhanVien, danhSachQuyen) VALUES (?, ?)
                """;
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, maNhanVien.trim());
            ps.setString(2, chuoi);
            ps.setString(3, maNhanVien.trim());
            ps.setString(4, maNhanVien.trim());
            ps.setString(5, chuoi);
            ps.executeUpdate();
        } catch (Exception ignored) {}

        // 2. Ghi vào tệp dự phòng cục bộ
        ghiFileDuPhong(maNhanVien, chuoi);
    }

    /**
     * Xóa tùy chỉnh quyền của nhân viên, quay về quyền mặc định theo vai trò.
     */
    public static synchronized void xoaTuyChinhQuyen(String maNhanVien) {
        if (maNhanVien == null || maNhanVien.isBlank()) return;

        BO_NHO_TAM.remove(maNhanVien);

        damBaoBangTonTai();
        String sql = "DELETE FROM PhanQuyenTaiKhoan WHERE maNhanVien = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, maNhanVien.trim());
            ps.executeUpdate();
        } catch (Exception ignored) {}

        // Xóa khỏi file
        try {
            Properties props = docFileDuPhong();
            if (props.containsKey(maNhanVien)) {
                props.remove(maNhanVien);
                luuFileDuPhong(props);
            }
        } catch (Exception ignored) {}
    }

    private static Set<String> giaiMaChuoiQuyen(String chuoi) {
        Set<String> set = new HashSet<>();
        if (chuoi != null && !chuoi.isBlank()) {
            String[] parts = chuoi.split(",");
            for (String p : parts) {
                if (!p.trim().isEmpty()) {
                    set.add(p.trim());
                }
            }
        }
        return set;
    }

    private static String maHoaChuoiQuyen(Set<String> set) {
        if (set == null || set.isEmpty()) return "";
        return String.join(",", set);
    }

    private static Properties docFileDuPhong() {
        Properties props = new Properties();
        if (Files.exists(PATH_FILE)) {
            try (InputStream in = Files.newInputStream(PATH_FILE);
                 Reader reader = new InputStreamReader(in, StandardCharsets.UTF_8)) {
                props.load(reader);
            } catch (Exception ignored) {}
        }
        return props;
    }

    private static void ghiFileDuPhong(String maNhanVien, String chuoi) {
        try {
            Properties props = docFileDuPhong();
            props.setProperty(maNhanVien, chuoi);
            luuFileDuPhong(props);
        } catch (Exception ignored) {}
    }

    private static void luuFileDuPhong(Properties props) {
        try {
            if (!Files.exists(PATH_FILE.getParent())) {
                Files.createDirectories(PATH_FILE.getParent());
            }
            try (OutputStream out = Files.newOutputStream(PATH_FILE);
                 Writer writer = new OutputStreamWriter(out, StandardCharsets.UTF_8)) {
                props.store(writer, "Phan quyen tuy chinh nhan vien - Happy Tails");
            }
        } catch (Exception ignored) {}
    }
}

