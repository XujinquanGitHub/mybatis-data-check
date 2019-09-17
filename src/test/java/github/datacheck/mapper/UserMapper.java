package github.datacheck.mapper;

import github.datacheck.model.User;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * @program: data-check
 * @description:
 * @author: 许金泉
 **/
@Mapper
public interface UserMapper  {


    @Insert("INSERT INTO user (id, name, age, email) VALUES(1, 'Jone', 18, 'test1@baomidou.com')")
    Integer insertUser(@Param("user")User user);


    @Select("select name,email from user where  id=2 ")
    User selectUser();

}
