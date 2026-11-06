import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.zip.*;
import java.util.regex.*;

public class ReadFeedbackDocs {
    public static void main(String[] args) throws Exception {
        readFile("docs/ĐIỂM CHẤM THỰC HÀNH.docx");
        System.out.println("\n=======================================================\n");
        readFile("docs/PTUD_Bien Ban LVN.docx");
    }

    static void readFile(String path) {
        System.out.println("--- FILE: " + path + " ---");
        try (ZipFile zip = new ZipFile(path)) {
            ZipEntry entry = zip.getEntry("word/document.xml");
            if (entry == null) {
                System.out.println("No document.xml");
                return;
            }
            String xml = new String(zip.getInputStream(entry).readAllBytes(), StandardCharsets.UTF_8);
            Pattern p = Pattern.compile("<w:p[ >].*?</w:p>");
            Matcher m = p.matcher(xml);
            while (m.find()) {
                String text = m.group().replaceAll("<[^>]+>", " ").replaceAll("\\s+", " ").trim();
                if (!text.isEmpty()) {
                    System.out.println(text);
                }
            }
        } catch (Exception e) {
            System.out.println("Error reading " + path + ": " + e.getMessage());
        }
    }
}
