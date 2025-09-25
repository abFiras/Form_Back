package com.form.form_back.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.form.form_back.Entity.*;
import com.form.form_back.Repo.FormHistoryRepository;
import com.form.form_back.Repo.FormRepository;
import com.form.form_back.Repo.LibraryFormRepository;
import com.form.form_back.Repo.UtilisateurRepository;
import com.form.form_back.dto.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class FormHistoryService {

    @Autowired
    private UtilisateurRepository userRepository;

    @Autowired
    private FormRepository formRepository;

    @Autowired
    private FormHistoryRepository formHistoryRepository;

    @Autowired
    private LibraryFormRepository libraryFormRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private static final Logger logger = LoggerFactory.getLogger(FormHistoryService.class);

    /**
     * Enregistrer une action dans l'historique
     */
    public void recordFormAction(Form form, String actionType, String actionDescription,
                                 Utilisateur performedBy, String ipAddress, String userAgent) {
        try {
            FormHistory history = new FormHistory();
            history.setFormId(form.getId());
            history.setFormName(form.getName() != null ? form.getName() : "Non spécifié");
            history.setSecteur(form.getSecteur());
            history.setDescription(form.getDescription());
            history.setStatus(form.getStatus());
            history.setActionType(actionType);
            history.setActionDescription(actionDescription);

            if (performedBy != null) {
                history.setPerformedById(performedBy.getId());
                history.setPerformedByUsername(
                        performedBy.getUsername() != null ? performedBy.getUsername() : "Système"
                );
                history.setPerformedByEmail(performedBy.getEmail());
            }


            // Sérialiser les groupes assignés
            if (form.getAssignedGroups() != null && !form.getAssignedGroups().isEmpty()) {
                List<Map<String, Object>> groupsInfo = form.getAssignedGroups().stream()
                        .map(group -> {
                            Map<String, Object> groupInfo = new HashMap<>();
                            groupInfo.put("id", group.getId());
                            groupInfo.put("name", group.getName());
                            groupInfo.put("color", group.getColor());
                            return groupInfo;
                        })
                        .collect(Collectors.toList());
                history.setAssignedGroups(objectMapper.writeValueAsString(groupsInfo));
            }

            history.setFieldCount(form.getFields() != null ? form.getFields().size() : 0);

            // Vérifier si le formulaire est dans la bibliothèque
            if (form.getId() != null) {
                boolean isInLibrary = libraryFormRepository.existsByOriginalFormId(form.getId());
                history.setIsInLibrary(isInLibrary);

                if (isInLibrary && "SHARED_TO_LIBRARY".equals(actionType)) {
                    history.setLibrarySharedDate(LocalDateTime.now());
                }
            }

            history.setIpAddress(ipAddress);
            history.setUserAgent(userAgent);
            history.setCreatedAt(LocalDateTime.now());

            formHistoryRepository.save(history);

            logger.info("Action '{}' enregistrée pour le formulaire '{}' par {}",
                    actionType, form.getName(),
                    performedBy != null ? performedBy.getUsername() : "Système");

        } catch (Exception e) {
            logger.error("Erreur lors de l'enregistrement de l'historique: {}", e.getMessage(), e);
        }
    }

    /**
     * Enregistrer des détails de changements
     */
    public void recordFormActionWithChanges(Form form, String actionType, String actionDescription,
                                            Utilisateur performedBy, Map<String, Object> changes,
                                            String ipAddress, String userAgent) {
        try {
            FormHistory history = new FormHistory();

            // ✅ Lier le formulaire avec TOUTES les informations
            history.setFormId(form.getId());
            history.setFormName(form.getName() != null ? form.getName() : "Non spécifié");
            history.setSecteur(form.getSecteur());  // ← Ajouté
            history.setDescription(form.getDescription());  // ← Ajouté
            history.setStatus(form.getStatus());  // ← Ajouté

            // ✅ Infos action
            history.setActionType(actionType);
            history.setActionDescription(actionDescription);
            history.setCreatedAt(LocalDateTime.now());

            // ✅ Utilisateur
            if (performedBy != null) {
                history.setPerformedById(performedBy.getId());
                history.setPerformedByUsername(
                        performedBy.getUsername() != null ? performedBy.getUsername() : "Système"
                );
                history.setPerformedByEmail(performedBy.getEmail());
            }

            // ✅ Sérialiser les groupes assignés - Ajouté
            if (form.getAssignedGroups() != null && !form.getAssignedGroups().isEmpty()) {
                List<Map<String, Object>> groupsInfo = form.getAssignedGroups().stream()
                        .map(group -> {
                            Map<String, Object> groupInfo = new HashMap<>();
                            groupInfo.put("id", group.getId());
                            groupInfo.put("name", group.getName());
                            groupInfo.put("color", group.getColor());
                            return groupInfo;
                        })
                        .collect(Collectors.toList());
                history.setAssignedGroups(objectMapper.writeValueAsString(groupsInfo));
            }

            // ✅ Nombre de champs - Ajouté
            history.setFieldCount(form.getFields() != null ? form.getFields().size() : 0);

            // ✅ Vérifier si le formulaire est dans la bibliothèque - Ajouté
            if (form.getId() != null) {
                boolean isInLibrary = libraryFormRepository.existsByOriginalFormId(form.getId());
                history.setIsInLibrary(isInLibrary);

                if (isInLibrary && "SHARED_TO_LIBRARY".equals(actionType)) {
                    history.setLibrarySharedDate(LocalDateTime.now());
                }
            }

            // ✅ Meta
            history.setIpAddress(ipAddress);
            history.setUserAgent(userAgent);

            // ✅ Changements
            if (changes != null && !changes.isEmpty()) {
                history.setChangesDetails(objectMapper.writeValueAsString(changes));
            }

            formHistoryRepository.save(history);

            logger.info("Action '{}' enregistrée pour le formulaire '{}' par {}",
                    actionType, form.getName(),
                    performedBy != null ? performedBy.getUsername() : "Système");

        } catch (Exception e) {
            logger.error("Erreur lors de l'enregistrement de l'historique avec changements: {}", e.getMessage(), e);
        }
    }
    /**
     * Obtenir l'historique d'un formulaire
     */
    public List<FormHistoryDTO> getFormHistory(Long formId, Long currentUserId) {
        // Vérifier d'abord si l'utilisateur a accès au formulaire
        if (!hasAccessToForm(formId, currentUserId)) {
            return new ArrayList<>(); // Retourner une liste vide si pas d'accès
        }

        List<FormHistory> historyList = formHistoryRepository.findByFormIdOrderByCreatedAtDesc(formId);
        return historyList.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Obtenir l'historique général avec filtres
     */
    public FormHistoryPageDTO getFormHistoryWithFilters(FormHistoryFiltersDTO filters, Long currentUserId) {
        // Récupérer tous les IDs de formulaires accessibles par l'utilisateur
        List<Long> accessibleFormIds = getAccessibleFormIds(currentUserId);

        if (accessibleFormIds.isEmpty()) {
            // Retourner une page vide si aucun formulaire accessible
            FormHistoryPageDTO emptyPage = new FormHistoryPageDTO();
            emptyPage.setContent(new ArrayList<>());
            emptyPage.setTotalElements(0);
            emptyPage.setTotalPages(0);
            emptyPage.setCurrentPage(filters.getPage());
            emptyPage.setPageSize(filters.getSize());
            emptyPage.setHasNext(false);
            emptyPage.setHasPrevious(false);
            return emptyPage;
        }

        List<FormHistory> historyList;

        if (filters.getSearchTerm() != null && !filters.getSearchTerm().trim().isEmpty()) {
            historyList = formHistoryRepository.searchInHistoryForAccessibleForms(
                    filters.getSearchTerm().trim(), accessibleFormIds);
        } else {
            historyList = formHistoryRepository.findWithFiltersForAccessibleForms(
                    filters.getFormName(),
                    filters.getSecteur(),
                    filters.getActionType(),
                    filters.getStatus(),
                    filters.getPerformedBy(),
                    filters.getStartDate(),
                    filters.getEndDate(),
                    accessibleFormIds
            );
        }

        // Pagination manuelle
        int start = filters.getPage() * filters.getSize();
        int end = Math.min(start + filters.getSize(), historyList.size());

        List<FormHistoryDTO> pageContent = historyList.subList(start, end)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        FormHistoryPageDTO pageDTO = new FormHistoryPageDTO();
        pageDTO.setContent(pageContent);
        pageDTO.setTotalElements(historyList.size());
        pageDTO.setTotalPages((int) Math.ceil((double) historyList.size() / filters.getSize()));
        pageDTO.setCurrentPage(filters.getPage());
        pageDTO.setPageSize(filters.getSize());
        pageDTO.setHasNext(end < historyList.size());
        pageDTO.setHasPrevious(filters.getPage() > 0);

        return pageDTO;
    }
    /**
     * Obtenir les statistiques de l'historique
     */
    public FormHistoryStatsDTO getHistoryStatistics(Long currentUserId) {
        // Récupérer tous les IDs de formulaires accessibles par l'utilisateur
        List<Long> accessibleFormIds = getAccessibleFormIds(currentUserId);

        if (accessibleFormIds.isEmpty()) {
            // Retourner des stats vides si aucun formulaire accessible
            return new FormHistoryStatsDTO();
        }

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime todayStart = now.truncatedTo(ChronoUnit.DAYS);
        LocalDateTime weekStart = now.minusWeeks(1);
        LocalDateTime monthStart = now.minusMonths(1);

        FormHistoryStatsDTO stats = new FormHistoryStatsDTO();

        // Comptes généraux - filtrer par formulaires accessibles
        stats.setTotalActions(formHistoryRepository.countByFormIdIn(accessibleFormIds));
        stats.setTodayActions(formHistoryRepository.findRecentActivityForAccessibleForms(todayStart, accessibleFormIds).size());
        stats.setWeekActions(formHistoryRepository.findRecentActivityForAccessibleForms(weekStart, accessibleFormIds).size());
        stats.setMonthActions(formHistoryRepository.findRecentActivityForAccessibleForms(monthStart, accessibleFormIds).size());

        // Statistiques par type d'action - filtrer par formulaires accessibles
        List<Object[]> actionStats = formHistoryRepository.getActionTypeStatisticsForAccessibleForms(accessibleFormIds);
        long totalActions = actionStats.stream().mapToLong(arr -> (Long) arr[1]).sum();

        List<ActionTypeStatsDTO> actionTypeStats = actionStats.stream()
                .map(arr -> {
                    String actionType = (String) arr[0];
                    Long count = (Long) arr[1];
                    double percentage = totalActions > 0 ? (double) count / totalActions * 100 : 0;

                    ActionTypeStatsDTO dto = new ActionTypeStatsDTO();
                    dto.setActionType(actionType);
                    dto.setActionTypeLabel(getActionTypeLabel(actionType));
                    dto.setCount(count);
                    dto.setPercentage(String.format("%.1f%%", percentage));
                    dto.setColor(getActionTypeColor(actionType));
                    return dto;
                })
                .sorted((a, b) -> b.getCount().compareTo(a.getCount()))
                .collect(Collectors.toList());

        stats.setActionTypeStats(actionTypeStats);

        // Statistiques par secteur - filtrer par formulaires accessibles
        List<Object[]> secteurStats = formHistoryRepository.getSecteurStatisticsForAccessibleForms(accessibleFormIds);
        long totalSecteurActions = secteurStats.stream().mapToLong(arr -> (Long) arr[1]).sum();

        List<SecteurStatsDTO> secteurStatsDTO = secteurStats.stream()
                .map(arr -> {
                    String secteur = (String) arr[0];
                    Long count = (Long) arr[1];
                    double percentage = totalSecteurActions > 0 ? (double) count / totalSecteurActions * 100 : 0;

                    SecteurStatsDTO dto = new SecteurStatsDTO();
                    dto.setSecteur(secteur != null ? secteur : "Non spécifié");
                    dto.setCount(count);
                    dto.setPercentage(String.format("%.1f%%", percentage));
                    return dto;
                })
                .sorted((a, b) -> b.getCount().compareTo(a.getCount()))
                .collect(Collectors.toList());

        stats.setSecteurStats(secteurStatsDTO);

        return stats;
    }
    /**
     * Obtenir l'activité récente
     */
    public List<FormHistoryDTO> getRecentActivity(int hours, Long currentUserId) {
        // Récupérer tous les IDs de formulaires accessibles par l'utilisateur
        List<Long> accessibleFormIds = getAccessibleFormIds(currentUserId);

        if (accessibleFormIds.isEmpty()) {
            return new ArrayList<>();
        }

        LocalDateTime since = LocalDateTime.now().minusHours(hours);
        List<FormHistory> recentHistory = formHistoryRepository.findRecentActivityForAccessibleForms(since, accessibleFormIds);

        return recentHistory.stream()
                .map(this::convertToDTO)
                .limit(50) // Limiter à 50 entrées récentes
                .collect(Collectors.toList());
    }
    /**
     * Convertir FormHistory en DTO
     */
    private FormHistoryDTO convertToDTO(FormHistory history) {
        FormHistoryDTO dto = new FormHistoryDTO();
        dto.setId(history.getId());
        dto.setFormId(history.getFormId());
        dto.setFormName(history.getFormName());
        dto.setSecteur(history.getSecteur());
        dto.setDescription(history.getDescription());
        dto.setStatus(history.getStatus());
        dto.setStatusLabel(history.getStatus() != null ?
                getStatusLabel(history.getStatus()) : "Statut inconnu");
        dto.setActionType(history.getActionType());
        dto.setActionTypeLabel(getActionTypeLabel(history.getActionType()));
        dto.setActionDescription(history.getActionDescription());
        dto.setPerformedById(history.getPerformedById());
        dto.setPerformedByUsername(history.getPerformedByUsername());
        dto.setPerformedByEmail(history.getPerformedByEmail());
        dto.setFieldCount(history.getFieldCount());
        dto.setIsInLibrary(history.getIsInLibrary());
        dto.setLibrarySharedDate(history.getLibrarySharedDate());
        dto.setChangesDetails(history.getChangesDetails());
        dto.setIpAddress(history.getIpAddress());
        dto.setCreatedAt(history.getCreatedAt());

        // Désérialiser les groupes
// Désérialiser les groupes
        if (history.getAssignedGroups() != null && !history.getAssignedGroups().trim().isEmpty()) {
            try {
                List<Map<String, Object>> groupsData = objectMapper.readValue(
                        history.getAssignedGroups(),
                        objectMapper.getTypeFactory().constructCollectionType(List.class, Map.class)
                );

                List<GroupInfoDTO> groups = groupsData.stream()
                        .map(groupData -> {
                            GroupInfoDTO groupInfo = new GroupInfoDTO();
                            groupInfo.setId(((Number) groupData.get("id")).longValue());
                            groupInfo.setName((String) groupData.get("name"));
                            groupInfo.setColor((String) groupData.get("color"));
                            return groupInfo;
                        })
                        .collect(Collectors.toList());

                dto.setAssignedGroups(groups);
            } catch (Exception e) {
                logger.error("Erreur lors de la désérialisation des groupes: {}", e.getMessage());
                dto.setAssignedGroups(new ArrayList<>());
            }
        } else {
            dto.setAssignedGroups(new ArrayList<>());  // ← Ajouté pour éviter null
        }
        // Formatage des dates
        dto.setFormattedDate(history.getCreatedAt().format(DateTimeFormatter.ofPattern("dd/MM/yyyy à HH:mm")));
        dto.setTimeAgo(getTimeAgo(history.getCreatedAt()));
        dto.setActionIcon(getActionTypeIcon(history.getActionType()));
        dto.setActionColor(getActionTypeColor(history.getActionType()));

        return dto;
    }
    private String getStatusLabel(String status) {
        if (status == null) {
            return "Statut inconnu";
        }
        switch (status) {
            case "DRAFT": return "Brouillon";
            case "PUBLISHED": return "Publié";
            case "ARCHIVED": return "Archivé";
            case "DELETED": return "Supprimé";
            default: return status;
        }
    }
    /**
     * Méthodes utilitaires pour l'affichage
     */
    private String getTimeAgo(LocalDateTime dateTime) {
        LocalDateTime now = LocalDateTime.now();
        long minutes = ChronoUnit.MINUTES.between(dateTime, now);
        long hours = ChronoUnit.HOURS.between(dateTime, now);
        long days = ChronoUnit.DAYS.between(dateTime, now);

        if (minutes < 60) {
            return minutes <= 1 ? "À l'instant" : "Il y a " + minutes + " minutes";
        } else if (hours < 24) {
            return "Il y a " + hours + " heure" + (hours > 1 ? "s" : "");
        } else if (days < 7) {
            return "Il y a " + days + " jour" + (days > 1 ? "s" : "");
        } else {
            return dateTime.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        }
    }

    private String getActionTypeLabel(String actionType) {
        if (actionType == null) {
            return "Action inconnue";
        }
        switch (actionType) {
            case "CREATED": return "Création";
            case "UPDATED": return "Modification";
            case "PUBLISHED": return "Publication";
            case "ARCHIVED": return "Archivage";
            case "DELETED": return "Suppression";
            case "SHARED_TO_LIBRARY": return "Partagé vers bibliothèque";
            case "REMOVED_FROM_LIBRARY": return "Retiré de la bibliothèque";
            case "GROUPS_ASSIGNED": return "Groupes assignés";
            default: return actionType;
        }
    }
    private String getActionTypeIcon(String actionType) {
        if (actionType == null) {
            return "help_outline";
        }
        switch (actionType) {
            case "CREATED": return "add_circle";
            case "UPDATED": return "edit";
            case "PUBLISHED": return "publish";
            case "ARCHIVED": return "archive";
            case "DELETED": return "delete";
            case "SHARED_TO_LIBRARY": return "library_add";
            case "REMOVED_FROM_LIBRARY": return "library_remove";
            case "GROUPS_ASSIGNED": return "group";
            default: return "history";
        }
    }

    private String getActionTypeColor(String actionType) {
        if (actionType == null) {
            return "#999999";
        }
        switch (actionType) {
            case "CREATED": return "#4caf50";
            case "UPDATED": return "#2196f3";
            case "PUBLISHED": return "#ff9800";
            case "ARCHIVED": return "#9e9e9e";
            case "DELETED": return "#f44336";
            case "SHARED_TO_LIBRARY": return "#9c27b0";
            case "REMOVED_FROM_LIBRARY": return "#795548";
            case "GROUPS_ASSIGNED": return "#607d8b";
            default: return "#666666";
        }
    }

    /**
     * Vérifier si un utilisateur a accès à un formulaire
     */
    private boolean hasAccessToForm(Long formId, Long userId) {
        Optional<Form> formOpt = formRepository.findByIdWithGroupsAndCreator(formId);
        if (!formOpt.isPresent()) {
            return false;
        }

        Optional<Utilisateur> userOpt = userRepository.findById(userId);
        if (!userOpt.isPresent()) {
            return false;
        }

        return formOpt.get().isAccessibleByUser(userOpt.get());
    }

    /**
     * Récupérer tous les IDs des formulaires accessibles par un utilisateur
     */
    private List<Long> getAccessibleFormIds(Long userId) {
        Utilisateur user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

        List<Form> allForms = formRepository.findAllWithGroupsAndCreator();
        List<Long> accessibleFormIds = new ArrayList<>();

        for (Form form : allForms) {
            if (form.isAccessibleByUser(user)) {
                accessibleFormIds.add(form.getId());
            }
        }

        return accessibleFormIds;
    }
}