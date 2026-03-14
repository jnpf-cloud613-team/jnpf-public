package jnpf.config;

import com.alibaba.cloud.nacos.NacosConfigManager;
import i18n.NacosMessageSouceProvider;
import jnpf.i18n.provider.MessageSourceProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 微服务消息来源配置
 *
 * @author JNPF开发平台组
 * @user N
 * @copyright 引迈信息技术有限公司
 * @date 2024/3/21 14:00
 */
@Configuration
public class I18nCloudAutoConfiguration {

    @Bean
    public MessageSourceProvider getNacosMessageSourceProvider(NacosConfigManager nacosConfigManager) {
        return new NacosMessageSouceProvider(nacosConfigManager);
    }
}
