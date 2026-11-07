package com.qst.medical.config;

import com.qst.medical.security.JwtFilter;
import com.qst.medical.security.handler.AuthAccessDeniedHandler;
import com.qst.medical.security.handler.AuthEntryPoint;
import com.qst.medical.security.handler.AuthFailHandler;
import com.qst.medical.security.handler.AuthSuccessHandler;
import com.qst.medical.service.AccountService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import javax.annotation.Resource;

/**
 * Spring Security 配置。
 *
 * <p>要点：</p>
 * <ol>
 *   <li>JwtFilter 放在 UsernamePasswordAuthenticationFilter 之前，先解析 token；</li>
 *   <li>登录入口 /api/login 由 formLogin 处理，成功/失败分别交给两个 Handler 返回 Msg；</li>
 *   <li>放行登录、Swagger、/image/**，其余全部需要认证；</li>
 *   <li>无状态（STATELESS）+ 关闭 CSRF + 开启 CORS。</li>
 * </ol>
 */
@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true, jsr250Enabled = true)
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {

    /** 无需认证即可访问的路径 */
    private static final String[] PERMIT_ALL = {
            "/api/login",
            "/login",
            // 自助注册
            "/api/register",
            // Swagger2
            "/v2/api-docs",
            "/swagger-resources",
            "/swagger-resources/**",
            "/swagger-ui.html",
            "/doc.html",
            "/webjars/**",
            // 药品图片等静态资源
            "/image/**",
            // 错误转发
            "/error",
            "/favicon.ico"
    };

    @Resource
    private JwtFilter jwtFilter;
    @Resource
    private AccountService accountService;
    @Resource
    private AuthSuccessHandler authSuccessHandler;
    @Resource
    private AuthFailHandler authFailHandler;
    @Resource
    private AuthEntryPoint authEntryPoint;
    @Resource
    private AuthAccessDeniedHandler authAccessDeniedHandler;

    /** 密码加密器：数据库里存的是 BCrypt 密文 */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(accountService).passwordEncoder(passwordEncoder());
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        http.formLogin()
                .loginProcessingUrl("/api/login")
                .successHandler(authSuccessHandler)
                .failureHandler(authFailHandler)
                .permitAll()
                .and()
                .exceptionHandling()
                .authenticationEntryPoint(authEntryPoint)
                .accessDeniedHandler(authAccessDeniedHandler)
                .and()
                .authorizeRequests()
                .antMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                .antMatchers(PERMIT_ALL).permitAll()
                .anyRequest().authenticated()
                .and()
                .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
                .cors()
                .configurationSource(corsConfigurationSource())
                .and()
                .csrf().disable();
    }

    /** 允许前端开发服务器（9092）跨域访问 */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.addAllowedOriginPattern("*");
        configuration.addAllowedHeader("*");
        configuration.addAllowedMethod("*");
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
