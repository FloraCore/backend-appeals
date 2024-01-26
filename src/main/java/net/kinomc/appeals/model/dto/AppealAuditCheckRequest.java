package net.kinomc.appeals.model.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class AppealAuditCheckRequest implements Serializable {
    private static final long serialVersionUID = 1L;
    private Long appealID;
    private Long userID;
}