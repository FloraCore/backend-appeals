package net.kinomc.appeals.service;

import com.baomidou.mybatisplus.extension.service.IService;
import net.kinomc.appeals.model.entity.AppealBlacklist;

import java.util.UUID;

public interface AppealBlacklistService extends IService<AppealBlacklist> {
    void addBlackList(UUID uuid, long assignee, String reason);

    void checkBlackList(UUID uuid);
}
