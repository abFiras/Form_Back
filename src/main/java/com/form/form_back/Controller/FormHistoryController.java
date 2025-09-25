package com.form.form_back.Controller;

import com.form.form_back.Service.AuthService;
import com.form.form_back.Service.FormHistoryService;
import com.form.form_back.dto.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/history")
@CrossOrigin(origins = "http://localhost:4200")
public class FormHistoryController {

    @Autowired
    private FormHistoryService formHistoryService;

    @Autowired
    private AuthService authService;

    private static final Logger logger = LoggerFactory.getLogger(FormHistoryController.class);

    /**
     * Obtenir l'historique général avec filtres et pagination
     */
    @GetMapping
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<FormHistoryPageDTO>> getFormHistory(
            @RequestParam(required = false) String formName,
            @RequestParam(required = false) String secteur,
            @RequestParam(required = false) String actionType,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String performedBy,
            @RequestParam(required = false) String searchTerm,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDirection
    ) {
        try {
            Long currentUserId = authService.getCurrentUserId();

            FormHistoryFiltersDTO filters = new FormHistoryFiltersDTO();
            filters.setFormName(formName);
            filters.setSecteur(secteur);
            filters.setActionType(actionType);
            filters.setStatus(status);
            filters.setPerformedBy(performedBy);
            filters.setSearchTerm(searchTerm);
            filters.setStartDate(startDate);
            filters.setEndDate(endDate);
            filters.setPage(page);
            filters.setSize(size);
            filters.setSortBy(sortBy);
            filters.setSortDirection(sortDirection);

            // Passer currentUserId au service
            FormHistoryPageDTO historyPage = formHistoryService.getFormHistoryWithFilters(filters, currentUserId);

            logger.info("Historique récupéré avec {} entrées pour l'utilisateur {}",
                    historyPage.getTotalElements(), currentUserId);

            return ResponseEntity.ok(new ApiResponse<>(
                    "Historique récupéré avec succès. " + historyPage.getTotalElements() + " entrée(s) trouvée(s).",
                    historyPage,
                    true
            ));
        } catch (Exception e) {
            logger.error("Erreur récupération historique: {}", e.getMessage());
            return ResponseEntity.badRequest().body(new ApiResponse<>(
                    "Erreur lors de la récupération de l'historique: " + e.getMessage(),
                    null,
                    false
            ));
        }
    }

    /**
     * Obtenir l'historique d'un formulaire spécifique
     */
    @GetMapping("/form/{formId}")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<FormHistoryDTO>>> getFormSpecificHistory(@PathVariable Long formId) {
        try {
            Long currentUserId = authService.getCurrentUserId();

            // Passer currentUserId au service
            List<FormHistoryDTO> history = formHistoryService.getFormHistory(formId, currentUserId);

            logger.info("Historique du formulaire {} récupéré: {} entrées", formId, history.size());

            return ResponseEntity.ok(new ApiResponse<>(
                    "Historique du formulaire récupéré avec succès. " + history.size() + " action(s) trouvée(s).",
                    history,
                    true
            ));
        } catch (Exception e) {
            logger.error("Erreur récupération historique du formulaire {}: {}", formId, e.getMessage());
            return ResponseEntity.badRequest().body(new ApiResponse<>(
                    "Erreur: " + e.getMessage(),
                    null,
                    false
            ));
        }
    }
    /**
     * Obtenir les statistiques de l'historique
     */
    @GetMapping("/statistics")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<FormHistoryStatsDTO>> getHistoryStatistics() {
        try {
            Long currentUserId = authService.getCurrentUserId();

            FormHistoryStatsDTO stats = formHistoryService.getHistoryStatistics(currentUserId);

            logger.info("Statistiques d'historique récupérées pour l'utilisateur {}", currentUserId);

            return ResponseEntity.ok(new ApiResponse<>(
                    "Statistiques récupérées avec succès",
                    stats,
                    true
            ));
        } catch (Exception e) {
            logger.error("Erreur récupération statistiques historique: {}", e.getMessage());
            return ResponseEntity.badRequest().body(new ApiResponse<>(
                    "Erreur: " + e.getMessage(),
                    null,
                    false
            ));
        }
    }
    /**
     * Obtenir l'activité récente
     */
    @GetMapping("/recent")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<FormHistoryDTO>>> getRecentActivity(
            @RequestParam(defaultValue = "24") int hours
    ) {
        try {
            Long currentUserId = authService.getCurrentUserId();

            List<FormHistoryDTO> recentActivity = formHistoryService.getRecentActivity(hours, currentUserId);

            logger.info("Activité récente ({} heures) récupérée: {} entrées", hours, recentActivity.size());

            return ResponseEntity.ok(new ApiResponse<>(
                    "Activité récente récupérée avec succès. " + recentActivity.size() + " action(s) dans les " + hours + " dernières heures.",
                    recentActivity,
                    true
            ));
        } catch (Exception e) {
            logger.error("Erreur récupération activité récente: {}", e.getMessage());
            return ResponseEntity.badRequest().body(new ApiResponse<>(
                    "Erreur: " + e.getMessage(),
                    null,
                    false
            ));
        }
    }
    /**
     * Obtenir les options de filtrage (pour les dropdowns)
     */
    @GetMapping("/filter-options")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<FormHistoryFilterOptionsDTO>> getFilterOptions() {
        try {
            // Cette méthode peut être étendue pour récupérer les options dynamiquement
            FormHistoryFilterOptionsDTO options = new FormHistoryFilterOptionsDTO();

            // Types d'actions disponibles
            options.setActionTypes(List.of(
                    new ActionTypeOptionDTO("CREATED", "Création", "#4caf50"),
                    new ActionTypeOptionDTO("UPDATED", "Modification", "#2196f3"),
                    new ActionTypeOptionDTO("PUBLISHED", "Publication", "#ff9800"),
                    new ActionTypeOptionDTO("ARCHIVED", "Archivage", "#9e9e9e"),
                    new ActionTypeOptionDTO("DELETED", "Suppression", "#f44336"),
                    new ActionTypeOptionDTO("SHARED_TO_LIBRARY", "Partagé vers bibliothèque", "#9c27b0"),
                    new ActionTypeOptionDTO("REMOVED_FROM_LIBRARY", "Retiré de la bibliothèque", "#795548"),
                    new ActionTypeOptionDTO("GROUPS_ASSIGNED", "Groupes assignés", "#607d8b")
            ));

            // Statuts disponibles
            options.setStatusOptions(List.of(
                    new StatusOptionDTO("DRAFT", "Brouillon", "#ff9800"),
                    new StatusOptionDTO("PUBLISHED", "Publié", "#4caf50"),
                    new StatusOptionDTO("ARCHIVED", "Archivé", "#9e9e9e"),
                    new StatusOptionDTO("DELETED", "Supprimé", "#f44336")
            ));

            return ResponseEntity.ok(new ApiResponse<>(
                    "Options de filtrage récupérées avec succès",
                    options,
                    true
            ));
        } catch (Exception e) {
            logger.error("Erreur récupération options de filtrage: {}", e.getMessage());
            return ResponseEntity.badRequest().body(new ApiResponse<>(
                    "Erreur: " + e.getMessage(),
                    null,
                    false
            ));
        }
    }
}

// DTOs pour les options de filtrage

