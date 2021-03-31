package us.spaceclouds42.playtime_tracker.extension

import com.mojang.authlib.GameProfile
import net.minecraft.server.PlayerManager
import net.minecraft.server.network.ServerPlayerEntity

fun GameProfile.toPlayer(playerManager: PlayerManager): ServerPlayerEntity? {
    return playerManager.getPlayer(this.id)
}