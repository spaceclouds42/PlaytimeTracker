package us.spaceclouds42.playtime_tracker.extension

import com.mojang.authlib.GameProfile
import net.minecraft.server.PlayerManager
import net.minecraft.server.network.ServerPlayerEntity

fun GameProfile.toPlayer(playerManager: PlayerManager): ServerPlayerEntity {
    var player = playerManager.getPlayer(this.id)

    if (player == null) {
        player = playerManager.createPlayer(this)
        playerManager.loadPlayerData(player)
    }

    return player!!
}