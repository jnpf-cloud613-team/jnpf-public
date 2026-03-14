package i18n;

import com.alibaba.cloud.nacos.NacosConfigManager;
import com.alibaba.nacos.api.config.listener.Listener;
import com.alibaba.nacos.api.exception.NacosException;
import jnpf.i18n.core.MyReloadableResourceBundleMessageSource;
import jnpf.i18n.provider.MessageSourceProvider;
import jnpf.util.ThreadPoolExecutorUtil;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.Executor;

/**
 * 国际化配置 Nacos读取
 *
 * @author JNPF开发平台组
 * @user N
 * @copyright 引迈信息技术有限公司
 * @date 2024/3/21 14:00
 */
@Slf4j
public class NacosMessageSouceProvider implements MessageSourceProvider {

    private NacosConfigManager nacosConfigManager;

    public NacosMessageSouceProvider(NacosConfigManager nacosConfigManager) {
        this.nacosConfigManager = nacosConfigManager;
    }

    @Override
    public String loadMessageResource(String name, MyReloadableResourceBundleMessageSource messageSource) throws IOException {
        File file = new File(name);
        String fileName = file.getName() + MessageSourceProvider.PROPERTIES_SUFFIX;
        try {
            return nacosConfigManager.getConfigService().getConfigAndSignListener(fileName
                    , nacosConfigManager.getNacosConfigProperties().getGroup()
                    , nacosConfigManager.getNacosConfigProperties().getTimeout()
                    , new Listener() {
                        @Override
                        public Executor getExecutor() {
                            return ThreadPoolExecutorUtil.getExecutor();
                        }

                        @Override
                        public void receiveConfigInfo(String configInfo) {
                            try {
                                messageSource.refreshConfig(name, configInfo);
                            } catch (IOException e) {
                                log.error("刷新语言配置失败：{}, {}", fileName, e.getMessage(), e);
                            }
                        }
                    });
        } catch (NacosException e) {
            throw new IOException(e);
        }
    }

}
