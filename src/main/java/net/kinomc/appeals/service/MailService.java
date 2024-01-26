package net.kinomc.appeals.service;

import org.springframework.scheduling.annotation.Async;

public interface MailService {
    @Async
    void sendCreateAppealNotice(String to, String appealType, String userName, long id);

    @Async
    void sendAppealAcceptNotice(String to, String appealType, String userName, long id, long assignee);

    @Async
    void sendAppealAuditNotice(String to, String appealType, String userName, long id, int conclusion, String conclusionReason);

    @Async
    void sendVerificationCode(String to, int id, String code);

    boolean checkVerificationCode(int id, String code);

    @Async
    void sendCreateAppealNoticeToUsers(String appealType, String userName, long id);
}
