import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.zip.*;
import java.util.regex.*;

public class FindClassDiagramImage {
    public static void main(String[] args) throws Exception {
        ZipFile zip = new ZipFile("docs/9_REPORT_ApplicationDevelopment.docx");
        ZipEntry entry = zip.getEntry("word/document.xml");
        InputStream is = zip.getInputStream(entry);
        String xml = new String(is.readAllBytes(), StandardCharsets.UTF_8);
        zip.close();

        int pos = xml.indexOf("Class Diagram ( Sơ đồ lớp )");
        int idxDrawing = xml.indexOf("<w:drawing>", pos);
        int endDrawing = xml.indexOf("</w:p>", idxDrawing) + 6;

        String drawingXml = xml.substring(idxDrawing, endDrawing);
        System.out.println("Drawing XML snippet: " + drawingXml.substring(0, Math.min(500, drawingXml.length())));

        Matcher m = Pattern.compile("r:embed=\"([^\"]+)\"").matcher(drawingXml);
        if (m.find()) {
            String rId = m.group(1);
            System.out.println("rId: " + rId);

            // Look up in document.xml.rels
            ZipFile zipRels = new ZipFile("docs/9_REPORT_ApplicationDevelopment.docx");
            ZipEntry relsEntry = zipRels.getEntry("word/_rels/document.xml.rels");
            String relsXml = new String(zipRels.getInputStream(relsEntry).readAllBytes(), StandardCharsets.UTF_8);
            zipRels.close();

            Matcher mRel = Pattern.compile("Id=\"" + rId + "\"[^>]*?Target=\"([^\"]+)\"").matcher(relsXml);
            if (mRel.find()) {
                System.out.println("Target image: " + mRel.group(1));
            }
        }
    }
}
