package net.kinomc.appeals.model.dto;

import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;

import java.io.Serializable;

/**
 * 理由更新请求
 */
@Data
public class ReasonUpdateRequest implements Serializable {
    @TableField(exist = false)
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
}