package com.petshop.bus;

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
public final class PhanQuyenBUS {

    private static final Path PATH_FILE = Paths.get("data", "phan_quyen.properties");
    private static final Map<String, Set<String>> BO_NHO_TAM = new HashMap<>();
    private static final Set<String> KHONG_CO_QUYEN_RIENG = new HashSet<>();
    private static boolean daKhoiTaoBang = false;
    private static long thoiDiemLoiDbCuoi = 0;

    private PhanQuyenBUS() {}

    private static boolean coTheThuDb() {
        return System.currentTimeMillis() - thoiDiemLoiDbCuoi > 15_000;
    }

    /**
     * Khởi tạo bảng PhanQuyenTaiKhoan trong CSDL nếu chưa tồn tại.
     */
    private static synchronized void damBaoBangTonTai() {
        if (daKhoiTaoBang || !coTheThuDb()) return;
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
            thoiDiemLoiDbCuoi = System.currentTimeMillis();
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
        if (KHONG_CO_QUYEN_RIENG.contains(maNhanVien)) {
            return null;
        }

        // 2. Kiểm tra trong CSDL
        if (coTheThuDb()) {
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
                        return set;
                    }
                }
            } catch (Exception ignored) {
                thoiDiemLoiDbCuoi = System.currentTimeMillis();
            }
        }

        // 3. Kiểm tra trong tệp dự phòng cục bộ
        Properties props = docFileDuPhong();
        if (props.containsKey(maNhanVien)) {
            String chuoi = props.getProperty(maNhanVien);
            Set<String> set = giaiMaChuoiQuyen(chuoi);
            BO_NHO_TAM.put(maNhanVien, set);
            return set;
        }

        KHONG_CO_QUYEN_RIENG.add(maNhanVien);
        return null;
    }

    /**
     * Lưu danh sách mã chức năng được cấp quyền cho nhân viên.
     */
    public static synchronized void luuDanhSachQuyen(String maNhanVien, Set<String> dsQuyen) {
        if (maNhanVien == null || maNhanVien.isBlank()) return;

        KHONG_CO_QUYEN_RIENG.remove(maNhanVien);
        Set<String> tapHop = (dsQuyen != null) ? new HashSet<>(dsQuyen) : new HashSet<>();
        BO_NHO_TAM.put(maNhanVien, tapHop);

        String chuoi = maHoaChuoiQuyen(tapHop);

        // 1. Ghi vào CSDL
        if (coTheThuDb()) {
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
            } catch (Exception ignored) {
                thoiDiemLoiDbCuoi = System.currentTimeMillis();
            }
        }

        // 2. Ghi vào tệp dự phòng cục bộ
        ghiFileDuPhong(maNhanVien, chuoi);
    }

    /**
     * Xóa tùy chỉnh quyền của nhân viên, quay về quyền mặc định theo vai trò.
     */
    public static synchronized void xoaTuyChinhQuyen(String maNhanVien) {
        if (maNhanVien == null || maNhanVien.isBlank()) return;

        BO_NHO_TAM.remove(maNhanVien);
        KHONG_CO_QUYEN_RIENG.remove(maNhanVien);

        if (coTheThuDb()) {
            damBaoBangTonTai();
            String sql = "DELETE FROM PhanQuyenTaiKhoan WHERE maNhanVien = ?";
            try (Connection conn = DatabaseManager.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, maNhanVien.trim());
                ps.executeUpdate();
            } catch (Exception ignored) {
                thoiDiemLoiDbCuoi = System.currentTimeMillis();
            }
        }

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
        List<String> list = new ArrayList<>(set);
        Collections.sort(list);
        return String.join(",", list);
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
            String cu = props.getProperty(maNhanVien);
            if (Objects.equals(cu, chuoi)) {
                return;
            }
            props.setProperty(maNhanVien, chuoi);
            luuFileDuPhong(props);
        } catch (Exception ignored) {}
    }

    private static void luuFileDuPhong(Properties props) {
        try {
            if (!Files.exists(PATH_FILE.getParent())) {
                Files.createDirectories(PATH_FILE.getParent());
            }
            List<String> keys = new ArrayList<>(props.stringPropertyNames());
            Collections.sort(keys);
            StringBuilder sb = new StringBuilder();
            sb.append("# Phan quyen tuy chinh nhan vien - Happy Tails\n");
            for (String k : keys) {
                sb.append(k).append("=").append(props.getProperty(k)).append("\n");
            }
            Files.writeString(PATH_FILE, sb.toString(), StandardCharsets.UTF_8);
        } catch (Exception ignored) {}
    }
}

