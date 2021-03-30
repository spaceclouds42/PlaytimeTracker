package us.spaceclouds42.playtime_tracker.mixin;

import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Util;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import us.spaceclouds42.playtime_tracker.duck.AFKPlayer;

@Mixin(ServerPlayNetworkHandler.class)
abstract class ServerPlayNetworkHandlerMixin_AFKDetection {
    @Shadow public ServerPlayerEntity player;
    @Unique private long lastTickTime = Util.getMeasuringTimeMs();

    @Inject(
            method = "tick",
            at = @At(
                    value = "TAIL"
            )
    )
    private void detectAFKPlayer(CallbackInfo ci) {
        AFKPlayer afkPlayer = (AFKPlayer) player;
        long nowTickTime = Util.getMeasuringTimeMs();

        if (player.getLastActionTime() > 0L && player.getLastActionTime() > 300000L) {
            afkPlayer.setAfk(true);
            afkPlayer.setPlaytime(afkPlayer.getPlaytime() - 300000L); // removes last 5 afk minutes of playtime
        } else {
            afkPlayer.setPlaytime(
                    afkPlayer.getPlaytime() + (nowTickTime - lastTickTime)
            );
        }

        lastTickTime = nowTickTime;
    }
}
