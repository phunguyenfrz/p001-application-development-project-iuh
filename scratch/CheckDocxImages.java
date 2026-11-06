import java.io.*;
import java.util.zip.*;

public class CheckDocxImages {
    public static void main(String[] args) throws Exception {
        ZipFile zip = new ZipFile("docs/9_REPORT_ApplicationDevelopment.docx");
        ZipEntry entry = zip.getEntry("word/media/image35.png");
        if (entry != null) {
            System.out.println("image35.png size: " + entry.getSize() + " bytes");
        } else {
            System.out.println("image35.png NOT found!");
        }
        zip.close();

        File refactored = new File("docs/diagrams/Class_Diagram_Refactored.png");
        System.out.println("Refactored png size on disk: " + refactored.length() + " bytes");
    }
}
