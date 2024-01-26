package net.kinomc.appeals.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@TableName(value = "ip_location")
@Data
public class IPLocation implements Serializable {
    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
    /**
     * id
     */
    @TableId(type = IdType.AUTO)
    private Long id;
    /**
     * ip地址
     */
    private String ip;
    /**
     * 国家（极少为空，当此项为空时会通过ip138查询）
     */
    private String country;
    /**
     * 省份/自治区/直辖市（少数为空，当此项为空时会通过ip138查询）
     */
    private String region;
    /**
     * 地级市（部分为空，当此项为空时会通过ip138查询）
     */
    private String city;
    /**
     * 区/县（部分为空）
     */
    private String district;
    /**
     * 运营商
     */
    private String isp;
    /**
     * 邮政编码
     */
    private String zip;
    /**
     * 地区区号
     */
    private String zone;
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
