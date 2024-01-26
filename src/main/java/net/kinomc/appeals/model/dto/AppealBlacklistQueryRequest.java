package net.kinomc.appeals.model.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;
import net.kinomc.appeals.common.PageRequest;

import java.io.Serializable;
import java.util.Date;

@EqualsAndHashCode(callSuper = true)
@Data
public class AppealBlacklistQueryRequest extends PageRequest implements Serializable {

    private static final long serialVersionUID = 1L;
    private Long id;
    /**
     * 玩家UUID，在litebans数据库
     */
    private String uuid;
    private String name;
    /**
     * STAFF-ID
     */
    private Long assignee;
    private String assigneeName;
    private String reason;
    /**
     * 创建时间
     */
    private Date createTime;
}
