package net.kinomc.appeals.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import net.kinomc.appeals.common.ErrorCode;
import net.kinomc.appeals.exception.BusinessException;
import net.kinomc.appeals.mapper.AppealPunishMapper;
import net.kinomc.appeals.model.entity.AppealPunish;
import net.kinomc.appeals.service.AppealPunishService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.UUID;

@Service
@Slf4j
public class AppealPunishServiceImpl extends ServiceImpl<AppealPunishMapper, AppealPunish> implements AppealPunishService {
    @Resource
    private AppealPunishMapper appealPunishMapper;

    @Override
    public void addPunishAppeal(UUID uuid, String userName, String userEmail, String appealReason, String[] photos, long punishInfo) {
        if (StringUtils.isAnyBlank(userName, userEmail)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数为空");
        }
        AppealPunish appealPunish = new AppealPunish();
        appealPunish.setPunishInfo(punishInfo);
        appealPunish.setUuid(uuid.toString());
        appealPunish.setUserName(userName);
        appealPunish.setUserEmail(userEmail);
        appealPunish.setAppealReason(appealReason);
        appealPunish.setPhotos(Arrays.toString(photos));
        boolean saveResult = this.save(appealPunish);
        if (!saveResult) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "添加失败，数据库错误");
        }
    }

    @Override
    public AppealPunish getPunish(UUID uuid) {
        QueryWrapper<AppealPunish> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("uuid", uuid.toString());
        AppealPunish appealPunish = appealPunishMapper.selectOne(queryWrapper);
        if (appealPunish == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "申诉不存在");
        }
        return appealPunish;
    }
}
