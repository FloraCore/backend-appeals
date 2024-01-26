package net.kinomc.appeals.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import net.kinomc.appeals.common.ErrorCode;
import net.kinomc.appeals.exception.BusinessException;
import net.kinomc.appeals.mapper.PunishInfoMapper;
import net.kinomc.appeals.model.entity.PunishInfo;
import net.kinomc.appeals.service.PunishInfoService;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class PunishInfoServiceImpl extends ServiceImpl<PunishInfoMapper, PunishInfo> implements PunishInfoService {
    @Resource
    private PunishInfoMapper punishInfoMapper;

    @Override
    public PunishInfo getPunishInfo(long id) {
        QueryWrapper<PunishInfo> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("id", id);
        PunishInfo punishInfo = punishInfoMapper.selectOne(queryWrapper);
        if (punishInfo == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "无记录的处罚信息");
        }
        return punishInfo;
    }
}
