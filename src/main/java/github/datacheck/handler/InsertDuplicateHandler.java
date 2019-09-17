package github.datacheck.handler;

import com.alibaba.druid.sql.ast.SQLExpr;
import com.alibaba.druid.sql.ast.expr.SQLIdentifierExpr;
import com.alibaba.druid.sql.ast.expr.SQLValuableExpr;
import com.alibaba.druid.sql.ast.statement.SQLInsertStatement;
import github.datacheck.DuplicateDataInterceptor;
import github.datacheck.util.StringUtil;
import org.apache.ibatis.executor.Executor;

import java.lang.reflect.Field;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @program: data-check
 * @description: 插入时检查数据是否重复
 * @author: 许金泉
 **/
public class InsertDuplicateHandler implements IDuplicateHandler {

    private SQLInsertStatement insertStatement;

    final String selectCountFormatter = " select %s from %s where %s";

    private Map<String, String> queryColumnValue = new HashMap<>();

    private String tableName;

    private Executor executor;

    // 数据库查询出来的数据
    private List<Map<String, String>> database = new ArrayList<>();


    public InsertDuplicateHandler(SQLInsertStatement insertStatement, Executor executor) {
        this.insertStatement = insertStatement;
    }

    @Override
    public boolean validateDataDuplicate() {
        return false;
    }

    public void queryDataBase() {
        List<String> whereList = queryColumnValue.entrySet().stream().map(u -> " " + u.getKey() + "='" + u.getValue() + "'").collect(Collectors.toList());
        String querySql = String.format(selectCountFormatter, queryColumnValue.keySet().stream().collect(Collectors.joining(",")), tableName, whereList.stream().collect(Collectors.joining(" or ")));
        PreparedStatement statement = null;
        try {
            statement = executor.getTransaction().getConnection().prepareStatement(querySql);
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                Map<String, String> rowData = new HashMap<>();
                for (int i = 1; i < queryColumnValue.size(); i++) {
                    String columnName = resultSet.getMetaData().getColumnName(i);
                    Object value = resultSet.getObject(i);
                    rowData.put(columnName, value.toString());
                }
                database.add(rowData);
            }
        } catch (SQLException e) {

        } finally {
            if (statement != null) {
                try {
                    statement.close();
                } catch (SQLException e) {

                }
            }
        }

        for (int i = 0; i < database.size(); i++) {
            Map<String, String> rowData = database.get(i);

        }
    }

    public void extractQueryFields() {
        this.tableName = insertStatement.getTableName().getSimpleName();
        // 提取需要检查的字段
        List<Field> fields = DuplicateDataInterceptor.tableColumn.entrySet().stream().filter(u -> u.getKey().equalsIgnoreCase(StringUtil.camel(tableName))).map(u -> u.getValue()).findFirst().orElse(null);
        if (fields == null || fields.size() <= 0) {
            return;
        }
        List<SQLExpr> values = insertStatement.getValues().getValues();
        List<SQLExpr> columns = insertStatement.getColumns();
        Map<String, String> columnValueMap = new HashMap<>(columns.size());
        for (int i = 0; i < columns.size(); i++) {
            SQLIdentifierExpr sqlExpr = (SQLIdentifierExpr) columns.get(i);
            SQLValuableExpr valueExpr = (SQLValuableExpr) values.get(i);
            columnValueMap.put(sqlExpr.getName(), valueExpr.getValue().toString());
        }
        List<String> whereList = new ArrayList<>();
        List<String> queryColumnList = new ArrayList<>();
        for (int i = 0; i < fields.size(); i++) {
            Field field = fields.get(i);
            String fieldName = StringUtil.humpToLine(field.getName());
            if (columnValueMap.containsKey(fieldName)) {
                queryColumnValue.put(fieldName, columnValueMap.get(fieldName));
            }
        }


    }


}
