package net.kinomc.appeals.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@TableName(value = "photos")
@Data
public class Photos implements Serializable {
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
     * 图片Base64数据
     */
    private String data;
    /**
     * 图片MD5校验
     */
    private String md5;
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
