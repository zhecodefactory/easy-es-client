package com.easy.es.config;

import lombok.Data;

import java.io.Serializable;
import java.util.Map;

/**
 * @author 王青玄
 * @Contact 1121586359@qq.com
 * @ClassName EsSourceData
 * @create 2024年12月06日 21:28
 * @Description 元数据类
 * @Version V1.0
 */
@Data
public class EsSourceData implements Serializable {

    /*
     * 文档ID
     */
    private String docId;

    /*
     * 元数据
     */
    private Map<String, Object> data;

}
