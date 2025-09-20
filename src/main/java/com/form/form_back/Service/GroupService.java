    package com.form.form_back.Service;

    import com.form.form_back.Entity.Group;
    import com.form.form_back.Entity.Utilisateur;
    import com.form.form_back.Repo.GroupRepository;
    import com.form.form_back.Repo.UtilisateurRepository;
    import com.form.form_back.dto.GroupDTO;
    import jakarta.annotation.PostConstruct;
    import org.slf4j.Logger;
    import org.slf4j.LoggerFactory;
    import org.springframework.beans.factory.annotation.Autowired;
    import org.springframework.boot.ApplicationArguments;
    import org.springframework.boot.ApplicationRunner;
    import org.springframework.stereotype.Service;
    import org.springframework.transaction.annotation.Transactional;

    import java.util.*;
    import java.util.stream.Collectors;

    @Service
    @Transactional
    public class GroupService implements ApplicationRunner {

        @Autowired
        private GroupRepository groupRepository;

        @Autowired
        private UtilisateurRepository userRepository;

        private static final Logger logger = LoggerFactory.getLogger(GroupService.class);

        // ✅ LES 7 GROUPES D&S
        private static final List<String> DS_GROUPS = Arrays.asList(
                "AQMARIS",
                "Alfadir",
                "SafetyShop",
                "KairosFormations",
                "FilDem",
                "DSIngenierie",
                "Quadance"
        );
        @Override
        public void run(ApplicationArguments args) {
            initializeGroups();
        }
        /**
         * Initialise les groupes D&S au démarrage de l'application
         */
        public void initializeGroups() {
            logger.info("Initialisation des groupes D&S...");

            Map<String, String> groupDescriptions = Map.of(
                    "AQMARIS", "Spécialisé dans les solutions aquatiques et marines",
                    "Alfadir", "Services de consulting et conseil stratégique",
                    "SafetyShop", "Solutions de sécurité et équipements de protection",
                    "KairosFormations", "Organisme de formation professionnelle",
                    "FilDem", "Services de développement et ingénierie",
                    "DSIngenierie", "Bureau d'études et ingénierie technique",
                    "Quadance", "Solutions digitales et transformation numérique"
            );

            Map<String, String> groupColors = Map.of(
                    "AQMARIS", "#1976D2",
                    "Alfadir", "#388E3C",
                    "SafetyShop", "#F57C00",
                    "KairosFormations", "#7B1FA2",
                    "FilDem", "#D32F2F",
                    "DSIngenierie", "#303F9F",
                    "Quadance", "#0288D1"
            );

            for (String groupName : DS_GROUPS) {
                if (!groupRepository.findByName(groupName).isPresent()) {
                    Group group = new Group();
                    group.setName(groupName);
                    group.setDescription(groupDescriptions.getOrDefault(groupName,
                            "Groupe " + groupName + " du groupe D&S"));
                    group.setColor(groupColors.getOrDefault(groupName, "#424242"));
                    group.setActive(true);

                    groupRepository.save(group);
                    logger.info("Groupe créé: {}", groupName);
                }
            }

            logger.info("Initialisation des groupes terminée.");
        }

        /**
         * Assigne automatiquement un utilisateur à un groupe selon la rotation
         */
        public Group assignUserToNextAvailableGroup(Utilisateur user) {
            List<Group> availableGroups = groupRepository.findByActiveTrue();

            if (availableGroups.isEmpty()) {
                logger.warn("Aucun groupe disponible pour assigner l'utilisateur {}", user.getUsername());
                return null;
            }

            // ✅ STRATÉGIE: Trouver le groupe avec le moins d'utilisateurs
            Group selectedGroup = availableGroups.stream()
                    .min((g1, g2) -> {
                        Long count1 = groupRepository.countUsersByGroup(g1);
                        Long count2 = groupRepository.countUsersByGroup(g2);
                        return count1.compareTo(count2);
                    })
                    .orElse(availableGroups.get(0));

            // Assigner le groupe à l'utilisateur
            user.setAssignedGroup(selectedGroup);
            user.addGroup(selectedGroup);

            logger.info("Utilisateur {} assigné au groupe {}",
                    user.getUsername(), selectedGroup.getName());

            return selectedGroup;
        }

        /**
         * Obtient le prochain groupe dans la rotation (alternative)
         */
        public Group getNextGroupInRotation() {
            List<Group> groups = groupRepository.findAllOrderedById();

            if (groups.isEmpty()) {
                return null;
            }

            // Compter les utilisateurs dans chaque groupe et prendre celui avec le moins
            return groups.stream()
                    .min(Comparator.comparing(g -> groupRepository.countUsersByGroup(g)))
                    .orElse(groups.get(0));
        }

        public List<GroupDTO> getAllGroups() {
            return groupRepository.findAll()
                    .stream()
                    .map(GroupDTO::new) // ← utilise le constructeur GroupDTO(Group)
                    .collect(Collectors.toList());
        }

        public List<GroupDTO> getActiveGroups() {
            return groupRepository.findByActiveTrue()
                    .stream()
                    .map(GroupDTO::new) // ← idem ici
                    .collect(Collectors.toList());
        }

        public Optional<Group> getGroupById(Long id) {
            return groupRepository.findById(id);
        }

        public Optional<Group> getGroupByName(String name) {
            return groupRepository.findByName(name);
        }

        public Group saveGroup(Group group) {
            return groupRepository.save(group);
        }

        public void deleteGroup(Long id) {
            groupRepository.deleteById(id);
        }

        /**
         * Obtient les statistiques des groupes

        public Map<String, Long> getGroupStatistics() {
            List<Group> groups = getAllGroups();
            Map<String, Long> statistics = new HashMap<>();

            for (Group group : groups) {
                Long userCount = groupRepository.countUsersByGroup(group);
                statistics.put(group.getName(), userCount);
            }

            return statistics;
        }*/
    }
