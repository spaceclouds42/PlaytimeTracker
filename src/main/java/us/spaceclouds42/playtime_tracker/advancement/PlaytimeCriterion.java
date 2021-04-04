package us.spaceclouds42.playtime_tracker.advancement;

import com.google.gson.JsonObject;
import net.minecraft.advancement.criterion.AbstractCriterion;
import net.minecraft.advancement.criterion.AbstractCriterionConditions;
import net.minecraft.predicate.entity.AdvancementEntityPredicateDeserializer;
import net.minecraft.predicate.entity.EntityPredicate;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import us.spaceclouds42.playtime_tracker.duck.AFKPlayer;
import us.spaceclouds42.playtime_tracker.util.AdvancementHelper;

public class PlaytimeCriterion extends AbstractCriterion<PlaytimeCriterion.Conditions> {
    private static final Identifier ID = new Identifier("playtime_tracker:playtime");

    static final long hour = 60000L * 60L;
    private static final long dedicatedTime = hour * 10;
    private static final long timeMarchesTime = hour * 25;
    private static final long ancientOneTime = hour * 100;
    private static final long endOfTime = hour * 1000;

    @Override
    public Identifier getId() {
        return ID;
    }

    @Override
    protected Conditions conditionsFromJson(JsonObject obj, EntityPredicate.Extended playerPredicate, AdvancementEntityPredicateDeserializer predicateDeserializer) {
        return null;
    }

    public static void trigger(ServerPlayerEntity player) {
        AFKPlayer afkPlayer = (AFKPlayer) player;
        if (afkPlayer.getPlaytime() >= endOfTime) {
            AdvancementHelper.INSTANCE.grant(player, "playtime_tracker:end_of_time");
        }

        if (afkPlayer.getPlaytime() >= ancientOneTime) {
            AdvancementHelper.INSTANCE.grant(player, "playtime_tracker:ancient_one");
        }

        if (afkPlayer.getPlaytime() >= timeMarchesTime) {
            AdvancementHelper.INSTANCE.grant(player, "playtime_tracker:time_marches");
        }

        if (afkPlayer.getPlaytime() >= dedicatedTime) {
            AdvancementHelper.INSTANCE.grant(player, "playtime_tracker:dedicated");
        }
    }

    public static class Conditions extends AbstractCriterionConditions {
        public Conditions(EntityPredicate.Extended playerPredicate) {
            super(PlaytimeCriterion.ID, playerPredicate);
        }
    }
}
