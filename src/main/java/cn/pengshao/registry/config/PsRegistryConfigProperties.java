package cn.pengshao.registry.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

/**
 * Description:registry config properties
 *
 * @Author: yezp
 * @date 2024/4/17 22:27
 */
@Data
@ConfigurationProperties(prefix = "psregistry")
public class PsRegistryConfigProperties {

    private List<String> serverList;

}
