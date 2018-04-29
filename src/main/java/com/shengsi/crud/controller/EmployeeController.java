package com.shengsi.crud.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.shengsi.crud.bean.Employee;
import com.shengsi.crud.bean.Msg;
import com.shengsi.crud.service.EmployeeService;

/**
 * 处理CRUD请求
 *
 */
@Controller
public class EmployeeController {

	@Autowired
	EmployeeService employeeService;
	
	/**
	 * 批量 员工删除 二合一
	 * 多个id 用1-2-3...
	 */
	
	/**
	 * 单个员工删除
	 * @param id
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value="/emp/{ids}",method = RequestMethod.DELETE)
	
	public Msg deleteEmpById(@PathVariable("ids")String ids){
		//批量删除
		if (ids.contains("-")) {
			List<Integer> del_ids = new ArrayList<>();
			String[] str_ids =  ids.split("-");
			//组装id的集合
			for (String string : str_ids) {
				del_ids.add(Integer.parseInt(string));
			}
			employeeService.deleteBatch(del_ids);
			
		}else
		//单个删除
		{
			Integer id = Integer.parseInt(ids);
			employeeService.deleteEmp(id);
		}
		
		return Msg.success();
		
	}
	
	/**
	 * 直接发送ajax=PUT的请求 封装的数据 除了id 其他全是null
	 * Employee [empId=1023, empName=null, gender=null, email=null, dId=null]
	 * 
	 * 请求体中 有数据，但是Employee不能封装
	 * update tbl_emp where emp_id = 1023 xxxxxxxxxxx
	 * 原因：
	 * Tomcat：	
	 * 		将请求体重的数据，封装一个map
	 * 		request.getParameter("empName")就会从这个map中取出值
	 * 		springMVC封装POJO 对象的时候，
	 * 			会把POJO中每个属性的值：request.getParameter("email");
	 * 
	 * ajax不能直接发送PUT请求
	 * 		PUT请求：请求体中的数据 request.getParameter("xxx") 拿不到
	 * 		tomcat 看到PUT请求 便不会封装请求体中的数据为map 只有POST形式才能封装请求体为map
	 * 我们需要能直接发送PUT之类的请求，并封装请求体中的数据
	 * 配置上HttpPutFormContentFilter 作用：将请求体中的数据解析包装成一个map 
	 * request被重新包装   request.getParameter()被重写，就会从自己封装的map中取数据
	 * 员工更新方法
	 * @param employee
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value="/emp/{empId}",method = RequestMethod.PUT)
	public Msg saveEmp(Employee employee,HttpServletRequest request){
		System.out.println("请求体中的值：" + request.getParameter("gender"));
		System.out.println("将要更新的员工数据：" +employee);
		employeeService.updateEmp(employee);
		return Msg.success();
	}
	
	/**
	 * 根据id查询员工
	 * @param id
	 * @return
	 */
	@RequestMapping(value="/emp/{id}",method = RequestMethod.GET)
	@ResponseBody
	public Msg getEmp(@PathVariable("id")Integer id){
		Employee employee = employeeService.getEmp(id);
		return Msg.success().add("emp", employee);
		
	}
	
	/**
	 * 检查用户名是否可用
	 * @param empName
	 * @return
	 */
	@ResponseBody
	@RequestMapping("/checkuser")
	public Msg checkuser(@RequestParam("empName")String empName){
		//先判断用户名是否是合法的表达式； java里边regx没有/ js里边的regx有/
		String regx = "(^[a-zA-Z0-9_-]{6,16}$)|(^[\u2E80-\u9FFF]{2,5})";
		if(!empName.matches(regx)){
			return Msg.fail().add("va_msg", "用户名必须是6-16位数字和字母的组合或者2-5位中文");
		}
		
		//数据库用户名重复校验
		boolean b = employeeService.checkUser(empName);
		if(b){
			return Msg.success();
		}else{
			return Msg.fail().add("va_msg", "用户名不可用");
		}
	}
	
	/**
	 * 员工保存
	 * 支持JSR303校验
	 * 导入hibernate-validator 5.4.1
	 * @return
	 */
	@RequestMapping(value="/emp",method=RequestMethod.POST)
	@ResponseBody
	public Msg saveEmp(@Valid Employee employee,BindingResult result){
		if(result.hasErrors()){
			//校验失败 应该返回迪拜 在状态框中显示校验失败的错误信息
			Map<String, Object> map = new  HashMap<>();
			List<FieldError> errors = result.getFieldErrors();
			for (FieldError fieldError : errors) {
				System.out.println("错误的字段名：" + fieldError.getField());
				System.out.println("错误信息：" + fieldError.getDefaultMessage());
				map.put(fieldError.getField(),fieldError.getDefaultMessage());
			}
			
			return Msg.fail().add("errorFields", map);
		}else {
			employeeService.savaEmp(employee); 
			return Msg.success();
		}
	}

	/**
	 * 导入jakson databind包 负责将对象转换成jason字符串
	 * @param pn
	 * @return
	 */
	@RequestMapping("/emps")
	@ResponseBody
	public Msg getEmpsWithJason(
			@RequestParam(value = "pn", defaultValue = "1") Integer pn) {

		PageHelper.startPage(pn, 5);
		// 下边的查询就是一个分页查询了
		List<Employee> emps = employeeService.getAll();

		// 使用pageInfo包装查询结果 只需要将pageInfo交给页面即可
		// 封装了详细的分页信息 包括查询出来的数据 连续显示的页数 为5
		@SuppressWarnings({ "rawtypes", "unchecked" })
		PageInfo page = new PageInfo(emps, 5);
		return Msg.success().add("pageInfo",page);

	}

	/**
	 * 查询员工数据 分页查询
	 * 
	 * @return
	 */
	// @RequestMapping("/emps")
	public String getEmps(@RequestParam(value = "pn", defaultValue = "1") Integer pn, Model model) {
		// 这不是一个分页
		// 引用pagehelper插件
		// 在查询之前调用 pn第几页 5 每页查询5条
		PageHelper.startPage(pn, 5);
		// 下边的查询就是一个分页查询了
		List<Employee> emps = employeeService.getAll();

		// 使用pageInfo包装查询结果 只需要将pageInfo交给页面即可
		// 封装了详细的分页信息 包括查询出来的数据 连续显示的页数 为5
		@SuppressWarnings({ "rawtypes", "unchecked" })
		PageInfo page = new PageInfo(emps, 5);
		model.addAttribute("pageInfo", page);
		// page.getNavigatepageNums();

		return "list";
	}

}
