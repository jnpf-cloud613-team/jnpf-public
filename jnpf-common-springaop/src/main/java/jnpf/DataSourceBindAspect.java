package jnpf;

/**
 * @author JNPF开发平台组
 * @version V3.1.0
 * @copyright 引迈信息技术有限公司（https://www.jnpfsoft.com）
 * @date 2021-03-26
 */

import cn.dev33.satoken.context.SaHolder;
import cn.dev33.satoken.router.SaRouter;
import jnpf.base.UserInfo;
import jnpf.config.ConfigValueUtil;
import jnpf.database.util.NotTenantPluginHolder;
import jnpf.database.util.TenantDataSourceUtil;
import jnpf.properties.GatewayWhite;
import jnpf.util.StringUtil;
import jnpf.util.TenantHolder;
import jnpf.util.UserProvider;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 *
 * @author JNPF开发平台组
 * @version V3.1.0
 * @copyright 引迈信息技术有限公司
 * @date 2021/3/15 17:12
 */
@Slf4j
@Aspect
@Component
@Order(1)
public class  DataSourceBindAspect {
    @Autowired
    private ConfigValueUtil configValueUtil;
    @Autowired
    private GatewayWhite gatewayWhite;

    @Pointcut("within(jnpf.*.controller.* || jnpf.controller.*)")
    public void bindDataSource() {

    }

    /**
     * NoDataSourceBind 不需要绑定数据库的注解
     *
     * @param pjp
     * @return
     * @throws Throwable
     */
    @Around("bindDataSource() && !@annotation(jnpf.util.NoDataSourceBind)")
    public Object doAroundService(ProceedingJoinPoint pjp) throws Throwable {
//        System.out.println(SaHolder.getRequest().getRequestPath());
        if (configValueUtil.isMultiTenancy()) {
            if(StringUtil.isEmpty(TenantHolder.getDatasourceId())){
                String url = null;
                try{
                    url = SaHolder.getRequest().getRequestPath();
                }catch (Exception e){}
                if(url != null){
                    // 白名单接口不传Token, 不检查租户信息, 要查询数据库需要手动切换租户
                    boolean isWhiteUrl = SaRouter.isMatch(gatewayWhite.excludeUrl, url) || SaRouter.isMatch(gatewayWhite.whiteUrl, url);
                    if(!isWhiteUrl) {
                        UserInfo userInfo = UserProvider.getUser();
                        log.error("未检测到租户信息, Tenant: {}, URL: {}, TOKEN: {}", userInfo.getTenantId(), url, userInfo.getToken());
                        return null;
                    }
                }
            }
            return pjp.proceed();
        }
        return pjp.proceed();
    }


    /**
     * NoDataSourceBind 不需要绑定数据库的注解 加入不切租户库标记
     *
     * @param pjp
     * @return
     * @throws Throwable
     */
    @Around("bindDataSource() && @annotation(jnpf.util.NoDataSourceBind)")
    public Object doAroundService2(ProceedingJoinPoint pjp) throws Throwable {
        try{
            NotTenantPluginHolder.setNotSwitchAlwaysFlag();
            //Filter中提前设置租户信息, 不需要切库的方法进行清除切库
            TenantDataSourceUtil.clearLocalTenantInfo();
            return pjp.proceed();
        }finally {
            NotTenantPluginHolder.clearNotSwitchAlwaysFlag();
        }
    }
}
