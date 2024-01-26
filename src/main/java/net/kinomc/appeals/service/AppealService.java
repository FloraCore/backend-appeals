package net.kinomc.appeals.service;

import com.baomidou.mybatisplus.extension.service.IService;
import net.kinomc.appeals.model.entity.Appeal;

import java.util.UUID;

public interface AppealService extends IService<Appeal> {
    /**
     * 添加申诉
     *
     * @param appealType 申诉类型：punish/account/report-staff
     * @return 申诉ID
     */
    long addAppeal(String userName, String appealType);

    /**
     * 获取申诉UUID
     *
     * @param id 申诉ID
     * @return 申诉UUID
     */
    UUID getAppealUUID(long id);

    boolean haveActiveAppeal(String userName);

    long addPunishInfo(UUID uuid);

    Appeal checkVO(String userName, long id);

    boolean accept(Long appealID, Long userID);

    boolean audit(Long id, Integer conclusion, String conclusionReason);

    boolean checkAudit(Long appealID, Long userID);

    int getTheNumberOfNewAppealsToday();
}
