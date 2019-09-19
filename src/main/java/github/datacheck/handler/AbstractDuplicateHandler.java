package github.datacheck.handler;

import com.alibaba.druid.sql.ast.SQLExpr;
import com.alibaba.druid.sql.ast.expr.SQLIdentifierExpr;
import com.alibaba.druid.sql.ast.expr.SQLValuableExpr;
import github.datacheck.DuplicateDataInterceptor;
import github.datacheck.util.StringUtil;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.logging.Log;
import org.apache.ibatis.logging.LogFactory;

import java.lang.reflect.Field;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @program: data-check
 * @description: 检查数据是否重复
 * @author: 许金泉
 **/
public abstract class AbstractDuplicateHandler {


    protected final Log log = LogFactory.getLog(getClass());

    protected final String selectCountFormatter = " select %s from %s where %s";

    protected String tableName;

    protected Executor executor;

    abstract List<DuplicateErrorMessage> handle();

}
