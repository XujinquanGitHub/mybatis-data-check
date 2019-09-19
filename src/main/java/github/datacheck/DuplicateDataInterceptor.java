package github.datacheck;

import github.datacheck.enumerate.DuplicateData;
import github.datacheck.handler.HandlerManager;
import github.datacheck.util.ReflectionUtil;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.ParameterMapping;
import org.apache.ibatis.plugin.*;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.type.TypeHandlerRegistry;

import java.lang.reflect.Field;
import java.text.DateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * @program: data-check
 * @description:
 * @author: 许金泉
 **/
@Intercepts({@Signature(type = Executor.class, method = "update", args = {MappedStatement.class, Object.class})})
public class DuplicateDataInterceptor implements Interceptor {

    public static Map<String, List<Field>> tableColumn;
    private static final Pattern pattern1 = Pattern.compile("\\?(?=\\s*[^']*\\s*,?\\s*(\\w|$))");
    private static final Pattern pattern2 = Pattern.compile("[\\s]+");

    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        Object[] args = invocation.getArgs();
        MappedStatement ms = (MappedStatement) args[0];
        Object parameter = args[1];
        BoundSql boundSql = ms.getBoundSql(parameter);
        Executor executor = (Executor) invocation.getTarget();
        String sql = getParameterizedSql(ms.getConfiguration(), boundSql);
        HandlerManager handlerManager = new HandlerManager(sql, executor);
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



    private String getParameterizedSql(Configuration configuration, BoundSql boundSql) {
        Object parameterObject = boundSql.getParameterObject();
        List<ParameterMapping> parameterMappings = boundSql.getParameterMappings();
        String sql = pattern2.matcher(boundSql.getSql()).replaceAll(" ");
        if (parameterMappings == null || parameterMappings.isEmpty() || parameterObject == null) {
            return sql;
        }
        TypeHandlerRegistry typeHandlerRegistry = configuration.getTypeHandlerRegistry();
        if (typeHandlerRegistry.hasTypeHandler(parameterObject.getClass())) {
            return pattern1.matcher(sql).replaceFirst(Matcher.quoteReplacement(getParameterValue(parameterObject)));
        }
        MetaObject metaObject = configuration.newMetaObject(parameterObject);
        for (ParameterMapping parameterMapping : parameterMappings) {
            String propertyName = parameterMapping.getProperty();
            if (metaObject.hasGetter(propertyName)) {
                Object obj = metaObject.getValue(propertyName);
                sql = pattern1.matcher(sql).replaceFirst(Matcher.quoteReplacement(getParameterValue(obj)));
            } else if (boundSql.hasAdditionalParameter(propertyName)) {
                Object obj = boundSql.getAdditionalParameter(propertyName);
                sql = pattern1.matcher(sql).replaceFirst(Matcher.quoteReplacement(getParameterValue(obj)));
            } else {
                sql = pattern1.matcher(sql).replaceFirst("缺失");
            }
        }
        return sql;
    }

    private static String getParameterValue(Object obj) {
        String value;
        if (obj instanceof String) {
            value = "'" + obj.toString() + "'";
        } else if (obj instanceof Date) {
            DateFormat formatter = DateFormat.getDateTimeInstance(DateFormat.DEFAULT, DateFormat.DEFAULT, Locale.CHINA);
            value = "'" + formatter.format(new Date()) + "'";
        } else {
            if (obj != null) {
                value = obj.toString();
            } else {
                value = "null";
            }

        }
        return value;
    }

}
