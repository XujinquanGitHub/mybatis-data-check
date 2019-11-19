package github.datacheck.exception;

/** 数据重复时的异常
 * @author: 许金泉
 **/
public class DuplicateDataException extends RuntimeException {

    public DuplicateDataException(String message, Throwable cause) {
        super(message, cause);
    }

    public DuplicateDataException(String message) {
        super(message);
    }

}
