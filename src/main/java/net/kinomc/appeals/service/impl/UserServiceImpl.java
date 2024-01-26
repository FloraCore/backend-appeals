package net.kinomc.appeals.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import net.kinomc.appeals.common.ErrorCode;
import net.kinomc.appeals.exception.BusinessException;
import net.kinomc.appeals.mapper.UserMapper;
import net.kinomc.appeals.model.entity.User;
import net.kinomc.appeals.service.UserService;
import net.kinomc.appeals.service.WechatBotService;
import net.kinomc.appeals.utils.NetUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import static net.kinomc.appeals.constant.UserConstant.*;

/**
 * 用户服务实现类
 */
@Service
@Slf4j
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {
    @Resource
    private UserMapper userMapper;
    @Resource
    private WechatBotService wechatBotService;

    @Override
    public User userLogin(String userName, String userPassword, boolean cookie, HttpServletRequest request) {
        // 校验
        if (StringUtils.isAnyBlank(userName, userPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数为空");
        }
        if (userName.length() < 4) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号错误");
        }
        if (userPassword.length() < 8) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "密码错误");
        }
        String ip = NetUtils.getIpAddress(request);
        // 加密
        String encryptPassword;
        if (cookie) {
            encryptPassword = userPassword;
        } else {
            encryptPassword = DigestUtils.md5DigestAsHex((SALT + userPassword).getBytes());
        }
        // 查询用户是否存在
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userName", userName);
        queryWrapper.eq("userPassword", encryptPassword);
        User user = userMapper.selectOne(queryWrapper);
        // 用户不存在
        if (user == null) {
            log.info("user login failed, userAccount cannot match userPassword");
            wechatBotService.sendUserLoginNotice(userName, false, cookie, String.format("账号或密码错误<font color=gray>(%s)</font>", userName), ip);
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户不存在或密码错误");
        }
        if (user.getIsBaned() != 0) {
            wechatBotService.sendUserLoginNotice(userName, false, cookie, String.format("账号已被封禁<font color=gray>(%s)</font>", userName), ip);
            throw new BusinessException(ErrorCode.ACCOUNT_BANED);
        }
        // 记录用户的登录态
        request.getSession().setAttribute(USER_LOGIN_STATE, user);
        return user;
    }

    /**
     * 获取当前登录用户
     */
    @Override
    public User getLoginUser(HttpServletRequest request) {
        // 先判断是否已登录
        Object userObj = request.getSession().getAttribute(USER_LOGIN_STATE);
        User currentUser = (User) userObj;
        if (currentUser == null || currentUser.getId() == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        // 从数据库查询（追求性能的话可以注释，直接走缓存）
        long userId = currentUser.getId();
        String oldPass = currentUser.getUserPassword();
        currentUser = this.getById(userId);
        if (currentUser == null || !currentUser.getUserPassword().equals(oldPass)) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        if (currentUser.getIsBaned() != 0) {
            throw new BusinessException(ErrorCode.ACCOUNT_BANED);
        }
        currentUser.setUserKey(null);
        return currentUser;
    }

    /**
     * 是否为管理员
     */
    @Override
    public boolean isAdmin(HttpServletRequest request) {
        // 仅管理员可查询
        Object userObj = request.getSession().getAttribute(USER_LOGIN_STATE);
        User user = (User) userObj;
        return user != null && ADMIN_ROLE.equals(user.getUserRole());
    }

    @Override
    public boolean isAdmin(long id) {
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("id", id);
        User user = userMapper.selectOne(queryWrapper);
        if (user == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户不存在");
        }
        return ADMIN_ROLE.equals(user.getUserRole());
    }

    /**
     * 用户注销
     */
    @Override
    public boolean userLogout(HttpServletRequest request) {
        if (request.getSession().getAttribute(USER_LOGIN_STATE) == null) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "未登录");
        }
        // 移除登录态
        request.getSession().removeAttribute(USER_LOGIN_STATE);
        return true;
    }

    @Override
    public boolean checkDuplicates(String userName) {
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userName", userName);
        long count = userMapper.selectCount(queryWrapper);
        if (count > 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号重复");
        }
        return false;
    }
}
