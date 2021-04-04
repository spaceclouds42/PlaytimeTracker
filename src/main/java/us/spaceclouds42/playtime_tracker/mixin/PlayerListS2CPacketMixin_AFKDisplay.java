package us.spaceclouds42.playtime_tracker.mixin;

import com.mojang.authlib.GameProfile;
import net.minecraft.network.packet.s2c.play.PlayerListS2CPacket;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import us.spaceclouds42.playtime_tracker.Common;
import us.spaceclouds42.playtime_tracker.duck.AFKPlayer;

@Mixin(PlayerListS2CPacket.class)
abstract class PlayerListS2CPacketMixin_AFKDisplay {
    @Mixin(PlayerListS2CPacket.Entry.class)
    private abstract static class EntryMixin {
        @Shadow @Final private Text displayName;

        @Shadow @Final private GameProfile profile;

        @Inject(
                method = "getDisplayName",
                at = @At(
                        value = "HEAD"
                ),
                cancellable = true
        )
        private void modifyDisplayName(CallbackInfoReturnable<Text> cir) {
            if (((AFKPlayer) Common.SERVER.getPlayerManager().createPlayer(this.profile)).isAfk()) {
                cir.setReturnValue(this.displayName.copy().formatted(Formatting.GRAY));
            }
        }
    }
}
