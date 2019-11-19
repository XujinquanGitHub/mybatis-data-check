package github.datacheck.model;

import github.datacheck.enumerate.DuplicateData;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * @author: 许金泉
 **/
@Data
@Accessors(chain = true)
public class User implements Serializable {

    private Long id;

    @DuplicateData(template = "名字重复！")
    private String name;

    @DuplicateData(template = "年份重复！")
    private Integer age;

    @DuplicateData(template = "邮箱重复！")
    private String email;
}