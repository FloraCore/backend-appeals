package net.kinomc.appeals.service;

import com.baomidou.mybatisplus.extension.service.IService;
import jakarta.servlet.http.HttpServletRequest;
import net.kinomc.appeals.model.entity.User;

/**
 * 用户服务
 */
public interface UserService extends IService<User> {

    /**
     * 用户登录
     *
     * @param userName     用户名
     * @param userPassword 用户密码
     * @param cookie       是否使用cookie登录
     * @return 脱敏后的用户信息
     */
    User userLogin(String userName, String userPassword, boolean cookie, HttpServletRequest request);

    /**
     * 获取当前登录用户
     */
    User getLoginUser(HttpServletRequest request);

    /**
     * 是否为管理员
     */
    boolean isAdmin(HttpServletRequest request);

    /**
     * 是否为管理员
     */
    boolean isAdmin(long id);

    /**
     * 用户注销
     */
    boolean userLogout(HttpServletRequest request);

    boolean checkDuplicates(String userName);
}
