package net.kinomc.appeals.model.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class AppealCheckRequest implements Serializable {
    private static final long serialVersionUID = 1L;
    private String userName;
    private Long id;
}