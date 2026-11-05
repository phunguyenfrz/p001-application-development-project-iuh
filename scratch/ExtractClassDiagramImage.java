import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.zip.*;
import java.util.regex.*;

public class ExtractClassDiagramImage {
    public static void main(String[] args) throws Exception {
        ZipFile zip = new ZipFile("docs/9_REPORT_ApplicationDevelopment.docx");
        ZipEntry relsEntry = zip.getEntry("word/_rels/document.xml.rels");
        String relsXml = new String(zip.getInputStream(relsEntry).readAllBytes(), StandardCharsets.UTF_8);

        Matcher m = Pattern.compile("Id=\"rId46\"[^>]*?Target=\"([^\"]+)\"").matcher(relsXml);
        if (m.find()) {
            String target = m.group(1);
            System.out.println("rId46 target: " + target);

            String entryPath = "word/" + target;
            ZipEntry imgEntry = zip.getEntry(entryPath);
            if (imgEntry != null) {
                byte[] bytes = zip.getInputStream(imgEntry).readAllBytes();
                FileOutputStream fos = new FileOutputStream("scratch/current_class_diagram.png");
                fos.write(bytes);
                fos.close();
                System.out.println("Extracted scratch/current_class_diagram.png (size: " + bytes.length + " bytes)");
            }
        }
        zip.close();
    }
}
