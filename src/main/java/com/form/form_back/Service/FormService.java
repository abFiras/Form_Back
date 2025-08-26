package com.form.form_back.Service;

import com.form.form_back.Entity.Form;
import com.form.form_back.Entity.FormField;
import com.form.form_back.Entity.FormSubmission;
import com.form.form_back.Repo.FormRepository;
import com.form.form_back.Repo.FormFieldRepository;
import com.form.form_back.Repo.FormSubmissionRepository;
import com.form.form_back.dto.FormDTO;
import com.form.form_back.dto.FormFieldDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class FormService {

    @Autowired
    private FormRepository formRepository;

    @Autowired
    private FormFieldRepository formFieldRepository;

    @Autowired
    private FormSubmissionRepository formSubmissionRepository;

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
                field.setPlaceholder(fieldDTO.getPlaceholder());
                field.setPosition(fieldDTO.getPosition());
                field.setRequired(fieldDTO.getRequired() != null ? fieldDTO.getRequired() : false);
                field.setOptions(fieldDTO.getOptions());
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
                field.setPlaceholder(fieldDTO.getPlaceholder());
                field.setPosition(fieldDTO.getPosition());
                field.setRequired(fieldDTO.getRequired() != null ? fieldDTO.getRequired() : false);
                field.setOptions(fieldDTO.getOptions());
                field.setValidation(fieldDTO.getValidation());
                field.setStyling(fieldDTO.getStyling());

                formFieldRepository.save(field);
            }
        }

        formRepository.save(form);
        return formRepository.findByIdWithFields(id);
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
