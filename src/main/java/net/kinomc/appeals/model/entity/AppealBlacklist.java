package net.kinomc.appeals.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@TableName(value = "appeal_blacklist")
@Data
public class AppealBlacklist implements Serializable {
    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
    /**
     * id
     */
    @TableId(type = IdType.AUTO)
    private Long id;
    /**
     * 玩家UUID，在litebans数据库
     */
    private String uuid;
    /**
     * STAFF-ID
     */
    private Long assignee;
    private String reason;
    /**
     * 创建时间
     */
    private Date createTime;
    /**
     * 是否删除
     */
    @TableLogic
    private Integer isDelete;
}
