package com.example.springbootesjd;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URL;

/**
 * @author :infinite-war
 * @date : 2022/11/10 14:58
 * @desc :
 */
public class SpiderTest {
    //测试爬虫
    @Test
    void testSpider() throws IOException {
        //获取请求 https://search.jd.com/Search?keyword=java
        //前提: 需要联网, 而且不能获取到AJAX
        String url="https://search.jd.com/Search?keyword=java";

        //设置超时时间 30s
        int timeOut=30000;

        //解析网页 ==> Document是浏览器的Document对象
        Document document = Jsoup.parse(new URL(url), timeOut);
        //可以执行js中的一些函数
        Element element = document.getElementById("J_goodsList");
        // System.out.println(element.html());

        //获取所有li元素
        Elements elements = element.getElementsByTag("li");

        for (Element el : elements) {
            //关于这种图片特别多的网站, 所有的图片都是延迟加载的
            //JD 放在class data-lazy-img
            String img=el.getElementsByTag("img").eq(0).attr("data-lazy-img");
            String price=el.getElementsByClass("p-price").eq(0).text();
            String title=el.getElementsByTag("p-name").eq(0).text();

            System.out.println("===================================================");
            System.out.println(img);
            System.out.println(price);
            System.out.println(title);
        }
    }

}
