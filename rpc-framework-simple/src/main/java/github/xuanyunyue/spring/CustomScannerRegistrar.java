package github.xuanyunyue.spring;

import github.xuanyunyue.annotations.RPCScan;
import github.xuanyunyue.annotations.RPCService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.core.type.StandardAnnotationMetadata;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * @author： zyx1128
 * @create： 2023/12/24 15:05
 * @description：
 * 在 @RpcScan 注解中 @Import 了 CustomScannerRegistrar 这个类，之后会调用这个类的registerBeanDefinitions() 方法，
 * 在这个方法中我们取出来 @RpcScan 注解的 basePackage 属性，此时 Spring 只拿到了 @RpcScan 的扫描路径，并不知道需要去扫描，
 * 之后我们再去定义自己的扫描器，指定扫描路径，就可以将扫描到的 Bean 添加到 Spring 容器中了.
 */
@Slf4j
public class CustomScannerRegistrar implements ImportBeanDefinitionRegistrar, ResourceLoaderAware {
    private static final String SPRING_BEAN_BASE_PACKAGE = "github.xuanyunyue";
    private static final String BASE_PACKAGE_ATTRIBUTE_NAME = "basePackage";
    private ResourceLoader resourceLoader;

    // 设置资源加载器
    @Override
    public void setResourceLoader(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;

    }

    @Override
    public void registerBeanDefinitions(AnnotationMetadata annotationMetadata, BeanDefinitionRegistry beanDefinitionRegistry) {
        // 获取RpcScan注解的属性和值,包括basePackage属性. 先获取RpcScan的属性
        // RPCScan.class.getName():获取RpcScan类的名称
        Map<String, Object> attributes = annotationMetadata.getAnnotationAttributes(RPCScan.class.getName());
        // 将属性封装为 AnnotationAttributes 对象
        AnnotationAttributes rpcScanAnnotationAttributes = AnnotationAttributes.fromMap(attributes);
        String[] rpcScanBasePackages = new String[0]; //创建一个长度为0的数组，以便下面做判断
        if (rpcScanAnnotationAttributes != null) {
            // 获取注解的 basePackage 属性值
            rpcScanBasePackages = rpcScanAnnotationAttributes.getStringArray(BASE_PACKAGE_ATTRIBUTE_NAME);
        }
        if (rpcScanBasePackages.length == 0) {
            /**
             * getIntrospectedClass()也就获取了使用RpcScan注解的这个类，也就是com.zqy.ClientTest.Server.NettyServer
             * 再获取它的包名，即为com.zqy.ClientTest.Server
             * 这里的目的也就是设置默认的BasePackage为使用RpcScan注解的类所在的packageName
             */
            rpcScanBasePackages = new String[]{((StandardAnnotationMetadata) annotationMetadata).getIntrospectedClass().getPackage().getName()};
        }
        // 扫描 RpcService 的扫描器
        CustomScanner rpcServiceScanner = new CustomScanner(beanDefinitionRegistry, RPCService.class);
        // 扫描 Component 的扫描器
        CustomScanner springBeanScanner = new CustomScanner(beanDefinitionRegistry, Component.class);
        if (resourceLoader != null) {
            // todo 2.不清楚设置resourceLoader的作用，暂时没发现用处
            rpcServiceScanner.setResourceLoader(resourceLoader);
            springBeanScanner.setResourceLoader(resourceLoader);
        }
        int springBeanAmount = springBeanScanner.scan(SPRING_BEAN_BASE_PACKAGE);
        log.info("springBeanScanner扫描的数量 [{}]", springBeanAmount);
        int rpcServiceCount = rpcServiceScanner.scan(rpcScanBasePackages);
        log.info("rpcServiceScanner扫描的数量 [{}]", rpcServiceCount);

    }
}
