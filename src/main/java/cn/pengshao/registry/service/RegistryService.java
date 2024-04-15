package cn.pengshao.registry.service;

import cn.pengshao.registry.model.InstanceMeta;

import java.util.List;
import java.util.Map;

/**
 * Description: Interface for registry service
 *
 * @Author: yezp
 * @date 2024/4/15 22:51
 */
public interface RegistryService {

    /**
     * 注册服务
     *
     * @param service 服务名称
     * @param instanceMeta 实例信息
     * @return 实例信息
     */
    InstanceMeta register(String service, InstanceMeta instanceMeta);

    /**
     * 服务下线
     *
     * @param service 服务名称
     * @param instanceMeta 实例信息
     * @return 实例信息
     */
    InstanceMeta unregister(String service, InstanceMeta instanceMeta);

    /**
     * 根据服务信息，查询实例列表
     *
     * @param service 服务名称
     * @return 实例信息
     */
    List<InstanceMeta> findAllInstances(String service);

    long renew(InstanceMeta instance, String... services);

    Long version(String service);

    Map<String, Long> versions(String... services);

}
