package github.xuanyunyue.spring;

import github.xuanyunyue.enums.TransportEnum;
import github.xuanyunyue.extension.ExtensionLoader;
import github.xuanyunyue.factory.SingletonFactory;
import github.xuanyunyue.annotations.RPCReference;
import github.xuanyunyue.annotations.RPCService;
import github.xuanyunyue.config.RPCServiceConfig;
import github.xuanyunyue.provider.Impl.ZkServiceProviderImpl;
import github.xuanyunyue.provider.ServiceProvider;
import github.xuanyunyue.proxy.RpcClientProxy;
import github.xuanyunyue.remoting.transport.SendRequest;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;

/**
 * @author： zyx1128
 * @create： 2023/12/25 14:49
 * @description：TODO
 */
@Slf4j
@Component
public class SpringBeanPostProcessor implements BeanPostProcessor {
    private final ServiceProvider serviceProvider;
    private final SendRequest rpcClient;

    public SpringBeanPostProcessor() {
        this.serviceProvider = SingletonFactory.getInstance(ZkServiceProviderImpl.class);
        this.rpcClient = ExtensionLoader.getExtensionLoader(SendRequest.class).getExtension(TransportEnum.NETTY.getName());
    }

    /**
     * 判断该 Bean 是否带有 @RpcService 注解，如果有，则将该 Bean 作为一个远程服务发布出去。
     * 发布远程服务也就是将该 Bean 的方法信息以及 Netty 通信的 ip+端口 拼成一个字符串，在 zookeeper中作为一个持久化节点
     * @param bean
     * @param beanName
     * @return Object
     * @throws BeansException
     */
    @SneakyThrows
    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        if (bean.getClass().isAnnotationPresent(RPCService.class)) {
            log.info("[{}] is annotated with  [{}]", bean.getClass().getName(), RPCService.class.getCanonicalName());
            // get RpcService annotation
            RPCService rpcService = bean.getClass().getAnnotation(RPCService.class);
            // build RpcServiceProperties
            RPCServiceConfig rpcServiceConfig = RPCServiceConfig.builder()
                    .group(rpcService.group())
                    .version(rpcService.version())
                    .service(bean).build();
            serviceProvider.publishService(rpcServiceConfig);
        }
        return bean;
    }


    // 将使用的远程服务设置为代理对象
    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        Class<?> targetClass = bean.getClass();
        Field[] declaredFields = targetClass.getDeclaredFields();
        for (Field declaredField : declaredFields) {
            RPCReference rpcReference = declaredField.getAnnotation(RPCReference.class);
            if (rpcReference != null) {
                RPCServiceConfig rpcServiceConfig = RPCServiceConfig.builder()
                        .group(rpcReference.group())
                        .version(rpcReference.version()).build();
                RpcClientProxy rpcClientProxy = new RpcClientProxy(rpcClient, rpcServiceConfig);
                Object clientProxy = rpcClientProxy.getProxy(declaredField.getType());
                declaredField.setAccessible(true);
                try {
                    declaredField.set(bean, clientProxy);
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }

        }
        return bean;
    }
}
