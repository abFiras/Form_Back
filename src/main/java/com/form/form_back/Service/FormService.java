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
public class FormService {

    @Autowired
    private FormRepository formRepository;

    @Autowired
    private FormFieldRepository formFieldRepository;

    @Autowired
    private FormSubmissionRepository formSubmissionRepository;

    @Autowired
    private UtilisateurRepository userRepository;

    @Autowired
    private GroupRepository groupRepository;

    @Autowired
    private FileStorageService fileStorageService;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private static final Logger logger = LoggerFactory.getLogger(FormService.class);

    // ✅ CRÉER UN FORMULAIRE avec assignation de groupes automatique
    // Méthode createForm corrigée dans FormService.java
    // ✅ CRÉATION DE FORMULAIRE CORRIGÉE - Les champs ne dépendent PAS des groupes
    @Transactional
    public FormDTO createForm(FormCreateRequest request) {
        // Valider l'utilisateur créateur
        Utilisateur creator = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

        // ✅ ÉTAPE 1 : Créer le formulaire de base
        Form form = new Form();
        form.setName(request.getName());
        form.setDescription(request.getDescription());
        form.setCreatedBy(creator);
        form.setStatus("DRAFT");

        // ✅ ÉTAPE 2 : Assigner les groupes (INDÉPENDAMMENT des champs)
        Set<Group> assignedGroups = new HashSet<>();
        if (request.getGroupIds() != null && !request.getGroupIds().isEmpty()) {
            for (Long groupId : request.getGroupIds()) {
                Group group = groupRepository.findById(groupId)
                        .orElseThrow(() -> new RuntimeException("Groupe non trouvé: " + groupId));
                assignedGroups.add(group);
            }
            logger.info("Groupes assignés au formulaire: {}",
                    request.getGroupIds().stream().map(String::valueOf).collect(Collectors.joining(", ")));
        }
        form.setAssignedGroups(assignedGroups);

        // ✅ SAUVEGARDER LE FORMULAIRE D'ABORD
        Form savedForm = formRepository.save(form);

        // ✅ ÉTAPE 3 : Créer les champs UNE SEULE FOIS (peu importe le nombre de groupes)
        if (request.getFields() != null && !request.getFields().isEmpty()) {
            logger.info("Création de {} champs pour le formulaire {}",
                    request.getFields().size(), savedForm.getName());

            // ✅ CRUCIAL : Une seule boucle pour créer les champs
            for (int i = 0; i < request.getFields().size(); i++) {
                FormFieldCreateDTO fieldDto = request.getFields().get(i);

                // Générer un fieldName unique si nécessaire
                if (fieldDto.getFieldName() == null || fieldDto.getFieldName().trim().isEmpty()) {
                    fieldDto.setFieldName(generateUniqueFieldName(fieldDto.getType(), i));
                }

                FormField field = createFormFieldFromDTO(fieldDto, savedForm, i);
                FormField savedField = formFieldRepository.save(field);

                logger.debug("Champ créé: {} (ordre: {})", savedField.getFieldName(), savedField.getOrder());
            }

            logger.info("Tous les champs ont été créés avec succès");
        }

        logger.info("Formulaire créé: '{}' avec {} groupes et {} champs",
                savedForm.getName(),
                savedForm.getAssignedGroups().size(),
                request.getFields() != null ? request.getFields().size() : 0);

        return convertToDTO(savedForm, creator.getId());
    }

    // ✅ MÉTHODE pour générer des noms de champs uniques
    private String generateUniqueFieldName(String fieldType, int index) {
        String baseType = fieldType.replaceAll("[^a-zA-Z0-9]", "_");
        long timestamp = System.currentTimeMillis();
        return String.format("%s_%d_%d", baseType, index, timestamp);
    }
    // ✅ OBTENIR LES FORMULAIRES PUBLIÉS accessibles à l'utilisateur
    public List<FormDTO> getPublishedFormsForUser(Long userId) {
        Utilisateur user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

        List<Form> publishedForms = formRepository.findByStatusWithGroups("PUBLISHED");
        List<FormDTO> accessibleForms = new ArrayList<>();

        for (Form form : publishedForms) {
            // ✅ Vérifier si l'utilisateur a accès via ses groupes
            if (form.isAccessibleByUser(user)) {
                FormDTO dto = convertToDTO(form, userId);
                accessibleForms.add(dto);

                logger.debug("Formulaire {} accessible pour utilisateur {} via groupes: {}",
                        form.getName(), user.getUsername(),
                        form.getAssignedGroups().stream().map(Group::getName).collect(Collectors.toList()));
            }
        }

        logger.info("Trouvé {} formulaires publiés accessibles pour {}",
                accessibleForms.size(), user.getUsername());

        return accessibleForms;
    }

    // ✅ PUBLIER UN FORMULAIRE et créer UNE SEULE template vide par groupe
    public FormDTO publishForm(Long formId, Long userId) {
        Form form = formRepository.findById(formId)
                .orElseThrow(() -> new RuntimeException("Formulaire non trouvé"));

        if (!form.getCreatedBy().getId().equals(userId)) {
            throw new RuntimeException("Seul le créateur peut publier ce formulaire");
        }

        form.setStatus("PUBLISHED");
        Form savedForm = formRepository.save(form);

        // ✅ CRÉATION DE TEMPLATES : Une seule template vide qui servira de modèle
        createSingleEmptyTemplate(savedForm);

        logger.info("Formulaire publié: {} avec {} groupes assignés",
                savedForm.getName(), savedForm.getAssignedGroups().size());

        return convertToDTO(savedForm, userId);
    }

    // ✅ NOUVELLE MÉTHODE : Créer une seule template vide par formulaire
    private void createSingleEmptyTemplate(Form form) {
        try {
            // Supprimer les anciennes templates si elles existent
            formSubmissionRepository.deleteByFormIdAndIsTemplateTrue(form.getId());

            // Créer une seule template vide
            FormSubmission template = new FormSubmission();
            template.setFormId(form.getId());
            template.setData(objectMapper.writeValueAsString(new HashMap<>())); // JSON vide
            template.setIsTemplate(true);
            template.setStatus("TEMPLATE");
            template.setSubmittedAt(LocalDateTime.now());

            // Assigner au créateur du formulaire
            template.setUtilisateur(form.getCreatedBy());
            template.setSubmitterEmail(form.getCreatedBy().getEmail());

            formSubmissionRepository.save(template);

            logger.info("Template unique créée pour le formulaire: {}", form.getName());

        } catch (Exception e) {
            logger.error("Erreur lors de la création de la template: {}", e.getMessage());
        }
    }

    // ✅ SOUMETTRE UN FORMULAIRE - Créer une nouvelle soumission sans toucher à la template
    public FormSubmissionResponseDTO submitForm(Long formId, FormSubmissionRequest request, Long userId) {
        Form form = formRepository.findByIdWithFields(formId);
        if (form == null) {
            throw new RuntimeException("Formulaire non trouvé");
        }

        Utilisateur user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

        // ✅ VÉRIFICATIONS D'ACCÈS strictes
        if (!form.isAccessibleByUser(user)) {
            throw new RuntimeException("Accès non autorisé à ce formulaire. Vous devez appartenir à un des groupes assignés: " +
                    form.getAssignedGroups().stream().map(Group::getName).collect(Collectors.joining(", ")));
        }

        if (!"PUBLISHED".equals(form.getStatus())) {
            throw new RuntimeException("Ce formulaire n'est pas encore publié");
        }

        try {
            // ✅ CRÉER UNE NOUVELLE SOUMISSION (pas une template)
            FormSubmission submission = new FormSubmission();
            submission.setFormId(formId);
            submission.setUtilisateur(user);
            submission.setIsTemplate(false); // ✅ IMPORTANT : Vraie soumission
            submission.setStatus("SUBMITTED");
            submission.setSubmittedAt(LocalDateTime.now());
            submission.setSubmitterEmail(user.getEmail());

            // Traiter et valider les données
            Map<String, Object> processedData = processSubmissionData(request.getData(), form.getFields());
            submission.setData(objectMapper.writeValueAsString(processedData));

            FormSubmission savedSubmission = formSubmissionRepository.save(submission);

            logger.info("NOUVELLE soumission créée par {} pour le formulaire {} (Template préservée)",
                    user.getUsername(), form.getName());

            return convertToSubmissionResponseDTO(savedSubmission);

        } catch (JsonProcessingException e) {
            logger.error("Erreur lors de la sérialisation des données: {}", e.getMessage());
            throw new RuntimeException("Erreur lors de l'enregistrement de la soumission");
        }
    }

    // ✅ OBTENIR LES SOUMISSIONS (exclut les templates)
    public List<FormSubmissionResponseDTO> getFormSubmissions(Long formId, Long userId) {
        Form form = formRepository.findById(formId)
                .orElseThrow(() -> new RuntimeException("Formulaire non trouvé"));

        // Vérifier que l'utilisateur peut voir les soumissions (créateur ou admin)
        if (!form.getCreatedBy().getId().equals(userId)) {
            throw new RuntimeException("Seul le créateur peut voir les soumissions");
        }

        // ✅ Récupérer UNIQUEMENT les vraies soumissions (isTemplate = false)
        List<FormSubmission> realSubmissions = formSubmissionRepository
                .findByFormIdAndIsTemplateFalseOrderBySubmittedAtDesc(formId);

        logger.info("Trouvé {} vraies soumissions pour le formulaire {} (templates exclues)",
                realSubmissions.size(), form.getName());

        return realSubmissions.stream()
                .map(this::convertToSubmissionResponseDTO)
                .collect(Collectors.toList());
    }

    // ✅ ASSIGNER DES GROUPES À UN FORMULAIRE
    public FormDTO assignGroupsToForm(Long formId, List<Long> groupIds, Long userId) {
        Form form = formRepository.findById(formId)
                .orElseThrow(() -> new RuntimeException("Formulaire non trouvé"));

        // Vérifier que l'utilisateur est le créateur du formulaire
        if (!form.getCreatedBy().getId().equals(userId)) {
            throw new RuntimeException("Seul le créateur peut assigner des groupes à ce formulaire");
        }

        // Vider les anciens groupes
        form.getAssignedGroups().clear();

        // Assigner les nouveaux groupes
        if (groupIds != null && !groupIds.isEmpty()) {
            Set<Group> groups = new HashSet<>();
            for (Long groupId : groupIds) {
                Group group = groupRepository.findById(groupId)
                        .orElseThrow(() -> new RuntimeException("Groupe non trouvé: " + groupId));
                groups.add(group);
            }
            form.setAssignedGroups(groups);
        }

        Form savedForm = formRepository.save(form);

        logger.info("Groupes assignés au formulaire {}: {}", formId, groupIds);

        return convertToDTO(savedForm, userId);
    }

    @Transactional
    public FormDTO updateForm(Long formId, FormUpdateRequest request, Long userId) {
        Form form = formRepository.findById(formId)
                .orElseThrow(() -> new RuntimeException("Formulaire non trouvé"));

        if (!form.getCreatedBy().getId().equals(userId)) {
            throw new RuntimeException("Seul le créateur peut modifier ce formulaire");
        }

        logger.info("Début de mise à jour du formulaire {}", formId);

        // ✅ ÉTAPE 1 : Mettre à jour les informations de base
        form.setName(request.getName());
        form.setDescription(request.getDescription());
        if (request.getStatus() != null) {
            form.setStatus(request.getStatus());
        }

        // ✅ ÉTAPE 2 : Mettre à jour les groupes (INDÉPENDAMMENT des champs)
        form.getAssignedGroups().clear();
        if (request.getGroupIds() != null && !request.getGroupIds().isEmpty()) {
            Set<Group> groups = new HashSet<>();
            for (Long groupId : request.getGroupIds()) {
                Group group = groupRepository.findById(groupId)
                        .orElseThrow(() -> new RuntimeException("Groupe non trouvé: " + groupId));
                groups.add(group);
            }
            form.setAssignedGroups(groups);
            logger.info("Groupes mis à jour: {}",
                    request.getGroupIds().stream().map(String::valueOf).collect(Collectors.joining(", ")));
        }

        // ✅ ÉTAPE 3 : SOLUTION PROPRE pour les champs - Supprimer tous et recréer
        try {
            // Compter les anciens champs
            List<FormField> oldFields = formFieldRepository.findByFormIdOrderByOrder(formId);
            int oldFieldCount = oldFields.size();

            // Supprimer TOUS les anciens champs
            logger.info("Suppression de {} anciens champs", oldFieldCount);
            formFieldRepository.deleteByFormId(formId);
            formFieldRepository.flush(); // S'assurer que la suppression est effective

            // Créer les nouveaux champs
            if (request.getFields() != null && !request.getFields().isEmpty()) {
                logger.info("Création de {} nouveaux champs", request.getFields().size());

                for (int i = 0; i < request.getFields().size(); i++) {
                    FormFieldCreateDTO fieldDto = request.getFields().get(i);

                    // Générer un fieldName absolument unique
                    String uniqueFieldName = generateUniqueFieldName(
                            fieldDto.getFieldName() != null ? fieldDto.getFieldName() : fieldDto.getType(),
                            i
                    );
                    fieldDto.setFieldName(uniqueFieldName);

                    FormField newField = createFormFieldFromDTO(fieldDto, form, i);
                    FormField savedField = formFieldRepository.save(newField);

                    logger.debug("Nouveau champ créé: {} (ordre: {})",
                            savedField.getFieldName(), savedField.getOrder());
                }

                formFieldRepository.flush(); // S'assurer que la création est effective
            }

        } catch (Exception e) {
            logger.error("Erreur lors de la mise à jour des champs: {}", e.getMessage(), e);
            throw new RuntimeException("Erreur lors de la mise à jour des champs: " + e.getMessage());
        }

        // ✅ ÉTAPE 4 : Sauvegarder le formulaire final
        Form savedForm = formRepository.save(form);

        logger.info("Formulaire mis à jour avec succès: '{}' - {} groupes, {} champs",
                savedForm.getName(),
                savedForm.getAssignedGroups().size(),
                request.getFields() != null ? request.getFields().size() : 0);

        return convertToDTO(savedForm, userId);
    }


    // ✅ NOUVELLE MÉTHODE : Garantir l'unicité des fieldName
    private String ensureUniqueFieldName(String originalFieldName, Long formId, int index) {
        if (originalFieldName == null || originalFieldName.trim().isEmpty()) {
            // Générer un nom complètement unique basé sur timestamp + random
            return "field_" + System.currentTimeMillis() + "_" + index + "_" +
                    Integer.toHexString(new Random().nextInt(65536));
        }

        // Nettoyer le nom original et ajouter un suffixe unique si nécessaire
        String cleanedName = originalFieldName.trim().replaceAll("[^a-zA-Z0-9_]", "_");

        // Ajouter un timestamp pour garantir l'unicité
        return cleanedName + "_" + System.currentTimeMillis() + "_" + index;
    }

    // ✅ MÉTHODE ALTERNATIVE : Version plus sophistiquée si vous voulez préserver les noms existants
    @Transactional
    public FormDTO updateFormAdvanced(Long formId, FormUpdateRequest request, Long userId) {
        Form form = formRepository.findById(formId)
                .orElseThrow(() -> new RuntimeException("Formulaire non trouvé"));

        if (!form.getCreatedBy().getId().equals(userId)) {
            throw new RuntimeException("Seul le créateur peut modifier ce formulaire");
        }

        // Mettre à jour les informations de base
        form.setName(request.getName());
        form.setDescription(request.getDescription());
        if (request.getStatus() != null) {
            form.setStatus(request.getStatus());
        }

        // Mettre à jour les groupes
        form.getAssignedGroups().clear();
        if (request.getGroupIds() != null && !request.getGroupIds().isEmpty()) {
            Set<Group> groups = new HashSet<>();
            for (Long groupId : request.getGroupIds()) {
                Group group = groupRepository.findById(groupId)
                        .orElseThrow(() -> new RuntimeException("Groupe non trouvé: " + groupId));
                groups.add(group);
            }
            form.setAssignedGroups(groups);
        }

        // ✅ APPROCHE ALTERNATIVE : Utiliser les IDs au lieu des noms pour éviter les doublons
        List<FormField> existingFields = formFieldRepository.findByFormIdOrderByOrder(formId);
        Set<Long> fieldsToKeep = new HashSet<>();

        if (request.getFields() != null) {
            for (int i = 0; i < request.getFields().size(); i++) {
                FormFieldCreateDTO fieldDto = request.getFields().get(i);

                // Chercher un champ existant par son ordre ou créer un nouveau
                FormField targetField = null;
                if (i < existingFields.size()) {
                    targetField = existingFields.get(i);
                    fieldsToKeep.add(targetField.getId());
                    updateExistingFieldAdvanced(targetField, fieldDto, i);
                } else {
                    targetField = createFormFieldFromDTO(fieldDto, form, i);
                }

                formFieldRepository.save(targetField);
            }
        }

        // Supprimer les champs non utilisés
        for (FormField existingField : existingFields) {
            if (!fieldsToKeep.contains(existingField.getId())) {
                formFieldRepository.delete(existingField);
            }
        }

        formFieldRepository.flush();
        Form savedForm = formRepository.save(form);

        return convertToDTO(savedForm, userId);
    }

    // ✅ Méthode d'assistance pour l'approche avancée
    private void updateExistingFieldAdvanced(FormField existingField, FormFieldCreateDTO dto, int newOrder) {
        existingField.setType(dto.getType());
        existingField.setLabel(dto.getLabel());
        existingField.setPlaceholder(dto.getPlaceholder());
        existingField.setOrder(newOrder);
        existingField.setRequired(dto.getRequired() != null ? dto.getRequired() : false);

        // Générer un nouveau fieldName unique basé sur l'ordre et le timestamp
        existingField.setFieldName("field_" + newOrder + "_" + System.currentTimeMillis());

        // Mettre à jour les options
        if (dto.getOptions() != null && !dto.getOptions().isEmpty()) {
            try {
                existingField.setOptions(objectMapper.writeValueAsString(dto.getOptions()));
            } catch (JsonProcessingException e) {
                logger.error("Erreur sérialisation options: {}", e.getMessage());
                existingField.setOptions(null);
            }
        } else {
            existingField.setOptions(null);
        }

        // Mettre à jour les attributs
        if (dto.getAttributes() != null && !dto.getAttributes().isEmpty()) {
            try {
                existingField.setAttributes(objectMapper.writeValueAsString(dto.getAttributes()));
            } catch (JsonProcessingException e) {
                logger.error("Erreur sérialisation attributs: {}", e.getMessage());
                existingField.setAttributes(null);
            }
        } else {
            existingField.setAttributes(null);
        }
    }

    // ✅ NOUVELLE MÉTHODE : Mettre à jour un champ existant
    private void updateExistingField(FormField existingField, FormFieldCreateDTO dto, int newOrder) {
        existingField.setType(dto.getType());
        existingField.setLabel(dto.getLabel());
        existingField.setPlaceholder(dto.getPlaceholder());
        existingField.setOrder(dto.getOrder() != null ? dto.getOrder() : newOrder);
        existingField.setRequired(dto.getRequired() != null ? dto.getRequired() : false);

        // Mettre à jour les options
        if (dto.getOptions() != null && !dto.getOptions().isEmpty()) {
            try {
                existingField.setOptions(objectMapper.writeValueAsString(dto.getOptions()));
            } catch (JsonProcessingException e) {
                logger.error("Erreur sérialisation options: {}", e.getMessage());
                existingField.setOptions(null);
            }
        } else {
            existingField.setOptions(null);
        }

        // Mettre à jour les attributs
        if (dto.getAttributes() != null && !dto.getAttributes().isEmpty()) {
            try {
                existingField.setAttributes(objectMapper.writeValueAsString(dto.getAttributes()));
            } catch (JsonProcessingException e) {
                logger.error("Erreur sérialisation attributs: {}", e.getMessage());
                existingField.setAttributes(null);
            }
        } else {
            existingField.setAttributes(null);
        }
    }

    // ✅ AUTRES MÉTHODES (getFormsForUser, getFormById, etc.) - garder les implémentations existantes
    public List<FormDTO> getFormsForUser(Long userId) {
        Utilisateur user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

        List<Form> allForms = formRepository.findAllWithGroupsAndCreator();
        List<FormDTO> accessibleForms = new ArrayList<>();

        for (Form form : allForms) {
            if (form.isAccessibleByUser(user)) {
                FormDTO dto = convertToDTO(form, userId);
                accessibleForms.add(dto);
            }
        }

        return accessibleForms;
    }

    // FormService.java - Méthode getFormById corrigée pour éviter la duplication
    @Transactional(readOnly = true)
    public FormDTO getFormById(Long formId, Long userId) {
        logger.info("=== DÉBUT getFormById - ID: {} pour utilisateur: {} ===", formId, userId);

        // ✅ SOLUTION 1: Utiliser une requête séparée pour éviter le produit cartésien
        // Récupérer le formulaire de base avec l'utilisateur créateur
        Form form = formRepository.findById(formId)
                .orElseThrow(() -> new RuntimeException("Formulaire non trouvé"));

        logger.info("Formulaire trouvé: {}", form.getName());

        // ✅ Récupérer les champs séparément pour éviter la duplication
        List<FormField> fields = formFieldRepository.findByFormIdOrderByOrder(formId);
        logger.info("Champs récupérés de la DB: {} champs", fields.size());

        // ✅ Log détaillé des champs pour debugging
        if (!fields.isEmpty()) {
            logger.info("=== ANALYSE DES CHAMPS DE LA DB ===");
            Map<String, Long> fieldNameCounts = fields.stream()
                    .collect(Collectors.groupingBy(FormField::getFieldName, Collectors.counting()));

            fieldNameCounts.forEach((fieldName, count) -> {
                if (count > 1) {
                    logger.error("❌ DOUBLON EN DB: fieldName '{}' apparaît {} fois", fieldName, count);
                } else {
                    logger.debug("✅ Champ unique en DB: '{}'", fieldName);
                }
            });

            // Log de tous les champs
            for (FormField field : fields) {
                logger.debug("Champ DB: id={}, fieldName='{}', order={}, formId={}",
                        field.getId(), field.getFieldName(), field.getOrder(), field.getForm().getId());
            }
        }

        // ✅ Assigner manuellement les champs au formulaire
        form.setFields(fields);

        // ✅ Récupérer les groupes séparément si nécessaire
        if (form.getAssignedGroups() == null || form.getAssignedGroups().isEmpty()) {
            // Recharger avec les groupes si pas déjà chargés
            Form formWithGroups = formRepository.findByIdWithGroupsOnly(formId).orElse(form);
            form.setAssignedGroups(formWithGroups.getAssignedGroups());
        }

        logger.info("Groupes assignés: {}", form.getAssignedGroups().size());

        // ✅ Vérification d'accès
        Utilisateur user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

        if (!form.isAccessibleByUser(user)) {
            throw new RuntimeException("Accès non autorisé à ce formulaire");
        }

        // ✅ Conversion en DTO
        FormDTO dto = convertToDTO(form, userId);

        logger.info("=== FIN getFormById - Retour: {} champs ===",
                dto.getFields() != null ? dto.getFields().size() : 0);

        return dto;
    }

    // ✅ MÉTHODE ALTERNATIVE: Si le problème persiste, utiliser cette approche encore plus sûre


    // ✅ CORRECTION du convertToDTO pour éviter les doublons
    private FormDTO convertToDTO(Form form, Long currentUserId) {
        FormDTO dto = new FormDTO();
        dto.setId(form.getId());
        dto.setName(form.getName());
        dto.setDescription(form.getDescription());
        dto.setStatus(form.getStatus());
        dto.setCreatedAt(form.getCreatedAt());
        dto.setUpdatedAt(form.getUpdatedAt());

        if (form.getCreatedBy() != null) {
            dto.setCreatedBy(form.getCreatedBy().getId());
            dto.setCanEdit(currentUserId != null && currentUserId.equals(form.getCreatedBy().getId()));
        }

        // ✅ Groupes assignés
        if (form.getAssignedGroups() != null && !form.getAssignedGroups().isEmpty()) {
            dto.setAssignedGroupIds(form.getAssignedGroups().stream()
                    .map(Group::getId)
                    .distinct() // ✅ Sécurité supplémentaire
                    .collect(Collectors.toList()));

            dto.setAssignedGroups(form.getAssignedGroups().stream()
                    .map(this::convertGroupToDTO)
                    .distinct() // ✅ Éviter les doublons de groupes aussi
                    .collect(Collectors.toList()));
        }

        // ✅ CORRECTION CRITIQUE: Éliminer les doublons de champs
        if (form.getFields() != null && !form.getFields().isEmpty()) {
            logger.debug("Conversion DTO: {} champs bruts reçus", form.getFields().size());

            // Éliminer les doublons par fieldName et conserver l'ordre
            Map<String, FormField> uniqueFieldsMap = new LinkedHashMap<>();

            form.getFields().stream()
                    .sorted(Comparator.comparing(FormField::getOrder))
                    .forEach(field -> {
                        String fieldName = field.getFieldName();
                        if (!uniqueFieldsMap.containsKey(fieldName)) {
                            uniqueFieldsMap.put(fieldName, field);
                            logger.debug("Champ ajouté au DTO: {}", fieldName);
                        } else {
                            logger.warn("Champ doublon ignoré dans DTO: {}", fieldName);
                        }
                    });

            dto.setFields(uniqueFieldsMap.values().stream()
                    .map(this::convertToFieldDTO)
                    .collect(Collectors.toList()));

            logger.debug("Conversion DTO: {} champs uniques dans le résultat final", dto.getFields().size());
        }

        // ✅ Vérifier l'accessibilité
        if (currentUserId != null) {
            try {
                Utilisateur user = userRepository.findById(currentUserId).orElse(null);
                dto.setIsAccessible(user != null && form.isAccessibleByUser(user));
            } catch (Exception e) {
                dto.setIsAccessible(false);
            }
        } else {
            dto.setIsAccessible(true);
        }

        return dto;
    }

    public FormDTO getPublicForm(Long formId) {
        Form form = formRepository.findByIdWithFields(formId);
        if (form == null || !"PUBLISHED".equals(form.getStatus())) {
            throw new RuntimeException("Formulaire non disponible");
        }

        return convertToDTO(form, null);
    }

    public void deleteForm(Long formId, Long userId) {
        Form form = formRepository.findById(formId)
                .orElseThrow(() -> new RuntimeException("Formulaire non trouvé"));

        if (!form.getCreatedBy().getId().equals(userId)) {
            throw new RuntimeException("Seul le créateur peut supprimer ce formulaire");
        }

        formRepository.deleteById(formId);
        logger.info("Formulaire {} supprimé par l'utilisateur {}", formId, userId);
    }

    // ✅ MÉTHODES PRIVÉES (processSubmissionData, convertToDTO, etc.) - garder les implémentations existantes
    private Map<String, Object> processSubmissionData(Map<String, Object> rawData, List<FormField> fields) {
        Map<String, Object> processedData = new HashMap<>(rawData);

        for (FormField field : fields) {
            String fieldName = field.getFieldName();
            Object value = rawData.get(fieldName);

            if (value == null) continue;

            switch (field.getType()) {
                case "signature":
                    if (value instanceof String && ((String) value).startsWith("data:image")) {
                        try {
                            String signatureUrl = fileStorageService.saveBase64Image(
                                    (String) value, "signatures",
                                    fieldName + "_" + System.currentTimeMillis());
                            processedData.put(fieldName, signatureUrl);
                            processedData.put(fieldName + "_type", "signature");
                        } catch (Exception e) {
                            logger.error("Erreur sauvegarde signature: {}", e.getMessage());
                        }
                    }
                    break;

                case "file":
                case "attachment":
                    if (value instanceof String && ((String) value).startsWith("data:")) {
                        try {
                            String fileUrl = fileStorageService.saveBase64File(
                                    (String) value, "uploads",
                                    fieldName + "_" + System.currentTimeMillis());
                            processedData.put(fieldName, fileUrl);
                            processedData.put(fieldName + "_type", "file");
                        } catch (Exception e) {
                            logger.error("Erreur sauvegarde fichier: {}", e.getMessage());
                        }
                    }
                    break;

                case "drawing":
                    if (value instanceof String && ((String) value).startsWith("data:image")) {
                        try {
                            String drawingUrl = fileStorageService.saveBase64Image(
                                    (String) value, "drawings",
                                    fieldName + "_" + System.currentTimeMillis());
                            processedData.put(fieldName, drawingUrl);
                            processedData.put(fieldName + "_type", "drawing");
                        } catch (Exception e) {
                            logger.error("Erreur sauvegarde dessin: {}", e.getMessage());
                        }
                    }
                    break;

                default:
                    processedData.put(fieldName + "_type", field.getType());
                    break;
            }
        }

        return processedData;
    }

    /*private FormDTO convertToDTO(Form form, Long currentUserId) {
        FormDTO dto = new FormDTO();
        dto.setId(form.getId());
        dto.setName(form.getName());
        dto.setDescription(form.getDescription());
        dto.setStatus(form.getStatus());
        dto.setCreatedAt(form.getCreatedAt());
        dto.setUpdatedAt(form.getUpdatedAt());

        if (form.getCreatedBy() != null) {
            dto.setCreatedBy(form.getCreatedBy().getId());
            dto.setCanEdit(currentUserId != null && currentUserId.equals(form.getCreatedBy().getId()));
        }

        // ✅ Groupes assignés
        if (!form.getAssignedGroups().isEmpty()) {
            dto.setAssignedGroupIds(form.getAssignedGroups().stream()
                    .map(Group::getId)
                    .collect(Collectors.toList()));

            dto.setAssignedGroups(form.getAssignedGroups().stream()
                    .map(this::convertGroupToDTO)
                    .collect(Collectors.toList()));
        }

        // Champs
        if (form.getFields() != null) {
            dto.setFields(form.getFields().stream()
                    .sorted(Comparator.comparing(FormField::getOrder))
                    .map(this::convertToFieldDTO)
                    .collect(Collectors.toList()));
        }

        // Vérifier l'accessibilité
        if (currentUserId != null) {
            try {
                Utilisateur user = userRepository.findById(currentUserId).orElse(null);
                dto.setIsAccessible(user != null && form.isAccessibleByUser(user));
            } catch (Exception e) {
                dto.setIsAccessible(false);
            }
        } else {
            dto.setIsAccessible(true); // Pour les formulaires publics
        }

        return dto;
    }*/

    private GroupDTO convertGroupToDTO(Group group) {
        GroupDTO dto = new GroupDTO();
        dto.setId(group.getId());
        dto.setName(group.getName());
        dto.setDescription(group.getDescription());
        dto.setColor(group.getColor());
        dto.setActive(group.getActive());
        return dto;
    }

    public FormFieldDTO convertToFieldDTO(FormField field) {
        FormFieldDTO dto = new FormFieldDTO();
        dto.setId(field.getId());
        dto.setType(field.getType());
        dto.setLabel(field.getLabel());
        dto.setFieldName(field.getFieldName());
        dto.setPlaceholder(field.getPlaceholder());
        dto.setOrder(field.getOrder());
        dto.setRequired(field.getRequired());

        // Désérialisation des options
        if (field.getOptions() != null && !field.getOptions().trim().isEmpty()) {
            try {
                List<FieldOptionDTO> options = objectMapper.readValue(
                        field.getOptions(),
                        objectMapper.getTypeFactory().constructCollectionType(List.class, FieldOptionDTO.class)
                );
                dto.setOptions(options);
            } catch (Exception e) {
                logger.error("Erreur désérialisation options: {}", e.getMessage());
                dto.setOptions(new ArrayList<>());
            }
        }

        // Désérialisation des attributs
        if (field.getAttributes() != null && !field.getAttributes().trim().isEmpty()) {
            try {
                Map<String, Object> attributes = objectMapper.readValue(
                        field.getAttributes(),
                        objectMapper.getTypeFactory().constructMapType(Map.class, String.class, Object.class)
                );
                dto.setAttributes(attributes);

                // Traitement spécial pour external-list
                if ("external-list".equals(field.getType())) {
                    dto.setExternalListId(extractLongFromAttributes(attributes, "externalListId"));
                    dto.setExternalListDisplayMode((String) attributes.get("externalListDisplayMode"));
                    dto.setExternalListUrl((String) attributes.get("externalListUrl"));
                    dto.setExternalListParams((Map<String, Object>) attributes.get("externalListParams"));
                }
            } catch (Exception e) {
                logger.error("Erreur désérialisation attributs: {}", e.getMessage());
                dto.setAttributes(new HashMap<>());
            }
        }

        return dto;
    }

    private Long extractLongFromAttributes(Map<String, Object> attributes, String key) {
        Object value = attributes.get(key);
        if (value instanceof Number) {
            return ((Number) value).longValue();
        } else if (value instanceof String) {
            try {
                return Long.parseLong((String) value);
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
    }

    private FormField createFormFieldFromDTO(FormFieldCreateDTO dto, Form form, int order) {
        FormField field = new FormField();
        field.setForm(form);
        field.setType(dto.getType());
        field.setLabel(dto.getLabel());
        field.setFieldName(dto.getFieldName());
        field.setPlaceholder(dto.getPlaceholder());
        field.setOrder(dto.getOrder() != null ? dto.getOrder() : order);
        field.setRequired(dto.getRequired() != null ? dto.getRequired() : false);

        // Sérialiser les options
        if (dto.getOptions() != null && !dto.getOptions().isEmpty()) {
            try {
                field.setOptions(objectMapper.writeValueAsString(dto.getOptions()));
            } catch (JsonProcessingException e) {
                logger.error("Erreur sérialisation options: {}", e.getMessage());
                field.setOptions(null);
            }
        }

        // Sérialiser les attributs
        if (dto.getAttributes() != null && !dto.getAttributes().isEmpty()) {
            try {
                field.setAttributes(objectMapper.writeValueAsString(dto.getAttributes()));
            } catch (JsonProcessingException e) {
                logger.error("Erreur sérialisation attributs: {}", e.getMessage());
                field.setAttributes(null);
            }
        }

        return field;
    }

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

        // Désérialiser les données JSON
        try {
            Map<String, Object> data = objectMapper.readValue(
                    submission.getData(),
                    objectMapper.getTypeFactory().constructMapType(Map.class, String.class, Object.class)
            );
            dto.setData(data);
        } catch (JsonProcessingException e) {
            logger.error("Erreur désérialisation données soumission: {}", e.getMessage());
            dto.setData(new HashMap<>());
        }

        return dto;
    }
}