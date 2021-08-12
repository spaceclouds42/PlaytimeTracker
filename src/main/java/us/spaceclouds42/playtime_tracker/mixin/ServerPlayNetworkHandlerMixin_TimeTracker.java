package us.spaceclouds42.playtime_tracker.mixin;

import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.network.packet.s2c.play.PlayerListS2CPacket;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Util;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import us.spaceclouds42.playtime_tracker.advancement.PlaytimeCriterion;
import us.spaceclouds42.playtime_tracker.duck.AFKPlayer;

@Mixin(ServerPlayNetworkHandler.class)
abstract class ServerPlayNetworkHandlerMixin_TimeTracker {
    @Shadow public ServerPlayerEntity player;
    @Unique private long lastTickTime = Util.getMeasuringTimeMs();
    @Unique private final long afkTime = 60000L * 5L;

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
            if (afkPlayer.getStrictLastActionTime() > 0L && nowTickTime - afkPlayer.getStrictLastActionTime() > this.afkTime) {
                afkPlayer.setAfk(true);
                afkPlayer.setPlaytime(afkPlayer.getPlaytime() - this.afkTime); // removes last 5 afk minutes of playtime
                afkPlayer.setTempPlaytime(afkPlayer.getTempPlaytime() - this.afkTime);
                this.player.server.getPlayerManager().sendToAll(
                    new PlayerListS2CPacket(PlayerListS2CPacket.Action.UPDATE_DISPLAY_NAME, this.player)
                );
            } else {
                afkPlayer.setPlaytime(
                        afkPlayer.getPlaytime() + (nowTickTime - this.lastTickTime)
                );
                afkPlayer.setTempPlaytime(
                        afkPlayer.getTempPlaytime() + (nowTickTime - this.lastTickTime)
                );

                PlaytimeCriterion.trigger(this.player);
            }
        }

        this.lastTickTime = nowTickTime;
    }

    @Inject(
            method = "onPlayerMove",
            at = @At(
                    value = "HEAD"
            )
    )
    private void updateLastActionTime(PlayerMoveC2SPacket packet, CallbackInfo ci) {
        if (packet instanceof PlayerMoveC2SPacket.LookAndOnGround) {
            ((AFKPlayer) this.player).setStrictLastActionTime(Util.getMeasuringTimeMs());
            if (((AFKPlayer) this.player).isAfk()) {
                ((AFKPlayer) this.player).setAfk(false);
                this.player.server.getPlayerManager().sendToAll(
                        new PlayerListS2CPacket(PlayerListS2CPacket.Action.UPDATE_DISPLAY_NAME, this.player)
                );
            }
        }
    }
}
