package github.datacheck.handler;

import com.alibaba.druid.sql.SQLUtils;
import com.alibaba.druid.sql.ast.SQLStatement;
import com.alibaba.druid.sql.ast.statement.SQLInsertStatement;
import com.alibaba.druid.util.JdbcConstants;
import org.apache.ibatis.executor.Executor;

import java.util.ArrayList;
import java.util.List;

/**
 * @program: data-check
 * @description:
 * @author: 许金泉
 **/
public class HandlerManager {

    private String executeSQL;

    private List<SQLStatement> sqlStatements;

    private Executor executor;

    public HandlerManager(String sql, Executor executor) {
        this.executeSQL = sql;
        this.sqlStatements = SQLUtils.parseStatements(this.executeSQL, JdbcConstants.MYSQL);
        this.executor = executor;
    }

    public void handle() {
        if (sqlStatements == null || sqlStatements.size() == 0) {
            return;
        }
        List<IDuplicateHandler> listHandler = new ArrayList<>();
        for (int i = 0; i < sqlStatements.size(); i++) {
            SQLStatement sqlStatement = sqlStatements.get(i);
            if (sqlStatement instanceof SQLInsertStatement) {
                listHandler.add(new InsertDuplicateHandler((SQLInsertStatement) sqlStatement,executor));
            }
        }
    }

}
