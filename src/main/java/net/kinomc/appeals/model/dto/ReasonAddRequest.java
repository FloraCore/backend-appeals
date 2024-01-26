package net.kinomc.appeals.model.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 理由创建请求
 */
@Data
public class ReasonAddRequest implements Serializable {
    private static final long serialVersionUID = 1L;
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
}