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
    /**
     * ✅ TRAITER LA VALEUR d'un champ selon son type - VERSION CORRIGÉE
     */
    /**
     * ✅ TRAITER LA VALEUR d'un champ selon son type - VERSION CORRIGÉE
     */
    private Object processFieldValue(FormField field, Object value) {
        if (value == null) {
            return null;
        }

        switch (field.getType()) {
            // Champs avec traitement spécial
            case "signature":
                return processSignatureField(field, value);

            case "file":
            case "attachment":
            case "file-fixed":
                return processFileField(field, value);

            case "drawing":
                return processDrawingField(field, value);

            case "geolocation":
                return processGeolocationField(field, value);

            case "datetime":
            case "date":
                return processDateTimeField(field, value);

            case "number":
            case "slider":
// Dans processFieldValue(), ajouter ce case pour calculation :
            case "calculation":
                if (value instanceof String) {
                    String calcValue = (String) value;
                    if (calcValue != null && !calcValue.trim().isEmpty() &&
                            !calcValue.equals("Erreur") && !calcValue.equals("Calcul non effectué")) {
                        try {
                            // Essayer de parser en nombre
                            return Double.parseDouble(calcValue.trim());
                        } catch (NumberFormatException e) {
                            // Si ce n'est pas un nombre, retourner la chaîne
                            return calcValue.trim();
                        }
                    }
                } else if (value instanceof Number) {
                    return value;
                }
                // Retourner 0 pour les calculs vides plutôt que null
                return 0;

            case "checkbox":
                // ✅ CORRECTION: Gérer les checkboxes multiples ET simples
                return processCheckboxField(field, value);

            case "external-list":
                return processExternalListField(field, value);

            // ✅ NOUVEAUX TYPES - Champs de sélection
            case "radio":
            case "select":
                return processSelectionField(field, value);

            // ✅ NOUVEAUX TYPES - Champs complexes
            case "address":
                return processAddressField(field, value);

            case "contact":
                return processContactField(field, value);

            case "reference":
                return processReferenceField(field, value);

            case "audio":
                return processAudioField(field, value);

            case "barcode":
            case "nfc":
                return processCodeField(field, value);
// Dans processFieldValue(), ajouter ce case pour calculation :

            case "table":
                return processTableField(field, value);

            case "schema":
                return processSchemaField(field, value);

            // ✅ Champs informatifs (pas de validation particulière)
            case "separator":
            case "fixed-text":
            case "image":
                return processInformationalField(field, value);

            // ✅ Champs textuels
            case "text":
            case "textarea":
            case "email":
            default:
                return processTextualField(field, value);
        }
    }
    private Object processCheckboxField(FormField field, Object value) {
        // ✅ DEBUG : Ajoutez ces logs
        logger.debug("Processing checkbox field: {}, value: {}, type: {}",
                field.getFieldName(), value, value != null ? value.getClass().getName() : "null");

        // Si c'est un array (checkboxes multiples)
        if (value instanceof List) {
            @SuppressWarnings("unchecked")
            List<Object> listValue = (List<Object>) value;

            // ✅ DEBUG : Log de la liste
            logger.debug("List size: {}, contents: {}", listValue.size(), listValue);

            List<String> result = listValue.stream()
                    .map(item -> item != null ? item.toString().trim() : null)
                    .filter(item -> item != null && !item.isEmpty())
                    .collect(Collectors.toList());

            // ✅ DEBUG : Log du résultat
            logger.debug("Filtered result: {}", result);

            return result;
        }

        // ✅ Vérifier si le champ a des options pour déterminer le comportement
        if (field.getOptions() != null && !field.getOptions().isEmpty()) {
            // Checkbox multiple - retourner array même si vide
            return new ArrayList<>();
        } else {
            // Checkbox simple - retourner boolean
            return processBooleanField(field, value);
        }
    }
    private Object processSelectionField(FormField field, Object value) {
        if (value instanceof String) {
            return ((String) value).trim();
        } else if (value instanceof List) {
            // Pour les checkboxes multiples
            @SuppressWarnings("unchecked")
            List<Object> listValue = (List<Object>) value;
            return listValue.stream()
                    .map(item -> item != null ? item.toString().trim() : null)
                    .filter(item -> item != null && !item.isEmpty())
                    .collect(Collectors.toList());
        }
        return null;
    }
    /**
     * ✅ TRAITER LES ADRESSES
     */
    /**
     * ✅ TRAITER LES ADRESSES - VERSION CORRIGÉE
     */
    private Object processAddressField(FormField field, Object value) {
        if (value instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, Object> addressData = (Map<String, Object>) value;

            Map<String, Object> result = new HashMap<>();

            // ✅ CORRECTION : Gérer les clés exactes envoyées par le frontend
            result.put("fullAddress", sanitizeString((String) addressData.get("fullAddress")));
            result.put("zipCode", sanitizeString((String) addressData.get("zipCode")));
            result.put("city", sanitizeString((String) addressData.get("city")));

            // Champs optionnels avec noms alternatifs
            result.put("street", sanitizeString((String) addressData.get("street")));
            result.put("postalCode", sanitizeString((String) addressData.get("postalCode")));
            result.put("country", sanitizeString((String) addressData.get("country")));
            result.put("latitude", addressData.get("latitude"));
            result.put("longitude", addressData.get("longitude"));

            // ✅ DEBUG : Log pour vérification
            logger.debug("Processing address field - Input: {}, Output: {}", addressData, result);

            return result;

        } else if (value instanceof String) {
            // Si c'est une chaîne simple, la traiter comme adresse textuelle
            Map<String, Object> result = new HashMap<>();
            result.put("fullAddress", sanitizeString((String) value));
            result.put("zipCode", "");
            result.put("city", "");
            return result;
        }

        return null;
    }
    /**
     * ✅ TRAITER LES CONTACTS
     */
    private Map<String, Object> processContactField(FormField field, Object value) {
        if (value instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, Object> contactData = (Map<String, Object>) value;

            Map<String, Object> result = new HashMap<>();
            result.put("name", sanitizeString((String) contactData.get("name")));
            result.put("phone", sanitizeString((String) contactData.get("phone")));
            result.put("email", sanitizeString((String) contactData.get("email")));
            result.put("company", sanitizeString((String) contactData.get("company")));

            // Valider l'email si présent
            String email = (String) result.get("email");
            if (email != null && !email.isEmpty() && !isValidEmail(email)) {
                logger.warn("Email invalide dans contact: {}", email);
                result.put("email", null);
            }

            return result;
        }
        return null;
    }

    /**
     * ✅ TRAITER LES RÉFÉRENCES
     */
    private Map<String, Object> processReferenceField(FormField field, Object value) {
        if (value instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, Object> refData = (Map<String, Object>) value;

            Map<String, Object> result = new HashMap<>();
            result.put("id", refData.get("id"));
            result.put("label", sanitizeString((String) refData.get("label")));
            result.put("type", sanitizeString((String) refData.get("type")));
            result.put("url", sanitizeString((String) refData.get("url")));

            return result;
        } else if (value instanceof String) {
            // Référence simple sous forme de texte
            Map<String, Object> result = new HashMap<>();
            result.put("value", sanitizeString((String) value));
            return result;
        }
        return null;
    }

    /**
     * ✅ TRAITER LES FICHIERS AUDIO
     */
    private Map<String, Object> processAudioField(FormField field, Object value) {
        if (!(value instanceof String) || !((String) value).startsWith("data:audio/")) {
            return null;
        }

        try {
            String audioData = (String) value;
            String filename = field.getFieldName() + "_" + System.currentTimeMillis();
            String savedUrl = fileStorageService.saveBase64File(audioData, "audio", filename);

            Map<String, Object> result = new HashMap<>();
            result.put("url", savedUrl);
            result.put("type", "audio");
            result.put("originalSize", audioData.length());
            result.put("savedAt", LocalDateTime.now().toString());

            logger.info("Fichier audio sauvegardé: {}", savedUrl);
            return result;

        } catch (Exception e) {
            logger.error("Erreur sauvegarde audio: {}", e.getMessage());
            throw new RuntimeException("Erreur lors de la sauvegarde du fichier audio");
        }
    }

    /**
     * ✅ TRAITER LES CODES (barcode, nfc)
     */
    private Map<String, Object> processCodeField(FormField field, Object value) {
        if (value instanceof String) {
            String code = sanitizeString((String) value);

            Map<String, Object> result = new HashMap<>();
            result.put("code", code);
            result.put("type", field.getType());
            result.put("scannedAt", LocalDateTime.now().toString());

            return result;
        }
        return null;
    }

    /**
     * ✅ TRAITER LES TABLEAUX
     */
    private List<Map<String, Object>> processTableField(FormField field, Object value) {
        if (value instanceof List) {
            @SuppressWarnings("unchecked")
            List<Object> tableData = (List<Object>) value;

            List<Map<String, Object>> result = new ArrayList<>();

            for (Object row : tableData) {
                if (row instanceof Map) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> rowData = (Map<String, Object>) row;

                    Map<String, Object> processedRow = new HashMap<>();
                    rowData.forEach((key, val) -> {
                        if (val instanceof String) {
                            processedRow.put(key, sanitizeString((String) val));
                        } else {
                            processedRow.put(key, val);
                        }
                    });

                    result.add(processedRow);
                }
            }

            return result;
        }
        return null;
    }

    /**
     * ✅ TRAITER LES SCHÉMAS/STRUCTURES
     */
    private Map<String, Object> processSchemaField(FormField field, Object value) {
        if (value instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, Object> schemaData = (Map<String, Object>) value;

            // Nettoyer récursivement les données du schéma
            return cleanMapData(schemaData);
        }
        return null;
    }

    /**
     * ✅ TRAITER LES CHAMPS INFORMATIFS (pas de données à sauvegarder)
     */
    private Object processInformationalField(FormField field, Object value) {
        if (value != null) {
            Map<String, Object> result = new HashMap<>();
            result.put("type", field.getType());
            result.put("acknowledged", true);
            result.put("timestamp", LocalDateTime.now().toString());

            // ✅ CORRECTION pour tous les champs informatifs
            if (field.getAttributes() != null) {
                try {
                    Map<String, Object> attributes = objectMapper.readValue(
                            field.getAttributes(),
                            objectMapper.getTypeFactory().constructMapType(Map.class, String.class, Object.class)
                    );

                    switch (field.getType()) {
                        case "image":
                            String imageUrl = (String) attributes.get("imageUrl");
                            if (imageUrl != null && !imageUrl.isEmpty()) {
                                result.put("imageUrl", imageUrl);
                                result.put("fileName", attributes.get("fileName"));
                                result.put("fileType", attributes.get("fileType"));
                            }
                            break;

                        case "file-fixed":
                            String fileUrl = (String) attributes.get("fileUrl");
                            if (fileUrl != null && !fileUrl.isEmpty()) {
                                result.put("fileUrl", fileUrl);
                                result.put("fileName", attributes.get("fileName"));
                                result.put("fileType", attributes.get("fileType"));
                            }
                            break;

                        case "fixed-text":
                            String content = (String) attributes.get("content");
                            if (content != null && !content.isEmpty()) {
                                result.put("content", content);
                            }
                            break;
                    }
                } catch (JsonProcessingException e) {
                    logger.error("Erreur parsing attributes pour champ {}: {}", field.getType(), e.getMessage());
                }
            }

            return result;
        }
        return null;
    }    private String sanitizeString(String input) {
        if (input == null) return null;
        return input.trim().replaceAll("[\\r\\n\\t]", " ").replaceAll("\\s+", " ");
    }

    private Map<String, Object> cleanMapData(Map<String, Object> data) {
        Map<String, Object> cleaned = new HashMap<>();

        data.forEach((key, value) -> {
            if (value instanceof String) {
                cleaned.put(key, sanitizeString((String) value));
            } else if (value instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, Object> mapValue = (Map<String, Object>) value;
                cleaned.put(key, cleanMapData(mapValue));
            } else if (value instanceof List) {
                // Traiter les listes récursivement si nécessaire
                cleaned.put(key, value);
            } else {
                cleaned.put(key, value);
            }
        });

        return cleaned;
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
        if (value == null) {
            return null;
        }

        // Si c'est déjà un objet Map (données GPS)
        if (value instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, Object> geoData = (Map<String, Object>) value;

            Map<String, Object> result = new HashMap<>();
            result.put("latitude", geoData.get("latitude"));
            result.put("longitude", geoData.get("longitude"));
            result.put("accuracy", geoData.get("accuracy"));
            result.put("source", geoData.get("source"));
            result.put("timestamp", geoData.get("timestamp"));

            return result;
        }

        // Si c'est une chaîne (saisie manuelle)
        if (value instanceof String) {
            String coordString = (String) value;

            // Ignorer les messages temporaires
            if (coordString.contains("cours") || coordString.trim().isEmpty()) {
                return null;
            }

            // Essayer de parser les coordonnées manuelles
            String[] coords = coordString.split(",");
            if (coords.length == 2) {
                try {
                    double latitude = Double.parseDouble(coords[0].trim());
                    double longitude = Double.parseDouble(coords[1].trim());

                    Map<String, Object> result = new HashMap<>();
                    result.put("latitude", latitude);
                    result.put("longitude", longitude);
                    result.put("accuracy", null);
                    result.put("source", "manual");
                    result.put("timestamp", LocalDateTime.now().toString());

                    return result;
                } catch (NumberFormatException e) {
                    logger.warn("Format de coordonnées invalide: {}", coordString);
                    return null;
                }
            }
        }

        return null;
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