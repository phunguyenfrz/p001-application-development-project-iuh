import java.awt.*;
import java.awt.geom.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.*;
import java.util.List;
import javax.imageio.ImageIO;

public class GenerateClassDiagram {

    static class UMLClass {
        String name;
        String stereotype;
        List<String> attributes = new ArrayList<>();
        List<String> methods = new ArrayList<>();
        int x, y, w, h;

        UMLClass(String name, String stereotype) {
            this.name = name;
            this.stereotype = stereotype;
        }

        void addAttr(String attr) { attributes.add(attr); }
        void addMethod(String method) { methods.add(method); }

        void calcSize(FontMetrics fmHeader, FontMetrics fmContent) {
            int maxW = fmHeader.stringWidth(name) + 40;
            if (stereotype != null) {
                maxW = Math.max(maxW, fmContent.stringWidth("<<" + stereotype + ">>") + 40);
            }
            for (String a : attributes) {
                maxW = Math.max(maxW, fmContent.stringWidth(a) + 30);
            }
            for (String m : methods) {
                maxW = Math.max(maxW, fmContent.stringWidth(m) + 30);
            }
            w = Math.max(maxW, 220);

            int headerH = (stereotype != null) ? 46 : 32;
            int attrH = Math.max(attributes.size() * 18 + 10, 20);
            int methH = Math.max(methods.size() * 18 + 10, 20);
            h = headerH + attrH + methH;
        }
    }

    public static void main(String[] args) throws Exception {
        System.out.println("Building Refactored Class Diagram based on Teacher's Feedback...");

        Map<String, UMLClass> classes = new LinkedHashMap<>();

        // 1. KhachHang
        UMLClass kh = new UMLClass("KhachHang", null);
        kh.addAttr("- maKhachHang: String");
        kh.addAttr("- hoTen: String");
        kh.addAttr("- soDienThoai: String");
        kh.addAttr("- email: String");
        kh.addAttr("- diemTichLuy: int");
        kh.addAttr("- diaChi: String");
        kh.addAttr("- ghiChu: String");
        kh.addMethod("+ congDiem(diem: int): void");
        kh.addMethod("+ truDiem(diem: int): void");
        kh.addMethod("+ kiemTraThanhVien(): boolean");
        classes.put("KhachHang", kh);

        // 2. ThuCung
        UMLClass tc = new UMLClass("ThuCung", null);
        tc.addAttr("- maThuCung: String");
        tc.addAttr("- tenThuCung: String");
        tc.addAttr("- loaiThuCung: String");
        tc.addAttr("- giongLoai: String");
        tc.addAttr("- canNang: double");
        tc.addAttr("- ngaySinh: LocalDate");
        tc.addAttr("- diUng: String");
        tc.addAttr("- gioiTinh: boolean");
        tc.addMethod("+ capNhatCanNang(kg: double): void");
        tc.addMethod("+ capNhatDiUng(ghiChu: String): void");
        classes.put("ThuCung", tc);

        // 3. NhanVien
        UMLClass nv = new UMLClass("NhanVien", null);
        nv.addAttr("- maNhanVien: String");
        nv.addAttr("- hoTen: String");
        nv.addAttr("- soDienThoai: String");
        nv.addAttr("- chucVu: String");
        nv.addAttr("- ngaySinh: LocalDate");
        nv.addAttr("- gioiTinh: boolean");
        nv.addAttr("- trangThai: boolean");
        nv.addMethod("+ themNhanVien(): void");
        nv.addMethod("+ suaThongTin(): void");
        nv.addMethod("+ capNhatTrangThai(status: boolean): void");
        classes.put("NhanVien", nv);

        // 4. TaiKhoan
        UMLClass tk = new UMLClass("TaiKhoan", null);
        tk.addAttr("- tenDangNhap: String");
        tk.addAttr("- matKhauHash: String");
        tk.addAttr("- vaiTro: VaiTro");
        tk.addAttr("- trangThai: boolean");
        tk.addMethod("+ dangNhap(user: String, pass: String): boolean");
        tk.addMethod("+ doiMatKhau(passMoi: String): void");
        tk.addMethod("+ kiemTraQuyen(chucNang: String): boolean");
        classes.put("TaiKhoan", tk);

        // 5. Ca & PhanCa
        UMLClass ca = new UMLClass("Ca", null);
        ca.addAttr("- maCa: String");
        ca.addAttr("- tenCa: String");
        ca.addAttr("- gioBatDau: LocalTime");
        ca.addAttr("- gioKetThuc: LocalTime");
        ca.addAttr("- trangThai: boolean");
        classes.put("Ca", ca);

        UMLClass pc = new UMLClass("PhanCa", null);
        pc.addAttr("- maPhanCa: String");
        pc.addAttr("- ngayLam: LocalDate");
        pc.addAttr("- ghiChu: String");
        pc.addAttr("- trangThai: String");
        pc.addMethod("+ kiemTraTrungCa(): boolean");
        classes.put("PhanCa", pc);

        // 6. DichVuChamSoc & BangGiaDichVu
        UMLClass dv = new UMLClass("DichVuChamSoc", null);
        dv.addAttr("- maDichVu: String");
        dv.addAttr("- tenDichVu: String");
        dv.addAttr("- moTa: String");
        dv.addAttr("- thoiLuongPhut: int");
        dv.addAttr("- trangThai: boolean");
        dv.addMethod("+ layGiaHienHanh(bac: PhanLoaiKichThuoc): BigDecimal");
        classes.put("DichVuChamSoc", dv);

        UMLClass bg = new UMLClass("BangGiaDichVu", null);
        bg.addAttr("- maBangGia: String");
        bg.addAttr("- phanLoaiKichThuoc: PhanLoaiKichThuoc");
        bg.addAttr("- donGia: BigDecimal");
        bg.addAttr("- ngayApDung: LocalDate");
        bg.addAttr("- ngayHetHieuLuc: LocalDate");
        bg.addMethod("+ kiemTraHieuLuc(ngay: LocalDate): boolean");
        classes.put("BangGiaDichVu", bg);

        // 7. LichHen & ChiTietLichHen
        UMLClass lh = new UMLClass("LichHen", null);
        lh.addAttr("- maLichHen: String");
        lh.addAttr("- ngayHen: LocalDate");
        lh.addAttr("- gioHen: LocalTime");
        lh.addAttr("- trangThai: TrangThaiLichHen");
        lh.addAttr("- ghiChu: String");
        lh.addMethod("+ xacNhanLich(): void");
        lh.addMethod("+ huyLich(lyDo: String): void");
        classes.put("LichHen", lh);

        UMLClass ctlh = new UMLClass("ChiTietLichHen", null);
        ctlh.addAttr("- soLuong: int");
        ctlh.addAttr("- donGia: BigDecimal");
        ctlh.addAttr("- /thanhTien: BigDecimal");
        ctlh.addMethod("+ tinhThanhTien(): BigDecimal");
        classes.put("ChiTietLichHen", ctlh);

        // 8. PhieuDichVu & ChiTietPhieuDichVu
        UMLClass pdv = new UMLClass("PhieuDichVu", null);
        pdv.addAttr("- maPhieuDichVu: String");
        pdv.addAttr("- ngayTiepNhan: LocalDate");
        pdv.addAttr("- gioTiepNhan: LocalTime");
        pdv.addAttr("- trangThai: TrangThaiDichVu");
        pdv.addAttr("- ghiChu: String");
        pdv.addMethod("+ chuyenTrangThai(tt: TrangThaiDichVu): void");
        classes.put("PhieuDichVu", pdv);

        UMLClass ctpdv = new UMLClass("ChiTietPhieuDichVu", null);
        ctpdv.addAttr("- soLuong: int");
        ctpdv.addAttr("- canNangThucTe: double");
        ctpdv.addAttr("- donGia: BigDecimal");
        ctpdv.addAttr("- /thanhTien: BigDecimal");
        ctpdv.addMethod("+ tinhThanhTien(): BigDecimal");
        classes.put("ChiTietPhieuDichVu", ctpdv);

        // 9. SanPham & NhaCungCap
        UMLClass sp = new UMLClass("SanPham", null);
        sp.addAttr("- maSanPham: String");
        sp.addAttr("- maVach: String");
        sp.addAttr("- tenSanPham: String");
        sp.addAttr("- donViTinh: String");
        sp.addAttr("- giaBan: BigDecimal");
        sp.addAttr("- soLuongTon: int");
        sp.addAttr("- mucTonToiThieu: int");
        sp.addAttr("- trangThai: boolean");
        sp.addMethod("+ capNhatTonKho(soLuongThayDoi: int): void");
        sp.addMethod("+ kiemTraSapHetHang(): boolean");
        classes.put("SanPham", sp);

        UMLClass ncc = new UMLClass("NhaCungCap", null);
        ncc.addAttr("- maNhaCungCap: String");
        ncc.addAttr("- tenNhaCungCap: String");
        ncc.addAttr("- soDienThoai: String");
        ncc.addAttr("- diaChi: String");
        ncc.addAttr("- email: String");
        ncc.addAttr("- trangThai: boolean");
        classes.put("NhaCungCap", ncc);

        // 10. DonNhapHang & ChiTietDonNhap
        UMLClass dnh = new UMLClass("DonNhapHang", null);
        dnh.addAttr("- maDonNhap: String");
        dnh.addAttr("- ngayLap: LocalDate");
        dnh.addAttr("- gioLap: LocalTime");
        dnh.addAttr("- trangThai: String");
        dnh.addAttr("- /tongTien: BigDecimal");
        dnh.addMethod("+ tinhTongTien(): BigDecimal");
        classes.put("DonNhapHang", dnh);

        UMLClass ctdn = new UMLClass("ChiTietDonNhap", null);
        ctdn.addAttr("- soLuong: int");
        ctdn.addAttr("- giaNhap: BigDecimal");
        ctdn.addAttr("- /thanhTien: BigDecimal");
        ctdn.addMethod("+ tinhThanhTien(): BigDecimal");
        classes.put("ChiTietDonNhap", ctdn);

        // 11. KhuyenMai
        UMLClass km = new UMLClass("KhuyenMai", null);
        km.addAttr("- maKhuyenMai: String");
        km.addAttr("- tenKhuyenMai: String");
        km.addAttr("- phanTramGiam: double");
        km.addAttr("- ngayBatDau: LocalDate");
        km.addAttr("- ngayKetThuc: LocalDate");
        km.addAttr("- trangThai: boolean");
        km.addMethod("+ kiemTraHieuLuc(ngay: LocalDate): boolean");
        km.addMethod("+ tinhTienGiam(tongTienGoc: BigDecimal): BigDecimal");
        classes.put("KhuyenMai", km);

        // 12. HoaDon & 2 Association Classes (SanPham & DichVu) - TEACHER FIX!
        UMLClass hd = new UMLClass("HoaDon", null);
        hd.addAttr("- maHoaDon: String");
        hd.addAttr("- ngayLap: LocalDate");
        hd.addAttr("- gioLap: LocalTime");
        hd.addAttr("- hinhThucThanhToan: HinhThucThanhToan");
        hd.addAttr("- trangThaiThanhToan: TrangThaiThanhToan");
        hd.addAttr("- tienKhachDua: BigDecimal");
        hd.addAttr("- diemTichLuySuDung: int");
        hd.addAttr("- /tienGiamTuDiem: BigDecimal");
        hd.addAttr("- /tienGiamKhuyenMai: BigDecimal");
        hd.addAttr("- /tongTienGoc: BigDecimal");
        hd.addAttr("- /tongTien: BigDecimal");
        hd.addAttr("- /tienThoiLai: BigDecimal");
        hd.addMethod("+ tinhTongTienGoc(): BigDecimal");
        hd.addMethod("+ tinhTienGiam(): BigDecimal");
        hd.addMethod("+ tinhTongTien(): BigDecimal");
        hd.addMethod("+ tinhTienThoiLai(): BigDecimal");
        classes.put("HoaDon", hd);

        UMLClass cthdSP = new UMLClass("ChiTietHoaDonSanPham", null);
        cthdSP.addAttr("- soLuong: int");
        cthdSP.addAttr("- donGia: BigDecimal");
        cthdSP.addAttr("- /thanhTien: BigDecimal");
        cthdSP.addMethod("+ tinhThanhTien(): BigDecimal");
        classes.put("ChiTietHoaDonSanPham", cthdSP);

        UMLClass cthdDV = new UMLClass("ChiTietHoaDonDichVu", null);
        cthdDV.addAttr("- soLuong: int");
        cthdDV.addAttr("- donGia: BigDecimal");
        cthdDV.addAttr("- /thanhTien: BigDecimal");
        cthdDV.addMethod("+ tinhThanhTien(): BigDecimal");
        classes.put("ChiTietHoaDonDichVu", cthdDV);

        // 13. PhieuDoiTra & ChiTietPhieuDoiTra
        UMLClass pdt = new UMLClass("PhieuDoiTra", null);
        pdt.addAttr("- maPhieuDoiTra: String");
        pdt.addAttr("- ngayLap: LocalDate");
        pdt.addAttr("- gioLap: LocalTime");
        pdt.addAttr("- lyDo: String");
        pdt.addAttr("- loaiXuLy: LoaiXuLyDoiTra");
        pdt.addAttr("- trangThai: TrangThaiDoiTra");
        pdt.addAttr("- /tongTienHangTra: BigDecimal");
        pdt.addAttr("- /tongTienHangDoi: BigDecimal");
        pdt.addAttr("- /tienHoan: BigDecimal");
        pdt.addAttr("- /tienBu: BigDecimal");
        pdt.addMethod("+ tinhTongTienHangTra(): BigDecimal");
        pdt.addMethod("+ tinhTongTienHangDoi(): BigDecimal");
        pdt.addMethod("+ tinhTienHoan(): BigDecimal");
        pdt.addMethod("+ tinhTienBu(): BigDecimal");
        classes.put("PhieuDoiTra", pdt);

        UMLClass ctpdt = new UMLClass("ChiTietPhieuDoiTra", null);
        ctpdt.addAttr("- soLuongTra: int");
        ctpdt.addAttr("- donGiaTra: BigDecimal");
        ctpdt.addAttr("- soLuongDoi: int");
        ctpdt.addAttr("- donGiaDoi: BigDecimal");
        ctpdt.addAttr("- lyDoChiTiet: String");
        ctpdt.addAttr("- tinhTrangHang: String");
        ctpdt.addAttr("- /thanhTienTra: BigDecimal");
        ctpdt.addAttr("- /thanhTienDoi: BigDecimal");
        ctpdt.addMethod("+ tinhThanhTienTra(): BigDecimal");
        ctpdt.addMethod("+ tinhThanhTienDoi(): BigDecimal");
        classes.put("ChiTietPhieuDoiTra", ctpdt);

        // Layout positions for rendering high-res diagram
        // 4 Columns layout: Canvas = 2200 x 1700
        Font fontHeader = new Font("Segoe UI", Font.BOLD, 13);
        Font fontContent = new Font("Segoe UI", Font.PLAIN, 11);
        BufferedImage dummy = new BufferedImage(10, 10, BufferedImage.TYPE_INT_RGB);
        Graphics2D gDummy = dummy.createGraphics();
        FontMetrics fmH = gDummy.getFontMetrics(fontHeader);
        FontMetrics fmC = gDummy.getFontMetrics(fontContent);

        for (UMLClass c : classes.values()) {
            c.calcSize(fmH, fmC);
        }

        // Column 1: Dịch vụ & Chăm sóc thú cưng (x = 50)
        tc.x = 50; tc.y = 50;
        dv.x = 50; dv.y = 300;
        bg.x = 50; bg.y = 520;
        lh.x = 50; lh.y = 740;
        ctlh.x = 50; ctlh.y = 960;
        pdv.x = 50; pdv.y = 1140;
        ctpdv.x = 50; ctpdv.y = 1360;

        // Column 2: Khách hàng & Nhân sự (x = 420)
        kh.x = 420; kh.y = 50;
        nv.x = 420; nv.y = 350;
        tk.x = 420; tk.y = 630;
        ca.x = 420; ca.y = 860;
        pc.x = 420; pc.y = 1070;

        // Column 3: Hóa đơn & Bán hàng (x = 800)
        hd.x = 800; hd.y = 50;
        cthdSP.x = 800; cthdSP.y = 440;
        cthdDV.x = 800; cthdDV.y = 610;
        km.x = 800; km.y = 780;
        pdt.x = 800; pdt.y = 1030;
        ctpdt.x = 800; ctpdt.y = 1380;

        // Column 4: Sản phẩm & Nhập hàng (x = 1200)
        sp.x = 1200; sp.y = 50;
        ncc.x = 1200; ncc.y = 400;
        dnh.x = 1200; dnh.y = 660;
        ctdn.x = 1200; ctdn.y = 910;

        // Render to high-res PNG image
        int imgW = 1600;
        int imgH = 1650;
        BufferedImage img = new BufferedImage(imgW, imgH, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, imgW, imgH);

        // Draw connections first
        g.setColor(new Color(60, 60, 60));
        g.setStroke(new BasicStroke(1.3f));

        // Draw association lines and multiplicities
        drawAssoc(g, kh, tc, "1", "0..*", "sở hữu");
        drawAssoc(g, kh, lh, "1", "0..*", "đặt lịch");
        drawAssoc(g, kh, hd, "0..1", "0..*", "mua hàng");
        drawAssoc(g, nv, tk, "1", "1", "sở hữu");
        drawAssoc(g, nv, pc, "1", "0..*", "được phân");
        drawAssoc(g, ca, pc, "1", "0..*", "thuộc ca");
        drawAssoc(g, nv, hd, "1", "0..*", "lập");
        drawAssoc(g, nv, pdv, "1", "0..*", "tiếp nhận");
        drawAssoc(g, dv, bg, "1", "1..*", "bảng giá bậc");
        drawAssoc(g, lh, ctlh, "1", "1..*", "chi tiết hẹn");
        drawAssoc(g, pdv, ctpdv, "1", "1..*", "chi tiết dv");
        drawAssoc(g, hd, cthdSP, "1", "0..*", "dòng sản phẩm");
        drawAssoc(g, hd, cthdDV, "1", "0..*", "dòng dịch vụ");
        drawAssoc(g, sp, cthdSP, "1", "0..*", "bán ra");
        drawAssoc(g, dv, cthdDV, "1", "0..*", "thực hiện");
        drawAssoc(g, km, hd, "0..1", "0..*", "áp dụng");
        drawAssoc(g, hd, pdt, "1", "0..1", "đổi trả");
        drawAssoc(g, pdt, ctpdt, "1", "1..*", "chi tiết trả");
        drawAssoc(g, sp, ctpdt, "1", "0..*", "sản phẩm trả");
        drawAssoc(g, ncc, sp, "1", "0..*", "cung cấp");
        drawAssoc(g, ncc, dnh, "1", "0..*", "nhập từ");
        drawAssoc(g, dnh, ctdn, "1", "1..*", "chi tiết nhập");
        drawAssoc(g, sp, ctdn, "1", "0..*", "nhập kho");

        // Draw classes
        for (UMLClass c : classes.values()) {
            drawUMLBox(g, c, fontHeader, fontContent);
        }

        g.dispose();
        new File("docs/diagrams").mkdirs();
        File outPng = new File("docs/diagrams/Class_Diagram_Refactored.png");
        ImageIO.write(img, "png", outPng);
        System.out.println("Saved " + outPng.getPath());

        // Also generate Draw.io XML
        String drawioXml = buildDrawioXml(classes);
        File outDrawio = new File("docs/diagrams/Class_Diagram_Refactored.drawio");
        Files.writeString(outDrawio.toPath(), drawioXml, StandardCharsets.UTF_8);
        System.out.println("Saved " + outDrawio.getPath());
    }

    static void drawUMLBox(Graphics2D g, UMLClass c, Font fHeader, Font fContent) {
        int x = c.x, y = c.y, w = c.w, h = c.h;

        // Background
        g.setColor(new Color(255, 252, 240)); // Light cream
        g.fillRect(x, y, w, h);

        // Header Background
        int headerH = (c.stereotype != null) ? 44 : 32;
        g.setColor(new Color(255, 242, 204)); // Pale yellow
        g.fillRect(x, y, w, headerH);

        // Border
        g.setColor(Color.BLACK);
        g.setStroke(new BasicStroke(1.4f));
        g.drawRect(x, y, w, h);
        g.drawLine(x, y + headerH, x + w, y + headerH);

        // Header Text
        g.setFont(fHeader);
        FontMetrics fmH = g.getFontMetrics();
        if (c.stereotype != null) {
            g.setFont(fContent);
            String st = "<<" + c.stereotype + ">>";
            g.drawString(st, x + (w - g.getFontMetrics().stringWidth(st)) / 2, y + 16);
            g.setFont(fHeader);
            g.drawString(c.name, x + (w - fmH.stringWidth(c.name)) / 2, y + 34);
        } else {
            g.drawString(c.name, x + (w - fmH.stringWidth(c.name)) / 2, y + 21);
        }

        // Attributes
        g.setFont(fContent);
        FontMetrics fmC = g.getFontMetrics();
        int curY = y + headerH + 16;
        for (String a : c.attributes) {
            if (a.contains("/t")) { // Derived attribute highlight
                g.setColor(new Color(0, 70, 150));
            } else {
                g.setColor(new Color(20, 20, 20));
            }
            g.drawString(a, x + 10, curY);
            curY += 18;
        }

        // Line between attributes and methods
        int lineAttrY = curY - 6;
        g.setColor(Color.BLACK);
        g.drawLine(x, lineAttrY, x + w, lineAttrY);

        // Methods
        curY = lineAttrY + 16;
        for (String m : c.methods) {
            if (m.startsWith("+ tinh")) {
                g.setColor(new Color(0, 110, 40)); // Green for calculation methods
            } else {
                g.setColor(new Color(20, 20, 20));
            }
            g.drawString(m, x + 10, curY);
            curY += 18;
        }
    }

    static void drawAssoc(Graphics2D g, UMLClass c1, UMLClass c2, String m1, String m2, String label) {
        int x1 = c1.x + c1.w / 2;
        int y1 = c1.y + c1.h / 2;
        int x2 = c2.x + c2.w / 2;
        int y2 = c2.y + c2.h / 2;

        // Choose border exit points
        Point p1 = getIntersection(c1, x2, y2);
        Point p2 = getIntersection(c2, x1, y1);

        g.drawLine(p1.x, p1.y, p2.x, p2.y);

        g.setFont(new Font("Segoe UI", Font.BOLD, 10));
        g.setColor(new Color(80, 80, 80));

        // Draw multiplicities near endpoints
        int dx1 = (p2.x > p1.x) ? 6 : -18;
        int dy1 = (p2.y > p1.y) ? 14 : -6;
        g.drawString(m1, p1.x + dx1, p1.y + dy1);

        int dx2 = (p1.x > p2.x) ? 6 : -18;
        int dy2 = (p1.y > p2.y) ? 14 : -6;
        g.drawString(m2, p2.x + dx2, p2.y + dy2);

        // Label at middle
        if (label != null) {
            g.setFont(new Font("Segoe UI", Font.ITALIC, 10));
            g.setColor(new Color(100, 100, 100));
            g.drawString(label, (p1.x + p2.x) / 2 + 5, (p1.y + p2.y) / 2 - 4);
        }
    }

    static Point getIntersection(UMLClass c, int targetX, int targetY) {
        int cx = c.x + c.w / 2;
        int cy = c.y + c.h / 2;
        double angle = Math.atan2(targetY - cy, targetX - cx);

        // Determine if intersecting vertical or horizontal border
        double halfW = c.w / 2.0;
        double halfH = c.h / 2.0;

        double tan = Math.tan(angle);
        if (Math.abs(tan) <= halfH / halfW) {
            // Intersects left or right
            if (targetX >= cx) {
                return new Point(c.x + c.w, cy + (int) (halfW * tan));
            } else {
                return new Point(c.x, cy - (int) (halfW * tan));
            }
        } else {
            // Intersects top or bottom
            double cot = 1.0 / tan;
            if (targetY >= cy) {
                return new Point(cx + (int) (halfH * cot), c.y + c.h);
            } else {
                return new Point(cx - (int) (halfH * cot), c.y);
            }
        }
    }

    static String buildDrawioXml(Map<String, UMLClass> classes) {
        StringBuilder sb = new StringBuilder();
        sb.append("<mxfile host=\"app.diagrams.net\" modified=\"2026-11-05T07:30:00.000Z\" agent=\"Antigravity\" version=\"24.0.0\" type=\"device\">\n");
        sb.append("  <diagram id=\"xVxHSCsavbxJmrzbO-_I\" name=\"Class_Diagram_Refactored\">\n");
        sb.append("    <mxGraphModel dx=\"1422\" dy=\"794\" grid=\"1\" gridSize=\"10\" guides=\"1\" tooltips=\"1\" connect=\"1\" arrows=\"1\" fold=\"1\" page=\"1\" pageScale=\"1\" pageWidth=\"2339\" pageHeight=\"1654\" math=\"0\" shadow=\"0\">\n");
        sb.append("      <root>\n");
        sb.append("        <mxCell id=\"0\" />\n");
        sb.append("        <mxCell id=\"1\" parent=\"0\" />\n");

        int id = 2;
        Map<String, Integer> classIds = new HashMap<>();

        for (UMLClass c : classes.values()) {
            int curId = id++;
            classIds.put(c.name, curId);

            StringBuilder label = new StringBuilder();
            if (c.stereotype != null) {
                label.append("&lt;&lt;").append(c.stereotype).append("&gt;&gt;&lt;br/&gt;");
            }
            label.append("&lt;b&gt;").append(c.name).append("&lt;/b&gt;&lt;hr/&gt;");
            for (String a : c.attributes) {
                label.append(escapeXml(a)).append("&lt;br/&gt;");
            }
            label.append("&lt;hr/&gt;");
            for (String m : c.methods) {
                label.append(escapeXml(m)).append("&lt;br/&gt;");
            }

            sb.append("        <mxCell id=\"").append(curId).append("\" value=\"").append(label)
              .append("\" style=\"swimlane;fontStyle=0;childLayout=stackLayout;horizontal=1;startSize=26;fillColor=#FFF2CC;strokeColor=#D6B656;shadow=0;resizeLast=0;collapsible=0;html=1;align=left;spacingLeft=6;\" vertex=\"1\" parent=\"1\">\n");
            sb.append("          <mxGeometry x=\"").append(c.x).append("\" y=\"").append(c.y)
              .append("\" width=\"").append(c.w).append("\" height=\"").append(c.h).append("\" as=\"geometry\" />\n");
            sb.append("        </mxCell>\n");
        }

        // Add standard associations
        String[][] links = {
            {"KhachHang", "ThuCung", "1", "0..*", "sở hữu"},
            {"KhachHang", "LichHen", "1", "0..*", "đặt lịch"},
            {"KhachHang", "HoaDon", "0..1", "0..*", "mua hàng"},
            {"NhanVien", "TaiKhoan", "1", "1", "sở hữu"},
            {"NhanVien", "PhanCa", "1", "0..*", "được phân"},
            {"Ca", "PhanCa", "1", "0..*", "thuộc ca"},
            {"NhanVien", "HoaDon", "1", "0..*", "lập"},
            {"NhanVien", "PhieuDichVu", "1", "0..*", "tiếp nhận"},
            {"DichVuChamSoc", "BangGiaDichVu", "1", "1..*", "bảng giá"},
            {"LichHen", "ChiTietLichHen", "1", "1..*", "chi tiết hẹn"},
            {"PhieuDichVu", "ChiTietPhieuDichVu", "1", "1..*", "chi tiết dv"},
            {"HoaDon", "ChiTietHoaDonSanPham", "1", "0..*", "dòng sp"},
            {"HoaDon", "ChiTietHoaDonDichVu", "1", "0..*", "dòng dv"},
            {"SanPham", "ChiTietHoaDonSanPham", "1", "0..*", "bán ra"},
            {"DichVuChamSoc", "ChiTietHoaDonDichVu", "1", "0..*", "thực hiện"},
            {"KhuyenMai", "HoaDon", "0..1", "0..*", "áp dụng"},
            {"HoaDon", "PhieuDoiTra", "1", "0..1", "đổi trả"},
            {"PhieuDoiTra", "ChiTietPhieuDoiTra", "1", "1..*", "chi tiết trả"},
            {"SanPham", "ChiTietPhieuDoiTra", "1", "0..*", "sp trả"},
            {"NhaCungCap", "SanPham", "1", "0..*", "cung cấp"},
            {"NhaCungCap", "DonNhapHang", "1", "0..*", "nhập từ"},
            {"DonNhapHang", "ChiTietDonNhap", "1", "1..*", "chi tiết nhập"},
            {"SanPham", "ChiTietDonNhap", "1", "0..*", "nhập kho"}
        };

        for (String[] link : links) {
            int linkId = id++;
            int srcId = classIds.get(link[0]);
            int trgId = classIds.get(link[1]);
            sb.append("        <mxCell id=\"").append(linkId).append("\" value=\"").append(link[4])
              .append("\" style=\"endArrow=none;html=1;rounded=0;exitX=0.5;exitY=1;entryX=0.5;entryY=0;\" edge=\"1\" parent=\"1\" source=\"")
              .append(srcId).append("\" target=\"").append(trgId).append("\">\n");
            sb.append("          <mxGeometry relative=\"1\" as=\"geometry\">\n");
            sb.append("            <mxPoint as=\"sourcePoint\" />\n");
            sb.append("            <mxPoint as=\"targetPoint\" />\n");
            sb.append("          </mxGeometry>\n");
            sb.append("        </mxCell>\n");
        }

        sb.append("      </root>\n");
        sb.append("    </mxGraphModel>\n");
        sb.append("  </diagram>\n");
        sb.append("</mxfile>\n");
        return sb.toString();
    }

    static String escapeXml(String s) {
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;");
    }
}
