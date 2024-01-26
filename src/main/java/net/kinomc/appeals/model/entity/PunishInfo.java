package net.kinomc.appeals.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;

@TableName(value = "punish_info")
@Data
public class PunishInfo implements Serializable {
    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
    /**
     * id
     */
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long punishID;
    private String reason;
    private String bannedByName;
    private String type;
    private long time;
    /**
     * <= 0 为永久。
     */
    private long until;
    /**
     * 是否删除
     */
    @TableLogic
    private Integer isDelete;
}
