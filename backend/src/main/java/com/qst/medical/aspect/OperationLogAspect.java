package com.qst.medical.aspect;

import com.qst.medical.domain.dto.AccountDto;
import com.qst.medical.domain.entity.OperationLog;
import com.qst.medical.domain.dataobject.AccountDo;
import com.qst.medical.mapper.AccountMapper;
import com.qst.medical.mapper.OperationLogMapper;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.Date;

/**
 * 操作日志切面。
 *
 * <p>切所有 Controller 里的写操作（POST/PUT/DELETE），成功后落一条日志。
 * 模块名从 URI 前缀推断，动作从 HTTP 方法推断，不要求每个接口加注解——
 * 新加接口只要路径按约定走，日志自动就有了。</p>
 *
 * <p>写日志失败只打 warn，绝不影响业务返回。</p>
 */
@Aspect
@Component
public class OperationLogAspect {

    private static final Logger log = LoggerFactory.getLogger(OperationLogAspect.class);

    @Resource
    private OperationLogMapper operationLogMapper;

    @Resource
    private AccountMapper accountMapper;

    /** 只切 Controller 包下的公开方法 */
    @Pointcut("execution(public * com.qst.medical.controller..*(..))")
    public void controllerMethods() {
    }

    @AfterReturning("controllerMethods()")
    public void recordAfterReturning(JoinPoint joinPoint) {
        try {
            ServletRequestAttributes attributes =
                    (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes == null) {
                return;
            }
            HttpServletRequest request = attributes.getRequest();
            String method = request.getMethod();
            if (!"POST".equals(method) && !"PUT".equals(method) && !"DELETE".equals(method)) {
                return;
            }

            String uri = request.getRequestURI();
            // 登录、注册这类接口不记（登录本身就是审计，注册没有操作人）
            if (uri.contains("/api/login") || uri.contains("/api/register")) {
                return;
            }

            OperationLog entity = new OperationLog();
            entity.setModule(moduleOf(uri));
            entity.setAction(actionOf(method, uri));
            entity.setDetail(method + " " + uri);
            entity.setIp(clientIp(request));
            entity.setCreatetime(new Date());

            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.getPrincipal() instanceof AccountDto) {
                AccountDto account = (AccountDto) authentication.getPrincipal();
                entity.setUname(account.getUname());
                // JwtFilter 里的 principal 只有 id/uname/role，姓名要回查一次
                if (account.getId() != null) {
                    AccountDo full = accountMapper.selectById(account.getId());
                    if (full != null) {
                        entity.setRealname(full.getRealname());
                    }
                }
            }
            if (entity.getUname() == null) {
                entity.setUname("匿名");
            }

            operationLogMapper.insert(entity);
        } catch (Exception e) {
            // 日志失败不能影响业务
            log.warn("记录操作日志失败：{}", e.getMessage());
        }
    }

    /** URI 前缀 → 模块中文名 */
    private String moduleOf(String uri) {
        if (uri.contains("/api/doctors")) {
            return "医师信息";
        }
        if (uri.contains("/api/drugs")) {
            return "药品信息";
        }
        if (uri.contains("/api/companies")) {
            return "医药公司";
        }
        if (uri.contains("/api/sales")) {
            return "销售地点";
        }
        if (uri.contains("/api/cities")) {
            return "城市信息";
        }
        if (uri.contains("/api/medicalPolicies")) {
            return "医保政策";
        }
        if (uri.contains("/api/companyPolicies")) {
            return "公司政策";
        }
        if (uri.contains("/api/materials")) {
            return "必备材料";
        }
        if (uri.contains("/api/accounts")) {
            return "账号管理";
        }
        if (uri.contains("/api/export")) {
            return "数据导出";
        }
        return "其他";
    }

    /** HTTP 方法 + 特殊路径 → 动作 */
    private String actionOf(String method, String uri) {
        if (uri.contains("/password")) {
            return "重置口令";
        }
        if (uri.contains("/api/export")) {
            return "导出";
        }
        switch (method) {
            case "POST":
                return "新增";
            case "PUT":
                return "修改";
            case "DELETE":
                return "删除";
            default:
                return method;
        }
    }

    /** 取真实客户端 IP（考虑 Nginx 反代） */
    private String clientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip)) {
            int comma = ip.indexOf(',');
            return comma > 0 ? ip.substring(0, comma).trim() : ip.trim();
        }
        ip = request.getHeader("X-Real-IP");
        if (ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip)) {
            return ip;
        }
        return request.getRemoteAddr();
    }
}
