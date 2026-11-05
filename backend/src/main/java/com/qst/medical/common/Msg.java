package com.qst.medical.common;

import lombok.Data;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 统一返回体。
 *
 * <p>返回码约定：{@link #CODE_SUCCESS} 20000 成功、{@link #CODE_FAIL} 10000 业务失败、
 * {@link #CODE_UNAUTHORIZED} 10006 未登录或 token 失效（前端收到后清本地状态并回登录页）。</p>
 *
 * <p>用法：{@code Msg.success().mess("登录成功").data("token", token)}。</p>
 */
@Data
public class Msg implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 成功 */
    public static final int CODE_SUCCESS = 20000;
    /** 业务失败 */
    public static final int CODE_FAIL = 10000;
    /** 未登录 / token 失效 */
    public static final int CODE_UNAUTHORIZED = 10006;

    private int code;
    private String mess;
    private Map<String, Object> data;

    public static Msg success() {
        return new Msg().with(CODE_SUCCESS, "操作成功");
    }

    public static Msg success(String mess) {
        return new Msg().with(CODE_SUCCESS, mess);
    }

    public static Msg fail() {
        return new Msg().with(CODE_FAIL, "操作失败");
    }

    public static Msg fail(String mess) {
        return new Msg().with(CODE_FAIL, mess);
    }

    public static Msg unauthorized() {
        return new Msg().with(CODE_UNAUTHORIZED, "未登录或登录已过期，请重新登录");
    }

    /** 链式设置提示语 */
    public Msg mess(String mess) {
        this.mess = mess;
        return this;
    }

    /** 链式追加一条 data */
    public Msg data(String key, Object value) {
        if (this.data == null) {
            this.data = new LinkedHashMap<>();
        }
        this.data.put(key, value);
        return this;
    }

    /** 链式合并一批 data */
    public Msg data(Map<String, Object> map) {
        if (map != null && !map.isEmpty()) {
            if (this.data == null) {
                this.data = new LinkedHashMap<>();
            }
            this.data.putAll(map);
        }
        return this;
    }

    private Msg with(int code, String mess) {
        this.code = code;
        this.mess = mess;
        return this;
    }
}
