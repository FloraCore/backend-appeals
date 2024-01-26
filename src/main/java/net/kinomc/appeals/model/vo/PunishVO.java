package net.kinomc.appeals.model.vo;

import lombok.Data;

import java.io.Serializable;

@Data
public class PunishVO implements Serializable {
    private static final long serialVersionUID = 1L;
    private long id;
    private String reason;
    private String bannedByName;
    private String type;
}