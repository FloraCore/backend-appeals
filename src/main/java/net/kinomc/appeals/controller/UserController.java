package net.kinomc.appeals.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.plugins.pagination.PageDTO;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import net.kinomc.appeals.annotation.AuthCheck;
import net.kinomc.appeals.common.BaseResponse;
import net.kinomc.appeals.common.DeleteRequest;
import net.kinomc.appeals.common.ErrorCode;
import net.kinomc.appeals.common.ResultUtils;
import net.kinomc.appeals.constant.UserConstant;
import net.kinomc.appeals.exception.BusinessException;
import net.kinomc.appeals.model.dto.*;
import net.kinomc.appeals.model.entity.User;
import net.kinomc.appeals.model.vo.UserVO;
import net.kinomc.appeals.service.MailService;
import net.kinomc.appeals.service.UserService;
import net.kinomc.appeals.service.WechatBotService;
import net.kinomc.appeals.utils.GoogleAuthenticator;
import net.kinomc.appeals.utils.NetUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.util.Base64Utils;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static net.kinomc.appeals.constant.UserConstant.SALT;

/**
 * 用户接口
 */
@RestController
@RequestMapping("/user")
@Slf4j
public class UserController {
    @Resource
    private UserService userService;
    @Resource
    private MailService mailService;
    @Resource
    private WechatBotService wechatBotService;
    @Resource
    private StringRedisTemplate stringRedisTemplate;
    @Value("${web.server-name:KinoMC}")
    private String serverName;
    @Value("${web.address:appeal.kinomc.net}")
    private String address;

    /**
     * 用户登录
     */
    @PostMapping("/login")
    public BaseResponse<UserVO> userLogin(@RequestBody UserLoginRequest userLoginRequest, HttpServletRequest request) {
        if (userLoginRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        String ip = NetUtils.getIpAddress(request);
        String userName = userLoginRequest.getUserName();
        String userPassword = userLoginRequest.getUserPassword();
        long code = userLoginRequest.getCode();
        if (StringUtils.isAnyBlank(userName, userPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User user = userService.userLogin(userName, userPassword, false, request);
        String key = user.getUserKey();
        long t = System.currentTimeMillis();
        GoogleAuthenticator ga = new GoogleAuthenticator();
        ga.setWindowSize(5);
        boolean r = ga.check_code(key, code, t);
        wechatBotService.sendUserLoginNotice(userName, r, false, "2FA验证失败", ip);
        if (r) {
            // 不存在loginUser的cookie
            String encryptPassword = DigestUtils.md5DigestAsHex((SALT + userPassword).getBytes());
            String var1 = String.format("userName:%s&userPassword:%s", userName, encryptPassword);
            UUID uuid = UUID.randomUUID();
            stringRedisTemplate.opsForValue().set(uuid.toString(), Base64Utils.encodeToString(var1.getBytes()), 7, TimeUnit.DAYS);
            UserVO userVO = new UserVO();
            userVO.setToken(uuid.toString());
            BeanUtils.copyProperties(user, userVO);
            return ResultUtils.success(userVO);
        } else {
            throw new BusinessException(ErrorCode.VALIDATION_FAILED);
        }
    }

    /**
     * 用户通过token登录
     */
    @PostMapping("/login/token")
    public BaseResponse<User> userLoginByCookie(String token, HttpServletRequest request) {
        if (token == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        String var1 = stringRedisTemplate.opsForValue().get(token);
        if (!StringUtils.isAnyBlank(var1)) {
            String ip = NetUtils.getIpAddress(request);
            byte[] decodedBytes = Base64Utils.decodeFromString(Objects.requireNonNull(var1));
            String decodedString = new String(decodedBytes);
            String[] parts = decodedString.split("&");
            String userName = parts[0].split(":")[1];
            String userPassword = parts[1].split(":")[1];
            if (StringUtils.isAnyBlank(userName, userPassword)) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR);
            }
            User user = userService.userLogin(userName, userPassword, true, request);
            wechatBotService.sendUserLoginNotice(userName, true, true, null, ip);
            return ResultUtils.success(user);
        } else {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }
    }

    /**
     * 用户注销
     */
    @PostMapping("/logout")
    @AuthCheck()
    public BaseResponse<Boolean> userLogout(String token, HttpServletRequest request) {
        if (request == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        if (!StringUtils.isAnyBlank()) {
            stringRedisTemplate.delete(token);
        }
        boolean result = userService.userLogout(request);
        return ResultUtils.success(result);
    }

    /**
     * 获取当前登录用户
     */
    @GetMapping("/get/login")
    public BaseResponse<UserVO> getLoginUser(HttpServletRequest request) {
        User user = userService.getLoginUser(request);
        UserVO userVO = new UserVO();
        BeanUtils.copyProperties(user, userVO);
        return ResultUtils.success(userVO);
    }

    /**
     * 创建用户
     */
    @PostMapping("/add")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Long> addUser(@RequestBody UserAddRequest userAddRequest) {
        if (userAddRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        if (StringUtils.isAnyBlank(userAddRequest.getUserName())) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数为空");
        }
        if (userAddRequest.getUserName().length() > 16) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户名过长");
        }
        if (userAddRequest.getUserPassword().length() < 8) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户密码过短");
        }
        boolean duplicates = userService.checkDuplicates(userAddRequest.getUserName());
        User user = new User();
        BeanUtils.copyProperties(userAddRequest, user);
        user.setUserKey(GoogleAuthenticator.generateSecretKey());
        String encryptPassword = DigestUtils.md5DigestAsHex((SALT + userAddRequest.getUserPassword()).getBytes());
        user.setUserPassword(encryptPassword);
        boolean result = userService.save(user);
        if (!result) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR);
        }
        return ResultUtils.success(user.getId());
    }

    /**
     * 删除用户
     */
    @PostMapping("/delete")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> deleteUser(@RequestBody DeleteRequest deleteRequest) {
        if (deleteRequest == null || deleteRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        boolean b = userService.removeById(deleteRequest.getId());
        return ResultUtils.success(b);
    }

    /**
     * 更新用户
     */
    @PostMapping("/update")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> updateUser(@RequestBody UserUpdateRequest userUpdateRequest) {
        if (userUpdateRequest == null || userUpdateRequest.getId() == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User user = new User();
        BeanUtils.copyProperties(userUpdateRequest, user);
        boolean result = userService.updateById(user);
        return ResultUtils.success(result);
    }

    /**
     * 更新用户
     */
    @PostMapping("/settings/update")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> updateUserBySettings(@RequestBody UserUpdateRequest userUpdateRequest) {
        if (userUpdateRequest == null || userUpdateRequest.getId() == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        if (userUpdateRequest.getUserPassword().length() < 8) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户密码过短");
        }
        User user = new User();
        BeanUtils.copyProperties(userUpdateRequest, user);
        String encryptPassword = DigestUtils.md5DigestAsHex((SALT + userUpdateRequest.getUserPassword()).getBytes());
        user.setUserPassword(encryptPassword);
        boolean result = userService.updateById(user);
        return ResultUtils.success(result);
    }

    @PostMapping("/settings/verify/send")
    public BaseResponse<Boolean> sendVerifyCode(int id) {
        if (id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User user = userService.getById(id);
        Random yzm = new Random();
        String yzm1 = "1234567890abcdefghijklmnopqrstuvwxwzABCDEFGHIJKLMNOPQRSTUVWXYZ";
        StringBuilder code = new StringBuilder();
        for (int i = 0; i < 5; i++) {
            int a = yzm.nextInt(58);
            code.append(yzm1.charAt(a));
        }
        mailService.sendVerificationCode(user.getUserEmail(), id, code.toString());
        return ResultUtils.success(true);
    }

    @PostMapping("/settings/verify/check")
    public BaseResponse<Boolean> checkVerifyCode(@RequestBody CheckVerifyCodeRequest checkVerifyCodeRequest) {
        if (checkVerifyCodeRequest == null || checkVerifyCodeRequest.getId() == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        boolean b = mailService.checkVerificationCode(checkVerifyCodeRequest.getId(), checkVerifyCodeRequest.getCode());
        if (!b) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR);
        }
        return ResultUtils.success(b);
    }

    /**
     * 根据 id 获取用户
     */
    @GetMapping("/get")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<UserVO> getUserById(int id) {
        if (id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User user = userService.getById(id);
        UserVO userVO = new UserVO();
        BeanUtils.copyProperties(user, userVO);
        return ResultUtils.success(userVO);
    }

    /**
     * 根据 id 获取用户名
     */
    @GetMapping("/get/name")
    public BaseResponse<String> getUserNameById(int id) {
        if (id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User user = userService.getById(id);
        if (user == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }
        return ResultUtils.success(user.getUserName());
    }

    /**
     * 根据 id 获取用户2FA密钥二维码内容
     */
    @GetMapping("/get/qrcode/2fa")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<String> getUserKey(int id) {
        if (id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User user = userService.getById(id);
        if (user == null) {
            throw new BusinessException(ErrorCode.VALIDATION_FAILED);
        }
        String key = user.getUserKey();
        return ResultUtils.success(GoogleAuthenticator.getQRBarcode(user.getUserName(), key, serverName, address));
    }

    /**
     * 获取用户列表
     */
    @GetMapping("/list")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<List<UserVO>> listUser(UserQueryRequest userQueryRequest, HttpServletRequest request) {
        User userQuery = new User();
        if (userQueryRequest != null) {
            BeanUtils.copyProperties(userQueryRequest, userQuery);
        }
        QueryWrapper<User> queryWrapper = new QueryWrapper<>(userQuery);
        List<User> userList = userService.list(queryWrapper);
        List<UserVO> userVOList = userList.stream().map(user -> {
            UserVO userVO = new UserVO();
            BeanUtils.copyProperties(user, userVO);
            return userVO;
        }).collect(Collectors.toList());
        return ResultUtils.success(userVOList);
    }

    /**
     * 分页获取用户列表
     */
    @GetMapping("/list/page")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Page<UserVO>> listUserByPage(UserQueryRequest userQueryRequest, HttpServletRequest request) {
        if (userQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User userQuery = new User();
        BeanUtils.copyProperties(userQueryRequest, userQuery);
        long current = userQueryRequest.getCurrent();
        long size = userQueryRequest.getPageSize();

        // 限制爬虫
        if (size > 100) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        QueryWrapper<User> queryWrapper = new QueryWrapper<>(userQuery);
        Page<User> userPage = userService.page(new Page<>(current, size), queryWrapper);
        Page<UserVO> userVOPage = new PageDTO<>(userPage.getCurrent(), userPage.getSize(), userPage.getTotal());
        List<UserVO> userVOList = userPage.getRecords().stream().map(user -> {
            UserVO userVO = new UserVO();
            BeanUtils.copyProperties(user, userVO);
            return userVO;
        }).collect(Collectors.toList());
        userVOPage.setRecords(userVOList);
        return ResultUtils.success(userVOPage);
    }
}
