package jnpf.model;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class QueryModel<T> {
    private QueryWrapper<T> obj;
    private Class<T> entity;
    private String queryJson;
    private String moduleId;
    private String dbLink;
    private String dbType;

    public QueryModel(QueryWrapper<T> obj, Class<T> entity, String queryJsonOrModuleId, String dbLink) {
        this.obj = obj;
        this.entity = entity;
        this.queryJson = queryJsonOrModuleId;
        this.moduleId = queryJsonOrModuleId;
        this.dbLink = dbLink;
    }

}
