package net.kinomc.appeals.redis;

import jakarta.annotation.Resource;
import net.kinomc.appeals.AppealsApplication;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.concurrent.TimeUnit;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {AppealsApplication.class})
public class RedisTests {
    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Test
    public void addRedisString() {
        stringRedisTemplate.opsForValue().set("redis", "1fh3f", 10, TimeUnit.SECONDS);
        System.out.println("success");
    }
}
