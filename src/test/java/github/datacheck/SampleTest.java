package github.datacheck;

import github.datacheck.exception.DuplicateDataException;
import github.datacheck.mapper.UserMapper;
import github.datacheck.model.User;
import github.datacheck.util.MybatisHelper;
import org.apache.ibatis.session.SqlSession;
import org.junit.Assert;
import org.junit.Test;

/**
 * @program: data-check
 * @description:
 * @author: 许金泉
 **/
public class SampleTest {


    @Test(expected = DuplicateDataException.class)
    public void testInsert() {
        SqlSession sqlSession = MybatisHelper.getSqlSession();
        UserMapper userMapper = sqlSession.getMapper(UserMapper.class);
        User user = new User().setId(23L).setAge(23).setEmail("abdn@qq.com").setName("adf");
        userMapper.insertUser(user);
        userMapper.insertUser(user);
    }

    @Test
    public void testSelectUser() {
        SqlSession sqlSession = MybatisHelper.getSqlSession();
        UserMapper userMapper = sqlSession.getMapper(UserMapper.class);
        User user = userMapper.selectUser();
        System.out.println(user.toString());
    }


}
