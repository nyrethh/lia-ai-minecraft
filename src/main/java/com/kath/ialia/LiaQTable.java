package com.kath.ialia;

/* Q-Table */

import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public class LiaQTable {

    /* Estado > Acción > Valor */
    private final Map<String, Map<LiaDecision.Action, Double>> table =
            new HashMap<>();

    /* Archivo donde se guarda el aprendizaje */
    private final Path saveFile =
            FabricLoader.getInstance()
                    .getConfigDir()
                    .resolve("ia_lia_qtable.txt");

    public LiaQTable() {

        // Carga el aprendizaje guardado
        load();
    }

    /* Busca el valor de una acción */
    public double getQValue(
            String state,
            LiaDecision.Action action
    ) {

        Map<LiaDecision.Action, Double> actions =
                table.computeIfAbsent(
                        state,
                        key -> new HashMap<>()
                );

        return actions.getOrDefault(action, 0.0);
    }

    public boolean hasState(String state) {
        return table.containsKey(state);
    }

    /* Cambia el valor de una acción */
    public void setQValue(
            String state,
            LiaDecision.Action action,
            double value
    ) {

        Map<LiaDecision.Action, Double> actions =
                table.computeIfAbsent(
                        state,
                        key -> new HashMap<>()
                );

        actions.put(action, value);
    }

    /* Busca el valor más alto */
    public double getMaxQValue(String state) {

        double maxValue = Double.NEGATIVE_INFINITY;

        for (LiaDecision.Action action :
                LiaDecision.Action.values()) {

            double value =
                    getQValue(state, action);

            if (value > maxValue) {
                maxValue = value;
            }
        }

        return maxValue;
    }

    /* Busca la mejor acción */
    public LiaDecision.Action getBestAction(String state) {

        LiaDecision.Action bestAction = null;

        double bestValue =
                Double.NEGATIVE_INFINITY;

        for (LiaDecision.Action action :
                LiaDecision.Action.values()) {

            double value =
                    getQValue(state, action);

            if (value > bestValue) {

                bestValue = value;
                bestAction = action;
            }
        }

        return bestAction;
    }

    /* Guarda la Q-Table */
    public void save() {

        try {

            StringBuilder content =
                    new StringBuilder();

            for (Map.Entry<String, Map<LiaDecision.Action, Double>> stateEntry
                    : table.entrySet()) {

                String state =
                        stateEntry.getKey();

                for (Map.Entry<LiaDecision.Action, Double> actionEntry
                        : stateEntry.getValue().entrySet()) {

                    LiaDecision.Action action =
                            actionEntry.getKey();

                    double value =
                            actionEntry.getValue();

                    content.append(state)
                            .append("|")
                            .append(action.name())
                            .append("|")
                            .append(value)
                            .append("\n");
                }
            }

            Files.writeString(
                    saveFile,
                    content.toString(),
                    StandardCharsets.UTF_8
            );

            Ia_lia.LOGGER.info(
                    "LIA guardó su aprendizaje en: {}",
                    saveFile
            );

        } catch (IOException e) {

            Ia_lia.LOGGER.error(
                    "No se pudo guardar la Q-Table de LIA.",
                    e
            );
        }
    }

    /* Carga la Q-Table */
    private void load() {

        if (!Files.exists(saveFile)) {

            Ia_lia.LOGGER.info(
                    "LIA no tiene aprendizaje guardado."
            );

            return;
        }

        try {

            for (String line :
                    Files.readAllLines(
                            saveFile,
                            StandardCharsets.UTF_8
                    )) {

                if (line.isBlank()) {
                    continue;
                }

                String[] parts =
                        line.split("\\|");

                if (parts.length != 3) {
                    continue;
                }

                String state =
                        parts[0];

                LiaDecision.Action action =
                        LiaDecision.Action.valueOf(parts[1]);

                double value =
                        Double.parseDouble(parts[2]);

                setQValue(
                        state,
                        action,
                        value
                );
            }

            Ia_lia.LOGGER.info(
                    "LIA cargó su aprendizaje anterior."
            );

        } catch (Exception e) {

            Ia_lia.LOGGER.error(
                    "No se pudo cargar la Q-Table de LIA.",
                    e
            );
        }
    }
}