package com.easy.es.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * @author 王青玄
 * @Contact 1121586359@qq.com
 * @ClassName EsConfigProperties
 * @create 2024年12月06日 20:59
 * @Description 读取ES配置相关信息类
 * @Version V1.0
 */
@Component
@ConfigurationProperties(prefix = "es.cluster")
public class EsConfigProperties implements Serializable {

    private List<EsClusterConfig> esConfigs = new ArrayList<>();

    /**
     * 提供获取到ES集群配置的信息
     */
    public List<EsClusterConfig> getEsConfigs() {
        return esConfigs;
    }

    /**
     * 提供设置到ES集群配置的信息
     */
    public void setEsConfigs() {
        this.esConfigs = esConfigs;
    }

}
