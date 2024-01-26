package net.kinomc.appeals.model.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 用户创建请求
 */
@Data
public class AppealAddRequest implements Serializable {
    private static final long serialVersionUID = 1L;
    /**
     * 用户名
     */
    private String userName;
    /**
     * 用户邮箱
     */
    private String userEmail;
    /**
     * 申诉理由
     */
    private String appealReason;
    /**
     * 图片Base64列表
     */
    private String[] photos;
}