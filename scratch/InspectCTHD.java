import java.io.File;
import java.nio.file.Files;

public class InspectCTHD {
    public static void main(String[] args) throws Exception {
        System.out.println("=== ChiTietHoaDonSanPham ===");
        System.out.println(Files.readString(new File("src/com/petshop/entity/ChiTietHoaDonSanPham.java").toPath()));
        System.out.println("=== ChiTietHoaDonDichVu ===");
        System.out.println(Files.readString(new File("src/com/petshop/entity/ChiTietHoaDonDichVu.java").toPath()));
    }
}
