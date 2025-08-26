package com.form.form_back.Controller;


import com.form.form_back.Entity.Form;
import com.form.form_back.Entity.FormSubmission;

import com.form.form_back.Service.FormService;
import com.form.form_back.dto.FormDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletRequest;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/forms")
@CrossOrigin(origins = "http://localhost:4200")
public class FormController {

    @Autowired
    private FormService formService;

    @GetMapping
    public List<Form> getAllForms() {
        return formService.getAllForms();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Form> getFormById(@PathVariable Long id) {
        Form form = formService.getFormById(id);
        return form != null ? ResponseEntity.ok(form) : ResponseEntity.notFound().build();
    }

    @PostMapping
    public ResponseEntity<Form> createForm(@RequestBody FormDTO formDTO) {
        Form form = formService.createForm(formDTO);
        return ResponseEntity.ok(form);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Form> updateForm(@PathVariable Long id, @RequestBody FormDTO formDTO) {
        try {
            Form form = formService.updateForm(id, formDTO);
            return ResponseEntity.ok(form);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteForm(@PathVariable Long id) {
        formService.deleteForm(id);
        return ResponseEntity.noContent().build();
    }

    // Endpoint pour soumettre un formulaire
    @PostMapping("/{id}/submit")
    public ResponseEntity<FormSubmission> submitForm(
            @PathVariable Long id,
            @RequestBody Map<String, Object> submissionData,
            HttpServletRequest request) {

        String email = (String) submissionData.get("email");
        String ip = request.getRemoteAddr();

        // Convertir les données en JSON (vous pouvez utiliser ObjectMapper)
        String jsonData = submissionData.toString(); // Simplification pour l'exemple

        FormSubmission submission = formService.submitForm(id, jsonData, email, ip);
        return ResponseEntity.ok(submission);
    }

    // Endpoint pour récupérer les soumissions d'un formulaire
    @GetMapping("/{id}/submissions")
    public List<FormSubmission> getFormSubmissions(@PathVariable Long id) {
        return formService.getFormSubmissions(id);
    }
}