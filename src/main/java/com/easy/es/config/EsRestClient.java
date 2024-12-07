package com.easy.es.config;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpHost;
import org.apache.http.util.EntityUtils;
import org.elasticsearch.action.bulk.BulkItemResponse;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.client.*;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.reindex.BulkByScrollResponse;
import org.elasticsearch.index.reindex.DeleteByQueryRequest;
import org.elasticsearch.index.reindex.UpdateByQueryRequest;
import org.elasticsearch.script.Script;
import org.elasticsearch.search.Scroll;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.FetchSourceContext;
import org.elasticsearch.search.sort.ScoreSortBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.util.NumberUtils;

import java.io.IOException;
import java.util.*;

/**
 * @author 王青玄
 * @Contact 1121586359@qq.com
 * @ClassName EsRestClient
 * @create 2024年12月06日 21:31
 * @Description ES客户端工具类
 * @Version V1.0
 */
@Slf4j
public class EsRestClient {

    private static Map<String, RestHighLevelClient> clientMap = new HashMap<>();
    private final EsConfigProperties esConfigProperties;
    private static final RequestOptions COMMON_OPTIONS;

    static {
        RequestOptions.Builder builder = RequestOptions.DEFAULT.toBuilder();
        COMMON_OPTIONS = builder.build();
    }

    public EsRestClient(EsConfigProperties esConfigProperties) {
        this.esConfigProperties = esConfigProperties;
    }

    /**
     * 初始化ES客户端
     * 根据配置创建RestHighLevelClient实例
     */
    public void init() {
        List<EsClusterConfig> esConfigs = esConfigProperties.getEsConfigs();
        for (EsClusterConfig esConfig : esConfigs) {
            log.info("init esConfig,clusterName->{},nodes->{}", esConfig.getClusterName(), esConfig.getNodes());
            RestHighLevelClient restHighLevelClient = initRestClient(esConfig);
            if (restHighLevelClient == null)
                log.error("config.name:{},node:{}.initError", esConfig.getClusterName(), esConfig.getNodes());
            clientMap.put(esConfig.getClusterName(), restHighLevelClient);
        }
    }

    private RestHighLevelClient initRestClient(EsClusterConfig esConfig) {
        String[] ipPortArr = esConfig.getNodes().split(",");
        List<HttpHost> httpHostList = new ArrayList<>(ipPortArr.length);
        for (String ipPort : ipPortArr) {
            String[] ipPortInfo = ipPort.split(":");
            if (ipPortInfo.length == 2) {
                HttpHost httpHost = new HttpHost(ipPortInfo[0], NumberUtils.parseNumber(ipPortInfo[1], Integer.class));
                httpHostList.add(httpHost);
            }
        }
        HttpHost[] httpHosts = new HttpHost[httpHostList.size()];
        httpHostList.toArray(httpHosts);

        RestClientBuilder builder = RestClient.builder(httpHosts);
        RestHighLevelClient restHighLevelClient = new RestHighLevelClient(builder);
        return restHighLevelClient;
    }

    private RestHighLevelClient getClient(String clusterName) {
        return clientMap.get(clusterName);
    }

    /**
     * 插入文档
     *
     * @param esIndexInfo  索引信息
     * @param esSourceData 文档数据
     * @return 是否插入成功
     */
    public boolean insertDoc(EsIndexInfo esIndexInfo,
                             EsSourceData esSourceData) {

        try {
            IndexRequest indexRequest = new IndexRequest(esIndexInfo.getIndexName());
            indexRequest.source(esSourceData.getData());
            indexRequest.index(esSourceData.getDocId());

            RestHighLevelClient client = getClient(esIndexInfo.getClusterName());
            client.index(indexRequest, COMMON_OPTIONS);
            return true;
        } catch (IOException e) {
            log.error("insertDoc.exception:{}", e.getMessage(), e);
        } finally {
            return false;
        }

    }

    /**
     * 更新文档
     *
     * @param esIndexInfo  索引信息
     * @param esSourceData 文档数据
     * @return 是否更新成功
     */
    public boolean updateDoc(EsIndexInfo esIndexInfo, EsSourceData esSourceData) {
        try {
            UpdateRequest updateRequest = new UpdateRequest();
            updateRequest.index(esIndexInfo.getIndexName());
            updateRequest.id(esSourceData.getDocId());
            updateRequest.doc(esSourceData.getData());
            getClient(esIndexInfo.getClusterName()).update(updateRequest, COMMON_OPTIONS);
            return true;
        } catch (Exception e) {
            log.error("updateDoc.exception:{}", e.getMessage(), e);
        } finally {
            return false;
        }

    }

    /**
     * 批量更新文档
     *
     * @param esIndexInfo      索引信息
     * @param esSourceDataList 文档数据列表
     * @return 是否执行成功
     */
    public boolean batchUpdateDoc(EsIndexInfo esIndexInfo,
                                  List<EsSourceData> esSourceDataList) {
        try {
            boolean flag = false;
            BulkRequest bulkRequest = new BulkRequest();
            for (EsSourceData esSourceData : esSourceDataList) {
                String docId = esSourceData.getDocId();
                if (StringUtils.isNotBlank(docId)) {
                    UpdateRequest updateRequest = new UpdateRequest();
                    updateRequest.index(esIndexInfo.getIndexName());
                    updateRequest.id(esSourceData.getDocId());
                    updateRequest.doc(esSourceData.getData());
                    bulkRequest.add(updateRequest);
                    flag = true;
                }
            }

            if (flag) {
                BulkResponse bulk = getClient(esIndexInfo.getClusterName()).bulk(bulkRequest, COMMON_OPTIONS);
                if (bulk.hasFailures()) {
                    return false;
                }
            }

            return true;
        } catch (Exception e) {
            log.error("batchUpdateDoc.exception:{}", e.getMessage(), e);
        } finally {
            return false;
        }

    }

    /**
     * 删除索引
     *
     * @param esIndexInfo 索引信息
     * @return 是否删除成功
     */
    public boolean delete(EsIndexInfo esIndexInfo) {

        try {
            DeleteByQueryRequest deleteByQueryRequest = new DeleteByQueryRequest(esIndexInfo.getIndexName());
            deleteByQueryRequest.setQuery(QueryBuilders.matchAllQuery());

            RestHighLevelClient client = getClient(esIndexInfo.getClusterName());
            BulkByScrollResponse response = client.deleteByQuery(deleteByQueryRequest, COMMON_OPTIONS);
            long deleted = response.getDeleted();
            log.info("deleted.size:{}", deleted);
            return true;
        } catch (IOException e) {
            log.error("delete.exception:{}", e.getMessage(), e);
        } finally {
            return false;
        }

    }

    /**
     * 删除文档
     *
     * @param esIndexInfo 索引信息
     * @param docId       文档ID
     * @return 是否删除成功
     */
    public boolean deleteDoc(EsIndexInfo esIndexInfo,
                             String docId) {

        try {
            DeleteRequest deleteRequest = new DeleteRequest(esIndexInfo.getIndexName());
            deleteRequest.id(docId);
            DeleteResponse response = getClient(esIndexInfo.getClusterName()).delete(deleteRequest, COMMON_OPTIONS);
            log.info("deleteDoc.response:{}", JSON.toJSONString(response));
            return true;
        } catch (IOException e) {
            log.error("delete.exception:{}", e.getMessage(), e);
        } finally {
            return false;
        }

    }

    /**
     * 判断文档是否存在
     *
     * @param esIndexInfo 索引信息
     * @param docId       文档ID
     * @return 是否存在
     */
    public boolean isExistDocById(EsIndexInfo esIndexInfo,
                                  String docId) {

        try {
            GetRequest getRequest = new GetRequest(esIndexInfo.getIndexName());
            getRequest.id(docId);
            boolean exists = getClient(esIndexInfo.getClusterName()).exists(getRequest, COMMON_OPTIONS);
            log.info("isExistDocById.response:{}", exists);
            return exists;
        } catch (IOException e) {
            log.error("isExistDocById.exception:{}", e.getMessage(), e);
        } finally {
            return false;
        }

    }


    /**
     * 获取文档
     *
     * @param esIndexInfo 索引信息
     * @param docId       文档ID
     * @return 文档内容
     */
    public Map<String, Object> getDocById(EsIndexInfo esIndexInfo,
                                          String docId) {

        try {
            GetRequest getRequest = new GetRequest(esIndexInfo.getIndexName());
            getRequest.id(docId);
            GetResponse response = getClient(esIndexInfo.getClusterName()).get(getRequest, COMMON_OPTIONS);
            Map<String, Object> source = response.getSource();

            return source;
        } catch (IOException e) {
            log.error("getDocById.exception:{}", e.getMessage(), e);
        } finally {
            return null;
        }

    }

    /**
     * 获取文档
     *
     * @param esIndexInfo 索引信息
     * @param docId       文档ID
     * @param fields      字段数组
     * @return 文档内容
     */
    public Map<String, Object> getDocById(EsIndexInfo esIndexInfo,
                                          String docId,
                                          String[] fields) {
        try {
            GetRequest getRequest = new GetRequest(esIndexInfo.getIndexName());
            getRequest.id(docId);
            FetchSourceContext fetchSourceContext = new FetchSourceContext(true, fields, null);
            getRequest.fetchSourceContext(fetchSourceContext);
            GetResponse response = getClient(esIndexInfo.getClusterName()).get(getRequest, COMMON_OPTIONS);
            Map<String, Object> source = response.getSource();
            return source;
        } catch (Exception e) {
            log.error("getDocById.exception:{}", e.getMessage(), e);
        } finally {
            return null;
        }

    }

    /**
     * 搜索文档
     *
     * @param esIndexInfo     索引信息
     * @param esSearchRequest 搜索请求参数
     * @return 搜索结果
     */
    public SearchResponse searchWithTermQuery(EsIndexInfo esIndexInfo,
                                              EsSearchRequest esSearchRequest) {
        try {
            BoolQueryBuilder boolQueryBuilder = esSearchRequest.getBoolQueryBuilder();
            String[] fields = esSearchRequest.getFields();
            int from = esSearchRequest.getFrom();
            int size = esSearchRequest.getSize();
            Long minutes = esSearchRequest.getMinutes();
            Boolean needScroll = esSearchRequest.getNeedScroll();
            String sortName = esSearchRequest.getSortName();
            SortOrder sortOrder = esSearchRequest.getSortOrder();

            SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
            searchSourceBuilder.query(boolQueryBuilder);
            searchSourceBuilder.fetchSource(fields, null).from(from).size(size);

            if (Objects.nonNull(esSearchRequest.getHighlightBuilder())) {
                searchSourceBuilder.highlighter(esSearchRequest.getHighlightBuilder());
            }

            if (StringUtils.isNotBlank(sortName)) {
                searchSourceBuilder.sort(sortName);
            }

            searchSourceBuilder.sort(new ScoreSortBuilder().order(sortOrder));

            SearchRequest searchRequest = new SearchRequest();
            searchRequest.searchType(SearchType.DEFAULT);
            searchRequest.indices(esIndexInfo.getIndexName());
            searchRequest.source(searchSourceBuilder);
            if (needScroll) {
                Scroll scroll = new Scroll(TimeValue.timeValueMinutes(minutes));
                searchRequest.scroll(scroll);
            }
            SearchResponse search = getClient(esIndexInfo.getClusterName()).search(searchRequest, COMMON_OPTIONS);
            return search;
        } catch (Exception e) {
            log.error("searchWithTermQuery.exception:{}", e.getMessage(), e);
        } finally {
            return null;
        }

    }

    public boolean batchInsertDoc(EsIndexInfo esIndexInfo, List<EsSourceData> esSourceDataList) {
        if (log.isInfoEnabled()) {
            log.info("批量新增ES:" + esSourceDataList.size());
            log.info("indexName:" + esIndexInfo.getIndexName());
        }
        try {
            boolean flag = false;
            BulkRequest bulkRequest = new BulkRequest();

            for (EsSourceData source : esSourceDataList) {
                String docId = source.getDocId();
                if (StringUtils.isNotBlank(docId)) {
                    IndexRequest indexRequest = new IndexRequest(esIndexInfo.getIndexName());
                    indexRequest.id(docId);
                    indexRequest.source(source.getData());
                    bulkRequest.add(indexRequest);
                    flag = true;
                }
            }


            if (flag) {
                BulkResponse response = getClient(esIndexInfo.getClusterName()).bulk(bulkRequest, COMMON_OPTIONS);
                if (response.hasFailures()) {
                    return false;
                }
            }
        } catch (Exception e) {
            log.error("batchInsertDoc.error", e);
        }

        return true;
    }

    public boolean updateByQuery(EsIndexInfo esIndexInfo, QueryBuilder queryBuilder, Script script, int batchSize) {
        if (log.isInfoEnabled()) {
            log.info("updateByQuery.indexName:" + esIndexInfo.getIndexName());
        }
        try {
            UpdateByQueryRequest updateByQueryRequest = new UpdateByQueryRequest(esIndexInfo.getIndexName());
            updateByQueryRequest.setQuery(queryBuilder);
            updateByQueryRequest.setScript(script);
            updateByQueryRequest.setBatchSize(batchSize);
            updateByQueryRequest.setAbortOnVersionConflict(false);
            BulkByScrollResponse response = getClient(esIndexInfo.getClusterName()).updateByQuery(updateByQueryRequest, RequestOptions.DEFAULT);
            List<BulkItemResponse.Failure> failures = response.getBulkFailures();
        } catch (Exception e) {
            log.error("updateByQuery.error", e);
        }
        return true;
    }

    /**
     * 分词方法
     */
    public List<String> getAnalyze(EsIndexInfo esIndexInfo, String text) throws Exception {
        List<String> list = new ArrayList<String>();
        Request request = new Request("GET", "_analyze");
        JSONObject entity = new JSONObject();
        entity.put("analyzer", "ik_smart");
        entity.put("text", text);
        request.setJsonEntity(entity.toJSONString());
        Response response = getClient(esIndexInfo.getClusterName()).getLowLevelClient().performRequest(request);
        JSONObject tokens = JSONObject.parseObject(EntityUtils.toString(response.getEntity()));
        JSONArray arrays = tokens.getJSONArray("tokens");
        for (int i = 0; i < arrays.size(); i++) {
            JSONObject obj = JSON.parseObject(arrays.getString(i));
            list.add(obj.getString("token"));
        }
        return list;
    }
}
