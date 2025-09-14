package com.form.form_back.Controller;

import com.form.form_back.Entity.Form;
import com.form.form_back.Entity.FormSubmission;
import com.form.form_back.Service.FormService;
import com.form.form_back.dto.FormDTO;
import com.form.form_back.dto.FormFieldDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/forms")
@CrossOrigin(origins = "*")
public class FormController {

    @Autowired
    private FormService formService;

    @GetMapping
    public List<Form> getAllForms() {
        return formService.getAllForms();
    }

    @GetMapping("/{id}")
    public ResponseEntity<FormDTO> getFormById(@PathVariable Long id) {
        try {
            Form form = formService.getFormById(id);
            if (form == null) {
                return ResponseEntity.notFound().build();
            }

            // ✅ Conversion manuelle avec gestion des attributs
            FormDTO formDTO = new FormDTO();
            formDTO.setId(form.getId());
            formDTO.setName(form.getName());
            formDTO.setDescription(form.getDescription());
            formDTO.setStatus(form.getStatus());

            // ✅ Conversion des champs avec désérialisation des attributs
            if (form.getFields() != null) {
                List<FormFieldDTO> fieldDTOs = form.getFields()
                        .stream()
                        .map(field -> formService.convertToFieldDTO(field)) // Utilise la méthode du service
                        .collect(Collectors.toList());
                formDTO.setFields(fieldDTOs);
            }

            return ResponseEntity.ok(formDTO);
        } catch (Exception e) {
            System.err.println("Erreur lors de la récupération du formulaire " + id + ": " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping
    public ResponseEntity<FormDTO> createForm(@RequestBody FormDTO formDTO) {
        try {
            System.out.println("Création d'un formulaire avec " +
                    (formDTO.getFields() != null ? formDTO.getFields().size() : 0) + " champs");

            Form createdForm = formService.createForm(formDTO);

            // Conversion pour la réponse
            FormDTO responseDTO = new FormDTO();
            responseDTO.setId(createdForm.getId());
            responseDTO.setName(createdForm.getName());
            responseDTO.setDescription(createdForm.getDescription());
            responseDTO.setStatus(createdForm.getStatus());

            // Conversion des champs avec désérialisation
            if (createdForm.getFields() != null) {
                List<FormFieldDTO> fieldDTOs = createdForm.getFields()
                        .stream()
                        .map(field -> formService.convertToFieldDTO(field))
                        .collect(Collectors.toList());
                responseDTO.setFields(fieldDTOs);
            }

            return ResponseEntity.ok(responseDTO);
        } catch (Exception e) {
            System.err.println("Erreur lors de la création du formulaire: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<FormDTO> updateForm(@PathVariable Long id, @RequestBody FormDTO formDTO) {
        try {
            System.out.println("Mise à jour du formulaire " + id + " avec " +
                    (formDTO.getFields() != null ? formDTO.getFields().size() : 0) + " champs");

            Form updatedForm = formService.updateForm(id, formDTO);

            // Conversion pour la réponse
            FormDTO responseDTO = new FormDTO();
            responseDTO.setId(updatedForm.getId());
            responseDTO.setName(updatedForm.getName());
            responseDTO.setDescription(updatedForm.getDescription());
            responseDTO.setStatus(updatedForm.getStatus());

            // Conversion des champs avec désérialisation
            if (updatedForm.getFields() != null) {
                List<FormFieldDTO> fieldDTOs = updatedForm.getFields()
                        .stream()
                        .map(field -> formService.convertToFieldDTO(field))
                        .collect(Collectors.toList());
                responseDTO.setFields(fieldDTOs);
            }

            return ResponseEntity.ok(responseDTO);
        } catch (Exception e) {
            System.err.println("Erreur lors de la mise à jour du formulaire " + id + ": " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteForm(@PathVariable Long id) {
        try {
            formService.deleteForm(id);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            System.err.println("Erreur lors de la suppression du formulaire " + id + ": " + e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/{id}/publish")
    public ResponseEntity<FormDTO> publishForm(@PathVariable Long id) {
        try {
            // Pour l'instant, nous simulons la publication en changeant le statut
            FormDTO formToUpdate = new FormDTO();
            formToUpdate.setStatus("PUBLISHED");

            Form publishedForm = formService.updateForm(id, formToUpdate);

            // Conversion pour la réponse
            FormDTO responseDTO = new FormDTO();
            responseDTO.setId(publishedForm.getId());
            responseDTO.setName(publishedForm.getName());
            responseDTO.setDescription(publishedForm.getDescription());
            responseDTO.setStatus(publishedForm.getStatus());

            if (publishedForm.getFields() != null) {
                List<FormFieldDTO> fieldDTOs = publishedForm.getFields()
                        .stream()
                        .map(field -> formService.convertToFieldDTO(field))
                        .collect(Collectors.toList());
                responseDTO.setFields(fieldDTOs);
            }

            return ResponseEntity.ok(responseDTO);
        } catch (Exception e) {
            System.err.println("Erreur lors de la publication du formulaire " + id + ": " + e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }



    @GetMapping("/{id}/submissions")
    public List<FormSubmission> getFormSubmissions(@PathVariable Long id) {
        return formService.getFormSubmissions(id);
    }
}