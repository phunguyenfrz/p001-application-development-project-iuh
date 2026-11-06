import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.zip.*;
import java.util.regex.*;

public class FindInSavedDoc {
    public static void main(String[] args) throws Exception {
        ZipFile zip = new ZipFile("docs/9_REPORT_ApplicationDevelopment.docx");
        ZipEntry entry = zip.getEntry("word/document.xml");
        InputStream is = zip.getInputStream(entry);
        String xml = new String(is.readAllBytes(), StandardCharsets.UTF_8);
        zip.close();

        // Search for UC010
        Pattern p = Pattern.compile("UC010");
        Matcher m = p.matcher(xml);
        int c = 0;
        while (m.find()) {
            c++;
            int start = Math.max(0, m.start() - 60);
            int end = Math.min(xml.length(), m.end() + 100);
            String snippet = xml.substring(start, end).replaceAll("<[^>]+>", " ").replaceAll("\\s+", " ").trim();
            System.out.printf("#%d (pos %d): %s%n", c, m.start(), snippet);
        }
    }
}
