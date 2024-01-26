package net.kinomc.appeals.service;

import org.springframework.scheduling.annotation.Async;

public interface WechatBotService {
    /**
     * @param userName     用户名
     * @param success      是否登录成功
     * @param failedReason 如果登录成功，这里填null；如果失败，则为失败原因。
     */
    @Async
    void sendUserLoginNotice(String userName, boolean success, boolean cookie, String failedReason, String ip);

    @Async
    void sendNewAppealNotice(String userName, String appealType, long id);

    @Async
    void sendAcceptAppealNotice(String userName, String appealType, long id, String assignee);

    @Async
    void sendAuditAppealNotice(String userName, String appealType, long id, String assignee, int conclusion, String conclusionReason);

    @Async
    void sendDailyStatisticsNotice(int count);

    @Async
    void sendStartNotice();
}
