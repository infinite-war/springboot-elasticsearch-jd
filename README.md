# springboot-elasticsearch-jd
springboot+elasticsearch(8.4.3)实现仿京东搜索引擎


技术版本：

sprinboot：2.7.5

elasticsearch：8.4.3



springboot中使用的还是elasticsearch7.x的RestApi(8.x推出了新的java api)。

springboot+es8.x兼容es7.x的RestApi(不过会被标记为不推荐)，在这种环境下运行es7.x的RestApi(比如查询es的索引)可能会出现空指针的错误，这是因为springboot目前(2022.11)还没有做好与es8.x的兼容，这个报错影响不大，而且返回结果还是正常的，所以每次调用`restHighLevelClient(具体见EsRestfulClientApplicationTests)`时，最好加上try-catch，防止因这种错误导致程序终止。


学习视频：https://www.bilibili.com/video/BV17a4y1x7zq
