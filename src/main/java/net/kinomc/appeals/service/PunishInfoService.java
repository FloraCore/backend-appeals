package net.kinomc.appeals.service;

import com.baomidou.mybatisplus.extension.service.IService;
import net.kinomc.appeals.model.entity.PunishInfo;

public interface PunishInfoService extends IService<PunishInfo> {
    PunishInfo getPunishInfo(long id);
}
