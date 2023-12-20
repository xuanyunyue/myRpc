package extension;

import java.lang.annotation.*;

/**
 * @author： zyx1128
 * @create： 2023/12/13 14:20
 * @description：TODO
 * SPI 机制用来做服务的扩展发现，是一种动态替换发现的机制，这里的 SPI 机制的实现参考了 Dubbo 的 SPI
 * 首先定义一个 @SPI 注解，使用 @SPI 标注的接口表示可以被动态替换，并且提供一个 ExtensionLoader类
 * 用于去加载 @SPI 的服务，并且获取 @SPI 的服务，
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface SPI {
}
