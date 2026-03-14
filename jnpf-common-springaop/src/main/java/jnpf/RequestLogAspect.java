package jnpf;

import cn.hutool.http.useragent.UserAgent;
import cn.hutool.http.useragent.UserAgentUtil;
import jnpf.annotation.HandleLog;
import jnpf.base.LogSortEnum;
import jnpf.base.UserInfo;
import jnpf.base.entity.LogEntity;
import jnpf.config.ConfigValueUtil;
import jnpf.provider.system.LogProvider;
import jnpf.util.IpUtil;
import jnpf.util.JsonUtil;
import jnpf.util.RandomUtil;
import jnpf.util.ServletUtil;
import jnpf.util.TenantHolder;
import jnpf.util.UserProvider;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboReference;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.lang.reflect.Method;
import java.util.Date;

/**
 * 日志记录
 *
 * @author JNPF开发平台组
 * @version V3.1.0
 * @copyright 引迈信息技术有限公司
 * @date 2021年3月13日 上午9:18
 */

@Slf4j
@Aspect
@Component
@Order(2)
public class RequestLogAspect {

    @Autowired
    private ConfigValueUtil configValueUtil;
     @DubboReference(scope = "remote", async = true)
    private LogProvider logProvider;



    @Pointcut("within(jnpf.*.controller.* || jnpf.controller.*)")
    public void requestLog() {
    }

    @Around("requestLog()")
    public Object doAroundService(ProceedingJoinPoint pjp) throws Throwable {
        long startTime = System.currentTimeMillis();
        Object obj = pjp.proceed();
        if(ServletUtil.getRequest() == null){
            return obj;
        }
        long costTime = System.currentTimeMillis() - startTime;
        UserInfo userInfo = UserProvider.getUser();
        if(userInfo.getUserId() != null && (!configValueUtil.isMultiTenancy() || TenantHolder.getLocalTenantCache() != null)) {
            // 得到请求参数
            Object[] args = pjp.getArgs();
            Signature signature = pjp.getSignature();
            printLog(userInfo, costTime, obj, args, signature);
            try {
                // 判断是否需要操作日志
                MethodSignature methodSignature = (MethodSignature) pjp.getSignature();
                // 得到请求方法
                Method method = methodSignature.getMethod();
                HandleLog methodAnnotation = method.getAnnotation(HandleLog.class);
                if (methodAnnotation != null) {
                    String moduleName = methodAnnotation.moduleName();
                    String requestMethod = methodAnnotation.requestMethod();
                    handleLog(userInfo, costTime, obj, moduleName, requestMethod, args, signature);
                }
            } catch (Exception e) {
                log.error("记录操作日志发生错误：" + e.getMessage());
            }
        }
        return obj;
    }

    /**
     * 请求日志
     *
     * @param userInfo
     * @param costTime
     */
    private void printLog(UserInfo userInfo, long costTime, Object obj, Object[] args, Signature signature) {
        LogEntity entity = new LogEntity();
        entity.setId(RandomUtil.uuId());
        entity.setType(LogSortEnum.Request.getCode());
        entity.setUserId(userInfo.getUserId());
        entity.setUserName(userInfo.getUserName() + "/" + userInfo.getUserAccount());
        //请求耗时
        entity.setRequestDuration((int) costTime);
        entity.setRequestUrl(ServletUtil.getRequest().getServletPath());
        entity.setRequestMethod(ServletUtil.getRequest().getMethod());
        String ipAddr = IpUtil.getIpAddr();
        entity.setIpAddress(ipAddr);
        entity.setIpAddressName(IpUtil.getIpCity(ipAddr));
        entity.setCreatorTime(new Date());
        UserAgent userAgent = UserAgentUtil.parse(ServletUtil.getUserAgent());
        if (userAgent != null) {
            entity.setPlatForm(userAgent.getPlatform().getName() + " " + userAgent.getOsVersion());
            entity.setBrowser(userAgent.getBrowser().getName() + " " + userAgent.getVersion());
        }
        String declaringTypeName = signature.getDeclaringTypeName();
        String name = signature.getName();
        entity.setRequestTarget(declaringTypeName + "." + name);
        entity.setJsons(obj + "");
        StringBuilder stringBuilder = new StringBuilder();
        for (Object o : args) {
            // 如果是MultipartFile则为导入
            if (o instanceof MultipartFile) {
                stringBuilder.append("{\"originalFilename\":\"" + ((MultipartFile) o).getOriginalFilename() + "\",");
                stringBuilder.append("\"contentType\":\"" + ((MultipartFile) o).getContentType() + "\",");
                stringBuilder.append("\"name\":\"" + ((MultipartFile) o).getName() + "\",");
                stringBuilder.append("\"resource\":\"" + ((MultipartFile) o).getResource() + "\",");
                stringBuilder.append("\"size\":\"" + ((MultipartFile) o).getSize() + "\"}");
            }
        }
        if (stringBuilder.length() > 0) {
            entity.setRequestParam(stringBuilder.toString());
        } else {
            entity.setRequestParam(JsonUtil.getObjectToString(args));
        }
        logProvider.writeLogRequest(entity);
    }

    /**
     * 添加操作日志
     *
     * @param userInfo      用户信息
     * @param costTime      操作耗时
     * @param obj           请求结果
     * @param moduleName    模块名称
     * @param requestMethod 请求方法
     * @param args           请求参数
     */
    private void handleLog(UserInfo userInfo, long costTime, Object obj, String moduleName, String requestMethod, Object[] args, Signature signature) {
        LogEntity entity = new LogEntity();
        entity.setId(RandomUtil.uuId());
        entity.setType(LogSortEnum.Operate.getCode());
        entity.setUserId(userInfo.getUserId());
        entity.setUserName(userInfo.getUserName() + "/" + userInfo.getUserAccount());
        //请求耗时
        entity.setRequestDuration((int) costTime);
        entity.setRequestMethod(ServletUtil.getRequest().getMethod());
        entity.setRequestUrl(ServletUtil.getRequest().getServletPath());
        String ipAddr = IpUtil.getIpAddr();
        entity.setIpAddress(ipAddr);
        entity.setIpAddressName(IpUtil.getIpCity(ipAddr));
        entity.setCreatorTime(new Date());
        // 请求设备
        UserAgent userAgent = UserAgentUtil.parse(ServletUtil.getUserAgent());
        if (userAgent != null) {
            entity.setPlatForm(userAgent.getPlatform().getName() + " " + userAgent.getOsVersion());
            entity.setBrowser(userAgent.getBrowser().getName() + " " + userAgent.getVersion());
        }
        // 操作模块
        entity.setModuleName(moduleName);
        String declaringTypeName = signature.getDeclaringTypeName();
        String name = signature.getName();
        entity.setRequestTarget(declaringTypeName + "." + name);
        // 操作记录
        StringBuilder stringBuilder = new StringBuilder();
        for (Object o : args) {
            // 如果是MultipartFile则为导入
            if (o instanceof MultipartFile) {
                stringBuilder.append("{\"originalFilename\":\"" + ((MultipartFile) o).getOriginalFilename() + "\",");
                stringBuilder.append("\"contentType\":\"" + ((MultipartFile) o).getContentType() + "\",");
                stringBuilder.append("\"name\":\"" + ((MultipartFile) o).getName() + "\",");
                stringBuilder.append("\"resource\":\"" + ((MultipartFile) o).getResource() + "\",");
                stringBuilder.append("\"size\":\"" + ((MultipartFile) o).getSize() + "\"}");
            }
        }
        if (stringBuilder.length() > 0) {
            entity.setRequestParam(stringBuilder.toString());
        } else {
            entity.setRequestParam(JsonUtil.getObjectToString(args));
        }
        entity.setJsons(obj + "");
        logProvider.writeLogRequest(entity);
    }
}


