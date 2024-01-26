package net.kinomc.appeals.model.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
public class AppealVO implements Serializable {
    private static final long serialVersionUID = 1L;
    private Long id;
    private String userName;
    private String appealType;
    private String uuid;
    private Date createTime;
    private Integer state;
}
