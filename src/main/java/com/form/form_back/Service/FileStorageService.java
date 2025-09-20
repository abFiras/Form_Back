package com.form.form_back.Service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.UUID;

@Service
public class FileStorageService {

    @Value("${file.upload-dir:uploads}")
    private String uploadDir;

    /**
     * Sauvegarde une image base64 (signatures, dessins)
     */
    public String saveBase64Image(String base64Data, String subfolder, String filename) {
        try {
            // Extraire le type MIME et les données
            String[] parts = base64Data.split(",");
            if (parts.length != 2) {
                throw new IllegalArgumentException("Format base64 invalide");
            }

            String mimeType = parts[0].split(":")[1].split(";")[0];
            String extension = getExtensionFromMimeType(mimeType);
            byte[] data = Base64.getDecoder().decode(parts[1]);

            // Créer le chemin de destination
            Path directory = Paths.get(uploadDir, subfolder);
            Files.createDirectories(directory);

            String finalFilename = filename + extension;
            Path filePath = directory.resolve(finalFilename);

            // Sauvegarder le fichier
            Files.write(filePath, data);

            // Retourner l'URL relative
            return "/" + subfolder + "/" + finalFilename;

        } catch (Exception e) {
            throw new RuntimeException("Erreur lors de la sauvegarde de l'image: " + e.getMessage(), e);
        }
    }

    /**
     * Sauvegarde un fichier base64 général
     */
    public String saveBase64File(String base64Data, String subfolder, String filename) {
        try {
            String[] parts = base64Data.split(",");
            if (parts.length != 2) {
                throw new IllegalArgumentException("Format base64 invalide");
            }

            String mimeType = parts[0].split(":")[1].split(";")[0];
            String extension = getExtensionFromMimeType(mimeType);
            byte[] data = Base64.getDecoder().decode(parts[1]);

            Path directory = Paths.get(uploadDir, subfolder);
            Files.createDirectories(directory);

            String finalFilename = filename + extension;
            Path filePath = directory.resolve(finalFilename);

            Files.write(filePath, data);

            return "/" + subfolder + "/" + finalFilename;

        } catch (Exception e) {
            throw new RuntimeException("Erreur lors de la sauvegarde du fichier: " + e.getMessage(), e);
        }
    }

    /**
     * Sauvegarde un enregistrement audio base64
     */
    public String saveBase64Audio(String base64Data, String subfolder, String filename) {
        try {
            String[] parts = base64Data.split(",");
            if (parts.length != 2) {
                throw new IllegalArgumentException("Format base64 invalide");
            }

            String mimeType = parts[0].split(":")[1].split(";")[0];
            String extension = getExtensionFromMimeType(mimeType);
            byte[] data = Base64.getDecoder().decode(parts[1]);

            Path directory = Paths.get(uploadDir, subfolder);
            Files.createDirectories(directory);

            String finalFilename = filename + extension;
            Path filePath = directory.resolve(finalFilename);

            Files.write(filePath, data);

            return "/" + subfolder + "/" + finalFilename;

        } catch (Exception e) {
            throw new RuntimeException("Erreur lors de la sauvegarde de l'audio: " + e.getMessage(), e);
        }
    }

    /**
     * Obtenir l'extension de fichier à partir du type MIME
     */
    private String getExtensionFromMimeType(String mimeType) {
        switch (mimeType.toLowerCase()) {
            // Images
            case "image/png": return ".png";
            case "image/jpeg": return ".jpg";
            case "image/jpg": return ".jpg";
            case "image/gif": return ".gif";
            case "image/webp": return ".webp";
            case "image/svg+xml": return ".svg";

            // Audio
            case "audio/mpeg": return ".mp3";
            case "audio/wav": return ".wav";
            case "audio/ogg": return ".ogg";
            case "audio/m4a": return ".m4a";
            case "audio/webm": return ".webm";

            // Documents
            case "application/pdf": return ".pdf";
            case "text/plain": return ".txt";
            case "application/msword": return ".doc";
            case "application/vnd.openxmlformats-officedocument.wordprocessingml.document": return ".docx";
            case "application/vnd.ms-excel": return ".xls";
            case "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet": return ".xlsx";

            // Vidéo
            case "video/mp4": return ".mp4";
            case "video/webm": return ".webm";
            case "video/ogg": return ".ogv";

            default: return ".bin"; // Extension par défaut pour types inconnus
        }
    }

    /**
     * Générer un nom de fichier unique
     */
    public String generateUniqueFilename(String originalName) {
        String uuid = UUID.randomUUID().toString();
        if (originalName != null && originalName.contains(".")) {
            String extension = originalName.substring(originalName.lastIndexOf("."));
            return uuid + extension;
        }
        return uuid;
    }

    /**
     * Supprimer un fichier
     */
    public boolean deleteFile(String relativePath) {
        try {
            Path filePath = Paths.get(uploadDir).resolve(relativePath.startsWith("/") ?
                    relativePath.substring(1) : relativePath);
            return Files.deleteIfExists(filePath);
        } catch (Exception e) {
            System.err.println("Erreur lors de la suppression du fichier: " + e.getMessage());
            return false;
        }
    }

    /**
     * Vérifier si un fichier existe
     */
    public boolean fileExists(String relativePath) {
        try {
            Path filePath = Paths.get(uploadDir).resolve(relativePath.startsWith("/") ?
                    relativePath.substring(1) : relativePath);
            return Files.exists(filePath);
        } catch (Exception e) {
            return false;
        }
    }
}