package us.spaceclouds42.playtime_tracker.mixin;

import net.minecraft.network.ClientConnection;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Util;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import us.spaceclouds42.playtime_tracker.duck.AFKPlayer;

@Mixin(PlayerManager.class)
abstract class PlayerManagerMixin_StartAFKDetection {
    @Inject(
            method = "onPlayerConnect",
            at = @At(
                    value = "TAIL"
            )
    )
    private void updateLastActionTime(ClientConnection connection, ServerPlayerEntity player, CallbackInfo ci) {
        ((AFKPlayer) player).setStrictLastActionTime(Util.getMeasuringTimeMs());
    }
}
