package us.spaceclouds42.playtime_tracker.mixin.access;

import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(PlayerManager.class)
public interface IAccessPlayerManager {
    @Invoker("savePlayerData")
    void invokeSavePlayerData(ServerPlayerEntity player);
}
