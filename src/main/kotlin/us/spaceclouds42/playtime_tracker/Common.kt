package us.spaceclouds42.playtime_tracker

import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback
import net.minecraft.advancement.criterion.Criteria
import us.spaceclouds42.playtime_tracker.advancement.PlaytimeCriterion
import us.spaceclouds42.playtime_tracker.command.PlaytimeCommand

object Common : ModInitializer {
    lateinit var PLAYTIME: PlaytimeCriterion

    override fun onInitialize() {
        println("[Playtime Tracker] Tracking playtime!")

        CommandRegistrationCallback.EVENT.register { dispatcher, _ ->
            dispatcher.root.addChild(PlaytimeCommand().register())
        }

        PLAYTIME = Criteria.register(PlaytimeCriterion())
    }
}

