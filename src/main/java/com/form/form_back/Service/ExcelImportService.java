package com.form.form_back.Service;

import com.form.form_back.Entity.ExternalList;
import com.form.form_back.Entity.ExternalListItem;
import com.form.form_back.Entity.Utilisateur;
import com.form.form_back.Repo.ExternalListRepository;
import com.form.form_back.Repo.ExternalListItemRepository;
import com.form.form_back.Repo.UtilisateurRepository;
import com.form.form_back.dto.ExcelImportResponse;
import com.form.form_back.dto.ExcelValidationResult;
import com.form.form_back.dto.ExternalListDTO;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;

@Service
@Transactional
public class ExcelImportService {

    @Autowired
    private ExternalListRepository externalListRepository;

    @Autowired
    private ExternalListItemRepository externalListItemRepository;

    @Autowired
    private UtilisateurRepository utilisateurRepository;

    @Autowired
    private ExternalListService externalListService;

    private static final int MAX_PREVIEW_ROWS = 10;
    private static final int MAX_FILE_SIZE = 5 * 1024 * 1024; // 5MB
    private static final String[] ALLOWED_EXTENSIONS = {".xlsx", ".xls"};

    private static final Logger logger = LoggerFactory.getLogger(ExcelImportService.class);

    /**
     * Valide un fichier Excel et retourne un aperçu des données
     */
    public ExcelValidationResult validateExcelFile(MultipartFile file) throws IOException {
        ExcelValidationResult result = new ExcelValidationResult();

        // Validation du fichier
        if (!isValidExcelFile(file, result)) {
            return result;
        }

        try (Workbook workbook = createWorkbook(file)) {
            Sheet sheet = workbook.getSheetAt(0);

            if (sheet.getPhysicalNumberOfRows() == 0) {
                result.getErrors().add("Le fichier Excel est vide");
                result.setValid(false);
                return result;
            }

            // Analyser les données
            analyzeExcelData(sheet, result);

            if (result.getErrors().isEmpty()) {
                result.setValid(true);
            }

        } catch (Exception e) {
            result.getErrors().add("Erreur lors de la lecture du fichier Excel: " + e.getMessage());
            result.setValid(false);
        }

        return result;
    }

    /**
     * Modifiez la méthode importFromExcel pour utiliser la nouvelle méthode
     */
    public ExcelImportResponse importFromExcel(MultipartFile file, String listName,
                                               String description, String rubrique, Long userId)
            throws IOException {
        ExcelImportResponse response = new ExcelImportResponse();

        // Valider d'abord le fichier
        ExcelValidationResult validation = validateExcelFile(file);
        if (!validation.isValid()) {
            response.setSuccess(false);
            response.setMessage("Fichier Excel invalide");
            response.setErrors(validation.getErrors());
            return response;
        }

        try {
            // Créer la liste externe avec les données du fichier Excel
            ExternalList externalList = createExternalListFromExcelData(file, listName, description, rubrique, userId);

            // Importer les données
            int importedItems = importDataFromExcel(file, externalList);

            // Convertir en DTO
            ExternalListDTO listDTO = externalListService.convertToDTO(externalList);

            response.setSuccess(true);
            response.setMessage("Importation réussie: " + importedItems + " éléments importés");
            response.setData(listDTO);
            response.setWarnings(validation.getWarnings());

        } catch (Exception e) {
            response.setSuccess(false);
            response.setMessage("Erreur lors de l'importation: " + e.getMessage());
            response.getErrors().add(e.getMessage());
        }

        return response;
    }
    /**
     * Vérifie si une liste avec le même nom existe pour un utilisateur
     */
    public boolean existsByNameAndUser(String listName, Long userId) {
        return externalListRepository.existsByNameAndUtilisateurId(listName, userId);
    }

    /**
     * Récupère l'ID d'une liste par nom et utilisateur
     */
    public Long getListIdByNameAndUser(String listName, Long userId) {
        return externalListRepository.findIdByNameAndUtilisateurId(listName, userId)
                .orElse(null);
    }

    /**
     * Valide les propriétés de base du fichier Excel
     */
    private boolean isValidExcelFile(MultipartFile file, ExcelValidationResult result) {
        // Vérifier la taille
        if (file.getSize() > MAX_FILE_SIZE) {
            result.getErrors().add("Le fichier dépasse la taille maximale autorisée (5MB)");
            result.setValid(false);
            return false;
        }

        // Vérifier l'extension
        String fileName = file.getOriginalFilename();
        if (fileName == null) {
            result.getErrors().add("Nom de fichier invalide");
            result.setValid(false);
            return false;
        }

        boolean validExtension = Arrays.stream(ALLOWED_EXTENSIONS)
                .anyMatch(ext -> fileName.toLowerCase().endsWith(ext));

        if (!validExtension) {
            result.getErrors().add("Format de fichier non supporté. Utilisez .xlsx ou .xls");
            result.setValid(false);
            return false;
        }

        // Vérifier le type MIME
        String contentType = file.getContentType();
        if (contentType == null || (!contentType.equals("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
                && !contentType.equals("application/vnd.ms-excel"))) {
            result.getWarnings().add("Type MIME du fichier non reconnu, mais l'extension semble correcte");
        }

        return true;
    }

    /**
     * Crée un workbook à partir du fichier
     */
    private Workbook createWorkbook(MultipartFile file) throws IOException {
        String fileName = file.getOriginalFilename();
        if (fileName != null && fileName.toLowerCase().endsWith(".xlsx")) {
            return new XSSFWorkbook(file.getInputStream());
        } else {
            return new HSSFWorkbook(file.getInputStream());
        }
    }

    /**
     * Analyse les données du fichier Excel
     */
    /**
     * Analyse les données du fichier Excel - VERSION CORRIGÉE
     */
    /**
     * Version debug complète pour identifier le problème exact
     */
    /**
     * Version ultra-simple pour diagnostiquer le problème
     */
    private void analyzeExcelData(Sheet sheet, ExcelValidationResult result) {
        int totalRows = sheet.getLastRowNum() + 1;
        result.setTotalRows(totalRows);

        logger.info("=== VALIDATION RENFORCÉE ===");
        logger.info("Nombre total de lignes: {}", totalRows);

        if (totalRows == 0) {
            result.getErrors().add("La feuille Excel est vide");
            return;
        }

        // 1. VALIDATION DE LA STRUCTURE OBLIGATOIRE
        if (!validateRequiredStructure(sheet, result, totalRows)) {
            return; // Arrêter si la structure de base n'est pas respectée
        }

        // 2. VALIDATION DES EN-TÊTES DE CONFIGURATION
        if (!validateConfigurationHeaders(sheet, result)) {
            return; // Arrêter si les en-têtes de configuration ne sont pas valides
        }

        // 3. VALIDATION DES DONNÉES DE CONFIGURATION
        if (!validateConfigurationData(sheet, result)) {
            return; // Arrêter si les données de configuration ne sont pas valides
        }

        // 4. VALIDATION DES EN-TÊTES DES ÉLÉMENTS
        int headerRowIndex = findElementsHeaderRow(sheet);
        if (headerRowIndex == -1) {
            result.getErrors().add("Section des éléments non trouvée. Le fichier doit contenir une ligne avec 'Libellé' en colonne B");
            return;
        }

        Row headerRow = sheet.getRow(headerRowIndex);
        if (!validateElementsHeaders(headerRow, result)) {
            return; // Arrêter si les en-têtes des éléments ne sont pas valides
        }

        // 5. VALIDATION DES DONNÉES DES ÉLÉMENTS
        List<String> headers = extractHeaders(headerRow);
        result.setHeaders(headers);

        if (!validateElementsData(sheet, headerRowIndex, headers, result, totalRows)) {
            return; // Arrêter si les données des éléments ne sont pas valides
        }

        logger.info("=== VALIDATION RÉUSSIE ===");
        result.setValid(true);
    }

    /**
     * Valide la structure générale obligatoire du fichier
     */
    private boolean validateRequiredStructure(Sheet sheet, ExcelValidationResult result, int totalRows) {
        // Vérifier qu'il y a au moins 4 lignes (config + headers + au moins 1 élément)
        if (totalRows < 4) {
            result.getErrors().add("Le fichier doit contenir au moins 4 lignes : " +
                    "1 ligne d'en-têtes de configuration, 1 ligne de données de configuration, " +
                    "1 ligne d'en-têtes d'éléments, et au moins 1 élément");
            return false;
        }

        // Vérifier qu'il y a une ligne vide entre config et éléments
        boolean foundEmptyLine = false;
        for (int i = 2; i < Math.min(5, totalRows); i++) {
            Row row = sheet.getRow(i);
            if (row == null || isRowEmpty(row)) {
                foundEmptyLine = true;
                break;
            }
        }

        if (!foundEmptyLine) {
            result.getWarnings().add("Il est recommandé d'avoir une ligne vide entre la configuration et les éléments");
        }

        return true;
    }

    /**
     * Valide les en-têtes de configuration (première ligne)
     */
    private boolean validateConfigurationHeaders(Sheet sheet, ExcelValidationResult result) {
        Row configHeaderRow = sheet.getRow(0);
        if (configHeaderRow == null) {
            result.getErrors().add("La première ligne (en-têtes de configuration) est vide");
            return false;
        }

        // En-têtes obligatoires pour la configuration
        String[] requiredConfigHeaders = {"Nom de la liste", "Nombre d'éléments", "Type"};
        String[] optionalConfigHeaders = {"Rubrique", "Liste avancée", "Filtrée", "Propriétaire"};

        List<String> foundHeaders = new ArrayList<>();
        for (int i = 0; i < configHeaderRow.getPhysicalNumberOfCells(); i++) {
            Cell cell = configHeaderRow.getCell(i);
            String header = getCellValueAsString(cell);
            if (header != null && !header.trim().isEmpty()) {
                foundHeaders.add(header.trim());
            }
        }

        // Vérifier la présence des en-têtes obligatoires
        for (String requiredHeader : requiredConfigHeaders) {
            if (!foundHeaders.stream().anyMatch(h -> h.equalsIgnoreCase(requiredHeader))) {
                result.getErrors().add("En-tête de configuration manquant : '" + requiredHeader + "'");
                return false;
            }
        }

        logger.info("En-têtes de configuration trouvés : {}", foundHeaders);
        return true;
    }

    /**
     * Valide les données de configuration (deuxième ligne)
     */
    private boolean validateConfigurationData(Sheet sheet, ExcelValidationResult result) {
        Row configDataRow = sheet.getRow(1);
        if (configDataRow == null) {
            result.getErrors().add("La deuxième ligne (données de configuration) est vide");
            return false;
        }

        Row configHeaderRow = sheet.getRow(0);
        Map<String, String> configData = new HashMap<>();

        for (int i = 0; i < Math.min(configHeaderRow.getPhysicalNumberOfCells(), configDataRow.getPhysicalNumberOfCells()); i++) {
            String header = getCellValueAsString(configHeaderRow.getCell(i));
            String value = getCellValueAsString(configDataRow.getCell(i));

            if (header != null && !header.trim().isEmpty()) {
                configData.put(header.trim(), value != null ? value.trim() : "");
            }
        }

        // Validation du nom de la liste
        String listName = configData.get("Nom de la liste");
        if (listName == null || listName.isEmpty()) {
            result.getErrors().add("Le nom de la liste est obligatoire");
            return false;
        }

        // Validation du type
        String type = configData.get("Type");
        if (type != null && !type.isEmpty()) {
            if (!type.equalsIgnoreCase("Statique") && !type.equalsIgnoreCase("Dynamique") &&
                    !type.equalsIgnoreCase("Static") && !type.equalsIgnoreCase("Dynamic")) {
                result.getErrors().add("Le type doit être 'Statique' ou 'Dynamique' (trouvé : '" + type + "')");
                return false;
            }
        }

        // Validation du nombre d'éléments
        String nombreElements = configData.get("Nombre d'éléments");
        if (nombreElements != null && !nombreElements.isEmpty() && !nombreElements.equals("-")) {
            try {
                int expected = Integer.parseInt(nombreElements);
                if (expected <= 0) {
                    result.getErrors().add("Le nombre d'éléments doit être positif");
                    return false;
                }
            } catch (NumberFormatException e) {
                result.getErrors().add("Le nombre d'éléments doit être un nombre entier valide");
                return false;
            }
        }

        // Validation des valeurs booléennes
        validateBooleanConfigValue(configData, "Liste avancée", result);
        validateBooleanConfigValue(configData, "Filtrée", result);

        logger.info("Données de configuration validées : {}", configData);
        return result.getErrors().isEmpty();
    }

    /**
     * Valide une valeur de configuration booléenne
     */
    private void validateBooleanConfigValue(Map<String, String> configData, String key, ExcelValidationResult result) {
        String value = configData.get(key);
        if (value != null && !value.isEmpty() && !value.equals("-")) {
            String normalizedValue = value.toLowerCase().trim();
            List<String> validValues = Arrays.asList("oui", "non", "yes", "no", "true", "false", "1", "0", "vrai", "faux");

            if (!validValues.contains(normalizedValue)) {
                result.getWarnings().add("Valeur non reconnue pour '" + key + "' : '" + value +
                        "'. Valeurs acceptées : Oui/Non, True/False, 1/0");
            }
        }
    }

    /**
     * Trouve la ligne des en-têtes des éléments
     */
    private int findElementsHeaderRow(Sheet sheet) {
        int totalRows = sheet.getLastRowNum() + 1;
        for (int i = 0; i < totalRows; i++) {
            Row row = sheet.getRow(i);
            if (row != null && row.getPhysicalNumberOfCells() > 1) {
                Cell cellB = row.getCell(1);
                String value = getCellValueAsString(cellB);
                if ("Libellé".equals(value)) {
                    return i;
                }
            }
        }
        return -1;
    }

    /**
     * Valide les en-têtes des éléments
     */
    private boolean validateElementsHeaders(Row headerRow, ExcelValidationResult result) {
        if (headerRow == null) {
            result.getErrors().add("Ligne d'en-têtes des éléments introuvable");
            return false;
        }

        // En-têtes obligatoires pour les éléments
        String[] requiredElementHeaders = {"#", "Libellé"};
        String[] optionalElementHeaders = {"Valeur", "Statut"};

        List<String> foundHeaders = new ArrayList<>();
        for (int i = 0; i < headerRow.getPhysicalNumberOfCells(); i++) {
            Cell cell = headerRow.getCell(i);
            String header = getCellValueAsString(cell);
            if (header != null && !header.trim().isEmpty()) {
                foundHeaders.add(header.trim());
            }
        }

        // Vérifier la présence des en-têtes obligatoires
        for (String requiredHeader : requiredElementHeaders) {
            if (!foundHeaders.stream().anyMatch(h -> h.equalsIgnoreCase(requiredHeader))) {
                result.getErrors().add("En-tête d'élément manquant : '" + requiredHeader + "'");
                return false;
            }
        }

        // Vérifier l'ordre des colonnes
        if (foundHeaders.size() >= 2) {
            if (!"#".equals(foundHeaders.get(0))) {
                result.getWarnings().add("La première colonne devrait être '#' pour l'ordre");
            }
            if (!"Libellé".equals(foundHeaders.get(1))) {
                result.getErrors().add("La deuxième colonne doit être 'Libellé'");
                return false;
            }
        }

        logger.info("En-têtes des éléments validés : {}", foundHeaders);
        return true;
    }

    /**
     * Valide les données des éléments
     */
    private boolean validateElementsData(Sheet sheet, int headerRowIndex, List<String> headers,
                                         ExcelValidationResult result, int totalRows) {
        List<Map<String, Object>> preview = new ArrayList<>();
        int validCount = 0;
        Set<String> usedLabels = new HashSet<>();
        Set<String> usedValues = new HashSet<>();

        for (int i = headerRowIndex + 1; i < totalRows; i++) {
            Row row = sheet.getRow(i);

            if (row == null) {
                continue; // Ignorer les lignes nulles
            }

            if (row.getPhysicalNumberOfCells() < 2) {
                continue; // Ignorer les lignes sans assez de données
            }

            // Extraire le libellé (colonne B = index 1)
            Cell libelleCell = row.getCell(1);
            String libelle = getCellValueAsString(libelleCell);

            if (libelle == null || libelle.trim().isEmpty() ||
                    libelle.equals("#") || libelle.equals("Libellé")) {
                continue; // Ignorer les lignes sans libellé valide
            }

            validCount++;

            // Validation des doublons de libellés
            String normalizedLabel = libelle.trim().toLowerCase();
            if (usedLabels.contains(normalizedLabel)) {
                result.getWarnings().add("Libellé en doublon détecté : '" + libelle + "' (ligne " + (i + 1) + ")");
            } else {
                usedLabels.add(normalizedLabel);
            }

            // Validation de la valeur si présente
            if (row.getPhysicalNumberOfCells() > 2) {
                Cell valeurCell = row.getCell(2);
                String valeur = getCellValueAsString(valeurCell);
                if (valeur != null && !valeur.trim().isEmpty()) {
                    String normalizedValue = valeur.trim().toLowerCase();
                    if (usedValues.contains(normalizedValue)) {
                        result.getWarnings().add("Valeur en doublon détectée : '" + valeur + "' (ligne " + (i + 1) + ")");
                    } else {
                        usedValues.add(normalizedValue);
                    }
                }
            }

            // Validation du statut si présent
            if (row.getPhysicalNumberOfCells() > 3) {
                Cell statutCell = row.getCell(3);
                String statut = getCellValueAsString(statutCell);
                if (statut != null && !statut.trim().isEmpty()) {
                    if (!statut.equalsIgnoreCase("Actif") && !statut.equalsIgnoreCase("Inactif") &&
                            !statut.equalsIgnoreCase("Active") && !statut.equalsIgnoreCase("Inactive")) {
                        result.getWarnings().add("Statut non reconnu : '" + statut + "' (ligne " + (i + 1) +
                                "). Valeurs acceptées : Actif/Inactif");
                    }
                }
            }

            // Ajouter à l'aperçu (limité aux 10 premiers)
            if (preview.size() < 10) {
                Map<String, Object> rowData = new LinkedHashMap<>();
                for (int j = 0; j < Math.min(headers.size(), row.getPhysicalNumberOfCells()); j++) {
                    String header = headers.get(j);
                    Cell cell = row.getCell(j);
                    String cellValue = getCellValueAsString(cell);
                    rowData.put(header, cellValue != null ? cellValue : "");
                }
                preview.add(rowData);
            }
        }

        result.setPreviewData(preview);

        // Validation du nombre d'éléments
        if (validCount == 0) {
            result.getErrors().add("Aucun élément valide trouvé dans le fichier");
            return false;
        }

        // Vérifier la cohérence avec le nombre d'éléments déclaré
        Row configDataRow = sheet.getRow(1);
        if (configDataRow != null && configDataRow.getPhysicalNumberOfCells() > 2) {
            Cell nombreElementsCell = configDataRow.getCell(2); // Colonne C
            String nombreElementsStr = getCellValueAsString(nombreElementsCell);
            if (nombreElementsStr != null && !nombreElementsStr.isEmpty() && !nombreElementsStr.equals("-")) {
                try {
                    int expectedCount = Integer.parseInt(nombreElementsStr);
                    if (expectedCount != validCount) {
                        result.getWarnings().add("Nombre d'éléments déclaré (" + expectedCount +
                                ") différent du nombre d'éléments trouvés (" + validCount + ")");
                    }
                } catch (NumberFormatException e) {
                    // Déjà géré dans validateConfigurationData
                }
            }
        }

        logger.info("=== VALIDATION DES ÉLÉMENTS ===");
        logger.info("Éléments valides trouvés: {}", validCount);
        logger.info("Libellés uniques: {}", usedLabels.size());
        logger.info("Valeurs uniques: {}", usedValues.size());

        return true;
    }
    private List<String> extractHeaders(Row headerRow) {
        List<String> headers = new ArrayList<>();

        for (int i = 0; i < headerRow.getPhysicalNumberOfCells(); i++) {
            Cell cell = headerRow.getCell(i);
            String headerValue = getCellValueAsString(cell);

            if (headerValue != null && !headerValue.trim().isEmpty()) {
                headers.add(headerValue.trim());
            } else {
                headers.add("Colonne_" + (i + 1));
            }
        }

        return headers;
    }

    /**
     * Valide la structure des données
     */
    private void validateDataStructure(Sheet sheet, List<String> headers, ExcelValidationResult result) {
        int emptyRows = 0;
        int rowsWithData = 0;

        // Commencer à la ligne 1 (ignorer les en-têtes)
        for (int i = 1; i < sheet.getPhysicalNumberOfRows(); i++) {
            Row row = sheet.getRow(i);

            if (row == null || isRowEmpty(row)) {
                emptyRows++;
                continue;
            }

            rowsWithData++;

            // Vérifier que la ligne a au moins une valeur dans les colonnes importantes
            boolean hasValidData = false;
            for (int j = 0; j < Math.min(headers.size(), row.getPhysicalNumberOfCells()); j++) {
                Cell cell = row.getCell(j);
                String cellValue = getCellValueAsString(cell);

                if (cellValue != null && !cellValue.trim().isEmpty()) {
                    hasValidData = true;
                    break;
                }
            }

            if (!hasValidData) {
                emptyRows++;
                rowsWithData--;
            }
        }

        if (rowsWithData == 0) {
            result.getErrors().add("Aucune donnée valide trouvée dans le fichier");
        } else {
            if (emptyRows > 0) {
                result.getWarnings().add(emptyRows + " ligne(s) vide(s) seront ignorées");
            }

            if (rowsWithData > 1000) {
                result.getWarnings().add("Le fichier contient " + rowsWithData + " lignes de données. " +
                        "L'importation peut prendre du temps.");
            }
        }
    }

    /**
     * Crée un aperçu des données pour la prévisualisation
     */
    private void createDataPreview(Sheet sheet, List<String> headers, ExcelValidationResult result) {
        List<Map<String, Object>> preview = new ArrayList<>();

        // Limiter l'aperçu aux premières lignes
        int maxRows = Math.min(sheet.getPhysicalNumberOfRows(), MAX_PREVIEW_ROWS + 1);

        for (int i = 1; i < maxRows; i++) { // Commencer à 1 pour ignorer les en-têtes
            Row row = sheet.getRow(i);

            if (row == null || isRowEmpty(row)) {
                continue;
            }

            Map<String, Object> rowData = new LinkedHashMap<>();

            for (int j = 0; j < headers.size(); j++) {
                String header = headers.get(j);
                Cell cell = row.getCell(j);
                String cellValue = getCellValueAsString(cell);

                rowData.put(header, cellValue != null ? cellValue : "");
            }

            preview.add(rowData);
        }

        result.setPreviewData(preview);
    }

    /**
     * Vérifie si une ligne est vide
     */
    private boolean isRowEmpty(Row row) {
        for (int i = 0; i < row.getPhysicalNumberOfCells(); i++) {
            Cell cell = row.getCell(i);
            String cellValue = getCellValueAsString(cell);

            if (cellValue != null && !cellValue.trim().isEmpty()) {
                return false;
            }
        }
        return true;
    }

    /**
     * Convertit une cellule en chaîne de caractères
     */
    private String getCellValueAsString(Cell cell) {
        if (cell == null) {
            return null;
        }

        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return cell.getDateCellValue().toString();
                } else {
                    double numericValue = cell.getNumericCellValue();
                    // Si c'est un entier, ne pas afficher les décimales
                    if (numericValue == Math.floor(numericValue)) {
                        return String.valueOf((long) numericValue);
                    } else {
                        return String.valueOf(numericValue);
                    }
                }
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:
                return cell.getCellFormula();
            case BLANK:
                return "";
            default:
                return "";
        }
    }

    /**
     * Crée une nouvelle liste externe
     */
    /**
     * Convertit les valeurs textuelles en booléens
     */
    private boolean convertTextToBoolean(String value) {
        if (value == null || value.trim().isEmpty()) {
            return false;
        }

        String normalizedValue = value.toLowerCase().trim();
        List<String> truthyValues = Arrays.asList("oui", "yes", "true", "1", "vrai", "o");

        return truthyValues.contains(normalizedValue);
    }

    /**
     * Extrait les données de configuration depuis la première ligne de données
     */
    private Map<String, String> extractConfigurationData(Sheet sheet, List<String> headers) {
        Map<String, String> config = new HashMap<>();

        // Prendre la première ligne de données (index 1, car 0 = headers)
        Row dataRow = sheet.getRow(1);
        if (dataRow == null) {
            return config;
        }

        for (int i = 0; i < headers.size() && i < dataRow.getPhysicalNumberOfCells(); i++) {
            String header = headers.get(i);
            Cell cell = dataRow.getCell(i);
            String value = getCellValueAsString(cell);

            if (value != null && !value.trim().isEmpty()) {
                config.put(header, value.trim());
            }
        }

        return config;
    }

    /**
     * Modifiez la méthode createExternalList pour utiliser les données du fichier Excel
     */
    private ExternalList createExternalListFromExcelData(MultipartFile file, String listName,
                                                         String description, String rubrique, Long userId) throws IOException {
        ExternalList externalList = new ExternalList();

        // Valeurs par défaut
        externalList.setName(listName);
        externalList.setDescription(description);
        externalList.setListType("STATIC");
        externalList.setRubrique(rubrique);
        externalList.setIsAdvanced(false);
        externalList.setIsFiltered(false);

        // Extraire les données de configuration depuis le fichier Excel
        try (Workbook workbook = createWorkbook(file)) {
            Sheet sheet = workbook.getSheetAt(0);
            Row headerRow = sheet.getRow(0);
            List<String> headers = extractHeaders(headerRow);

            Map<String, String> configData = extractConfigurationData(sheet, headers);

            // Appliquer les configurations depuis le fichier Excel
            if (configData.containsKey("Type")) {
                String type = configData.get("Type").toLowerCase();
                externalList.setListType("dynamique".equals(type) ? "DYNAMIC" : "STATIC");
            }

            if (configData.containsKey("Liste avancée")) {
                boolean isAdvanced = convertTextToBoolean(configData.get("Liste avancée"));
                externalList.setIsAdvanced(isAdvanced);
            }

            if (configData.containsKey("Filtrée")) {
                boolean isFiltered = convertTextToBoolean(configData.get("Filtrée"));
                externalList.setIsFiltered(isFiltered);
            }

            if (configData.containsKey("Rubrique") && !"-".equals(configData.get("Rubrique"))) {
                externalList.setRubrique(configData.get("Rubrique"));
            }
        }

        externalList.setCreatedAt(LocalDateTime.now());
        externalList.setUpdatedAt(LocalDateTime.now());

        Utilisateur user = utilisateurRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable"));
        externalList.setUtilisateur(user);

        return externalListRepository.save(externalList);
    }
    /**
     * Importe les données depuis le fichier Excel vers la liste externe
     */
// Dans ExcelImportService.java - Remplacer la méthode importDataFromExcel

    /**
     * Importe les données depuis le fichier Excel vers la liste externe
     */
    /**
     * Alternative approach - imports each non-empty cell in data rows as separate items
     */
    /**
     * Version avec debug complet pour diagnostiquer le problème
     */
    /**
     * Solution simple pour votre structure Excel spécifique
     * Ligne 4: en-têtes (# | Libellé | Valeur | Statut)
     * Lignes 5+: données
     */
    /**
     * Solution corrigée qui gère correctement la structure Excel à sections multiples
     */
    /**
     * Solution corrigée pour importer TOUS les éléments du fichier Excel
     */
    private int importDataFromExcel(MultipartFile file, ExternalList externalList) throws IOException {
        int importedCount = 0;

        try (Workbook workbook = createWorkbook(file)) {
            Sheet sheet = workbook.getSheetAt(0);

            // CORRECTION PRINCIPALE : Utiliser getLastRowNum() + 1
            int totalRows = sheet.getLastRowNum() + 1;

            System.out.println("=== IMPORT EXCEL CORRIGÉ ===");
            System.out.println("Nombre total de lignes (getLastRowNum + 1): " + totalRows);
            System.out.println("Nombre de lignes physiques (getPhysicalNumberOfRows): " + sheet.getPhysicalNumberOfRows());

            // Chercher la ligne qui contient "Libellé" dans la colonne B
            int dataStartRow = -1;
            for (int i = 0; i < totalRows; i++) {
                Row row = sheet.getRow(i);
                if (row != null && row.getPhysicalNumberOfCells() > 1) {
                    Cell cellB = row.getCell(1); // Colonne B
                    String value = getCellValueAsString(cellB);
                    if ("Libellé".equals(value)) {
                        dataStartRow = i + 1; // Les données commencent à la ligne suivante
                        System.out.println("En-têtes trouvés ligne " + i + ", données commencent ligne " + dataStartRow);
                        break;
                    }
                }
            }

            if (dataStartRow == -1) {
                System.out.println("Structure de données non reconnue");
                return 0;
            }

            // Traiter TOUTES les lignes de données - CORRECTION ICI
            for (int i = dataStartRow; i < totalRows; i++) {
                Row row = sheet.getRow(i);

                // Amélioration : Vérifier aussi les lignes nulles mais continuer le traitement
                if (row == null) {
                    System.out.println("Ligne " + i + " null, mais on continue...");
                    continue;
                }

                System.out.println("Traitement ligne " + i + " avec " + row.getPhysicalNumberOfCells() + " cellules");

                // Vérifier si la ligne a des données suffisantes
                if (row.getPhysicalNumberOfCells() < 2) {
                    System.out.println("Ligne " + i + " insuffisante (" + row.getPhysicalNumberOfCells() + " cellules), mais on continue...");
                    continue;
                }

                // Extraire le libellé (colonne B, index 1)
                Cell libelleCell = row.getCell(1);
                String libelle = getCellValueAsString(libelleCell);

                System.out.println("Ligne " + i + ": cellule B = '" + libelle + "'");

                // CORRECTION PRINCIPALE : Vérifier mieux si le libellé est valide
                if (libelle == null || libelle.trim().isEmpty() ||
                        libelle.equals("#") || libelle.equalsIgnoreCase("Libellé")) {
                    System.out.println("Ligne " + i + " sans libellé valide ('" + libelle + "'), mais on continue...");
                    continue;
                }

                System.out.println("Traitement élément: libellé = '" + libelle + "'");

                // Créer l'élément
                ExternalListItem item = new ExternalListItem();
                item.setLabel(libelle.trim());

                // Valeur (colonne C, index 2)
                String valeur = libelle.toLowerCase().replaceAll("[^a-zA-Z0-9]", "_");
                if (row.getPhysicalNumberOfCells() > 2) {
                    Cell valeurCell = row.getCell(2);
                    String cellValeur = getCellValueAsString(valeurCell);
                    if (cellValeur != null && !cellValeur.trim().isEmpty()) {
                        valeur = cellValeur.trim();
                    }
                }
                item.setValue(valeur);

                // Statut (colonne D, index 3)
                boolean isActive = true;
                if (row.getPhysicalNumberOfCells() > 3) {
                    Cell statutCell = row.getCell(3);
                    String statut = getCellValueAsString(statutCell);
                    if (statut != null && "Inactif".equalsIgnoreCase(statut.trim())) {
                        isActive = false;
                    }
                }
                item.setIsActive(isActive);

                item.setDisplayOrder(importedCount);
                item.setExternalList(externalList);

                // Sauvegarder
                try {
                    externalListItemRepository.save(item);
                    importedCount++;
                    System.out.println("✓ Élément " + importedCount + " sauvé: " + item.getLabel() + " -> " + item.getValue());
                } catch (Exception e) {
                    System.out.println("✗ Erreur sauvegarde élément '" + libelle + "': " + e.getMessage());
                    // Continuer même en cas d'erreur sur un élément
                }
            }
        }

        System.out.println("=== FIN IMPORT - Total importé: " + importedCount + " éléments ===");
        return importedCount;
    }

    private ExternalListItem createItemFromCell(String cellValue, ExternalList externalList, int displayOrder) {
        if (cellValue == null || cellValue.trim().isEmpty()) {
            return null;
        }

        ExternalListItem item = new ExternalListItem();
        item.setLabel(cellValue.trim());
        item.setValue(cellValue.toLowerCase().replaceAll("[^a-zA-Z0-9]", "_"));
        item.setDisplayOrder(displayOrder);
        item.setIsActive(true);
        item.setExternalList(externalList);

        return item;
    }

    /**
     * Checks if a value looks like configuration data that should be ignored
     */
    private boolean isConfigurationValue(String value) {
        String[] configPatterns = {
                "nom de la liste", "rubrique", "type", "liste avancée", "filtrée",
                "propriétaire", "libellé", "label", "valeur", "value", "statut", "status"
        };

        String lowerValue = value.toLowerCase();
        for (String pattern : configPatterns) {
            if (lowerValue.contains(pattern)) {
                return true;
            }
        }

        return false;
    }    private int importSimpleFormat(Sheet sheet, ExternalList externalList) {
        int importedCount = 0;
        Row headerRow = sheet.getRow(0);
        List<String> headers = extractHeaders(headerRow);

        // Commencer à partir de la ligne 1 (après les en-têtes)
        for (int i = 1; i < sheet.getPhysicalNumberOfRows(); i++) {
            Row row = sheet.getRow(i);

            if (row == null || isRowEmpty(row)) {
                continue;
            }

            ExternalListItem item = createSimpleListItem(row, headers, externalList, importedCount);
            if (item != null) {
                externalListItemRepository.save(item);
                importedCount++;
                System.out.println("Élément simple importé: " + item.getLabel());
            }
        }

        return importedCount;
    }

    /**
     * Crée un élément simple à partir d'une ligne
     */
    private ExternalListItem createSimpleListItem(Row row, List<String> headers,
                                                  ExternalList externalList, int displayOrder) {
        // Pour le format simple, utiliser la première colonne comme label
        Cell firstCell = row.getCell(0);
        String label = getCellValueAsString(firstCell);

        if (label == null || label.trim().isEmpty()) {
            return null;
        }

        ExternalListItem item = new ExternalListItem();
        item.setLabel(label.trim());
        item.setValue(label.toLowerCase().replaceAll("[^a-zA-Z0-9]", "_"));
        item.setDisplayOrder(displayOrder);
        item.setIsActive(true);
        item.setExternalList(externalList);

        return item;
    }

    /**
     * Crée un élément de liste à partir de la section des éléments
     */
    private ExternalListItem createListItemFromElementsSection(Row row, List<String> headers,
                                                               ExternalList externalList, int displayOrder) {
        System.out.println("Création d'un élément à partir de la ligne avec " + row.getPhysicalNumberOfCells() + " cellules");

        // Chercher les colonnes importantes
        int labelColumnIndex = findColumnIndex(headers, new String[]{"Libellé", "Label", "Nom"});
        int valueColumnIndex = findColumnIndex(headers, new String[]{"Valeur", "Value", "Code"});
        int statusColumnIndex = findColumnIndex(headers, new String[]{"Statut", "Status", "Actif"});

        System.out.println("Indices des colonnes - Label: " + labelColumnIndex + ", Value: " + valueColumnIndex + ", Status: " + statusColumnIndex);

        // Si pas de colonne Libellé trouvée, chercher la première colonne non-numérique
        if (labelColumnIndex == -1) {
            labelColumnIndex = findFirstTextColumn(row);
            System.out.println("Utilisation de la colonne texte: " + labelColumnIndex);
        }

        if (labelColumnIndex == -1 || labelColumnIndex >= row.getPhysicalNumberOfCells()) {
            System.out.println("Aucune colonne de libellé trouvée");
            return null;
        }

        Cell labelCell = row.getCell(labelColumnIndex);
        String label = getCellValueAsString(labelCell);
        System.out.println("Label extrait: '" + label + "'");

        if (label == null || label.trim().isEmpty()) {
            System.out.println("Label vide, élément ignoré");
            return null;
        }

        ExternalListItem item = new ExternalListItem();
        item.setLabel(label.trim());

        // Définir la valeur
        String value = label.toLowerCase().replaceAll("[^a-zA-Z0-9]", "_");
        if (valueColumnIndex >= 0 && valueColumnIndex < row.getPhysicalNumberOfCells()) {
            Cell valueCell = row.getCell(valueColumnIndex);
            String cellValue = getCellValueAsString(valueCell);
            if (cellValue != null && !cellValue.trim().isEmpty()) {
                value = cellValue.trim();
            }
        }
        item.setValue(value);
        System.out.println("Valeur définie: '" + value + "'");

        // Définir le statut
        boolean isActive = true;
        if (statusColumnIndex >= 0 && statusColumnIndex < row.getPhysicalNumberOfCells()) {
            Cell statusCell = row.getCell(statusColumnIndex);
            String statusValue = getCellValueAsString(statusCell);
            if (statusValue != null) {
                isActive = !"Inactif".equalsIgnoreCase(statusValue.trim()) &&
                        !"Inactive".equalsIgnoreCase(statusValue.trim()) &&
                        !"Non".equalsIgnoreCase(statusValue.trim()) &&
                        !"False".equalsIgnoreCase(statusValue.trim()) &&
                        !"0".equals(statusValue.trim());
            }
        }
        item.setIsActive(isActive);

        item.setDisplayOrder(displayOrder);
        item.setExternalList(externalList);

        System.out.println("Élément créé: " + item.getLabel() + " -> " + item.getValue());
        return item;
    }

    /**
     * Trouve la première colonne contenant du texte (non numérique uniquement)
     */
    private int findFirstTextColumn(Row row) {
        for (int i = 0; i < row.getPhysicalNumberOfCells(); i++) {
            Cell cell = row.getCell(i);
            String value = getCellValueAsString(cell);
            if (value != null && !value.trim().isEmpty() && !isNumericOnly(value.trim())) {
                return i;
            }
        }
        return -1;
    }
    /**
     * Trouve la ligne où commencent les données des éléments
     * Cherche une ligne qui contient "#" ou "Libellé" comme premiers éléments
     */
    private int findItemsDataStartRow(Sheet sheet) {
        for (int i = 0; i < sheet.getPhysicalNumberOfRows(); i++) {
            Row row = sheet.getRow(i);
            if (row == null) continue;

            // Vérifier plusieurs cellules de la ligne pour détecter les en-têtes d'éléments
            boolean foundItemHeader = false;

            // Chercher dans les 3 premières cellules
            for (int j = 0; j < Math.min(3, row.getPhysicalNumberOfCells()); j++) {
                Cell cell = row.getCell(j);
                String cellValue = getCellValueAsString(cell);

                if (cellValue != null) {
                    String normalizedValue = cellValue.trim();
                    // Chercher les en-têtes typiques des éléments
                    if ("#".equals(normalizedValue) ||
                            "Libellé".equalsIgnoreCase(normalizedValue) ||
                            "Label".equalsIgnoreCase(normalizedValue) ||
                            "Ordre".equalsIgnoreCase(normalizedValue) ||
                            "Valeur".equalsIgnoreCase(normalizedValue) ||
                            "Value".equalsIgnoreCase(normalizedValue)) {
                        foundItemHeader = true;
                        break;
                    }
                }
            }

            if (foundItemHeader) {
                System.out.println("En-têtes des éléments trouvés à la ligne: " + i);
                return i;
            }
        }

        System.out.println("Aucune section d'éléments trouvée");
        return -1;
    }
    private List<Integer> findLabelColumns(List<String> headers) {
        List<Integer> labelColumns = new ArrayList<>();
        for (int i = 0; i < headers.size(); i++) {
            String header = headers.get(i).toLowerCase();
            if (header.contains("libellé") || header.contains("label")) {
                labelColumns.add(i);
            }
        }
        return labelColumns;
    }

    private List<ExternalListItem> createListItemsFromRow(Row row, List<String> headers,
                                                          ExternalList externalList, int startDisplayOrder) {
        List<ExternalListItem> items = new ArrayList<>();
        List<Integer> labelColumns = findLabelColumns(headers);

        int currentDisplayOrder = startDisplayOrder;
        for (int colIndex : labelColumns) {
            if (colIndex < row.getPhysicalNumberOfCells()) { // Check bounds
                Cell cell = row.getCell(colIndex);
                String label = getCellValueAsString(cell);
                if (label != null && !label.trim().isEmpty()) {
                    ExternalListItem item = new ExternalListItem();
                    item.setLabel(label.trim());

                    // Create a unique value for each item
                    String value = label.toLowerCase().replaceAll("[^a-zA-Z0-9]", "_");
                    String finalValue = value;
                    if (items.stream().anyMatch(existingItem -> existingItem.getValue().equals(finalValue))) {
                        value = value + "_" + (items.size() + 1);
                    }
                    item.setValue(value);

                    item.setIsActive(true);
                    item.setDisplayOrder(currentDisplayOrder); // This will be overridden in the calling method
                    item.setExternalList(externalList);
                    items.add(item);

                    System.out.println("Élément créé dans la ligne: " + item.getLabel() + " -> " + item.getValue());
                }
            }
        }

        return items;
    }
    /**
     * Vérifie si une ligne est une ligne de configuration (à ignorer)
     */
    private boolean isConfigurationRow(Row row, List<String> itemHeaders) {
        Cell firstCell = row.getCell(0);
        String firstValue = getCellValueAsString(firstCell);

        if (firstValue == null) return false;

        // Ignorer les lignes qui ressemblent à des configurations
        String[] configKeywords = {
                "Nom de la liste", "Rubrique", "Type", "Liste avancée",
                "Filtrée", "Propriétaire", "#", "Libellé", "Label"
        };

        for (String keyword : configKeywords) {
            if (firstValue.contains(keyword)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Crée un élément de liste à partir de la section des éléments
     */

    /**
     * Trouve l'index d'une colonne par ses noms possibles
     */
    private int findColumnIndex(List<String> headers, String[] possibleNames) {
        for (int i = 0; i < headers.size(); i++) {
            String header = headers.get(i);
            for (String name : possibleNames) {
                if (name.equalsIgnoreCase(header)) {
                    return i;
                }
            }
        }
        return -1;
    }

    /**
     * Trouve la première colonne qui ne contient pas que des chiffres
     */
    private int findFirstNonNumericColumn(Row row) {
        for (int i = 0; i < row.getPhysicalNumberOfCells(); i++) {
            Cell cell = row.getCell(i);
            String value = getCellValueAsString(cell);
            if (value != null && !isNumericOnly(value.trim())) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Vérifie si une chaîne ne contient que des chiffres
     */
    private boolean isNumericOnly(String str) {
        if (str == null || str.isEmpty()) return false;
        return str.matches("\\d+");
    }
    /**
     * Crée un élément de liste à partir d'une ligne Excel
     */
    private ExternalListItem createListItem(Row row, List<String> headers,
                                            ExternalList externalList, int displayOrder) {
        // Utiliser la première colonne comme label et value par défaut
        Cell firstCell = row.getCell(0);
        String label = getCellValueAsString(firstCell);

        if (label == null || label.trim().isEmpty()) {
            return null; // Ignorer les lignes sans label
        }

        ExternalListItem item = new ExternalListItem();
        item.setLabel(label.trim());

        // Si il y a une deuxième colonne, l'utiliser comme value, sinon utiliser le label
        String value = label.toLowerCase().replaceAll("[^a-zA-Z0-9]", "_");
        if (headers.size() > 1 && row.getPhysicalNumberOfCells() > 1) {
            Cell secondCell = row.getCell(1);
            String secondValue = getCellValueAsString(secondCell);
            if (secondValue != null && !secondValue.trim().isEmpty()) {
                value = secondValue.trim();
            }
        }

        item.setValue(value);
        item.setDisplayOrder(displayOrder);
        item.setIsActive(true);
        item.setExternalList(externalList);

        // Ajouter des données supplémentaires si il y a plus de 2 colonnes
        if (headers.size() > 2) {
            Map<String, Object> extraData = new HashMap<>();
            for (int j = 2; j < Math.min(headers.size(), row.getPhysicalNumberOfCells()); j++) {
                Cell cell = row.getCell(j);
                String cellValue = getCellValueAsString(cell);
                if (cellValue != null && !cellValue.trim().isEmpty()) {
                    extraData.put(headers.get(j), cellValue);
                }
            }

            if (!extraData.isEmpty()) {
                // Convertir en JSON simple
                StringBuilder jsonBuilder = new StringBuilder("{");
                extraData.forEach((key, val) -> {
                    if (jsonBuilder.length() > 1) jsonBuilder.append(",");
                    jsonBuilder.append("\"").append(key).append("\":\"").append(val).append("\"");
                });
                jsonBuilder.append("}");
                item.setExtraData(jsonBuilder.toString());
            }
        }

        return item;
    }
}