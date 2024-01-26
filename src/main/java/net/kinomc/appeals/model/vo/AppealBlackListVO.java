package net.kinomc.appeals.model.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * @author xLikeWATCHDOG
 */
@Data
public class AppealBlackListVO implements Serializable {
    private static final long serialVersionUID = 1L;
    private Long id;
    private String uuid;
    private String name;
    private Long assignee;
    private String assigneeName;
    private String reason;
    private Date createTime;
}
