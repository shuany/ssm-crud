package com.shengsi.crud.test;

import java.util.UUID;

import org.apache.ibatis.session.SqlSession;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.shengsi.crud.bean.Employee;
import com.shengsi.crud.dao.DepartmentMapper;
import com.shengsi.crud.dao.EmployeeMapper;

/**
 * 测试dao层 推荐使用spring的项目可以使用spring的单元测试， 可以自动注入我们需要的组件 1.导入springtest模块
 * 2.@ContextConfiguration制定spring配置文件的位置
 * 
 * @RunWith 指定目标单元测试 3.直接autowired要使用的组件即可
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:applicationContext.xml" })
public class MapperTest {

	@Autowired
	DepartmentMapper departmentMapper;

	@Autowired
	EmployeeMapper employeeMapper;

	@Autowired
	SqlSession sqlSession;

	/**
	 * 测试DepartmentMapper
	 */

	@Test
	public void testCRUD() {

		// //1.创建ioc容器
		// ApplicationContext ioc = new
		// ClassPathXmlApplicationContext("applicationContext.xml");
		// //2.从容器中获取mapper
		// DepartmentMapper bean= ioc.getBean(DepartmentMapper.class);

		System.out.println(departmentMapper);

		// 1.插入几个部门
		// departmentMapper.insertSelective(new Department(null,"开发部"));
		// departmentMapper.insertSelective(new Department(null,"测试部"));

		// 2.插入1个员工：
		//employeeMapper.insertSelective(new Employee(null, "tom", "F", "tom@shen.com", 1));

		// 3.批量插入 使用可以执行批量操作的sqlSession

		// for(){
		// employeeMapper.insertSelective(new Employee(null, "tom", "F",
		// "tom@shen.com", 1));
		// }

		EmployeeMapper mapper = sqlSession.getMapper(EmployeeMapper.class);
		for(int i =0;i<1000;i++){
			String uuid = UUID.randomUUID().toString().substring(0,5)+i;
			mapper.insertSelective(new Employee(null, uuid, "M", uuid+"@xiyou.com", 1));
		}
		System.out.println("批量完成");
	}

}
