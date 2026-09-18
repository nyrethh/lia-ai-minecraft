package com.kath.ialia;



/*percepcion de lia con su entorno  observar el mundo */

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;

import java.util.List;

public class LiaPerception {

    public static List<Entity> getNearbyEntities(LiaEntity lia, double distance) {
        return lia.level().getEntities(
                lia,
                lia.getBoundingBox().inflate(distance),
                entity -> entity != lia
        );
    }

    public static String identifyEntity(Entity entity) {

        if (entity instanceof Player) { // identifica a un jugador
            return "jugador";
        }

        if (entity instanceof Monster) { // indentifica a un mob malo x
            return "monstruo";
        }

        if (entity.getType().getCategory().isFriendly()) { //y aqui un animal 
            return "animal";
        }

        return "otra entidad";
    }


    public static LiaState getCurrentState(LiaEntity lia, double distance) { 


        //empieza el estado en falso
    boolean playerNearby = false;
    boolean animalNearby = false;
    boolean monsterNearby = false;

    List<Entity> entities = getNearbyEntities(lia, distance);

    for (Entity entity : entities) {

        String type = identifyEntity(entity);

        switch (type) {

            case "jugador":
                playerNearby = true;
                break;

            case "animal":
                animalNearby = true;
                break;

            case "monstruo":
                monsterNearby = true;
                break;
        }
    }

    return new LiaState(
            playerNearby,
            animalNearby,
            monsterNearby
    );
}

    public static double getNearestMonsterDistance(LiaEntity lia, double distance) {

        Entity nearestMonster = lia.level().getEntities(
                        lia,
                        lia.getBoundingBox().inflate(distance),
                        entity -> entity instanceof Monster
                ).stream()
                .min((entity1, entity2) ->
                        Double.compare(
                                lia.distanceTo(entity1),
                                lia.distanceTo(entity2)
                        )
                )
                .orElse(null);

        if (nearestMonster == null) {
            return -1;
        }

        return lia.distanceTo(nearestMonster);
    }









}