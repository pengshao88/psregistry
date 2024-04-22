package cn.pengshao.registry.cluster;

import cn.pengshao.registry.config.PsRegistryConfigProperties;
import cn.pengshao.registry.service.PsRegistryService;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.commons.util.InetUtils;
import org.springframework.cloud.commons.util.InetUtilsProperties;

import java.util.ArrayList;
import java.util.List;

/**
 * Description:Registry cluster
 *
 * @Author: yezp
 * @date 2024/4/17 22:38
 */
@Slf4j
public class Cluster {

    @Value("${server.port}")
    String port;

    String host;

    Server MYSELF;

    PsRegistryConfigProperties properties;

    public Cluster(PsRegistryConfigProperties properties) {
        this.properties = properties;
    }

    @Getter
    private List<Server> servers;

    public void init() {
        try (InetUtils inetUtils = new InetUtils(new InetUtilsProperties())) {
            host = inetUtils.findFirstNonLoopbackHostInfo().getIpAddress();
            log.info("[Cluster] ===> host:{}", host);
        }

        MYSELF = new Server("http://" + host + ":" + port, true, false, -1L);
        log.info("[Cluster] ===> MYSELF:{}", MYSELF);

        initServers();
        new ServerHealth(this).checkServerHealth();
    }

    private void initServers() {
        List<Server> servers = new ArrayList<>();
        for (String url : properties.getServerList()) {
            if (url.contains("localhost")) {
                url = url.replace("localhost", host);
            } else if (url.contains("127.0.0.1")) {
                url = url.replace("127.0.0.1", host);
            }

            if (url.equals(MYSELF.getUrl())) {
                servers.add(MYSELF);
            } else {
                servers.add(new Server(url, false, false, -1L));
            }
        }

        this.servers = servers;
        log.info("[Cluster] ===> servers:{}", servers);
    }

    public Server self() {
        MYSELF.setVersion(PsRegistryService.VERSION.get());
        return MYSELF;
    }

    public Server leader() {
        return this.servers.stream().filter(Server::isStatus)
                .filter(Server::isLeader).findFirst().orElse(null);
    }

}
