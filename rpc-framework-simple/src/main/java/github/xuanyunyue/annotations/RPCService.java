package github.xuanyunyue.annotations;

import java.lang.annotation.*;

/**
 * @author： zyx1128
 * @create： 2023/12/24 14:57
 * @description：TODO 用于标记服务实现类.
 * 注解的作用是在运行时提供元数据信息,在这里,它提供了服务的版本和组信息.
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
@Inherited // 允许子类继承父类的注解.
public @interface RPCService {

    /**
     * Service version, default value is empty string
     */
    String version() default "";

    /**
     * Service group, default value is empty string
     */
    String group() default "";
}
