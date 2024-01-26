package net.kinomc.appeals.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import net.kinomc.appeals.mapper.MarkdownsMapper;
import net.kinomc.appeals.model.entity.Markdowns;
import net.kinomc.appeals.service.MarkdownsService;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class MarkdownsServiceImpl extends ServiceImpl<MarkdownsMapper, Markdowns> implements MarkdownsService {
    @Resource
    private MarkdownsMapper markdownsMapper;
}
