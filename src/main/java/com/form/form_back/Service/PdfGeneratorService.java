package com.form.form_back.Service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.form.form_back.dto.*;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.borders.Border;
import com.itextpdf.layout.borders.SolidBorder;
import com.itextpdf.layout.element.*;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class PdfGeneratorService {

    @Autowired
    private ExternalListService externalListService;

    private static final Logger logger = LoggerFactory.getLogger(PdfGeneratorService.class);
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy à HH:mm");

    public byte[] generateSubmissionPdf(FormDTO form, FormSubmissionResponseDTO submission) throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PdfWriter writer = new PdfWriter(baos);
        PdfDocument pdfDoc = new PdfDocument(writer);
        Document document = new Document(pdfDoc);

        // Titre
        addTitle(document, form.getName() + " - Soumission complétée");

        // Secteur
        if (form.getSecteur() != null) {
            Paragraph sector = new Paragraph("Secteur: " + form.getSecteur())
                    .setBold()
                    .setFontColor(ColorConstants.RED)
                    .setMarginBottom(10);
            document.add(sector);
        }

        // Séparateur
        document.add(new Paragraph("\n"));

        // Informations de soumission
        addSectionTitle(document, "INFORMATIONS DE SOUMISSION");
        addSubmissionInfo(document, submission);

        // Séparateur
        document.add(new Paragraph("\n"));

        // Données du formulaire
        addSectionTitle(document, "RÉPONSES DU FORMULAIRE");

        // Traiter chaque champ
        for (FormFieldDTO field : form.getFields()) {
            generateFieldInPdf(document, field, submission.getData());
        }

        document.close();
        return baos.toByteArray();
    }

    private void addTitle(Document document, String title) {
        Paragraph titlePara = new Paragraph(title)
                .setFontSize(18)
                .setBold()
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginBottom(20);
        document.add(titlePara);
    }

    private void addSectionTitle(Document document, String title) {
        Paragraph section = new Paragraph("═══ " + title + " ═══")
                .setFontSize(14)
                .setBold()
                .setMarginTop(15)
                .setMarginBottom(10);
        document.add(section);
    }

    private void addSubmissionInfo(Document document, FormSubmissionResponseDTO submission) {
        addInfoLine(document, "ID de soumission:", "#" + submission.getId());
        addInfoLine(document, "Date de soumission:", submission.getSubmittedAt().format(DATE_FORMATTER));

        if (submission.getSubmitterName() != null) {
            addInfoLine(document, "Soumis par:", submission.getSubmitterName());
        }

        if (submission.getSubmitterEmail() != null) {
            addInfoLine(document, "Email:", submission.getSubmitterEmail());
        }
    }

    private void addInfoLine(Document document, String label, String value) {
        Paragraph para = new Paragraph()
                .add(new Text(label + " ").setBold())
                .add(new Text(value))
                .setMarginBottom(5);
        document.add(para);
    }

    private void generateFieldInPdf(Document document, FormFieldDTO field, Map<String, Object> data) {
        // Label du champ
        Paragraph labelPara = new Paragraph(field.getLabel() + (field.getRequired() ? " *" : ""))
                .setBold()
                .setMarginTop(10)
                .setMarginBottom(5);
        document.add(labelPara);

        Object value = data.get(field.getFieldName());

        try {
            switch (field.getType()) {
                case "signature":
                case "drawing":
                    handleImageFieldPdf(document, field, value, field.getType());
                    break;

                case "table":
                    handleTableFieldPdf(document, field, value);
                    break;

                case "image":
                    handleStaticImageFieldPdf(document, field, value);
                    break;

                case "address":
                    handleAddressFieldPdf(document, field, value);
                    break;

                case "contact":
                    handleContactFieldPdf(document, field, value);
                    break;

                case "checkbox":
                    handleCheckboxFieldPdf(document, field, value);
                    break;

                case "external-list":
                    handleExternalListFieldPdf(document, field, value);
                    break;

                case "barcode":
                case "nfc":
                    handleBarcodeFieldPdf(document, field, value);
                    break;

                case "file":
                case "attachment":
                    handleFileFieldPdf(document, field, value);
                    break;

                case "file-fixed":
                    handleFileFixedFieldPdf(document, field, value);
                    break;

                case "geolocation":
                    handleGeolocationFieldPdf(document, field, value);
                    break;

                case "calculation":
                    handleCalculationFieldPdf(document, field, value);
                    break;

                case "fixed-text":
                    handleFixedTextFieldPdf(document, field, value);
                    break;

                case "separator":
                    handleSeparatorFieldPdf(document, field, value);
                    break;

                default:
                    handleDefaultFieldPdf(document, field, value);
                    break;
            }
        } catch (Exception e) {
            logger.error("Erreur traitement champ {}: {}", field.getFieldName(), e.getMessage());
            document.add(new Paragraph("Erreur lors du traitement de ce champ")
                    .setItalic()
                    .setFontColor(ColorConstants.RED));
        }

        // Espacement
        document.add(new Paragraph("\n"));
    }

    private void handleImageFieldPdf(Document document, FormFieldDTO field, Object value, String fieldType) {
        String icon = fieldType.equals("signature") ? "✍️ " : "🎨 ";
        String label = fieldType.equals("signature") ? "Signature capturée" : "Dessin capturé";

        if (value != null) {
            try {
                String imageData = null;

                if (value instanceof Map) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> dataMap = (Map<String, Object>) value;
                    imageData = (String) dataMap.get("data");
                    if (imageData == null) imageData = (String) dataMap.get("url");
                    if (imageData == null) imageData = (String) dataMap.get("imageData");
                } else if (value instanceof String) {
                    imageData = (String) value;
                }

                if (imageData != null && !imageData.trim().isEmpty()) {
                    // Base64
                    if (imageData.startsWith("data:image/")) {
                        String[] parts = imageData.split(",");
                        if (parts.length == 2) {
                            byte[] imageBytes = Base64.getDecoder().decode(parts[1]);
                            Image img = new Image(ImageDataFactory.create(imageBytes));
                            img.setWidth(UnitValue.createPercentValue(40));
                            img.setMarginTop(10);

                            document.add(new Paragraph(icon + label + ":").setBold());
                            document.add(img);
                            return;
                        }
                    }
                    // Fichier
                    else if (imageData.startsWith("/uploads/") ||
                            imageData.startsWith("/signatures/") ||
                            imageData.startsWith("/drawings/")) {

                        String[] possiblePaths = {
                                "." + imageData,
                                "uploads" + imageData,
                                System.getProperty("user.dir") + imageData
                        };

                        for (String path : possiblePaths) {
                            File imageFile = new File(path);
                            if (imageFile.exists()) {
                                try (FileInputStream fis = new FileInputStream(imageFile)) {
                                    byte[] imageBytes = fis.readAllBytes();
                                    Image img = new Image(ImageDataFactory.create(imageBytes));
                                    img.setWidth(UnitValue.createPercentValue(40));
                                    img.setMarginTop(10);

                                    document.add(new Paragraph(icon + label + ":").setBold());
                                    document.add(img);
                                    return;
                                }
                            }
                        }
                    }
                }
            } catch (Exception e) {
                logger.error("Erreur traitement image PDF: {}", e.getMessage());
            }
        }

        // Fallback
        document.add(new Paragraph(icon + label + " non disponible")
                .setItalic()
                .setFontColor(new DeviceRgb(153, 153, 153)));
    }

    private void handleTableFieldPdf(Document document, FormFieldDTO field, Object value) {
        if (value == null) {
            document.add(new Paragraph("(Tableau non renseigné)").setItalic());
            return;
        }

        try {
            Map<String, Object> tableData = null;

            if (value instanceof String) {
                ObjectMapper mapper = new ObjectMapper();
                tableData = mapper.readValue((String) value, new TypeReference<Map<String, Object>>() {});
            } else if (value instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, Object> mapValue = (Map<String, Object>) value;
                tableData = mapValue;
            }

            if (tableData != null) {
                List<?> columns = (List<?>) tableData.get("columns");
                List<?> rows = (List<?>) tableData.get("data");

                if (columns != null && rows != null && !columns.isEmpty() && !rows.isEmpty()) {
                    Table table = new Table(columns.size());
                    table.setWidth(UnitValue.createPercentValue(100));

                    // En-têtes
                    for (Object column : columns) {
                        Cell headerCell = new Cell()
                                .add(new Paragraph(column.toString()).setBold())
                                .setBackgroundColor(new DeviceRgb(230, 230, 250))
                                .setBorder(new SolidBorder(1));
                        table.addHeaderCell(headerCell);
                    }

                    // Données
                    for (Object rowData : rows) {
                        if (rowData instanceof Map) {
                            @SuppressWarnings("unchecked")
                            Map<String, Object> rowMap = (Map<String, Object>) rowData;

                            for (Object column : columns) {
                                String columnName = column.toString();
                                Object cellValue = rowMap.get(columnName);
                                String cellText = cellValue != null ? cellValue.toString() : "";

                                Cell cell = new Cell()
                                        .add(new Paragraph(cellText))
                                        .setBorder(new SolidBorder(1));
                                table.addCell(cell);
                            }
                        }
                    }

                    document.add(table);
                    return;
                }
            }
        } catch (Exception e) {
            logger.error("Erreur traitement table PDF: {}", e.getMessage());
        }

        document.add(new Paragraph("(Erreur affichage tableau)").setItalic());
    }

    private void handleStaticImageFieldPdf(Document document, FormFieldDTO field, Object value) {
        if (field.getAttributes() != null) {
            String imageUrl = (String) field.getAttributes().get("imageUrl");

            if (imageUrl != null && imageUrl.startsWith("data:image/")) {
                try {
                    String[] parts = imageUrl.split(",");
                    if (parts.length == 2) {
                        byte[] imageBytes = Base64.getDecoder().decode(parts[1]);
                        Image img = new Image(ImageDataFactory.create(imageBytes));
                        img.setWidth(UnitValue.createPercentValue(50));

                        document.add(new Paragraph("Image du formulaire:").setBold());
                        document.add(img);
                        return;
                    }
                } catch (Exception e) {
                    logger.error("Erreur image fixe PDF: {}", e.getMessage());
                }
            }
        }

        document.add(new Paragraph("(Image non configurée)").setItalic());
    }

    private void handleAddressFieldPdf(Document document, FormFieldDTO field, Object value) {
        if (value instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, Object> addressData = (Map<String, Object>) value;

            String fullAddress = (String) addressData.get("fullAddress");
            String zipCode = (String) addressData.get("zipCode");
            String city = (String) addressData.get("city");

            if (fullAddress != null && !fullAddress.trim().isEmpty()) {
                document.add(new Paragraph("Adresse: " + fullAddress));
            } else {
                List<String> parts = new ArrayList<>();
                if (zipCode != null) parts.add("CP: " + zipCode);
                if (city != null) parts.add("Ville: " + city);

                if (!parts.isEmpty()) {
                    document.add(new Paragraph("Adresse: " + String.join(", ", parts)));
                } else {
                    document.add(new Paragraph("(Adresse non renseignée)").setItalic());
                }
            }
        } else {
            document.add(new Paragraph("(Adresse non renseignée)").setItalic());
        }
    }

    private void handleContactFieldPdf(Document document, FormFieldDTO field, Object value) {
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
                document.add(new Paragraph("Contact: " + String.join(" | ", contactParts)));
            } else {
                document.add(new Paragraph("(Contact non renseigné)").setItalic());
            }
        } else {
            document.add(new Paragraph("(Contact non renseigné)").setItalic());
        }
    }

    private void handleCheckboxFieldPdf(Document document, FormFieldDTO field, Object value) {
        if (value instanceof List) {
            @SuppressWarnings("unchecked")
            List<Object> selectedValues = (List<Object>) value;

            if (!selectedValues.isEmpty()) {
                List<String> labels = new ArrayList<>();
                for (Object selectedValue : selectedValues) {
                    labels.add(selectedValue.toString());
                }
                document.add(new Paragraph("Sélections: " + String.join(", ", labels)));
            } else {
                document.add(new Paragraph("Aucune option sélectionnée"));
            }
        } else if (value instanceof Boolean) {
            document.add(new Paragraph("Case cochée: " + (((Boolean) value) ? "Oui" : "Non")));
        } else {
            document.add(new Paragraph("(Non renseigné)").setItalic());
        }
    }

    private void handleExternalListFieldPdf(Document document, FormFieldDTO field, Object value) {
        String displayValue = formatExternalListValue(field, value);
        document.add(new Paragraph("Sélection: " + displayValue));
    }

    private void handleBarcodeFieldPdf(Document document, FormFieldDTO field, Object value) {
        if (value != null && value instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, Object> barcodeData = (Map<String, Object>) value;

            String code = (String) barcodeData.get("code");
            String scannedAt = (String) barcodeData.get("scannedAt");

            if (code != null && !code.trim().isEmpty()) {
                document.add(new Paragraph("📱 Code scanné: " + code).setBold());
                if (scannedAt != null) {
                    document.add(new Paragraph("Scanné le: " + formatDateTime(scannedAt))
                            .setItalic()
                            .setFontSize(9)
                            .setFontColor(new DeviceRgb(102, 102, 102)));
                }
            } else {
                document.add(new Paragraph("📱 Code non disponible").setItalic());
            }
        } else {
            document.add(new Paragraph("📱 Code non scanné").setItalic());
        }
    }

    private void handleFileFieldPdf(Document document, FormFieldDTO field, Object value) {
        if (value != null && value instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, Object> fileData = (Map<String, Object>) value;

            String url = (String) fileData.get("url");
            Object originalSize = fileData.get("originalSize");

            if (url != null && !url.trim().isEmpty()) {
                String fileName = extractFileNameFromUrl(url);
                document.add(new Paragraph("📎 Fichier téléchargé").setBold());
                document.add(new Paragraph("Nom: " + fileName));

                if (originalSize != null) {
                    document.add(new Paragraph("Taille: " + formatFileSize(((Number) originalSize).longValue())));
                }
            } else {
                document.add(new Paragraph("📎 Fichier non disponible").setItalic());
            }
        } else {
            document.add(new Paragraph("📎 Aucun fichier téléchargé").setItalic());
        }
    }

    private void handleFileFixedFieldPdf(Document document, FormFieldDTO field, Object value) {
        if (value != null && value instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, Object> fileData = (Map<String, Object>) value;

            String fileName = (String) fileData.get("fileName");

            if (fileName != null) {
                document.add(new Paragraph("📄 Fichier fixe: " + fileName).setBold());
            } else {
                document.add(new Paragraph("📄 Fichier fixe non configuré").setItalic());
            }
        } else {
            document.add(new Paragraph("📄 Fichier fixe non configuré").setItalic());
        }
    }

    private void handleGeolocationFieldPdf(Document document, FormFieldDTO field, Object value) {
        if (value != null && value instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, Object> geoData = (Map<String, Object>) value;

            Object lat = geoData.get("latitude");
            Object lng = geoData.get("longitude");

            if (lat != null && lng != null) {
                document.add(new Paragraph("🌍 Position géographique").setBold());
                document.add(new Paragraph(String.format("Coordonnées: %.6f, %.6f",
                        ((Number) lat).doubleValue(),
                        ((Number) lng).doubleValue())));
            } else {
                document.add(new Paragraph("🌍 Position non disponible").setItalic());
            }
        } else {
            document.add(new Paragraph("🌍 Géolocalisation non renseignée").setItalic());
        }
    }

    private void handleCalculationFieldPdf(Document document, FormFieldDTO field, Object value) {
        if (value != null && !value.toString().trim().isEmpty()) {
            document.add(new Paragraph("🧮 Résultat du calcul").setBold());
            document.add(new Paragraph("Valeur: " + value.toString())
                    .setFontSize(12)
                    .setBold()
                    .setFontColor(new DeviceRgb(0, 102, 204)));
        } else {
            document.add(new Paragraph("🧮 Calcul non effectué").setItalic());
        }
    }

    private void handleFixedTextFieldPdf(Document document, FormFieldDTO field, Object value) {
        document.add(new Paragraph("📝 Texte informatif du formulaire")
                .setItalic()
                .setFontColor(new DeviceRgb(102, 102, 102)));

        if (field.getAttributes() != null && field.getAttributes().containsKey("content")) {
            String content = (String) field.getAttributes().get("content");
            if (content != null && !content.trim().isEmpty()) {
                document.add(new Paragraph(content.length() > 100 ? content.substring(0, 97) + "..." : content)
                        .setFontColor(new DeviceRgb(153, 153, 153)));
            }
        }
    }

    private void handleSeparatorFieldPdf(Document document, FormFieldDTO field, Object value) {
        Paragraph separator = new Paragraph("═══════════════════════════════════════════════════════════════════")
                .setBold()
                .setTextAlignment(TextAlignment.CENTER)
                .setFontColor(new DeviceRgb(102, 102, 102))
                .setMarginTop(10)
                .setMarginBottom(10);
        document.add(separator);
    }

    private void handleDefaultFieldPdf(Document document, FormFieldDTO field, Object value) {
        if (value != null) {
            switch (field.getType()) {
                case "date":
                case "datetime":
                    document.add(new Paragraph("Date: " + formatDateTime(value.toString())));
                    break;
                case "number":
                    document.add(new Paragraph("Valeur: " + value.toString()));
                    break;
                case "email":
                    document.add(new Paragraph("Email: " + value.toString()));
                    break;
                default:
                    document.add(new Paragraph("Réponse: " + value.toString()));
                    break;
            }
        } else {
            document.add(new Paragraph("(Non renseigné)")
                    .setItalic()
                    .setFontColor(new DeviceRgb(153, 153, 153)));
        }
    }

    // Méthodes utilitaires
    private String formatExternalListValue(FormFieldDTO field, Object value) {
        if (field.getAttributes() != null && field.getAttributes().containsKey("externalListId")) {
            Long externalListId = extractLongFromAttributes(field.getAttributes(), "externalListId");

            if (externalListId != null) {
                try {
                    List<ExternalListItemDTO> options = externalListService.getListItems(externalListId);
                    return formatValueWithLabels(value, options);
                } catch (Exception e) {
                    logger.error("Erreur chargement options liste externe: {}", e.getMessage());
                }
            }
        }

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

    private String extractFileNameFromUrl(String url) {
        if (url == null || url.isEmpty()) return "fichier_inconnu";

        String[] parts = url.split("/");
        String fileName = parts[parts.length - 1];

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
            return dateTime.format(DATE_FORMATTER);
        } catch (Exception e) {
            try {
                if (dateTimeString.contains("T")) {
                    dateTimeString = dateTimeString.substring(0, dateTimeString.indexOf('T') + 9);
                    LocalDateTime dateTime = LocalDateTime.parse(dateTimeString + ":00");
                    return dateTime.format(DATE_FORMATTER);
                }
            } catch (Exception ex) {
                logger.warn("Impossible de parser la date: {}", dateTimeString);
            }
            return dateTimeString;
        }
    }
}