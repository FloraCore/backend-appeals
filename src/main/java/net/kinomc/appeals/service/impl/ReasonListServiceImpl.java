package net.kinomc.appeals.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import net.kinomc.appeals.mapper.ReasonListMapper;
import net.kinomc.appeals.model.entity.ReasonList;
import net.kinomc.appeals.service.ReasonListService;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class ReasonListServiceImpl extends ServiceImpl<ReasonListMapper, ReasonList> implements ReasonListService {
    @Resource
    private ReasonListMapper reasonListMapper;

}
