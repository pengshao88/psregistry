package cn.pengshao.registry.service;

import cn.pengshao.registry.cluster.Snapshot;
import cn.pengshao.registry.model.InstanceMeta;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

/**
 * Description: Default registry service
 *
 * @Author: yezp
 * @date 2024/4/15 22:57
 */
@Slf4j
public class PsRegistryService implements RegistryService {

    // 注册服务信息
    final static MultiValueMap<String, InstanceMeta> REGISTRY = new LinkedMultiValueMap<>();
    // 记录版本信息
    final static Map<String, Long> VERSIONS = new ConcurrentHashMap<>();
    // 记录时间戳
    public static final Map<String, Long> TIMESTAMPS = new ConcurrentHashMap<>();
    public final static AtomicLong VERSION = new AtomicLong(0);

    @Override
    public synchronized InstanceMeta register(String service, InstanceMeta instanceMeta) {
        List<InstanceMeta> metaList = REGISTRY.get(service);
        if (metaList != null && !metaList.isEmpty()) {
            if (metaList.contains(instanceMeta)) {
                log.info(" ===> instance {} already registered", instanceMeta.toUrl());
                instanceMeta.setStatus(true);
                return instanceMeta;
            }
        }

        log.info(" ===> instance {} register success", instanceMeta.toUrl());
        instanceMeta.setStatus(true);
        REGISTRY.add(service, instanceMeta);
        renew(instanceMeta, service);
        VERSIONS.put(service, VERSION.incrementAndGet());
        return instanceMeta;
    }

    @Override
    public synchronized InstanceMeta unregister(String service, InstanceMeta instanceMeta) {
        List<InstanceMeta> metaList = REGISTRY.get(service);
        if (metaList == null || metaList.isEmpty()) {
            return null;
        }

        log.info(" ===> instance {} unregister success", instanceMeta.toUrl());
        metaList.removeIf(meta -> meta.equals(instanceMeta));
        instanceMeta.setStatus(false);
        renew(instanceMeta, service);
        VERSIONS.put(service, VERSION.incrementAndGet());
        return instanceMeta;
    }

    @Override
    public List<InstanceMeta> findAllInstances(String service) {
        return REGISTRY.get(service);
    }

    @Override
    public synchronized long renew(InstanceMeta instance, String... services) {
        long now = System.currentTimeMillis();
        for (String service : services) {
            TIMESTAMPS.put(service + "@" + instance.toUrl(), now);
        }
        return now;
    }

    @Override
    public Long version(String service) {
        return VERSIONS.get(service);
    }

    @Override
    public Map<String, Long> versions(String... services) {
        return Arrays.stream(services).collect(Collectors.toMap(k -> k, VERSIONS::get, (a, b) -> b));
    }

    public static synchronized Snapshot snapshot() {
        LinkedMultiValueMap<String, InstanceMeta> registry = new LinkedMultiValueMap<>();
        registry.addAll(REGISTRY);
        Map<String, Long> versions = new HashMap<>(VERSIONS);
        Map<String, Long> timestamps = new HashMap<>(TIMESTAMPS);
        return new Snapshot(registry, versions, timestamps, VERSION.get());
    }

    public synchronized static long restore(Snapshot snapshot) {
        REGISTRY.clear();
        REGISTRY.addAll(snapshot.getREGISTRY());
        VERSIONS.clear();
        VERSIONS.putAll(snapshot.getVERSIONS());
        TIMESTAMPS.clear();
        TIMESTAMPS.putAll(snapshot.getTIMESTAMPS());
        VERSION.set(snapshot.getVersion());
        return snapshot.getVersion();
    }
}
