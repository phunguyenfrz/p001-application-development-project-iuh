import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.zip.*;
import java.util.regex.*;

public class ReadClassSection {
    public static void main(String[] args) throws Exception {
        ZipFile zip = new ZipFile("docs/9_REPORT_ApplicationDevelopment.docx");
        InputStream is = zip.getInputStream(zip.getEntry("word/document.xml"));
        String xml = new String(is.readAllBytes(), StandardCharsets.UTF_8);
        zip.close();

        int pos = 3960000;
        int end = Math.min(xml.length(), pos + 30000);
        String sub = xml.substring(pos, end);

        Pattern p = Pattern.compile("<w:p[ >].*?</w:p>");
        Matcher m = p.matcher(sub);
        while (m.find()) {
            String pXml = m.group();
            String text = pXml.replaceAll("<[^>]+>", " ").replaceAll("\\s+", " ").trim();
            if (!text.isEmpty()) {
                System.out.println(text);
            }
        }
    }
}
