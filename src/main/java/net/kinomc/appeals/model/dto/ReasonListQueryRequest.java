package net.kinomc.appeals.model.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;
import net.kinomc.appeals.common.PageRequest;

import java.io.Serializable;
import java.util.Date;

@EqualsAndHashCode(callSuper = true)
@Data
public class ReasonListQueryRequest extends PageRequest implements Serializable {
    private static final long serialVersionUID = 1L;
    /**
     * id
     */
    private Long id;
    /**
     * 理由标题
     */
    private String title;
    /**
     * 结论理由
     */
    private String reason;
    /**
     * 结论：通过(0)/不通过(1)/无效(2)
     */
    private Integer type;
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