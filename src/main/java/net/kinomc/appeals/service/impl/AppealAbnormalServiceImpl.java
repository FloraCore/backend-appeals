package net.kinomc.appeals.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import net.kinomc.appeals.common.ErrorCode;
import net.kinomc.appeals.exception.BusinessException;
import net.kinomc.appeals.mapper.AppealAbnormalMapper;
import net.kinomc.appeals.model.entity.AppealAbnormal;
import net.kinomc.appeals.service.AppealAbnormalService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.UUID;

@Service
@Slf4j
public class AppealAbnormalServiceImpl extends ServiceImpl<AppealAbnormalMapper, AppealAbnormal> implements AppealAbnormalService {
    @Resource
    private AppealAbnormalMapper appealAbnormalMapper;

    @Override
    public void addAbnormalAppeal(UUID uuid, String userName, String userEmail, String appealReason, String[] photos) {
        if (StringUtils.isAnyBlank(userName, userEmail)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数为空");
        }
        AppealAbnormal appealAbnormal = new AppealAbnormal();
        appealAbnormal.setUuid(uuid.toString());
        appealAbnormal.setUserName(userName);
        appealAbnormal.setUserEmail(userEmail);
        appealAbnormal.setAppealReason(appealReason);
        appealAbnormal.setPhotos(Arrays.toString(photos));
        boolean saveResult = this.save(appealAbnormal);
        if (!saveResult) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "添加失败，数据库错误");
        }
    }

    @Override
    public AppealAbnormal getAbnormal(UUID uuid) {
        QueryWrapper<AppealAbnormal> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("uuid", uuid.toString());
        AppealAbnormal appealAbnormal = appealAbnormalMapper.selectOne(queryWrapper);
        if (appealAbnormal == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "申诉不存在");
        }
        return appealAbnormal;
    }
}
