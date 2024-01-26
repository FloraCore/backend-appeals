package net.kinomc.appeals.service;

import net.kinomc.appeals.model.litebans.Punish;

import java.util.UUID;

public interface LitebansService {
    UUID getUUIDByName(String username);

    String getIPByUUID(UUID uuid);

    String getNameByUUID(UUID uuid);

    Punish checkPunish(UUID uuid);

    boolean unBanPlayer(UUID uuid);

    boolean banPlayer(UUID uuid, String reason, Long date);
}
