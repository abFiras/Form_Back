// FormController.java - Version mise à jour avec FormSubmissionService
package com.form.form_back.Controller;

import com.form.form_back.Service.AuthService;
import com.form.form_back.Service.FormService;
import com.form.form_back.Service.FormSubmissionService;
import com.form.form_back.dto.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletRequest;

import java.util.List;

@RestController
@RequestMapping("/api/forms")
@CrossOrigin(origins = "http://localhost:4200")
public class FormController {

    @Autowired
    private FormService formService;

    @Autowired
    private FormSubmissionService formSubmissionService; // ✅ Nouveau service dédié

    @Autowired
    private AuthService authService;

    private static final Logger logger = LoggerFactory.getLogger(FormController.class);

    // ✅ CRÉER UN FORMULAIRE
    @PostMapping
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<FormDTO>> createForm(@RequestBody FormCreateRequest request) {
        try {
            Long currentUserId = authService.getCurrentUserId();
            request.setUserId(currentUserId);

            FormDTO createdForm = formService.createForm(request);

            logger.info("Formulaire créé par l'utilisateur {}: {}", currentUserId, createdForm.getName());

            return ResponseEntity.ok(new ApiResponse<>(
                    "Formulaire créé avec succès",
                    createdForm,
                    true
            ));
        } catch (Exception e) {
            logger.error("Erreur création formulaire: {}", e.getMessage());
            return ResponseEntity.badRequest().body(new ApiResponse<>(
                    "Erreur lors de la création: " + e.getMessage(),
                    null,
                    false
            ));
        }
    }

    // ✅ OBTENIR TOUS LES FORMULAIRES de l'utilisateur
    @GetMapping
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<FormDTO>>> getAllForms() {
        try {
            Long currentUserId = authService.getCurrentUserId();
            List<FormDTO> forms = formService.getFormsForUser(currentUserId);

            return ResponseEntity.ok(new ApiResponse<>(
                    "Formulaires récupérés avec succès",
                    forms,
                    true
            ));
        } catch (Exception e) {
            logger.error("Erreur récupération formulaires: {}", e.getMessage());
            return ResponseEntity.badRequest().body(new ApiResponse<>(
                    "Erreur: " + e.getMessage(),
                    null,
                    false
            ));
        }
    }

    // ✅ OBTENIR LES FORMULAIRES PUBLIÉS pour remplissage
    @GetMapping("/published")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<FormDTO>>> getPublishedForms() {
        try {
            Long currentUserId = authService.getCurrentUserId();
            List<FormDTO> forms = formService.getPublishedFormsForUser(currentUserId);

            logger.debug("Récupération de {} formulaires publiés pour l'utilisateur {}",
                    forms.size(), currentUserId);

            return ResponseEntity.ok(new ApiResponse<>(
                    "Formulaires publiés récupérés avec succès. " +
                            forms.size() + " formulaire(s) accessible(s) selon vos groupes.",
                    forms,
                    true
            ));
        } catch (Exception e) {
            logger.error("Erreur récupération formulaires publiés: {}", e.getMessage());
            return ResponseEntity.badRequest().body(new ApiResponse<>(
                    "Erreur: " + e.getMessage(),
                    null,
                    false
            ));
        }
    }

    // ✅ OBTENIR UN FORMULAIRE pour remplissage (avec vérifications strictes)
    // ✅ OBTENIR UN FORMULAIRE pour remplissage - VERSION SÉCURISÉE
    private void validateFormId(Long id, String operation) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException(
                    String.format("ID de formulaire invalide pour l'opération '%s': %s", operation, id)
            );
        }
    }
    @GetMapping("/{id}/fill")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<FormDTO>> getFormForFilling(@PathVariable Long id) {
        try {
            // ✅ VALIDATION EXPLICITE de l'ID
            validateFormId(id, "remplissage");

            Long currentUserId = authService.getCurrentUserId();
            FormDTO form = formService.getFormById(id, currentUserId);

            if (!"PUBLISHED".equals(form.getStatus())) {
                return ResponseEntity.badRequest().body(new ApiResponse<>(
                        "Ce formulaire n'est pas encore publié et ne peut pas être rempli",
                        null,
                        false
                ));
            }

            if (!form.getIsAccessible()) {
                String groupNames = form.getAssignedGroups() != null ?
                        form.getAssignedGroups().stream()
                                .map(GroupDTO::getName)
                                .reduce((a, b) -> a + ", " + b)
                                .orElse("aucun groupe") : "aucun groupe";

                return ResponseEntity.badRequest().body(new ApiResponse<>(
                        "Accès refusé. Ce formulaire est réservé aux groupes : " + groupNames,
                        null,
                        false
                ));
            }

            logger.info("Formulaire {} chargé pour remplissage par l'utilisateur {}",
                    form.getName(), currentUserId);

            return ResponseEntity.ok(new ApiResponse<>(
                    "Formulaire récupéré pour remplissage. Vous pouvez maintenant le compléter.",
                    form,
                    true
            ));
        } catch (IllegalArgumentException e) {
            logger.error("Paramètre invalide pour getFormForFilling: {}", e.getMessage());
            return ResponseEntity.badRequest().body(new ApiResponse<>(
                    e.getMessage(),
                    null,
                    false
            ));
        } catch (Exception e) {
            logger.error("Erreur récupération formulaire pour remplissage ID {}: {}", id, e.getMessage());
            return ResponseEntity.badRequest().body(new ApiResponse<>(
                    "Erreur: " + e.getMessage(),
                    null,
                    false
            ));
        }
    }

    // ✅ OBTENIR UN FORMULAIRE par ID (pour édition)
    @GetMapping("/{id}/submissions")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<FormSubmissionResponseDTO>>> getFormSubmissions(@PathVariable Long id) {
        try {
            // ✅ VALIDATION EXPLICITE de l'ID
            validateFormId(id, "consultation des soumissions");

            Long currentUserId = authService.getCurrentUserId();
            List<FormSubmissionResponseDTO> submissions = formService.getFormSubmissions(id, currentUserId);

            logger.debug("Récupération de {} soumissions pour le formulaire {}", submissions.size(), id);

            return ResponseEntity.ok(new ApiResponse<>(
                    "Soumissions récupérées avec succès. " + submissions.size() + " soumission(s) trouvée(s). " +
                            "(Les templates vides sont automatiquement exclues)",
                    submissions,
                    true
            ));
        } catch (IllegalArgumentException e) {
            logger.error("Paramètre invalide pour getFormSubmissions: {}", e.getMessage());
            return ResponseEntity.badRequest().body(new ApiResponse<>(
                    e.getMessage(),
                    null,
                    false
            ));
        } catch (Exception e) {
            logger.error("Erreur récupération soumissions formulaire {}: {}", id, e.getMessage());
            return ResponseEntity.badRequest().body(new ApiResponse<>(
                    "Erreur: " + e.getMessage(),
                    null,
                    false
            ));
        }
    }

    // ✅ OBTENIR UN FORMULAIRE PAR ID - VERSION SÉCURISÉE
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<FormDTO>> getFormById(@PathVariable Long id) {
        try {
            // ✅ VALIDATION EXPLICITE de l'ID
            validateFormId(id, "consultation");

            Long currentUserId = authService.getCurrentUserId();
            FormDTO form = formService.getFormById(id, currentUserId);

            return ResponseEntity.ok(new ApiResponse<>(
                    "Formulaire récupéré avec succès",
                    form,
                    true
            ));
        } catch (IllegalArgumentException e) {
            logger.error("Paramètre invalide pour getFormById: {}", e.getMessage());
            return ResponseEntity.badRequest().body(new ApiResponse<>(
                    e.getMessage(),
                    null,
                    false
            ));
        } catch (Exception e) {
            logger.error("Erreur récupération formulaire {}: {}", id, e.getMessage());
            return ResponseEntity.badRequest().body(new ApiResponse<>(
                    "Erreur: " + e.getMessage(),
                    null,
                    false
            ));
        }
    }

    // ✅ METTRE À JOUR UN FORMULAIRE
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<FormDTO>> updateForm(
            @PathVariable Long id,
            @RequestBody FormUpdateRequest request) {
        try {
            Long currentUserId = authService.getCurrentUserId();
            FormDTO updatedForm = formService.updateForm(id, request, currentUserId);

            logger.info("Formulaire {} mis à jour par l'utilisateur {}", id, currentUserId);

            return ResponseEntity.ok(new ApiResponse<>(
                    "Formulaire mis à jour avec succès",
                    updatedForm,
                    true
            ));
        } catch (Exception e) {
            logger.error("Erreur mise à jour formulaire {}: {}", id, e.getMessage());
            return ResponseEntity.badRequest().body(new ApiResponse<>(
                    "Erreur lors de la mise à jour: " + e.getMessage(),
                    null,
                    false
            ));
        }
    }

    // ✅ PUBLIER UN FORMULAIRE
    @PostMapping("/{id}/publish")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<FormDTO>> publishForm(@PathVariable Long id) {
        try {
            Long currentUserId = authService.getCurrentUserId();
            FormDTO publishedForm = formService.publishForm(id, currentUserId);

            logger.info("Formulaire {} publié par l'utilisateur {}", id, currentUserId);

            return ResponseEntity.ok(new ApiResponse<>(
                    "Formulaire publié avec succès! Il est maintenant accessible aux utilisateurs " +
                            "des groupes assignés. Une copie vide restera disponible pour chaque soumission.",
                    publishedForm,
                    true
            ));
        } catch (Exception e) {
            logger.error("Erreur publication formulaire {}: {}", id, e.getMessage());
            return ResponseEntity.badRequest().body(new ApiResponse<>(
                    "Erreur lors de la publication: " + e.getMessage(),
                    null,
                    false
            ));
        }
    }

    // ✅ SOUMETTRE UN FORMULAIRE (authentifié) - NOUVELLE IMPLÉMENTATION
    @PostMapping("/{id}/submit")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<FormSubmissionResponseDTO>> submitForm(
            @PathVariable Long id,
            @RequestBody FormSubmissionRequest request,
            HttpServletRequest httpRequest) {
        try {
            Long currentUserId = authService.getCurrentUserId();
            String clientIp = getClientIpAddress(httpRequest);

            // ✅ Utiliser le nouveau service de soumission
            FormSubmissionResponseDTO submission = formSubmissionService
                    .submitFormAuthenticated(id, request, currentUserId);

            logger.info("Formulaire {} soumis avec succès par l'utilisateur {} depuis IP {}",
                    id, currentUserId, clientIp);

            return ResponseEntity.ok(new ApiResponse<>(
                    "Formulaire soumis avec succès! Vos données ont été enregistrées de manière sécurisée. " +
                            "Le formulaire reste disponible pour d'autres utilisateurs.",
                    submission,
                    true
            ));
        } catch (Exception e) {
            logger.error("Erreur soumission formulaire {}: {}", id, e.getMessage());
            return ResponseEntity.badRequest().body(new ApiResponse<>(
                    "Erreur lors de la soumission: " + e.getMessage(),
                    null,
                    false
            ));
        }
    }

    // ✅ SOUMISSION ANONYME (pour formulaires publics)
    @PostMapping("/{id}/submit-anonymous")
    public ResponseEntity<ApiResponse<FormSubmissionResponseDTO>> submitFormAnonymous(
            @PathVariable Long id,
            @RequestBody FormSubmissionRequest request,
            HttpServletRequest httpRequest) {
        try {
            String clientIp = getClientIpAddress(httpRequest);

            // ✅ Utiliser le nouveau service pour soumission anonyme
            FormSubmissionResponseDTO submission = formSubmissionService
                    .submitFormAnonymous(id, request, clientIp);

            logger.info("Soumission anonyme pour le formulaire {} depuis IP {}", id, clientIp);

            return ResponseEntity.ok(new ApiResponse<>(
                    "Formulaire soumis avec succès! Votre soumission anonyme a été enregistrée.",
                    submission,
                    true
            ));
        } catch (Exception e) {
            logger.error("Erreur soumission anonyme formulaire {}: {}", id, e.getMessage());
            return ResponseEntity.badRequest().body(new ApiResponse<>(
                    "Erreur lors de la soumission: " + e.getMessage(),
                    null,
                    false
            ));
        }
    }

    // ✅ OBTENIR LES SOUMISSIONS d'un formulaire (exclut automatiquement les templates)

    // ✅ ASSIGNER DES GROUPES à un formulaire
    @PostMapping("/{id}/assign-groups")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<FormDTO>> assignGroupsToForm(
            @PathVariable Long id,
            @RequestBody AssignGroupsRequest request) {
        try {
            Long currentUserId = authService.getCurrentUserId();
            FormDTO updatedForm = formService.assignGroupsToForm(id, request.getGroupIds(), currentUserId);

            logger.info("Groupes assignés au formulaire {} par l'utilisateur {}", id, currentUserId);

            return ResponseEntity.ok(new ApiResponse<>(
                    "Groupes assignés avec succès. Le formulaire est maintenant accessible " +
                            "uniquement aux membres des groupes sélectionnés.",
                    updatedForm,
                    true
            ));
        } catch (Exception e) {
            logger.error("Erreur assignation groupes formulaire {}: {}", id, e.getMessage());
            return ResponseEntity.badRequest().body(new ApiResponse<>(
                    "Erreur lors de l'assignation: " + e.getMessage(),
                    null,
                    false
            ));
        }
    }

    // ✅ SUPPRIMER UN FORMULAIRE
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteForm(@PathVariable Long id) {
        try {
            Long currentUserId = authService.getCurrentUserId();
            formService.deleteForm(id, currentUserId);

            logger.info("Formulaire {} supprimé par l'utilisateur {}", id, currentUserId);

            return ResponseEntity.ok(new ApiResponse<>(
                    "Formulaire supprimé avec succès. Toutes les soumissions associées ont également été supprimées.",
                    null,
                    true
            ));
        } catch (Exception e) {
            logger.error("Erreur suppression formulaire {}: {}", id, e.getMessage());
            return ResponseEntity.badRequest().body(new ApiResponse<>(
                    "Erreur lors de la suppression: " + e.getMessage(),
                    null,
                    false
            ));
        }
    }

    // ✅ ENDPOINTS PUBLICS (sans authentification)

    // ✅ OBTENIR UN FORMULAIRE PUBLIC
    @GetMapping("/{id}/public")
    public ResponseEntity<ApiResponse<FormDTO>> getPublicForm(@PathVariable Long id) {
        try {
            FormDTO form = formService.getPublicForm(id);

            logger.debug("Formulaire public {} récupéré", id);

            return ResponseEntity.ok(new ApiResponse<>(
                    "Formulaire public récupéré avec succès",
                    form,
                    true
            ));
        } catch (Exception e) {
            logger.error("Erreur récupération formulaire public {}: {}", id, e.getMessage());
            return ResponseEntity.badRequest().body(new ApiResponse<>(
                    "Formulaire non disponible: " + e.getMessage(),
                    null,
                    false
            ));
        }
    }

    // ✅ ENDPOINTS UTILITAIRES

    // ✅ VÉRIFIER L'ACCÈS à un formulaire

    @GetMapping("/{id}/stats")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<FormStatsResponseDTO>> getFormStats(@PathVariable Long id) {
        try {
            Long currentUserId = authService.getCurrentUserId();

            // Vérifier l'accès au formulaire
            FormDTO form = formService.getFormById(id, currentUserId);
            if (!form.getCanEdit()) {
                throw new RuntimeException("Seul le créateur peut voir les statistiques");
            }

            // Obtenir les soumissions (exclut automatiquement les templates)
            List<FormSubmissionResponseDTO> submissions = formService.getFormSubmissions(id, currentUserId);

            FormStatsResponseDTO stats = new FormStatsResponseDTO();
            stats.setFormId(id);
            stats.setFormName(form.getName());
            stats.setTotalSubmissions(submissions.size());
            stats.setStatus(form.getStatus());

            // Calculer les statistiques
            long authenticatedSubmissions = submissions.stream()
                    .filter(s -> s.getSubmitterId() != null)
                    .count();
            long anonymousSubmissions = submissions.size() - authenticatedSubmissions;

            stats.setAuthenticatedSubmissions((int) authenticatedSubmissions);
            stats.setAnonymousSubmissions((int) anonymousSubmissions);

            logger.info("Statistiques du formulaire {} récupérées: {} soumissions",
                    id, submissions.size());

            return ResponseEntity.ok(new ApiResponse<>(
                    "Statistiques récupérées avec succès",
                    stats,
                    true
            ));
        } catch (Exception e) {
            logger.error("Erreur récupération statistiques formulaire {}: {}", id, e.getMessage());
            return ResponseEntity.badRequest().body(new ApiResponse<>(
                    "Erreur: " + e.getMessage(),
                    null,
                    false
            ));
        }
    }

    // ✅ MÉTHODES UTILITAIRES

    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedForHeader = request.getHeader("X-Forwarded-For");
        if (xForwardedForHeader == null || xForwardedForHeader.isEmpty()) {
            return request.getRemoteAddr();
        } else {
            return xForwardedForHeader.split(",")[0].trim();
        }
    }
}

