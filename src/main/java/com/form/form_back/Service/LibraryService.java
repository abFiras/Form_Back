package com.form.form_back.Service;

import com.form.form_back.Entity.Form;
import com.form.form_back.Entity.FormField;
import com.form.form_back.Entity.LibraryForm;
import com.form.form_back.Repo.FormFieldRepository;
import com.form.form_back.Repo.FormRepository;
import com.form.form_back.Repo.LibraryFormRepository;
import com.form.form_back.dto.LibraryFormDTO;

import org.apache.poi.xwpf.usermodel.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class LibraryService {

    @Autowired
    private LibraryFormRepository libraryFormRepository;

    @Autowired
    private FormRepository formRepository;

    @Autowired
    private FormFieldRepository formFieldRepository;

    /**
     * Récupère les détails d'un formulaire de la bibliothèque
     */
    public LibraryFormDTO getLibraryFormDetail(Long libraryFormId) {
        LibraryForm libraryForm = libraryFormRepository.findById(libraryFormId)
                .orElseThrow(() -> new RuntimeException("Formulaire de bibliothèque non trouvé"));

        LibraryFormDTO dto = convertToDTO(libraryForm);

        // Récupérer les champs du formulaire original
        Form originalForm = formRepository.findByIdWithFields(libraryForm.getOriginalFormId());
        if (originalForm != null && originalForm.getFields() != null) {
            // Vous pouvez ajouter les champs au DTO si nécessaire
            // Pour l'instant, nous utilisons juste le nombre de champs
        }

        return dto;
    }

    /**
     * Récupère tous les formulaires de la bibliothèque avec filtres
     */
    public List<LibraryFormDTO> getLibraryForms(String search, String origin, String language, String sortBy) {
        List<LibraryForm> forms;

        if (search != null && !search.trim().isEmpty()) {
            forms = libraryFormRepository.findByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase(
                    search.trim(), search.trim());
        } else {
            forms = libraryFormRepository.findAll();
        }

        // Appliquer les filtres
        if (origin != null && !origin.isEmpty()) {
            forms = forms.stream()
                    .filter(form -> origin.equals(form.getOrigin()))
                    .collect(Collectors.toList());
        }

        if (language != null && !language.isEmpty()) {
            forms = forms.stream()
                    .filter(form -> language.equals(form.getLanguage()))
                    .collect(Collectors.toList());
        }

        // Appliquer le tri
        switch (sortBy != null ? sortBy : "relevance") {
            case "recent":
                forms.sort((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()));
                break;
            case "updated":
                forms.sort((a, b) -> b.getUpdatedAt().compareTo(a.getUpdatedAt()));
                break;
            case "popular":
                forms.sort((a, b) -> Integer.compare(
                        b.getViewCount() + b.getDownloadCount(),
                        a.getViewCount() + a.getDownloadCount()
                ));
                break;
            default: // relevance
                if (search != null && !search.trim().isEmpty()) {
                    forms.sort((a, b) -> Integer.compare(
                            calculateRelevanceScore(b, search),
                            calculateRelevanceScore(a, search)
                    ));
                }
                break;
        }

        return forms.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Export d'un formulaire au format Word
     */
    public byte[] exportFormAsWord(Long libraryFormId) {
        LibraryForm libraryForm = libraryFormRepository.findById(libraryFormId)
                .orElseThrow(() -> new RuntimeException("Formulaire de bibliothèque non trouvé"));

        Form originalForm = formRepository.findByIdWithFields(libraryForm.getOriginalFormId());
        if (originalForm == null) {
            throw new RuntimeException("Formulaire original non trouvé");
        }

        try {
            XWPFDocument document = new XWPFDocument();

            // Titre du document
            XWPFParagraph titlePara = document.createParagraph();
            titlePara.setAlignment(ParagraphAlignment.CENTER);
            XWPFRun titleRun = titlePara.createRun();
            titleRun.setText(libraryForm.getName());
            titleRun.setBold(true);
            titleRun.setFontSize(18);

            // Description
            if (libraryForm.getDescription() != null && !libraryForm.getDescription().isEmpty()) {
                XWPFParagraph descPara = document.createParagraph();
                XWPFRun descRun = descPara.createRun();
                descRun.setText("Description: " + libraryForm.getDescription());
                descRun.setItalic(true);
            }

            // Informations du formulaire
            XWPFParagraph infoPara = document.createParagraph();
            XWPFRun infoRun = infoPara.createRun();
            infoRun.setText("Origine: " + libraryForm.getOrigin());
            infoRun.addBreak();
            infoRun.setText("Langue: " + libraryForm.getLanguage().toUpperCase());
            infoRun.addBreak();
            infoRun.setText("Créé le: " + libraryForm.getCreatedAt().format(DateTimeFormatter.ofPattern("dd/MM/yyyy à HH:mm")));
            infoRun.addBreak();
            infoRun.setText("Partagé par: " + libraryForm.getSharedBy());

            // Ligne de séparation
            document.createParagraph().createRun().addBreak();

            // Titre de la section champs
            XWPFParagraph fieldsTitlePara = document.createParagraph();
            XWPFRun fieldsTitleRun = fieldsTitlePara.createRun();
            fieldsTitleRun.setText("Champs du formulaire:");
            fieldsTitleRun.setBold(true);
            fieldsTitleRun.setFontSize(14);

            // Liste des champs
            if (originalForm.getFields() != null && !originalForm.getFields().isEmpty()) {
                for (FormField field : originalForm.getFields()) {
                    XWPFParagraph fieldPara = document.createParagraph();
                    XWPFRun fieldRun = fieldPara.createRun();

                    String fieldText = String.format("• %s (%s)",
                            field.getLabel(),
                            getFieldTypeLabel(field.getType()));

                    if (field.getRequired()) {
                        fieldText += " - Obligatoire";
                    }

                    fieldRun.setText(fieldText);

                    // Ajouter les options si applicable
                    if (field.getOptions() != null && !field.getOptions().isEmpty()) {
                        fieldRun.addBreak();
                        fieldRun.setText("  Options: " + String.join(", ", field.getOptions()));
                        fieldRun.setColor("666666");
                    }
                }
            } else {
                XWPFParagraph noFieldsPara = document.createParagraph();
                XWPFRun noFieldsRun = noFieldsPara.createRun();
                noFieldsRun.setText("Aucun champ disponible pour l'aperçu.");
                noFieldsRun.setItalic(true);
            }

            // Pied de page
            document.createParagraph().createRun().addBreak();
            XWPFParagraph footerPara = document.createParagraph();
            footerPara.setAlignment(ParagraphAlignment.CENTER);
            XWPFRun footerRun = footerPara.createRun();
            footerRun.setText("Document généré depuis la bibliothèque de formulaires");
            footerRun.setFontSize(10);
            footerRun.setColor("999999");

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            document.write(out);
            document.close();

            return out.toByteArray();

        } catch (IOException e) {
            throw new RuntimeException("Erreur lors de la génération du document Word", e);
        }
    }

    /**
     * Export d'un formulaire au format Excel
     */
    public byte[] exportFormAsExcel(Long libraryFormId) {
        // Implémentation Excel similaire
        throw new RuntimeException("Export Excel non encore implémenté");
    }

    /**
     * Incrémente le compteur de téléchargements
     */
    public void incrementDownloadCount(Long libraryFormId) {
        LibraryForm form = libraryFormRepository.findById(libraryFormId)
                .orElseThrow(() -> new RuntimeException("Formulaire de bibliothèque non trouvé"));

        form.setDownloadCount(form.getDownloadCount() + 1);
        libraryFormRepository.save(form);
    }

    /**
     * Calcule le score de pertinence pour le tri
     */
    private int calculateRelevanceScore(LibraryForm form, String searchTerm) {
        int score = 0;
        String term = searchTerm.toLowerCase();

        if (form.getName().toLowerCase().contains(term)) score += 10;
        if (form.getDescription().toLowerCase().contains(term)) score += 5;
        if (form.getTags() != null && form.getTags().toLowerCase().contains(term)) score += 3;

        return score;
    }

    /**
     * Ajoute un formulaire à la bibliothèque
     */
    public LibraryForm shareFormToLibrary(Long formId, String origin, String language, String tags) {
        Form originalForm = formRepository.findById(formId)
                .orElseThrow(() -> new RuntimeException("Formulaire non trouvé"));

        LibraryForm libraryForm = new LibraryForm();
        libraryForm.setOriginalFormId(formId);
        libraryForm.setName(originalForm.getName());
        libraryForm.setDescription(originalForm.getDescription());
        libraryForm.setOrigin(origin);
        libraryForm.setLanguage(language);
        libraryForm.setTags(tags);
        libraryForm.setFieldCount(originalForm.getFields() != null ? originalForm.getFields().size() : 0);
        libraryForm.setViewCount(0);
        libraryForm.setDownloadCount(0);
        libraryForm.setSharedBy("Utilisateur"); // À remplacer par l'utilisateur connecté
        libraryForm.setCreatedAt(LocalDateTime.now());
        libraryForm.setUpdatedAt(LocalDateTime.now());

        return libraryFormRepository.save(libraryForm);
    }

    /**
     * Incrémente le compteur de vues
     */
    public void incrementViewCount(Long libraryFormId) {
        LibraryForm form = libraryFormRepository.findById(libraryFormId)
                .orElseThrow(() -> new RuntimeException("Formulaire de bibliothèque non trouvé"));

        form.setViewCount(form.getViewCount() + 1);
        libraryFormRepository.save(form);
    }

    /**
     * Copie un formulaire de la bibliothèque vers le compte utilisateur
     */
    public Form addFormToAccount(Long libraryFormId) {
        LibraryForm libraryForm = libraryFormRepository.findById(libraryFormId)
                .orElseThrow(() -> new RuntimeException("Formulaire de bibliothèque non trouvé"));

        Form originalForm = formRepository.findByIdWithFields(libraryForm.getOriginalFormId());
        if (originalForm == null) {
            throw new RuntimeException("Formulaire original non trouvé");
        }

        // Créer une copie du formulaire
        Form newForm = new Form();
        newForm.setName(originalForm.getName() + " (Copie)");
        newForm.setDescription(originalForm.getDescription());
        newForm.setStatus("DRAFT");

        Form savedForm = formRepository.save(newForm);

        // Copier les champs
        if (originalForm.getFields() != null) {
            originalForm.getFields().forEach(field -> {
                FormField newField = new FormField();
                newField.setForm(savedForm);
                newField.setType(field.getType());
                newField.setLabel(field.getLabel());
                newField.setFieldName(field.getFieldName());
                newField.setPlaceholder(field.getPlaceholder());
                newField.setOrder(field.getOrder());
                newField.setRequired(field.getRequired());
                newField.setOptions(field.getOptions());
                newField.setValidation(field.getValidation());
                newField.setStyling(field.getStyling());

                formFieldRepository.save(newField);
            });
        }

        // Incrémenter le compteur de téléchargements
        incrementDownloadCount(libraryFormId);

        return formRepository.findByIdWithFields(savedForm.getId());
    }

    /**
     * Supprime un formulaire de la bibliothèque
     */
    public void removeFromLibrary(Long libraryFormId) {
        libraryFormRepository.deleteById(libraryFormId);
    }

    /**
     * Récupère les formulaires les plus populaires
     */
    public List<LibraryFormDTO> getPopularForms(int limit) {
        List<LibraryForm> forms = libraryFormRepository.findTopByOrderByViewCountDescDownloadCountDesc();

        return forms.stream()
                .limit(limit)
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Récupère les formulaires récents
     */
    public List<LibraryFormDTO> getRecentForms(int limit) {
        List<LibraryForm> forms = libraryFormRepository.findTopByOrderByCreatedAtDesc();

        return forms.stream()
                .limit(limit)
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Obtient le label d'un type de champ
     */
    private String getFieldTypeLabel(String fieldType) {
        switch (fieldType) {
            case "text": return "Texte";
            case "email": return "Email";
            case "number": return "Nombre";
            case "textarea": return "Zone de texte";
            case "select": return "Liste déroulante";
            case "radio": return "Bouton radio";
            case "checkbox": return "Case à cocher";
            case "date": return "Date";
            case "datetime": return "Date et heure";
            case "file": return "Fichier";
            case "phone": return "Téléphone";
            case "url": return "URL";
            default: return fieldType;
        }
    }

    /**
     * Convertit LibraryForm en LibraryFormDTO
     */
    private LibraryFormDTO convertToDTO(LibraryForm form) {
        LibraryFormDTO dto = new LibraryFormDTO();
        dto.setId(form.getId());
        dto.setOriginalFormId(form.getOriginalFormId());
        dto.setName(form.getName());
        dto.setDescription(form.getDescription());
        dto.setOrigin(form.getOrigin());
        dto.setLanguage(form.getLanguage());
        dto.setFieldCount(form.getFieldCount());
        dto.setViewCount(form.getViewCount());
        dto.setDownloadCount(form.getDownloadCount());
        dto.setSharedBy(form.getSharedBy());
        dto.setCreatedAt(form.getCreatedAt());
        dto.setUpdatedAt(form.getUpdatedAt());
        dto.setTags(form.getTags());
        return dto;
    }
}