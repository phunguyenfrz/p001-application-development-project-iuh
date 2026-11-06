import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.zip.*;
import java.util.regex.*;

public class InspectEntityDetails {
    public static void main(String[] args) throws Exception {
        ZipFile zip = new ZipFile("docs/9_REPORT_ApplicationDevelopment.docx");
        InputStream is = zip.getInputStream(zip.getEntry("word/document.xml"));
        String xml = new String(is.readAllBytes(), StandardCharsets.UTF_8);
        zip.close();

        int pos = 3960000;
        String sub = xml.substring(pos);
        String text = sub.replaceAll("<[^>]+>", " ").replaceAll("\\s+", " ");

        int pHD = text.indexOf("Thực thể HoaDon");
        int pPDT = text.indexOf("Thực thể PhieuDoiTra");
        if (pHD != -1 && pPDT != -1) {
            System.out.println("=== HOADON & CHITIETHOADON ===");
            System.out.println(text.substring(pHD, pPDT));
        }

        int pEnd = text.indexOf("Chương 4", pPDT);
        if (pEnd == -1) pEnd = pPDT + 15000;
        System.out.println("=== PHIEUDOITRA ===");
        System.out.println(text.substring(pPDT, Math.min(text.length(), pEnd)));
    }
}
