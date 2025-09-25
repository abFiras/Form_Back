package com.form.form_back.Service;

import com.form.form_back.Entity.ExternalList;
import com.form.form_back.Entity.ExternalListItem;
import com.form.form_back.Entity.Utilisateur;
import com.form.form_back.Repo.ExternalListRepository;
import com.form.form_back.Repo.ExternalListItemRepository;
import com.form.form_back.Repo.UtilisateurRepository;
import com.form.form_back.dto.CreateExternalListRequest;
import com.form.form_back.dto.ExternalListDTO;

import com.form.form_back.dto.ExternalListItemDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class ExternalListService {

    @Autowired
    private ExternalListRepository externalListRepository;

    @Autowired
    private ExternalListItemRepository externalListItemRepository;
@Autowired
private UtilisateurRepository utilisateurRepository;
    /**
     * Récupère toutes les listes externes
     */
    public List<ExternalListDTO> getAllExternalLists() {
        return externalListRepository.findAll()
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Récupère toutes les listes externes créées par un utilisateur
     */
    public List<ExternalListDTO> getExternalListsByUser(Long userId) {
        return externalListRepository.findByUtilisateurIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Récupère une liste externe par ID avec ses éléments
     */
    public ExternalListDTO getExternalListById(Long id) {
        ExternalList list = externalListRepository.findByIdWithItems(id)
                .orElseThrow(() -> new RuntimeException("Liste externe non trouvée"));

        return convertToDTOWithItems(list);
    }

    /**
     * Crée une nouvelle liste externe
     */
    public ExternalListDTO createExternalList(CreateExternalListRequest request, Long userId) {
        ExternalList externalList = new ExternalList();
        externalList.setName(request.getName());
        externalList.setDescription(request.getDescription());
        externalList.setListType(request.getListType());
        externalList.setRubrique(request.getRubrique());
        externalList.setIsAdvanced(request.getIsAdvanced());
        externalList.setIsFiltered(request.getIsFiltered());
        externalList.setCreatedAt(LocalDateTime.now());
        externalList.setUpdatedAt(LocalDateTime.now());

        // 🔹 Récupérer l'utilisateur et l'associer
        Utilisateur user = utilisateurRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable"));
        externalList.setUtilisateur(user);

        ExternalList savedList = externalListRepository.save(externalList);

        // Ajouter les éléments
        if (request.getItems() != null && !request.getItems().isEmpty()) {
            for (int i = 0; i < request.getItems().size(); i++) {
                ExternalListItemDTO itemDTO = request.getItems().get(i);
                ExternalListItem item = new ExternalListItem();
                item.setLabel(itemDTO.getLabel());
                item.setValue(itemDTO.getValue());
                item.setDisplayOrder(i);
                item.setIsActive(true);
                item.setExternalList(savedList);
                item.setExtraData(itemDTO.getExtraData());

                externalListItemRepository.save(item);
            }
        }

        return convertToDTO(savedList);
    }


    /**
     * Met à jour une liste externe
     */
    public ExternalListDTO updateExternalList(Long id, CreateExternalListRequest request) {
        ExternalList externalList = externalListRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Liste externe non trouvée"));

        externalList.setName(request.getName());
        externalList.setDescription(request.getDescription());
        externalList.setListType(request.getListType());
        externalList.setRubrique(request.getRubrique());
        externalList.setIsAdvanced(request.getIsAdvanced());
        externalList.setIsFiltered(request.getIsFiltered());
        externalList.setUpdatedAt(LocalDateTime.now());

        // Supprimer les anciens éléments
        externalListItemRepository.deleteByExternalListId(id);

        // Ajouter les nouveaux éléments
        if (request.getItems() != null && !request.getItems().isEmpty()) {
            for (int i = 0; i < request.getItems().size(); i++) {
                ExternalListItemDTO itemDTO = request.getItems().get(i);
                ExternalListItem item = new ExternalListItem();
                item.setLabel(itemDTO.getLabel());
                item.setValue(itemDTO.getValue());
                item.setDisplayOrder(i);
                item.setIsActive(true);
                item.setExternalList(externalList);
                item.setExtraData(itemDTO.getExtraData());

                externalListItemRepository.save(item);
            }
        }

        ExternalList savedList = externalListRepository.save(externalList);
        return convertToDTO(savedList);
    }

    /**
     * Supprime une liste externe
     */
    public void deleteExternalList(Long id) {
        if (!externalListRepository.existsById(id)) {
            throw new RuntimeException("Liste externe non trouvée");
        }
        externalListRepository.deleteById(id);
    }

    /**
     * Importe des données depuis un fichier CSV
     */
    public ExternalListDTO importFromCSV(MultipartFile file, String listName, String description,
                                         String rubrique, Long userId) throws IOException {

        // Créer la liste externe
        ExternalList externalList = new ExternalList();
        externalList.setName(listName);
        externalList.setDescription(description);
        externalList.setListType("STATIC");
        externalList.setRubrique(rubrique);
        externalList.setIsAdvanced(false);
        externalList.setIsFiltered(false);
        Utilisateur user = utilisateurRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable"));
        externalList.setUtilisateur(user);

        ExternalList savedList = externalListRepository.save(externalList);

        // Lire le fichier CSV
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream()))) {
            String line;
            int order = 0;
            boolean isFirstLine = true;

            while ((line = reader.readLine()) != null) {
                // Ignorer l'en-tête s'il existe
                if (isFirstLine && (line.toLowerCase().contains("label") || line.toLowerCase().contains("nom"))) {
                    isFirstLine = false;
                    continue;
                }

                String[] columns = line.split(",");
                if (columns.length >= 1) {
                    String label = columns[0].trim();
                    String value = columns.length >= 2 ? columns[1].trim() : label.toLowerCase().replace(" ", "_");

                    if (!label.isEmpty()) {
                        ExternalListItem item = new ExternalListItem();
                        item.setLabel(label);
                        item.setValue(value);
                        item.setDisplayOrder(order++);
                        item.setIsActive(true);
                        item.setExternalList(savedList);

                        // Données supplémentaires si disponibles
                        if (columns.length > 2) {
                            StringBuilder extraData = new StringBuilder("{");
                            for (int i = 2; i < columns.length; i++) {
                                if (i > 2) extraData.append(",");
                                extraData.append("\"col").append(i).append("\":\"").append(columns[i].trim()).append("\"");
                            }
                            extraData.append("}");
                            item.setExtraData(extraData.toString());
                        }

                        externalListItemRepository.save(item);
                    }
                }
                isFirstLine = false;
            }
        }

        return convertToDTO(savedList);
    }

    /**
     * Récupère les éléments actifs d'une liste externe
     */
    public List<ExternalListItemDTO> getListItems(Long listId) {
        return externalListItemRepository.findByExternalListIdAndIsActiveTrueOrderByDisplayOrderAsc(listId)
                .stream()
                .map(this::convertItemToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Récupère toutes les rubriques disponibles
     */
    public List<String> getAllRubriques() {
        return externalListRepository.findDistinctRubriques();
    }

    /**
     * Recherche des listes externes par nom
     */
    public List<ExternalListDTO> searchListsByName(String searchTerm) {
        return externalListRepository.findByNameContainingIgnoreCaseOrderByNameAsc(searchTerm)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Récupère les listes externes par rubrique
     */
    public List<ExternalListDTO> getListsByRubrique(String rubrique) {
        return externalListRepository.findByRubriqueOrderByNameAsc(rubrique)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Convertit ExternalList en ExternalListDTO
     */
    public ExternalListDTO convertToDTO(ExternalList entity) {
        ExternalListDTO dto = new ExternalListDTO();
        dto.setId(entity.getId());
        dto.setName(entity.getName());
        dto.setDescription(entity.getDescription());
        dto.setListType(entity.getListType());
        dto.setRubrique(entity.getRubrique());
        dto.setIsAdvanced(entity.getIsAdvanced());
        dto.setIsFiltered(entity.getIsFiltered());
        dto.setCreatedAt(entity.getCreatedAt());
        dto.setUpdatedAt(entity.getUpdatedAt());
        if (entity.getUtilisateur() != null) {
            dto.setCreatedBy(entity.getUtilisateur().getId());
            dto.setCreatedName(entity.getUtilisateur().getUsername());
        }
        // Compter les éléments actifs
        Long itemCount = externalListItemRepository.countActiveItemsByListId(entity.getId());
        dto.setItemCount(itemCount.intValue());

        return dto;
    }

    /**
     * Convertit ExternalList en ExternalListDTO avec ses éléments
     */
    private ExternalListDTO convertToDTOWithItems(ExternalList entity) {
        ExternalListDTO dto = convertToDTO(entity);

        if (entity.getItems() != null) {
            List<ExternalListItemDTO> itemDTOs = entity.getItems()
                    .stream()
                    .filter(ExternalListItem::getIsActive)
                    .sorted((a, b) -> Integer.compare(a.getDisplayOrder(), b.getDisplayOrder()))
                    .map(this::convertItemToDTO)
                    .collect(Collectors.toList());
            dto.setItems(itemDTOs);
        }

        return dto;
    }

    /**
     * Convertit ExternalListItem en ExternalListItemDTO
     */
    private ExternalListItemDTO convertItemToDTO(ExternalListItem item) {
        ExternalListItemDTO dto = new ExternalListItemDTO();
        dto.setId(item.getId());
        dto.setLabel(item.getLabel());
        dto.setValue(item.getValue());
        dto.setDisplayOrder(item.getDisplayOrder());
        dto.setIsActive(item.getIsActive());
        dto.setExtraData(item.getExtraData());
        return dto;
    }
}