package cn.pengshao.registry.cluster;

import cn.pengshao.registry.config.PsRegistryConfigProperties;
import cn.pengshao.registry.http.HttpInvoker;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.commons.util.InetUtils;
import org.springframework.cloud.commons.util.InetUtilsProperties;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

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
    // 集群探活、选举
    final ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
    long timeout = 5000L;

    public void init() {
        try (InetUtils inetUtils = new InetUtils(new InetUtilsProperties())) {
            host = inetUtils.findFirstNonLoopbackHostInfo().getIpAddress();
            log.info("[Cluster] ===> host:{}", host);
        }

        MYSELF = new Server("http://" + host + ":" + port, true, false, -1L);
        log.info("[Cluster] ===> MYSELF:{}", MYSELF);
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

        executor.scheduleWithFixedDelay(() -> {
            try {
                updateServers();
                electLeader();
            } catch (Exception e) {
                log.error("[Cluster] ===> error", e);
            }
        }, 0, timeout, TimeUnit.MILLISECONDS);
    }

    private void updateServers() {
        // 探活
        servers.forEach(server -> {
            try {
                if (server.getUrl().equals(MYSELF.getUrl())) {
                    // 自己不需要探活
                    return;
                }

                Server serverInfo = HttpInvoker.httpGet(server.getUrl() + "/info", Server.class);
                if (serverInfo != null) {
                    server.setStatus(true);
                    server.setLeader(serverInfo.isLeader());
                    server.setVersion(serverInfo.getVersion());
                }
            } catch (Exception e) {
                log.info("[Cluster] ===> health check failed for {}", server);
                server.setStatus(false);
                server.setLeader(false);
            }
        });
    }

    private void electLeader() {
        List<Server> masters = servers.stream().filter(Server::isStatus).filter(Server::isLeader).toList();
        if (masters.isEmpty()) {
            log.info("[Cluster] ===> no leader, electing...");
            elect();
        } else if (masters.size() > 1) {
            log.info("[Cluster] ===> more than one leader, electing...");
            elect();
        } else {
            log.info("[Cluster] ===> no need election for leader:{}", masters.get(0));
        }
    }

    private void elect() {
        // 1.各种节点自己选，算法保证大家选的是同一个
        // 2.外部有一个分布式锁，谁拿到锁，谁是主
        // 3.分布式一致性算法，比如paxos, raft 后面再学习
        Server candidate = null;
        for (Server server : servers) {
            server.setLeader(false);
            if (!server.isStatus()) {
                continue;
            }

            if (candidate == null) {
                candidate = server;
            } else if (candidate.hashCode() > server.hashCode()) {
                candidate = server;
            }
        }

        if (candidate != null) {
            candidate.setLeader(true);
            log.info("[Cluster] ===> elect for leader:{}", candidate);
        } else {
            log.info("[Cluster] ===> elect failed for no leaders:{}", servers);
        }
    }

    public Server self() {
        return MYSELF;
    }

    public Server leader() {
        return this.servers.stream().filter(Server::isStatus)
                .filter(Server::isLeader).findFirst().orElse(null);
    }

}
