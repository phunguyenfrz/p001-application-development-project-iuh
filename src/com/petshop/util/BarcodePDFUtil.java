package com.petshop.util;

import com.lowagie.text.*;
import com.lowagie.text.pdf.*;
import com.petshop.entity.SanPham;

import java.io.File;
import java.io.FileOutputStream;
import java.text.DecimalFormat;
import java.util.List;

/**
 * Tiện ích tạo và xuất tem mã vạch (Barcode sticker PDF) cho sản phẩm cửa hàng Happy Tails.
 * Tuân thủ bộ chỉ thị kỹ thuật IUH:
 * - Thuộc tầng com.petshop.util
 * - Dùng chính maSanPham làm mã vạch Barcode 1D (Code 128)
 * - Giá bán format VND từ kiểu double (getGiaBanDouble() / getGiaBan())
 * - Bố cục lưới nhãn tem dán (3 cột trên khổ A4 tiêu chuẩn)
 */
public final class BarcodePDFUtil {

    private static final DecimalFormat DINH_DANG_TIEN = new DecimalFormat("#,##0 VNĐ");

    private BarcodePDFUtil() {}

    /**
     * Xuất danh sách sản phẩm thành file PDF tem nhãn mã vạch (Grid Sticker).
     *
     * @param ds Danh sách sản phẩm cần in tem
     * @param destination File PDF đầu ra
     * @throws Exception nếu xảy ra lỗi ghi file hoặc tạo PDF
     */
    public static void xuatDanhSachMaVachPDF(List<SanPham> ds, File destination) throws Exception {
        if (ds == null || ds.isEmpty()) {
            throw new IllegalArgumentException("Danh sách sản phẩm xuất mã vạch không được rỗng!");
        }
        if (destination == null) {
            throw new IllegalArgumentException("File đích không được null!");
        }

        // Tạo font chữ hỗ trợ tiếng Việt có dấu (sử dụng Arial hệ thống Windows nếu có)
        Font fontShop;
        Font fontTenSp;
        Font fontGia;
        try {
            File fontArial = new File("C:/Windows/Fonts/arial.ttf");
            File fontArialBold = new File("C:/Windows/Fonts/arialbd.ttf");
            String pathBold = fontArialBold.exists() ? fontArialBold.getAbsolutePath() : (fontArial.exists() ? fontArial.getAbsolutePath() : null);
            String pathNormal = fontArial.exists() ? fontArial.getAbsolutePath() : null;

            if (pathBold != null && pathNormal != null) {
                BaseFont bfBold = BaseFont.createFont(pathBold, BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
                BaseFont bfNormal = BaseFont.createFont(pathNormal, BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
                fontShop = new Font(bfBold, 8.5f, Font.BOLD, new java.awt.Color(3, 105, 161));
                fontTenSp = new Font(bfBold, 9.0f, Font.BOLD, new java.awt.Color(15, 23, 42));
                fontGia = new Font(bfBold, 9.5f, Font.BOLD, new java.awt.Color(185, 28, 28));
            } else {
                fontShop = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 8.5f, new java.awt.Color(3, 105, 161));
                fontTenSp = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9.0f, new java.awt.Color(15, 23, 42));
                fontGia = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9.5f, new java.awt.Color(185, 28, 28));
            }
        } catch (Exception e) {
            fontShop = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 8.5f, new java.awt.Color(3, 105, 161));
            fontTenSp = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9.0f, new java.awt.Color(15, 23, 42));
            fontGia = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9.5f, new java.awt.Color(185, 28, 28));
        }

        // Khổ giấy A4, lề 20pt
        Document document = new Document(PageSize.A4, 18, 18, 24, 24);
        PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(destination));
        document.open();

        PdfContentByte cb = writer.getDirectContent();

        // Tiêu đề đầu trang in
        Paragraph tieuDeTrang = new Paragraph("BẢNG TEM MÃ VẠCH SẢN PHẨM - HAPPY TAILS PET SHOP\n\n",
                FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, new java.awt.Color(15, 23, 42)));
        tieuDeTrang.setAlignment(Element.ALIGN_CENTER);
        document.add(tieuDeTrang);

        // Bố cục lưới 3 cột tem nhãn tiêu chuẩn
        int soCot = 3;
        PdfPTable table = new PdfPTable(soCot);
        table.setWidthPercentage(100);
        table.setSpacingBefore(4);

        for (SanPham sp : ds) {
            String maSP = sp.getMaSanPham() != null ? sp.getMaSanPham().trim() : "SP000000";
            String tenSP = sp.getTenSanPham() != null ? sp.getTenSanPham().trim() : "Sản phẩm";
            if (tenSP.length() > 32) {
                tenSP = tenSP.substring(0, 29) + "...";
            }

            double giaBan = sp.getGiaBan();

            // Tạo mã vạch Code 128 từ chính maSanPham
            Barcode128 code128 = new Barcode128();
            code128.setCode(maSP);
            code128.setCodeType(Barcode128.CODE128);
            code128.setBarHeight(28f);
            code128.setSize(9f);
            code128.setBaseline(10f);
            code128.setTextAlignment(Element.ALIGN_CENTER);

            Image barcodeImg = code128.createImageWithBarcode(cb, null, null);
            barcodeImg.setAlignment(Element.ALIGN_CENTER);
            barcodeImg.scalePercent(105);

            // Khối nội dung 1 tem nhãn
            PdfPCell cell = new PdfPCell();
            cell.setPadding(8);
            cell.setBorderColor(new java.awt.Color(203, 213, 225));
            cell.setBorderWidth(1f);
            cell.setBackgroundColor(new java.awt.Color(255, 255, 255));
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);

            // 1. Tên cửa hàng
            Paragraph pShop = new Paragraph("🐾 HAPPY TAILS PET SHOP", fontShop);
            pShop.setAlignment(Element.ALIGN_CENTER);
            pShop.setSpacingAfter(3);
            cell.addElement(pShop);

            // 2. Tên sản phẩm
            Paragraph pTen = new Paragraph(tenSP, fontTenSp);
            pTen.setAlignment(Element.ALIGN_CENTER);
            pTen.setSpacingAfter(4);
            cell.addElement(pTen);

            // 3. Hình ảnh Barcode 1D + mã số dưới
            cell.addElement(barcodeImg);

            // 4. Giá bán niêm yết
            Paragraph pGia = new Paragraph("Giá: " + DINH_DANG_TIEN.format(giaBan), fontGia);
            pGia.setAlignment(Element.ALIGN_CENTER);
            pGia.setSpacingBefore(4);
            cell.addElement(pGia);

            table.addCell(cell);
        }

        // Bổ sung các ô trống nếu hàng cuối chưa đủ 3 cột
        int oConThieu = ds.size() % soCot;
        if (oConThieu != 0) {
            int canThem = soCot - oConThieu;
            for (int i = 0; i < canThem; i++) {
                PdfPCell emptyCell = new PdfPCell();
                emptyCell.setBorder(Rectangle.NO_BORDER);
                table.addCell(emptyCell);
            }
        }

        document.add(table);
        document.close();
    }
}

