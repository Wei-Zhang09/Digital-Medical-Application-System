package com.qst.medical.security.handler;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.qst.medical.common.Msg;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * Handler 统一输出工具：把 Msg 以 UTF-8 JSON 写回响应体。
 *
 * <p>HTTP 状态码固定 200，业务状态看响应体里的 code（20000/10000/10006），
 * 这样前端 axios 只需处理成功分支，不会因为 401 直接落到 error 回调。</p>
 */
final class MsgWriter {

    private static final ObjectMapper MAPPER = new ObjectMapper()
            .setSerializationInclusion(JsonInclude.Include.NON_NULL);

    private MsgWriter() {
    }

    static void write(HttpServletResponse response, Msg msg) throws IOException {
        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType("application/json;charset=UTF-8");
        response.setCharacterEncoding("UTF-8");
        try (PrintWriter writer = response.getWriter()) {
            writer.write(MAPPER.writeValueAsString(msg));
            writer.flush();
        }
    }
}
