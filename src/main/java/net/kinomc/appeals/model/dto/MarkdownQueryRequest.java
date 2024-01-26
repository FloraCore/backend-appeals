package net.kinomc.appeals.model.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;
import net.kinomc.appeals.common.PageRequest;

import java.io.Serializable;

@EqualsAndHashCode(callSuper = true)
@Data
public class MarkdownQueryRequest extends PageRequest implements Serializable {

    private static final long serialVersionUID = 1L;
    private Long id;
    /**
     * 类型目前分为两个。0表示主页公告；1表示规则公告
     */
    private Integer type;
    ;
    /**
     * 标题
     */
    private String title;
    /**
     * 是否置顶这个md：否(0)/是(1)
     */
    private Integer top;
    /**
     * 是否启用md（若未启用，则在获取时将会自动过滤）：否(0)/是(1)
     */
    private Integer enabled;
}
