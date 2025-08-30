package com.form.form_back.Controller;

import com.form.form_back.Entity.Form;
import com.form.form_back.Entity.LibraryForm;
import com.form.form_back.Service.LibraryService;
import com.form.form_back.dto.LibraryFormDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/library")
@CrossOrigin(origins = "*")
public class LibraryController {

    @Autowired
    private LibraryService libraryService;

    /**
     * Récupère tous les formulaires de la bibliothèque avec filtres
     */
    @GetMapping("/forms")
    public ResponseEntity<List<LibraryFormDTO>> getLibraryForms(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String origin,
            @RequestParam(required = false) String language,
            @RequestParam(required = false, defaultValue = "relevance") String sortBy) {

        List<LibraryFormDTO> forms = libraryService.getLibraryForms(search, origin, language, sortBy);
        return ResponseEntity.ok(forms);
    }

    /**
     * Récupère les détails d'un formulaire de la bibliothèque
     */
    @GetMapping("/forms/{libraryFormId}")
    public ResponseEntity<LibraryFormDTO> getLibraryFormDetail(@PathVariable Long libraryFormId) {
        LibraryFormDTO formDetail = libraryService.getLibraryFormDetail(libraryFormId);
        return ResponseEntity.ok(formDetail);
    }

    /**
     * Récupère les formulaires populaires
     */
    @GetMapping("/forms/popular")
    public ResponseEntity<List<LibraryFormDTO>> getPopularForms(
            @RequestParam(defaultValue = "10") int limit) {

        List<LibraryFormDTO> forms = libraryService.getPopularForms(limit);
        return ResponseEntity.ok(forms);
    }

    /**
     * Récupère les formulaires récents
     */
    @GetMapping("/forms/recent")
    public ResponseEntity<List<LibraryFormDTO>> getRecentForms(
            @RequestParam(defaultValue = "10") int limit) {

        List<LibraryFormDTO> forms = libraryService.getRecentForms(limit);
        return ResponseEntity.ok(forms);
    }

    /**
     * Partage un formulaire dans la bibliothèque
     */
    @PostMapping("/forms/{formId}/share")
    public ResponseEntity<LibraryForm> shareFormToLibrary(
            @PathVariable Long formId,
            @RequestBody Map<String, String> shareData) {

        String origin = shareData.get("origin");
        String language = shareData.get("language");
        String tags = shareData.get("tags");

        LibraryForm libraryForm = libraryService.shareFormToLibrary(formId, origin, language, tags);
        return ResponseEntity.ok(libraryForm);
    }

    /**
     * Incrémente le compteur de vues
     */
    @PostMapping("/forms/{libraryFormId}/view")
    public ResponseEntity<Void> incrementViewCount(@PathVariable Long libraryFormId) {
        libraryService.incrementViewCount(libraryFormId);
        return ResponseEntity.ok().build();
    }

    /**
     * Ajoute un formulaire de la bibliothèque au compte utilisateur
     */
    @PostMapping("/forms/{libraryFormId}/add-to-account")
    public ResponseEntity<Form> addFormToAccount(@PathVariable Long libraryFormId) {
        Form form = libraryService.addFormToAccount(libraryFormId);
        return ResponseEntity.ok(form);
    }

    /**
     * Supprime un formulaire de la bibliothèque
     */
    @DeleteMapping("/forms/{libraryFormId}")
    public ResponseEntity<Void> removeFromLibrary(@PathVariable Long libraryFormId) {
        libraryService.removeFromLibrary(libraryFormId);
        return ResponseEntity.ok().build();
    }

    /**
     * Export d'un formulaire au format Word
     */
    @GetMapping("/forms/{libraryFormId}/export/word")
    public ResponseEntity<byte[]> exportFormAsWord(@PathVariable Long libraryFormId) {
        byte[] wordData = libraryService.exportFormAsWord(libraryFormId);

        // Incrémenter le compteur de téléchargements
        libraryService.incrementDownloadCount(libraryFormId);

        String filename = "formulaire_bibliotheque_" + libraryFormId + ".docx";

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_TYPE, "application/vnd.openxmlformats-officedocument.wordprocessingml.document")
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
                .body(wordData);
    }

    /**
     * Export d'un formulaire au format Excel
     */
    @GetMapping("/forms/{libraryFormId}/export/excel")
    public ResponseEntity<byte[]> exportFormAsExcel(@PathVariable Long libraryFormId) {
        byte[] excelData = libraryService.exportFormAsExcel(libraryFormId);

        // Incrémenter le compteur de téléchargements
        libraryService.incrementDownloadCount(libraryFormId);

        String filename = "formulaire_bibliotheque_" + libraryFormId + ".xlsx";

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_TYPE, "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
                .body(excelData);
    }
}