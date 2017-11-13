package com.haina.daoImpl;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.haina.bean.User;
import com.haina.dao.BaseDao;
import com.haina.utils.ConnectionManager;

public class BaseDaoImpl<T> implements BaseDao<T> {
	
	@Override
	public int add(T t) throws Exception {
		String sql = "insert into";
		//获取类的权限名称 class com.haina.bean.User
		Class clazz = t.getClass();
		
		String clazzName = clazz.getName();//com.haina.bean.User
		//获取表名
		String tableName = clazzName.substring(clazzName.lastIndexOf(".")+1,clazzName.length() );//User
		sql = sql +" "+ tableName +"(";
		 // 返回 Field 对象的一个数组，这些对象反映此 Class 对象所表示的类或接口所声明的所有字段。
		Field [] fields = clazz.getDeclaredFields();
		 for (Field field : fields) {
	          String fieldName = field.getName();
	            
	          System.out.println(fieldName);
	            sql += fieldName + ",";
	            System.out.println(sql);
	        }
		  // 切割最后一个属性的逗号
		 sql=sql.substring(0,sql.length()-1);
		 sql+=") values (";
		 System.out.println(sql);
		 //获取属性的数量
		 int fieldLength = fields.length;
		 for(int i=1;i<=fieldLength;i++){
			 sql +="?,";
		 }
		 sql = sql.substring(0, sql.length() - 1);
	        sql += ")";
	        System.out.println(sql);
		 Connection conn = ConnectionManager.getConnection();
		 PreparedStatement ps = null;
		 try {
			ps = conn.prepareStatement(sql);
			int i = 0;
			Field [] field = clazz.getDeclaredFields();
			Field.setAccessible(field, true);
			//对属性遍历
			for(Field field1 : field){
				i = i +1;
				//通过调用 getFoo 和 setFoo 存取方法，为符合标准 Java 约定的属性构造一个 PropertyDescriptor
				//(propertyName - 属性的编程名称。) beanClass - 目标 bean 的 Class 对象。例如 sun.beans.OurButton.class。 
				PropertyDescriptor pd = new PropertyDescriptor(field1.getName(),clazz );
				 //根据属性拿到对应的get方法
                Method getMethod = pd.getReadMethod();
                System.out.println(getMethod);
                 //执行get方法
               
               Object obj2 = (Object) getMethod.invoke(t);
               
               //判断属性的类型
               if(field1.getType()==String.class){
            	   ps.setString(i, obj2.toString());
               }else if(field1.getType()==Integer.class){
            	   
            	   ps.setInt(i, Integer.parseInt(obj2.toString()));
               }
               
			}
			ps.execute();
			
		} catch (SQLException e) {
			e.printStackTrace();
		}
		 ConnectionManager.close(conn, ps);
		return 0;
	}

	@Override
	public void deleteByid(T t) throws Exception {
		String sql = "delete from";
		Class clazz = t.getClass();
		String clazzname = clazz.getName();
		System.out.println(clazzname);
		String titleName = clazzname.substring(clazzname.lastIndexOf(".")+1,clazzname.length());
		sql+=" "+titleName+" "+"where"+" "+"id=?";
		System.out.println(sql);
		Connection conn = ConnectionManager.getConnection();
		PreparedStatement ps = null;
		try {
			ps = conn.prepareStatement(sql);
			PropertyDescriptor pd = new PropertyDescriptor("id", clazz);
			Method getMethod = pd.getReadMethod();
			Object obj = getMethod.invoke(t);
			ps.setInt(1, Integer.parseInt(obj.toString()));
			ps.execute();
			
		} catch (SQLException e) {
			e.printStackTrace();
		}
		ConnectionManager.close(conn, ps);
	}

	@Override
	public void update(T t) {
		 String sql = "update ";
	        Class clazz = t.getClass();
	        String clazzName = clazz.getName();
	        String tableName = clazzName.substring(clazzName.lastIndexOf(".") + 1, clazzName.length());
	        sql += tableName;
	        String setString = " set ";
	        sql += setString;

	        Field[] fields = clazz.getDeclaredFields();
	        for (Field field : fields) {
	            String fieldName = field.getName();
	            fieldName = fieldName.substring(fieldName.lastIndexOf(".") + 1, fieldName.length());
	            // 如果属性名称为id，就跳出当前循环
	            if (fieldName.equals("id")) {
	                continue;
	            }
	            sql += fieldName + "=?,";

	        }
	        sql = sql.substring(0, sql.length() - 1);

	        // 因为默认是根据id查询，所以直接这样拼接
	        sql += " where id=?";
	        
	        Connection conn = ConnectionManager.getConnection();
	        PreparedStatement ps = null;
	        try {
	            ps = conn.prepareStatement(sql);
	            int i = 0;
	            int j = 0;
	            Field[] field = clazz.getDeclaredFields();
	            for (Field field1 : field) {
	            //id值不允许修改，所以如果属性名为id，那么跳出本次循环
	                if ("id".equals(field1.getName())) {
	                    continue;
	                }
	                i = i + 1;
	                PropertyDescriptor pd = new PropertyDescriptor(field1.getName(), clazz);
	                Method getMethod = pd.getReadMethod();

	                Object obj = (Object) getMethod.invoke(t);

	                if (field1.getType() == String.class) {

	                    ps.setString(i, obj.toString());
	                } 
	                //最后一个占位符
	                j = i;
	                System.out.println("j="+j);
	                
	            }
	            //只剩下ID的没有赋值
	            for (Field field1 : field) {
	                //获取get方法
	            	//field1.getName()=id
	                PropertyDescriptor pd = new PropertyDescriptor(field1.getName(), clazz);
	                Method getMethod = pd.getReadMethod();
	                System.out.println(field1.getName());
	                Object obj = (Object) getMethod.invoke(t);
	               
	                ps.setInt(j + 1, Integer.parseInt(obj.toString()));
	                break;
	            }
	            ps.executeUpdate();

	        } catch (SQLException e) {
	            e.printStackTrace();
	        } catch (IllegalArgumentException e) {
	            e.printStackTrace();
	        } catch (IntrospectionException e) {
	            e.printStackTrace();
	        } catch (IllegalAccessException e) {
	            e.printStackTrace();
	        } catch (InvocationTargetException e) {
	            e.printStackTrace();
	        }
	        ConnectionManager.close(conn, ps);
	        System.out.println(sql);
	      
	    }
		
	

	@Override
	public List<T> select(T t) {
		 String sql = "select * from ";
	        Class clazz = t.getClass();
	        String clazzName = clazz.getName();
	        String tableName = clazzName.substring(clazzName.lastIndexOf(".") + 1, clazzName.length());
	        sql += tableName;
	        System.out.println(sql);
	        Connection conn = ConnectionManager.getConnection();
	        List<T> list = new ArrayList<T>();
	        Field[] field = clazz.getDeclaredFields();
	        PreparedStatement ps = null;
	        ResultSet rs = null;
	        try {
	            ps = conn.prepareStatement(sql);

	            rs= ps.executeQuery();

	            while (rs.next()) {
	                T t2 = (T)clazz.newInstance();
	                    for (int j = 1; j <=field.length; j++) {
	                        for (Field field1 : field) {
	                            PropertyDescriptor pd = new PropertyDescriptor(field1.getName(), clazz);
	                            Method setMethod = pd.getWriteMethod();
	                        if (field1.getType() == String.class) {

	                            setMethod.invoke(t2, rs.getString(j));
	                            j+=1;
	                        } else if (field1.getType() == Integer.class) {
	                            setMethod.invoke(t2, rs.getInt(j));
	                            j+=1;
	                        }
	                    }
	                }
	                    list.add(t2);

	            }

	        } catch (SQLException e) {
	            e.printStackTrace();
	        } catch (IllegalArgumentException e) {
	            e.printStackTrace();
	        } catch (IllegalAccessException e) {
	            e.printStackTrace();
	        } catch (IntrospectionException e) {
	            e.printStackTrace();
	        } catch (InvocationTargetException e) {
	            e.printStackTrace();
	        } catch (InstantiationException e) {
	            e.printStackTrace();
	        }

	        ConnectionManager.close(conn, ps, rs);

	        return list;
	    }
	@Override
	public List<T> selectById(T t) {
		    String sql = "select * from ";
	        Class clazz = t.getClass();
	        String clazzName = clazz.getName();
	        String tableName = clazzName.substring(clazzName.lastIndexOf(".") + 1, clazzName.length());
	        sql += tableName+" "+"where id=?";
	        System.out.println(sql);
	        Connection conn = ConnectionManager.getConnection();
	        List<T> list = new ArrayList<T>();
	        Field[] field = clazz.getDeclaredFields();
	        PreparedStatement ps = null;
	        ResultSet rs = null;
	        try {
	            ps= conn.prepareStatement(sql);
	            PropertyDescriptor pd = new PropertyDescriptor("id", clazz);
				Method getMethod = pd.getReadMethod();
				Object obj = getMethod.invoke(t);
				ps.setInt(1, Integer.parseInt(obj.toString()));
	            rs = ps.executeQuery();
	            
	            while (rs.next()) {
	                T t2 = (T)clazz.newInstance();
	                    for (int j = 1; j <=field.length; j++) {
	                        for (Field field1 : field) {
	                            pd = new PropertyDescriptor(field1.getName(), clazz);
	                            Method setMethod = pd.getWriteMethod();
	                        if (field1.getType() == String.class) {

	                            setMethod.invoke(t2, rs.getString(j));
	                            j+=1;
	                        } else if (field1.getType() == Integer.class) {
	                            setMethod.invoke(t2, rs.getInt(j));
	                            j+=1;
	                        }
	                    }
	                }
	                    list.add(t2);

	            }

	        } catch (SQLException e) {
	            e.printStackTrace();
	        } catch (IllegalArgumentException e) {
	            e.printStackTrace();
	        } catch (IllegalAccessException e) {
	            e.printStackTrace();
	        } catch (IntrospectionException e) {
	            e.printStackTrace();
	        } catch (InvocationTargetException e) {
	            e.printStackTrace();
	        } catch (InstantiationException e) {
	            e.printStackTrace();
	        }
	        ConnectionManager.close(conn, ps, rs);
			return list;

 }
	@Override
	public List<T> selectByRow(T t,String username,String password) {
		 String sql = "select ?,? from ";
		 	
	        Class clazz = t.getClass();
	        String clazzName = clazz.getName();
	        String tableName = clazzName.substring(clazzName.lastIndexOf(".") + 1, clazzName.length());
	        sql += tableName;
	        System.out.println(sql);
	        Connection conn = ConnectionManager.getConnection();
	        List<T> list = new ArrayList<T>();
	        Field[] field = clazz.getDeclaredFields();
	        PreparedStatement ps = null;
	        ResultSet rs = null;
	        try {
	            ps = conn.prepareStatement(sql);
	            
	            PropertyDescriptor pd = new PropertyDescriptor(username, clazz);
	             pd = new PropertyDescriptor(password, clazz);
	             ps.setString(1,username);
	             ps.setString(2,password);
                Method setMethod = pd.getWriteMethod();
	            
	            rs = ps.executeQuery();

	            while (rs.next()) {
	                T t2 = (T)clazz.newInstance();
	                    	int j =1;
	                       
	                        	  
	                        	
	                       
	                            setMethod.invoke(t2, rs.getString(j));
	                            j++;
	                        
	                    
	                
	                    list.add(t2);

	            }

	        } catch (SQLException e) {
	            e.printStackTrace();
	        } catch (IllegalArgumentException e) {
	            e.printStackTrace();
	        } catch (IllegalAccessException e) {
	            e.printStackTrace();
	        } catch (IntrospectionException e) {
	            e.printStackTrace();
	        } catch (InvocationTargetException e) {
	            e.printStackTrace();
	        } catch (InstantiationException e) {
	            e.printStackTrace();
	        }


	        ConnectionManager.close(conn, ps, rs);
	        return list;
	}
	@Override
	public List<T> selectByn(T t,String a) {
		String sql = "select   * from ";
        Class clazz = t.getClass();
        String clazzName = clazz.getName();
        String tableName = clazzName.substring(clazzName.lastIndexOf(".") + 1, clazzName.length());
        sql += tableName+" "+"limit"+" "+"0,"+a;
        System.out.println(sql);
        Connection conn = ConnectionManager.getConnection();
        List<T> list = new ArrayList<T>();
        Field[] field = clazz.getDeclaredFields();
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
           ps = conn.prepareStatement(sql);
           
			
			
			
            rs = ps.executeQuery();
            
            while (rs.next()) {
                T t2 = (T)clazz.newInstance();
                    for (int j = 1; j <=field.length; j++) {
                        for (Field field1 : field) {
                        	PropertyDescriptor pd = new PropertyDescriptor(field1.getName(), clazz);
                            Method setMethod = pd.getReadMethod();
                        if (field1.getType() == String.class) {

                            setMethod.invoke(t2, rs.getString(j));
                            j+=1;
                        } else if (field1.getType() == Integer.class) {
                            setMethod.invoke(t2, rs.getInt(j));
                            j+=1;
                        }
                    }
                }
                    list.add(t2);

            }

        } catch (SQLException e) {
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (IntrospectionException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        }
        ConnectionManager.close(conn, ps, rs);
		return list;
	}
	@Override
	public int selectTotal(T t) throws Exception {
		   String sql = "select count(*) from ";
	        Class clazz = t.getClass();
	        String clazzName = clazz.getName();
	        String tableName = clazzName.substring(clazzName.lastIndexOf(".") + 1, clazzName.length());
	        sql += " "+tableName;
	        System.out.println(sql);
	        Connection conn = ConnectionManager.getConnection();
	        Field[] field = clazz.getDeclaredFields();
	        PreparedStatement ps = null;
	        ResultSet rs = null;
	        int a=0;
	        try {
				ps = conn.prepareStatement(sql);
				rs = ps.executeQuery();
				while(rs.next()){
			     a =rs.getInt(1);
			    
				}
				
			} catch (SQLException e) {
				e.printStackTrace();
			}
	        
		return a;
	}
	@Override
	public List<T> selectDistinct(T t, String distinct) {
		String sql = "select distinct ";
	 	
        Class clazz = t.getClass();
        String clazzName = clazz.getName();
        String tableName = clazzName.substring(clazzName.lastIndexOf(".") + 1, clazzName.length());
        sql += " "+distinct+" "+"from"+" "+tableName;
        System.out.println(sql);
        Connection conn = ConnectionManager.getConnection();
        List<T> list = new ArrayList<T>();
       
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = conn.prepareStatement(sql);
            
            rs = ps.executeQuery();

            while (rs.next()) {
                
                  String result =  rs.getString(1);
                  list.add((T) result.toString()) ; 
                    
                                
            }
           

        } catch (SQLException e) {
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }
        
        


        ConnectionManager.close(conn, ps, rs);
        return list;
	}
	@Override
	public List<T> selectFenYe(T t, String currentPage2) throws Exception {
		//1.给每页显示的条数赋值
		int pageSize =10;
		BaseDao dao = new BaseDaoImpl();
		//2.表里的数据总数
		int totalCount= dao.selectTotal(t);
		//3.计算总页码
		int pageCount = totalCount%pageSize==0?
				 totalCount/pageSize:totalCount/pageSize+1;
		//4.获取当前页
		int currentPage = 0;
		String r = currentPage2;
		 try{if(r==null){
			 currentPage=1;
		 }else{
			 currentPage = Integer.parseInt(r);//字符串类型的数字例如"3"，转换成int类型的数据
			 if(currentPage<=0){//页码小于0，当前页=1
				 currentPage = 1;
			 }else if(currentPage>pageCount){//页码大于最大页，当前页等于最大页
				 currentPage = pageCount;
			 }
		 }
		 }catch(Exception e){currentPage=1;}
		 List<T> list = new ArrayList<T>();
		 Class clazz = t.getClass();
		 Field[] field = clazz.getDeclaredFields();
		String sql = "select * from ";
		String clazzName = clazz.getName();
		String tableName = clazzName.substring(clazzName.lastIndexOf(".")+1,clazzName.length() );
		sql+=tableName+" "+"order by id desc limit ?,?";
		System.out.println(sql);
		Connection conn = ConnectionManager.getConnection();
		PreparedStatement ps = conn.prepareStatement(sql);
		ps.setInt(1, (currentPage-1)*pageSize);
		
		ps.setInt(2, pageSize);
		ResultSet rs=ps.executeQuery();
		while(rs.next()){
			T t2 = (T)clazz.newInstance();
            for (int j = 1; j <=field.length; j++) {
                for (Field field1 : field) {
                    PropertyDescriptor pd = new PropertyDescriptor(field1.getName(), clazz);
                    Method setMethod = pd.getWriteMethod();
                if (field1.getType() == String.class) {

                    setMethod.invoke(t2, rs.getString(j));
                    j+=1;
                } else if (field1.getType() == Integer.class) {
                    setMethod.invoke(t2, rs.getInt(j));
                    j+=1;
                }
            }
        }
            list.add(t2);

			 
		}
		return list;
	}
	public static void main(String[] args) throws Exception {
		BaseDao dao = new BaseDaoImpl();
		User user = new User();
		/*u.setId(7);
		u.setUsername("sasa");
		u.setPassword("256153");
		u.setEmail("eewew");*/
		//b.add(u);
		//b.deleteByid(u);
		/*更新
		 * BaseDao dao = new BaseDaoImpl();
        User user = new User();
        user.setId(2);
        user.setUsername("xdd");
        user.setPassword("xdingdang");
        user.setEmail("djsakldjsakld");
        dao.update(user);
		*/
		/*List list = dao.select(user);
		for(int i=0;i<list.size();i++){
			User user1 = (User) list.get(i);
			System.out.println("id="+user1.getId()+" name="+user1.getUsername()+" password="+user1.getPassword()+" email="+user1.getEmail());
		}
	}*/
		
		/*String username = "username";
		String password = "password";
		List list =dao.selectByRow(user,username,password);*/
		/*String a = "3";
		List list  = dao.selectByn(user, a);
		for(int i=0;i<list.size();i++){
			User user1 = (User) list.get(i);
			System.out.println(" name="+user1.getUsername()+" password="+user1.getPassword()+" email="+user1.getEmail());
		}*/
		
		/*int a = dao.selectTotal(user);
		System.out.println(a);*/
		
		/*List list = dao.selectDistinct(user, "username");
		for(int i=0;i<list.size();i++){
			String username  =  (String) list.get(i);
			System.out.println("username:"+username);
		}*/
		List list  =dao.selectFenYe(user, "1");
		for(int i=0;i<list.size();i++){
			User user1 = (User) list.get(i);
			System.out.println("id="+user1.getId()+" name="+user1.getUsername()+" password="+user1.getPassword()+" email="+user1.getEmail());
		}
	}



	

	

	
	
}
