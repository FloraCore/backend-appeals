package net.kinomc.appeals.model.dto;

import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;

import java.io.Serializable;

/**
 * 用户更新请求
 */
@Data
public class UserUpdateRequest implements Serializable {
    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
    /**
     * id
     */
    private Long id;
    /**
     * 用户名
     */
    private String userName;
    /**
     * 用户邮箱
     */
    private String userEmail;
    /**
     * 用户头像
     */
    private String userAvatar;
    /**
     * 性别
     */
    private Integer userGender;
    /**
     * 用户职位：
     * unknown：未知；
     * helper：志愿者；
     * mod：客服：
     * admin：管理；
     * executive：高管；
     * development；开发
     * owner：服主
     */
    private String userPosition;
    /**
     * 用户角色: user, admin
     */
    private String userRole;
    /**
     * 密码
     */
    private String userPassword;
    /**
     * 是否接受邮件，1为接受
     */
    private Integer acceptMail;
    /**
     * 是否封禁，1为封禁
     */
    private Integer isBaned;
}