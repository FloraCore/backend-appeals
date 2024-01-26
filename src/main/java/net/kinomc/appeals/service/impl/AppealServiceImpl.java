package net.kinomc.appeals.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import net.kinomc.appeals.common.ErrorCode;
import net.kinomc.appeals.exception.BusinessException;
import net.kinomc.appeals.mapper.AppealMapper;
import net.kinomc.appeals.mapper.UserMapper;
import net.kinomc.appeals.model.entity.*;
import net.kinomc.appeals.model.litebans.Punish;
import net.kinomc.appeals.service.*;
import org.apache.commons.lang3.StringUtils;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static net.kinomc.appeals.constant.AppealConstant.TYPE_ABNORMAL;
import static net.kinomc.appeals.constant.AppealConstant.TYPE_PUNISH;

@Service
@Slf4j
public class AppealServiceImpl extends ServiceImpl<AppealMapper, Appeal> implements AppealService {
    @Resource
    private AppealMapper appealMapper;
    @Resource
    private UserMapper userMapper;
    @Resource
    private LitebansService litebansService;
    @Resource
    private AppealPunishService appealPunishService;
    @Resource
    private AppealAbnormalService appealAbnormalService;
    @Resource
    private MailService mailService;
    @Resource
    private PunishInfoService punishInfoService;
    @Resource
    private WechatBotService wechatBotService;
    @Resource
    private UserService userService;
    @Resource
    private JdbcTemplate jdbcTemplate;

    @Override
    public long addAppeal(String userName, String appealType) {
        if (StringUtils.isAnyBlank(appealType)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数为空");
        }
        synchronized (appealType.intern()) {
            Appeal appeal = new Appeal();
            appeal.setUserName(userName);
            appeal.setAppealType(appealType);
            UUID uuid = UUID.randomUUID();
            QueryWrapper<Appeal> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("uuid", uuid.toString());
            appeal.setUuid(uuid.toString());
            boolean saveResult = this.save(appeal);
            if (!saveResult) {
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, "添加失败，数据库错误");
            }
            return appeal.getId();
        }
    }

    @Override
    public UUID getAppealUUID(long id) {
        QueryWrapper<Appeal> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("id", id);
        Appeal appeal = appealMapper.selectOne(queryWrapper);
        if (appeal == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "申诉不存在");
        }
        try {
            return UUID.fromString(appeal.getUuid());
        } catch (IllegalArgumentException e) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "无效的UUID");
        }
    }

    @Override
    public boolean haveActiveAppeal(String userName) {
        QueryWrapper<Appeal> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userName", userName);
        queryWrapper.eq("state", 0);
        Appeal appeal = appealMapper.selectOne(queryWrapper);
        QueryWrapper<Appeal> queryWrapper1 = new QueryWrapper<>();
        queryWrapper1.eq("userName", userName);
        queryWrapper1.eq("state", 1);
        Appeal appeal1 = appealMapper.selectOne(queryWrapper1);
        return appeal != null || appeal1 != null;
    }

    @Override
    public long addPunishInfo(UUID uuid) {
        Punish punish = litebansService.checkPunish(uuid);
        PunishInfo punishInfo = new PunishInfo();
        punishInfo.setPunishID(punish.getId());
        punishInfo.setType(punish.getType());
        punishInfo.setReason(punish.getReason());
        punishInfo.setBannedByName(punish.getBannedByName());
        punishInfo.setTime(punish.getTime());
        punishInfo.setUntil(punish.getUntil());
        boolean saveResult = punishInfoService.save(punishInfo);
        if (!saveResult) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "添加失败，数据库错误");
        }
        return punishInfo.getId();
    }

    @Override
    public Appeal checkVO(String userName, long id) {
        QueryWrapper<Appeal> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("id", id);
        Appeal appeal = appealMapper.selectOne(queryWrapper);
        if (appeal == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "申诉不存在");
        }
        if (!userName.equalsIgnoreCase(appeal.getUserName())) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "申诉信息不匹配");
        }
        return appeal;
    }

    @Override
    public boolean accept(Long appealID, Long userID) {
        QueryWrapper<Appeal> queryAppealWrapper = new QueryWrapper<>();
        queryAppealWrapper.eq("id", appealID);
        Appeal appeal = appealMapper.selectOne(queryAppealWrapper);
        if (appeal == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "申诉不存在");
        }
        QueryWrapper<User> queryUserWrapper = new QueryWrapper<>();
        queryUserWrapper.eq("id", userID);
        User user = userMapper.selectOne(queryUserWrapper);
        if (user == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户不存在");
        }
        int state = appeal.getState();
        if (state != 0) {
            throw new BusinessException(ErrorCode.APPEAL_ALREADY_ACCEPT);
        }
        String appealType = appeal.getAppealType();
        UUID uuid;
        try {
            uuid = UUID.fromString(appeal.getUuid());
        } catch (IllegalArgumentException e) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "无效的UUID");
        }
        appeal.setState(1);
        String userEmail;
        String userName;
        boolean result1 = updateById(appeal);
        boolean result2;
        if (TYPE_PUNISH.equals(appealType)) {
            AppealPunish appealPunish = appealPunishService.getPunish(uuid);
            appealPunish.setAssignee(userID);
            appealPunish.setProcessingTime(new Date());
            userEmail = appealPunish.getUserEmail();
            userName = appealPunish.getUserName();
            result2 = appealPunishService.updateById(appealPunish);
        } else if (TYPE_ABNORMAL.equals(appealType)) {
            AppealAbnormal appealAbnormal = appealAbnormalService.getAbnormal(uuid);
            appealAbnormal.setAssignee(userID);
            appealAbnormal.setProcessingTime(new Date());
            userEmail = appealAbnormal.getUserEmail();
            userName = appealAbnormal.getUserName();
            result2 = appealAbnormalService.updateById(appealAbnormal);
        } else {
            throw new BusinessException(ErrorCode.APPEAL_TYPE_UNKNOWN);
        }
        mailService.sendAppealAcceptNotice(userEmail, appealType, userName, appealID, userID);
        User u = userService.getById(userID);
        wechatBotService.sendAcceptAppealNotice(userName, appealType, appealID, u.getUserName());
        return result1 && result2;
    }

    @Override
    public boolean checkAudit(Long appealID, Long userID) {
        QueryWrapper<Appeal> queryAppealWrapper = new QueryWrapper<>();
        queryAppealWrapper.eq("id", appealID);
        Appeal appeal = appealMapper.selectOne(queryAppealWrapper);
        if (appeal == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "申诉不存在");
        }
        QueryWrapper<User> queryUserWrapper = new QueryWrapper<>();
        queryUserWrapper.eq("id", userID);
        User user = userMapper.selectOne(queryUserWrapper);
        if (user == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户不存在");
        }
        String appealType = appeal.getAppealType();
        UUID uuid;
        try {
            uuid = UUID.fromString(appeal.getUuid());
        } catch (IllegalArgumentException e) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "无效的UUID");
        }
        if (TYPE_PUNISH.equals(appealType)) {
            AppealPunish appealPunish = appealPunishService.getPunish(uuid);
            long assignee = appealPunish.getAssignee();
            return assignee == userID;
        } else if (TYPE_ABNORMAL.equals(appealType)) {
            AppealAbnormal appealAbnormal = appealAbnormalService.getAbnormal(uuid);
            long assignee = appealAbnormal.getAssignee();
            return assignee == userID;
        } else {
            throw new BusinessException(ErrorCode.APPEAL_TYPE_UNKNOWN);
        }
    }

    @Override
    public boolean audit(Long id, Integer conclusion, String conclusionReason) {
        QueryWrapper<Appeal> queryAppealWrapper = new QueryWrapper<>();
        queryAppealWrapper.eq("id", id);
        Appeal appeal = appealMapper.selectOne(queryAppealWrapper);
        if (appeal == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "申诉不存在");
        }
        int state = appeal.getState();
        if (state == 0) {
            throw new BusinessException(ErrorCode.APPEAL_NOT_ACCEPT);
        }
        String appealType = appeal.getAppealType();
        UUID uuid;
        String userEmail;
        String userName;
        long assignee;
        try {
            uuid = UUID.fromString(appeal.getUuid());
        } catch (IllegalArgumentException e) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "无效的UUID");
        }
        appeal.setState(2);
        boolean result1 = updateById(appeal);
        boolean result2;
        if (TYPE_PUNISH.equals(appealType)) {
            AppealPunish appealPunish = appealPunishService.getPunish(uuid);
            appealPunish.setConclusion(conclusion);
            appealPunish.setConclusionReason(conclusionReason);
            userEmail = appealPunish.getUserEmail();
            userName = appealPunish.getUserName();
            appealPunish.setUpdateTime(new Date());
            result2 = appealPunishService.updateById(appealPunish);
            assignee = appealPunish.getAssignee();
        } else if (TYPE_ABNORMAL.equals(appealType)) {
            AppealAbnormal appealAbnormal = appealAbnormalService.getAbnormal(uuid);
            appealAbnormal.setConclusion(conclusion);
            appealAbnormal.setConclusionReason(conclusionReason);
            userEmail = appealAbnormal.getUserEmail();
            userName = appealAbnormal.getUserName();
            appealAbnormal.setUpdateTime(new Date());
            result2 = appealAbnormalService.updateById(appealAbnormal);
            assignee = appealAbnormal.getAssignee();
        } else {
            throw new BusinessException(ErrorCode.APPEAL_TYPE_UNKNOWN);
        }
        mailService.sendAppealAuditNotice(userEmail, appealType, userName, id, conclusion, conclusionReason);
        User u = userService.getById(assignee);
        wechatBotService.sendAuditAppealNotice(userName, appealType, id, u.getUserName(), conclusion, conclusionReason);
        return result1 && result2;
    }

    @Override
    public int getTheNumberOfNewAppealsToday() {
        String sql = "SELECT id FROM appeal WHERE DATEDIFF(createTime,NOW())=0";
        List<Map<String, Object>> list = jdbcTemplate.queryForList(sql);
        return list.size();
    }
}
