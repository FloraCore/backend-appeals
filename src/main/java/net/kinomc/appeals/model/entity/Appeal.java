package net.kinomc.appeals.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@TableName(value = "appeal")
@Data
public class Appeal implements Serializable {
    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
    /**
     * id
     */
    @TableId(type = IdType.AUTO)
    private Long id;
    private String userName;
    /**
     * 申诉类型：punish/account
     */
    private String appealType;
    /**
     * UUID，与详情表中的UUID一致
     */
    private String uuid;
    /**
     * 创建时间
     */
    private Date createTime;
    /**
     * 受理状态：未受理(0)/受理(1)/已处理(2)
     */
    private Integer state;
    /**
     * 是否删除
     */
    @TableLogic
    private Integer isDelete;
}
