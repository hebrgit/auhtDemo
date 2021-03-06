package com.hebo.authDemo.config.auth;

import cn.hutool.jwt.JWT;
import cn.hutool.jwt.JWTUtil;
import cn.hutool.jwt.signers.JWTSigner;
import cn.hutool.jwt.signers.JWTSignerUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.hebo.authDemo.entity.LoginUser;
import com.hebo.authDemo.entity.User;
import com.hebo.authDemo.utils.RedisCache;
import com.hebo.dto.Response;
import io.jsonwebtoken.Jws;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.stereotype.Component;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.security.Principal;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

/**
 * @Author: Hutengfei
 * @Description: 登出成功处理逻辑
 * @Date Create in 2019/9/4 10:17
 */
@Component
public class CustomizeLoginSuccessHandler implements AuthenticationSuccessHandler {


    @Autowired
    private RedisCache redisCache;
    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {

        LoginUser principal = (LoginUser)authentication.getPrincipal();
        User user = principal.getUser();
        Long userId = user.getUserId();
        //用户信息存入到redis
        redisCache.setCacheObject(userId.toString(), JSONObject.toJSONString(user),60*60*1000, TimeUnit.SECONDS);
        final JWTSigner signer = JWTSignerUtil.hs256(userId.toString().getBytes());
        String token = JWTUtil.createToken(new HashMap<String, Object>() {
            {
                put("userId", user);
                put("expireTime",System.currentTimeMillis()+60*60*1000);

            }
        }, signer);
        HashMap<String, String> map = new HashMap<>();
        map.put("token",token);
        response.setContentType("text/json;charset=utf-8");
        Response result = Response.success(Response.getSUCCESS(),"登录成功",map);
        response.getWriter().write(JSON.toJSONString(result));
    }
}





