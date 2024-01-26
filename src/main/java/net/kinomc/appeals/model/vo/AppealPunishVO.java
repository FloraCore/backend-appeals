package net.kinomc.appeals.model.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
public class AppealPunishVO implements Serializable {
    private static final long serialVersionUID = 1L;
    private Long id;
    private Long punishInfo;
    private String uuid;
    private String userName;
    private String userEmail;
    private String appealReason;
    private String photos;
    private Long assignee;
    private Date processingTime;
    private Integer conclusion;
    private String conclusionReason;
    private Date updateTime;
}
