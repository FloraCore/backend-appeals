package net.kinomc.appeals.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.plugins.pagination.PageDTO;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import net.kinomc.appeals.annotation.AuthCheck;
import net.kinomc.appeals.common.BaseResponse;
import net.kinomc.appeals.common.DeleteRequest;
import net.kinomc.appeals.common.ErrorCode;
import net.kinomc.appeals.common.ResultUtils;
import net.kinomc.appeals.constant.CommonConstant;
import net.kinomc.appeals.constant.UserConstant;
import net.kinomc.appeals.exception.BusinessException;
import net.kinomc.appeals.model.dto.*;
import net.kinomc.appeals.model.entity.*;
import net.kinomc.appeals.model.vo.AppealAbnormalVO;
import net.kinomc.appeals.model.vo.AppealBlackListVO;
import net.kinomc.appeals.model.vo.AppealPunishVO;
import net.kinomc.appeals.model.vo.AppealVO;
import net.kinomc.appeals.service.*;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/appeal")
@Slf4j
public class AppealController {
    @Resource
    private AppealService appealService;
    @Resource
    private AppealPunishService appealPunishService;
    @Resource
    private AppealBlacklistService appealBlacklistService;
    @Resource
    private UserService userService;
    @Resource
    private MailService mailService;
    @Resource
    private AppealAbnormalService appealAbnormalService;
    @Resource
    private LitebansService litebansService;
    @Resource
    private PhotosService photosService;
    @Resource
    private WechatBotService wechatBotService;
    @Resource
    private ReasonListService reasonListService;

    @PostMapping("/add")
    public BaseResponse<Long> addAppeal(@RequestBody AppealAddRequest appealAddRequest) {
        if (appealAddRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        String userName = appealAddRequest.getUserName();
        String userEmail = appealAddRequest.getUserEmail();
        String appealReason = appealAddRequest.getAppealReason();
        String[] photos = appealAddRequest.getPhotos();
        if (StringUtils.isAnyBlank(userName, userEmail, appealReason)) {
            return null;
        }
        if (appealService.haveActiveAppeal(userName)) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "这名玩家存在一个尚未处理的申诉");
        }
        long id = appealService.addAppeal(userName, "punish");
        UUID uuid = appealService.getAppealUUID(id);
        UUID userUUID = litebansService.getUUIDByName(userName);
        long punishInfo = appealService.addPunishInfo(userUUID);
        List<String> ret = new ArrayList<>();
        Set<String> seen = new HashSet<>(); // 用 Set 来保存已经添加的元素
        for (String data : photos) {
            String u = photosService.addPhoto(data).getUuid();
            if (!seen.contains(u)) { // 如果当前 uuid 没有出现过，则添加到集合和列表中
                ret.add(u);
                seen.add(u);
            }
        }
        appealPunishService.addPunishAppeal(uuid, userName, userEmail, appealReason, ret.toArray(new String[0]), punishInfo);
        mailService.sendCreateAppealNotice(userEmail, "punish", userName, id);
        wechatBotService.sendNewAppealNotice(userName, "punish", id);
        return ResultUtils.success(id);
    }

    @PostMapping("/abnormal/add")
    public BaseResponse<Long> addAppealAbnormal(@RequestBody AppealAddRequest appealAddRequest) {
        if (appealAddRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        String userName = appealAddRequest.getUserName();
        // 确认这么玩家的上线记录
        UUID userUUID = litebansService.getUUIDByName(userName);
        String userEmail = appealAddRequest.getUserEmail();
        String appealReason = appealAddRequest.getAppealReason();
        String[] photos = appealAddRequest.getPhotos();
        if (StringUtils.isAnyBlank(userName, userEmail, appealReason)) {
            return null;
        }
        if (appealService.haveActiveAppeal(userName)) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "这名玩家存在一个尚未处理的申诉");
        }
        long id = appealService.addAppeal(userName, "abnormal");
        UUID uuid = appealService.getAppealUUID(id);
        List<String> ret = new ArrayList<>();
        Set<String> seen = new HashSet<>(); // 用 Set 来保存已经添加的元素
        for (String data : photos) {
            String u = photosService.addPhoto(data).getUuid();
            if (!seen.contains(u)) { // 如果当前 uuid 没有出现过，则添加到集合和列表中
                ret.add(u);
                seen.add(u);
            }
        }
        appealAbnormalService.addAbnormalAppeal(uuid, userName, userEmail, appealReason, ret.toArray(new String[0]));
        mailService.sendCreateAppealNotice(userEmail, "abnormal", userName, id);
        wechatBotService.sendNewAppealNotice(userName, "abnormal", id);
        return ResultUtils.success(id);
    }

    @PostMapping("/accept")
    @AuthCheck()
    public BaseResponse<Boolean> acceptAppeal(@RequestBody AppealAcceptRequest appealAcceptRequest) {
        if (appealAcceptRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        long appealID = appealAcceptRequest.getAppealID();
        long userID = appealAcceptRequest.getUserID();
        boolean result = appealService.accept(appealID, userID);
        return ResultUtils.success(result);
    }

    @PostMapping("/blacklist/add")
    @AuthCheck()
    public BaseResponse<Boolean> addBlacklist(@RequestBody AppealBlacklistAddRequest appealBlacklistAddRequest) {
        if (appealBlacklistAddRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        String userName = appealBlacklistAddRequest.getUserName();
        UUID uuid = litebansService.getUUIDByName(userName);
        appealBlacklistService.addBlackList(uuid, appealBlacklistAddRequest.getAssignee(), appealBlacklistAddRequest.getReason());
        return ResultUtils.success(true);
    }

    @GetMapping("/blacklist/check")
    public BaseResponse<Boolean> checkBlacklist(String userName) {
        UUID uuid = litebansService.getUUIDByName(userName);
        appealBlacklistService.checkBlackList(uuid);
        return ResultUtils.success(true);
    }

    @PostMapping("/check/audit")
    @AuthCheck()
    public BaseResponse<Boolean> checkAuditAppeal(@RequestBody AppealAuditCheckRequest appealAuditCheckRequest) {
        if (appealAuditCheckRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        long userID = appealAuditCheckRequest.getUserID();
        boolean result = appealService.checkAudit(appealAuditCheckRequest.getAppealID(), userID);
        if (!result && !userService.isAdmin(userID)) {
            throw new BusinessException(ErrorCode.APPEAL_NOT_AUDIT_PERMISSION, "您没有这个权限处理这个申诉");
        }
        return ResultUtils.success(result);
    }

    @PostMapping("/audit")
    @AuthCheck()
    public BaseResponse<Boolean> auditAppeal(@RequestBody AppealAuditRequest appealAuditRequest) {
        if (appealAuditRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        boolean result = appealService.audit(appealAuditRequest.getId(), appealAuditRequest.getConclusion(), appealAuditRequest.getConclusionReason());
        return ResultUtils.success(result);
    }

    @PostMapping("/checkVO")
    public BaseResponse<AppealVO> checkAppeal(@RequestBody AppealCheckRequest appealCheckRequest) {
        if (appealCheckRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        String userName = appealCheckRequest.getUserName();
        long id = appealCheckRequest.getId();
        Appeal appeal = appealService.checkVO(userName, id);
        AppealVO appealVO = new AppealVO();
        BeanUtils.copyProperties(appeal, appealVO);
        return ResultUtils.success(appealVO);
    }

    @GetMapping("/check/VO/punish")
    public BaseResponse<AppealPunishVO> checkAppealPunish(String uuid) {
        UUID u;
        try {
            u = UUID.fromString(uuid);
        } catch (IllegalArgumentException e) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "无效的UUID");
        }
        AppealPunish appealPunish = appealPunishService.getPunish(u);
        AppealPunishVO appealPunishVO = new AppealPunishVO();
        BeanUtils.copyProperties(appealPunish, appealPunishVO);
        return ResultUtils.success(appealPunishVO);
    }

    @GetMapping("/check/VO/abnormal")
    public BaseResponse<AppealAbnormalVO> checkAppealAbnoraml(String uuid) {
        UUID u;
        try {
            u = UUID.fromString(uuid);
        } catch (IllegalArgumentException e) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "无效的UUID");
        }
        AppealAbnormal appealAbnormal = appealAbnormalService.getAbnormal(u);
        AppealAbnormalVO appealAbnormalVO = new AppealAbnormalVO();
        BeanUtils.copyProperties(appealAbnormal, appealAbnormalVO);
        return ResultUtils.success(appealAbnormalVO);
    }

    @GetMapping("/check")
    public BaseResponse<Boolean> checkPunish(String username) {
        // 确认这么玩家的上线记录
        UUID userUUID = litebansService.getUUIDByName(username);
        if (appealService.haveActiveAppeal(username)) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "这名玩家存在一个尚未处理的申诉");
        }
        return ResultUtils.success(true);
    }

    /**
     * 删除理由
     */
    @PostMapping("/reason/delete")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> deleteReason(@RequestBody DeleteRequest deleteRequest) {
        if (deleteRequest == null || deleteRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        boolean b = reasonListService.removeById(deleteRequest.getId());
        return ResultUtils.success(b);
    }

    /**
     * 创建用户
     */
    @PostMapping("/reason/add")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Long> addReason(@RequestBody ReasonAddRequest reasonAddRequest) {
        if (reasonAddRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        if (StringUtils.isAnyBlank(reasonAddRequest.getTitle(), reasonAddRequest.getReason())) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数为空");
        }
        if (reasonAddRequest.getTitle().length() > 16) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "标题过长");
        }
        ReasonList reasonList = new ReasonList();
        BeanUtils.copyProperties(reasonAddRequest, reasonList);
        boolean result = reasonListService.save(reasonList);
        if (!result) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR);
        }
        return ResultUtils.success(reasonList.getId());
    }

    /**
     * 更新用户
     */
    @PostMapping("/reason/update")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> updateReason(@RequestBody ReasonUpdateRequest reasonUpdateRequest) {
        if (reasonUpdateRequest == null || reasonUpdateRequest.getId() == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        if (StringUtils.isAnyBlank(reasonUpdateRequest.getTitle(), reasonUpdateRequest.getReason())) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数为空");
        }
        if (reasonUpdateRequest.getTitle().length() > 16) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "标题过长");
        }
        ReasonList reasonList = new ReasonList();
        BeanUtils.copyProperties(reasonUpdateRequest, reasonList);
        boolean result = reasonListService.updateById(reasonList);
        return ResultUtils.success(result);
    }

    @GetMapping("/list/page")
    @AuthCheck()
    public BaseResponse<Page<Appeal>> getAppealListByPage(AppealQueryRequest appealQueryRequest) {
        if (appealQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Appeal appealQuery = new Appeal();
        BeanUtils.copyProperties(appealQueryRequest, appealQuery);
        long current = appealQueryRequest.getCurrent();
        long size = appealQueryRequest.getPageSize();
        String sortField = appealQueryRequest.getSortField();
        String sortOrder = appealQueryRequest.getSortOrder();
        String userName = appealQueryRequest.getUserName();
        // 默认以id排序
        if (sortField == null) {
            sortField = "id";
        }
        // userName 需支持模糊搜索
        appealQueryRequest.setUserName(null);
        // 限制爬虫
        if (size > 100) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        QueryWrapper<Appeal> queryWrapper = new QueryWrapper<>(appealQuery);
        queryWrapper.like(StringUtils.isNotBlank(userName), "userName", userName);
        queryWrapper.orderBy(StringUtils.isNotBlank(sortField), sortOrder.equals(CommonConstant.SORT_ORDER_ASC), sortField);
        Page<Appeal> appealPage = appealService.page(new Page<>(current, size), queryWrapper);
        return ResultUtils.success(appealPage);
    }

    @GetMapping("/blacklist/page")
    public BaseResponse<Page<AppealBlackListVO>> getBlacklistByPage(AppealBlacklistQueryRequest appealBlacklistQueryRequest) {
        if (appealBlacklistQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        AppealBlacklist appealBlacklistQuery = new AppealBlacklist();
        BeanUtils.copyProperties(appealBlacklistQueryRequest, appealBlacklistQuery);
        long current = appealBlacklistQueryRequest.getCurrent();
        long size = appealBlacklistQueryRequest.getPageSize();
        String sortField = appealBlacklistQueryRequest.getSortField();
        String sortOrder = appealBlacklistQueryRequest.getSortOrder();
        // 默认以id排序
        if (sortField == null) {
            sortField = "id";
        }
        // 限制爬虫
        if (size > 100) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        QueryWrapper<AppealBlacklist> queryWrapper = new QueryWrapper<>(appealBlacklistQuery);
        queryWrapper.orderBy(StringUtils.isNotBlank(sortField), sortOrder.equals(CommonConstant.SORT_ORDER_ASC), sortField);
        Page<AppealBlacklist> appealBlacklistPage = appealBlacklistService.page(new Page<>(current, size), queryWrapper);
        Page<AppealBlackListVO> appealBlackListVOPage = new PageDTO<>(appealBlacklistPage.getCurrent(), appealBlacklistPage.getSize(), appealBlacklistPage.getTotal());
        List<AppealBlackListVO> appealBlackListVOList = appealBlacklistPage.getRecords().stream().map(appealBlacklist -> {
            AppealBlackListVO appealBlackListVO = new AppealBlackListVO();
            BeanUtils.copyProperties(appealBlacklist, appealBlackListVO);
            appealBlackListVO.setName(litebansService.getNameByUUID(UUID.fromString(appealBlacklist.getUuid())));
            User user = userService.getById(appealBlacklist.getAssignee());
            if (user != null) {
                appealBlackListVO.setAssigneeName(user.getUserName());
            } else {
                appealBlackListVO.setAssigneeName("Unknown " + appealBlacklist.getAssignee());
            }
            return appealBlackListVO;
        }).collect(Collectors.toList());
        appealBlackListVOPage.setRecords(appealBlackListVOList);
        return ResultUtils.success(appealBlackListVOPage);
    }

    @GetMapping("/reason/page")
    @AuthCheck()
    public BaseResponse<Page<ReasonList>> getReasonListByPage(ReasonListQueryRequest reasonListQueryRequest) {
        if (reasonListQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        ReasonList reasonList = new ReasonList();
        BeanUtils.copyProperties(reasonListQueryRequest, reasonList);
        long current = reasonListQueryRequest.getCurrent();
        long size = reasonListQueryRequest.getPageSize();
        String sortField = reasonListQueryRequest.getSortField();
        String sortOrder = reasonListQueryRequest.getSortOrder();
        String title = reasonListQueryRequest.getTitle();
        // 默认以id排序
        if (sortField == null) {
            sortField = "id";
        }
        // 支持模糊搜索
        reasonListQueryRequest.setTitle(null);
        // 限制爬虫
        if (size > 100) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        QueryWrapper<ReasonList> queryWrapper = new QueryWrapper<>(reasonList);
        queryWrapper.like(StringUtils.isNotBlank(title), "title", title);
        queryWrapper.orderBy(StringUtils.isNotBlank(sortField), sortOrder.equals(CommonConstant.SORT_ORDER_ASC), sortField);
        Page<ReasonList> reasonListPage = reasonListService.page(new Page<>(current, size), queryWrapper);
        return ResultUtils.success(reasonListPage);
    }
}
