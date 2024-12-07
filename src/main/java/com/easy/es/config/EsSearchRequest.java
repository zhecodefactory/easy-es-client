package com.easy.es.config;

import lombok.Data;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.sort.SortOrder;

/**
 * @author 王青玄
 * @Contact 1121586359@qq.com
 * @ClassName EsSearchRequest
 * @create 2024年12月06日 21:15
 * @Description ES请求封装类
 * @VerslQ V1.0
 */
@Data
public class EsSearchRequest {
    /**
     * 查询条件
     */
    private BoolQueryBuilder boolQueryBuilder;

    /**
     * 查询字段
     */
    private String[] fields;

    /**
     * 查询页码
     */
    private int from;

    /**
     * 每页条数
     */
    private int size;

    /**
     * 是否需要快照
     */
    private Boolean needScroll;

    /**
     * 快照缓存时间
     */
    private Long minutes;

    /**
     * 基于某个字段排序
     */
    private String sortName;

    /**
     * 基于某个字段排序类型
     */
    private SortOrder sortOrder = SortOrder.DESC;

    /**
     * 高亮条件
     */
    private HighlightBuilder highlightBuilder;
}
