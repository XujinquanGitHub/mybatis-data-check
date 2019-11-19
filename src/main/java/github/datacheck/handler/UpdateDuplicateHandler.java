package github.datacheck.handler;

import com.alibaba.druid.sql.ast.SQLExpr;
import com.alibaba.druid.sql.ast.expr.SQLBinaryOpExpr;
import com.alibaba.druid.sql.ast.expr.SQLIdentifierExpr;
import com.alibaba.druid.sql.ast.expr.SQLValuableExpr;
import com.alibaba.druid.sql.ast.statement.SQLInsertStatement;
import com.alibaba.druid.sql.ast.statement.SQLUpdateSetItem;
import com.alibaba.druid.sql.ast.statement.SQLUpdateStatement;
import github.datacheck.DuplicateDataInterceptor;
import github.datacheck.enumerate.DuplicateData;
import github.datacheck.util.StringUtil;
import org.apache.ibatis.executor.Executor;

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
 * @author: 许金泉
 **/
public class UpdateDuplicateHandler extends AbstractDuplicateHandler {

    private SQLUpdateStatement updateStatement;

    protected Map<Field, String> queryColumnValue = new HashMap<>();


    // 数据库查询出来的数据
    protected List<Map<String, String>> database = new ArrayList<>();

    public UpdateDuplicateHandler(SQLUpdateStatement insertStatement, Executor executor) {
        this.updateStatement = insertStatement;
        this.executor = executor;
    }

    @Override
    List<DuplicateErrorMessage> handle() {
        extractQueryFields();
        queryDataBase();
        return validateDataDuplicate();
    }

    /**
     * 验证数据是否重复
     *
     * @return java.util.List<github.datacheck.handler.DuplicateErrorMessage>
     * @author 许金泉
     */
    private List<DuplicateErrorMessage> validateDataDuplicate() {
        List<DuplicateErrorMessage> errorMessages = new ArrayList<>();
        if (database == null || database.size() == 0 || database.size() == 1) {
            return errorMessages;
        }
        return queryColumnValue.entrySet().stream().filter(u -> {
            String columnValue = StringUtil.humpToLine(u.getKey().getName()).toLowerCase();
            return database.stream().anyMatch(m -> m.containsKey(columnValue) && u.getValue().equalsIgnoreCase(m.get(columnValue)));
        }).map(u -> {
            DuplicateData annotation = u.getKey().getAnnotation(DuplicateData.class);
            if (annotation == null) {
                return null;
            }
            DuplicateErrorMessage errorMessage = new DuplicateErrorMessage();
            if (annotation.errorMessage() != null && !"".equals(annotation.errorMessage())) {
                errorMessage.setMessage(annotation.errorMessage());
            } else if (annotation.template() != null && !"".equals(annotation.template())) {
                String describe = annotation.fieldDescribe();
                if (describe == null || "".equals(describe)) {
                    describe = u.getKey().getName();
                }
                errorMessage.setMessage(annotation.template().replace("${fieldDescribe}", describe));
            } else {
                String describe = annotation.fieldDescribe();
                if (describe == null || "".equals(describe)) {
                    describe = u.getKey().getName();
                }
                errorMessage.setMessage(describe + "重复！");
            }
            return errorMessage;
        }).filter(u -> u != null).collect(Collectors.toList());
    }

    /**
     * 在数据库中查询所需数据
     *
     * @return void
     * @author 许金泉
     */
    private void queryDataBase() {
        List<String> whereList = queryColumnValue.entrySet().stream().map(u -> " " + u.getKey().getName() + "='" + u.getValue() + "'").collect(Collectors.toList());
        String whereString = "";
        SQLBinaryOpExpr where = (SQLBinaryOpExpr) updateStatement.getWhere();
        if (!"".equalsIgnoreCase(where.toString())) {
            whereString = where.toString() + " or " + String.join(" or ", whereList);
        } else {
            whereString = String.join(" or ", whereList);
        }
        String querySql = String.format(selectCountFormatter, queryColumnValue.keySet().stream().map(u -> StringUtil.humpToLine(u.getName())).collect(Collectors.joining(",")), tableName, whereString);
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

    /**
     * 提取需要检查的字段
     *
     * @author 许金泉
     */
    private void extractQueryFields() {
        this.tableName = updateStatement.getTableName().getSimpleName();
        // 提取需要检查的字段
        List<Field> fields = DuplicateDataInterceptor.tableColumn.entrySet().stream().filter(u -> u.getKey().equalsIgnoreCase(StringUtil.underlineToHump(tableName))).map(Map.Entry::getValue).findFirst().orElse(null);
        if (fields == null || fields.size() <= 0) {
            return;
        }
        List<SQLUpdateSetItem> items = updateStatement.getItems();
        Map<String, String> columnValueMap = new HashMap<>(items.size());
        for (int i = 0; i < items.size(); i++) {
            SQLUpdateSetItem sqlUpdateSetItem = items.get(i);
            SQLIdentifierExpr column = (SQLIdentifierExpr) sqlUpdateSetItem.getColumn();
            SQLValuableExpr valueExpr = (SQLValuableExpr) sqlUpdateSetItem.getValue();
            columnValueMap.put(column.getName(), valueExpr.getValue().toString());
        }
        for (Field field : fields) {
            String fieldName = StringUtil.humpToLine(field.getName());
            if (columnValueMap.containsKey(fieldName)) {
                queryColumnValue.put(field, columnValueMap.get(fieldName));
            }
        }
    }

}
