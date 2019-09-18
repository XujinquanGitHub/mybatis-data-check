package github.datacheck.handler;

import java.util.List;

/**
 * @program: data-check
 * @description: 检查数据是否重复
 * @author: 许金泉
 **/
public interface IDuplicateHandler {

    List<DuplicateErrorMessage> handle();

}
