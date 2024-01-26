package net.kinomc.appeals.model.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 用户视图
 *
 * @TableName user
 */
@Data
public class UserVO implements Serializable {
    private static final long serialVersionUID = 1L;
    /**
     * id
     */
    private Long id;
    /**
     * 用户昵称
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
     * 2FA
     */
    private String userKey;
    /**
     * 创建时间
     */
    private Date createTime;
    /**
     * 更新时间
     */
    private Date updateTime;
    /**
     * 是否封禁，1为封禁
     */
    private Integer isBaned;
    /**
     * 是否接受邮件，1为接受
     */
    private Integer acceptMail;
    /**
     * 只有在调用登录接口的时候才会有数值。
     */
    private String token;
    /**
     * 是否删除
     */
    private Integer isDelete;
}