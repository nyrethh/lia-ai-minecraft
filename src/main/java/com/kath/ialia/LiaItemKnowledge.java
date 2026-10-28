
package com.kath.ialia;

import java.util.HashMap;
import java.util.Map;

public class LiaItemKnowledge {

    // Guarda cuánto valor ha aprendido LIA sobre cada objeto.
    private final Map<String, Double> itemValues = new HashMap<>();

    public double getValue(String itemName) {
        return itemValues.getOrDefault(itemName, 0.0);
    }

    public void learn(String itemName, double reward) {
        double currentValue = getValue(itemName);

        double newValue =
                currentValue + (0.1 * (reward - currentValue));

        itemValues.put(itemName, newValue);

        Ia_lia.LOGGER.info(
                "LIA aprende sobre {} | Valor anterior: {} | Recompensa: {} | Nuevo valor: {}",
                itemName,
                currentValue,
                reward,
                newValue
        );
    }

    public Map<String, Double> getItemValues() {
        return itemValues;
    }

    // Aprende a valorar las herramientas según el tiempo de tala.
    public void learnToolTime(String toolName, int ticks) {
        if (ticks <= 0) {
            return;
        }

        double reward = 100.0 / ticks;

        learn("tool:" + toolName, reward);

        Ia_lia.LOGGER.info(
                "LIA compara herramientas | Herramienta: {} | Tiempo: {} ticks | Recompensa: {}",
                toolName,
                ticks,
                reward
        );
    }

    public double getToolValue(String toolName) {
        return getValue("tool:" + toolName);
    }
}
