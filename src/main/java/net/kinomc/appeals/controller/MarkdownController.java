package net.kinomc.appeals.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import net.kinomc.appeals.annotation.AuthCheck;
import net.kinomc.appeals.common.BaseResponse;
import net.kinomc.appeals.common.DeleteRequest;
import net.kinomc.appeals.common.ErrorCode;
import net.kinomc.appeals.common.ResultUtils;
import net.kinomc.appeals.constant.CommonConstant;
import net.kinomc.appeals.constant.UserConstant;
import net.kinomc.appeals.exception.BusinessException;
import net.kinomc.appeals.model.dto.MarkdownAddRequest;
import net.kinomc.appeals.model.dto.MarkdownQueryRequest;
import net.kinomc.appeals.model.dto.MarkdownUpdateRequest;
import net.kinomc.appeals.model.entity.Markdowns;
import net.kinomc.appeals.service.MarkdownsService;
import net.kinomc.appeals.service.UserService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/markdown")
@Slf4j
public class MarkdownController {
    @Resource
    private MarkdownsService markdownsService;
    @Resource
    private UserService userService;

    /**
     * 创建MD
     */
    @PostMapping("/add")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Long> addReason(@RequestBody MarkdownAddRequest markdownAddRequest, HttpServletRequest request) {
        if (markdownAddRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        if (StringUtils.isAnyBlank(markdownAddRequest.getTitle(), markdownAddRequest.getContent())) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数为空");
        }
        if (markdownAddRequest.getTitle().length() > 50) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "标题过长");
        }
        Markdowns markdowns = new Markdowns();
        BeanUtils.copyProperties(markdownAddRequest, markdowns);
        long newInt = userService.getLoginUser(request).getId();
        markdowns.setCreator(newInt);
        List<Long> list = new ArrayList<>();
        list.add(newInt);
        markdowns.setParticipant(list.toString());
        UUID uuid = UUID.randomUUID();
        markdowns.setUuid(uuid.toString());
        boolean result = markdownsService.save(markdowns);
        if (!result) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR);
        }
        return ResultUtils.success(markdowns.getId());
    }

    /**
     * 删除MD
     */
    @PostMapping("/delete")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> deleteMarkdown(@RequestBody DeleteRequest deleteRequest) {
        if (deleteRequest == null || deleteRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        boolean b = markdownsService.removeById(deleteRequest.getId());
        return ResultUtils.success(b);
    }

    /**
     * 更新md
     */
    @PostMapping("/update")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> updateMarkdown(@RequestBody MarkdownUpdateRequest markdownUpdateRequest, HttpServletRequest request) {
        if (markdownUpdateRequest == null || markdownUpdateRequest.getId() == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Markdowns markdowns = new Markdowns();
        BeanUtils.copyProperties(markdownUpdateRequest, markdowns);
        Markdowns mi = markdownsService.getById(markdowns.getId());
        String str = mi.getParticipant();
        str = str.substring(1, str.length() - 1);  // 去掉字符串两端的括号
        String[] strArr = str.split(",");  // 将字符串按逗号分隔成数组
        List<Long> list = new ArrayList<>();
        for (String s : strArr) {
            list.add(Long.parseLong(s.trim()));  // 将字符串转换为整数并添加到 List 中
        }
        long newInt = userService.getLoginUser(request).getId();
        if (!list.contains(newInt)) {
            list.add(newInt);
        }
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        for (int i = 0; i < list.size(); i++) {
            sb.append(list.get(i));
            if (i != list.size() - 1) {
                sb.append(",");
            }
        }
        sb.append("]");
        markdowns.setParticipant(sb.toString());
        boolean result = markdownsService.updateById(markdowns);
        return ResultUtils.success(result);
    }

    @GetMapping("/list")
    public BaseResponse<List<Markdowns>> listMarkdowns(MarkdownQueryRequest markdownQueryRequest, HttpServletRequest request) {
        if (markdownQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Markdowns markdownsQuery = new Markdowns();
        BeanUtils.copyProperties(markdownQueryRequest, markdownsQuery);
        String sortField = "id";
        String sortOrder = markdownQueryRequest.getSortOrder();
        QueryWrapper<Markdowns> queryWrapper = new QueryWrapper<>(markdownsQuery);
        queryWrapper.orderBy(StringUtils.isNotBlank(sortField), sortOrder.equals(CommonConstant.SORT_ORDER_ASC), sortField);
        queryWrapper.eq("enabled", 1);
        Page<Markdowns> markdownsPage = markdownsService.page(new Page<>(1, 10), queryWrapper);
        List<Markdowns> markdownsList = markdownsPage.getRecords();
        return ResultUtils.success(markdownsList);
    }

    @GetMapping("/list/page")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Page<Markdowns>> listMarkdownsByPage(MarkdownQueryRequest markdownQueryRequest) {
        if (markdownQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Markdowns markdownsQuery = new Markdowns();
        BeanUtils.copyProperties(markdownQueryRequest, markdownsQuery);
        long current = markdownQueryRequest.getCurrent();
        long size = markdownQueryRequest.getPageSize();
        String sortField = markdownQueryRequest.getSortField();
        String sortOrder = markdownQueryRequest.getSortOrder();
        String title = markdownQueryRequest.getTitle();
        // title 需支持模糊搜索
        markdownsQuery.setTitle(null);
        // 限制爬虫
        if (size > 100) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        QueryWrapper<Markdowns> queryWrapper = new QueryWrapper<>(markdownsQuery);
        queryWrapper.like(StringUtils.isNotBlank(title), "title", title);
        queryWrapper.orderBy(StringUtils.isNotBlank(sortField), sortOrder.equals(CommonConstant.SORT_ORDER_ASC), sortField);
        Page<Markdowns> markdownsPage = markdownsService.page(new Page<>(current, size), queryWrapper);
        return ResultUtils.success(markdownsPage);
    }
}
