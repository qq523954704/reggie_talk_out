package com.itheima.reggie;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.concurrent.TimeUnit;

@SpringBootTest
public class TestRedis {

    @Autowired
    private RedisTemplate redisTemplate;

    @Test
    public void redisTest(){

        redisTemplate.opsForValue().set("name","张三",10l, TimeUnit.SECONDS);//设置string类型,存10s
        String name = (String) redisTemplate.opsForValue().get("name");
        System.out.println(name);
    }

}
