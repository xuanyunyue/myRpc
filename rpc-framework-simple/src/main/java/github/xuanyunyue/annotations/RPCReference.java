package github.xuanyunyue.annotations;

import java.lang.annotation.*;

/**
 * @author： zyx1128
 * @create： 2023/12/24 15:02
 * @description：TODO
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD}) // 表示该注解只能用于字段上.
@Inherited
public @interface RPCReference {
    /**
     * Service version, default value is empty string
     */
    String version() default "";

    /**
     * Service group, default value is empty string
     */
    String group() default "";
}
