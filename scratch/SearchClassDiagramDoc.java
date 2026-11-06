import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.zip.*;
import java.util.regex.*;

public class SearchClassDiagramDoc {
    public static void main(String[] args) throws Exception {
        ZipFile zip = new ZipFile("docs/9_REPORT_ApplicationDevelopment.docx");
        ZipEntry entry = zip.getEntry("word/document.xml");
        InputStream is = zip.getInputStream(entry);
        String xml = new String(is.readAllBytes(), StandardCharsets.UTF_8);
        zip.close();

        // Search for Class Diagram or Sơ đồ lớp
        Pattern p = Pattern.compile("(?i)(class\\s*diagram|sơ\\s*đồ\\s*lớp)");
        Matcher m = p.matcher(xml);

        int count = 0;
        while (m.find()) {
            count++;
            int start = Math.max(0, m.start() - 60);
            int end = Math.min(xml.length(), m.end() + 150);
            String snippet = xml.substring(start, end).replaceAll("<[^>]+>", " ").replaceAll("\\s+", " ").trim();
            System.out.printf("#%d (pos %d): %s%n", count, m.start(), snippet);
        }
    }
}
