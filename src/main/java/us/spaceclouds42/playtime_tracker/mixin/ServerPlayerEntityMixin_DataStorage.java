package us.spaceclouds42.playtime_tracker.mixin;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.LongTag;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import us.spaceclouds42.playtime_tracker.duck.AFKPlayer;

@Mixin(ServerPlayerEntity.class)
abstract class ServerPlayerEntityMixin_DataStorage implements AFKPlayer {
    @Unique private boolean isAfk;
    @Unique private long playtime;
    @Unique private long tempPlaytime;
    @Unique private long strictLastActionTime;

    @Override
    public boolean isAfk() {
        return this.isAfk;
    }

    @Override
    public void setAfk(boolean isAfk) {
        this.isAfk = isAfk;
    }

    @Override
    public long getPlaytime() {
        return this.playtime;
    }

    @Override
    public void setPlaytime(long playtime) {
        this.playtime = playtime;
    }

    @Override
    public long getTempPlaytime() {
        return this.tempPlaytime;
    }

    @Override
    public void setTempPlaytime(long playtime) {
        this.tempPlaytime = playtime;
    }

    @Override
    public long getStrictLastActionTime() {
        return this.strictLastActionTime;
    }

    @Override
    public void setStrictLastActionTime(long playtime) {
        this.strictLastActionTime = playtime;
    }

    @Inject(
            method = "writeCustomDataToTag",
            at = @At(
                    value = "TAIL"
            )
    )
    private void saveData(CompoundTag tag, CallbackInfo ci) {
        LongTag playtimeTag = LongTag.of(this.playtime);
        tag.put("Playtime", playtimeTag);

        LongTag tempPlaytimeTag = LongTag.of(this.tempPlaytime);
        tag.put("TempPlaytime", tempPlaytimeTag);
    }

    @Inject(
            method = "readCustomDataFromTag",
            at = @At(
                    value = "TAIL"
            )
    )
    private void readData(CompoundTag tag, CallbackInfo ci) {
        if (tag.contains("Playtime")) {
            this.playtime = tag.getLong("Playtime");
        }

        if (tag.contains("TempPlaytime")) {
            this.tempPlaytime = tag.getLong("TempPlaytime");
        }
    }

    @Inject(
            method = "copyFrom",
            at = @At(
                    value = "TAIL"
            )
    )
    private void copyData(ServerPlayerEntity oldPlayer, boolean alive, CallbackInfo ci) {
        this.isAfk = ((AFKPlayer) oldPlayer).isAfk();
        this.playtime = ((AFKPlayer) oldPlayer).getPlaytime();
        this.tempPlaytime = ((AFKPlayer) oldPlayer).getTempPlaytime();
    }
}
