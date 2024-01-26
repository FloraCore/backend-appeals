package net.kinomc.appeals.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import net.kinomc.appeals.common.ErrorCode;
import net.kinomc.appeals.exception.BusinessException;
import net.kinomc.appeals.mapper.PhotosMapper;
import net.kinomc.appeals.model.entity.Photos;
import net.kinomc.appeals.service.PhotosService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.util.UUID;

@Service
@Slf4j
public class PhotosServiceImpl extends ServiceImpl<PhotosMapper, Photos> implements PhotosService {
    @Resource
    private PhotosMapper photosMapper;

    @Override
    public String getPhotoByUUID(UUID uuid) {
        QueryWrapper<Photos> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("uuid", uuid.toString());
        Photos photos = photosMapper.selectOne(queryWrapper);
        if (photos == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "无记录的图片数据");
        }
        return photos.getData();
    }

    @Override
    public String getPhotoByID(long id) {
        QueryWrapper<Photos> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("id", id);
        Photos photos = photosMapper.selectOne(queryWrapper);
        if (photos == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "无记录的图片数据");
        }
        return photos.getData();
    }

    @Override
    public Photos addPhoto(String data) {
        if (StringUtils.isAnyBlank(data)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "图片为空");
        }
        synchronized (data.intern()) {
            Photos photos = new Photos();
            photos.setData(data);
            UUID uuid = UUID.randomUUID();
            QueryWrapper<Photos> queryWrapper = new QueryWrapper<>();
            String md5 = DigestUtils.md5DigestAsHex(data.getBytes());
            queryWrapper.eq("md5", md5);
            Photos p1 = photosMapper.selectOne(queryWrapper);
            if (p1 != null) {
                return p1;
            }
            photos.setMd5(md5);
            queryWrapper.eq("uuid", uuid.toString());
            photos.setUuid(uuid.toString());
            boolean saveResult = this.save(photos);
            if (!saveResult) {
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, "添加失败，数据库错误");
            }
            return photos;
        }
    }
}
