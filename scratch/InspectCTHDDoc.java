import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.zip.*;

public class InspectCTHDDoc {
    public static void main(String[] args) throws Exception {
        ZipFile zip = new ZipFile("docs/9_REPORT_ApplicationDevelopment.docx");
        InputStream is = zip.getInputStream(zip.getEntry("word/document.xml"));
        String xml = new String(is.readAllBytes(), StandardCharsets.UTF_8);
        zip.close();

        int pos = 3960000;
        String text = xml.substring(pos).replaceAll("<[^>]+>", " ").replaceAll("\\s+", " ");

        int pCTHD = text.indexOf("Thực thể ChiTietHoaDon");
        int pPDT = text.indexOf("Thực thể PhieuDoiTra");
        if (pCTHD != -1 && pPDT != -1) {
            System.out.println("=== CHITIETHOADON ===");
            System.out.println(text.substring(pCTHD, pPDT));
        }
    }
}
