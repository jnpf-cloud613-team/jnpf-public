package jnpf.config;

import com.alibaba.cloud.commons.lang.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.Ordered;
import org.springframework.core.env.Environment;

/**
 * 新增日志等级初始化, 提前设置日志目录
 */
@Slf4j
public class SentinelAutoConfiguration implements ApplicationContextInitializer<ConfigurableApplicationContext>, Ordered {


    public static final String LOG_DIR = "csp.sentinel.log.dir";
    public static final String LOG_LEVEL = "csp.sentinel.log.level";


    @Override
    public void initialize(ConfigurableApplicationContext applicationContext) {
        Environment environment = applicationContext.getEnvironment();
        String dir = environment.getProperty("spring.cloud.sentinel.log.dir");
        String level = environment.getProperty("spring.cloud.sentinel.log.level");
        if (StringUtils.isEmpty(System.getProperty(LOG_DIR))
                && StringUtils.isNotBlank(dir)) {
            log.debug("Set sentinel log directory to {}", dir);
            System.setProperty(LOG_DIR, dir);
        }
        if (StringUtils.isEmpty(System.getProperty(LOG_LEVEL))
                && StringUtils.isNotBlank(level)) {
            if ("false".equals(level)) {
                level = "OFF";
            }
            log.debug("Set sentinel log level to {}", level);
            System.setProperty(LOG_LEVEL, level.toUpperCase());
        }
    }

    @Override
    public int getOrder() {
        // 在SentinelApplicationContextInitializer前, PropertiSouceBootstrapConfiguration之后进行初始化
        return Ordered.HIGHEST_PRECEDENCE+1000;
    }
}
