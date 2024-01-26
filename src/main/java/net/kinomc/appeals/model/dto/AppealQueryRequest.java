package net.kinomc.appeals.model.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;
import net.kinomc.appeals.common.PageRequest;

import java.io.Serializable;

@EqualsAndHashCode(callSuper = true)
@Data
public class AppealQueryRequest extends PageRequest implements Serializable {

    private static final long serialVersionUID = 1L;
    private Long id;
    private String userName;
    private String appealType;
    private Integer state;
}
