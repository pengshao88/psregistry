package cn.pengshao.registry.cluster;

import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * Description:
 *
 * @Author: yezp
 * @date 2024/4/22 22:00
 */
@Slf4j
public class Election {

    public void electLeader(List<Server> servers) {
        List<Server> masters = servers.stream().filter(Server::isStatus).filter(Server::isLeader).toList();
        if (masters.isEmpty()) {
            log.info("[Election] ===> no leader, electing...");
            elect(servers);
        } else if (masters.size() > 1) {
            log.info("[Election] ===> more than one leader, electing...");
            elect(servers);
        } else {
            log.debug("[Election] ===> no need election for leader:{}", masters.get(0));
        }
    }

    private void elect(List<Server> servers) {
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
            log.info("[Election] ===> elect for leader:{}", candidate);
        } else {
            log.warn("[Election] ===> elect failed for no leaders:{}", servers);
        }
    }

}
