package com.itheima.reggie.controller;


import com.itheima.reggie.common.R;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;

import java.io.IOException;
import java.util.UUID;

/**
 * 通用Controller类
 */

@RestController
@RequestMapping("/common")
@Slf4j
public class CommonController {


    @Value("${reggie.path}")
    private String basePath;
    /**
     * 文件上传
     * @param file  参数起名与页面那么一致
     * @return 返回上传文件新名
     */
    @PostMapping("/upload")
    public R<String> upload(MultipartFile file){
        log.info("上传图片:{}", file.getName());

        //传来文件的原始文件名
        String originalFilename = file.getOriginalFilename();//abc.jpg
        //获取格式后缀名
        String substring = originalFilename.substring(originalFilename.lastIndexOf("."));//.jpg

        //生成随机文件名，避免覆盖
        String fileName = UUID.randomUUID().toString()+substring; //adsd.jpg

        //判断目录是否存在
        File dir =new File(basePath);
        if (!dir.exists()){
            dir.mkdirs();
        }

        try {
            //将文件转存到指定位置
            file.transferTo(new File(basePath+fileName));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return R.success(fileName);
    }


    /**
     * 文件下载，在浏览器打开 ，   由于在文件上传成功后放回新文件名，并传到文件下载
     * @param name 文件名
     * @param response 响应输出流写回浏览器
     */
    @GetMapping("/download")
    public void download(String name, HttpServletResponse response){
        //输入流读文件内容
        try {
            //FileInputStream inputStream = new FileInputStream(new File(basePath+name));

            FileInputStream inputStream = FileUtils.openInputStream(new File(basePath + name));
            response.setContentType("image/jpeg");
            //输出流，将文件写回浏览器，在浏览器展示图片
            ServletOutputStream outputStream = response.getOutputStream();
            IOUtils.copy(inputStream,outputStream);

            inputStream.close();
            outputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

}
