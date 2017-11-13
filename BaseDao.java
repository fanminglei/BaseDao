package com.haina.dao;

import java.beans.IntrospectionException;
import java.util.List;



public interface BaseDao<T> {
	
	void update(T t);
	List<T> select(T t);
	int add(T t) throws Exception;
	void deleteByid(T t)throws Exception;
	List<T> selectById(T t);
	List<T> selectByRow(T t,String username,String password);
	//��ѯǰn����¼
	List<T> selectByn(T t,String a);
	int selectTotal(T t) throws Exception;
	//ȥ���ظ���¼
	List<T> selectDistinct(T t,String distinct);
	List<T> selectFenYe(T t,String currentPage2)throws Exception;
}
