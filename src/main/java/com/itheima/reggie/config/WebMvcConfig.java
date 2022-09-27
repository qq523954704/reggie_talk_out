package com.itheima.reggie.config;

import com.itheima.reggie.common.JacksonObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport;

import java.util.List;

/**
 * 静态资源映射配置类
 */
@Slf4j
@Configuration
public class WebMvcConfig extends WebMvcConfigurationSupport {
    /**
     * 设置静态资源
     *
     * @param registry
     */
    @Override
    protected void addResourceHandlers(ResourceHandlerRegistry registry) {
        log.info("开始静态资源映射配");

        //请求路径xxx/backed/xxx指向静态资源calsspath:/backed/
        registry.addResourceHandler("/backend/**").addResourceLocations("classpath:/backend/");
        //同理
        registry.addResourceHandler("/front/**").addResourceLocations("classpath:/front/");

    }


    /**
     * 扩展mvc的消息转换器
     * @param converters
     */
    @Override
    protected void extendMessageConverters(List<HttpMessageConverter<?>> converters) {
        //创建消息转换器对象
        MappingJackson2HttpMessageConverter messageConverter = new MappingJackson2HttpMessageConverter();
        //设置对象转换器，用Json将java格式转换
        messageConverter.setObjectMapper(new JacksonObjectMapper());
        //将转换器添加到mvc框架的转换器集合中
        converters.add(0,messageConverter); //0为加到开头

    }
}
