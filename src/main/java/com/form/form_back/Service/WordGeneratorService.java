package com.form.form_back.Service;

import com.form.form_back.dto.*;
import org.apache.poi.xwpf.usermodel.ParagraphAlignment;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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

    private void generateFieldWithValueInDocument(XWPFDocument document, FormFieldDTO field, Map<String, Object> data) {
        // Label du champ
        XWPFParagraph labelParagraph = document.createParagraph();
        XWPFRun labelRun = labelParagraph.createRun();
        labelRun.setText(field.getLabel() + (field.getRequired() ? " *" : ""));
        labelRun.setBold(true);

        // Récupérer la valeur soumise
        Object value = data.get(field.getFieldName());

        // Afficher la réponse selon le type de champ
        XWPFParagraph responseParagraph = document.createParagraph();
        XWPFRun responseRun = responseParagraph.createRun();

        if (value != null) {
            switch (field.getType()) {
                case "external-list":
                    // ✅ TRAITEMENT SPÉCIAL pour external-list
                    String displayValue = formatExternalListValue(field, value);
                    responseRun.setText("Sélection: " + displayValue);
                    break;
                case "select":
                case "radio":
                    responseRun.setText("Réponse sélectionnée: " + value.toString());
                    break;
                case "checkbox":
                    responseRun.setText("Case cochée: " + (Boolean.parseBoolean(value.toString()) ? "Oui" : "Non"));
                    break;
                case "datetime":
                case "date":
                    responseRun.setText("Date saisie: " + value.toString());
                    break;
                case "number":
                    responseRun.setText("Valeur numérique: " + value.toString());
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

        document.createParagraph();
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