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

    private final String selectCountFormatter = " select %s from %s where %s";
    // 需要查询的字段和值
    protected Map<Field, String> queryColumnValue = new HashMap<>();

    protected String tableName;

    protected Executor executor;

    // 数据库查询出来的数据
    protected List<Map<String, String>> database = new ArrayList<>();

    /**
     * 在数据库中查询所需数据
     * @return void
     * @author 许金泉
     */
    protected void queryDataBase() {
        List<String> whereList = queryColumnValue.entrySet().stream().map(u -> " " + u.getKey().getName() + "='" + u.getValue() + "'").collect(Collectors.toList());
        String querySql = String.format(selectCountFormatter, queryColumnValue.keySet().stream().map(u -> StringUtil.humpToLine(u.getName())).collect(Collectors.joining(",")), tableName, String.join(" or ", whereList));
        PreparedStatement statement = null;
        try {
            statement = executor.getTransaction().getConnection().prepareStatement(querySql);
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                int size = queryColumnValue.size();
                Map<String, String> rowData = new HashMap<>(size);
                for (int i = 1; i < size + 1; i++) {
                    String columnName = resultSet.getMetaData().getColumnName(i);
                    Object value = resultSet.getObject(i);
                    rowData.put(columnName.toLowerCase(), value.toString());
                }
                database.add(rowData);
            }
        } catch (SQLException e) {
            log.error(e.toString(), e);
        } finally {
            if (statement != null) {
                try {
                    statement.close();
                } catch (SQLException e) {
                    log.error(e.toString(), e);
                }
            }
        }
    }



    abstract List<DuplicateErrorMessage> handle();

}
