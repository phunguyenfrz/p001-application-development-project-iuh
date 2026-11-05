import java.io.*;
import java.nio.file.*;
import java.util.zip.*;

public class CheckAndApply {
    public static void main(String[] args) throws Exception {
        File docFile = new File("docs/9_REPORT_ApplicationDevelopment.docx");
        File tmpFile = new File("scratch/temp_fixed_final.docx");
        File newImgFile = new File("docs/diagrams/Class_Diagram_Refactored.png");

        byte[] newImgBytes = Files.readAllBytes(newImgFile.toPath());
        System.out.println("New image size: " + newImgBytes.length + " bytes");

        try (ZipInputStream zin = new ZipInputStream(new FileInputStream(docFile));
             ZipOutputStream zout = new ZipOutputStream(new FileOutputStream(tmpFile))) {

            ZipEntry inEntry;
            while ((inEntry = zin.getNextEntry()) != null) {
                String name = inEntry.getName();
                ZipEntry outEntry = new ZipEntry(name);
                zout.putNextEntry(outEntry);

                if (name.equals("word/media/image35.png")) {
                    zout.write(newImgBytes);
                    System.out.println("Replaced word/media/image35.png!");
                } else {
                    byte[] buf = new byte[8192];
                    int r;
                    while ((r = zin.read(buf)) != -1) zout.write(buf, 0, r);
                }
                zout.closeEntry();
            }
        }

        System.out.println("Successfully created " + tmpFile.getPath());
        try {
            Files.copy(tmpFile.toPath(), docFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            System.out.println("SUCCESSFULLY updated " + docFile.getPath() + " directly!");
        } catch (Exception e) {
            System.out.println("File is locked by Word: " + e.getMessage());
            File fixedCopy = new File("docs/9_REPORT_ApplicationDevelopment_fixed.docx");
            Files.copy(tmpFile.toPath(), fixedCopy.toPath(), StandardCopyOption.REPLACE_EXISTING);
            System.out.println("Saved copy to: " + fixedCopy.getPath());
        }
    }
}
