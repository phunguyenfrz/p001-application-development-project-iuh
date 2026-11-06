import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;

public class CheckImgDim {
    public static void main(String[] args) throws Exception {
        BufferedImage img = ImageIO.read(new File("C:/Users/tranl/.gemini/antigravity/brain/470a6bf6-01df-4638-b6dc-8e91ae29e780/.user_uploaded/media_1793866488730_11a45486.jpg"));
        if (img != null) {
            System.out.printf("Image dimensions: %d x %d%n", img.getWidth(), img.getHeight());
        }
    }
}
