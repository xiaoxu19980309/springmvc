package com.example.springbootdemo.mybatis;

import com.example.springbootdemo.pojo.User;
import org.apache.ibatis.annotations.*;
import org.springframework.stereotype.Service;

import java.util.List;

@Mapper
public interface UserDao{
    /**
     * 根据用户名查找用户
     * @param username
     * @return
     */
    @Select("select * from user WHERE username = #{username}")
    User queryUserByUserName(@Param("username") String username);

    /**
     * 查找所有用户
     * @return
     */
    @Select("select * from user")
    List<User> queryUserAll();

    /**
     * 添加用户
     * @param user
     */
    @Insert("insert into user(username,password,gmt_create,gmt_modified) values(#{username},#{password},now(),now())")
    int insertUser(User user);

    /**
     * 更新用户
     * @param user
     */
    @Update("update account set username=#{username} where username = #{username}")
    public void updateUser(User user);

    /**
     * 删除用户
     */
    @Delete("delete from account where username = #{username}")
    public void deleteUser(@Param("username") String username);
}