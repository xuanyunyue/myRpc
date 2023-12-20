package github.xuanyunyue.provider;

import github.xuanyunyue.config.RPCServiceConfig;

/**
 * @author： zyx1128
 * @create： 2023/12/19 16:17
 * @description：TODO 存储并提供服务,该接口的实现需要一下方法：
 * <br>1. addService添加服务
 * <br>2. getService获取服务
 * <br>3. publishService发布服务？？？
 */
public interface ServiceProvider {

    /**
     - @description: TODO 添加服务
      * @param rpcServiceConfig
     - @return null
     */
    void addService(RPCServiceConfig rpcServiceConfig);

    /**
     - @description: TODO 获取服务
     * @param rpcServiceName
    - @return java.lang.Object
     */
    Object getService(String rpcServiceName);

    /**
     - @description: TODO 发布服务
     * @param rpcServiceConfig
    - @return void
     */
    void publishService(RPCServiceConfig rpcServiceConfig);

}
