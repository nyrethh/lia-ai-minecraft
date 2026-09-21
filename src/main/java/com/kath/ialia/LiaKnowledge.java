package com.kath.ialia;

import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class LiaKnowledge {

    private final Set<String> knownBlocks = new HashSet<>();
    private final Set<String> knownEntities = new HashSet<>();

    private final List<LiaExperience> experiences =
            new ArrayList<>();

    // Archivo donde se guarda el conocimiento de LIA.
    private final Path saveFile =
            FabricLoader.getInstance()
                    .getConfigDir()
                    .resolve("ia_lia_knowledge.txt");

    public LiaKnowledge() {

        // Carga el conocimiento anterior.
        load();
    }

    public void learnBlock(String block) {

        if (knownBlocks.add(block)) {

            Ia_lia.LOGGER.info(
                    "LIA aprendio un bloque nuevo: {}",
                    block
            );

            save();
        }
    }

    public void learnEntity(String entity) {

        if (knownEntities.add(entity)) {

            Ia_lia.LOGGER.info(
                    "LIA aprendio una entidad nueva: {}",
                    entity
            );

            save();
        }
    }

    public void rememberExperience(
            String entity,
            LiaDecision.Action action,
            boolean tookDamage,
            double distance,
            double reward
    ) {

        // Si no hay una entidad relacionada, no guardamos la experiencia.
        if (entity.equals("none")) {
            return;
        }

        // Evita guardar la misma experiencia muchas veces.
        for (LiaExperience experience : experiences) {

            if (experience.getEntity().equals(entity)
                    && experience.getAction() == action
                    && experience.tookDamage() == tookDamage
                    && experience.getReward() == reward) {

                return;
            }
        }

        LiaExperience experience =
                new LiaExperience(
                        entity,
                        action,
                        tookDamage,
                        distance,
                        reward
                );

        experiences.add(experience);

        Ia_lia.LOGGER.info(
                "LIA recuerda experiencia: {}",
                experience
        );

        save();
    }

    public boolean knowsBlock(String block) {
        return knownBlocks.contains(block);
    }

    public boolean knowsEntity(String entity) {
        return knownEntities.contains(entity);
    }

    public Set<String> getKnownBlocks() {
        return knownBlocks;
    }

    public Set<String> getKnownEntities() {
        return knownEntities;
    }

    public List<LiaExperience> getExperiences() {
        return experiences;
    }

    // Guarda el conocimiento de LIA.
    private void save() {

        try {

            StringBuilder content =
                    new StringBuilder();

            content.append("[BLOCKS]\n");

            for (String block : knownBlocks) {
                content.append(block).append("\n");
            }

            content.append("[ENTITIES]\n");

            for (String entity : knownEntities) {
                content.append(entity).append("\n");
            }

            content.append("[EXPERIENCES]\n");

            for (LiaExperience experience : experiences) {

                content.append(
                                experience.getEntity()
                        )
                        .append("|")
                        .append(
                                experience.getAction().name()
                        )
                        .append("|")
                        .append(
                                experience.tookDamage()
                        )
                        .append("|")
                        .append(
                                experience.getDistance()
                        )
                        .append("|")
                        .append(
                                experience.getReward()
                        )
                        .append("\n");
            }

            Files.writeString(
                    saveFile,
                    content.toString(),
                    StandardCharsets.UTF_8
            );

        } catch (IOException e) {

            Ia_lia.LOGGER.error(
                    "No se pudo guardar el conocimiento de LIA.",
                    e
            );
        }
    }

    // Carga el conocimiento guardado.
    private void load() {

        if (!Files.exists(saveFile)) {

            Ia_lia.LOGGER.info(
                    "LIA no tiene conocimiento guardado."
            );

            return;
        }

        try {

            boolean readingBlocks = false;
            boolean readingEntities = false;
            boolean readingExperiences = false;

            for (String line :
                    Files.readAllLines(
                            saveFile,
                            StandardCharsets.UTF_8
                    )) {

                if (line.isBlank()) {
                    continue;
                }

                if (line.equals("[BLOCKS]")) {

                    readingBlocks = true;
                    readingEntities = false;
                    readingExperiences = false;

                    continue;
                }

                if (line.equals("[ENTITIES]")) {

                    readingBlocks = false;
                    readingEntities = true;
                    readingExperiences = false;

                    continue;
                }

                if (line.equals("[EXPERIENCES]")) {

                    readingBlocks = false;
                    readingEntities = false;
                    readingExperiences = true;

                    continue;
                }

                if (readingBlocks) {

                    knownBlocks.add(line);

                    continue;
                }

                if (readingEntities) {

                    knownEntities.add(line);

                    continue;
                }

                if (readingExperiences) {

                    String[] parts =
                            line.split("\\|");

                    if (parts.length != 5) {
                        continue;
                    }

                    String entity =
                            parts[0];

                    LiaDecision.Action action =
                            LiaDecision.Action.valueOf(
                                    parts[1]
                            );

                    boolean tookDamage =
                            Boolean.parseBoolean(
                                    parts[2]
                            );

                    double distance =
                            Double.parseDouble(
                                    parts[3]
                            );

                    double reward =
                            Double.parseDouble(
                                    parts[4]
                            );

                    experiences.add(
                            new LiaExperience(
                                    entity,
                                    action,
                                    tookDamage,
                                    distance,
                                    reward
                            )
                    );
                }
            }

            Ia_lia.LOGGER.info(
                    "LIA cargo su conocimiento anterior."
            );

        } catch (Exception e) {

            Ia_lia.LOGGER.error(
                    "No se pudo cargar el conocimiento de LIA.",
                    e
            );
        }
    }
}