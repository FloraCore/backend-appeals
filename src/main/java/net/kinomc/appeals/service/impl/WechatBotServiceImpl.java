package net.kinomc.appeals.service.impl;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import net.kinomc.appeals.service.IPLocationService;
import net.kinomc.appeals.service.WechatBotService;
import net.kinomc.appeals.utils.WeChatBotUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.Date;

@Service
@Slf4j
public class WechatBotServiceImpl implements WechatBotService {
    @Value("${wechat.robot.webhook:https://qyapi.weixin.qq.com/cgi-bin/webhook/send?key=8a43745c-4d96-4bea-b30a-ef73e70e8968}")
    private String botUrl;
    @Resource
    private IPLocationService ipLocationService;

    @Override
    @Async
    public void sendUserLoginNotice(String userName, boolean success, boolean cookie, String failedReason, String ip) {
        String i = """
                ## 用户登录告警
                > **用户名：**%s
                > **登录时间：**%s
                > **登录IP：**%s
                > **归属地：**%s
                > **登录方式：**%s
                > **登录状态：**%s""";
        String status = success ? "<font color=green>成功</font>" : "<font color=red>失败</font>\n" + "> **失败原因：**" + failedReason;
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date = new Date(System.currentTimeMillis());
        String type = cookie ? "自动登录" : "账号密码登录";
        String msg = String.format(i, userName, formatter.format(date), ip, ipLocationService.getIPLocation(ip), type, status);
        try {
            WeChatBotUtils.sendMarkdownMsg(botUrl, msg);
        } catch (Exception e) {
            log.warn("信息推送失败！");
        }
    }

    @Override
    @Async
    public void sendNewAppealNotice(String userName, String appealType, long id) {
        String i = """
                ## 申诉创建通知
                > **用户名：**%s
                > **申诉类型：**%s
                > **申诉ID：#**%s
                > **申诉时间：**%s""";
        String type = appealType.equals("punish") ? "处罚申诉" : "异常申诉";
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date = new Date(System.currentTimeMillis());
        String msg = String.format(i, userName, type, id, formatter.format(date));
        try {
            WeChatBotUtils.sendMarkdownMsg(botUrl, msg);
        } catch (Exception e) {
            log.warn("信息推送失败！");
        }
    }

    @Override
    @Async
    public void sendAcceptAppealNotice(String userName, String appealType, long id, String assignee) {
        String i = """
                ## 申诉受理通知
                > **用户名：**%s
                > **申诉类型：**%s
                > **申诉ID：#**%s
                > **受理人：**%s
                > **受理时间：**%s""";
        String type = appealType.equals("punish") ? "处罚申诉" : "异常申诉";
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date = new Date(System.currentTimeMillis());
        String msg = String.format(i, userName, type, id, assignee, formatter.format(date));
        try {
            WeChatBotUtils.sendMarkdownMsg(botUrl, msg);
        } catch (Exception e) {
            log.warn("信息推送失败！");
        }
    }

    @Override
    @Async
    public void sendAuditAppealNotice(String userName, String appealType, long id, String assignee, int conclusion, String conclusionReason) {
        String i = """
                ## 申诉处理通知
                > **用户名：**%s
                > **申诉类型：**%s
                > **申诉ID：#**%s
                > **受理人：**%s
                > **结论：**%s
                > **理由：**%s
                > **处理时间：**%s""";
        String type = appealType.equals("punish") ? "处罚申诉" : "异常申诉";
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date = new Date(System.currentTimeMillis());
        String status = conclusion == 0 ? "<font color=green>通过</font>" : conclusion == 1 ? "<font color=red>不通过</font>" : "<font color=gray>无效</font>";
        String msg = String.format(i, userName, type, id, assignee, status, conclusionReason, formatter.format(date));
        try {
            WeChatBotUtils.sendMarkdownMsg(botUrl, msg);
        } catch (Exception e) {
            log.warn("信息推送失败！");
        }
    }

    @Override
    @Async
    public void sendDailyStatisticsNotice(int count) {
        String i = """
                ## 每日统计
                > **今日申诉数：**%s""";
        String msg = String.format(i, count);
        try {
            WeChatBotUtils.sendMarkdownMsg(botUrl, msg);
        } catch (Exception e) {
            log.warn("信息推送失败！");
        }
    }

    @Override
    public void sendStartNotice() {
        String i = """
                ## 申诉系统-启动通知
                > **服务器主机名：**%s
                > **时间：**%s""";
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date = new Date(System.currentTimeMillis());
        InetAddress localHost;
        String hostName = "UNKNOWN";
        try {
            localHost = InetAddress.getLocalHost();
            hostName = localHost.getHostName();
        } catch (UnknownHostException e) {
            log.warn("无法获取主机IP地址。");
        }
        String msg = String.format(i, hostName, formatter.format(date));
        try {
            WeChatBotUtils.sendMarkdownMsg(botUrl, msg);
        } catch (Exception e) {
            log.warn("信息推送失败！");
        }
    }
}
