import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.zip.*;

public class InspectUC10 {
    public static void main(String[] args) throws Exception {
        ZipFile zip = new ZipFile("docs/9_REPORT_ApplicationDevelopment.docx");
        InputStream is = zip.getInputStream(zip.getEntry("word/document.xml"));
        String xml = new String(is.readAllBytes(), StandardCharsets.UTF_8);
        zip.close();

        int pos = 3089648;
        int end = Math.min(xml.length(), pos + 40000);
        String chunk = xml.substring(pos, end);
        int tblStart = chunk.indexOf("<w:tbl");
        System.out.println("tblStart: " + tblStart);
        if (tblStart != -1) {
            int tblEnd = chunk.indexOf("</w:tbl>", tblStart);
            System.out.println("tblEnd: " + tblEnd);
            if (tblEnd != -1) {
                String tbl = chunk.substring(tblStart, tblEnd + 8);
                System.out.println("Table raw length: " + tbl.length());
                String text = tbl.replaceAll("<[^>]+>", " ").replaceAll("\\s+", " ").trim();
                System.out.println("Table text:\n" + text);
            }
        }
    }
}
