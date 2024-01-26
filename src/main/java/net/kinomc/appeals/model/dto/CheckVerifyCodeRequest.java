package net.kinomc.appeals.model.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class CheckVerifyCodeRequest implements Serializable {
    private static final long serialVersionUID = 1L;
    private Integer id;
    private String code;
}
