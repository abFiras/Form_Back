package com.form.form_back.Service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.form.form_back.dto.*;
import org.apache.poi.util.Units;
import org.apache.poi.xwpf.usermodel.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class WordGeneratorService {

    @Autowired
    private ExternalListService externalListService;

    private static final Logger logger = LoggerFactory.getLogger(WordGeneratorService.class);

    public byte[] generateFormDocument(FormDTO form) throws Exception {
        // Utiliser Apache POI pour créer le document Word
        XWPFDocument document = new XWPFDocument();

        // Titre du formulaire
        XWPFParagraph titleParagraph = document.createParagraph();
        titleParagraph.setAlignment(ParagraphAlignment.CENTER);
        XWPFRun titleRun = titleParagraph.createRun();
        titleRun.setText(form.getName());
        titleRun.setBold(true);
        titleRun.setFontSize(18);

        // Secteur
        if (form.getSecteur() != null) {
            XWPFParagraph sectorParagraph = document.createParagraph();
            XWPFRun sectorRun = sectorParagraph.createRun();
            sectorRun.setText("Secteur: " + form.getSecteur());
            sectorRun.setBold(true);
            sectorRun.setColor("FF0000");
        }

        // Description
        if (form.getDescription() != null) {
            XWPFParagraph descParagraph = document.createParagraph();
            XWPFRun descRun = descParagraph.createRun();
           // descRun.setText("Description: " + form.getDescription());
        }

        // Espacement
        document.createParagraph();

        // Champs du formulaire
        for (FormFieldDTO field : form.getFields()) {
            generateFieldInDocument(document, field);
        }

        // Convertir en byte array
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        document.write(out);
        document.close();

        return out.toByteArray();
    }

    private void generateFieldInDocument(XWPFDocument document, FormFieldDTO field) {
        // Label du champ
        XWPFParagraph labelParagraph = document.createParagraph();
        XWPFRun labelRun = labelParagraph.createRun();
        labelRun.setText(field.getLabel() + (field.getRequired() ? " *" : ""));
        labelRun.setBold(true);

        // Zone de réponse selon le type de champ
        switch (field.getType()) {
            case "text":
            case "email":
                createTextResponseLine(document);
                break;
            case "textarea":
                createTextAreaResponse(document);
                break;
            case "select":
            case "radio":
                createOptionsResponse(document, field.getOptions());
                break;
            case "checkbox":
                createCheckboxResponse(document, field.getOptions());
                break;
            case "datetime":
                createDateTimeResponse(document);
                break;
            default:
                createTextResponseLine(document);
                break;
        }

        // Espacement entre les champs
        document.createParagraph();
    }

    private void createTextResponseLine(XWPFDocument document) {
        XWPFParagraph responseParagraph = document.createParagraph();
        XWPFRun responseRun = responseParagraph.createRun();
        responseRun.setText("Réponse: ________________________________");
    }

    private void createTextAreaResponse(XWPFDocument document) {
        for (int i = 0; i < 4; i++) {
            XWPFParagraph responseParagraph = document.createParagraph();
            XWPFRun responseRun = responseParagraph.createRun();
            responseRun.setText("________________________________");
        }
    }

    private void createOptionsResponse(XWPFDocument document, List<FieldOptionDTO> options) {
        if (options != null) {
            for (FieldOptionDTO option : options) {
                XWPFParagraph optionParagraph = document.createParagraph();
                XWPFRun optionRun = optionParagraph.createRun();
                optionRun.setText("☐ " + option.getLabel());
            }
        }
    }

    private void createCheckboxResponse(XWPFDocument document, List<FieldOptionDTO> options) {
        if (options != null) {
            for (FieldOptionDTO option : options) {
                XWPFParagraph optionParagraph = document.createParagraph();
                XWPFRun optionRun = optionParagraph.createRun();
                optionRun.setText("☐ " + option.getLabel());
            }
        }
    }

    private void createDateTimeResponse(XWPFDocument document) {
        XWPFParagraph responseParagraph = document.createParagraph();
        XWPFRun responseRun = responseParagraph.createRun();
        responseRun.setText("Date: ___/___/______ Heure: ___:___");
    }
    public byte[] generateSubmissionDocument(FormDTO form, FormSubmissionResponseDTO submission) throws Exception {
        XWPFDocument document = new XWPFDocument();

        // Titre du document
        XWPFParagraph titleParagraph = document.createParagraph();
        titleParagraph.setAlignment(ParagraphAlignment.CENTER);
        XWPFRun titleRun = titleParagraph.createRun();
        titleRun.setText(form.getName() + " - Soumission complétée");
        titleRun.setBold(true);
        titleRun.setFontSize(18);

        // Secteur et description
        if (form.getSecteur() != null) {
            XWPFParagraph sectorParagraph = document.createParagraph();
            XWPFRun sectorRun = sectorParagraph.createRun();
            sectorRun.setText("Secteur: " + form.getSecteur());
            sectorRun.setBold(true);
            sectorRun.setColor("FF0000");
        }

        if (form.getDescription() != null) {
            XWPFParagraph descParagraph = document.createParagraph();
            XWPFRun descRun = descParagraph.createRun();
            //descRun.setText("Description: " + form.getDescription());
        }

        // Informations de soumission
        document.createParagraph();
        XWPFParagraph infoParagraph = document.createParagraph();
        XWPFRun infoRun = infoParagraph.createRun();
        infoRun.setText("═══ INFORMATIONS DE SOUMISSION ═══");
        infoRun.setBold(true);
        infoRun.setFontSize(14);

        // Détails de soumission
        addSubmissionInfo(document, "ID de soumission:", "#" + submission.getId());
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

// Conversion LocalDateTime -> String
        String submittedAtStr = submission.getSubmittedAt().format(formatter);

// Utilisation
        addSubmissionInfo(document, "Date de soumission:", submittedAtStr);
        if (submission.getSubmitterName() != null) {
            addSubmissionInfo(document, "Soumis par:", submission.getSubmitterName());
        }
        if (submission.getSubmitterEmail() != null) {
            addSubmissionInfo(document, "Email:", submission.getSubmitterEmail());
        }

        document.createParagraph();

        // Données du formulaire
        XWPFParagraph dataParagraph = document.createParagraph();
        XWPFRun dataRun = dataParagraph.createRun();
        dataRun.setText("═══ RÉPONSES DU FORMULAIRE ═══");
        dataRun.setBold(true);
        dataRun.setFontSize(14);

        // Parcourir les champs avec leurs réponses
        for (FormFieldDTO field : form.getFields()) {
            generateFieldWithValueInDocument(document, field, submission.getData());
        }

        // Convertir en byte array
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        document.write(out);
        document.close();

        return out.toByteArray();
    }

    private void addSubmissionInfo(XWPFDocument document, String label, String value) {
        XWPFParagraph paragraph = document.createParagraph();
        XWPFRun labelRun = paragraph.createRun();
        labelRun.setText(label + " ");
        labelRun.setBold(true);

        XWPFRun valueRun = paragraph.createRun();
        valueRun.setText(value);
    }

// Ajoutez ces imports en haut de votre WordGeneratorService.java


// Dans votre méthode generateFieldWithValueInDocument, remplacez le contenu par :

    private void generateFieldWithValueInDocument(XWPFDocument document, FormFieldDTO field, Map<String, Object> data) {
        // Label du champ
        XWPFParagraph labelParagraph = document.createParagraph();
        XWPFRun labelRun = labelParagraph.createRun();
        labelRun.setText(field.getLabel() + (field.getRequired() ? " *" : ""));
        labelRun.setBold(true);

        // Récupérer la valeur soumise
        Object value = data.get(field.getFieldName());

        // Traitement selon le type de champ
        switch (field.getType()) {
            case "signature":
            case "drawing":
                handleImageField(document, field, value, field.getType());
                break;

            case "table":
                handleTableField(document, field, value);
                break;

            case "image":
                handleStaticImageField(document, field, value);
                break;

            case "address":
                handleAddressField(document, field, value);
                break;

            case "contact":
                handleContactField(document, field, value);
                break;

            case "checkbox":
                handleCheckboxField(document, field, value);
                break;

            case "external-list":
                handleExternalListField(document, field, value);
                break;

            default:
                handleDefaultField(document, field, value);
                break;
        }

        // Espacement entre les champs
        document.createParagraph();
    }

    // ✅ NOUVELLE MÉTHODE : Gérer les signatures et dessins
    private void handleImageField(XWPFDocument document, FormFieldDTO field, Object value, String fieldType) {
        XWPFParagraph responseParagraph = document.createParagraph();

        // DEBUG: Ajoutez ces logs temporaires pour voir la structure des données
        logger.info("=== DEBUG SIGNATURE/DRAWING ===");
        logger.info("Field name: {}", field.getFieldName());
        logger.info("Field type: {}", fieldType);
        logger.info("Value class: {}", value != null ? value.getClass().getSimpleName() : "null");
        logger.info("Value: {}", value);

        if (value instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, Object> map = (Map<String, Object>) value;
            logger.info("Map keys: {}", map.keySet());
            for (Map.Entry<String, Object> entry : map.entrySet()) {
                logger.info("  {}: {} ({})", entry.getKey(), entry.getValue(),
                        entry.getValue() != null ? entry.getValue().getClass().getSimpleName() : "null");
            }
        }
        logger.info("=== END DEBUG ===");

        if (value != null) {
            try {
                String imageData = null;
                String imageUrl = null;

                // Cas 1: Map avec différentes structures possibles
                if (value instanceof Map) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> dataMap = (Map<String, Object>) value;

                    // Vérifier plusieurs clés possibles
                    imageData = (String) dataMap.get("data");
                    imageUrl = (String) dataMap.get("url");

                    // Si pas de 'data' ou 'url', vérifier d'autres clés possibles
                    if (imageData == null && imageUrl == null) {
                        imageData = (String) dataMap.get("imageData");
                        imageUrl = (String) dataMap.get("imageUrl");
                    }

                    // Si toujours rien, prendre la première valeur String qui ressemble à une image
                    if (imageData == null && imageUrl == null) {
                        for (Object val : dataMap.values()) {
                            if (val instanceof String) {
                                String strVal = (String) val;
                                if (strVal.startsWith("data:image/") || strVal.startsWith("/uploads/")) {
                                    imageData = strVal;
                                    break;
                                }
                            }
                        }
                    }
                }
                // Cas 2: String directe
                else if (value instanceof String) {
                    String strValue = (String) value;
                    if (strValue.startsWith("data:image/") || strValue.startsWith("/uploads/")) {
                        imageData = strValue;
                    }
                }

                // Traitement de l'image si on a trouvé des données
                String finalImageData = imageData != null ? imageData : imageUrl;

                if (finalImageData != null && !finalImageData.trim().isEmpty()) {
                    logger.info("Found image data: {}", finalImageData.substring(0, Math.min(50, finalImageData.length())) + "...");

                    // Traiter les données base64
                    if (finalImageData.startsWith("data:image/")) {
                        try {
                            String[] parts = finalImageData.split(",");
                            if (parts.length == 2) {
                                byte[] imageBytes = Base64.getDecoder().decode(parts[1]);

                                XWPFRun imageRun = responseParagraph.createRun();
                                String icon = fieldType.equals("signature") ? "✍️" : "🎨";
                                imageRun.setText(icon + " " +
                                        (fieldType.equals("signature") ? "Signature capturée :" : "Dessin capturé :"));
                                imageRun.setBold(true);
                                imageRun.addBreak();

                                try (ByteArrayInputStream bis = new ByteArrayInputStream(imageBytes)) {
                                    imageRun.addPicture(bis, XWPFDocument.PICTURE_TYPE_PNG,
                                            "image.png", Units.toEMU(250), Units.toEMU(150));
                                }

                                logger.info("Image successfully inserted for field: {}", field.getFieldName());
                                return;
                            }
                        } catch (Exception e) {
                            logger.error("Erreur traitement base64 pour {}: {}", field.getFieldName(), e.getMessage());
                        }
                    }

                    // Dans handleImageField, remplacez la section de traitement des fichiers par :

// Traiter les URLs de fichier (/uploads/, /signatures/, /drawings/)
                    else if (finalImageData.startsWith("/uploads/") ||
                            finalImageData.startsWith("/signatures/") ||
                            finalImageData.startsWith("/drawings/")) {
                        try {
                            // Essayer plusieurs chemins possibles selon votre configuration
                            String[] possiblePaths = {
                                    "." + finalImageData,                    // ./signatures/...
                                    "uploads" + finalImageData,              // uploads/signatures/...
                                    "src/main/resources/static" + finalImageData, // src/main/resources/static/signatures/...
                                    System.getProperty("user.dir") + finalImageData, // chemin absolu
                                    "static" + finalImageData                // static/signatures/...
                            };

                            java.io.File imageFile = null;

                            // Tester chaque chemin jusqu'à trouver le fichier
                            for (String path : possiblePaths) {
                                java.io.File testFile = new java.io.File(path);
                                logger.debug("Testing path: {}", testFile.getAbsolutePath());
                                if (testFile.exists()) {
                                    imageFile = testFile;
                                    logger.info("Found image file at: {}", testFile.getAbsolutePath());
                                    break;
                                }
                            }

                            if (imageFile != null && imageFile.exists()) {
                                XWPFRun imageRun = responseParagraph.createRun();
                                String icon = fieldType.equals("signature") ? "✍️" : "🎨";
                                imageRun.setText(icon + " " +
                                        (fieldType.equals("signature") ? "Signature capturée :" : "Dessin capturé :"));
                                imageRun.setBold(true);
                                imageRun.addBreak();

                                try (java.io.FileInputStream fis = new java.io.FileInputStream(imageFile)) {
                                    imageRun.addPicture(fis, XWPFDocument.PICTURE_TYPE_PNG,
                                            imageFile.getName(), Units.toEMU(250), Units.toEMU(150));
                                }

                                // Ajouter les informations du fichier
                                imageRun.addBreak();
                                XWPFRun infoRun = responseParagraph.createRun();
                                infoRun.setText("Image " + (fieldType.equals("signature") ? "signature" : "dessin") + " - Cliquez pour agrandir");
                                infoRun.setItalic(true);
                                infoRun.setFontSize(9);
                                infoRun.setColor("666666");

                                logger.info("File image successfully inserted for field: {}", field.getFieldName());
                                return;
                            } else {
                                logger.warn("Image file not found in any of the tested paths for: {}", finalImageData);

                                // Log tous les chemins testés pour debug
                                for (String path : possiblePaths) {
                                    logger.debug("Path tested: {} - exists: {}", path, new java.io.File(path).exists());
                                }

                                // Fallback : afficher les informations même si le fichier n'existe pas
                                XWPFRun imageRun = responseParagraph.createRun();
                                String icon = fieldType.equals("signature") ? "✍️" : "🎨";
                                imageRun.setText(icon + " " +
                                        (fieldType.equals("signature") ? "Signature enregistrée" : "Dessin enregistré"));
                                imageRun.setBold(true);
                                imageRun.addBreak();

                                XWPFRun pathRun = responseParagraph.createRun();
                                pathRun.setText("Fichier non accessible: " + finalImageData);
                                pathRun.setFontFamily("Courier New");
                                pathRun.setFontSize(9);
                                pathRun.setColor("FF6666"); // Rouge pour indiquer un problème

                                pathRun.addBreak();
                                XWPFRun helpRun = responseParagraph.createRun();
                                helpRun.setText("Vérifiez la configuration des chemins de fichiers");
                                helpRun.setItalic(true);
                                helpRun.setFontSize(8);
                                helpRun.setColor("999999");
                                return;
                            }
                        } catch (Exception e) {
                            logger.error("Erreur traitement fichier pour {}: {}", field.getFieldName(), e.getMessage());
                        }
                    }
                } else {
                    logger.warn("No valid image data found for field: {}", field.getFieldName());
                }
            } catch (Exception e) {
                logger.error("Erreur générale traitement image pour {}: {}", field.getFieldName(), e.getMessage());
            }
        } else {
            logger.warn("Value is null for field: {}", field.getFieldName());
        }

        // Fallback si aucune image n'a pu être traitée
        XWPFRun responseRun = responseParagraph.createRun();
        String icon = fieldType.equals("signature") ? "✍️" : "🎨";
        responseRun.setText(icon + " " +
                (fieldType.equals("signature") ? "Signature non disponible" : "Dessin non disponible"));
        responseRun.setItalic(true);
        responseRun.setColor("999999");

        logger.info("Fallback applied for field: {}", field.getFieldName());
    }


    private void handleSeparatorField(XWPFDocument document, FormFieldDTO field, Object value) {
        XWPFParagraph separatorParagraph = document.createParagraph();
        separatorParagraph.setAlignment(ParagraphAlignment.CENTER);
        XWPFRun separatorRun = separatorParagraph.createRun();

        // Créer une ligne de séparation visuelle
        separatorRun.setText("═══════════════════════════════════════════════════════════════════");
        separatorRun.setBold(true);
        separatorRun.setColor("666666");

        // Ajouter un espacement
        document.createParagraph();
    }

    // Modifiez la méthode handleDefaultField pour inclure tous les types manquants
    private void handleDefaultField(XWPFDocument document, FormFieldDTO field, Object value) {
        switch (field.getType()) {
            case "separator":
                handleSeparatorField(document, field, value);
                return;

            case "barcode":
            case "nfc":
                handleBarcodeField(document, field, value);
                return;

            case "file":
            case "attachment":
                // Déterminer si c'est une image ou un fichier
                if (isImageFile(value)) {
                    handleImageFileField(document, field, value);
                } else {
                    handleFileField(document, field, value);
                }
                return;

            case "file-fixed":
                handleFileFixedField(document, field, value);
                return;

            case "geolocation":
                handleGeolocationField(document, field, value);
                return;

            case "calculation":
                handleCalculationField(document, field, value);
                return;

            case "fixed-text":
                handleFixedTextField(document, field, value);
                return;

            default:
                handleStandardField(document, field, value);
                break;
        }
    }
    // Méthode pour déterminer si un fichier est une image
    private boolean isImageFile(Object value) {
        if (!(value instanceof Map)) return false;

        @SuppressWarnings("unchecked")
        Map<String, Object> fileData = (Map<String, Object>) value;
        String url = (String) fileData.get("url");

        if (url == null) return false;

        // Vérifier l'extension du fichier
        String lowerUrl = url.toLowerCase();
        return lowerUrl.endsWith(".png") ||
                lowerUrl.endsWith(".jpg") ||
                lowerUrl.endsWith(".jpeg") ||
                lowerUrl.endsWith(".gif") ||
                lowerUrl.endsWith(".bmp") ||
                lowerUrl.endsWith(".webp");
    }

    // Nouvelle méthode pour gérer les fichiers image (photos)
    private void handleImageFileField(XWPFDocument document, FormFieldDTO field, Object value) {
        XWPFParagraph responseParagraph = document.createParagraph();

        if (value != null && value instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, Object> fileData = (Map<String, Object>) value;

            String url = (String) fileData.get("url");
            Object originalSize = fileData.get("originalSize");
            String savedAt = (String) fileData.get("savedAt");

            if (url != null && !url.trim().isEmpty()) {
                XWPFRun imageRun = responseParagraph.createRun();
                imageRun.setText("📷 Photo téléchargée");
                imageRun.setBold(true);
                imageRun.addBreak();

                // Tentative de charger et afficher l'image
                boolean imageInserted = false;

                if (url.startsWith("/uploads/")) {
                    try {
                        // Construire le chemin complet du fichier
                        String fullPath = "." + url; // Ajustez selon votre configuration
                        java.io.File imageFile = new java.io.File(fullPath);

                        if (imageFile.exists()) {
                            try (java.io.FileInputStream fis = new java.io.FileInputStream(imageFile)) {
                                // Déterminer le type d'image
                                int pictureType = determinePictureType(url);

                                imageRun.addPicture(fis, pictureType,
                                        imageFile.getName(), Units.toEMU(300), Units.toEMU(200));
                                imageInserted = true;

                                // Ajouter les informations du fichier
                                imageRun.addBreak();
                                XWPFRun infoRun = responseParagraph.createRun();
                                infoRun.setText("Nom: " + extractFileNameFromUrl(url));

                                if (originalSize != null) {
                                    infoRun.addBreak();
                                    XWPFRun sizeRun = responseParagraph.createRun();
                                    sizeRun.setText("Taille: " + formatFileSize(((Number) originalSize).longValue()));
                                }

                                if (savedAt != null) {
                                    infoRun.addBreak();
                                    XWPFRun timeRun = responseParagraph.createRun();
                                    timeRun.setText("Téléchargée le: " + formatDateTime(savedAt));
                                    timeRun.setItalic(true);
                                    timeRun.setColor("666666");
                                }
                            }
                        }
                    } catch (Exception e) {
                        logger.error("Erreur insertion image fichier: {}", e.getMessage());
                    }
                }

                // Si l'image n'a pas pu être insérée, afficher les informations
                if (!imageInserted) {
                    XWPFRun fileRun = responseParagraph.createRun();
                    fileRun.setText("Nom: " + extractFileNameFromUrl(url));
                    // ... reste des informations comme dans handleFileField
                }

            } else {
                XWPFRun responseRun = responseParagraph.createRun();
                responseRun.setText("📷 Photo non disponible");
                responseRun.setItalic(true);
                responseRun.setColor("999999");
            }
        }
    }

    // Méthode pour déterminer le type d'image pour Apache POI
    private int determinePictureType(String url) {
        String lowerUrl = url.toLowerCase();

        if (lowerUrl.endsWith(".png")) {
            return XWPFDocument.PICTURE_TYPE_PNG;
        } else if (lowerUrl.endsWith(".jpg") || lowerUrl.endsWith(".jpeg")) {
            return XWPFDocument.PICTURE_TYPE_JPEG;
        } else if (lowerUrl.endsWith(".gif")) {
            return XWPFDocument.PICTURE_TYPE_GIF;
        } else if (lowerUrl.endsWith(".bmp")) {
            return XWPFDocument.PICTURE_TYPE_BMP;
        } else {
            return XWPFDocument.PICTURE_TYPE_PNG;
        }
    }
    // Nouvelle méthode pour gérer les codes-barres et NFC
    private void handleBarcodeField(XWPFDocument document, FormFieldDTO field, Object value) {
        XWPFParagraph responseParagraph = document.createParagraph();
        XWPFRun responseRun = responseParagraph.createRun();

        if (value != null && value instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, Object> barcodeData = (Map<String, Object>) value;

            String code = (String) barcodeData.get("code");
            String type = (String) barcodeData.get("type");
            String scannedAt = (String) barcodeData.get("scannedAt");

            if (code != null && !code.trim().isEmpty()) {
                // Ajouter une icône textuelle pour le code-barres
                responseRun.setText("📱 " + (field.getType().equals("barcode") ? "Code-barres scanné" : "Tag NFC scanné"));
                responseRun.setBold(true);
                responseRun.addBreak();

                // Afficher le code dans un format encadré
                XWPFRun codeRun = responseParagraph.createRun();
                codeRun.setText("┌─ Code: " + code + " ─┐");
                codeRun.setFontFamily("Courier New");
                codeRun.addBreak();

                if (scannedAt != null) {
                    XWPFRun timeRun = responseParagraph.createRun();
                    timeRun.setText("Scanné le: " + formatDateTime(scannedAt));
                    timeRun.setItalic(true);
                    timeRun.setColor("666666");
                }
            } else {
                responseRun.setText("📱 Code non disponible");
                responseRun.setItalic(true);
                responseRun.setColor("999999");
            }
        } else {
            responseRun.setText("📱 " + (field.getType().equals("barcode") ? "Code-barres" : "Tag NFC") + " non scanné");
            responseRun.setItalic(true);
            responseRun.setColor("999999");
        }
    }

    // Nouvelle méthode pour gérer les fichiers/pièces jointes
    private void handleFileField(XWPFDocument document, FormFieldDTO field, Object value) {
        XWPFParagraph responseParagraph = document.createParagraph();
        XWPFRun responseRun = responseParagraph.createRun();

        if (value != null && value instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, Object> fileData = (Map<String, Object>) value;

            String url = (String) fileData.get("url");
            Object originalSize = fileData.get("originalSize");
            String type = (String) fileData.get("type");
            String savedAt = (String) fileData.get("savedAt");

            if (url != null && !url.trim().isEmpty()) {
                // Icône de fichier
                responseRun.setText("📎 Fichier téléchargé");
                responseRun.setBold(true);
                responseRun.addBreak();

                // Nom du fichier extrait de l'URL
                String fileName = extractFileNameFromUrl(url);
                XWPFRun fileRun = responseParagraph.createRun();
                fileRun.setText("Nom: " + fileName);
                fileRun.addBreak();

                // Taille du fichier si disponible
                if (originalSize != null) {
                    XWPFRun sizeRun = responseParagraph.createRun();
                    sizeRun.setText("Taille: " + formatFileSize(((Number) originalSize).longValue()));
                    sizeRun.addBreak();
                }

                // Date de sauvegarde
                if (savedAt != null) {
                    XWPFRun timeRun = responseParagraph.createRun();
                    timeRun.setText("Téléchargé le: " + formatDateTime(savedAt));
                    timeRun.setItalic(true);
                    timeRun.setColor("666666");
                }

                // URL (raccourcie pour éviter les URLs trop longues)
                XWPFRun urlRun = responseParagraph.createRun();
                urlRun.setText("URL: " + (url.length() > 50 ? url.substring(0, 47) + "..." : url));
                urlRun.setFontFamily("Courier New");
                urlRun.setFontSize(9);
                urlRun.setColor("666666");

            } else {
                responseRun.setText("📎 Fichier non disponible");
                responseRun.setItalic(true);
                responseRun.setColor("999999");
            }
        } else {
            responseRun.setText("📎 Aucun fichier téléchargé");
            responseRun.setItalic(true);
            responseRun.setColor("999999");
        }
    }
    private void handleFileFixedField(XWPFDocument document, FormFieldDTO field, Object value) {
        XWPFParagraph responseParagraph = document.createParagraph();
        XWPFRun responseRun = responseParagraph.createRun();

        if (value != null && value instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, Object> fileData = (Map<String, Object>) value;

            String fileUrl = (String) fileData.get("fileUrl");
            String fileName = (String) fileData.get("fileName");

            if (fileUrl != null && fileUrl.startsWith("data:")) {
                // C'est un fichier encodé en base64
                responseRun.setText("📄 Fichier fixe configuré");
                responseRun.setBold(true);
                responseRun.addBreak();

                if (fileName != null) {
                    XWPFRun nameRun = responseParagraph.createRun();
                    nameRun.setText("Nom: " + fileName);
                    nameRun.addBreak();
                }

                // Déterminer le type de fichier depuis le data URL
                String fileType = extractFileTypeFromDataUrl(fileUrl);
                if (fileType != null) {
                    XWPFRun typeRun = responseParagraph.createRun();
                    typeRun.setText("Type: " + fileType);
                    typeRun.setItalic(true);
                    typeRun.setColor("666666");
                }

            } else {
                responseRun.setText("📄 Fichier fixe non configuré");
                responseRun.setItalic(true);
                responseRun.setColor("999999");
            }
        } else {
            responseRun.setText("📄 Fichier fixe non configuré");
            responseRun.setItalic(true);
            responseRun.setColor("999999");
        }
    }

    // Nouvelle méthode pour gérer la géolocalisation
    private void handleGeolocationField(XWPFDocument document, FormFieldDTO field, Object value) {
        XWPFParagraph responseParagraph = document.createParagraph();
        XWPFRun responseRun = responseParagraph.createRun();

        if (value != null && value instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, Object> geoData = (Map<String, Object>) value;

            Object lat = geoData.get("latitude");
            Object lng = geoData.get("longitude");

            if (lat != null && lng != null) {
                responseRun.setText("🌍 Position géographique");
                responseRun.setBold(true);
                responseRun.addBreak();

                XWPFRun coordRun = responseParagraph.createRun();
                coordRun.setText(String.format("Coordonnées: %.6f, %.6f",
                        ((Number) lat).doubleValue(),
                        ((Number) lng).doubleValue()));
                coordRun.setFontFamily("Courier New");
            } else {
                responseRun.setText("🌍 Position non disponible");
                responseRun.setItalic(true);
                responseRun.setColor("999999");
            }
        } else {
            responseRun.setText("🌍 Géolocalisation non renseignée");
            responseRun.setItalic(true);
            responseRun.setColor("999999");
        }
    }

    // Nouvelle méthode pour gérer les calculs
    private void handleCalculationField(XWPFDocument document, FormFieldDTO field, Object value) {
        XWPFParagraph responseParagraph = document.createParagraph();
        XWPFRun responseRun = responseParagraph.createRun();

        if (value != null && !value.toString().trim().isEmpty()) {
            responseRun.setText("🧮 Résultat du calcul");
            responseRun.setBold(true);
            responseRun.addBreak();

            XWPFRun resultRun = responseParagraph.createRun();
            if (value instanceof Number) {
                resultRun.setText("Valeur: " + ((Number) value).toString());
            } else {
                resultRun.setText("Valeur: " + value.toString());
            }
            resultRun.setFontFamily("Courier New");
            resultRun.setFontSize(12);
            resultRun.setBold(true);
            resultRun.setColor("0066CC");
        } else {
            responseRun.setText("🧮 Calcul non effectué");
            responseRun.setItalic(true);
            responseRun.setColor("999999");
        }
    }

    // Nouvelle méthode pour gérer le texte fixe
    private void handleFixedTextField(XWPFDocument document, FormFieldDTO field, Object value) {
        XWPFParagraph responseParagraph = document.createParagraph();
        XWPFRun responseRun = responseParagraph.createRun();

        responseRun.setText("📝 Texte informatif du formulaire");
        responseRun.setItalic(true);
        responseRun.setColor("666666");

        // Optionnel: afficher le contenu si configuré dans les attributs du champ
        if (field.getAttributes() != null && field.getAttributes().containsKey("content")) {
            String content = (String) field.getAttributes().get("content");
            if (content != null && !content.trim().isEmpty()) {
                responseRun.addBreak();
                XWPFRun contentRun = responseParagraph.createRun();
                contentRun.setText(content.length() > 100 ? content.substring(0, 97) + "..." : content);
                contentRun.setColor("999999");
            }
        }
    }

    // Méthode pour les champs standards (texte, nombre, etc.)
    private void handleStandardField(XWPFDocument document, FormFieldDTO field, Object value) {
        XWPFParagraph responseParagraph = document.createParagraph();
        XWPFRun responseRun = responseParagraph.createRun();

        if (value != null) {
            switch (field.getType()) {
                case "date":
                case "datetime":
                    responseRun.setText("Date: " + formatDateTime(value.toString()));
                    break;
                case "number":
                    responseRun.setText("Valeur: " + value.toString());
                    break;
                case "email":
                    responseRun.setText("Email: " + value.toString());
                    break;
                default:
                    responseRun.setText("Réponse: " + value.toString());
                    break;
            }
        } else {
            responseRun.setText("(Non renseigné)");
            responseRun.setItalic(true);
            responseRun.setColor("999999");
        }
    }

    // Méthodes utilitaires
    private String extractFileNameFromUrl(String url) {
        if (url == null || url.isEmpty()) return "fichier_inconnu";

        // Extraire le nom de fichier de l'URL
        String[] parts = url.split("/");
        String fileName = parts[parts.length - 1];

        // Nettoyer les paramètres d'URL
        if (fileName.contains("?")) {
            fileName = fileName.substring(0, fileName.indexOf("?"));
        }

        return fileName.isEmpty() ? "fichier" : fileName;
    }

    private String formatFileSize(long bytes) {
        if (bytes <= 0) return "0 B";

        String[] units = {"B", "KB", "MB", "GB"};
        int digitGroups = (int) (Math.log10(bytes) / Math.log10(1024));

        return String.format("%.1f %s",
                bytes / Math.pow(1024, digitGroups),
                units[digitGroups]);
    }

    private String formatDateTime(String dateTimeString) {
        try {
            LocalDateTime dateTime = LocalDateTime.parse(dateTimeString);
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy à HH:mm");
            return dateTime.format(formatter);
        } catch (Exception e) {
            // Fallback pour d'autres formats
            try {
                // Essayer avec le format ISO complet
                if (dateTimeString.contains("T")) {
                    dateTimeString = dateTimeString.substring(0, dateTimeString.indexOf('T') + 9);
                    LocalDateTime dateTime = LocalDateTime.parse(dateTimeString + ":00");
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy à HH:mm");
                    return dateTime.format(formatter);
                }
            } catch (Exception ex) {
                logger.warn("Impossible de parser la date: {}", dateTimeString);
            }
            return dateTimeString;
        }
    }

    private String extractFileTypeFromDataUrl(String dataUrl) {
        if (dataUrl == null || !dataUrl.startsWith("data:")) return null;

        try {
            String mimeType = dataUrl.substring(5, dataUrl.indexOf(';'));
            switch (mimeType) {
                case "image/png": return "Image PNG";
                case "image/jpeg": return "Image JPEG";
                case "image/gif": return "Image GIF";
                case "application/pdf": return "Document PDF";
                case "text/plain": return "Fichier texte";
                case "application/msword": return "Document Word";
                default: return mimeType.toUpperCase();
            }
        } catch (Exception e) {
            return "Fichier";
        }
    }
    // ✅ NOUVELLE MÉTHODE : Gérer les tables
    private void handleTableField(XWPFDocument document, FormFieldDTO field, Object value) {
        if (value == null) {
            XWPFParagraph responseParagraph = document.createParagraph();
            XWPFRun responseRun = responseParagraph.createRun();
            responseRun.setText("(Tableau non renseigné)");
            responseRun.setItalic(true);
            return;
        }

        try {
            Map<String, Object> tableData = null;

            if (value instanceof String) {
                // Parser JSON string
                ObjectMapper mapper = new ObjectMapper();
                tableData = mapper.readValue((String) value, new TypeReference<Map<String, Object>>() {});
            } else if (value instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, Object> mapValue = (Map<String, Object>) value;
                tableData = mapValue;
            }

            if (tableData != null) {
                List<?> columns = (List<?>) tableData.get("columns");
                List<?> rows = (List<?>) tableData.get("data"); // ou "rows" selon votre structure

                if (columns != null && rows != null && !columns.isEmpty() && !rows.isEmpty()) {
                    // Créer le tableau Word
                    XWPFTable table = document.createTable(rows.size() + 1, columns.size());
                    table.setWidth("100%");

                    // En-têtes
                    XWPFTableRow headerRow = table.getRow(0);
                    for (int i = 0; i < columns.size(); i++) {
                        XWPFTableCell cell = headerRow.getCell(i);
                        cell.setText(columns.get(i).toString());
                        cell.setColor("E6E6FA"); // Couleur de fond pour l'en-tête

                        // Style de l'en-tête
                        XWPFParagraph cellPara = cell.getParagraphs().get(0);
                        XWPFRun cellRun = cellPara.getRuns().get(0);
                        cellRun.setBold(true);
                    }

                    // Données
                    for (int rowIndex = 0; rowIndex < rows.size(); rowIndex++) {
                        Object rowData = rows.get(rowIndex);
                        XWPFTableRow row = table.getRow(rowIndex + 1);

                        if (rowData instanceof Map) {
                            @SuppressWarnings("unchecked")
                            Map<String, Object> rowMap = (Map<String, Object>) rowData;

                            for (int colIndex = 0; colIndex < columns.size(); colIndex++) {
                                String columnName = columns.get(colIndex).toString();
                                Object cellValue = rowMap.get(columnName);
                                String cellText = cellValue != null ? cellValue.toString() : "";

                                XWPFTableCell cell = row.getCell(colIndex);
                                cell.setText(cellText);
                            }
                        }
                    }

                    return;
                }
            }
        } catch (Exception e) {
            logger.error("Erreur traitement table: {}", e.getMessage());
        }

        // Fallback
        XWPFParagraph responseParagraph = document.createParagraph();
        XWPFRun responseRun = responseParagraph.createRun();
        responseRun.setText("(Erreur affichage tableau)");
        responseRun.setItalic(true);
    }

    // ✅ NOUVELLE MÉTHODE : Gérer les images fixes
    private void handleStaticImageField(XWPFDocument document, FormFieldDTO field, Object value) {
        // Pour les images fixes, récupérer depuis les attributs du champ
        if (field.getAttributes() != null) {
            String imageUrl = (String) field.getAttributes().get("imageUrl");

            if (imageUrl != null && imageUrl.startsWith("data:image/")) {
                try {
                    String[] parts = imageUrl.split(",");
                    if (parts.length == 2) {
                        byte[] imageBytes = Base64.getDecoder().decode(parts[1]);

                        XWPFParagraph responseParagraph = document.createParagraph();
                        XWPFRun imageRun = responseParagraph.createRun();
                        imageRun.setText("Image du formulaire :");
                        imageRun.addBreak();

                        try (ByteArrayInputStream bis = new ByteArrayInputStream(imageBytes)) {
                            imageRun.addPicture(bis, XWPFDocument.PICTURE_TYPE_PNG,
                                    "static_image.png", Units.toEMU(300), Units.toEMU(200));
                        }
                        return;
                    }
                } catch (Exception e) {
                    logger.error("Erreur insertion image fixe: {}", e.getMessage());
                }
            }
        }

        // Fallback
        XWPFParagraph responseParagraph = document.createParagraph();
        XWPFRun responseRun = responseParagraph.createRun();
        responseRun.setText("(Image non configurée)");
        responseRun.setItalic(true);
    }

    // ✅ NOUVELLE MÉTHODE : Gérer les adresses
    private void handleAddressField(XWPFDocument document, FormFieldDTO field, Object value) {
        XWPFParagraph responseParagraph = document.createParagraph();
        XWPFRun responseRun = responseParagraph.createRun();

        if (value instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, Object> addressData = (Map<String, Object>) value;

            String fullAddress = (String) addressData.get("fullAddress");
            String zipCode = (String) addressData.get("zipCode");
            String city = (String) addressData.get("city");

            if (fullAddress != null && !fullAddress.trim().isEmpty()) {
                responseRun.setText("Adresse : " + fullAddress);
            } else {
                List<String> parts = new ArrayList<>();
                if (zipCode != null && !zipCode.trim().isEmpty()) parts.add("CP: " + zipCode);
                if (city != null && !city.trim().isEmpty()) parts.add("Ville: " + city);

                if (!parts.isEmpty()) {
                    responseRun.setText("Adresse : " + String.join(", ", parts));
                } else {
                    responseRun.setText("(Adresse non renseignée)");
                    responseRun.setItalic(true);
                }
            }
        } else {
            responseRun.setText("(Adresse non renseignée)");
            responseRun.setItalic(true);
        }
    }

    // ✅ NOUVELLE MÉTHODE : Gérer les contacts
    private void handleContactField(XWPFDocument document, FormFieldDTO field, Object value) {
        XWPFParagraph responseParagraph = document.createParagraph();
        XWPFRun responseRun = responseParagraph.createRun();

        if (value instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, Object> contactData = (Map<String, Object>) value;

            List<String> contactParts = new ArrayList<>();

            String name = (String) contactData.get("name");
            String phone = (String) contactData.get("phone");
            String email = (String) contactData.get("email");

            if (name != null && !name.trim().isEmpty()) contactParts.add("Nom: " + name);
            if (phone != null && !phone.trim().isEmpty()) contactParts.add("Tél: " + phone);
            if (email != null && !email.trim().isEmpty()) contactParts.add("Email: " + email);

            if (!contactParts.isEmpty()) {
                responseRun.setText("Contact : " + String.join(" | ", contactParts));
            } else {
                responseRun.setText("(Contact non renseigné)");
                responseRun.setItalic(true);
            }
        } else {
            responseRun.setText("(Contact non renseigné)");
            responseRun.setItalic(true);
        }
    }

    // ✅ NOUVELLE MÉTHODE : Gérer les checkboxes
    private void handleCheckboxField(XWPFDocument document, FormFieldDTO field, Object value) {
        XWPFParagraph responseParagraph = document.createParagraph();
        XWPFRun responseRun = responseParagraph.createRun();

        if (value instanceof List) {
            @SuppressWarnings("unchecked")
            List<Object> selectedValues = (List<Object>) value;

            if (!selectedValues.isEmpty()) {
                List<String> labels = new ArrayList<>();
                for (Object selectedValue : selectedValues) {
                    labels.add(selectedValue.toString());
                }
                responseRun.setText("Sélections : " + String.join(", ", labels));
            } else {
                responseRun.setText("Aucune option sélectionnée");
            }
        } else if (value instanceof Boolean) {
            responseRun.setText("Case cochée : " + (((Boolean) value) ? "Oui" : "Non"));
        } else {
            responseRun.setText("(Non renseigné)");
            responseRun.setItalic(true);
        }
    }

    // ✅ NOUVELLE MÉTHODE : Traitement par défaut

    // ✅ NOUVELLE MÉTHODE : Gérer les listes externes (gardez votre implémentation existante)
    private void handleExternalListField(XWPFDocument document, FormFieldDTO field, Object value) {
        XWPFParagraph responseParagraph = document.createParagraph();
        XWPFRun responseRun = responseParagraph.createRun();

        String displayValue = formatExternalListValue(field, value);
        responseRun.setText("Sélection : " + displayValue);
    }
    // ✅ NOUVELLE MÉTHODE : Formater les valeurs de liste externe pour Word
    private String formatExternalListValue(FormFieldDTO field, Object value) {
        // Récupérer les options de la liste externe depuis les attributs du champ
        if (field.getAttributes() != null && field.getAttributes().containsKey("externalListId")) {
            Long externalListId = extractLongFromAttributes(field.getAttributes(), "externalListId");

            if (externalListId != null) {
                // Charger les options depuis ExternalListService
                try {
                    List<ExternalListItemDTO> options = externalListService.getListItems(externalListId);
                    return formatValueWithLabels(value, options);
                } catch (Exception e) {
                    logger.error("Erreur chargement options liste externe: {}", e.getMessage());
                }
            }
        }

        // Fallback : afficher les valeurs brutes
        if (value instanceof List) {
            return ((List<?>) value).stream()
                    .map(Object::toString)
                    .collect(Collectors.joining(", "));
        }

        return value.toString();
    }
    private Long extractLongFromAttributes(Map<String, Object> attributes, String key) {
        try {
            Object value = attributes.get(key);
            if (value == null) return null;

            if (value instanceof Number) {
                return ((Number) value).longValue();
            }
            if (value instanceof String) {
                return Long.parseLong((String) value);
            }
        } catch (Exception e) {
            logger.error("Erreur conversion attribut {} en Long: {}", key, e.getMessage());
        }
        return null;
    }

    // ✅ MÉTHODE HELPER : Convertir valeurs en labels
    private String formatValueWithLabels(Object value, List<ExternalListItemDTO> options) {
        Map<String, String> valueToLabel = options.stream()
                .collect(Collectors.toMap(
                        option -> option.getValue().toString(),
                        ExternalListItemDTO::getLabel
                ));

        if (value instanceof List) {
            List<String> labels = ((List<?>) value).stream()
                    .map(v -> valueToLabel.getOrDefault(v.toString(), v.toString()))
                    .collect(Collectors.toList());

            List<String> values = ((List<?>) value).stream()
                    .map(Object::toString)
                    .collect(Collectors.toList());

            // Format: "label1 (value1), label2 (value2)"
            StringBuilder result = new StringBuilder();
            for (int i = 0; i < labels.size(); i++) {
                if (i > 0) result.append(", ");
                result.append(labels.get(i)).append(" (").append(values.get(i)).append(")");
            }
            return result.toString();
        } else {
            String label = valueToLabel.getOrDefault(value.toString(), value.toString());
            return label + " (" + value.toString() + ")";
        }
    }

    private String formatDate(String dateString) {
        try {
            LocalDateTime dateTime = LocalDateTime.parse(dateString);
            return dateTime.format(DateTimeFormatter.ofPattern("dd/MM/yyyy à HH:mm"));
        } catch (Exception e) {
            return dateString;
        }
    }
}