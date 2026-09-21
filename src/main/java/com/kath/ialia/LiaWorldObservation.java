package com.kath.ialia;

import java.util.List;

public class LiaWorldObservation {

    private final List<String> blocks;
    private final List<String> entities;

    public LiaWorldObservation(
            List<String> blocks,
            List<String> entities
    ) {
        this.blocks = blocks;
        this.entities = entities;
    }

    public List<String> getBlocks() {
        return blocks;
    }

    public List<String> getEntities() {
        return entities;
    }
}