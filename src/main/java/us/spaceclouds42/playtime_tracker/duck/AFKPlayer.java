package us.spaceclouds42.playtime_tracker.duck;

public interface AFKPlayer {
    boolean isAfk();
    void setAfk(boolean isAfk);

    long getPlaytime();
    void setPlaytime(long playtime);

    long strictLastActionTime();
    void setStrictLastActionTime(long time);
}
