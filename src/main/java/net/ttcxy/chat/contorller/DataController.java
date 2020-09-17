package net.ttcxy.chat.contorller;

import cn.hutool.core.util.StrUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.Map;

/**
 * created by HuangLei on 2020/9/17
 */
@Controller
public class DataController {

    @Autowired
    private HttpSession httpSession;

    private static Map<String,String> usernamePassword = new HashMap<>();

    @GetMapping("/add-username/{username}/{password}")
    @ResponseBody
    public String setUsername(@PathVariable("username") String username,@PathVariable("password") String password){
        if (StrUtil.isBlank(usernamePassword.get(username))){
            usernamePassword.put(username,password);
            httpSession.setAttribute("username",username);
            return "设置成功";
        }else{
            if(StrUtil.equals(usernamePassword.get(username),password)){
                return "设置成功";
            }else{
                return "密码错误";
            }
        }
    }

    @GetMapping("/get-username")
    @ResponseBody
    public Object getUsername(){
        return httpSession.getAttribute("username");
    }
}
