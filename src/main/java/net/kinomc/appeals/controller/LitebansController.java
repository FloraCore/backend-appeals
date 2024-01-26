package net.kinomc.appeals.controller;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import net.kinomc.appeals.annotation.AuthCheck;
import net.kinomc.appeals.common.BaseResponse;
import net.kinomc.appeals.common.ErrorCode;
import net.kinomc.appeals.common.ResultUtils;
import net.kinomc.appeals.exception.BusinessException;
import net.kinomc.appeals.model.dto.LitebansExecuteRequest;
import net.kinomc.appeals.model.entity.PunishInfo;
import net.kinomc.appeals.model.litebans.Punish;
import net.kinomc.appeals.model.vo.PunishInfoVO;
import net.kinomc.appeals.model.vo.PunishVO;
import net.kinomc.appeals.service.AppealService;
import net.kinomc.appeals.service.LitebansService;
import net.kinomc.appeals.service.PunishInfoService;
import net.kinomc.appeals.utils.DurationParser;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

/**
 * Litebans接口
 */
@RestController
@RequestMapping("/litebans")
@Slf4j
public class LitebansController {
    @Resource
    private LitebansService litebansService;
    @Resource
    private AppealService appealService;
    @Resource
    private PunishInfoService punishInfoService;

    public static Duration parseDuration(String input) {
        try {
            long number = Long.parseLong(input);
            Instant now = Instant.now().truncatedTo(ChronoUnit.SECONDS);
            return DurationParser.checkPastDate(Duration.between(now, Instant.ofEpochSecond(number)));
        } catch (NumberFormatException e) {
            // ignore
        }

        try {
            return DurationParser.checkPastDate(DurationParser.parseDuration(input));
        } catch (IllegalArgumentException e) {
            // ignore
        }

        return null;
    }

    @GetMapping("/check/punish")
    public BaseResponse<PunishVO> checkPunish(String username) {
        UUID uuid = litebansService.getUUIDByName(username);
        Punish punish = litebansService.checkPunish(uuid);
        PunishVO punishVO = new PunishVO();
        BeanUtils.copyProperties(punish, punishVO);
        if (appealService.haveActiveAppeal(username)) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "这名玩家存在一个尚未处理的申诉");
        }
        return ResultUtils.success(punishVO);
    }

    @GetMapping("/check/punish/id")
    public BaseResponse<PunishInfoVO> checkPunish(long id) {
        PunishInfo punishInfo = punishInfoService.getPunishInfo(id);
        PunishInfoVO punishInfoVO = new PunishInfoVO();
        BeanUtils.copyProperties(punishInfo, punishInfoVO);
        return ResultUtils.success(punishInfoVO);
    }

    @PostMapping("/execute")
    @AuthCheck()
    public BaseResponse<Boolean> litebansExecute(@RequestBody LitebansExecuteRequest litebansExecuteRequest) {
        /*
          假设获取到了，应该能拿到下面数值。
          操作类型：unban(2)、ban(1)。（后续项目结构大改后会陆续加入mute和unmute）
          用户名。
          原因。（任意带un的操作此项为null）
          时间。（任意带un的操作此项为null）
         */
        String userName = litebansExecuteRequest.getUserName();
        int type = litebansExecuteRequest.getType();
        String reason = litebansExecuteRequest.getReason();
        String duration = litebansExecuteRequest.getDate();
        if (!StringUtils.isNotBlank(userName)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        UUID uuid = litebansService.getUUIDByName(userName);
        boolean i;
        if (type == 1) {
            Duration d = parseDuration(duration);
            if (d != null) {
                // 将当前时间加上时间差
                Instant newTime = Instant.now().plus(d);
                // 将结果转换为时间戳
                long date = newTime.toEpochMilli();
                i = litebansService.banPlayer(uuid, reason, date);
            } else {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "输入的日期无效");
            }
        } else if (type == 2) {
            i = litebansService.unBanPlayer(uuid);
        } else {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "未知的操作类型");
        }
        if (!i) throw new BusinessException(ErrorCode.OPERATION_ERROR, "操作失败，无目标记录");
        return ResultUtils.success(true);
    }
}
