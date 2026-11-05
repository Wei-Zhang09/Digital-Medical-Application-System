package com.qst.medical.security;

import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * JwtUtil 单元测试：签发、解析、请求头处理、篡改与空值。
 */
class JwtUtilTest {

    @Test
    @DisplayName("签发的 token 能解析回 id / uname / role")
    void getJwtToken_shouldCarryClaims() {
        String token = JwtUtil.getJwtToken(33L, "王小明4125", "ROLE_2");

        assertNotNull(token);
        assertEquals(3, token.split("\\.").length, "JWT 应该是三段");

        Claims claims = JwtUtil.getClaimsByToken(token);
        assertNotNull(claims);
        assertEquals(33, JwtUtil.getId(claims));
        assertEquals("王小明4125", claims.get("uname", String.class));
        assertEquals("ROLE_2", claims.get("role", String.class));
        assertEquals("medical-user", claims.getSubject());
    }

    @Test
    @DisplayName("resolveToken 去掉 Bearer 前缀")
    void resolveToken_shouldStripPrefix() {
        assertEquals("abc.def.ghi", JwtUtil.resolveToken("Bearer abc.def.ghi"));
    }

    @Test
    @DisplayName("resolveToken 对空值与非 Bearer 前缀返回 null")
    void resolveToken_shouldRejectInvalidHeader() {
        assertNull(JwtUtil.resolveToken(null));
        assertNull(JwtUtil.resolveToken(""));
        assertNull(JwtUtil.resolveToken("   "));
        assertNull(JwtUtil.resolveToken("Basic abc.def.ghi"));
    }

    @Test
    @DisplayName("空 token 解析返回 null")
    void getClaimsByToken_shouldReturnNullForBlank() {
        assertNull(JwtUtil.getClaimsByToken(null));
        assertNull(JwtUtil.getClaimsByToken(""));
    }

    @Test
    @DisplayName("被篡改的 token 解析返回 null（签名校验不通过）")
    void getClaimsByToken_shouldReturnNullWhenTampered() {
        String token = JwtUtil.getJwtToken(1L, "admin_1", "ROLE_1");
        // 改动最后一段（签名）的最后一个字符
        char last = token.charAt(token.length() - 1);
        String tampered = token.substring(0, token.length() - 1) + (last == 'a' ? 'b' : 'a');

        assertNull(JwtUtil.getClaimsByToken(tampered));
    }

    @Test
    @DisplayName("有效期是 24 小时")
    void expire_shouldBe24Hours() {
        assertEquals(24 * 60 * 60 * 1000L, JwtUtil.EXPIRE);
    }
}
