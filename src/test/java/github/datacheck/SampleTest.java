package github.datacheck;

import github.datacheck.exception.DuplicateDataException;
import github.datacheck.mapper.UserMapper;
import github.datacheck.model.User;
import github.datacheck.util.MybatisHelper;
import org.apache.ibatis.exceptions.PersistenceException;
import org.apache.ibatis.session.SqlSession;
import org.junit.Assert;
import org.junit.Test;

/**
 * @program: data-check
 * @description:
 * @author: 许金泉
 **/
public class SampleTest {


    @Test(expected = PersistenceException.class)
    public void testInsert() {
        SqlSession sqlSession = MybatisHelper.getSqlSession();
        UserMapper userMapper = sqlSession.getMapper(UserMapper.class);
        User user = new User().setId(23L).setAge(23).setEmail("abdn@qq.com").setName("adf");
        userMapper.insertUser(user);
        userMapper.insertUser(user);
    }

    @Test
    public void testUpdate() {
        //((1, 'Jone', 18, 'test1@baomidou.com'),
        //(2, 'Jack', 20, 'test2@baomidou.com'),
        //(3, 'Tom', 28, 'test3@baomidou.com'),
        //(4, 'Sandy', 21, 'test4@baomidou.com'),
        //(5, 'Billie', 24, 'test5@baomidou.com');
        SqlSession sqlSession = MybatisHelper.getSqlSession();
        UserMapper userMapper = sqlSession.getMapper(UserMapper.class);
        User user = new User().setId(1L).setAge(182).setEmail("test3454@baomidou.com").setName("Bilsfalie");
        userMapper.updateUserByUserId(user);
    }


    @Test
    public void testSelectUser() {
        SqlSession sqlSession = MybatisHelper.getSqlSession();
        UserMapper userMapper = sqlSession.getMapper(UserMapper.class);
        User user = userMapper.selectUser();
        System.out.println(user.toString());
    }


}
