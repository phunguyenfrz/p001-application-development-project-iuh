import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.zip.*;
import java.util.regex.*;

public class InspectReview2Pptx {
    public static void main(String[] args) throws Exception {
        String path = "C:/Users/tranl/.gemini/antigravity/brain/470a6bf6-01df-4638-b6dc-8e91ae29e780/.user_uploaded/media_1793848454529_3911bbc3.pptx";
        try (ZipFile zip = new ZipFile(path)) {
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
