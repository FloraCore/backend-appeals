package net.kinomc.appeals.mail;

import net.kinomc.appeals.AppealsApplication;
import net.kinomc.appeals.service.MailService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {AppealsApplication.class})
public class MailServiceTest {
    @Autowired
    private MailService mailService;

    @Test
    public void testSend() {
        //mailService.sendVerificationCode("86328425@qq.com",  1);
    }
}
