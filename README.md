# elasticsearch-similarity-plugin
### ElasticSearch Plugin
### 包括：
* 计算汉明距离/相似度
* 余弦距离/相似度
* 欧几里得距离/相似度
* 莱文斯坦比

---

<em>
目前只支持elasticsearch 7.6.0，如果要改版本，需要改pom.xml和plugin-descriptor.properties中的${elasticsearch.version}值
</em>



打包：

`mvn clean package`


es查询条件：

 ```
GET image/_search
{
    "from": 0,
    "size": 30,
    "query": {
      "function_score": {
        "query": {
          "bool": {
            "must_not": [
              {
                "term": {
                  "id": 244453574648528896
                }
              }
            ]
          }
        },
        "functions": [
          {
            "script_score": {
              "script": {
                  "source": "hist_cos",
                  "lang" : "wv_similarity",
                  "params": {
                      "field": "hist",
                      "term": "{'436': 2, '437': 1, ..., '510': 3, '511': 1005}"
                  }
              }
            }
          }
        ]
      }
    }
}
 ```

 ```
 GET image/_search
 {
     "from": 0,
     "size": 30,
     "query": {
       "function_score": {
         "query": {
           "bool": {
             "must":[
               {
                 "exists":{ 
                   "field": "phash"
                 }
               }
             ], 
             "must_not": [
               {
                 "term": {
                   "id": 244453574648528896
                 }
               }
             ]
           }
         },
         "functions": [
           {
             "script_score": {
               "script": {
                   "source": "hamming",
                   "lang" : "wv_similarity",
                   "params": {
                       "field": "phash",
                       "term": "1000000000000000000000000000000000000000000000000000000000000000"
                   }
               }
             }
           }
         ],
         "min_score": 0.96
       }
     }
 }
```
