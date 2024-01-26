package net.kinomc.appeals.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import jakarta.annotation.Resource;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import net.kinomc.appeals.model.entity.User;
import net.kinomc.appeals.service.MailService;
import net.kinomc.appeals.service.UserService;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.List;

@Service
@Slf4j
public class MailServiceImpl implements MailService {
    @Resource
    private UserService userService;
    @Resource
    private JavaMailSender javaMailSender;
    @Resource
    private TemplateEngine templateEngine;
    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Override
    @Async
    public void sendCreateAppealNotice(String to, String appealType, String userName, long id) {
        MimeMessage message = javaMailSender.createMimeMessage();
        try {
            MimeMessageHelper helper = new MimeMessageHelper(message, true);
            helper.setFrom("appeals_reply@yeah.net");
            helper.setTo(to);
            helper.setSubject("申诉系统-申诉成功");
            // 解析邮件模板
            Context context = new Context();
            context.setVariable("appealType", "申诉类型：" + (appealType.equals("punish") ? "处罚申诉" : "异常申诉"));
            context.setVariable("userName", "玩家：" + userName);
            context.setVariable("appealID", "申诉ID：#" + id);
            String text = templateEngine.process("CreateAppealNotice", context);
            helper.setText(text, true);
            javaMailSender.send(message);
        } catch (Exception e) {
            e.printStackTrace();
        }
        sendCreateAppealNoticeToUsers(appealType, userName, id);
    }

    @Override
    @Async
    public void sendAppealAcceptNotice(String to, String appealType, String userName, long id, long assignee) {
        MimeMessage message = javaMailSender.createMimeMessage();
        try {
            MimeMessageHelper helper = new MimeMessageHelper(message, true);
            helper.setFrom("appeals_reply@yeah.net");
            helper.setTo(to);
            helper.setSubject("申诉系统-受理成功");
            // 解析邮件模板
            Context context = new Context();
            context.setVariable("appealType", "申诉类型：" + (appealType.equals("punish") ? "处罚申诉" : "异常申诉"));
            context.setVariable("userName", "玩家：" + userName);
            context.setVariable("appealID", "申诉ID：#" + id);
            User user = userService.getById(assignee);
            context.setVariable("assignee", "受理人：" + user.getUserName());
            String text = templateEngine.process("AppealAcceptNotice", context);
            helper.setText(text, true);
            javaMailSender.send(message);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    @Async
    public void sendAppealAuditNotice(String to, String appealType, String userName, long id, int conclusion, String conclusionReason) {
        MimeMessage message = javaMailSender.createMimeMessage();
        try {
            MimeMessageHelper helper = new MimeMessageHelper(message, true);
            helper.setFrom("appeals_reply@yeah.net");
            helper.setTo(to);
            helper.setSubject("申诉系统-结论通知");
            // 解析邮件模板
            Context context = new Context();
            context.setVariable("appealType", "申诉类型：" + (appealType.equals("punish") ? "处罚申诉" : "异常申诉"));
            context.setVariable("userName", "玩家：" + userName);
            context.setVariable("appealID", "申诉ID：#" + id);
            context.setVariable("conclusion", "结论：" + (conclusion == 0 ? "通过" : conclusion == 1 ? "不通过" : "无效"));
            context.setVariable("conclusionReason", "结论理由：" + conclusionReason);
            String text = templateEngine.process("AppealAuditNotice", context);
            helper.setText(text, true);
            javaMailSender.send(message);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    @Async
    public void sendVerificationCode(String to, int id, String code) {
        stringRedisTemplate.opsForValue().set(String.valueOf(id), code);
        MimeMessage message = javaMailSender.createMimeMessage();
        try {
            MimeMessageHelper helper = new MimeMessageHelper(message, true);
            helper.setFrom("appeals_reply@yeah.net");
            helper.setTo(to);
            helper.setSubject("申诉系统-验证码");
            helper.setCc("appeals_reply@yeah.net");
            // 解析邮件模板
            Context context = new Context();
            context.setVariable("verifyCode", code);
            String text = templateEngine.process("VerificationCode", context);
            helper.setText(text, true);
            javaMailSender.send(message);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean checkVerificationCode(int id, String code) {
        String code1 = stringRedisTemplate.opsForValue().get(String.valueOf(id));
        if (code1 == null) return false;
        return code1.equals(code);
    }

    @Override
    @Async
    public void sendCreateAppealNoticeToUsers(String appealType, String userName, long id) {
        User userQuery = new User();
        userQuery.setAcceptMail(1);
        QueryWrapper<User> queryWrapper = new QueryWrapper<>(userQuery);
        List<User> userList = userService.list(queryWrapper);
        for (User user : userList) {
            String userEmail = user.getUserEmail();
            MimeMessage message = javaMailSender.createMimeMessage();
            try {
                MimeMessageHelper helper = new MimeMessageHelper(message, true);
                helper.setFrom("appeals_reply@yeah.net");
                helper.setTo(userEmail);
                helper.setSubject("申诉系统-有一个新的申诉");
                // 解析邮件模板
                Context context = new Context();
                context.setVariable("appealType", "申诉类型：" + (appealType.equals("punish") ? "处罚申诉" : "异常申诉"));
                context.setVariable("userName", "玩家：" + userName);
                context.setVariable("appealID", "申诉ID：#" + id);
                String text = templateEngine.process("CreateAppealNoticeToUsers", context);
                helper.setText(text, true);
                javaMailSender.send(message);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
