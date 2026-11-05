import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.zip.*;
import java.util.regex.*;

public class InspectPptxReview3 {
    public static void main(String[] args) throws Exception {
        String path = "docs/Application Development 2017_03_Review 3_OOP.pptx";
        try (ZipFile zip = new ZipFile(path)) {
            // Find slides
            int slideNum = 1;
            while (true) {
                ZipEntry entry = zip.getEntry("ppt/slides/slide" + slideNum + ".xml");
                if (entry == null) break;
                String xml = new String(zip.getInputStream(entry).readAllBytes(), StandardCharsets.UTF_8);
                Pattern p = Pattern.compile("<a:t>(.*?)</a:t>");
                Matcher m = p.matcher(xml);
                StringBuilder sb = new StringBuilder();
                while (m.find()) {
                    sb.append(m.group(1)).append(" ");
                }
                String text = sb.toString().replaceAll("\\s+", " ").trim();
                System.out.printf("Slide %d: %s%n", slideNum, text);
                slideNum++;
            }
        }
    }
}
