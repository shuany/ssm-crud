package com.shengsi.crud.test;

import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import com.github.pagehelper.PageInfo;
import com.shengsi.crud.bean.Employee;

/**
 * 使用spring测试模块提供的测试请求功能，测试crud请求的准确性 
 * spring test需要spring3.0以上的支持
 */

@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
@ContextConfiguration(locations = { "classpath:applicationContext.xml",
		"file:src/main/webapp/WEB-INF/dispatcherServlet-servlet.xml" })

public class MvcTest {

	// 传入springMVC的ioc
	@Autowired
	WebApplicationContext context;
	// 虚拟mvc请求，获取处理请求 Mock 虚假
	MockMvc mockMvc;

	// 初始化
	@Before
	public void initMockMvc() {
		mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
	}

	@SuppressWarnings("rawtypes")
	@Test
	public void testPage() throws Exception {
		// 模拟请求 拿到返回值
		MvcResult result = mockMvc.perform(MockMvcRequestBuilders.get("/emps").param("pn", "5")).andReturn();

		// 请求成功后，请求域中会有pageInfo 我们可以取出pageInfo进行验证
		MockHttpServletRequest request = result.getRequest();
		PageInfo pi = (PageInfo) request.getAttribute("pageInfo");
		System.out.println("当前页码:" + pi.getPageNum());
		System.out.println("总页码:" + pi.getPages());
		System.out.println("总记录数:" + pi.getTotal());
		System.out.println("在页面连续显示的页码:");
		int[] nums = pi.getNavigatepageNums();
		for (int i : nums) {
			System.out.println(" " + i);
		}

		// 获取员工数据
		@SuppressWarnings("unchecked")
		List<Employee> list = pi.getList();
		for (Employee employee : list) {
			System.out.println("ID:" + employee.getdId() + "==>NAME:" + employee.getEmpName());
		}
	}
}