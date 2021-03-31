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
abstract class ServerPlayNetworkHandlerMixin_TimeTracker {
    @Shadow public ServerPlayerEntity player;
    @Unique private long lastTickTime = Util.getMeasuringTimeMs();
    @Unique private final long afkTime = 60000L;

    @Inject(
            method = "tick",
            at = @At(
                    value = "TAIL"
            )
    )
    private void trackTime(CallbackInfo ci) {
        AFKPlayer afkPlayer = (AFKPlayer) this.player;
        long nowTickTime = Util.getMeasuringTimeMs();

        if (!afkPlayer.isAfk()) {
            if (this.player.getLastActionTime() > 0L && nowTickTime - this.player.getLastActionTime() > this.afkTime) {
                afkPlayer.setAfk(true);
                afkPlayer.setPlaytime(afkPlayer.getPlaytime() - this.afkTime); // removes last 5 afk minutes of playtime
            } else {
                afkPlayer.setPlaytime(
                        afkPlayer.getPlaytime() + (nowTickTime - this.lastTickTime)
                );
            }
        } else {
            if (this.player.getLastActionTime() > 0L && nowTickTime - this.player.getLastActionTime() < this.afkTime) {
                afkPlayer.setAfk(false);
            }
        }

        this.lastTickTime = nowTickTime;
    }
}
