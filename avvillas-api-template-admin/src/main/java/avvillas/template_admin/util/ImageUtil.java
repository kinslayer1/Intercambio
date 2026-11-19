package avvillas.template_admin.util;

import avvillas.template_admin.service.dto.object.response.ObjectImageResponseDTO;
import avvillas.template_admin.web.controller.exception.PdfProcessingException;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Color;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;

public class ImageUtil {
    private ImageUtil() {}

    private static final String ERROR_IMAGE = "Error al procesar la imagen: ";
    private static final String ERROR_BASE64 = "La cadena Base64 está vacía o es nula.";
    private static final String IMAGE_NOT_FOUND = "No se pudo leer la imagen. Los datos están corruptos o no tienen el formato esperado.";

    public static String buildImageHtml(ObjectImageResponseDTO imageObject, String styles) {
        try {
            String base64Image = imageObject.getImage();

            if (base64Image == null || base64Image.isEmpty())
                throw new PdfProcessingException(ERROR_BASE64);

            if (base64Image.startsWith("data:image/"))
                base64Image = base64Image.substring(base64Image.indexOf(",") + 1);

            byte[] decodedBytes = Base64.getDecoder().decode(base64Image);

            BufferedImage originalImage = ImageIO.read(new ByteArrayInputStream(decodedBytes));
            if (originalImage == null) throw new PdfProcessingException(IMAGE_NOT_FOUND);

            Dimension targetDimensions = calculateNewDimensions(originalImage);
            int targetWidth = targetDimensions.width;
            int targetHeight = targetDimensions.height;

            BufferedImage resizedImage = resizeImage(originalImage, targetWidth, targetHeight);

            byte[] compressedImageBytes = compressImage(resizedImage);

            String optimizedBase64Image = Base64.getEncoder().encodeToString(compressedImageBytes);

            return "<img src='data:image/jpeg;base64," + optimizedBase64Image + "' style='max-width: 100%; " + styles + "' />";

        } catch (Exception e) {
            throw new PdfProcessingException(ERROR_IMAGE + e.getMessage(), e);
        }
    }

    private static BufferedImage resizeImage(BufferedImage originalImage, int targetWidth, int targetHeight) {
        BufferedImage resizedImage = new BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_RGB);

        Graphics2D graphics = resizedImage.createGraphics();

        graphics.setColor(Color.WHITE);
        graphics.fillRect(0, 0, targetWidth, targetHeight);

        graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        graphics.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        graphics.drawImage(originalImage, 0, 0, targetWidth, targetHeight, null);

        graphics.dispose();
        return resizedImage;
    }


    private static Dimension calculateNewDimensions(BufferedImage originalImage) {
        int originalWidth = originalImage.getWidth();
        int originalHeight = originalImage.getHeight();

        double widthRatio = (double) 500 / originalWidth;
        double heightRatio = (double) 500 / originalHeight;
        double scaleFactor = Math.min(widthRatio, heightRatio);

        int newWidth = (int) (originalWidth * scaleFactor);
        int newHeight = (int) (originalHeight * scaleFactor);

        return new Dimension(newWidth, newHeight);
    }

    private static byte[] compressImage(BufferedImage image) throws IOException {
        ByteArrayOutputStream compressedOutputStream = new ByteArrayOutputStream();
        ImageWriter writer = ImageIO.getImageWritersByFormatName("jpeg").next();
        ImageOutputStream ios = ImageIO.createImageOutputStream(compressedOutputStream);
        writer.setOutput(ios);

        ImageWriteParam param = writer.getDefaultWriteParam();
        if (param.canWriteCompressed()) {
            param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
            param.setCompressionQuality(0.5f);
        }

        writer.write(null, new IIOImage(image, null, null), param);
        ios.close();
        writer.dispose();

        return compressedOutputStream.toByteArray();
    }
}