package com.example.utils;

import com.example.entity.Content;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.context.annotation.Configuration;


import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

/**
 * @author :infinite-war
 * @date : 2022/11/10 14:52
 * @desc :
 */
@Configuration
public class HtmlParseUtil {
    public static void main(String[] args) throws IOException {
        // URL会将符号转义!
        // HtmlParseUtil.parseJD("C%2B%2B").forEach(System.out::println);
        // 查询中文需要URL转码
        // HtmlParseUtil.parseJD("心理学").forEach(System.out::println);
        new HtmlParseUtil().parseJD("C++").forEach(System.out::println);
    }

    public List<Content> parseJD(String keywords) throws IOException{
        //URL会对符号和汉字转码
        //要先转码再拼接, 否则URL无法解析 (因为会将url中的符号也一起转码, 无法识别)
        String urlKeywords= URLEncoder.encode(keywords,"UTF-8");
        //获取请求 https://search.jd.com/Search?keyword=java
        //前提: 需要联网, 而且不能获取到AJAX!
        String url ="https://search.jd.com/Search?keyword=" + urlKeywords + "&enc=utf-8";
        //设置超时时间 30S
        int timeOut = 30000;
        //解析网页 ==> Document就是浏览器的Document对象
        Document document = Jsoup.parse(new URL(url), timeOut);
        //所有你在JS中可以使用的方法, 这里都能用!
        Element element = document.getElementById("J_goodsList");
        //获取所有的li元素
        Elements elements = element.getElementsByTag("li");

        List<Content> goodsList = new ArrayList<>();
        //获取元素中的内容, 这里的el就是每一个li标签了
        for (Element el : elements) {
            //关于这种图片特别多的网站, 所有的图片都是延迟加载的!
            //JD 放在了这个class data-lazy-img
            String img = el.getElementsByTag("img").eq(0).attr("data-lazy-img");
            String price = el.getElementsByClass("p-price").eq(0).text();
            String title = el.getElementsByClass("p-name").eq(0).text();

            Content content = new Content();
            content.setImg(img)
                    .setPrice(price)
                    .setTitle(title);
            goodsList.add(content);
        }
        return goodsList;
    }
}
