package com.form.form_back.Controller;

import com.form.form_back.Service.ExternalListService;
import com.form.form_back.dto.ExternalListDTO;
import com.form.form_back.dto.ExternalListItemDTO;
import com.form.form_back.dto.CreateExternalListRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/external-lists")
@CrossOrigin(origins = "http://localhost:4200")
public class ExternalListController {

    @Autowired
    private ExternalListService externalListService;

    /**
     * Récupère toutes les listes externes
     */
    @GetMapping
    public ResponseEntity<List<ExternalListDTO>> getAllExternalLists() {
        List<ExternalListDTO> lists = externalListService.getAllExternalLists();
        return ResponseEntity.ok(lists);
    }

    /**
     * Récupère les listes externes d'un utilisateur
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<ExternalListDTO>> getExternalListsByUser(@PathVariable Long userId) {
        List<ExternalListDTO> lists = externalListService.getExternalListsByUser(userId);
        return ResponseEntity.ok(lists);
    }

    /**
     * Récupère une liste externe par ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<ExternalListDTO> getExternalListById(@PathVariable Long id) {
        try {
            ExternalListDTO list = externalListService.getExternalListById(id);
            return ResponseEntity.ok(list);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Crée une nouvelle liste externe
     */
    @PostMapping
    public ResponseEntity<ExternalListDTO> createExternalList(
            @RequestBody CreateExternalListRequest request,
            @RequestParam Long userId) {
        try {
            ExternalListDTO list = externalListService.createExternalList(request, userId);
            return ResponseEntity.ok(list);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Met à jour une liste externe
     */
    @PutMapping("/{id}")
    public ResponseEntity<ExternalListDTO> updateExternalList(
            @PathVariable Long id,
            @RequestBody CreateExternalListRequest request) {
        try {
            ExternalListDTO list = externalListService.updateExternalList(id, request);
            return ResponseEntity.ok(list);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Supprime une liste externe
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteExternalList(@PathVariable Long id) {
        try {
            externalListService.deleteExternalList(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Importe une liste depuis un fichier CSV
     */
    @PostMapping("/import-csv")
    public ResponseEntity<ExternalListDTO> importFromCSV(
            @RequestParam("file") MultipartFile file,
            @RequestParam("listName") String listName,
            @RequestParam("description") String description,
            @RequestParam(value = "rubrique", required = false) String rubrique,
            @RequestParam("userId") Long userId) {
        try {
            if (file.isEmpty()) {
                return ResponseEntity.badRequest().build();
            }

            ExternalListDTO list = externalListService.importFromCSV(file, listName, description, rubrique, userId);
            return ResponseEntity.ok(list);
        } catch (IOException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Récupère les éléments d'une liste externe
     */
    @GetMapping("/{id}/items")
    public ResponseEntity<List<ExternalListItemDTO>> getListItems(@PathVariable Long id) {
        List<ExternalListItemDTO> items = externalListService.getListItems(id);
        return ResponseEntity.ok(items);
    }

    /**
     * Recherche des listes par nom
     */
    @GetMapping("/search")
    public ResponseEntity<List<ExternalListDTO>> searchLists(@RequestParam String query) {
        List<ExternalListDTO> lists = externalListService.searchListsByName(query);
        return ResponseEntity.ok(lists);
    }

    /**
     * Récupère toutes les rubriques disponibles
     */
    @GetMapping("/rubriques")
    public ResponseEntity<List<String>> getAllRubriques() {
        List<String> rubriques = externalListService.getAllRubriques();
        return ResponseEntity.ok(rubriques);
    }

    /**
     * Récupère les listes d'une rubrique
     */
    @GetMapping("/rubrique/{rubrique}")
    public ResponseEntity<List<ExternalListDTO>> getListsByRubrique(@PathVariable String rubrique) {
        List<ExternalListDTO> lists = externalListService.getListsByRubrique(rubrique);
        return ResponseEntity.ok(lists);
    }
}