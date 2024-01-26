package net.kinomc.appeals.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@TableName(value = "markdowns")
@Data
public class Markdowns implements Serializable {
    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
    /**
     * id
     */
    @TableId(type = IdType.AUTO)
    private Long id;
    /**
     * UUID，与详情表中的UUID一致
     */
    private String uuid;
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
     * 创建这个md的人(id)
     */
    private Long creator;
    /**
     * 参与修改这个md的人(ids)
     */
    private String participant;
    /**
     * 是否置顶这个md：否(0)/是(1)
     */
    private Integer top;
    /**
     * 是否启用md（若未启用，则在获取时将会自动过滤）：否(0)/是(1)
     */
    private Integer enabled;
    /**
     * 创建时间
     */
    private Date createTime;
    /**
     * 更新时间
     */
    private Date updateTime;
    /**
     * 是否删除
     */
    @TableLogic
    private Integer isDelete;
}
