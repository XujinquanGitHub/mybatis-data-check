package github.datacheck.handler;

import com.alibaba.druid.sql.ast.SQLExpr;
import com.alibaba.druid.sql.ast.expr.SQLIdentifierExpr;
import com.alibaba.druid.sql.ast.expr.SQLValuableExpr;
import com.alibaba.druid.sql.ast.statement.SQLInsertStatement;
import github.datacheck.DuplicateDataInterceptor;
import github.datacheck.enumerate.DuplicateData;
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
 * @description: 插入时检查数据是否重复
 * @author: 许金泉
 **/
public class InsertDuplicateHandler extends AbstractDuplicateHandler {

    private SQLInsertStatement insertStatement;

    public InsertDuplicateHandler(SQLInsertStatement insertStatement, Executor executor) {
        this.insertStatement = insertStatement;
        this.executor = executor;
    }

    @Override
    public List<DuplicateErrorMessage> handle() {
        extractQueryFields();
        queryDataBase();
        return validateDataDuplicate();
    }

    /**
     * 验证数据是否重复
     * @return java.util.List<github.datacheck.handler.DuplicateErrorMessage>
     * @author 许金泉
     */
    private List<DuplicateErrorMessage> validateDataDuplicate() {
        List<DuplicateErrorMessage> errorMessages = new ArrayList<>();
        if (database == null || database.size() == 0) {
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
     * 提取需要检查的字段
     * @author 许金泉
     */
    protected void extractQueryFields() {
        this.tableName = insertStatement.getTableName().getSimpleName();
        // 提取需要检查的字段
        List<Field> fields = DuplicateDataInterceptor.tableColumn.entrySet().stream().filter(u -> u.getKey().equalsIgnoreCase(StringUtil.camel(tableName))).map(Map.Entry::getValue).findFirst().orElse(null);
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
        for (Field field : fields) {
            String fieldName = StringUtil.humpToLine(field.getName());
            if (columnValueMap.containsKey(fieldName)) {
                queryColumnValue.put(field, columnValueMap.get(fieldName));
            }
        }
    }




}
