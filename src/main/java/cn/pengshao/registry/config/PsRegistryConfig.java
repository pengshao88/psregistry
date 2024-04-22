package cn.pengshao.registry.config;

import cn.pengshao.registry.cluster.Cluster;
import cn.pengshao.registry.health.HealthChecker;
import cn.pengshao.registry.health.PsHealthChecker;
import cn.pengshao.registry.service.PsRegistryService;
import cn.pengshao.registry.service.RegistryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Description: configuration for all beans.
 *
 * @Author: yezp
 * @date 2024/4/16 22:17
 */
@Configuration
public class PsRegistryConfig {

    @Bean
    public RegistryService registryService() {
        return new PsRegistryService();
    }

//    @Bean(initMethod = "start", destroyMethod = "stop")
//    public HealthChecker healthChecker(@Autowired RegistryService registryService) {
//        return new PsHealthChecker(registryService);
//    }

    @Bean(initMethod = "init")
    public Cluster cluster(@Autowired PsRegistryConfigProperties properties) {
        return new Cluster(properties);
    }

}
