package jnpf.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description="子表条件组合模型")
public class GeneraterSwapModel {
    @Schema(description = "表单json")
    private String formDataStr;
    @Schema(description = "表列表")
    private String tableList;
    @Schema(description = "子表名称")
    private String childTable;
    @Schema(description = "菜单id")
    private String menuId;
    @Schema(description = "查询条件json")
    private String queryJson;
    @Schema(description = "高级查询条件json")
    private String superQueryJson;
    @Schema(description = "pc列表")
    private String columnData;
    @Schema(description = "app列表")
    private String appColumnData;
}
