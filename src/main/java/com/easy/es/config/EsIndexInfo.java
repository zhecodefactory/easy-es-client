package com.easy.es.config;

import lombok.Data;

import java.io.Serializable;

/**
 * @author 王青玄
 * @Contact 1121586359@qq.com
 * @ClassName EsIndexInfo
 * @create 2024年12月06日 21:13
 * @Description ES索引信息类
 * @Version V1.0
 */
@Data
public class EsIndexInfo implements Serializable {

    /*
    * 集群名称
    */
    private String clusterName;

    /*
     * 索引名称
     */
    private String indexName;
}
