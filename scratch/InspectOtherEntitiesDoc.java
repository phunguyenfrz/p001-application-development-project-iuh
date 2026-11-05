import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.zip.*;

public class InspectOtherEntitiesDoc {
    public static void main(String[] args) throws Exception {
        ZipFile zip = new ZipFile("docs/9_REPORT_ApplicationDevelopment.docx");
        InputStream is = zip.getInputStream(zip.getEntry("word/document.xml"));
        String xml = new String(is.readAllBytes(), StandardCharsets.UTF_8);
        zip.close();

        int pos = 3960000;
        String text = xml.substring(pos).replaceAll("<[^>]+>", " ").replaceAll("\\s+", " ");

        printSection(text, "Thực thể DonHang", "Thực thể KhuyenMai");
        printSection(text, "Thực thể PhieuDoiTra", "Chương 4");
        printSection(text, "Thực thể TaiKhoan", "Thực thể Ca");
        printSection(text, "Thực thể PhieuDichVu", "Thực thể NhaCungCap");
    }

    static void printSection(String text, String startMarker, String endMarker) {
        int start = text.indexOf(startMarker);
        if (start != -1) {
            int end = text.indexOf(endMarker, start);
            if (end == -1) end = Math.min(text.length(), start + 6000);
            System.out.println("=== " + startMarker + " ===");
            System.out.println(text.substring(start, end));
        }
    }
}
