package jnpf.util;

import cn.afterturn.easypoi.excel.ExcelExportUtil;
import cn.afterturn.easypoi.excel.entity.ExportParams;
import cn.afterturn.easypoi.excel.entity.enmus.ExcelType;
import cn.afterturn.easypoi.excel.entity.params.ExcelExportEntity;
import cn.hutool.core.util.ObjectUtil;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.github.yulichang.wrapper.MPJLambdaWrapper;
import jnpf.annotation.JnpfField;
import jnpf.base.ActionResult;
import jnpf.base.AreaApi;
import jnpf.base.BillRuleApi;
import jnpf.base.DataInterFaceApi;
import jnpf.base.DataSourceApi;
import jnpf.base.DictionaryDataApi;
import jnpf.base.ModuleApi;
import jnpf.base.UserInfo;
import jnpf.base.VisualdevApi;
import jnpf.base.entity.DictionaryDataEntity;
import jnpf.base.entity.ModuleEntity;
import jnpf.base.entity.ProvinceEntity;
import jnpf.base.entity.VisualdevEntity;
import jnpf.base.entity.VisualdevReleaseEntity;
import jnpf.base.model.ColumnDataModel;
import jnpf.base.model.OnlineImport.ExcelImportModel;
import jnpf.base.model.OnlineImport.ImportDataModel;
import jnpf.base.model.OnlineImport.ImportFormCheckUniqueModel;
import jnpf.base.model.OnlineImport.VisualdevModelDataInfoVO;
import jnpf.base.model.datainterface.DataInterfaceActionVo;
import jnpf.base.model.datainterface.DataInterfaceModel;
import jnpf.base.model.datainterface.DataInterfacePage;
import jnpf.base.model.filter.RuleInfo;
import jnpf.base.service.FilterService;
import jnpf.base.util.DateTimeFormatConstant;
import jnpf.base.util.FlowFormDataUtil;
import jnpf.base.util.FormCheckUtils;
import jnpf.base.util.FormPublicUtils;
import jnpf.base.util.VisualBillUtil;
import jnpf.base.util.VisualUtils;
import jnpf.base.util.common.DataControlUtils;
import jnpf.base.vo.DownloadVO;
import jnpf.base.vo.PageListVO;
import jnpf.base.vo.PaginationVO;
import jnpf.constant.FileTypeConstant;
import jnpf.constant.JnpfConst;
import jnpf.constant.PermissionConst;
import jnpf.database.model.entity.DbLinkEntity;
import jnpf.database.model.superQuery.ConditionJsonModel;
import jnpf.database.model.superQuery.SuperJsonModel;
import jnpf.database.model.superQuery.SuperQueryConditionModel;
import jnpf.database.model.superQuery.SuperQueryJsonModel;
import jnpf.database.util.ConnUtil;
import jnpf.database.util.DynamicDataSourceUtil;
import jnpf.entity.FileParameter;
import jnpf.excel.ExcelExportStyler;
import jnpf.excel.ExcelHelper;
import jnpf.exception.DataException;
import jnpf.exception.WorkFlowException;
import jnpf.flowable.TaskApi;
import jnpf.flowable.TemplateApi;
import jnpf.flowable.entity.TaskEntity;
import jnpf.flowable.entity.TemplateJsonEntity;
import jnpf.model.ExcelModel;
import jnpf.model.OnlineDevData;
import jnpf.model.QueryAllModel;
import jnpf.model.SystemParamModel;
import jnpf.model.generater.GenerParamConst;
import jnpf.model.generater.GenerViewConst;
import jnpf.model.visualJson.FieLdsModel;
import jnpf.model.visualJson.FormDataModel;
import jnpf.model.visualJson.TableModel;
import jnpf.model.visualJson.TemplateJsonModel;
import jnpf.model.visualJson.config.ConfigModel;
import jnpf.model.visualJson.config.HeaderModel;
import jnpf.model.visualJson.config.RuleConfig;
import jnpf.onlinedev.VisualdevOnlineApi;
import jnpf.onlinedev.model.OnlineDevEnum.CacheKeyEnum;
import jnpf.onlinedev.model.PaginationModelExport;
import jnpf.onlinedev.model.VisualErrInfo;
import jnpf.onlinedev.util.onlineDevUtil.*;
import jnpf.permission.AuthorizeApi;
import jnpf.permission.GroupApi;
import jnpf.permission.OrganizeApi;
import jnpf.permission.PositionApi;
import jnpf.permission.RoleApi;
import jnpf.permission.UserApi;
import jnpf.permission.UserRelationApi;
import jnpf.permission.entity.GroupEntity;
import jnpf.permission.entity.OrganizeEntity;
import jnpf.permission.entity.PositionEntity;
import jnpf.permission.entity.RoleEntity;
import jnpf.permission.entity.UserEntity;
import jnpf.permission.entity.UserRelationEntity;
import jnpf.util.context.RequestContext;
import jnpf.util.visiual.JnpfKeyConsts;
import lombok.Cleanup;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Workbook;
import org.dromara.x.file.storage.core.FileInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.sql.Connection;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.StringJoiner;
import java.util.stream.Collectors;

import static jnpf.util.Constants.ADMIN_KEY;

/**
 * 数据转换(代码生成器用)
 *
 * @author JNPF开发平台组
 * @version V3.1.0
 * @copyright 引迈信息技术有限公司（https://www.jnpfsoft.com）
 * @date 2021/3/16
 */
@Slf4j
@Component
public class GeneraterSwapUtil {

    @Autowired
    private OrganizeApi organizeService;
    @Autowired
    private PositionApi positionService;
    @Autowired
    private AreaApi provinceService;
    @Autowired
    private FilterService filterService;
    @Autowired
    private UserApi userService;
    @Autowired
    private VisualdevApi visualdevService;
    @Autowired
    private VisualdevOnlineApi visualDevInfoService;
    @Autowired
    private DictionaryDataApi dictionaryDataService;
    @Autowired
    private DataInterFaceApi dataInterfaceService;
    @Autowired
    private BillRuleApi billRuleService;
    @Autowired
    private RoleApi roleService;
    @Autowired
    private GroupApi groupService;
    @Autowired
    private UserRelationApi userRelationService;
    @Autowired
    private DataSourceApi dblinkService;
    @Autowired
    private AuthorizeApi authorizeService;
    @Autowired
    private OnlineSwapDataUtils swapDataUtils;
    @Autowired
    private OnlineExcelUtil onlineExcelUtil;

    @Autowired
    private RedisUtil redisUtil;

    @Autowired
    private ModuleApi moduleService;
    @Autowired
    private FlowFormDataUtil flowFormDataUtil;

    //以下新的流程api
    @Autowired
    private TaskApi taskApi;

    @Autowired
    private TemplateApi templateApi;
    @Autowired
    private FormCheckUtils formCheckUtils;

    @Autowired
    private VisualBillUtil visualBillUtil;

    public final String regEx = "[\\[\\]\"]";


    private static long DEFAULT_CACHE_TIME = 60 * 5;

    /**
     * 日期时间戳字符串转换
     *
     * @param date
     * @param format
     * @return
     */
    public String dateSwap(String date, String format) {
        if (StringUtil.isNotEmpty(date)) {
            DateTimeFormatter ftf = DateTimeFormatter.ofPattern(format);
            if (date.contains(",")) {
                String[] dates = date.split(",");
                long time1 = Long.parseLong(dates[0]);
                long time2 = Long.parseLong(dates[1]);
                String value1 = ftf.format(LocalDateTime.ofInstant(Instant.ofEpochMilli(time1), ZoneId.systemDefault()));
                String value2 = ftf.format(LocalDateTime.ofInstant(Instant.ofEpochMilli(time2), ZoneId.systemDefault()));
                return value1 + "至" + value2;
            }
            long time = Long.parseLong(date);
            return ftf.format(LocalDateTime.ofInstant(Instant.ofEpochMilli(time), ZoneId.systemDefault()));
        }
        return date;
    }

    /**
     * 行政区划转换
     *
     * @param data
     * @return
     */
    public String provinceData(String data, Map<String, Object> localCache) {
        Map<String, String> proMap = new HashMap<>();
        if (localCache != null && localCache.containsKey("__pro_map")) {
            proMap = (Map<String, String>) localCache.get("__pro_map");
        }
        if (StringUtil.isNotEmpty(data)) {
            try {
                if (data.contains("[[")) {
                    List<String> addList = new ArrayList<>();
                    String[][] provinceDataS = JsonUtil.getJsonToBean(data, String[][].class);
                    for (String[] AddressData : provinceDataS) {
                        List<String> provList = new ArrayList(Arrays.asList(AddressData));
                        List<String> nameList = new ArrayList<>();
                        if (localCache != null) {
                            for (String info : provList) {
                                nameList.add(proMap.get(info));
                            }
                        } else {
                            List<ProvinceEntity> proList = provinceService.getProvinceList(provList);
                            for (ProvinceEntity info : proList) {
                                nameList.add(info.getFullName());
                            }
                        }
                        addList.add(String.join("/", nameList));
                    }
                    return String.join(";", addList);
                } else if (data.contains("[")) {
                    List<String> provList = JsonUtil.getJsonToList(data, String.class);
                    List<String> nameList = new ArrayList<>();
                    if (localCache != null) {
                        for (String info : provList) {
                            nameList.add(proMap.get(info));
                        }
                    } else {
                        List<ProvinceEntity> proList = provinceService.getProvinceList(provList);
                        for (ProvinceEntity info : proList) {
                            nameList.add(info.getFullName());
                        }
                    }
                    return String.join("/", nameList);
                } else {
                    String[] strs = data.split(",");
                    List<String> provList = new ArrayList(Arrays.asList(strs));
                    List<String> proNameList = new ArrayList<>();
                    if (localCache != null) {
                        for (String info : provList) {
                            proNameList.add(proMap.get(info));
                        }
                    } else {
                        List<ProvinceEntity> proList = provinceService.getProvinceList(provList);
                        for (ProvinceEntity info : proList) {
                            proNameList.add(info.getFullName());
                        }
                    }
                    return String.join("/", proNameList);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return "";
    }

    public Map<String, Object> localCache() {
        //公共数据
        String dsName = Optional.ofNullable(TenantHolder.getDatasourceId()).orElse("");
        Map<String, Object> localCache = new HashMap<>();
        //省市区
        Map<Object, Object> proMap = redisUtil.getMap(String.format("%s-%s-%d", dsName, "province", 1));
        List<Map<String, String>> proMapList = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(proMap)) {
            //分级存储
            for (int i = 1; i <= 4; i++) {
                String redisKey = String.format("%s-%s-%d", dsName, "province", i);
                if (!redisUtil.exists(redisKey)) {
                    List<ProvinceEntity> provinceEntityList = provinceService.getProListBytype(String.valueOf(i));
                    Map<String, String> provinceMap = new HashMap<>(16);
                    if (provinceEntityList != null) {
                        provinceEntityList.forEach(p -> provinceMap.put(p.getId(), p.getFullName()));
                    }
                    proMapList.add(provinceMap);
                    //区划基本不修改 不做是否缓存判断
                    redisUtil.insert(redisKey, provinceMap, RedisUtil.CAHCEWEEK);
                }
            }
        } else {
            for (int i = 1; i <= 4; i++) {
                proMapList.add(redisUtil.getMap(String.format("%s-%s-%d", dsName, "province", i)));
            }
        }

        Map<String, String> proMapr = new HashMap<>();
        proMapList.forEach(proMapr::putAll);
        localCache.put("__pro_map", proMapr);
        return localCache;
    }

    /**
     * 公司部门id转名称
     *
     * @param value
     * @return
     */
    public String comSelectValue(String value, String showLevel) {
        if (StringUtil.isNotEmpty(String.valueOf(value))) {
            OrganizeEntity organizeEntity = organizeService.getInfoById(String.valueOf(value));
            if ("all".equals(showLevel)) {
                List<OrganizeEntity> organizeListAll = organizeService.getList(false);
                String[] organizeTreeId = StringUtil.isNotEmpty(organizeEntity.getOrganizeIdTree()) ? organizeEntity.getOrganizeIdTree().split(",") : new String[]{};
                List<String> organizeTreeList = Arrays.stream(organizeTreeId).filter(t -> !t.isEmpty()).collect(Collectors.toList());
                StringJoiner joiner = new StringJoiner("/");
                for (String id : organizeTreeList) {
                    OrganizeEntity entity = organizeListAll.stream().filter(t -> t.getId().equals(id)).findFirst().orElse(null);
                    if (entity != null) {
                        joiner.add(entity.getFullName());
                    }
                }
                value = joiner.toString();
            } else {
                if (organizeEntity != null) {
                    if (organizeEntity.getCategory().equals("company")) {
                        return " ";
                    }
                    value = organizeEntity.getFullName();
                }
            }
        } else {
            value = " ";
        }
        return value;
    }

    /**
     * 公司部门id转名称(多选)
     *
     * @param ids
     * @return
     */
    public String comSelectValues(String ids, Boolean mul) {
        List<String> comValueList = new ArrayList<>();
        if (StringUtil.isEmpty(ids)) {
            return null;
        }
        String Separator = mul ? "," : "/";
        if (ids.contains("[[")) {
            String[][] idArrays = JsonUtil.getJsonToBean(ids, String[][].class);
            for (String[] array : idArrays) {
                List<String> idList = new ArrayList<>();
                for (String s : array) {
                    OrganizeEntity info = organizeService.getInfoById(s);
                    idList.add(Objects.nonNull(info) ? info.getFullName() : s);
                }
                String orgCom = String.join("/", idList);
                comValueList.add(orgCom);
            }
            return String.join(";", comValueList);
        } else if (ids.contains("[")) {
            List<String> idList = JsonUtil.getJsonToList(ids, String.class);
            List<String> nameList = new ArrayList<>();
            for (String orgId : idList) {
                OrganizeEntity info = organizeService.getInfoById(orgId);
                nameList.add(Objects.nonNull(info) ? info.getFullName() : orgId);
            }
            return String.join(Separator, nameList);
        } else {
            ids = ids.replaceAll("\"", "");
            String[] idList = ids.split(",");
            if (idList.length > 0) {
                List<String> comSelectList = new ArrayList<>();
                for (String id : idList) {
                    OrganizeEntity organizeEntity = organizeService.getInfoById(id);
                    if (organizeEntity != null) {
                        comSelectList.add(organizeEntity.getFullName());
                    }
                }
                return String.join(",", comSelectList);
            }
        }
        return null;
    }


    /**
     * 岗位id转名称
     *
     * @param id
     * @return
     */
    public String posSelectValue(String id) {
        if (StringUtil.isNotEmpty(id)) {
            PositionEntity positionApiInfo = positionService.getInfoById(id);
            if (ObjectUtil.isNotEmpty(positionApiInfo)) {
                return positionApiInfo.getFullName();
            }
            return id;
        }
        return " ";
    }

    /**
     * 岗位id转名称(多选)
     *
     * @param ids
     * @return
     */
    public String posSelectValues(String ids) {
        if (StringUtil.isEmpty(ids)) {
            return "";
        }
        List<String> posList = new ArrayList<>();
        if (ids.contains("[")) {
            List<String> idList = JsonUtil.getJsonToList(ids, String.class);
            List<String> nameList = new ArrayList<>();
            for (String orgId : idList) {
                PositionEntity info = positionService.getInfoById(orgId);
                nameList.add(Objects.nonNull(info) ? info.getFullName() : orgId);
            }
            posList = nameList;
        } else {
            String[] idList = ids.split(",");
            for (String id : idList) {
                PositionEntity positionEntity = positionService.getInfoById(id);
                if (ObjectUtil.isNotEmpty(positionEntity)) {
                    posList.add(positionEntity.getFullName());
                }
            }
        }
        return String.join(",", posList);
    }

    /**
     * 用户id转名称
     *
     * @param id
     * @return
     */
    public String userSelectValue(String id) {
        if (StringUtil.isNotEmpty(id)) {
            UserEntity userEntity = userService.getInfoById(id);
            if (ObjectUtil.isNotEmpty(userEntity)) {
                return userEntity.getRealName() + "/" + userEntity.getAccount();
            }
            return id;
        }
        return "";
    }

    /**
     * 用户id转名称(多选)
     *
     * @param ids
     * @return
     */
    public String userSelectValues(String ids) {
        //公共数据
        String dsName = Optional.ofNullable(TenantHolder.getDatasourceId()).orElse("");
        //人员
        String redisKey = dsName + CacheKeyEnum.USER.getName();
        Map<String, Object> userMap;
        if (redisUtil.exists(redisKey)) {
            userMap = redisUtil.getMap(redisKey);
            userMap = Optional.ofNullable(userMap).orElse(new HashMap<>(20));
        } else {
            userMap = userService.getUserMap("id-fullName");
            redisUtil.insert(redisKey, userMap, DEFAULT_CACHE_TIME);
        }

        if (StringUtil.isEmpty(ids)) {
            return ids;
        }
        if (ids.contains("[")) {
            List<String> nameList = new ArrayList<>();
            List<String> jsonToList = JsonUtil.getJsonToList(ids, String.class);
            for (String userId : jsonToList) {
                nameList.add(Objects.nonNull(userMap.get(userId)) ? userMap.get(userId).toString() : userId);
            }
            return String.join(";", nameList);
        } else {
            List<String> userInfoList = new ArrayList<>();
            String[] idList = ids.split(",");
            for (String userId : idList) {
                userInfoList.add(Objects.nonNull(userMap.get(userId)) ? userMap.get(userId).toString() : userId);
            }
            return String.join("-", userInfoList);
        }
    }

    /**
     * 用户组件id转名称(多选)
     *
     * @param ids
     * @return
     */
    public String usersSelectValues(String ids) {
        if (StringUtil.isEmpty(ids)) {
            return ids;
        }
        List<String> dataNoSwapInMethod = OnlinePublicUtils.getDataNoSwapInMethod(ids);
        StringJoiner valueJoin = new StringJoiner(",");
        for (String data : dataNoSwapInMethod) {
            String id = data.contains("--") ? data.substring(0, data.lastIndexOf("--")) : data;
            String type = data.contains("--") ? data.substring(data.lastIndexOf("--") + 2) : "";
            switch (type) {
                case "role":
                    RoleEntity roleEntity = roleService.getInfoById(id);
                    if (roleEntity != null) {
                        valueJoin.add(roleEntity.getFullName());
                    } else {
                        valueJoin.add(data);
                    }
                    break;
                case "position":
                    PositionEntity positionEntity = positionService.getInfoById(id);
                    if (positionEntity != null) {
                        valueJoin.add(positionEntity.getFullName());
                    } else {
                        valueJoin.add(data);
                    }
                    break;
                case "company":
                case "department":
                    OrganizeEntity organizeEntity = organizeService.getInfoById(id);
                    if (organizeEntity != null) {
                        valueJoin.add(organizeEntity.getFullName());
                    } else {
                        valueJoin.add(data);
                    }
                    break;
                case "group":
                    GroupEntity groupEntity = groupService.getInfoById(id);
                    if (groupEntity != null) {
                        valueJoin.add(groupEntity.getFullName());
                    } else {
                        valueJoin.add(data);
                    }
                    break;
                case "user":
                default:
                    UserEntity userEntity = userService.getInfoById(id);
                    if (userEntity != null) {
                        valueJoin.add(userEntity.getRealName() + "/" + userEntity.getAccount());
                    } else {
                        valueJoin.add(data);
                    }
                    break;
            }
        }
        return valueJoin.toString();
    }


    /**
     * 开关
     *
     * @param data
     * @return
     */
    public String switchSelectValue(String data, String activeTxt, String inactiveTxt) {
        if (StringUtil.isNotEmpty(data)) {
            if (data.equals("0") || data.equals("false")) {
                return inactiveTxt;
            } else if (data.equals("1") || data.equals("true")) {
                return activeTxt;
            } else {
                return data;
            }
        }
        return null;
    }




    /**
     * 弹窗
     *
     * @param interfaceId
     * @param propsValue
     * @param relationField
     * @param dataValue
     * @return
     */
    public String getPopupSelectValue(String interfaceId, String propsValue, String relationField, String dataValue, Map<String, Object> dataMaps, String json, int num, Map<String, Object> dataAll) {
        if (StringUtil.isEmpty(interfaceId)) {
            return null;
        }
        List<TemplateJsonModel> list = JsonUtil.getJsonToList(json, TemplateJsonModel.class);
        Map<String, String> infoMap = new HashMap<>();
        List<DataInterfaceModel> listParam = new ArrayList<>();
        for (TemplateJsonModel templateJsonModel : list) {
            DataInterfaceModel dataInterfaceModel = JsonUtil.getJsonToBean(templateJsonModel, DataInterfaceModel.class);
            String defaultV = "";
            if (StringUtil.isNotEmpty(templateJsonModel.getRelationField())) {
                String[] mastTable = templateJsonModel.getRelationField().split(JnpfConst.SIDE_MARK);
                String[] child = templateJsonModel.getRelationField().split("-");
                if (mastTable.length > 1) {
                    if (dataAll.get(mastTable[0]) instanceof Map) {
                        Map<String, Object> mastTableData = (Map<String, Object>) dataAll.get(mastTable[0]);
                        infoMap.put(templateJsonModel.getField(), String.valueOf(mastTableData.get(mastTable[1])));
                        defaultV = String.valueOf(mastTableData.get(mastTable[1]));
                    }
                } else if (child.length > 1) {
                    if (dataAll.get(child[0]) instanceof List) {
                        List<Map<String, Object>> chidList = (List<Map<String, Object>>) dataAll.get(child[0]);
                        for (int i = 0; i < chidList.size(); i++) {
                            Map<String, Object> objectMap = chidList.get(i);
                            if (i == num) {
                                infoMap.put(templateJsonModel.getField(), String.valueOf(objectMap.get(child[1])));
                                defaultV = String.valueOf(objectMap.get(child[1]));
                            }
                        }
                    }
                } else {
                    infoMap.put(templateJsonModel.getField(), String.valueOf(dataAll.get(templateJsonModel.getRelationField())));
                    defaultV = String.valueOf(String.valueOf(dataAll.get(templateJsonModel.getRelationField())));
                }
            }
            dataInterfaceModel.setDefaultValue(defaultV);
            listParam.add(dataInterfaceModel);
        }
        if (StringUtil.isNotEmpty(dataValue)) {
//            Object data = dataInterfaceService.infoToId(interfaceId, null, infoMap).getData();
//            List<Map<String, Object>> dataInterfaceDataList;
//            if (data instanceof ActionResult) {
//                ActionResult actionVo = (ActionResult) data;
//                dataInterfaceDataList = (List<Map<String, Object>>) actionVo.getData();
//            } else {
//                dataInterfaceDataList = (List<Map<String, Object>>) data;
//            }
            DataInterfacePage dataInterfacePage = new DataInterfacePage();
            dataInterfacePage.setParamList(listParam);
            dataInterfacePage.setInterfaceId(interfaceId);
            List<String> ids = new ArrayList<>();
            if (dataValue.startsWith("[")) {
                ids = JsonUtil.getJsonToList(dataValue, String.class);
            } else {
                ids.add(dataValue);
            }
            dataInterfacePage.setIds(ids);
            dataInterfacePage.setPropsValue(propsValue);
            dataInterfacePage.setRelationField(relationField);
            List<Map<String, Object>> dataInterfaceDataList = dataInterfaceService.infoToInfo(interfaceId, dataInterfacePage);
            if (dataValue.contains("[")) {
                List<String> valueList = JsonUtil.getJsonToList(dataValue, String.class);
                List<String> swapValue = new ArrayList<>();
                for (String va : valueList) {
                    dataInterfaceDataList.stream().filter(map ->
                            map.get(propsValue).equals(va)
                    ).forEach(
                            modelMap -> swapValue.add(String.valueOf(modelMap.get(relationField)))
                    );
                }
                return String.join(",", swapValue);
            }
            if (dataInterfaceDataList != null) {
                Map<String, Object> dataMap = dataInterfaceDataList.stream().filter(d -> d.get(propsValue).equals(dataValue)).findFirst().orElse(null);
                if (dataMap != null) {
                    dataMaps.putAll(dataMap);
                    return String.valueOf(dataMap.get(relationField));
                }
            }
            return null;
        } else {
            return null;
        }
    }

    /**
     * 弹窗
     *
     * @param interfaceId
     * @param propsValue
     * @param relationField
     * @param dataValue
     * @return
     */
    public String getPopupSelectValue(String interfaceId, String propsValue, String relationField, String dataValue, Map<String, Object> dataMaps) {
        if (StringUtil.isEmpty(interfaceId)) {
            return null;
        }
        if (StringUtil.isNotEmpty(dataValue)) {
            Object data = dataInterfaceService.infoToId(interfaceId, null, null).getData();
            List<Map<String, Object>> dataInterfaceDataList;
            if (data instanceof ActionResult) {
                ActionResult actionVo = (ActionResult) data;
                dataInterfaceDataList = (List<Map<String, Object>>) actionVo.getData();
            } else {
                dataInterfaceDataList = (List<Map<String, Object>>) data;
            }
            if (dataValue.contains("[")) {
                List<String> valueList = JsonUtil.getJsonToList(dataValue, String.class);
                List<String> swapValue = new ArrayList<>();
                for (String va : valueList) {
                    dataInterfaceDataList.stream().filter(map ->
                            map.get(propsValue).equals(va)
                    ).forEach(
                            modelMap -> swapValue.add(String.valueOf(modelMap.get(relationField)))
                    );
                }
                return swapValue.stream().collect(Collectors.joining(","));
            }
            Map<String, Object> dataMap = dataInterfaceDataList.stream().filter(d -> d.get(propsValue).equals(dataValue)).findFirst().orElse(null);
            if (dataMap != null) {
                dataMaps.putAll(dataMap);
                return String.valueOf(dataMap.get(relationField));
            }
            return null;
        } else {
            return null;
        }
    }


    public String getFileNameInJson(String fileJson) {
        if (StringUtil.isNotEmpty(fileJson) && !"null".equals(fileJson)) {
            return fileJson;
        }
        return "";
    }


    /**
     * 获取数据字典数据
     *
     * @param feild
     * @return
     */
    public String getDicName(String feild, String dictionaryTypeId) {
        if (StringUtil.isNotEmpty(feild)) {
            //去除中括号以及双引号
            feild = feild.replaceAll(regEx, "");
            //判断多选框
            String[] feilds = feild.split(",");
            if (feilds.length > 1) {
                StringBuilder feildsValue = new StringBuilder();
                DictionaryDataEntity dictionaryDataEntity;
                for (String feil : feilds) {
                    dictionaryDataEntity = dictionaryDataService.getSwapInfo(feil, dictionaryTypeId);
                    if (dictionaryDataEntity != null) {
                        feildsValue.append(dictionaryDataEntity.getFullName()).append(",");
                    } else {
                        feildsValue.append(feil).append(",");
                    }
                }
                String finalValue;
                if (StringUtil.isEmpty(feildsValue) || feildsValue.equals("")) {
                    finalValue = feildsValue.toString();
                } else {
                    finalValue = feildsValue.substring(0, feildsValue.length() - 1);
                }
                return finalValue;
            }
            DictionaryDataEntity dictionaryDataentity = dictionaryDataService.getSwapInfo(feild, dictionaryTypeId);
            if (dictionaryDataentity != null) {
                return dictionaryDataentity.getFullName();
            }
            return feild;
        }
        if (StringUtil.isNotEmpty(feild)) {
            List<DictionaryDataEntity> dicList = dictionaryDataService.getDicList(dictionaryTypeId);
        }
        return feild;
    }

    /**
     * 获取数据字典数据-
     *
     * @param feild
     * @param keyName id或encode
     * @return
     */
    public String getDicName(String feild, String dictionaryTypeId, String keyName, boolean isMultiple, String separator) {
        Object dataConversion = "";
        String redisKey = dictionaryTypeId + "-" + feild + "-" + keyName;
        if (StringUtil.isNotEmpty(feild)) {
            List<DictionaryDataEntity> dicList;
            if (redisUtil.exists(redisKey)) {
                List<Object> tmpList = redisUtil.get(redisKey, 0, -1);
                dicList = JsonUtil.getJsonToList(tmpList, DictionaryDataEntity.class);
            } else {
                dicList = dictionaryDataService.getDicList(dictionaryTypeId);
                redisUtil.insert(redisKey, dicList, DEFAULT_CACHE_TIME);
            }
            Map<String, Object> idMap = new HashMap<>(dicList.size());
            Map<String, Object> enCodeMap = new HashMap<>(dicList.size());
            for (DictionaryDataEntity dd : dicList) {
                idMap.put(dd.getId(), dd.getFullName());
                enCodeMap.put(dd.getEnCode(), dd.getFullName());
            }
            if (StringUtil.isNotEmpty(separator)) {
                separator = "/";
            }
            if ("enCode".equals(keyName)) {
                dataConversion = FormPublicUtils.getDataConversion(enCodeMap, feild, isMultiple, separator);
            } else {
                dataConversion = FormPublicUtils.getDataConversion(idMap, feild, isMultiple, separator);
            }
        }
        return dataConversion.toString();
    }

    /**
     * 获取远端数据
     *
     * @param urlId
     * @param label
     * @param value
     * @param feildValue
     * @return
     * @throws IOException
     */
    public String getDynName(String urlId, String label, String value, String feildValue, String json, int num, Map<String, Object> dataAll) {
        List<TemplateJsonModel> list = JsonUtil.getJsonToList(json, TemplateJsonModel.class);
        Map<String, String> infoMap = list.size() > 0 ? new HashMap<>() : null;
        for (TemplateJsonModel templateJsonModel : list) {
            if (StringUtil.isNotEmpty(templateJsonModel.getRelationField())) {
                String[] mastTable = templateJsonModel.getRelationField().split(JnpfConst.SIDE_MARK);
                String[] child = templateJsonModel.getRelationField().split("-");
                if (mastTable.length > 1) {
                    if (dataAll.get(mastTable[0]) instanceof Map) {
                        Map<String, Object> mastTableData = (Map<String, Object>) dataAll.get(mastTable[0]);
                        infoMap.put(templateJsonModel.getField(), String.valueOf(mastTableData.get(mastTable[1])));
                    }
                } else if (child.length > 1) {
                    if (dataAll.get(child[0]) instanceof List) {
                        List<Map<String, Object>> chidList = (List<Map<String, Object>>) dataAll.get(child[0]);
                        for (int i = 0; i < chidList.size(); i++) {
                            Map<String, Object> objectMap = chidList.get(i);
                            if (i == num) {
                                infoMap.put(templateJsonModel.getField(), String.valueOf(objectMap.get(child[1])));
                            }
                        }
                    }
                } else {
                    infoMap.put(templateJsonModel.getField(), String.valueOf(dataAll.get(templateJsonModel.getRelationField())));
                }
            }
        }
        if (StringUtil.isNotEmpty(feildValue)) {
            //去除中括号以及双引号
            feildValue = feildValue.replaceAll(regEx, "");
            //获取远端数据
            Map<String, String> a = new HashMap<>();
            ActionResult object = dataInterfaceService.infoToId(urlId, null, infoMap);
            if (object.getData() != null && object.getData() instanceof DataInterfaceActionVo) {
                DataInterfaceActionVo vo = (DataInterfaceActionVo) object.getData();
                List<Map<String, Object>> dataList = (List<Map<String, Object>>) vo.getData();
                //判断是否多选
                String[] feildValues = feildValue.split(",");
                if (feildValues.length > 0) {
                    //转换的真实值
                    StringBuilder feildVa = new StringBuilder();
                    for (String feild : feildValues) {
                        for (Map<String, Object> data : dataList) {
                            if (String.valueOf(data.get(value)).equals(feild)) {
                                feildVa.append(data.get(label)).append(",");
                            }
                        }
                    }
                    String finalValue;
                    if (StringUtil.isEmpty(feildVa) || feildVa.equals("")) {
                        finalValue = feildVa.toString();
                    } else {
                        finalValue = feildVa.substring(0, feildVa.length() - 1);
                    }
                    return finalValue;
                }
                for (Map<String, Object> data : dataList) {
                    if (feildValue.equals(String.valueOf(data.get(value)))) {
                        return data.get(label).toString();
                    }
                    return feildValue;
                }
            }
            return feildValue;
        }
        return feildValue;
    }

    /**
     * 获取远端数据
     *
     * @param urlId
     * @param name
     * @param id
     * @param children
     * @param feildValue
     * @return
     */
    public String getDynName(String urlId, String name, String id, String children, String feildValue, boolean mul) {
        List<String> result = new ArrayList<>();
        String sep = ",";
        if (mul) {
            sep = "/";
        }
        if (StringUtil.isNotEmpty(feildValue)) {
            Map<String, String> a = new HashMap<>();
            ActionResult object = dataInterfaceService.infoToId(urlId, null, null);
            List<Map<String, Object>> dataList = (List<Map<String, Object>>) object.getData();
//			if (actionVo.getData() instanceof List) {
//				dataList = (List<Map<String, Object>>) actionVo.getData();
//			}
            JSONArray dataAll = JsonUtil.getListToJsonArray(dataList);
            List<Map<String, Object>> list = new ArrayList<>();
            treeToList(id, name, children, dataAll, list);
            String value = feildValue.replaceAll("\\[", "").replaceAll("\\]", "");
            Map<String, String> resultMap = new HashMap<>();
            list.stream().forEach(t -> {
                resultMap.put(String.valueOf(t.get(id)), String.valueOf(t.get(name)));
            });

            if (feildValue.startsWith("[[")) {
                String[][] fv = JsonUtil.getJsonToBean(feildValue, String[][].class);
                StringJoiner f1 = new StringJoiner(",");
                for (String[] f : fv) {
                    StringJoiner v1 = new StringJoiner("/");
                    for (String v : f) {
                        v1.add(resultMap.get(v));
                    }
                    f1.add(v1.toString());
                }
                return f1.toString();
            } else if (feildValue.startsWith("[")) {
                List<String> fvs = JsonUtil.getJsonToList(feildValue, String.class);
                return fvs.stream().map(resultMap::get).collect(Collectors.joining(sep));
            } else {
                return resultMap.get(feildValue);
            }
        }
        return feildValue;
    }


    /**
     * 获取远端数据
     *
     * @param urlId
     * @param name
     * @param id
     * @param children
     * @param feildValue
     * @param mul        是否多选
     * @param isFullPath 全路径
     * @return
     */
    public String getDynName(String urlId, String name, String id, String children, String feildValue, boolean mul, boolean isFullPath, String json, int num, Map<String, Object> dataAll1) {
        List<TemplateJsonModel> list = JsonUtil.getJsonToList(json, TemplateJsonModel.class);
        Map<String, String> infoMap = CollectionUtils.isNotEmpty(list) ? new HashMap<>() : null;
        for (TemplateJsonModel templateJsonModel : list) {
            if (StringUtil.isNotEmpty(templateJsonModel.getRelationField())) {
                String[] mastTable = templateJsonModel.getRelationField().split(JnpfConst.SIDE_MARK);
                String[] child = templateJsonModel.getRelationField().split("-");
                if (mastTable.length > 1) {
                    if (dataAll1.get(mastTable[0]) instanceof Map) {
                        Map<String, Object> mastTableData = (Map<String, Object>) dataAll1.get(mastTable[0]);
                        infoMap.put(templateJsonModel.getField(), String.valueOf(mastTableData.get(mastTable[1])));
                    }
                } else if (child.length > 1) {
                    if (dataAll1.get(child[0]) instanceof List) {
                        List<Map<String, Object>> chidList = (List<Map<String, Object>>) dataAll1.get(child[0]);
                        for (int i = 0; i < chidList.size(); i++) {
                            Map<String, Object> objectMap = chidList.get(i);
                            if (i == num) {
                                infoMap.put(templateJsonModel.getField(), String.valueOf(objectMap.get(child[1])));
                            }
                        }
                    }
                } else {
                    infoMap.put(templateJsonModel.getField(), String.valueOf(dataAll1.get(templateJsonModel.getRelationField())));
                }
            }
        }

        if (StringUtil.isNotEmpty(feildValue)) {
            Map<String, String> a = new HashMap<>();
            ActionResult data = dataInterfaceService.infoToId(urlId, null, infoMap);
            List<Map<String, Object>> dataList = (List<Map<String, Object>>) data.getData();
            JSONArray dataAll = JsonUtil.getListToJsonArray(dataList);
            List<Map<String, Object>> datalist = new ArrayList<>();
            treeToList(id, name, children, dataAll, datalist);
            String value = feildValue.replaceAll("\\[", "").replaceAll("\\]", "");
            Map<String, Object> resultMap = new HashMap<>();
            datalist.forEach(t -> {
                resultMap.put(String.valueOf(t.get(id)), String.valueOf(t.get(name)));
            });
            Object dataConversion = FormPublicUtils.getDataConversion(resultMap, feildValue, mul, "/");
            feildValue = String.valueOf(dataConversion);
        }
        return feildValue;
    }

    /**
     * 树转成list
     **/
    private void treeToList(String id, String fullName, String children, JSONArray data, List<Map<String, Object>> result) {
        if (data != null) {
            for (int i = 0; i < data.size(); i++) {
                JSONObject ob = data.getJSONObject(i);
                Map<String, Object> tree = new HashMap<>(16);
                tree.put(id, String.valueOf(ob.get(id)));
                tree.put(fullName, String.valueOf(ob.get(fullName)));
                result.add(tree);
                if (ob.get(children) != null) {
                    JSONArray childArray = ob.getJSONArray(children);
                    treeToList(id, fullName, children, childArray, result);
                }
            }
        }
    }

    /**
     * 生成单据规则
     *
     * @param encode
     * @param isCache
     * @return
     * @throws DataException
     */
    @DS("")
    public String getBillNumber(String encode, Boolean isCache) throws DataException {
        return billRuleService.getBillNumber(encode).getData();
    }

    /**
     * 生成单据规则方法2（表单内单据配置）
     *
     * @param visualId 功能id
     * @param ruleId   规则id
     * @param ruleJson 规则josn
     * @param obj      表单数据（联动用）
     * @return
     * @throws DataException
     */
    @DS("")
    public String getBillNumber2(String visualId, String ruleId, String ruleJson, Object obj) throws DataException {
        Map<String, Object> dataMap = JsonUtil.entityToMap(obj);
        FieLdsModel fieLdsModel = new FieLdsModel();
        ConfigModel config = new ConfigModel();
        config.setRuleType(2);
        config.setJnpfKey(JnpfKeyConsts.BILLRULE);
        config.setFormId(ruleId);
        RuleConfig ruleConfig = JsonUtil.getJsonToBean(ruleJson, RuleConfig.class);
        config.setRuleConfig(ruleConfig);
        fieLdsModel.setConfig(config);
        Object billNumber = visualBillUtil.getBillNumber(visualId, fieLdsModel, dataMap, null);
        return billNumber.toString();
    }


    @DS("")
    public UserEntity getUser(String userId) {
        return userService.getInfoById(userId);
    }


    public String getGroupSelect(String groupIds) {
        if (StringUtil.isEmpty(groupIds)) {
            return groupIds;
        }
        List<String> swapList = new ArrayList<>();
        if (groupIds.contains("[")) {
            List<String> groups = JsonUtil.getJsonToList(groupIds, String.class);
            for (String g : groups) {
                GroupEntity info = groupService.getInfoById(g);
                String s = info != null ? info.getFullName() : "";
                swapList.add(s);
            }
        } else {
            GroupEntity info = groupService.getInfoById(groupIds);
            swapList.add(info != null ? info.getFullName() : "");
        }
        return String.join(",", swapList);
    }

    public String getRoleSelect(String roleIds) {
        if (StringUtil.isEmpty(roleIds)) {
            return roleIds;
        }
        List<String> swapList = new ArrayList<>();
        if (roleIds.contains("[")) {
            List<String> groups = JsonUtil.getJsonToList(roleIds, String.class);
            for (String g : groups) {
                RoleEntity info = roleService.getInfoById(g);
                String s = info != null ? info.getFullName() : "";
                swapList.add(s);
            }
        } else {
            RoleEntity info = roleService.getInfoById(roleIds);
            swapList.add(info != null ? info.getFullName() : "");
        }
        return String.join(",", swapList);
    }


    /**
     * 高级查询
     *
     * @param conditionModel
     * @param entity
     * @param num
     * @return
     */
    public Integer getCondition(SuperQueryConditionModel conditionModel, Object entity, int num) {
        QueryWrapper<?> queryWrapper = conditionModel.getObj();
        List<ConditionJsonModel> queryConditionModels = conditionModel.getConditionList();
        String op = conditionModel.getMatchLogic();
        String tableName = conditionModel.getTableName();
        List<ConditionJsonModel> useCondition = new ArrayList<>();
        for (ConditionJsonModel queryConditionModel : queryConditionModels) {
            if (queryConditionModel.getTableName().equalsIgnoreCase(tableName)) {
                if (queryConditionModel.getField().contains("jnpf")) {
                    String child = queryConditionModel.getField();
                    String s1 = child.substring(child.lastIndexOf(JnpfConst.SIDE_MARK_PRE)).replace(JnpfConst.SIDE_MARK_PRE, "");
                    queryConditionModel.setField(s1);
                }
                if (queryConditionModel.getField().startsWith("tableField")) {
                    String child = queryConditionModel.getField();
                    String s1 = child.substring(child.indexOf("-") + 1);
                    queryConditionModel.setField(s1);
                }
                useCondition.add(queryConditionModel);
            }
        }

        if (queryConditionModels.isEmpty() || useCondition.size() < 1) {
            return num;
        }
        if (!useCondition.isEmpty()) {
            num += 1;
        }
        //处理控件 转换为有效值
        for (ConditionJsonModel queryConditionModel : useCondition) {
            String jnpfKey = queryConditionModel.getJnpfKey();
            String fieldValue = queryConditionModel.getFieldValue();
            if (StringUtil.isEmpty(fieldValue)) {
                if (jnpfKey.equals(JnpfKeyConsts.CASCADER) || jnpfKey.equals(JnpfKeyConsts.CHECKBOX) || jnpfKey.equals(JnpfKeyConsts.ADDRESS)) {
                    queryConditionModel.setFieldValue("[]");
                } else {
                    queryConditionModel.setFieldValue("");
                }
                if (queryConditionModel.getSymbol().equals("like")) {
                    queryConditionModel.setSymbol("==");
                } else if (queryConditionModel.getSymbol().equals("notLike")) {
                    queryConditionModel.setSymbol("<>");
                }
            }
            if (jnpfKey.equals(JnpfKeyConsts.DATE)) {
                String startTime = "";
                if (StringUtil.isNotEmpty(fieldValue)) {
                    Long o1 = Long.valueOf(fieldValue);
                    startTime = DateUtil.daFormatHHMMSS(o1);
                }
                queryConditionModel.setFieldValue(startTime);
            } else if (jnpfKey.equals(JnpfKeyConsts.CREATETIME) || jnpfKey.equals(JnpfKeyConsts.MODIFYTIME)) {
                String startTime = "";
                if (StringUtil.isNotEmpty(fieldValue)) {
                    Long o1 = Long.valueOf(fieldValue);
                    startTime = DateUtil.daFormatHHMMSS(o1);
                }
                queryConditionModel.setFieldValue(startTime);
            }
        }
        //反射获取数据库实际字段
        Class<?> aClass = entity.getClass();

        queryWrapper.and(tw -> {
            for (ConditionJsonModel conditionJsonModel : useCondition) {
                String conditionField = conditionJsonModel.getField();
                String jnpfKey = conditionJsonModel.getJnpfKey();
                Field declaredField = null;
                try {
                    declaredField = aClass.getDeclaredField(conditionField);
                } catch (NoSuchFieldException e) {
                    e.printStackTrace();
                }
                declaredField.setAccessible(true);
                String field = declaredField.getAnnotation(TableField.class).value();
                String fieldValue = conditionJsonModel.getFieldValue();
                String symbol = conditionJsonModel.getSymbol();
                if ("AND".equalsIgnoreCase(op)) {
                    if (symbol.equals("==")) {
                        tw.and(qw -> {
                                    List<String> multJnpf = new ArrayList() {{
                                        add(JnpfKeyConsts.CASCADER);
                                        add(JnpfKeyConsts.COMSELECT);
                                        add(JnpfKeyConsts.ADDRESS);
                                        add(JnpfKeyConsts.SELECT);
                                        add(JnpfKeyConsts.TREESELECT);
                                    }};
                                    if (JnpfKeyConsts.CHECKBOX.equals(jnpfKey) || (multJnpf.contains(jnpfKey) && conditionJsonModel.isFormMultiple())) {
                                        //todo 多选，高级查询只选一个，需要拼成数组查询，其他控件目前没发现，后续添加至此
                                        String eavalue = "";
                                        if (fieldValue.contains("[")) {
                                            eavalue = "[" + fieldValue + "]";
                                        } else {
                                            JSONArray jarr = new JSONArray();
                                            jarr.add(fieldValue);
                                            eavalue = jarr.toJSONString();
                                        }
                                        qw.eq(field, eavalue);
                                    } else if (!jnpfKey.equals(JnpfKeyConsts.NUM_INPUT) && !jnpfKey.equals(JnpfKeyConsts.CALCULATE)) {
                                        qw.eq(field, fieldValue);
                                    } else {
                                        if (StringUtil.isNotEmpty(fieldValue)) {
                                            qw.eq(field, fieldValue);
                                        }
                                    }
                                    if (StringUtil.isEmpty(fieldValue)) {
                                        qw.or(
                                                ew -> ew.isNull(field)
                                        );
                                    }
                                }
                        );
                    } else if (symbol.equals(">=")) {
                        tw.ge(field, fieldValue);
                    } else if (symbol.equals("<=")) {
                        tw.and(ew -> {
                            ew.le(field, fieldValue);
                            ew.and(
                                    qw -> qw.ne(field, "")
                            );
                        });
                    } else if (symbol.equals(">")) {
                        tw.gt(field, fieldValue);
                    } else if (symbol.equals("<")) {
                        tw.and(ew -> {
                            ew.lt(field, fieldValue);
                            ew.and(
                                    qw -> qw.ne(field, "")
                            );
                        });
                    } else if (symbol.equals("<>")) {
                        tw.and(ew -> {
                            ew.ne(field, fieldValue);
                            if (StringUtil.isNotEmpty(fieldValue)) {
                                ew.or(
                                        qw -> qw.isNull(field)
                                );
                            } else {
                                ew.and(
                                        qw -> qw.isNotNull(field)
                                );
                            }
                        });
                    } else if (symbol.equals("like")) {
                        tw.and(ew -> {
                            if (StringUtil.isNotEmpty(fieldValue)) {
                                ew.like(field, fieldValue);
                            } else {
                                ew.isNull(field);
                            }
                        });
                    } else if (symbol.equals("notLike")) {
                        tw.and(ew -> {
                            if (StringUtil.isNotEmpty(fieldValue)) {
                                ew.notLike(field, fieldValue);
                                ew.or(
                                        qw -> qw.isNull(field)
                                );
                            } else {
                                ew.isNotNull(field);
                            }
                        });
                    }
                } else {
                    if (symbol.equals("==")) {
                        tw.or(
                                qw -> qw.eq(field, fieldValue)
                        );
                    } else if (symbol.equals(">=")) {
                        tw.or(
                                qw -> qw.ge(field, fieldValue)
                        );
                    } else if (symbol.equals("<=")) {
                        tw.or(
                                qw -> qw.le(field, fieldValue)
                        );
                    } else if (symbol.equals(">")) {
                        tw.or(
                                qw -> qw.gt(field, fieldValue)
                        );
                    } else if (symbol.equals("<")) {
                        tw.or(
                                qw -> qw.lt(field, fieldValue)
                        );
                    } else if (symbol.equals("<>")) {
                        tw.or(
                                qw -> qw.ne(field, fieldValue)
                        );
                        if (StringUtil.isNotEmpty(fieldValue)) {
                            tw.or(
                                    qw -> qw.isNull(field)
                            );
                        }
                    } else if (symbol.equals("like")) {
                        if (StringUtil.isNotEmpty(fieldValue)) {
                            tw.or(
                                    qw -> qw.like(field, fieldValue)
                            );
                        } else {
                            tw.or(
                                    qw -> qw.isNull(field)
                            );
                        }
                    } else if (symbol.equals("notLike")) {
                        if (StringUtil.isNotEmpty(fieldValue)) {
                            tw.or(
                                    qw -> qw.notLike(field, fieldValue)
                            );
                            tw.or(
                                    qw -> qw.isNull(field)
                            );
                        } else {
                            tw.or(
                                    qw -> qw.isNotNull(field)
                            );
                        }
                    }
                }
            }
        });
        return num;
    }

    /**
     * 取主表交集
     *
     * @param lists
     * @return
     */
    public List<String> getIntersection(List<List<String>> lists) {
        if (lists == null || lists.isEmpty()) {
            return new ArrayList<>();
        }
        ArrayList<List<String>> arrayList = new ArrayList<>(lists);
        for (List<String> list : arrayList) {
            if (list == null || list.isEmpty()) {
                return new ArrayList<>();
            }
        }
        List<String> intersection = arrayList.get(0);
        for (List<String> list : arrayList) {
            intersection.retainAll(list);
        }
        return intersection;
    }

    public Map<String, Object> putCache(Map<String, Object> localCache) {
        //读取系统控件 所需编码 id
        Map<String, Object> depMap = organizeService.getOrgEncodeAndName("department");
        localCache.put("_dep_map", depMap);
        Map<String, Object> comMap = organizeService.getOrgNameAndId("");
        localCache.put("_com_map", comMap);
        Map<String, Object> posMap = positionService.getPosEncodeAndName();
        localCache.put("_pos_map", posMap);
        Map<String, Object> userMap = userService.getUserNameAndIdMap();
        localCache.put("_user_map", userMap);
        Map<String, Object> roleMap = roleService.getRoleNameAndIdMap();
        localCache.put("_role_map", roleMap);
        Map<String, Object> groupMap = groupService.getGroupEncodeMap();
        localCache.put("_group_map", groupMap);
        return localCache;
    }


    /**
     * 时间是否在范围内
     *
     * @param jnpfField
     * @param parse
     * @return
     */
    private boolean timeInRange(JnpfField jnpfField, Date parse) {
        boolean flag = true;
        if (StringUtil.isNotEmpty(jnpfField.startTime())) {
            long startTime = Long.parseLong(jnpfField.startTime());
            flag = parse.after(new Date(startTime));
        }
        if (flag && StringUtil.isNotEmpty(jnpfField.endTime())) {
            long endTime = Long.parseLong(jnpfField.endTime());
            flag = parse.before(new Date(endTime));
        }
        return flag;
    }

    private List<String> checkOptionsControl(boolean multiple, Map<String, Object> insMap, String vModel, String label, Map<String, Object> cacheMap, List<String> valueList, StringJoiner errInfo) {
        boolean error = false;
        if (!multiple) {
            //非多选填入多选值
            if (valueList.size() > 1) {
                error = true;
                errInfo.add(label + "非多选");
            }
        }
        List<String> dataList = new ArrayList<>();
        if (!error) {
            boolean errorHapen = false;
            for (String va : valueList) {
                Object vo = cacheMap.get(va);
                if (vo == null) {
                    errorHapen = true;
                } else {
                    dataList.add(vo.toString());
                }

            }
            if (errorHapen) {
                errInfo.add(label + "值不正确");
            } else {
                insMap.put(vModel, !multiple ? dataList.get(0) : JsonUtil.getObjectToString(dataList));
            }
        }
        return dataList;
    }

    /**
     * 递归查询
     *
     * @param label
     * @param value
     * @param Children
     * @param data
     * @param options
     */
    public static void getOptions(String label, String value, String Children, JSONArray data, List<Map<String, Object>> options) {
        for (int i = 0; i < data.size(); i++) {
            JSONObject ob = data.getJSONObject(i);
            Map<String, Object> tree = new HashMap<>(16);
            tree.put(value, String.valueOf(ob.get(value)));
            tree.put(label, String.valueOf(ob.get(label)));
            options.add(tree);
            if (ob.get(Children) != null) {
                JSONArray childrenArray = ob.getJSONArray(Children);
                getOptions(label, value, Children, childrenArray, options);
            }
        }
    }

    /**
     * 获取用户主件查询条件
     *
     * @param value
     * @return
     */
    public List<String> usersSelectQuery(String value) {
        List<String> userSList = new ArrayList<>();
        String userValue = value.substring(0, value.indexOf("--"));
        UserEntity userEntity = userService.getInfoById(userValue);
        if (userEntity != null) {
            //在用户关系表中取出
            List<UserRelationEntity> groupRel = Optional.ofNullable(userRelationService.getListByObjectType(userValue, PermissionConst.GROUP)).orElse(new ArrayList<>());
            List<UserRelationEntity> orgRel = Optional.ofNullable(userRelationService.getListByObjectType(userValue, PermissionConst.ORGANIZE)).orElse(new ArrayList<>());
            List<UserRelationEntity> posRel = Optional.ofNullable(userRelationService.getListByObjectType(userValue, PermissionConst.POSITION)).orElse(new ArrayList<>());
            List<UserRelationEntity> roleRel = Optional.ofNullable(userRelationService.getListByObjectType(userValue, PermissionConst.ROLE)).orElse(new ArrayList<>());

            if (!groupRel.isEmpty()) {
                for (UserRelationEntity split : groupRel) {
                    userSList.add(split.getObjectId());
                }
            }
            if (StringUtil.isNotEmpty(userEntity.getOrganizeId())) {
                //向上递归 查出所有上级组织
                List<String> allUpOrgIDs = new ArrayList<>();
                organizeService.upWardRecursion(allUpOrgIDs, userEntity.getOrganizeId());
                userSList.addAll(allUpOrgIDs);
            }
            if (!posRel.isEmpty()) {
                for (UserRelationEntity split : posRel) {
                    userSList.add(split.getObjectId());
                }
            }
            if (!roleRel.isEmpty()) {
                for (UserRelationEntity split : roleRel) {
                    userSList.add(split.getObjectId());
                }
            }
            return userSList;
        } else {
            return null;
        }
    }

    /**
     * 获取用户主件查询条件(多选)
     *
     * @param values
     * @return
     */
    public List<String> usersSelectQuery(List<String> values) {
        List<String> userSList = new ArrayList<>();
        for (String value : values) {
            String userValue = value.substring(0, value.indexOf("--"));
            UserEntity userEntity = userService.getInfoById(userValue);
            if (userEntity != null) {
                //在用户关系表中取出
                List<UserRelationEntity> groupRel = Optional.ofNullable(userRelationService.getListByObjectType(userValue, PermissionConst.GROUP)).orElse(new ArrayList<>());
                List<UserRelationEntity> orgRel = Optional.ofNullable(userRelationService.getListByObjectType(userValue, PermissionConst.ORGANIZE)).orElse(new ArrayList<>());
                List<UserRelationEntity> posRel = Optional.ofNullable(userRelationService.getListByObjectType(userValue, PermissionConst.POSITION)).orElse(new ArrayList<>());
                List<UserRelationEntity> roleRel = Optional.ofNullable(userRelationService.getListByObjectType(userValue, PermissionConst.ROLE)).orElse(new ArrayList<>());

                if (!groupRel.isEmpty()) {
                    for (UserRelationEntity split : groupRel) {
                        userSList.add(split.getObjectId());
                    }
                }
                if (StringUtil.isNotEmpty(userEntity.getOrganizeId())) {
                    //向上递归 查出所有上级组织
                    List<String> allUpOrgIDs = new ArrayList<>();
                    organizeService.upWardRecursion(allUpOrgIDs, userEntity.getOrganizeId());
                    userSList.addAll(allUpOrgIDs);
                }
                if (!posRel.isEmpty()) {
                    for (UserRelationEntity split : posRel) {
                        userSList.add(split.getObjectId());
                    }
                }
                if (!roleRel.isEmpty()) {
                    for (UserRelationEntity split : roleRel) {
                        userSList.add(split.getObjectId());
                    }
                }
            }
        }
        return userSList;
    }

    @DS("")
    public List<RuleInfo> getFilterCondition(String id) {
        return filterService.getCondition(id);
    }

    public static List convertToList(Object obj) {
        return OnlineSwapDataUtils.convertToList(obj);
    }

    public static String convertValueToString(String obj, boolean mult, boolean isOrg) {
        return OnlineSwapDataUtils.convertValueToString(obj, mult, isOrg);
    }

    /**
     * 获取数据连接
     *
     * @param dbLink
     * @return
     */
    public DbLinkEntity getDataSource(String dbLink) {
//        QueryWrapper<DbLinkEntity> queryWrapper = new QueryWrapper<>();
//        queryWrapper.lambda().eq(DbLinkEntity::getFullName, dbLink);
        return dblinkService.getOneByDbLink(dbLink);
    }


    /**
     * 静态数据转换
     *
     * @param param    需要转换的值
     * @param options  静态数据模型
     * @param key      label key-value编码对应
     * @param multiple 是否多选
     * @return 转换后的值
     */
    public static String selectStaitcSwap(String param, String options, String key, String label, boolean multiple) {
        List<String> textList = new ArrayList<>();
        List<Map> optionsList = JsonUtil.getJsonToList(options, Map.class);
        if (multiple) {
            List<String> jsonToList = JsonUtil.getJsonToList(param, String.class);
            for (String list1 : jsonToList) {
                if (list1.contains("[")) {
                    List<String> textList2 = new ArrayList<>();
                    List<String> jsonToList2 = JsonUtil.getJsonToList(list1, String.class);
                    for (String str : jsonToList2) {
                        textList2.add(loop(optionsList, str, key, label));
                    }
                    textList.add(String.join("/", textList2));
                } else {
                    textList.add(loop(optionsList, list1, key, label));
                }
            }
        } else {
            if (param.contains("[")) {
                List<String> textList2 = new ArrayList<>();
                List<String> jsonToList = JsonUtil.getJsonToList(param, String.class);
                for (String str : jsonToList) {
                    textList2.add(loop(optionsList, str, key, label));
                }
                textList.add(String.join("/", textList2));
            } else {
                textList.add(loop(optionsList, param, key, label));
            }
        }
        return String.join(",", textList);
    }

    public static String loop(List<Map> options, String oneData, String key, String label) {
        for (int i = 0; i < options.size(); i++) {
            if (options.get(i).get(key).equals(oneData)) {
                return options.get(i).get(label).toString();
            } else if (options.get(i).get("children") != null) {
                List<Map> children = JsonUtil.getJsonToList(options.get(i).get("children"), Map.class);
                String loop = loop(children, oneData, key, label);
                if (loop != null) {
                    return loop;
                }
            }
        }
        return null;
    }

    /**
     * 功能表单获取流程信息-导入绑定多流程的第一个
     *
     * @param formId
     * @return
     * @throws WorkFlowException
     */
    public String getFlowTempJsonId(String formId) throws WorkFlowException {
        String flowTemjsonId = "";
        //todo 功能表单获取流程信息调整取流程id位置变化
//        if (form == null || StringUtil.isEmpty(form.getFlowId())) {
//            throw new WorkFlowException("该功能未配置流程不可用");
//        }
//        FlowTemplateInfoVO vo = flowTemplateService.info(form.getFlowId());
//        if (vo == null || StringUtil.isEmpty(vo.getFlowTemplateJson()) || "[]".equals(vo.getFlowTemplateJson())) {
//            throw new WorkFlowException("流程未设计！");
//        }
//        List<FlowJsonModel> collect = JsonUtil.getJsonToList(vo.getFlowTemplateJson(), FlowJsonModel.class);
//        flowTemjsonId = collect.get(0).getId();
        return flowTemjsonId;
    }


    /**
     * 输入时表单时间字段根据格式转换去尾巴
     *
     * @param form
     */
    public static void swapDatetime(Object form) {
        Field[] declaredFields = form.getClass().getDeclaredFields();
        for (Field f : declaredFields) {
            try {
                //副表处理
                if (f.getType().getName().startsWith("jnpf.model")) {
                    if (!f.isAccessible()) {
                        f.setAccessible(true);
                    }
                    Object o = f.get(form);
                    if (o == null) {
                        continue;
                    }
                    swapDatetime(o);
                    f.set(form, o);
                    continue;
                }
                //子表处理
                if (List.class.isAssignableFrom(f.getType())) {
                    Type type = f.getGenericType();
                    if (type instanceof ParameterizedType) {
                        if (!f.isAccessible()) {
                            f.setAccessible(true);
                        }
                        List list = getList(f, f.get(form));
                        for (Object o : list) {
                            swapDatetime(o);
                        }
                        if (list.size() > 0) {
                            f.set(form, list);
                        }
                    }
                    continue;
                }
                //主表处理
                if (f.getAnnotation(JnpfField.class) == null) {
                    continue;
                }
                JnpfField annotation = f.getAnnotation(JnpfField.class);
                if (!"date".equals(annotation.jnpfKey()) || StringUtil.isEmpty(annotation.format())) {
                    continue;
                }
                String format = annotation.format();
                f.setAccessible(true);
                if (f.get(form) != null && Long.parseLong(String.valueOf(f.get(form))) > 0) {
                    Date date = new Date(Long.parseLong(String.valueOf(f.get(form))));
                    String completionStr = "";
                    switch (format) {
                        case "yyyy":
                            completionStr = "-01-01 00:00:00";
                            break;
                        case "yyyy-MM":
                            completionStr = "-01 00:00:00";
                            break;
                        case "yyyy-MM-dd":
                            completionStr = " 00:00:00";
                            break;
                        case "yyyy-MM-dd HH":
                            completionStr = ":00:00";
                            break;
                        case "yyyy-MM-dd HH:mm":
                            completionStr = ":00";
                            break;
                        default:
                            break;
                    }
                    String datestr = DateUtil.dateToString(date, format);
                    long time = DateUtil.stringToDate(datestr + completionStr).getTime();
                    f.set(form, String.valueOf(time));
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

    public static List getList(Field field, Object object) {
        List resultList = new ArrayList<>();
        if (object != null) {
            try {
                Class clzz = object.getClass();
                //反射调用获取到list的size方法来获取到集合的大小
                Method sizeMethod = clzz.getDeclaredMethod("size");
                if (!sizeMethod.isAccessible()) {
                    sizeMethod.setAccessible(true);
                }
                //集合长度
                int size = (int) sizeMethod.invoke(object);
                //循环遍历获取到数据
                for (int i = 0; i < size; i++) {
                    //反射获取到list的get方法
                    Method getMethod = clzz.getDeclaredMethod("get", int.class);
                    //调用get方法获取数据
                    if (!getMethod.isAccessible()) {
                        getMethod.setAccessible(true);
                    }
                    Object invoke = getMethod.invoke(object, i);
                    resultList.add(invoke);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return resultList;

    }

    /**
     * 小数转换带上0
     *
     * @param decimalValue
     */
    public static String getDecimalStr(Object decimalValue) {
        if (Objects.isNull(decimalValue)) {
            return "";
        }
        if (decimalValue instanceof BigDecimal) {
            BigDecimal bd = (BigDecimal) decimalValue;
            return bd.toPlainString();
        }
        return String.valueOf(decimalValue);
    }

    /**
     * 获取当前组织完整路径
     *
     * @param orgId
     * @return
     */
    public String getCurrentOrgIds(String orgId, String showLevel) {
        return flowFormDataUtil.getCurrentOrgIds(orgId, showLevel);
    }

    /* ****************以下vue3转换信息****************** */

    /**
     * 通过副表名取副表数据map
     *
     * @param data
     * @param tableName
     */
    public static Map<String, Object> getMastTabelData(Object data, String tableName) {
        Map<String, Object> map = JsonUtil.entityToMap(data);
        Map<String, Object> mapRes = new HashMap<>();
        for (String key : map.keySet()) {
            String[] jnpf_s = key.split(JnpfConst.SIDE_MARK);
            if (jnpf_s.length == 2 && jnpf_s[0].contains(tableName)) {
                mapRes.put(jnpf_s[1], map.get(key));
            }
        }
        return mapRes;
    }


    /**
     * List数据转换
     *
     * @param realList
     * @return
     */
    public List<Map<String, Object>> swapDataList(List<Map<String, Object>> realList, GenerParamConst paramConst, String moduleId, boolean inlineEdit) {

        List<FieLdsModel> fieLdsModels = JsonUtil.getJsonToList(paramConst.getFields(), FieLdsModel.class);
        List<FieLdsModel> fields = new ArrayList<>();
        VisualUtils.recursionFields(fieLdsModels, fields);
        //树形-添加父级字段+_id
        if (OnlineDevData.COLUMNTYPE_FIVE.equals(paramConst.getColumnType())) {
            realList.forEach(item -> {
                item.put(paramConst.getColumnParentField() + "_id", item.get(paramConst.getColumnParentField()));
            });
        }
        //数据转换
        realList = swapDataUtils.getSwapList(realList, fields, moduleId, inlineEdit);
        return realList;
    }

    /**
     * 列表补充流程状态
     *
     * @param realList
     * @throws WorkFlowException
     */
    @DS("")
    public void getFlowStatus(List<Map<String, Object>> realList) throws WorkFlowException {
        swapDataUtils.getFlowStatus(realList);
    }

    /**
     * List数据转树形和分组
     *
     * @param realList
     * @param columnDataStr
     * @return
     */
    public List<Map<String, Object>> swapDataList(List<Map<String, Object>> realList, String columnDataStr, String subField) {
        ColumnDataModel columnDataModel = JsonUtil.getJsonToBean(columnDataStr, ColumnDataModel.class);
        //判断数据是否分组
        if (OnlineDevData.COLUMNTYPE_THREE.equals(columnDataModel.getType())) {
            realList = OnlineDevListUtils.groupData(realList, columnDataModel);
        }
        //树形列表
        if (OnlineDevData.COLUMNTYPE_FIVE.equals(columnDataModel.getType())) {
            columnDataModel.setSubField(subField);
            realList = OnlineDevListUtils.treeListData(realList, columnDataModel);
        }
        return realList;
    }

    /**
     * 树形列表转换
     *
     * @param realList
     * @param parentField
     * @param subField
     * @return
     */
    public List<Map<String, Object>> treeTable(List<Map<String, Object>> realList, String parentField, String subField) {
        ColumnDataModel columnDataModel = new ColumnDataModel();
        columnDataModel.setSubField(subField);
        columnDataModel.setParentField(parentField);
        realList = OnlineDevListUtils.treeListData(realList, columnDataModel);
        return realList;
    }

    /**
     * 列表分组方法
     *
     * @param realList
     * @param groupField 分组字段
     * @param firstField 非分组第一个字段（用于展示分组数据）
     * @return
     */
    public static List<Map<String, Object>> groupTable(List<Map<String, Object>> realList, String groupField, String firstField) {
        Map<String, List<Map<String, Object>>> twoMap = new LinkedHashMap<>(16);

        for (Map<String, Object> realMap : realList) {
            String value = String.valueOf(realMap.get(groupField));
            if (realMap.get(groupField) instanceof Double) {
                value = realMap.get(groupField).toString().replaceAll(".0+?$", "").replaceAll("[.]$", "");
            }
            boolean isKey = twoMap.get(value) != null;
            if (isKey) {
                List<Map<String, Object>> maps = twoMap.get(value);
                maps.add(realMap);
                twoMap.put(value, maps);
            } else {
                List<Map<String, Object>> childrenList = new ArrayList<>();
                childrenList.add(realMap);
                twoMap.put(value, childrenList);
            }
        }

        List<Map<String, Object>> resultList = new ArrayList<>();
        for (String key : twoMap.keySet()) {
            Map<String, Object> thirdMap = new HashMap<>(16);
            thirdMap.put(firstField, !key.equals("null") ? key : "");
            thirdMap.put("top", true);
            thirdMap.put("id", RandomUtil.uuId());
            thirdMap.put("children", twoMap.get(key));
            resultList.add(thirdMap);
        }
        return resultList;
    }

    /**
     * 编辑form数据转换
     *
     * @param dataMap
     * @return
     */
    public Map<String, Object> swapDataForm(Map<String, Object> dataMap, GenerParamConst paramConst) {
        List<FieLdsModel> fieLdsModels = JsonUtil.getJsonToList(paramConst.getFields(), FieLdsModel.class);
        List<FieLdsModel> fields = new ArrayList<>();
        VisualUtils.recursionFields(fieLdsModels, fields);
        //是流程添加流程相关字段
        swapDataUtils.getFLowFields(dataMap);
        //数据转换
        return this.swapDataForm(dataMap, fields, null, paramConst.getTableFieldKey(), paramConst.getTableRenames());
    }

    private Map<String, Object> swapDataForm(Map<String, Object> dataMap, List<FieLdsModel> fields, Map<String, Object> mainMap
            , Map<String, String> tableField, Map<String, String> tableRename) {
        if (dataMap == null || dataMap.isEmpty()) {
            return new HashMap<>();
        }
        for (FieLdsModel item : fields) {
            String jnpfKey = item.getConfig().getJnpfKey();
            String vModel = item.getVModel();
            String dataType = item.getConfig().getDataType();
            Boolean isMultiple = Objects.nonNull(item.getMultiple()) ? item.getMultiple() : false;

            //获取原字段数据
            FormPublicUtils.relationGetJnpfId(dataMap, jnpfKey, dataMap.get(vModel), vModel);

            List<String> systemConditions = new ArrayList() {{
                add(JnpfKeyConsts.CURRORGANIZE);
                add(JnpfKeyConsts.CURRDEPT);
                add(JnpfKeyConsts.CURRPOSITION);
            }};
            //多选二维数组
            List<String> multTow = new ArrayList() {{
                add(JnpfKeyConsts.CASCADER);
                add(JnpfKeyConsts.ADDRESS);
            }};
            //一维维数组
            List<String> multOne = new ArrayList() {{
                add(JnpfKeyConsts.CHECKBOX);
            }};
            List<String> nullIsList = new ArrayList() {{
                add(JnpfKeyConsts.UPLOADFZ);
                add(JnpfKeyConsts.UPLOADIMG);
            }};
            if (Objects.nonNull(dataMap.get(vModel))) {
                if (multTow.contains(jnpfKey) && isMultiple) {
                    //二维数据转换
                    dataMap.replace(vModel, JSONObject.parseArray(dataMap.get(vModel).toString(), List.class));
                } else if (multTow.contains(jnpfKey) || isMultiple || multOne.contains(jnpfKey)) {
                    //一维数据转换
                    dataMap.replace(vModel, JSONObject.parseArray(dataMap.get(vModel).toString(), String.class));
                }
            } else if (!JnpfKeyConsts.CHILD_TABLE.equals(jnpfKey)) {
                if (systemConditions.contains(jnpfKey)) {
                    dataMap.put(vModel, " ");
                }
                if (nullIsList.contains(jnpfKey)) {
                    dataMap.put(vModel, Collections.emptyList());
                }
                continue;
            }
            switch (jnpfKey) {
                case JnpfKeyConsts.RATE:
                case JnpfKeyConsts.SLIDER:
                    BigDecimal value = new BigDecimal(0);
                    if (dataMap.get(vModel) != null) {
                        value = new BigDecimal(dataMap.get(vModel).toString());
                    }
                    dataMap.put(vModel, value);
                    break;
                case JnpfKeyConsts.SWITCH:
                    dataMap.put(vModel, dataMap.get(vModel) != null ? Integer.parseInt(String.valueOf(dataMap.get(vModel))) : null);
                    break;
                case JnpfKeyConsts.DATE:
                case JnpfKeyConsts.DATE_CALCULATE:
                    Long dateTime = DateTimeFormatConstant.getDateObjToLong(dataMap.get(vModel));
                    dataMap.put(vModel, dateTime != null ? dateTime : dataMap.get(vModel));
                    break;
                //编辑是的系统控件转换
                case JnpfKeyConsts.CURRORGANIZE:
                case JnpfKeyConsts.CURRDEPT:
                    String orgName = organizeService.getNameByIdStr(String.valueOf(dataMap.get(vModel)));
                    dataMap.put(vModel,  StringUtil.isNotEmpty(orgName) ? orgName : " ");
                    break;
                case JnpfKeyConsts.CREATEUSER:
                case JnpfKeyConsts.MODIFYUSER:
                    UserEntity userEntity = userService.getInfoById(String.valueOf(dataMap.get(vModel)));
                    String userValue = Objects.nonNull(userEntity) ? userEntity.getAccount().equalsIgnoreCase(ADMIN_KEY)
                            ? "管理员/" + ADMIN_KEY : userEntity.getRealName() + "/" + userEntity.getAccount() : String.valueOf(dataMap.get(vModel));
                    dataMap.put(vModel, userValue);
                    break;
                case JnpfKeyConsts.CURRPOSITION:
                    String posName = positionService.getNameByIdStr(String.valueOf(dataMap.get(vModel)));
                    dataMap.put(vModel, StringUtil.isNotEmpty(posName) ? posName : " ");
                    break;
                case JnpfKeyConsts.CREATETIME:
                case JnpfKeyConsts.MODIFYTIME:
                    if (ObjectUtil.isNotEmpty(dataMap.get(vModel))) {
                        Long dateLong = Long.parseLong(String.valueOf(dataMap.get(vModel)));
                        String dateStr = DateUtil.dateFormat(new Date(dateLong));
                        dataMap.put(vModel, dateStr);
                    }
                    break;
                case JnpfKeyConsts.UPLOADFZ:
                case JnpfKeyConsts.UPLOADIMG:
                    //数据传递-乱塞有bug强行置空
                    if (ObjectUtil.isNotEmpty(dataMap.get(vModel))) {
                        List<Map<String, Object>> fileList = new ArrayList<>();
                        try {
                            fileList = JsonUtil.getJsonToListMap(dataMap.get(vModel).toString());
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        dataMap.put(vModel, fileList);
                    }
                    break;
                case JnpfKeyConsts.CHILD_TABLE:
                    List<FieLdsModel> childrens = item.getConfig().getChildren();
                    String childTableRename = "";
                    try {
                        childTableRename = tableRename.get(tableField.get(vModel));
                    } catch (Exception e) {
                    }
                    if (StringUtil.isNotEmpty(childTableRename)) {
                        vModel = childTableRename + "List";
                    }
                    vModel = DataControlUtils.initialLowercase(vModel);
                    List<Map<String, Object>> childList = (List<Map<String, Object>>) dataMap.get(vModel);
                    if (CollectionUtils.isEmpty(childList)) {
                        break;
                    }
                    for (int i = 0; i < childList.size(); i++) {
                        Map<String, Object> childMap = childList.get(i);
                        childList.set(i, this.swapDataForm(childMap, childrens, dataMap, tableField, tableRename));
                    }
                    dataMap.put(vModel, childList);
                    break;
                default:
                    dataMap.put(vModel, dataMap.get(vModel));
                    break;
            }
        }
        return dataMap;
    }


    /**
     * 详情Detail数据转换
     *
     * @param map
     * @return
     */
    public Map<String, Object> swapDataDetail(Map<String, Object> map, JSONArray filedArray, String moduleId, boolean inlineEdit) {
        List<FieLdsModel> fieLdsModels = JsonUtil.getJsonToList(filedArray, FieLdsModel.class);
        List<FieLdsModel> fields = new ArrayList<>();
        VisualUtils.recursionFields(fieLdsModels, fields);
        //数据转换
        if (map != null) {
            Map<String, Object> finalMap = map;
            List<Map<String, Object>> realList = new ArrayList() {{
                add(finalMap);
            }};
            realList = swapDataUtils.getSwapInfo(realList, fields, moduleId, inlineEdit, null);
            map = realList.get(0);
        }
        return map;
    }

    /**
     * 导入数据
     */
    public ExcelImportModel importData(GenerParamConst paramConst, List<Map<String, Object>> dataList, ImportFormCheckUniqueModel uniqueModel) throws WorkFlowException {
        FormDataModel formDataModel = new FormDataModel();
        formDataModel.setBusinessKeyList(new String[]{});
        formDataModel.setBusinessKeyTip("");
        ExcelImportModel importModel = new ExcelImportModel();
        Map<String, Object> localCache = swapDataUtils.getlocalCache();
        List<Map<String, Object>> failResult = new ArrayList<>();
        List<FieLdsModel> fieLdsModels = JsonUtil.getJsonToList(paramConst.getFields(), FieLdsModel.class);
        List<FieLdsModel> fields = new ArrayList<>();
        VisualUtils.recursionFields(fieLdsModels, fields);
        uniqueModel.setMain(true);
        uniqueModel.setTableModelList(JsonUtil.getJsonToList(paramConst.getTableList(), TableModel.class));
        DbLinkEntity linkEntity = dblinkService.getInfo(uniqueModel.getDbLinkId());
        uniqueModel.setLinkEntity(linkEntity);

        //流程表单导入，传流程小id查询，查询各个版本flowIds用于过滤数据
        String mainFlowID = null;
        if (StringUtil.isNotEmpty(uniqueModel.getFlowId())) {
            List<TemplateJsonEntity> flowVersionIds = templateApi.getFlowIdsByTemplate(uniqueModel.getFlowId());
            uniqueModel.setFlowId(uniqueModel.getFlowId());
            uniqueModel.setFlowIdList(flowVersionIds.stream().map(TemplateJsonEntity::getId).distinct().collect(Collectors.toList()));
            mainFlowID = flowVersionIds.stream().filter(t -> Objects.equals(t.getState(), 1)).findFirst().orElse(new TemplateJsonEntity()).getId();
        }
        try {
            for (int i = 0, len = dataList.size(); i < len; i++) {
                Map<String, Object> data = dataList.get(i);
                //导入时默认第一个流程
                data.put(FlowFormConstant.FLOWID, mainFlowID);
                data.put(TableFeildsEnum.FLOWID.getField(), mainFlowID);
                Map<String, Object> resultMap = new HashMap<>(data);
                StringJoiner errInfo = new StringJoiner(",");
                Map<String, Object> errorMap = new HashMap<>(data);

                List<String> errList = onlineExcelUtil.checkExcelData(fields, data, localCache, resultMap, errorMap, uniqueModel);
                //业务主键判断--导入新增或者跟新
                VisualErrInfo visualErrInfo;
                try {
                    DynamicDataSourceUtil.switchToDataSource(linkEntity);
                    visualErrInfo = formCheckUtils.checkBusinessKey(fields, resultMap, uniqueModel.getTableModelList(), formDataModel, null);
                } finally {
                    DynamicDataSourceUtil.clearSwitchDataSource();
                }

                if (uniqueModel.isUpdate()) {
                    if (ObjectUtil.isNotEmpty(visualErrInfo) && StringUtil.isNotEmpty(visualErrInfo.getId())) {
                        uniqueModel.setId(visualErrInfo.getId());
                        //判断流程是否已发起
                        if (StringUtil.isNotEmpty(visualErrInfo.getFlowTaskId())) {
                            String finalTaskId = visualErrInfo.getFlowTaskId();
                            List<String> flowIdList = new ArrayList<>();
                            flowIdList.add(finalTaskId);
                            List<TaskEntity> tasks = taskApi.getInfosSubmit(flowIdList.toArray(new String[]{}), TaskEntity::getStatus, TaskEntity::getId);
                            if (tasks.size() > 0) {
                                boolean errorMsg = tasks.stream().filter(t -> Objects.equals(t.getStatus(), 0)).count() == 0;
                                String msg = "已发起流程，导入失败";
                                if (errorMsg) {
                                    errorMap.put("errorsInfo", msg);
                                    failResult.add(errorMap);
                                    continue;
                                }
                            }
                        }
                    } else {
                        String excelHas = formCheckUtils.checkBusinessKeyExcel(formDataModel, resultMap, uniqueModel);
                        if (StringUtil.isNotEmpty(excelHas)) {
                            continue;
                        }
                    }
                } else {
                    if (ObjectUtil.isNotEmpty(visualErrInfo) && StringUtil.isNotEmpty(visualErrInfo.getErrMsg())) {
                        errorMap.put("errorsInfo", visualErrInfo.getErrMsg());
                        failResult.add(errorMap);
                        continue;
                    }
                    String excelHas = formCheckUtils.checkBusinessKeyExcel(formDataModel, resultMap, uniqueModel);
                    if (StringUtil.isNotEmpty(excelHas)) {
                        errorMap.put("errorsInfo", excelHas);
                        failResult.add(errorMap);
                        continue;
                    }
                }
                swapDataUtils.checkUnique(fields, data, errList, uniqueModel);

                errList.stream().forEach(t -> {
                    if (StringUtil.isNotEmpty(t)) {
                        errInfo.add(t);
                    }
                });
                if (errInfo.length() > 0) {
                    errorMap.put("errorsInfo", errInfo.toString());
                    failResult.add(errorMap);
                } else {
                    List<ImportDataModel> importDataModel = uniqueModel.getImportDataModel();
                    ImportDataModel model = new ImportDataModel();
                    model.setId(uniqueModel.getId());
                    Map<String, Map<String, Object>> map = new HashMap<>(16);
                    Map<String, Object> tableMap = new HashMap<>(16);
                    for (Object key : resultMap.keySet().toArray()) {
                        if (paramConst.getTableFieldKey().get(key) != null) {
                            tableMap.put(paramConst.getTableFieldKey().get(key) + "List", resultMap.remove(key));
                        }
                    }
                    resultMap.putAll(map);
                    resultMap.putAll(tableMap);
                    model.setResultData(resultMap);
                    importDataModel.add(model);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new WorkFlowException("导入异常！");
        }
        importModel.setFnum(failResult.size());
        importModel.setSnum(dataList.size() - failResult.size());
        importModel.setResultType(failResult.size() > 0 ? 1 : 0);
        importModel.setFailResult(failResult);
        return importModel;
    }


    /**
     * vue3获取数据过滤方案列表
     *
     * @param columnStr
     * @param appColumnStr
     * @return
     */
    public List<RuleInfo> getFilterRules(String columnStr, String appColumnStr) {
        ColumnDataModel columnDataModel = JsonUtil.getJsonToBean(columnStr, ColumnDataModel.class);
        ColumnDataModel appColumnDataModel = JsonUtil.getJsonToBean(appColumnStr, ColumnDataModel.class);
        List<Map> ruleList = JsonUtil.getJsonToList(columnDataModel.getRuleList().getConditionList(), Map.class);
        List<Map> appRuleList = JsonUtil.getJsonToList(appColumnDataModel.getRuleList().getConditionList(), Map.class);
        boolean isPc = RequestContext.isOrignPc();
        List<RuleInfo> res = JsonUtil.getJsonToList(ruleList, RuleInfo.class);
        if (!isPc) {
            res = JsonUtil.getJsonToList(appRuleList, RuleInfo.class);
        }
        return res;
    }

    public QueryWrapper wrapperHandle(String columnStr, String appColumnStr, QueryWrapper<?> wrapper, Class<?> aClass, String type, String tableName) {
        try {
            // 避免空and
            wrapper.apply(" 1=1 ");
            List<RuleInfo> ruleInfos = getFilterRules(columnStr, appColumnStr);
            for (RuleInfo info : ruleInfos) {
                String field = info.getField();
                if ("main".equals(type) && field.contains("-")) {
                    continue;
                }
                if ("main".equals(type) && field.contains(JnpfConst.SIDE_MARK)) {
                    continue;
                }
                if ("sub".equals(type) && !field.contains("-")) {
                    continue;
                }
                if ("sub-jnpf".equals(type) && !field.contains(JnpfConst.SIDE_MARK)) {
                    continue;
                }
                String fieldName = field;
                String table = "";
                if (field.contains("-")) {
                    fieldName = field.split("-")[1];
                    if (!tableName.equals(field.split("-")[0])) {
                        continue;
                    }
                }
                if (field.contains(JnpfConst.SIDE_MARK)) {
                    fieldName = field.split(JnpfConst.SIDE_MARK)[1];
                    table = field.split(JnpfConst.SIDE_MARK)[0];
                    table = table.replace(JnpfConst.SIDE_MARK_PRE, "");
                }
                if ("sub-jnpf".equals(type) && !tableName.equals(table)) {
                    continue;
                }
                Field declaredField = aClass.getDeclaredField(fieldName);
                declaredField.setAccessible(true);
                String fieldDb = declaredField.getAnnotation(TableField.class).value();
                GenUtil genUtil = JsonUtil.getJsonToBean(info, GenUtil.class);
                genUtil.setOperator(info.getOperator());
                genUtil.solveValue(wrapper, fieldDb);
            }
            return wrapper;
        } catch (Exception e) {
            return wrapper;
        }
    }

    /**
     * 是否只有主表过滤
     *
     * @param columnStr
     * @param appColumnStr
     * @return
     */
    public boolean onlyMainFilter(String columnStr, String appColumnStr) {
        List<RuleInfo> ruleInfos = getFilterRules(columnStr, appColumnStr);
        for (RuleInfo info : ruleInfos) {
            if (info.getField().contains(JnpfConst.SIDE_MARK) || info.getField().contains("-")) {
                return false;
            }
        }
        return true;
    }

    /**
     * 输入时表单时间字段根据格式转换去尾巴
     *
     * @param obj 数据
     */
    public static String swapDatetime(JSONArray fieldsStr, Object obj, Map<String, String> tableRename) {
        List<FieLdsModel> fieLdsModels = JsonUtil.getJsonToList(fieldsStr, FieLdsModel.class);
        Map<String, Object> map = JsonUtil.entityToMap(obj);
        for (String tabelRealName : tableRename.keySet()) {
            String reName = DataControlUtils.initialLowercase(tableRename.get(tabelRealName));
            map.put(tabelRealName + "List", map.get(reName + "List"));
        }
        OnlineSwapDataUtils.swapDatetime(fieLdsModels, map);
        for (String tabelRealName : tableRename.keySet()) {
            if (map.get(tabelRealName + "List") != null) {
                String reName = DataControlUtils.initialLowercase(tableRename.get(tabelRealName));
                JSONArray listToJsonArray = JsonUtil.getListToJsonArray((List) map.get(tabelRealName + "List"));
                map.replace(reName + "List", listToJsonArray);
            }
        }
        return JsonUtil.getObjectToString(map);
    }

    /**
     * 三种搜索条件组合查询
     *
     * @param queryAllModel
     * @return
     */
    @DS("")
    public MPJLambdaWrapper getConditionAllTable(QueryAllModel queryAllModel) {
        UserInfo userInfo = UserProvider.getUser();
        try {
            queryAllModel.setSystemCode(RequestContext.getAppCode());
            DbLinkEntity linkEntity = queryAllModel.getDbLink() != null ? getDataSource(queryAllModel.getDbLink()) : null;
            DynamicDataSourceUtil.switchToDataSource(linkEntity);
            @Cleanup Connection connection = ConnUtil.getConnOrDefault(linkEntity);
            queryAllModel.setDbType(connection.getMetaData().getDatabaseProductName().trim());
        } catch (Exception e) {
        } finally {
            DynamicDataSourceUtil.clearSwitchDataSource();
        }

        MPJLambdaWrapper wrapper = queryAllModel.getWrapper();
        List<List<SuperJsonModel>> superList = new ArrayList<>();

        //高级查询
        String superQuery = queryAllModel.getSuperJson();
        if (StringUtil.isNotEmpty(superQuery)) {
            List<SuperJsonModel> list = new ArrayList<>();
            SuperJsonModel jsonToBean = JsonUtil.getJsonToBean(queryAllModel.getSuperJson(), SuperJsonModel.class);
            list.add(jsonToBean);
            superList.add(list);
        }
        //数据过滤
        String ruleQuery = queryAllModel.getRuleJson();
        if (StringUtil.isNotEmpty(ruleQuery)) {
            List<SuperJsonModel> list = new ArrayList<>();
            SuperJsonModel jsonToBean = JsonUtil.getJsonToBean(queryAllModel.getRuleJson(), SuperJsonModel.class);
            list.add(jsonToBean);
            superList.add(list);
        }
        //数据权限  不为空有开启权限（存在多权限--权限方案之间用or，和其他条件之间用and）
        if (queryAllModel.getModuleId() != null) {
            if (!userInfo.getIsAdministrator()) {
                List<SuperJsonModel> authorizeListAll = authorizeService.getConditionSql(queryAllModel.getModuleId(),queryAllModel.getSystemCode());
                if (CollectionUtils.isNotEmpty(authorizeListAll)) {
                    superList.add(authorizeListAll);
                } else {
                    return null;
                }
            }
        }
        //系统参数替换
        List<String> json = superList.stream().flatMap(List::stream).map(SuperJsonModel::toString).collect(Collectors.toList());
        Map<String, String> systemFieldValue = userService.getSystemFieldValue(new SystemParamModel(json));
        for (List<SuperJsonModel> superJsonModels : superList) {
            for (SuperJsonModel superJsonModel : superJsonModels) {
                for (SuperQueryJsonModel superQueryJsonModel : superJsonModel.getConditionList()) {
                    for (FieLdsModel fieLdsModel : superQueryJsonModel.getGroups()) {
                        OnlineProductSqlUtils.replaceSystemParam(systemFieldValue, fieLdsModel);
                    }
                }
            }
        }
        QueryUtil queryUtil = new QueryUtil();
        queryAllModel.setQueryList(superList);
        queryUtil.queryList(queryAllModel);
        return wrapper;
    }

    /**
     * 视图代码生成数据接口调用在线开发
     *
     * @param hasPage 是否分页
     * @param viewKey 视图主键
     * @return
     */
    @DS("")
    public PageListVO getInterfaceData(Object pagination, GenerViewConst paramConst, Boolean hasPage, String viewKey) {
        boolean isPc = "pc".equals(ServletUtil.getHeader("jnpf-origin" ));
        VisualdevReleaseEntity visualdevEntity = new VisualdevReleaseEntity();
        visualdevEntity.setInterfaceId(paramConst.getInterfaceId());
        visualdevEntity.setInterfaceParam(paramConst.getInterfaceParam().toJSONString());
        ColumnDataModel columnDataModel = new ColumnDataModel();
        columnDataModel.setType(paramConst.getColumnType());
        columnDataModel.setHasPage(hasPage);
        columnDataModel.setViewKey(viewKey);
        if(isPc){
            columnDataModel.setSearchList(paramConst.getSearchList().toString());
        }else{
            columnDataModel.setSearchList(paramConst.getSearchListApp().toString());
        }
        PaginationModelExport jsonToBean = JsonUtil.getJsonToBean(pagination, PaginationModelExport.class);
        jsonToBean.setQueryJson(JsonUtil.getObjectToString(pagination));
        List<Map<String, Object>> interfaceData = swapDataUtils.getInterfaceData(visualdevEntity, jsonToBean, columnDataModel);
        if (isPc && columnDataModel.getType() == 3) {
            //分组数据转换
            interfaceData = groupTable(interfaceData, paramConst.getGroupField(), paramConst.getFirstField());
        }
        //返回对象
        PageListVO vo = new PageListVO();
        vo.setList(interfaceData);
        PaginationVO page = JsonUtil.getJsonToBean(jsonToBean, PaginationVO.class);
        vo.setPagination(page);
        return vo;
    }

    @DS("")
    public DownloadVO exportInterfaceData(Object pagination, GenerViewConst paramConst, Boolean hasPage, String viewKey) {
        boolean isPc = "pc".equals(ServletUtil.getHeader("jnpf-origin" ));
        VisualdevReleaseEntity visualdevEntity = new VisualdevReleaseEntity();
        visualdevEntity.setInterfaceId(paramConst.getInterfaceId());
        visualdevEntity.setInterfaceParam(paramConst.getInterfaceParam().toJSONString());
        ColumnDataModel columnDataModel = new ColumnDataModel();
        columnDataModel.setType(paramConst.getColumnType());
        columnDataModel.setHasPage(hasPage);
        columnDataModel.setViewKey(viewKey);
        columnDataModel.setComplexHeaderList(JsonUtil.getJsonToList(paramConst.getComplexHeaderList().toString(),HeaderModel.class));
        if(isPc){
            columnDataModel.setSearchList(paramConst.getSearchList().toString());
            columnDataModel.setColumnList(paramConst.getColumnData().toString());
        }else{
            columnDataModel.setSearchList(paramConst.getSearchListApp().toString());
            columnDataModel.setColumnList(paramConst.getColumnDataApp().toString());
        }
        PaginationModelExport jsonToBean = JsonUtil.getJsonToBean(pagination, PaginationModelExport.class);
        jsonToBean.setQueryJson(JsonUtil.getObjectToString(pagination));
        List<Map<String, Object>> realList = swapDataUtils.getInterfaceData(visualdevEntity, jsonToBean, columnDataModel);

        ModuleEntity menuInfo = moduleService.getInfo(jsonToBean.getMenuId());
        String[] keys = jsonToBean.getSelectKey();
        List<Object> selectIds = Arrays.asList(jsonToBean.getSelectIds());
        realList = "2".equals(jsonToBean.getDataType()) ? realList.stream().filter(t -> selectIds.contains(t.get(columnDataModel.getViewKey()))).collect(Collectors.toList()) : realList;
        return VisualUtils.createModelExcelApiData(JsonUtil.getObjectToString(columnDataModel), realList, Arrays.asList(keys), "表单信息", menuInfo.getFullName(), new ExcelModel());
    }

    /**
     * 根据菜单ID获取菜单名称
     *
     * @param menuId
     * @return
     */
    @DS("")
    public String getMenuName(String menuId) {
        String name = "";
        if (StringUtil.isNotEmpty(menuId)) {
            ModuleEntity menuInfo = moduleService.getInfo(menuId);
            if (menuInfo != null && StringUtil.isNotEmpty(menuInfo.getFullName())) {
                name = menuInfo.getFullName();
            }
        }
        return name;
    }

    /**
     * 根据表单获取所有字段
     *
     * @param fieldStr
     * @return
     */
    @DS("")
    public ExcelModel getExcelParams(String fieldStr, List<String> selectKey) {
        FormDataModel formJson = new FormDataModel();
        formJson.setFields(fieldStr);
        return swapDataUtils.getDefaultValue(JsonUtil.getObjectToString(formJson), selectKey);
    }

    /**
     * 导出Excel
     *
     * @param entitys
     * @param list
     * @param keys
     * @param menuId
     * @param moduleId
     * @return
     */
    @DS("")
    public DownloadVO creatModelExcel(List<ExcelExportEntity> entitys, List<Map<String, Object>> list, String[] keys, String menuId, String moduleId,
                                      GenerParamConst generParamConst, boolean inlineEdit) {
        //数据转换
        list = this.swapDataList(list, generParamConst, moduleId, inlineEdit);

        String menuFullName = this.getMenuName(menuId);
        DownloadVO vo = DownloadVO.builder().build();
        ExportParams exportParams = new ExportParams(null, "表单信息");
        exportParams.setType(ExcelType.XSSF);
        try {
            @Cleanup Workbook workbook = new HSSFWorkbook();
            if (entitys.size() > 0) {
                if (list.size() == 0) {
                    list.add(new HashMap<>());
                }
                //去除空数据
                List<Map<String, Object>> dataList = new ArrayList<>();
                for (Map<String, Object> map : list) {
                    int i = 0;
                    for (String key : keys) {
                        //子表
                        if (key.toLowerCase().startsWith("tablefield")) {
                            String tableField = key.substring(0, key.indexOf("-"));
                            String field = key.substring(key.indexOf("-") + 1);
                            Object o = map.get(tableField);
                            if (o != null) {
                                List<Map<String, Object>> childList = (List<Map<String, Object>>) o;
                                for (Map<String, Object> childMap : childList) {
                                    if (childMap.get(field) != null) {
                                        i++;
                                    }
                                }
                            }
                        } else {
                            Object o = map.get(key);
                            if (o != null) {
                                i++;
                            }
                        }
                    }
                    if (i > 0) {
                        dataList.add(map);
                    }
                }
                List<ExcelExportEntity> mergerEntitys = new ArrayList<>(entitys);
                List<Map<String, Object>> mergerList = new ArrayList<>(dataList);
                //复杂表头-表头和数据处理
                List<HeaderModel> complexHeaderList = JsonUtil.getJsonToList(generParamConst.getComplexHeaderList(), HeaderModel.class);
                if (!Objects.equals(generParamConst.getColumnType(), 3) && !Objects.equals(generParamConst.getColumnType(), 5)) {
                    entitys = VisualUtils.complexHeaderHandel(entitys, complexHeaderList, Objects.equals(generParamConst.getColumnType(), 4));
                    dataList = VisualUtils.complexHeaderDataHandel(dataList, complexHeaderList, Objects.equals(generParamConst.getColumnType(), 4));
                }

                exportParams.setStyle(ExcelExportStyler.class);
                workbook = ExcelExportUtil.exportExcel(exportParams, entitys, dataList);
                VisualUtils.mergerVertical(workbook, mergerEntitys, mergerList);
                ExcelModel excelModel = this.getExcelParams(generParamConst.getFields().toJSONString(), Arrays.asList(keys));
                ExcelHelper helper = new ExcelHelper();
                helper.init(workbook, exportParams, entitys, excelModel);
                helper.doPreHandle();
                helper.doPostHandle();
            }
            String fileName = menuFullName + "_" + DateUtil.dateNow("yyyyMMddHHmmss") + ".xls";
            MultipartFile multipartFile = ExcelUtil.workbookToCommonsMultipartFile(workbook, fileName);
            FileInfo fileInfo = FileUploadUtils.uploadFile(new FileParameter(FileTypeConstant.TEMPORARY, fileName), multipartFile);
            vo.setName(fileInfo.getFilename());
            vo.setUrl(UploaderUtil.uploaderFile(fileInfo.getFilename() + "#" + FileTypeConstant.TEMPORARY) + "&name=" + fileName);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return vo;
    }


    //以下新的流程api

    /**
     * 获取流程版本列表
     *
     * @param templateId
     * @return
     */
    @DS("")
    public List<String> getFlowIds(String templateId) {
        List<String> flowIds = new ArrayList<>();
        if (StringUtil.isNotEmpty(templateId)) {
            flowIds.addAll(templateApi.getFlowIdsByTemplateId(templateId));
            if (CollectionUtils.isNotEmpty(flowIds)) {
                return flowIds;
            }
            String templateByVersionId = templateApi.getTemplateByVersionId(templateId);
            if (StringUtil.isNotEmpty(templateByVersionId)) {
                flowIds.addAll(templateApi.getFlowIdsByTemplateId(templateByVersionId));
            }
        }
        if (CollectionUtils.isEmpty(flowIds)) {
            flowIds.add("noFlowVer");
        }
        return flowIds;
    }

    /**
     * 删除流程任务
     *
     * @param flowTaskId
     * @return
     */
    @DS("")
    public String deleteFlowTask(String flowTaskId) {
        String errMsg = "";
        TaskEntity taskEntity = taskApi.getInfoSubmit(flowTaskId, TaskEntity::getId, TaskEntity::getParentId, TaskEntity::getFullName, TaskEntity::getStatus);
        if (taskEntity != null) {
            try {
                taskApi.delete(taskEntity);
            } catch (Exception e) {
                errMsg = e.getMessage();
            }
        }

        return errMsg;
    }
}
