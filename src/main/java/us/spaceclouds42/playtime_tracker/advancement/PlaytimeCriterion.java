package us.spaceclouds42.playtime_tracker.advancement;

import com.google.gson.JsonObject;
import net.minecraft.advancement.criterion.AbstractCriterion;
import net.minecraft.advancement.criterion.AbstractCriterionConditions;
import net.minecraft.predicate.entity.AdvancementEntityPredicateDeserializer;
import net.minecraft.predicate.entity.EntityPredicate;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

public class PlaytimeCriterion extends AbstractCriterion<PlaytimeCriterion.Conditions> {
    private static final Identifier ID = new Identifier("playtime_tracker:playtime");

    @Override
    public Identifier getId() {
        return ID;
    }

    @Override
    protected Conditions conditionsFromJson(JsonObject obj, EntityPredicate.Extended playerPredicate, AdvancementEntityPredicateDeserializer predicateDeserializer) {
        return null;
    }

    public static class Conditions extends AbstractCriterionConditions {
        public Conditions(EntityPredicate.Extended playerPredicate) {
            super(PlaytimeCriterion.ID, playerPredicate);
        }
    }
}
