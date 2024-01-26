package net.kinomc.appeals.model.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class LitebansExecuteRequest implements Serializable {
    private static final long serialVersionUID = 1L;
    private String userName;
    private Integer type;
    private String reason;
    private String date;
}