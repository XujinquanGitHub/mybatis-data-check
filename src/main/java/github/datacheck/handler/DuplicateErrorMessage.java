package github.datacheck.handler;

import java.lang.reflect.Field;

/**
 * @author: 许金泉
 **/
public class DuplicateErrorMessage {

    private String message;

    private Field field;

    private String value;

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Field getField() {
        return field;
    }

    public void setField(Field field) {
        this.field = field;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return "DuplicateErrorMessage{" + "message='" + message + '\'' + ", field=" + field + ", value='" + value + '\'' + '}';
    }
}
