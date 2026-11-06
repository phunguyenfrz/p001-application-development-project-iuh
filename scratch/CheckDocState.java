import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.zip.*;
import java.util.regex.*;

public class CheckDocState {
    public static void main(String[] args) throws Exception {
        ZipFile zip = new ZipFile("docs/9_REPORT_ApplicationDevelopment.docx");
        ZipEntry entry = zip.getEntry("word/document.xml");
        InputStream is = zip.getInputStream(entry);
        String xml = new String(is.readAllBytes(), StandardCharsets.UTF_8);
        zip.close();

        System.out.println("Document XML length: " + xml.length());

        // Check UC010 description table
        int pos10 = xml.indexOf("10.1. Mô tả use case UC010_Đổi trả hàng");
        System.out.println("pos10: " + pos10);
        if (pos10 != -1) {
            int nextTbl = xml.indexOf("<w:tbl", pos10);
            int endTbl = xml.indexOf("</w:tbl>", nextTbl) + 8;
            String tbl = xml.substring(nextTbl, endTbl);
            String tblText = tbl.replaceAll("<[^>]+>", " ").replaceAll("\\s+", " ").trim();
            if (tblText.length() > 300) tblText = tblText.substring(0, 300) + "...";
            System.out.println("UC010 Table preview:\n" + tblText);
        }
    }
}
