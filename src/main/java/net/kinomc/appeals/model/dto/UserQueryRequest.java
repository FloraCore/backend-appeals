package net.kinomc.appeals.model.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;
import net.kinomc.appeals.common.PageRequest;

import java.io.Serializable;
import java.util.Date;

@EqualsAndHashCode(callSuper = true)
@Data
public class UserQueryRequest extends PageRequest implements Serializable {
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
     * 账号
     */
    private String userAccount;
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
}