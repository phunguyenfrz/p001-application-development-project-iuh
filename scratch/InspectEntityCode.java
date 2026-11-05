import java.io.File;
import java.nio.file.Files;

public class InspectEntityCode {
    public static void main(String[] args) throws Exception {
        readCode("src/com/petshop/entity/HoaDon.java");
        readCode("src/com/petshop/entity/ChiTietHoaDonSanPham.java");
        readCode("src/com/petshop/entity/ChiTietHoaDonDichVu.java");
        readCode("src/com/petshop/entity/PhieuDoiTra.java");
        readCode("src/com/petshop/entity/ChiTietPhieuDoiTra.java");
    }

    static void readCode(String path) {
        System.out.println("=== " + path + " ===");
        try {
            File f = new File(path);
            if (!f.exists()) {
                System.out.println("FILE NOT FOUND");
                return;
            }
            String s = Files.readString(f.toPath());
            // Print fields and method signatures
            for (String line : s.split("\n")) {
                line = line.trim();
                if (line.startsWith("private ") || line.startsWith("public ") && line.contains("(")) {
                    System.out.println("  " + line);
                }
            }
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }
}
