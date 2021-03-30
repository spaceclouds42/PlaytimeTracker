package us.spaceclouds42.playtime_tracker

import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback
import us.spaceclouds42.playtime_tracker.command.PlaytimeCommand

object Common : ModInitializer {
    override fun onInitialize() {
        println("[Playtime Tracker] Tracking playtime!")

        CommandRegistrationCallback.EVENT.register { dispatcher, _ ->
            dispatcher.root.addChild(PlaytimeCommand().register())
        }
    }
}

