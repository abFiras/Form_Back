package com.form.form_back.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.form.form_back.Entity.Form;
import com.form.form_back.Entity.FormField;
import com.form.form_back.Entity.FormSubmission;
import com.form.form_back.Repo.FormRepository;
import com.form.form_back.Repo.FormFieldRepository;
import com.form.form_back.Repo.FormSubmissionRepository;
import com.form.form_back.dto.FieldOptionDTO;
import com.form.form_back.dto.FormDTO;
import com.form.form_back.dto.FormFieldDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
@Transactional
public class FormService {

    @Autowired
    private FormRepository formRepository;

    @Autowired
    private FormFieldRepository formFieldRepository;

    @Autowired
    private FormSubmissionRepository formSubmissionRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();

    public List<Form> getAllForms() {
        return formRepository.findAll();
    }

    public Form getFormById(Long id) {
        return formRepository.findByIdWithFields(id);
    }

    public Form createForm(FormDTO formDTO) {
        Form form = new Form();
        form.setName(formDTO.getName());
        form.setDescription(formDTO.getDescription());
        form.setStatus(formDTO.getStatus() != null ? formDTO.getStatus() : "DRAFT");

        Form savedForm = formRepository.save(form);

        // Sauvegarder les champs
        if (formDTO.getFields() != null) {
            for (FormFieldDTO fieldDTO : formDTO.getFields()) {
                FormField field = new FormField();
                field.setForm(savedForm);
                field.setType(fieldDTO.getType());
                field.setLabel(fieldDTO.getLabel());
                field.setFieldName(fieldDTO.getFieldName());
                field.setPlaceholder(fieldDTO.getPlaceholder());
                field.setOrder(fieldDTO.getOrder() != null ? fieldDTO.getOrder() : 0);
                field.setRequired(fieldDTO.getRequired() != null ? fieldDTO.getRequired() : false);

                // ✅ Conversion des options List<FieldOptionDTO> -> JSON String
                if (fieldDTO.getOptions() != null && !fieldDTO.getOptions().isEmpty()) {
                    try {
                        String optionsJson = objectMapper.writeValueAsString(fieldDTO.getOptions());
                        field.setOptions(optionsJson);
                    } catch (Exception e) {
                        System.err.println("Erreur lors de la sérialisation des options: " + e.getMessage());
                        field.setOptions(null);
                    }
                }

                // ✅ NOUVEAU: Gestion des attributs JSON
                if (fieldDTO.getAttributes() != null && !fieldDTO.getAttributes().isEmpty()) {
                    try {
                        String attributesJson = objectMapper.writeValueAsString(fieldDTO.getAttributes());
                        field.setAttributes(attributesJson);
                        System.out.println("Attributs sérialisés pour le champ " + field.getFieldName() + ": " + attributesJson);
                    } catch (Exception e) {
                        System.err.println("Erreur lors de la sérialisation des attributs: " + e.getMessage());
                        field.setAttributes(null);
                    }
                }

                field.setValidation(fieldDTO.getValidation());
                field.setStyling(fieldDTO.getStyling());

                formFieldRepository.save(field);
            }
        }

        return formRepository.findByIdWithFields(savedForm.getId());
    }

    public Form updateForm(Long id, FormDTO formDTO) {
        Form form = formRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Form not found"));

        form.setName(formDTO.getName());
        form.setDescription(formDTO.getDescription());
        form.setStatus(formDTO.getStatus());

        // Supprimer les anciens champs
        formFieldRepository.deleteByFormId(id);

        // Ajouter les nouveaux champs
        if (formDTO.getFields() != null) {
            for (FormFieldDTO fieldDTO : formDTO.getFields()) {
                FormField field = new FormField();
                field.setForm(form);
                field.setType(fieldDTO.getType());
                field.setLabel(fieldDTO.getLabel());
                field.setFieldName(fieldDTO.getFieldName());
                field.setPlaceholder(fieldDTO.getPlaceholder());
                field.setOrder(fieldDTO.getOrder() != null ? fieldDTO.getOrder() : 0);
                field.setRequired(fieldDTO.getRequired() != null ? fieldDTO.getRequired() : false);

                // ✅ Conversion des options List<FieldOptionDTO> -> JSON String
                if (fieldDTO.getOptions() != null && !fieldDTO.getOptions().isEmpty()) {
                    try {
                        String optionsJson = objectMapper.writeValueAsString(fieldDTO.getOptions());
                        field.setOptions(optionsJson);
                    } catch (Exception e) {
                        System.err.println("Erreur lors de la sérialisation des options: " + e.getMessage());
                        field.setOptions(null);
                    }
                }

                // ✅ NOUVEAU: Gestion des attributs JSON pour la mise à jour
                if (fieldDTO.getAttributes() != null && !fieldDTO.getAttributes().isEmpty()) {
                    try {
                        String attributesJson = objectMapper.writeValueAsString(fieldDTO.getAttributes());
                        field.setAttributes(attributesJson);
                        System.out.println("Attributs mis à jour pour le champ " + field.getFieldName() + ": " + attributesJson);
                    } catch (Exception e) {
                        System.err.println("Erreur lors de la sérialisation des attributs: " + e.getMessage());
                        field.setAttributes(null);
                    }
                }

                field.setValidation(fieldDTO.getValidation());
                field.setStyling(fieldDTO.getStyling());

                formFieldRepository.save(field);
            }
        }

        formRepository.save(form);
        return formRepository.findByIdWithFields(id);
    }

    // ✅ Méthode corrigée pour convertir FormField -> FormFieldDTO avec désérialisation complète
    public FormFieldDTO convertToFieldDTO(FormField field) {
        FormFieldDTO dto = new FormFieldDTO();
        dto.setId(field.getId());
        dto.setType(field.getType());
        dto.setLabel(field.getLabel());
        dto.setFieldName(field.getFieldName());
        dto.setPlaceholder(field.getPlaceholder());
        dto.setOrder(field.getOrder());
        dto.setRequired(field.getRequired());
        dto.setValidation(field.getValidation());
        dto.setStyling(field.getStyling());

        // Désérialisation des options JSON -> List<FieldOptionDTO>
        if (field.getOptions() != null && !field.getOptions().trim().isEmpty()) {
            try {
                List<FieldOptionDTO> options = objectMapper.readValue(
                        field.getOptions(),
                        objectMapper.getTypeFactory().constructCollectionType(List.class, FieldOptionDTO.class)
                );
                dto.setOptions(options);
            } catch (Exception e) {
                System.err.println("Erreur lors de la désérialisation des options: " + e.getMessage());
                dto.setOptions(null);
            }
        }

        // ✅ NOUVEAU: Désérialisation des attributs JSON -> Map<String, Object>
        if (field.getAttributes() != null && !field.getAttributes().trim().isEmpty()) {
            try {
                Map<String, Object> attributes = objectMapper.readValue(
                        field.getAttributes(),
                        objectMapper.getTypeFactory().constructMapType(Map.class, String.class, Object.class)
                );
                dto.setAttributes(attributes);
                System.out.println("Attributs désérialisés pour le champ " + field.getFieldName() + ": " + attributes);
            } catch (Exception e) {
                System.err.println("Erreur lors de la désérialisation des attributs: " + e.getMessage());
                dto.setAttributes(null);
            }
        }

        return dto;
    }

    public void deleteForm(Long id) {
        formRepository.deleteById(id);
    }

    public FormSubmission submitForm(Long formId, String jsonData, String email, String ip) {
        FormSubmission submission = new FormSubmission();
        submission.setFormId(formId);
        submission.setData(jsonData);
        submission.setSubmitterEmail(email);
        submission.setSubmitterIp(ip);

        return formSubmissionRepository.save(submission);
    }

    public List<FormSubmission> getFormSubmissions(Long formId) {
        return formSubmissionRepository.findByFormIdOrderBySubmittedAtDesc(formId);
    }
}