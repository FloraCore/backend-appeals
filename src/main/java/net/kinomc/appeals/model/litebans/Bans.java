package net.kinomc.appeals.model.litebans;

import lombok.Data;

import java.io.Serializable;
import java.util.UUID;

@Data
public class Bans implements Serializable {
    private static final long serialVersionUID = 1L;
    private long id;
    private UUID uuid;
    private String reason;
    private String bannedByName;
    private long time;
    /**
     * <= 0 为永久。
     */
    private long until;
    private int active;
}
