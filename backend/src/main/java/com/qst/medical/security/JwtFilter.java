package com.qst.medical.security;

import com.qst.medical.domain.dto.AccountDto;
import io.jsonwebtoken.Claims;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;

/**
 * JWT 过滤器：每个请求解析 Authorization 头，验签通过则写入 SecurityContext。
 *
 * <p>在 {@code WebSecurityConfig} 里被放在 UsernamePasswordAuthenticationFilter 之前。
 * 没有 token 或 token 无效时不抛异常，交给后续的 AuthenticationEntryPoint 返回 10006。</p>
 */
@Component
public class JwtFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String token = JwtUtil.resolveToken(request.getHeader(JwtUtil.HEADER));
        Claims claims = JwtUtil.getClaimsByToken(token);
        if (claims != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            String uname = claims.get("uname", String.class);
            String role = claims.get("role", String.class);
            if (uname != null && role != null) {
                // principal 用 AccountDto，后续接口能直接拿到 id
                AccountDto principal = new AccountDto();
                principal.setId(JwtUtil.getId(claims));
                principal.setUname(uname);
                principal.setUtype(role);
                principal.setRole(role);

                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                        principal, null, Collections.singletonList(new SimpleGrantedAuthority(role)));
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        }
        chain.doFilter(request, response);
    }
}
