package github.datacheck.handler;

import com.alibaba.druid.sql.SQLUtils;
import com.alibaba.druid.sql.ast.SQLStatement;
import com.alibaba.druid.sql.ast.statement.SQLInsertStatement;
import com.alibaba.druid.util.JdbcConstants;
import github.datacheck.exception.DuplicateDataException;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.logging.Log;
import org.apache.ibatis.logging.LogFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @program: data-check
 * @description:
 * @author: 许金泉
 **/
public class HandlerManager {

    private final Log log = LogFactory.getLog(HandlerManager.class.toString());

    private List<SQLStatement> sqlStatements;

    private Executor executor;

    private List<AbstractDuplicateHandler> listHandler = new ArrayList<>();

    private List<DuplicateErrorMessage> errorMessages = new ArrayList<>();

    public HandlerManager(String sql, Executor executor) {
        this.sqlStatements = SQLUtils.parseStatements(sql, JdbcConstants.MYSQL);
        this.executor = executor;
        initHandler();
    }

    private void initHandler() {
        if (sqlStatements == null || sqlStatements.size() == 0) {
            return;
        }
        for (SQLStatement sqlStatement : sqlStatements) {
            if (sqlStatement instanceof SQLInsertStatement) {
                listHandler.add(new InsertDuplicateHandler((SQLInsertStatement) sqlStatement, executor));
            }
        }
    }

    public void handle() {
        if (listHandler == null || listHandler.size() == 0) {
            return;
        }
        listHandler.forEach(item -> errorMessages.addAll(item.handle()));
        if (errorMessages != null && errorMessages.size() > 0) {
            log.error(errorMessages.stream().map(DuplicateErrorMessage::toString).collect(Collectors.joining("--------\n")));
            throw new DuplicateDataException(errorMessages.stream().map(DuplicateErrorMessage::getMessage).collect(Collectors.joining(",")));
        }
    }

}
