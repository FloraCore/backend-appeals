package net.kinomc.appeals.model.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * md更新请求
 */
@Data
public class MarkdownUpdateRequest implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;
    /**
     * 类型目前分为两个。0表示主页公告；1表示规则公告
     */
    private Integer type;
    /**
     * 标题
     */
    private String title;
    /**
     * 内容
     */
    private String content;
    /**
     * 是否置顶这个md：否(0)/是(1)
     */
    private Integer top;
    /**
     * 是否启用md（若未启用，则在获取时将会自动过滤）：否(0)/是(1)
     */
    private Integer enabled;
}
