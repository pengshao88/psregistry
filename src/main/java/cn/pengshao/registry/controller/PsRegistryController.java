package cn.pengshao.registry.controller;

import cn.pengshao.registry.cluster.Cluster;
import cn.pengshao.registry.cluster.Server;
import cn.pengshao.registry.model.InstanceMeta;
import cn.pengshao.registry.service.RegistryService;
import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * Description:
 *
 * @Author: yezp
 * @date 2024/4/16 22:19
 */
@Slf4j
@RestController
public class PsRegistryController {

    @Autowired
    RegistryService registryService;
    @Autowired
    Cluster cluster;

    @RequestMapping("/register")
    public InstanceMeta register(@RequestParam String service, @RequestBody InstanceMeta instanceMeta) {
        log.info(" ===> register: {} @ {}", service, instanceMeta);
        return registryService.register(service, instanceMeta);
    }

    @RequestMapping("/unregister")
    public InstanceMeta unregister(@RequestParam String service, @RequestBody InstanceMeta instanceMeta) {
        log.info(" ===> unregister: {} @ {}", service, instanceMeta);
        return registryService.unregister(service, instanceMeta);
    }

    @RequestMapping("/findAll")
    public Object findAllInstances(@RequestParam String service) {
        log.info(" ===> findAllInstances: {}", service);
        return registryService.findAllInstances(service);
    }

    @RequestMapping("/renews")
    public long renews(@RequestParam String services, @RequestBody InstanceMeta instanceMeta) {
        log.info(" ===> renews: {} @ {}", services, instanceMeta);
        return registryService.renew(instanceMeta, services.split(","));
    }

    @RequestMapping("/renew")
    public long renew(@RequestParam String service, @RequestBody InstanceMeta instanceMeta) {
        log.info(" ===> renew: {} @ {}", service, instanceMeta);
        return registryService.renew(instanceMeta, service);
    }

    @RequestMapping("/version")
    public Long version(@RequestParam String service) {
        log.info(" ===> version: {}", service);
        return registryService.version(service);
    }

    @RequestMapping("/versions")
    public Map<String, Long> versions(@RequestParam String services) {
        log.info(" ===> versions: {}", services);
        return registryService.versions(services.split(","));
    }

    public static void main(String[] args) {
        InstanceMeta instanceMeta = InstanceMeta.http("127.0.0.1", 8080)
                .addParams(Map.of("name", "pengshao", "env", "dev", "tag", "RED"));
        System.out.println(JSON.toJSONString(instanceMeta));
    }

    @RequestMapping("/info")
    public Server info()
    {
        log.debug(" ===> info: {}", cluster.self());
        return cluster.self();
    }

    @RequestMapping("/cluster")
    public List<Server> cluster()
    {
        log.info(" ===> info: {}", cluster.getServers());
        return cluster.getServers();
    }

    @RequestMapping("/leader")
    public Server leader()
    {
        log.info(" ===> leader: {}", cluster.leader());
        return cluster.leader();
    }

    @RequestMapping("/setLeader")
    public Server setLeader()
    {
        cluster.self().setLeader(true);
        log.info(" ===> leader: {}", cluster.self());
        return cluster.self();
    }

}
