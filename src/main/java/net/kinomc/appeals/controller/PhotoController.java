package net.kinomc.appeals.controller;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import net.kinomc.appeals.common.BaseResponse;
import net.kinomc.appeals.common.ErrorCode;
import net.kinomc.appeals.common.ResultUtils;
import net.kinomc.appeals.exception.BusinessException;
import net.kinomc.appeals.service.PhotosService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/**
 * 图片接口
 */
@RestController
@RequestMapping("/photo")
@Slf4j
public class PhotoController {
    @Resource
    private PhotosService photosService;

    @GetMapping("/get")
    public BaseResponse<String> getPhoto(String uuid) {
        try {
            UUID u = UUID.fromString(uuid);
            return ResultUtils.success(photosService.getPhotoByUUID(u));
        } catch (IllegalArgumentException e) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "无效的UUID");
        }
    }
}
