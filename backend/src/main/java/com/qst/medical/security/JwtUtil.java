package com.qst.medical.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.util.StringUtils;

import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * JWT 工具类：生成 token、解析 token。
 *
 * <p>算法 HS256，载荷带 id / uname / role 三个自定义 claim，
 * subject 固定 medical-user，有效期 24 小时。</p>
 */
public class JwtUtil {

    /** 签名密钥（HS256，字节长度足够） */
    private static final String APP_SECRET = "huiyi-medical-jwt-secret-key-2026-zhangwei";
    private static final byte[] SECRET_BYTES = APP_SECRET.getBytes(StandardCharsets.UTF_8);

    /** 有效期：24 小时 */
    public static final long EXPIRE = 24 * 60 * 60 * 1000L;

    /** 请求头名 */
    public static final String HEADER = "Authorization";
    /** token 前缀 */
    public static final String TOKEN_PREFIX = "Bearer ";

    private JwtUtil() {
    }

    /**
     * 生成 token。
     *
     * @param id    用户 id
     * @param uname 登录名
     * @param role  角色名（ROLE_1 / ROLE_2）
     */
    public static String getJwtToken(Long id, String uname, String role) {
        return Jwts.builder()
                .setHeaderParam("typ", "JWT")
                .setHeaderParam("alg", "HS256")
                .setSubject("medical-user")
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRE))
                .claim("id", id)
                .claim("uname", uname)
                .claim("role", role)
                .signWith(SignatureAlgorithm.HS256, SECRET_BYTES)
                .compact();
    }

    /**
     * 解析 token。
     *
     * @param token 不含 Bearer 前缀的 token
     * @return 载荷；token 为空、签名不匹配或已过期时返回 null
     */
    public static Claims getClaimsByToken(String token) {
        if (!StringUtils.hasText(token)) {
            return null;
        }
        try {
            return Jwts.parser()
                    .setSigningKey(SECRET_BYTES)
                    .parseClaimsJws(token.trim())
                    .getBody();
        } catch (Exception e) {
            // 过期、篡改、格式错误统一视为未登录
            return null;
        }
    }

    /**
     * 从请求头值里剥掉 Bearer 前缀。
     *
     * <p>只认 {@code Bearer <token>}：其它认证方案（Basic 等）或裸 token 一律返回 null，
     * 避免把别的凭据当 JWT 去解析。前端 request.js 统一发 Bearer。</p>
     */
    public static String resolveToken(String header) {
        if (!StringUtils.hasText(header)) {
            return null;
        }
        if (!header.startsWith(TOKEN_PREFIX)) {
            return null;
        }
        String token = header.substring(TOKEN_PREFIX.length()).trim();
        return StringUtils.hasText(token) ? token : null;
    }

    /** claim 里的 id 反序列化后可能是 Integer，统一取 long */
    public static Long getId(Claims claims) {
        if (claims == null) {
            return null;
        }
        Object id = claims.get("id");
        return id instanceof Number ? ((Number) id).longValue() : null;
    }
}
