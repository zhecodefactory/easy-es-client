# Easy Elasticsearch

一个简单易用的 Elasticsearch 客户端封装工具。

## 快速开始

### 1. 添加依赖

首先在你的 Spring Boot 项目的 `pom.xml` 中添加以下依赖：

```xml
<dependency>
    <groupId>com.easy.es</groupId>
    <artifactId>elasticsearch-spring-boot-starter</artifactId>
    <version>1.0.0</version>
</dependency>
```

### 2. 配置文件

在 `application.yml` 中添加以下配置：
```yaml
es:
  cluster:
    esConfigs[0]:
      clusterName: cluster1 # ES集群名称
      nodes: "localhost:9200" # ES集群节点
```

### 3. 使用示例
## 主要功能

- 支持多集群配置
- 文档操作
    - 插入文档
    - 更新文档
    - 批量更新
    - 删除文档
    - 查询文档
- 搜索功能
    - 条件查询
    - 分页查询
    - 排序
    - 高亮显示

## 注意事项

1. 确保你的项目使用的 Spring Boot 版本兼容此 starter
2. 确保你的 Elasticsearch 版本与此 starter 兼容
3. 配置文件中的 cluster-name 将作为多集群区分的关键字，请确保其唯一性

## 版本说明

- JDK 要求：1.8+
- Spring Boot：2.x
- Elasticsearch：7.x