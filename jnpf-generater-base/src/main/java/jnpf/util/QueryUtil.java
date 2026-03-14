package jnpf.util;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.annotation.TableField;
import com.github.yulichang.wrapper.MPJLambdaWrapper;
import jnpf.database.model.superQuery.SuperJsonModel;
import jnpf.database.model.superQuery.SuperQueryJsonModel;
import jnpf.emnus.SearchMethodEnum;
import jnpf.model.QueryAllModel;
import jnpf.model.visualJson.FieLdsModel;
import jnpf.model.visualJson.config.ConfigModel;
import jnpf.onlinedev.util.onlineDevUtil.OnlineProductSqlUtils;
import lombok.Data;
import org.apache.poi.ss.formula.functions.T;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Data
public class QueryUtil {

    /**
     * 运算符
     */
    private SearchMethodEnum symbol;
    /**
     * 逻辑拼接符号
     */
    private boolean and;
    /**
     * 组件标识
     */
    private String jnpfKey;
    /**
     * 字段key
     */
    private String vModel;
    /**
     * 自定义的值
     */
    private Object fieldValue;
    /**
     * 自定义的值2
     */
    private Object fieldValueTwo;
    /**
     * 实体对象
     */
    private Map<String, Class> classMap;
    /**
     * 数据库类型
     */
    private String dbType;

    private Boolean isSqlServer = false;

    private List<String> dataList = new ArrayList<>();


    public <T> MPJLambdaWrapper<T> queryList(QueryAllModel queryAllModel) {
        MPJLambdaWrapper<T> wrapper = queryAllModel.getWrapper();
        classMap = queryAllModel.getClassMap();
        dbType = queryAllModel.getDbType();
        isSqlServer = "Microsoft SQL Server".equalsIgnoreCase(dbType);
        List<List<SuperJsonModel>> superJsonModelList = queryAllModel.getQueryList();
        for (List<SuperJsonModel> list : superJsonModelList) {
            boolean flag = false;
            for (SuperJsonModel superJsonModel : list) {
                if (superJsonModel.getConditionList().size() > 0) {
                    for (SuperQueryJsonModel item : superJsonModel.getConditionList()) {
                        if (item.getGroups().size() > 0) {
                            flag = true;
                        }
                    }
                }
            }
            if (list.size() > 0 && flag) {
                wrapper.and(gw -> {
                    for (SuperJsonModel superJsonModel : list) {
                        String matchLogic = superJsonModel.getMatchLogic();
                        Boolean authorizeLogic = superJsonModel.getAuthorizeLogic();
                        boolean isAddMatchLogic = SearchMethodEnum.And.getSymbol().equalsIgnoreCase(matchLogic);
                        List<SuperQueryJsonModel> conditionList = superJsonModel.getConditionList();
                        if (conditionList.size() == 0) continue;
                        //参数值转换
                        OnlineProductSqlUtils.superList(conditionList, true);
                        if (authorizeLogic) {
                            gw.and(tw -> {
                                queryWrapperList(tw, conditionList, isAddMatchLogic);
                            });
                        } else {
                            gw.or(tw -> {
                                queryWrapperList(tw, conditionList, isAddMatchLogic);
                            });
                        }
                    }
                });
            }
        }
        return wrapper;
    }

    private <T> void queryWrapperList(MPJLambdaWrapper<T> tw, List<SuperQueryJsonModel> conditionList, boolean isAddMatchLogic) {
        for (SuperQueryJsonModel superQueryJsonModel : conditionList) {
            String logic = superQueryJsonModel.getLogic();
            and = SearchMethodEnum.And.getSymbol().equalsIgnoreCase(logic);
            List<FieLdsModel> queryList = superQueryJsonModel.getGroups();
            if (queryList.size() > 0) {
                queryWrapper(tw, queryList, isAddMatchLogic);
            }
        }
    }

    private <T> void queryWrapper(MPJLambdaWrapper<T> tw, List<FieLdsModel> queryListAll, boolean isAnd) {
        if (isAnd) {
            tw.and(qw -> {
                query(qw, queryListAll);
            });
        } else {
            tw.or(qw -> {
                query(qw, queryListAll);
            });
        }
    }

    private <T> void query(MPJLambdaWrapper<T> qw, List<FieLdsModel> queryListAll) {
        for (FieLdsModel fieLdsModel : queryListAll) {
            List<FieLdsModel> queryList = new ArrayList() {{
                add(fieLdsModel);
            }};
            if (and) {
                qw.and(ew -> {
                    fieldsModel(ew, queryList);
                });
            } else {
                qw.or(ew -> {
                    fieldsModel(ew, queryList);
                });
            }
        }
    }

    private void fieldsModel(MPJLambdaWrapper wrapper, List<FieLdsModel> queryList) {
        for (FieLdsModel fieLdsModel : queryList) {
            ConfigModel config = fieLdsModel.getConfig();
            jnpfKey = config.getJnpfKey();
            symbol = SearchMethodEnum.getSearchMethod(fieLdsModel.getSymbol());
            vModel = fieLdsModel.getVModel();
            if (!and) {
                wrapper.or();
            }
            String table = ObjectUtil.isNotEmpty(config.getRelationTable()) ? config.getRelationTable() : config.getTableName();
            Class<T> tClass = classMap.get(table);
            try {
                Field declaredField = null;
                Field[] declaredFields = tClass.getDeclaredFields();
                for (Field item : declaredFields) {
                    String tableFieldName = item.getAnnotation(TableField.class) == null ? "" : item.getAnnotation(TableField.class).value();
                    if (vModel.equalsIgnoreCase(tableFieldName) || item.getName().equals(vModel)) {
                        declaredField = item;
                        break;
                    }
                }
                declaredField.setAccessible(true);
                vModel = table + "." + declaredField.getAnnotation(TableField.class).value();
            } catch (Exception e) {
                e.printStackTrace();
            }
            fieldValue = fieLdsModel.getFieldValueOne();
            fieldValueTwo = fieLdsModel.getFieldValueTwo();
            dataList = fieLdsModel.getDataList();
            getSymbolWrapper(wrapper);
        }
    }

    private void getNullWrapper(MPJLambdaWrapper<?> wrapper) {
        if (!and) {
            wrapper.or(t -> t.isNull(vModel));
        } else {
            wrapper.and(t -> t.isNull(vModel));
        }
    }

    private void getNotNullWrapper(MPJLambdaWrapper<?> wrapper) {
        if (!and) {
            wrapper.or(t -> t.isNotNull(vModel));
        } else {
            wrapper.and(t -> t.isNotNull(vModel));
        }
    }

    private void getInWrapper(MPJLambdaWrapper<?> wrapper) {
        if (!and) {
            wrapper.or(qw -> {
                for (String id : dataList) {
                    if (isSqlServer) {
                        id = String.valueOf(id).replaceAll("\\[", "[[]");
                    }
                    switch (symbol) {
                        case Included:
                            qw.or().like(vModel, id);
                            break;
                        default:
                            qw.notLike(vModel, id);
                            break;
                    }
                }
            });
        } else {
            wrapper.and(qw -> {
                for (String id : dataList) {
                    if (isSqlServer) {
                        id = String.valueOf(id).replaceAll("\\[", "[[]");
                    }
                    switch (symbol) {
                        case Included:
                            qw.or().like(vModel, id);
                            break;
                        default:
                            qw.notLike(vModel, id);
                            break;
                    }
                }
            });
        }
    }

    private void getSymbolWrapper(MPJLambdaWrapper<?> wrapper) {
        switch (symbol) {
            case IsNull:
                getNullWrapper(wrapper);
                break;
            case IsNotNull:
                getNotNullWrapper(wrapper);
                break;
            case Equal:
                wrapper.eq(vModel, fieldValue);
                break;
            case NotEqual:
                wrapper.ne(vModel, fieldValue);
                break;
            case GreaterThan:
                wrapper.gt(vModel, fieldValue);
                break;
            case LessThan:
                wrapper.lt(vModel, fieldValue);
                break;
            case GreaterThanOrEqual:
                wrapper.ge(vModel, fieldValue);
                break;
            case LessThanOrEqual:
                wrapper.le(vModel, fieldValue);
                break;
            case Like:
                if (isSqlServer) {
                    fieldValue = String.valueOf(fieldValue).replaceAll("\\[", "[[]");
                }
                wrapper.like(vModel, fieldValue);
                break;
            case NotLike:
                if (isSqlServer) {
                    fieldValue = String.valueOf(fieldValue).replaceAll("\\[", "[[]");
                }
                wrapper.notLike(vModel, fieldValue);
                break;
            case Included:
            case NotIncluded:
                getInWrapper(wrapper);
                break;
            case Between:
                wrapper.between(vModel, fieldValue, fieldValueTwo);
                break;
            default:
                break;
        }
    }
}
