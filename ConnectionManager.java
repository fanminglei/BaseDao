package com.haina.utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;



/**
 * 获取数据库连接
 * @author dell
 *
 */
public class ConnectionManager {
	public static Connection getConnection(){
		Connection conn = null;
		String url = "jdbc:mysql://localhost:3306/usermanager";
		String username = "root";
		String password = "root";
		try {
			conn = DriverManager.getConnection(url,username,password);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return conn;//返回connection对象
	}
	public static void close(Connection conn,PreparedStatement ps){
		try {
			conn.close();
			ps.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	public static void close(Connection conn,PreparedStatement ps,ResultSet rs){
		try {
			conn.close();
			ps.close();
			rs.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

}
