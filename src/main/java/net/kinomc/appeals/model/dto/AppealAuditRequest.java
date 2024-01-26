package net.kinomc.appeals.model.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class AppealAuditRequest implements Serializable {
    private static final long serialVersionUID = 1L;
    /**
     * 申诉ID
     */
    private Long id;
    /**
     * 结论：通过(0)/不通过(1)/无效(2)
     */
    private Integer conclusion;
    /**
     * 结论理由
     */
    private String conclusionReason;
}