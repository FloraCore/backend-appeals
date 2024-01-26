package net.kinomc.appeals.user;

import jakarta.annotation.Resource;
import net.kinomc.appeals.AppealsApplication;
import net.kinomc.appeals.common.ErrorCode;
import net.kinomc.appeals.exception.BusinessException;
import net.kinomc.appeals.model.entity.User;
import net.kinomc.appeals.service.UserService;
import net.kinomc.appeals.utils.GoogleAuthenticator;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.DigestUtils;

import java.util.Random;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {AppealsApplication.class})
public class UserTests {
    private static final String SALT = "kinomc";
    @Resource
    private UserService userService;
    @Value("${web.server-name:KinoMC}")
    private String serverName;
    @Value("${web.address:appeal.kinomc.net}")
    private String address;

    @Test
    public void addUser() {
        String userName = "124124151";
        String userEmail = "86328425@qq.com";
        String userPassword = "woskxn2023";
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
        System.out.println("注册成功！");
        System.out.println("用户名：" + userName);
        System.out.println("密码：" + userPassword);
        System.out.println("邮箱：" + userEmail);
        System.out.println("2FAKey：" + userKey);
        System.out.println("2FA-QR：" + GoogleAuthenticator.getQRBarcode(userName, userKey, serverName, address));
        System.out.println("二维码：/qrcode?text=" + GoogleAuthenticator.getQRBarcode(userName, userKey, serverName, address));
        System.out.println("用户认证：" + userRole);
    }

    public String getRandomPassword(int len) {
        String result = this.makeRandomPassword(len);
        if (result.matches(".*[a-z]{1,}.*") && result.matches(".*[A-Z]{1,}.*") && result.matches(".*\\d{1,}.*") && result.matches(".*[~!@#$%^&*\\.?]{1,}.*")) {
            return result;
        }
        result = makeRandomPassword(len);
        return result;
    }

    public String makeRandomPassword(int len) {
        char[] charr = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890~!@#$%^&*.?".toCharArray();
        StringBuilder sb = new StringBuilder();
        Random r = new Random();
        for (int x = 0; x < len; ++x) {
            sb.append(charr[r.nextInt(charr.length)]);
        }
        return sb.toString();
    }
}
