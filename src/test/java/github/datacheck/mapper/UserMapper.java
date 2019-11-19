package github.datacheck.mapper;

import github.datacheck.model.User;
import org.apache.ibatis.annotations.*;

/**
 * @author: 许金泉
 **/
@Mapper
public interface UserMapper {


    @Insert("INSERT INTO user (id, name, age, email) VALUES(#{user.id,jdbcType=VARCHAR},#{user.name,jdbcType=VARCHAR}, #{user.age,jdbcType=VARCHAR},#{user.email,jdbcType=VARCHAR});")
    Integer insertUser(@Param("user") User user);

    @Update("update user set name=#{user.name},age=#{user.age},email=#{user.email} where id=#{user.id}")
    Integer updateUserByUserId(@Param("user") User user);



    @Select("select name,email from user where  id=2 ")
    User selectUser();

}
