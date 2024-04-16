package cn.pengshao.registry.health;

import cn.pengshao.registry.model.InstanceMeta;
import cn.pengshao.registry.service.PsRegistryService;
import cn.pengshao.registry.service.RegistryService;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Description:
 *
 * @Author: yezp
 * @date 2024/4/16 22:34
 */
@Slf4j
public class PsHealthChecker implements HealthChecker {

    RegistryService registryService;

    final ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
    long timeout = 20000L;

    public PsHealthChecker(RegistryService registryService) {
        this.registryService = registryService;
    }

    @Override
    public void start() {
        executor.scheduleWithFixedDelay(() -> {
            log.info(" ===> Health checker running...");
            long currentTime = System.currentTimeMillis();
            PsRegistryService.TIMESTAMPS.keySet().forEach(serviceAndInst -> {
                Long timestamp = PsRegistryService.TIMESTAMPS.get(serviceAndInst);
                if (currentTime - timestamp > timeout) {
                    log.info(" ===> Health checker timeout, service: {}", serviceAndInst);

                    int index = serviceAndInst.indexOf("@");
                    String service = serviceAndInst.substring(0, index);
                    String url = serviceAndInst.substring(index + 1);
                    InstanceMeta instance = InstanceMeta.from(url);
                    registryService.unregister(service, instance);
                    PsRegistryService.TIMESTAMPS.remove(serviceAndInst);
                }

            });
        }, 10, 10, TimeUnit.SECONDS);
    }

    @Override
    public void stop() {
        executor.shutdown();
    }
}
