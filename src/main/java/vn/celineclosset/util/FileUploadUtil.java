package vn.celineclosset.util;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.Part;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Locale;
import java.util.UUID;

/** Hàm tải ảnh dùng chung cho avatar và sản phẩm. */
public final class FileUploadUtil {
    private FileUploadUtil() {
    }

    public static String uploadImage(HttpServletRequest req, String partName,
                                     String filePrefix, String uploadFolder)
            throws IOException, ServletException {
        Part part = req.getPart(partName);
        if (part == null || part.getSize() <= 0 || part.getSubmittedFileName() == null
                || part.getSubmittedFileName().isBlank()) {
            return null;
        }

        String originalName = Paths.get(part.getSubmittedFileName()).getFileName().toString();
        String extension = extensionOf(originalName);
        if (!isAllowedImage(extension)) {
            throw new ServletException("Chỉ cho phép tải ảnh JPG, JPEG, PNG, GIF hoặc WEBP.");
        }

        String realPath = req.getServletContext().getRealPath(uploadFolder);
        if (realPath == null) {
            throw new ServletException("Không xác định được thư mục lưu ảnh trên Tomcat.");
        }

        String fileName = filePrefix + "-" + System.currentTimeMillis() + "-"
                + UUID.randomUUID().toString().substring(0, 8) + extension;
        Path folder = Path.of(realPath);
        Files.createDirectories(folder);

        try (InputStream inputStream = part.getInputStream()) {
            Files.copy(inputStream, folder.resolve(fileName), StandardCopyOption.REPLACE_EXISTING);
        }
        return uploadFolder.substring(1) + "/" + fileName;
    }

    private static String extensionOf(String fileName) {
        int dot = fileName.lastIndexOf('.');
        return dot < 0 ? "" : fileName.substring(dot).toLowerCase(Locale.ROOT);
    }

    private static boolean isAllowedImage(String extension) {
        return extension.equals(".jpg") || extension.equals(".jpeg") || extension.equals(".png")
                || extension.equals(".gif") || extension.equals(".webp");
    }
}
