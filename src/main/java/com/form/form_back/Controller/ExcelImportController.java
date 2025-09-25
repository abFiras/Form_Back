package com.form.form_back.Controller;

import com.form.form_back.Service.ExcelImportService;
import com.form.form_back.dto.ExcelImportResponse;
import com.form.form_back.dto.ExcelValidationResult;
import com.form.form_back.dto.ExternalListDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/api/external-lists")
@CrossOrigin(origins = "http://localhost:4200")
public class ExcelImportController {

    @Autowired
    private ExcelImportService excelImportService;

    /**
     * Valide un fichier Excel avant importation
     */
    @PostMapping("/validate-excel")
    public ResponseEntity<ExcelValidationResult> validateExcelFile(
            @RequestParam("file") MultipartFile file) {
        try {
            if (file.isEmpty()) {
                ExcelValidationResult result = new ExcelValidationResult();
                result.setValid(false);
                result.getErrors().add("Le fichier est vide");
                return ResponseEntity.badRequest().body(result);
            }

            ExcelValidationResult result = excelImportService.validateExcelFile(file);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            ExcelValidationResult result = new ExcelValidationResult();
            result.setValid(false);
            result.getErrors().add("Erreur lors de la validation: " + e.getMessage());
            return ResponseEntity.badRequest().body(result);
        }
    }

    /**
     * Importe une liste depuis un fichier Excel
     */
    @PostMapping("/import-excel")
    public ResponseEntity<ExcelImportResponse> importFromExcel(
            @RequestParam("file") MultipartFile file,
            @RequestParam("listName") String listName,
            @RequestParam("description") String description,
            @RequestParam(value = "rubrique", required = false) String rubrique,
            @RequestParam("userId") Long userId) {
        try {
            if (file.isEmpty()) {
                ExcelImportResponse response = new ExcelImportResponse();
                response.setSuccess(false);
                response.setMessage("Le fichier est vide");
                return ResponseEntity.badRequest().body(response);
            }

            // Vérifier si une liste avec le même nom existe déjà
            if (excelImportService.existsByNameAndUser(listName, userId)) {
                ExcelImportResponse response = new ExcelImportResponse();
                response.setSuccess(false);
                response.setMessage("Une liste avec ce nom existe déjà pour cet utilisateur");
                return ResponseEntity.badRequest().body(response);
            }

            ExcelImportResponse result = excelImportService.importFromExcel(
                    file, listName, description, rubrique, userId);
            return ResponseEntity.ok(result);

        } catch (Exception e) {
            ExcelImportResponse response = new ExcelImportResponse();
            response.setSuccess(false);
            response.setMessage("Erreur lors de l'importation: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * Vérifie si une liste avec le même nom existe déjà pour un utilisateur
     */
    @GetMapping("/check-existing")
    public ResponseEntity<Map<String, Object>> checkExistingList(
            @RequestParam("listName") String listName,
            @RequestParam("userId") Long userId) {
        try {
            boolean exists = excelImportService.existsByNameAndUser(listName, userId);
            Long existingListId = null;

            if (exists) {
                existingListId = excelImportService.getListIdByNameAndUser(listName, userId);
            }

            return ResponseEntity.ok(Map.of(
                    "exists", exists,
                    "listId", existingListId != null ? existingListId : 0
            ));
        } catch (Exception e) {
            return ResponseEntity.ok(Map.of(
                    "exists", false,
                    "listId", 0
            ));
        }
    }
}