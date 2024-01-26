package net.kinomc.appeals.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@TableName(value = "appeal_punish")
@Data
public class AppealPunish implements Serializable {
    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
    /**
     * id
     */
    @TableId(type = IdType.AUTO)
    private Long id;
    /**
     * 处罚信息ID
     */
    private Long punishInfo;
    /**
     * UUID，与详情表中的UUID一致
     */
    private String uuid;
    /**
     * 用户昵称
     */
    private String userName;
    /**
     * 用户邮箱
     */
    private String userEmail;
    /**
     * 申诉理由
     */
    private String appealReason;
    /**
     * 图片数据库UUID
     */
    private String photos;
    /**
     * STAFF-ID
     */
    private Long assignee;
    /**
     * 受理时间
     */
    private Date processingTime;
    /**
     * 结论：通过(0)/不通过(1)/无效(2)
     */
    private Integer conclusion;
    /**
     * 结论理由
     */
    private String conclusionReason;
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
