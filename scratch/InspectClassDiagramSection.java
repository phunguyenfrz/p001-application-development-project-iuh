import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.zip.*;
import java.util.regex.*;

public class InspectClassDiagramSection {
    public static void main(String[] args) throws Exception {
        ZipFile zip = new ZipFile("docs/9_REPORT_ApplicationDevelopment.docx");
        ZipEntry entry = zip.getEntry("word/document.xml");
        InputStream is = zip.getInputStream(entry);
        String xml = new String(is.readAllBytes(), StandardCharsets.UTF_8);
        zip.close();

        int pos = xml.indexOf("I.Thiết kế sơ đồ lớp");
        if (pos == -1) pos = xml.indexOf("Thiết kế sơ đồ lớp");
        // We want the occurrence around 3960000
        int pos2 = xml.indexOf("Class Diagram", 3950000);
        System.out.println("pos2: " + pos2);

        int start = Math.max(0, pos2 - 500);
        int end = Math.min(xml.length(), pos2 + 10000);

        String sub = xml.substring(start, end);
        Pattern pP = Pattern.compile("<w:p[ >].*?</w:p>");
        Matcher mP = pP.matcher(sub);
        int p = 0;
        while (mP.find()) {
            p++;
            String pXml = mP.group();
            String text = pXml.replaceAll("<[^>]+>", " ").replaceAll("\\s+", " ").trim();
            boolean hasDrawing = pXml.contains("<w:drawing>");
            if (!text.isEmpty() || hasDrawing) {
                System.out.printf("P[%d]%s: %s%n", p, (hasDrawing ? " [DRAWING]" : ""), text);
            }
        }
    }
}
