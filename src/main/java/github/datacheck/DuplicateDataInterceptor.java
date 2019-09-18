package github.datacheck;

import github.datacheck.enumerate.DuplicateData;
import github.datacheck.handler.HandlerManager;
import github.datacheck.util.ReflectionUtil;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.plugin.*;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;

/**
 * @program: data-check
 * @description:
 * @author: 许金泉
 **/
@Intercepts({@Signature(type = Executor.class, method = "update", args = {MappedStatement.class, Object.class})})
public class DuplicateDataInterceptor implements Interceptor {

    public static Map<String, List<Field>> tableColumn;


    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        Object[] args = invocation.getArgs();
        MappedStatement ms = (MappedStatement) args[0];
        Object parameter = args[1];
        BoundSql boundSql = ms.getBoundSql(parameter);
        Executor executor = (Executor) invocation.getTarget();
        HandlerManager handlerManager = new HandlerManager(boundSql.getSql(), executor);
        handlerManager.handle();
        return invocation.proceed();
    }

    @Override
    public Object plugin(Object target) {
        return Plugin.wrap(target, this);
    }

    @Override
    public void setProperties(Properties properties) {
        String[] modelPackages = properties.getProperty("modelPackages").split(",");
        tableColumn = Arrays.stream(modelPackages).flatMap(u -> ReflectionUtil.listAllFieldByPackage(u).stream()).filter(u -> u.isAnnotationPresent(DuplicateData.class)).collect(Collectors.groupingBy(u -> u.getDeclaringClass().getSimpleName()));
    }
}
