package net.kinomc.appeals.service;

import com.baomidou.mybatisplus.extension.service.IService;
import net.kinomc.appeals.model.entity.AppealAbnormal;

import java.util.UUID;

public interface AppealAbnormalService extends IService<AppealAbnormal> {
    /**
     * 添加处罚申诉
     *
     * @param userName  玩家用户名
     * @param userEmail 邮箱
     * @param photos    图片Base64列表
     */
    void addAbnormalAppeal(UUID uuid, String userName, String userEmail, String appealReason, String[] photos);

    AppealAbnormal getAbnormal(UUID uuid);
}
