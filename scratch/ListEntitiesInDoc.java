import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.zip.*;
import java.util.regex.*;

public class ListEntitiesInDoc {
    public static void main(String[] args) throws Exception {
        ZipFile zip = new ZipFile("docs/9_REPORT_ApplicationDevelopment.docx");
        InputStream is = zip.getInputStream(zip.getEntry("word/document.xml"));
        String xml = new String(is.readAllBytes(), StandardCharsets.UTF_8);
        zip.close();

        int pos = 3960000;
        String sub = xml.substring(pos);
        String text = sub.replaceAll("<[^>]+>", " ").replaceAll("\\s+", " ");

        Pattern p = Pattern.compile("Thực thể\\s+([A-Za-z0-9_]+)\\s*\\(([^)]+)\\)");
        Matcher m = p.matcher(text);
        while (m.find()) {
            System.out.printf("Found: %s (%s)%n", m.group(1), m.group(2));
        }
    }
}
