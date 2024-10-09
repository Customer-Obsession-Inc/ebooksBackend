package com.cusob.ebooks.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import com.cusob.ebooks.pojo.User;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper extends BaseMapper<User> {
}
