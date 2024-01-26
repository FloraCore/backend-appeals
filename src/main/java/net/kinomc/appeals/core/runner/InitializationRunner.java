package net.kinomc.appeals.core.runner;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import net.kinomc.appeals.common.ErrorCode;
import net.kinomc.appeals.exception.BusinessException;
import net.kinomc.appeals.model.entity.User;
import net.kinomc.appeals.service.UserService;
import net.kinomc.appeals.utils.GoogleAuthenticator;
import net.kinomc.appeals.utils.RandomStringGenerator;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.util.DigestUtils;

import java.util.List;

import static net.kinomc.appeals.constant.UserConstant.SALT;

@Component
@Slf4j
public class InitializationRunner implements ApplicationRunner {
    @Resource
    private UserService userService;
    @Value("${web.server-name:KinoMC}")
    private String serverName;
    @Value("${web.address:appeal.kinomc.net}")
    private String address;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        List<User> userList = userService.list(queryWrapper);
        if (userList.isEmpty()) {
            log.warn("检测到当前用户数据库未存在账户，正在创建管理员账户。");
            String userName = "admin";
            String userEmail = "admin@qq.com";
            String userPassword = RandomStringGenerator.generate(10);
            //用户角色: user, admin
            String userRole = "admin";
            String userKey = GoogleAuthenticator.generateSecretKey();
            String encryptPassword = DigestUtils.md5DigestAsHex((SALT + userPassword).getBytes());
            User user = new User();
            user.setUserName(userName);
            user.setUserEmail(userEmail);
            user.setUserPassword(encryptPassword);
            user.setUserKey(userKey);
            user.setUserRole(userRole);
            boolean result = userService.save(user);
            if (!result) {
                throw new BusinessException(ErrorCode.OPERATION_ERROR);
            }
            log.info("管理员账户创建成功！");
            log.info("用户名：" + userName);
            log.info("密码：" + userPassword);
            log.info("邮箱：" + userEmail);
            log.info("2FAKey：" + userKey);
            log.info("2FA-QR：" + GoogleAuthenticator.getQRBarcode(userName, userKey, serverName, address));
            log.info("二维码：/qrcode?text=" + GoogleAuthenticator.getQRBarcode(userName, userKey, serverName, address));
            log.warn("请前往二维码生成网站，将上述二维码后面的内容粘贴进去以生成2FA二维码。");
            log.warn("请网站管理员在登录后及时修改账户信息（用户名、密码、邮箱等）。");
            log.warn("此信息只显示一次，请妥善保管！");
        }
    }
}
