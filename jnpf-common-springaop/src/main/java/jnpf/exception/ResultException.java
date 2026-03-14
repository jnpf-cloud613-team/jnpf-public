package jnpf.exception;

import cn.dev33.satoken.exception.NotLoginException;
import cn.dev33.satoken.exception.NotPermissionException;
import cn.dev33.satoken.exception.NotRoleException;
import cn.dev33.satoken.exception.SameTokenInvalidException;
import cn.hutool.http.useragent.UserAgent;
import cn.hutool.http.useragent.UserAgentUtil;
import com.alibaba.fastjson.JSON;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jnpf.base.ActionResult;
import jnpf.base.ActionResultCode;
import jnpf.base.UserInfo;
import jnpf.base.entity.LogEntity;
import jnpf.config.ConfigValueUtil;
import jnpf.constant.MsgCode;
import jnpf.database.util.NotTenantPluginHolder;
import jnpf.database.util.TenantDataSourceUtil;
import jnpf.provider.system.LogProvider;
import jnpf.util.IpUtil;
import jnpf.util.JsonUtil;
import jnpf.util.RandomUtil;
import jnpf.util.ReflectionUtil;
import jnpf.util.ServletUtil;
import jnpf.util.StringUtil;
import jnpf.util.TenantHolder;
import jnpf.util.TenantProvider;
import jnpf.util.UserProvider;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.web.ErrorProperties;
import org.springframework.boot.autoconfigure.web.servlet.error.BasicErrorController;
import org.springframework.boot.web.servlet.error.DefaultErrorAttributes;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author JNPF开发平台组
 * @version V3.1.0
 * @copyright 引迈信息技术有限公司
 * @date 2021/3/16 10:51
 */
@Slf4j
@RestController
@RestControllerAdvice
public class ResultException extends BasicErrorController {

    @DubboReference(scope = "remote", async = true)
    private LogProvider logProvider;
    @Autowired
    private UserProvider userProvider;
    @Autowired
    private ConfigValueUtil configValueUtil;


    public ResultException(){
        super(new DefaultErrorAttributes(), new ErrorProperties());
    }



    @ResponseBody
    @ExceptionHandler(value = LoginException.class)
    public ActionResult loginException(LoginException e) {
        ActionResult result = ActionResult.fail(ActionResultCode.Fail.getCode(), e.getMessage());
        return result;
    }

    @ResponseBody
    @ExceptionHandler(value = {ImportException.class, DataException.class, EncryptFailException.class})
    public ActionResult simpleException(Exception e) {
        ActionResult result = ActionResult.fail(ActionResultCode.Fail.getCode(), e.getMessage());
        return result;
    }

    /**
     * 租户数据库异常
     *
     * @param e
     * @return
     */
    @ResponseBody
    @ExceptionHandler(value = {TenantDatabaseException.class, TenantInvalidException.class})
    public ActionResult<String> tenantDatabaseException(TenantInvalidException e) {
        String msg;
        if(e.getMessage() == null){
            if (configValueUtil.getMultiTenancyUrl().contains("https")) {
                // https 官网提示
                msg = MsgCode.LOG109.get();
            } else {
                msg = MsgCode.LOG110.get();
            }
        }else{
            msg = e.getMessage();
        }
        if(e.getLogMsg() != null){
            log.error(e.getLogMsg());
        }
        return ActionResult.fail(ActionResultCode.Fail.getCode(), msg);
    }

///
//    @ResponseBody
//    @ExceptionHandler(value = SQLSyntaxErrorException.class)
//    public ActionResult sqlException(SQLSyntaxErrorException e) {
//        ActionResult result;
//        log.error(e.getMessage());
//        e.printStackTrace();
//        if (e.getMessage().contains("Unknown database")) {
//            printLog(e, "请求失败");
//            result = ActionResult.fail(ActionResultCode.Fail.getCode(), "请求失败");
//        } else {
//            printLog(e, "数据库异常");
//            result = ActionResult.fail(ActionResultCode.Fail.getCode(), "数据库异常");
//        }
//        return result;
//    }
//
//    @ResponseBody
//    @ExceptionHandler(value = SQLServerException.class)
//    public ActionResult sqlServerException(SQLServerException e) {
//        ActionResult result;
//        printLog(e, "系统异常");
//        if (e.getMessage().contains("将截断字符串")) {
//            printLog(e, "某个字段字符长度超过限制，请检查。");
//            result = ActionResult.fail(ActionResultCode.Fail.getCode(), "某个字段字符长度超过限制，请检查。");
//        } else {
//            log.error(e.getMessage());
//            printLog(e, "数据库异常，请检查。");
//            result = ActionResult.fail(ActionResultCode.Fail.getCode(), "数据库异常，请检查。");
//        }
//        return result;
//    }

    @ResponseBody
    @ExceptionHandler(value = MethodArgumentNotValidException.class)
    public ActionResult methodArgumentNotValidException(MethodArgumentNotValidException e) {
        Map<String, String> map = new HashMap<>(16);
        List<ObjectError> allErrors = e.getBindingResult().getAllErrors();
        for (int i = 0; i < allErrors.size(); i++) {
            String s = allErrors.get(i).getCodes()[0];
            //用分割的方法得到字段名
            String[] parts = s.split("\\.");
            String part1 = parts[parts.length - 1];
            map.put(part1, allErrors.get(i).getDefaultMessage());
        }
        String json = JSON.toJSONString(map);
        ActionResult result = ActionResult.fail(ActionResultCode.ValidateError.getCode(), json);
        printLog(e, "字段验证异常", 4);
        return result;
    }

    @ResponseBody
    @ExceptionHandler(value = WorkFlowException.class)
    public ActionResult workFlowException(WorkFlowException e) {
        if (e.getCode() == 200) {
            Map<String, Object> map = JsonUtil.stringToMap(e.getMessage());
            return ActionResult.success(map);
        } else {
            if(e.getSuppressed()!=null) {
                printLog(e, "系统异常", 4);
            }
            return ActionResult.fail(e.getMessage());
        }
    }

    @ResponseBody
    @ExceptionHandler(value = WxErrorException.class)
    public ActionResult wxErrorException(WxErrorException e) {
        return ActionResult.fail(e.getError().getErrorCode(), MsgCode.AD103.get());
    }

    @ResponseBody
    @ExceptionHandler(value = ServletException.class)
    public void exception(ServletException e) throws Exception {
        log.error("系统异常:" + e.getMessage(), e);
        printLog(e, "系统异常", 4);
        throw new Exception();
    }

    @ResponseBody
    @ExceptionHandler(value = Exception.class)
    public ActionResult exception(Exception e) throws Exception {
        ActionResult result = ActionResult.fail(ActionResultCode.Fail.getCode(), MsgCode.AD102.get());
        log.error("系统异常:" + e.getMessage(), e);
        printLog(e, "系统异常", 4);
        if(e instanceof ConnectDatabaseException || e.getCause() instanceof ConnectDatabaseException){
            Throwable t = e;
            if(e.getCause() instanceof ConnectDatabaseException){
                t = e.getCause();
            }
            return ActionResult.fail(ActionResultCode.Fail.getCode(), t.getMessage());
        }
        return checkFeign(result, e);
    }

    /**
     * 权限码异常
     */
    @ResponseBody
    @ExceptionHandler(NotPermissionException.class)
    public ActionResult<Void> handleNotPermissionException(NotPermissionException e) {
        return ActionResult.fail(ActionResultCode.Fail.getCode(),  MsgCode.AD104.get());
    }

    /**
     * 角色权限异常
     */
    @ResponseBody
    @ExceptionHandler(NotRoleException.class)
    public ActionResult<Void> handleNotRoleException(NotRoleException e) {
        return ActionResult.fail(ActionResultCode.ValidateError.getCode(),  MsgCode.AD104.get());
    }

    /**
     * 认证失败
     */
    @ResponseBody
    @ExceptionHandler(NotLoginException.class)
    public ActionResult<Void> handleNotLoginException(NotLoginException e) {
        return ActionResult.fail(ActionResultCode.SessionOverdue.getCode(), MsgCode.AD105.get());
    }

    /**
     * 无效认证
     */
    @ResponseBody
    @ExceptionHandler(SameTokenInvalidException.class)
    public ActionResult<Void> handleIdTokenInvalidException(SameTokenInvalidException e) {
        return ActionResult.fail(ActionResultCode.SessionOverdue.getCode(), MsgCode.AD106.get());
    }

    private void printLog(Exception e, String msg, int type) {
        try {
            UserInfo userInfo = userProvider.get();
            if (userInfo.getId() == null) {
                e.printStackTrace();
                return;
            }
            //接口错误将不会进入数据库切源拦截器需要手动设置
            if (configValueUtil.isMultiTenancy() && TenantHolder.getDatasourceId() == null) {
                try {
                    TenantDataSourceUtil.switchTenant(userInfo.getTenantId());
                } catch (Exception ee){
                    e.printStackTrace();
                    return;
                }
            }
            LogEntity entity = new LogEntity();
            entity.setId(RandomUtil.uuId());
            entity.setUserId(userInfo.getUserId());
            entity.setUserName(userInfo.getUserName() + "/" + userInfo.getUserAccount());
//            if (!ServletUtil.getIsMobileDevice()) {
            entity.setDescription(msg);
//            }
            StringBuilder sb = new StringBuilder();
            sb.append(e.toString() + "\n");
            StackTraceElement[] stackArray = e.getStackTrace();
            for (int i = 0; i < stackArray.length; i++) {
                StackTraceElement element = stackArray[i];
                sb.append(element.toString() + "\n");
            }
            entity.setJsons(sb.toString());
            entity.setRequestUrl(ServletUtil.getRequest().getServletPath());
            entity.setRequestMethod(ServletUtil.getRequest().getMethod());
            entity.setType(type);
            entity.setUserId(userInfo.getUserId());
            // ip
            String ipAddr = IpUtil.getIpAddr();
            entity.setIpAddress(ipAddr);
            entity.setIpAddressName(IpUtil.getIpCity(ipAddr));
            entity.setCreatorTime(new Date());
            UserAgent userAgent = UserAgentUtil.parse(ServletUtil.getUserAgent());
            if (userAgent != null) {
                entity.setPlatForm(userAgent.getPlatform().getName() + " " + userAgent.getOsVersion());
                entity.setBrowser(userAgent.getBrowser().getName() + " " + userAgent.getVersion());
            }
            if (configValueUtil.isMultiTenancy() && StringUtil.isEmpty(TenantHolder.getDatasourceId())) {
                log.error("请求异常， 无登陆租户：" + ReflectionUtil.toString(entity), e);
            } else {
                logProvider.writeLogRequest(entity);
            }
        }catch (Exception g){
            log.error(g.getMessage());
        }finally {
            UserProvider.clearLocalUser();
            TenantProvider.clearBaseSystemIfo();
            TenantDataSourceUtil.clearLocalTenantInfo();
            NotTenantPluginHolder.clearNotSwitchFlag();
        }
    }

    private ActionResult checkFeign(ActionResult t, Exception e) throws Exception {
        //SEATA 全局事务内调用FEIGN， 因为服务统一封装结果返回200导致FEIGN认为调用成功事务无法回滚，请求中存在SEATA事务ID直接报错
        //FEIGN 接口API 需要去掉fallback默认处理异常
        if(ServletUtil.getRequest().getHeader("TX_XID") != null) {
            throw e;
        }
        return t;
    }


    /**
     * 覆盖默认的JSON响应
     */
    @Override
    @RequestMapping
    public ResponseEntity<Map<String, Object>> error(HttpServletRequest request) {
        HttpStatus status = getStatus(request);

        if (status == HttpStatus.NOT_FOUND) {
            return new ResponseEntity<>(status);
        }
        return super.error(request);
    }
}
