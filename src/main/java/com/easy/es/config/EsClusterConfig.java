package com.easy.es.config;

import lombok.Data;

import java.io.Serializable;

/**
 * @author 王青玄
 * @Contact 1121586359@qq.com
 * @ClassName EsClusterConfig
 * @create 2024年12月06日 20:59
 * @Description ES集群类
 * @Version V1.0
 */
@Data
public class EsClusterConfig implements Serializable {
    /**
     * 集群名称
     */
    private String clusterName;

    /**
     * 集群节点地址
     */
    private String nodes;
} 