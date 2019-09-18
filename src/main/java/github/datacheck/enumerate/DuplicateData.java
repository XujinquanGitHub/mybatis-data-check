package github.datacheck.enumerate;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author 许金泉
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface DuplicateData {

    // 字段描述
    String fieldDescribe() default "";

    // 字段重复时抛出的异常信息
    String errorMessage() default "";

    // errorMessage 为空时使用模板信息
    String template() default "${fieldDescribe}重复！";


}
