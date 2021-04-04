package us.spaceclouds42.playtime_tracker.util

import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.util.Identifier

class AdvancementHelper(player: ServerPlayerEntity, id: String) {
    private val tracker = player.advancementTracker
    private val advancement = player.server.advancementLoader[Identifier.tryParse(id)]
    private val progress = tracker.getProgress(advancement)

    fun grant() {
        progress.unobtainedCriteria.forEach {
            this.tracker.grantCriterion(advancement, it)
        }
    }

    fun revoke() {
        progress.obtainedCriteria.forEach {
            this.tracker.revokeCriterion(advancement, it)
        }
    }

    fun completed(): Boolean {
        return this.progress.isDone
    }
}