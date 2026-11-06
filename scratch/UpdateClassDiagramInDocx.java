import java.io.*;
import java.nio.file.*;
import java.util.zip.*;

public class UpdateClassDiagramInDocx {
    public static void main(String[] args) throws Exception {
        File srcFile = new File("docs/9_REPORT_ApplicationDevelopment_fixed.docx");
        File tmpFile = new File("scratch/temp_doc_with_class_diagram.docx");
        File newImgFile = new File("docs/diagrams/Class_Diagram_Refactored.png");

        byte[] newImgBytes = Files.readAllBytes(newImgFile.toPath());
        System.out.println("New image size: " + newImgBytes.length + " bytes");

        try (ZipInputStream zin = new ZipInputStream(new FileInputStream(srcFile));
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

        Files.copy(tmpFile.toPath(), srcFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
        System.out.println("Updated " + srcFile.getPath() + "!");

        // Also try copying to docs/9_REPORT_ApplicationDevelopment.docx
        File targetFile = new File("docs/9_REPORT_ApplicationDevelopment.docx");
        try {
            Files.copy(srcFile.toPath(), targetFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            System.out.println("SUCCESSFULLY updated " + targetFile.getPath() + " directly!");
        } catch (Exception e) {
            System.out.println("Target file locked by Word. Please close Word and run apply_fixed_report.ps1.");
        }
    }
}
