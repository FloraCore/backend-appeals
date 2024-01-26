package net.kinomc.appeals.core.runner;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import net.kinomc.appeals.service.WechatBotService;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class StartRunner implements ApplicationRunner {
    @Resource
    private WechatBotService wechatBotService;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        wechatBotService.sendStartNotice();
    }
}
