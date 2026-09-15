package com.kath.ialia;

import net.minecraft.world.entity.Entity;

import java.util.ArrayList;
import java.util.List;

public class LiaMemory {

    private final List<String> memories = new ArrayList<>();

    public void remember(Entity entity) {

        String tipo = LiaPerception.identifyEntity(entity);

        String memory = "Vi un " + tipo; /*lo llama */

        if (memories.size() >= 20) { /*solo va a recordar 20 cosas por ahora */
            memories.remove(0);
        }

        memories.add(memory);
        Ia_lia.LOGGER.info(
                "LIA recuerda: {}",
                memory);
    }

    public List<String> getMemories() {
        return memories;
    }
}