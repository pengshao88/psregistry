package cn.pengshao.registry;

import cn.pengshao.registry.config.PsRegistryConfigProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

/**
 * Description:
 *
 * @Author: yezp
 * @date 2024/4/15 22:45
 */
@SpringBootApplication
@EnableConfigurationProperties({PsRegistryConfigProperties.class})
public class PsRegistryApplication {

    public static void main(String[] args) {
        SpringApplication.run(PsRegistryApplication.class, args);
    }

}
