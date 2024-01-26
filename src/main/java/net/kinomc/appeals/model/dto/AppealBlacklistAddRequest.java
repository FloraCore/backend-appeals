package net.kinomc.appeals.model.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class AppealBlacklistAddRequest implements Serializable {
    private static final long serialVersionUID = 1L;
    private String userName;
    private Long assignee;
    private String reason;
}
