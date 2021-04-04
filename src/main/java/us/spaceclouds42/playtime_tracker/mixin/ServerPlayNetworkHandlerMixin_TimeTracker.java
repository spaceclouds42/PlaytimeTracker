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
import us.spaceclouds42.playtime_tracker.util.AdvancementHelper;

@Mixin(ServerPlayNetworkHandler.class)
abstract class ServerPlayNetworkHandlerMixin_TimeTracker {
    @Shadow public ServerPlayerEntity player;
    @Unique private long lastTickTime = Util.getMeasuringTimeMs();

    final long min = 60000L;
    final long hour = min * 60;
    @Unique private final long afkTime = min * 5;
    // Advancement time requirements
    @Unique private final long dedicatedTime = hour * 10;
    @Unique private final long timeMarchesTime = hour * 25;
    @Unique private final long ancientOneTime = hour * 100;

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

                applyAdvancements(afkPlayer);
            }
        } else {
            if (this.player.getLastActionTime() > 0L && nowTickTime - this.player.getLastActionTime() < this.afkTime) {
                afkPlayer.setAfk(false);
            }
        }

        this.lastTickTime = nowTickTime;
    }

    @Unique
    private void applyAdvancements(AFKPlayer afkPlayer) {
        if (afkPlayer.getPlaytime() >= this.ancientOneTime) {
            AdvancementHelper helper = new AdvancementHelper(this.player, "playtime_tracker:ancient_one");
            if (!helper.completed()) {
                helper.grant();
            }
        }

        if (afkPlayer.getPlaytime() >= this.timeMarchesTime) {
            AdvancementHelper helper = new AdvancementHelper(this.player, "playtime_tracker:time_marches");
            if (!helper.completed()) {
                helper.grant();
            }
        }

        if (afkPlayer.getPlaytime() >= this.dedicatedTime) {
            AdvancementHelper helper = new AdvancementHelper(this.player, "playtime_tracker:dedicated");
            if (!helper.completed()) {
                helper.grant();
            }
        }
    }
}
