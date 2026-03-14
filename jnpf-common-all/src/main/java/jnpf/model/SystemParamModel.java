package jnpf.model;

import jnpf.constant.DataInterfaceVarConst;
import jnpf.util.StringUtil;
import lombok.Data;

import java.util.*;
import java.util.stream.Collectors;

@Data
public class SystemParamModel {

    public static final Set<String> MARKERS = Collections.unmodifiableSet(new HashSet<>(Arrays.asList(
            DataInterfaceVarConst.CURRENTTIME,
            DataInterfaceVarConst.ORGANDSUB,
            DataInterfaceVarConst.ORGANIZEANDPROGENY,
            DataInterfaceVarConst.USERANDSUB,
            DataInterfaceVarConst.USERANDPROGENY,
            DataInterfaceVarConst.POSITIONANDSUB,
            DataInterfaceVarConst.POSITIONANDPROGENY,
            DataInterfaceVarConst.USER,
            DataInterfaceVarConst.POSITIONID,
            DataInterfaceVarConst.ORG
    )));

    private String str;
    private List<String> list;

    /**
     * 全量系统参数查询
     */
    public SystemParamModel() {
        this.list = new ArrayList<>(MARKERS);
    }

    /**
     * 字符串过滤查询
     * @param str 字符串
     */
    public SystemParamModel(String str) {
        this.list = StringUtil.isNotEmpty(str) ?
                MARKERS.stream().filter(str::contains).collect(Collectors.toList())
                : new ArrayList<>();
    }

    /**
     * 集合过滤查询
     * @param list 字符串集合
     */
    public SystemParamModel(List<String> list) {
        List<String> needList=new ArrayList<>();
        for (String string : list) {
            needList.addAll(MARKERS.stream().filter(string::contains).collect(Collectors.toList()));
        }
        this.list = needList.stream().distinct().collect(Collectors.toList());
    }

}
