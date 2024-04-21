package cn.pengshao.registry.cluster;

import cn.pengshao.registry.http.HttpInvoker;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Description:
 *
 * @Author: yezp
 * @date 2024/4/21 22:59
 */
@Slf4j
public class ServerHealth {

    final Cluster cluster;

    final ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
    long interval = 5000;

    public ServerHealth(Cluster cluster) {
        this.cluster = cluster;
    }

    public void checkServerHealth() {
        executor.scheduleWithFixedDelay(() -> {
            try {
                updateServers();            // 1、更新服务器状态
                doElect();                  // 2、选主
                syncSnapshotFromLeader();   // 3、同步快照
            } catch (Exception e) {
                log.error("[ServerHealth] ===>> check server health error", e);
            }
        }, 0, interval, TimeUnit.MILLISECONDS);
    }

    private void updateServers() {
        // 探活
        List<Server> servers = cluster.getServers();
        servers.stream().parallel().forEach(server -> {
            try {
                if (server.getUrl().equals(cluster.self().getUrl())) {
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

    private void doElect() {


    }

    private void syncSnapshotFromLeader() {

    }
}
