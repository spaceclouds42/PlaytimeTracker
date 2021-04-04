package us.spaceclouds42.playtime_tracker.util

import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.util.Identifier

object AdvancementHelper {
    fun grant(player: ServerPlayerEntity, id: String) {
        val tracker = player.advancementTracker
        val advancement = player.server.advancementLoader[Identifier.tryParse(id)]
        val progress = tracker.getProgress(advancement)

        progress.unobtainedCriteria.forEach { criterion ->
            tracker.grantCriterion(advancement, criterion)
        }
    }

    fun revoke(player: ServerPlayerEntity, id: String) {
        val tracker = player.advancementTracker
        val advancement = player.server.advancementLoader[Identifier.tryParse(id)]
        val progress = tracker.getProgress(advancement)

        progress.obtainedCriteria.forEach { criterion ->
            tracker.revokeCriterion(advancement, criterion)
        }
    }
}