package jnpf.model;

import com.github.yulichang.wrapper.MPJLambdaWrapper;
import jnpf.database.model.superQuery.SuperJsonModel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class QueryAllModel {
    private MPJLambdaWrapper wrapper;
    private Map<String,Class> classMap;
    /**
     * 数据过滤
     */
    private String ruleJson;
    /**
     * 高级查询
     */
    private String superJson;
    /**
     * 数据权限-通过菜单id查询
     */
    private String moduleId;
    private String dbLink;
    private String dbType;
    /**
     * 所有分组都放进来统一处理
     */
    private List<List<SuperJsonModel>> queryList;

    private String systemCode;
}
