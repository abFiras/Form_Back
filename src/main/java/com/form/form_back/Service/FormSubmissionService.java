// FormSubmissionService.java - Service dédié pour la gestion des soumissions
package com.form.form_back.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.form.form_back.Entity.*;
import com.form.form_back.Repo.*;
import com.form.form_back.dto.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class FormSubmissionService {

    @Autowired
    private FormRepository formRepository;

    @Autowired
    private FormSubmissionRepository formSubmissionRepository;

    @Autowired
    private UtilisateurRepository userRepository;

    @Autowired
    private FileStorageService fileStorageService;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private static final Logger logger = LoggerFactory.getLogger(FormSubmissionService.class);

    /**
     * ✅ SOUMETTRE UN FORMULAIRE - Créer une nouvelle soumission RÉELLE
     */
    public FormSubmissionResponseDTO submitFormAuthenticated(Long formId, FormSubmissionRequest request, Long userId) {
        // Vérifications préliminaires
        Form form = formRepository.findByIdWithFields(formId);
        if (form == null) {
            throw new RuntimeException("Formulaire non trouvé");
        }

        Utilisateur user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

        // ✅ VÉRIFICATION STRICTE DES ACCÈS
        if (!form.isAccessibleByUser(user)) {
            String groupNames = form.getAssignedGroups().stream()
                    .map(Group::getName)
                    .collect(Collectors.joining(", "));
            throw new RuntimeException(
                    "Accès refusé. Ce formulaire est réservé aux groupes : " + groupNames +
                            ". Vos groupes actuels : " + user.getGroups().stream()
                            .map(Group::getName).collect(Collectors.joining(", ")));
        }

        if (!"PUBLISHED".equals(form.getStatus())) {
            throw new RuntimeException("Ce formulaire n'est pas encore publié");
        }

        // ✅ VALIDER ET TRAITER LES DONNÉES
        Map<String, Object> validatedData = validateAndProcessSubmissionData(request.getData(), form.getFields());

        try {
            // ✅ CRÉER UNE NOUVELLE SOUMISSION RÉELLE (pas une template)
            FormSubmission submission = new FormSubmission();
            submission.setFormId(formId);
            submission.setUtilisateur(user);
            submission.setIsTemplate(false); // ✅ IMPORTANT : Vraie soumission
            submission.setStatus("SUBMITTED");
            submission.setSubmittedAt(LocalDateTime.now());
            submission.setSubmitterEmail(user.getEmail());

            // Ajouter des métadonnées de soumission
            validatedData.put("_submission_metadata", createSubmissionMetadata(user, form));
            submission.setData(objectMapper.writeValueAsString(validatedData));

            FormSubmission savedSubmission = formSubmissionRepository.save(submission);

            logger.info("✅ NOUVELLE soumission créée: ID={}, User={}, Form={}",
                    savedSubmission.getId(), user.getUsername(), form.getName());

            return convertToSubmissionResponseDTO(savedSubmission);

        } catch (JsonProcessingException e) {
            logger.error("Erreur sérialisation données soumission: {}", e.getMessage());
            throw new RuntimeException("Erreur lors de l'enregistrement des données");
        }
    }

    /**
     * ✅ SOUMISSION ANONYME pour formulaires publics
     */
    public FormSubmissionResponseDTO submitFormAnonymous(Long formId, FormSubmissionRequest request, String clientIp) {
        Form form = formRepository.findByIdWithFields(formId);
        if (form == null || !"PUBLISHED".equals(form.getStatus())) {
            throw new RuntimeException("Formulaire non disponible");
        }

        // Pour les soumissions anonymes, vérifier si le formulaire accepte les soumissions publiques
        // (vous pouvez ajouter un champ "allowAnonymous" dans votre entité Form)

        Map<String, Object> validatedData = validateAndProcessSubmissionData(request.getData(), form.getFields());

        try {
            FormSubmission submission = new FormSubmission();
            submission.setFormId(formId);
            submission.setUtilisateur(null); // Soumission anonyme
            submission.setIsTemplate(false);
            submission.setStatus("SUBMITTED");
            submission.setSubmittedAt(LocalDateTime.now());
            submission.setSubmitterIp(clientIp);

            // Ajouter des métadonnées anonymes
            validatedData.put("_submission_metadata", createAnonymousSubmissionMetadata(clientIp, form));
            submission.setData(objectMapper.writeValueAsString(validatedData));

            FormSubmission savedSubmission = formSubmissionRepository.save(submission);

            logger.info("✅ Soumission anonyme créée: ID={}, IP={}, Form={}",
                    savedSubmission.getId(), clientIp, form.getName());

            return convertToSubmissionResponseDTO(savedSubmission);

        } catch (JsonProcessingException e) {
            logger.error("Erreur sérialisation données soumission anonyme: {}", e.getMessage());
            throw new RuntimeException("Erreur lors de l'enregistrement des données");
        }
    }

    /**
     * ✅ VALIDER ET TRAITER LES DONNÉES de soumission
     */
    private Map<String, Object> validateAndProcessSubmissionData(Map<String, Object> rawData, List<FormField> fields) {
        Map<String, Object> processedData = new HashMap<>();
        Set<String> fieldNames = fields.stream().map(FormField::getFieldName).collect(Collectors.toSet());

        for (FormField field : fields) {
            String fieldName = field.getFieldName();
            Object value = rawData.get(fieldName);

            // ✅ Validation des champs obligatoires
            if (field.getRequired() && (value == null || "".equals(value))) {
                throw new RuntimeException("Le champ '" + field.getLabel() + "' est obligatoire");
            }

            if (value != null) {
                // ✅ Traitement selon le type de champ
                Object processedValue = processFieldValue(field, value);
                if (processedValue != null) {
                    processedData.put(fieldName, processedValue);
                }
            }
        }

        // ✅ Nettoyer les données non autorisées
        rawData.entrySet().stream()
                .filter(entry -> !entry.getKey().startsWith("_") && fieldNames.contains(entry.getKey()))
                .forEach(entry -> {
                    if (!processedData.containsKey(entry.getKey())) {
                        processedData.put(entry.getKey(), entry.getValue());
                    }
                });

        return processedData;
    }

    /**
     * ✅ TRAITER LA VALEUR d'un champ selon son type
     */
    private Object processFieldValue(FormField field, Object value) {
        switch (field.getType()) {
            case "signature":
                return processSignatureField(field, value);

            case "file":
            case "attachment":
                return processFileField(field, value);

            case "drawing":
                return processDrawingField(field, value);

            case "geolocation":
                return processGeolocationField(field, value);

            case "datetime":
                return processDateTimeField(field, value);

            case "number":
            case "slider":
                return processNumberField(field, value);

            case "checkbox":
                return processBooleanField(field, value);

            case "external-list":
                return processExternalListField(field, value);

            default:
                return processTextualField(field, value);
        }
    }

    /**
     * ✅ TRAITER LES SIGNATURES
     */
    private Map<String, Object> processSignatureField(FormField field, Object value) {
        if (!(value instanceof String) || !((String) value).startsWith("data:image/")) {
            return null;
        }

        try {
            String signatureData = (String) value;
            String filename = field.getFieldName() + "_" + System.currentTimeMillis();
            String savedUrl = fileStorageService.saveBase64Image(signatureData, "signatures", filename);

            Map<String, Object> result = new HashMap<>();
            result.put("url", savedUrl);
            result.put("type", "signature");
            result.put("originalSize", signatureData.length());
            result.put("savedAt", LocalDateTime.now().toString());

            logger.info("Signature sauvegardée: {}", savedUrl);
            return result;

        } catch (Exception e) {
            logger.error("Erreur sauvegarde signature: {}", e.getMessage());
            throw new RuntimeException("Erreur lors de la sauvegarde de la signature");
        }
    }

    /**
     * ✅ TRAITER LES FICHIERS
     */
    private Map<String, Object> processFileField(FormField field, Object value) {
        if (!(value instanceof String) || !((String) value).startsWith("data:")) {
            return null;
        }

        try {
            String fileData = (String) value;
            String filename = field.getFieldName() + "_" + System.currentTimeMillis();
            String savedUrl = fileStorageService.saveBase64File(fileData, "uploads", filename);

            Map<String, Object> result = new HashMap<>();
            result.put("url", savedUrl);
            result.put("type", "file");
            result.put("originalSize", fileData.length());
            result.put("savedAt", LocalDateTime.now().toString());

            logger.info("Fichier sauvegardé: {}", savedUrl);
            return result;

        } catch (Exception e) {
            logger.error("Erreur sauvegarde fichier: {}", e.getMessage());
            throw new RuntimeException("Erreur lors de la sauvegarde du fichier");
        }
    }

    /**
     * ✅ TRAITER LES DESSINS
     */
    private Map<String, Object> processDrawingField(FormField field, Object value) {
        if (!(value instanceof String) || !((String) value).startsWith("data:image/")) {
            return null;
        }

        try {
            String drawingData = (String) value;
            String filename = field.getFieldName() + "_" + System.currentTimeMillis();
            String savedUrl = fileStorageService.saveBase64Image(drawingData, "drawings", filename);

            Map<String, Object> result = new HashMap<>();
            result.put("url", savedUrl);
            result.put("type", "drawing");
            result.put("originalSize", drawingData.length());
            result.put("savedAt", LocalDateTime.now().toString());

            logger.info("Dessin sauvegardé: {}", savedUrl);
            return result;

        } catch (Exception e) {
            logger.error("Erreur sauvegarde dessin: {}", e.getMessage());
            throw new RuntimeException("Erreur lors de la sauvegarde du dessin");
        }
    }

    /**
     * ✅ TRAITER LA GÉOLOCALISATION
     */
    private Map<String, Object> processGeolocationField(FormField field, Object value) {
        if (!(value instanceof Map)) {
            return null;
        }

        @SuppressWarnings("unchecked")
        Map<String, Object> geoData = (Map<String, Object>) value;

        Map<String, Object> result = new HashMap<>();
        result.put("latitude", geoData.get("latitude"));
        result.put("longitude", geoData.get("longitude"));
        result.put("accuracy", geoData.get("accuracy"));
        result.put("timestamp", LocalDateTime.now().toString());

        return result;
    }

    /**
     * ✅ TRAITER LES DATES
     */
    private String processDateTimeField(FormField field, Object value) {
        if (value instanceof String) {
            try {
                // Valider et normaliser le format de date
                LocalDateTime.parse((String) value);
                return (String) value;
            } catch (Exception e) {
                logger.warn("Format de date invalide: {}", value);
                return null;
            }
        }
        return null;
    }

    /**
     * ✅ TRAITER LES NOMBRES
     */
    private Number processNumberField(FormField field, Object value) {
        if (value instanceof Number) {
            return (Number) value;
        } else if (value instanceof String) {
            try {
                return Double.parseDouble((String) value);
            } catch (NumberFormatException e) {
                logger.warn("Format numérique invalide: {}", value);
                return null;
            }
        }
        return null;
    }

    /**
     * ✅ TRAITER LES BOOLÉENS
     */
    private Boolean processBooleanField(FormField field, Object value) {
        if (value instanceof Boolean) {
            return (Boolean) value;
        } else if (value instanceof String) {
            return Boolean.parseBoolean((String) value);
        }
        return false;
    }

    /**
     * ✅ TRAITER LES LISTES EXTERNES
     */
    private Object processExternalListField(FormField field, Object value) {
        // Validation spécifique pour les listes externes
        return value;
    }

    /**
     * ✅ TRAITER LES CHAMPS TEXTUELS
     */
    private String processTextualField(FormField field, Object value) {
        if (value instanceof String) {
            String textValue = ((String) value).trim();

            // Validation selon le type
            switch (field.getType()) {
                case "email":
                    if (!isValidEmail(textValue)) {
                        throw new RuntimeException("Format d'email invalide: " + textValue);
                    }
                    break;

                case "textarea":
                    // Limiter la longueur si nécessaire
                    if (textValue.length() > 5000) {
                        textValue = textValue.substring(0, 5000);
                        logger.warn("Texte tronqué pour le champ: {}", field.getFieldName());
                    }
                    break;
            }

            return textValue;
        }
        return null;
    }

    /**
     * ✅ CRÉER LES MÉTADONNÉES de soumission authentifiée
     */
    private Map<String, Object> createSubmissionMetadata(Utilisateur user, Form form) {
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("submittedBy", user.getUsername());
        metadata.put("submitterEmail", user.getEmail());
        metadata.put("submitterGroups", user.getGroups().stream().map(Group::getName).collect(Collectors.toList()));
        metadata.put("formName", form.getName());
        metadata.put("formId", form.getId());
        metadata.put("submissionTimestamp", LocalDateTime.now().toString());
        metadata.put("submissionType", "AUTHENTICATED");
        return metadata;
    }

    /**
     * ✅ CRÉER LES MÉTADONNÉES de soumission anonyme
     */
    private Map<String, Object> createAnonymousSubmissionMetadata(String clientIp, Form form) {
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("submitterIp", clientIp);
        metadata.put("formName", form.getName());
        metadata.put("formId", form.getId());
        metadata.put("submissionTimestamp", LocalDateTime.now().toString());
        metadata.put("submissionType", "ANONYMOUS");
        return metadata;
    }

    /**
     * ✅ CONVERTIR EN DTO de réponse
     */
    private FormSubmissionResponseDTO convertToSubmissionResponseDTO(FormSubmission submission) {
        FormSubmissionResponseDTO dto = new FormSubmissionResponseDTO();
        dto.setId(submission.getId());
        dto.setFormId(submission.getFormId());
        dto.setStatus(submission.getStatus());
        dto.setSubmittedAt(submission.getSubmittedAt());
        dto.setSubmitterEmail(submission.getSubmitterEmail());
        dto.setSubmitterIp(submission.getSubmitterIp());
        dto.setIsTemplate(submission.getIsTemplate());

        if (submission.getUtilisateur() != null) {
            dto.setSubmitterId(submission.getUtilisateur().getId());
            dto.setSubmitterName(submission.getUtilisateur().getUsername());
        }

        try {
            Map<String, Object> data = objectMapper.readValue(
                    submission.getData(),
                    objectMapper.getTypeFactory().constructMapType(Map.class, String.class, Object.class)
            );
            dto.setData(data);
        } catch (JsonProcessingException e) {
            logger.error("Erreur désérialisation données: {}", e.getMessage());
            dto.setData(new HashMap<>());
        }

        return dto;
    }

    /**
     * ✅ MÉTHODES UTILITAIRES
     */
    private boolean isValidEmail(String email) {
        return email != null && email.matches("^[A-Za-z0-9+_.-]+@([A-Za-z0-9.-]+\\.[A-Za-z]{2,})$");
    }
}