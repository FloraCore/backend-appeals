package net.kinomc.appeals.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import net.kinomc.appeals.common.ErrorCode;
import net.kinomc.appeals.exception.BusinessException;
import net.kinomc.appeals.mapper.AppealBlacklistMapper;
import net.kinomc.appeals.model.entity.AppealBlacklist;
import net.kinomc.appeals.service.AppealBlacklistService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@Slf4j
public class AppealBlacklistServiceImpl extends ServiceImpl<AppealBlacklistMapper, AppealBlacklist> implements AppealBlacklistService {
    @Resource
    private AppealBlacklistMapper appealBlacklistMapper;

    @Override
    public void addBlackList(UUID uuid, long assignee, String reason) {
        if (StringUtils.isAnyBlank(reason)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "理由不能为空");
        }
        QueryWrapper<AppealBlacklist> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("uuid", uuid.toString());
        long count = appealBlacklistMapper.selectCount(queryWrapper);
        if (count > 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "该玩家已经被拉入黑名单了");
        }
        AppealBlacklist appealBlacklist = new AppealBlacklist();
        appealBlacklist.setUuid(uuid.toString());
        appealBlacklist.setAssignee(assignee);
        appealBlacklist.setReason(reason);
        boolean saveResult = this.save(appealBlacklist);
        if (!saveResult) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "添加失败，数据库错误");
        }
    }

    @Override
    public void checkBlackList(UUID uuid) {
        QueryWrapper<AppealBlacklist> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("uuid", uuid.toString());
        long count = appealBlacklistMapper.selectCount(queryWrapper);
        if (count > 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "该玩家已经被拉入黑名单了");
        }
    }
}
