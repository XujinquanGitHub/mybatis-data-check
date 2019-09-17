package github.datacheck;

import com.alibaba.druid.sql.SQLUtils;
import com.alibaba.druid.sql.ast.SQLExpr;
import com.alibaba.druid.sql.ast.SQLStatement;
import com.alibaba.druid.sql.ast.expr.SQLIdentifierExpr;
import com.alibaba.druid.sql.ast.expr.SQLValuableExpr;
import com.alibaba.druid.sql.ast.statement.SQLInsertStatement;
import com.alibaba.druid.util.JdbcConstants;
import github.datacheck.enumerate.DuplicateData;
import github.datacheck.handler.HandlerManager;
import github.datacheck.util.ReflectionUtil;
import github.datacheck.util.StringUtil;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.plugin.*;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @program: data-check
 * @description:
 * @author: 许金泉
 **/
@Intercepts({@Signature(type = Executor.class, method = "update", args = {MappedStatement.class, Object.class})})
public class DuplicateDataInterceptor implements Interceptor {

    public static Map<String, List<Field>> tableColumn;

    final String selectCountFormatter = " select %s from %s where %s";


    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        Object[] args = invocation.getArgs();
        MappedStatement ms = (MappedStatement) args[0];
        Object parameter = args[1];
        BoundSql boundSql = ms.getBoundSql(parameter);
        Executor executor = (Executor) invocation.getTarget();
        Connection connection = executor.getTransaction().getConnection();
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
