package github.xuanyunyue.annotations;

import github.xuanyunyue.spring.CustomScannerRegistrar;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 * @author： zyx1128
 * @create： 2023/12/24 14:23
 * @description：TODO
 */
@Target({ElementType.TYPE, ElementType.METHOD})// 表示该注解可以用于类或方法上.
@Retention(RetentionPolicy.RUNTIME)// 表示在运行时,该注解会被保留在字节码中,可以通过反射获取到.
@Import(CustomScannerRegistrar.class)// 表示在扫描时,会导入CustomScannerRegistrar类,这个类会进行相关处理.
@Documented// 表示该注解会被包含在JavaDoc中.
public @interface RPCScan {
    // 用于指定需要扫描的包.
    String[] basePackage();
}
