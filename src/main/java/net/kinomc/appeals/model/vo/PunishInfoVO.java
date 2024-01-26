package net.kinomc.appeals.model.vo;

import lombok.Data;

import java.io.Serializable;

@Data
public class PunishInfoVO implements Serializable {
    private static final long serialVersionUID = 1L;
    private long id;
    private long punishID;
    private String reason;
    private String bannedByName;
    private String type;
    private long time;
    /**
     * <= 0 为永久。
     */
    private long until;
}