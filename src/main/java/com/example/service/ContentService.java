package com.example.service;

import com.alibaba.fastjson.JSON;
import com.example.entity.Content;
import com.example.utils.HtmlParseUtil;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.text.Text;
import org.elasticsearch.core.TimeValue;
import org.elasticsearch.index.query.MatchQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.elasticsearch.xcontent.XContent;
import org.elasticsearch.xcontent.XContentType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * @author :infinite-war
 * @date : 2022/11/10 15:04
 * @desc :
 */
@Service
public class ContentService {

    @Autowired
    @Qualifier("restHighLevelClient")
    private RestHighLevelClient rest;


    // 解析数据，放入es索引中
    public Boolean parseContent(String keywords) throws IOException{
        List<Content> contents =new HtmlParseUtil().parseJD(keywords);
        //把查询的数据放入es
        BulkRequest bulkRequest = new BulkRequest();
        bulkRequest.timeout(TimeValue.timeValueSeconds(2L));

        for(int i=0;i<contents.size();i++){
            bulkRequest.add(new IndexRequest("jd_goods")
                    .source(JSON.toJSONString(contents.get(i)), XContentType.JSON));
        }
        try {
            BulkResponse bulk = rest.bulk(bulkRequest, RequestOptions.DEFAULT);
            return !bulk.hasFailures();
        }catch (Exception e){}
        return true;
    }

    //搜索高亮
    public List<Map<String, Object>> searchPage(String keyword, int pageNo, int pageSize) throws IOException{
        if(pageNo<=1){
            pageNo=1;
        }
        //条件搜索
        SearchRequest searchRequest = new SearchRequest("jd_goods");
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
        //精确匹配
        MatchQueryBuilder matchQueryBuilder = QueryBuilders.matchQuery("title", keyword);
        sourceBuilder.query(matchQueryBuilder).timeout(TimeValue.timeValueSeconds(1L));

        //高亮
        HighlightBuilder highlightBuilder = new HighlightBuilder();
        //定义要高亮的标签和样式
        highlightBuilder.field("title")
                .preTags("<span style='color:red'>")
                .postTags("</span>")
                .requireFieldMatch(false);//是否需要高亮多个字段
        sourceBuilder.highlighter(highlightBuilder);
        //分页
        sourceBuilder.from(pageNo).size(pageSize);

        //执行搜索
        searchRequest.source(sourceBuilder);
        SearchResponse searchResponse=null;
        try {
            searchResponse = rest.search(searchRequest, RequestOptions.DEFAULT);
        }catch (Exception e){}
        //解析结果
        ArrayList<Map<String,Object>> list = new ArrayList<>();
        for (SearchHit documentFields : searchResponse.getHits().getHits()) {
            //解析高亮的字段
            Map<String, HighlightField> highlightFields = documentFields.getHighlightFields();
            HighlightField title = highlightFields.get("title");
            //这里是原来的结果(不含高亮)
            Map<String, Object> sourceAsMap = documentFields.getSourceAsMap();

            if(title!=null){
                Text[] fragments = title.fragments();
                StringBuilder highlightTitle= new StringBuilder();
                for(Text text:fragments){
                    highlightTitle.append(text);
                }
                //将高亮字段替换没有高亮的字段
                sourceAsMap.put("title", highlightTitle.toString());
            }
            list.add(sourceAsMap);
        }
        return list;
    }

}
