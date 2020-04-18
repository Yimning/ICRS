package com.shencangblue.design.icrs.controller;

import com.shencangblue.design.icrs.model.Student;

import com.shencangblue.design.icrs.result.Result;
import com.shencangblue.design.icrs.result.ResultFactory;
import com.shencangblue.design.icrs.service.StudentService;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.AuthenticationException;

import org.apache.shiro.authc.IncorrectCredentialsException;
import org.apache.shiro.authc.UnknownAccountException;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.crypto.SecureRandomNumberGenerator;
import org.apache.shiro.crypto.hash.SimpleHash;
import org.apache.shiro.subject.Subject;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.HtmlUtils;

import javax.annotation.Resource;
import java.util.List;


@Controller
public class StudentController {
    @Resource
    StudentService studentService;

    @CrossOrigin
    @PostMapping(value = "api/login")
    @ResponseBody
    public Result login(@RequestBody Student requestStudent) {
        // 对 html 标签进行转义，防止 XSS 攻击
        String studentIdName = requestStudent.getStudentIdName();
        studentIdName =HtmlUtils.htmlEscape(studentIdName);

        Subject subject = SecurityUtils.getSubject();
        UsernamePasswordToken usernamePasswordToken = new UsernamePasswordToken(studentIdName,requestStudent.getPassword());
        usernamePasswordToken.setRememberMe(true);
        try{
            Student student = studentService.getByStudentIdName(studentIdName);
            if (!student.isEnabled()){
                return ResultFactory.buildFailResult("该用户已被禁用");
            }
            subject.login(usernamePasswordToken);
            return ResultFactory.buildSuccessResult(usernamePasswordToken);
        }catch (IncorrectCredentialsException e){
            return ResultFactory.buildFailResult("密码错误");
        } catch (UnknownAccountException e) {
            return ResultFactory.buildFailResult("账号不存在");
        }
    }
    @CrossOrigin
    @ResponseBody
    @GetMapping(value = "api/logout")
    public Result logout(){
        Subject subject = SecurityUtils.getSubject();
        subject.logout();
        String message= "成功登出账号";
        return ResultFactory.buildSuccessResult(message);
    }

    @CrossOrigin
    @PostMapping(value = "api/register-stu")
    @ResponseBody
    public Result register(@RequestBody Student student) {
        int status = studentService.register(student);
        switch (status){
            case 0:
                return ResultFactory.buildFailResult("用户名和密码不能为空");
            case 1:
                return ResultFactory.buildSuccessResult("注册成功");
            case 2:
                return ResultFactory.buildFailResult("用户已存在");
        }
        return ResultFactory.buildFailResult("未知错误");
    }

    @CrossOrigin
    @ResponseBody
    @GetMapping(value = "api/authentication")
    public String authentication(){
        return "身份认证成功";
    }

    @CrossOrigin
    @ResponseBody
    @GetMapping("/api/admin/user")
    public Iterable<Student> listUsers() {
        return studentService.list();
    }

    @CrossOrigin
    @ResponseBody
    @PostMapping("/api/admin/user/status")
    public Result updateUserStatus(@RequestBody Student requestStudent) {
        if (studentService.updateStudentStatus(requestStudent)) {
            return ResultFactory.buildSuccessResult("用户状态更新成功");
        } else {
            return ResultFactory.buildFailResult("参数错误，更新失败");
        }
    }
    @CrossOrigin
    @ResponseBody
    @PostMapping("/api/admin/user/password")
    public Result resetPassword(@RequestBody Student requestStudent) {
        if (studentService.resetPassword(requestStudent)) {
            return ResultFactory.buildSuccessResult("重置密码成功");
        } else {
            return ResultFactory.buildFailResult("参数错误，重置失败");
        }
    }

    @CrossOrigin
    @ResponseBody
    @PostMapping("/api/admin/user")
    public Result editUser(@RequestBody Student requestStudent) {
        if(studentService.editUser(requestStudent)) {
            return ResultFactory.buildSuccessResult("修改用户信息成功");
        } else {
            return ResultFactory.buildFailResult("参数错误，修改失败");
        }
    }
}

