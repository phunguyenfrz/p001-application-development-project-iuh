import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.zip.*;
import java.util.regex.*;

public class FindAllImagesInSection {
    public static void main(String[] args) throws Exception {
        ZipFile zip = new ZipFile("docs/9_REPORT_ApplicationDevelopment.docx");
        ZipEntry entry = zip.getEntry("word/document.xml");
        InputStream is = zip.getInputStream(entry);
        String xml = new String(is.readAllBytes(), StandardCharsets.UTF_8);
        zip.close();

        int pos = 3961000;
        int nextSec = xml.indexOf("Chương", pos + 100);
        if (nextSec == -1) nextSec = xml.length();

        String sub = xml.substring(pos, Math.min(xml.length(), pos + 30000));
        Matcher m = Pattern.compile("r:embed=\"([^\"]+)\"").matcher(sub);
        while (m.find()) {
            System.out.println("Found r:embed: " + m.group(1));
        }

        // Also check if there are any drawings or shapes
        Matcher mDraw = Pattern.compile("<w:drawing>.*?</w:drawing>").matcher(sub);
        int d = 0;
        while (mDraw.find()) {
            d++;
            System.out.printf("Drawing #%d (len %d): %s%n", d, mDraw.group().length(), mDraw.group().substring(0, Math.min(100, mDraw.group().length())));
        }
    }
}
