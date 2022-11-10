package com.example.springbootesjd;

import com.alibaba.fastjson.JSON;
import com.example.entity.User;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.support.master.AcknowledgedResponse;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.client.indices.CreateIndexResponse;
import org.elasticsearch.client.indices.GetIndexRequest;
import org.elasticsearch.core.TimeValue;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.TermQueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.FetchSourceContext;
import org.elasticsearch.xcontent.XContentType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

@SpringBootTest
class EsRestfulClientApplicationTests {

    @Autowired
    private RestHighLevelClient restHighLevelClient;

    // 测试索引的创建
    @Test
    void testCreateIndex() throws IOException {
        // 1.创建索引请求
        CreateIndexRequest request = new CreateIndexRequest("kuang_index");
        // 2.执行创建请求，请求后获得响应createIndexResponse
        CreateIndexResponse createIndexResponse = restHighLevelClient.indices().create(request, RequestOptions.DEFAULT);
        System.out.println(createIndexResponse);
    }

    // 测试获取索引，只能判断其是否存在
    @Test
    void testExistIndex() throws IOException{
        // 1.创建索引请求
        GetIndexRequest request = new GetIndexRequest("kuang_index");
        // 2.获取索引，判断是否存在
        boolean exists = restHighLevelClient.indices().exists(request,RequestOptions.DEFAULT);
        System.out.println(exists);
    }


    // 测试删除索引
    @Test
    void testDeleteIndex() throws IOException{
        // 1.创建索引请求
        DeleteIndexRequest request = new DeleteIndexRequest("kuang_index");
        // 2.获取删除状态
        AcknowledgedResponse delete = restHighLevelClient.indices().delete(request,RequestOptions.DEFAULT);
        System.out.println(delete);
    }

    //================文档操作===========================

    // 添加文档
    @Test
    void testAddDocument() throws IOException{
        // 1.创建对象
        User user = new User("cxg", 21);
        // 2.创建请求
        IndexRequest request = new IndexRequest("kuang_index");
        // 3.路径 put /kuang_index/_doc/1
        request.id("1");
        request.timeout(TimeValue.timeValueSeconds(1));// 或者 request.timeout("1s");
        // 4.把数据放入请求json中

        request.source(JSON.toJSONBytes(user), XContentType.JSON);
        // 5.客户端发送请求，获取响应结果
        try {
            IndexResponse indexResponse = restHighLevelClient.index(request, RequestOptions.DEFAULT);
            System.out.println(indexResponse.toString());   //返回的json
            System.out.println(indexResponse.status());  // 返回的状态
        }catch (Exception e){
            e.printStackTrace();
        }

    }


    // 获取文档信息，判断是否存在 get /index/_doc/1
    @Test
    void testIsExists() throws IOException{
        GetRequest getRequest = new GetRequest("kuang_index", "1");
        //不获取返回_source的上下文
        getRequest.fetchSourceContext(new FetchSourceContext(false));
        getRequest.storedFields("_none_");
        boolean exists = restHighLevelClient.exists(getRequest, RequestOptions.DEFAULT);
        System.out.println(exists);
    }

    // 获取文档的信息
    @Test
    void testGetDocument() throws IOException{
        GetRequest getRequest = new GetRequest("kuang_index", "1");
        GetResponse getResponse = restHighLevelClient.get(getRequest, RequestOptions.DEFAULT);
        System.out.println(getResponse.getSourceAsString());
        System.out.println(getResponse);   // 返回的全部内容和命令一样
    }

    // 更新文档的信息
    @Test
    void testUpdatedDocument() throws IOException{
        UpdateRequest updateRequest = new UpdateRequest("kuang_index", "1");
        updateRequest.timeout("1s");

        User user = new User("cxk", 22);
        updateRequest.doc(JSON.toJSONString(user),XContentType.JSON);
        try {
            UpdateResponse update = restHighLevelClient.update(updateRequest, RequestOptions.DEFAULT);
            System.out.println(update.status());
        }catch (Exception e){
        }
    }

    // 删除文档记录
    @Test
    void testDeleteRequest() throws IOException{
        DeleteRequest deleteRequest = new DeleteRequest("kuang_index", "1");
        deleteRequest.timeout("1s");
        try {
            DeleteResponse deleteResponse = restHighLevelClient.delete(deleteRequest, RequestOptions.DEFAULT);
            System.out.println(deleteResponse.status());
        }catch (Exception e){}
    }



    // =========================批量插入数据=================
    @Test
    void testBulkRequest() throws IOException{
        BulkRequest bulkRequest = new BulkRequest();
        bulkRequest.timeout("1s");

        ArrayList<User> users = new ArrayList<>();
        users.add(new User("alice",32));
        users.add(new User("bob",24));
        users.add(new User("john",32));
        users.add(new User("csk",25));
        users.add(new User("cxg",62));
        users.add(new User("smd",21));

        try {
            for (int i = 0; i < users.size(); i++) {
                bulkRequest.add(new IndexRequest("kuang_index")
                        .id("" + (i + 1))   //如果不设置id，则会生成默认的id
                        .source(JSON.toJSONString(users.get(i)), XContentType.JSON));
            }
            BulkResponse bulkResponse = restHighLevelClient.bulk(bulkRequest, RequestOptions.DEFAULT);
            System.out.println(bulkResponse.hasFailures());  //是否失败
        }catch (Exception e){}
    }




    // =====================================查询=====================================
    // SearchRequest 搜索请求
    // SearchSourceBuilder 条件构造
    // HighlightBuilder 构建高亮
    // TermQueryBuilder精确查询
    // MatchALLQueryBuilder
    // xxx QueryBuilder 对应刚才看到的命令
    @Test
    void testSearch() throws IOException{
        //创建请求
        SearchRequest searchRequest = new SearchRequest("kuang_index");
        //构建搜索条件
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
        //查询条件，使用QueryBuilders实现
        // term查询中文会失败
        TermQueryBuilder termQueryBuilder = QueryBuilders.termQuery("name", "csk");//精确匹配
        //MatchAllQueryBuilder matchAllQueryBuilder = QueryBuilders.matchAllQuery();// 匹配所有

        sourceBuilder.query(termQueryBuilder);
        sourceBuilder.timeout(new TimeValue(60, TimeUnit.SECONDS));

        searchRequest.source(sourceBuilder);
        SearchResponse searchResponse = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
        System.out.println(JSON.toJSONString(searchResponse.getHits()));
        System.out.println("===============================");
        for (SearchHit hit : searchResponse.getHits().getHits()) {
            System.out.println(hit.getSourceAsMap());
        }
    }
}

