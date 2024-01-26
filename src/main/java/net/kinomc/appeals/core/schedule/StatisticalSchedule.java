package net.kinomc.appeals.core.schedule;

import jakarta.annotation.Resource;
import net.kinomc.appeals.service.AppealService;
import net.kinomc.appeals.service.WechatBotService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class StatisticalSchedule {
    @Resource
    private WechatBotService wechatBotService;
    @Resource
    private AppealService appealService;

    @Scheduled(cron = "00 59 23 * * ?")
    public void sendDailyStatisticsNotice() {
        wechatBotService.sendDailyStatisticsNotice(appealService.getTheNumberOfNewAppealsToday());
    }
}
