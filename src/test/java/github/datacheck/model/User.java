package github.datacheck.model;

import github.datacheck.enumerate.DuplicateData;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * @program: data-check
 * @description:
 * @author: 许金泉
 **/
@Data
@Accessors(chain = true)
public class User implements Serializable {

    private Long id;

    @DuplicateData
    private String name;

    private Integer age;

    @DuplicateData
    private String email;
}