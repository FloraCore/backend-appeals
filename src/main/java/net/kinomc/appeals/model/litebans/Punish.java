package net.kinomc.appeals.model.litebans;

import lombok.Data;

@Data
public class Punish {
    private static final long serialVersionUID = 1L;
    private String type;
    private long id;
    private String reason;
    private String bannedByName;
    private long time;
    /**
     * <= 0 为永久。
     */
    private long until;

}
