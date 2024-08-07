package com.xuecheng.content;


import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;

/**
 * 测试freemaker页面静态化方法
 */
@SpringBootTest
public class FreemarkerTest {
    //@Autowired
    //CoursePublishService coursePublishService;

    @Test
    public void testGenerateHtmlByTemplate() throws IOException, TemplateException {

        Configuration configuration = new Configuration(Configuration.getVersion());
        //拿到Classpath
        String classpath = this.getClass().getResource("/").getPath();
        //指定模板的目录
        configuration.setDirectoryForTemplateLoading(new File(classpath+"/template/"));
        //指定编码
        configuration.setDefaultEncoding("utf-8");

        //得到模板
        Template template = configuration.getTemplate("course_template.ftl");
        //准备数据
        //CoursePreviewDto coursePreviewInfo =coursePublishService.getCoursePreviewInfo(120L);
        HashMap<String, Object>map=new HashMap<>();
        //map.put("model",coursePreviewInfo);
        //Template 模板，Object 数据
        String html = FreeMarkerTemplateUtils.processTemplateIntoString(template, map);
        //将静态化内容输出到文件中
        InputStream inputStream = IOUtils.toInputStream(html,"utf-8");
        //输出流
        FileOutputStream outputStream = new FileOutputStream("D:\\develop\\test.html");
        //使用流将html写入文件
        IOUtils.copy(inputStream, outputStream);
    }
}
