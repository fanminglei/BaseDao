package com.haina.dao;

import java.util.List;

import com.haina.bean.User;

public interface UserDao extends BaseDao<User>{

	 List<User>findAll();

}
