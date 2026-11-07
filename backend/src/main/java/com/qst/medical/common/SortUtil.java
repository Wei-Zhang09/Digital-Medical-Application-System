package com.qst.medical.common;

import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * 列表排序白名单。
 *
 * <p>前端只能按白名单里的字段排序；字段名到 SQL 列的映射全部写死在服务端，
 * 不认识的字段直接忽略（返回 null，不排序），避免 {@code ORDER BY ${}} 注入。</p>
 *
 * <p>别名前缀（d./c.）与各 Mapper 的 SQL 保持一致，多表 join 时不会歧义。</p>
 */
public final class SortUtil {

    private SortUtil() {
    }

    private static final Map<String, Map<String, String>> ALLOWED;

    static {
        Map<String, Map<String, String>> all = new HashMap<>();

        all.put("doctor", map(
                "name", "d.name",
                "age", "d.age",
                "sex", "d.sex",
                "phone", "d.phone",
                "hospital", "d.hospital",
                "createtime", "d.createtime"));
        all.put("drug", map(
                "drugName", "drug_name",
                "drugInfo", "drug_info",
                "createtime", "createtime",
                "updatetime", "updatetime"));
        all.put("company", map(
                "companyName", "company_name",
                "companyPhone", "company_phone",
                "createtime", "createtime"));
        all.put("sale", map(
                "saleName", "sale_name",
                "salePhone", "sale_phone",
                "createtime", "createtime"));
        all.put("city", map(
                "cityNumber", "c.city_number",
                "createtime", "c.createtime"));
        all.put("medicalPolicy", map(
                "title", "title",
                "createTime", "create_time"));
        all.put("companyPolicy", map(
                "title", "title",
                "createTime", "create_time"));
        all.put("material", map(
                "title", "title",
                "createTime", "create_time"));
        all.put("account", map(
                "uname", "uname",
                "realname", "realname",
                "createtime", "createtime"));

        ALLOWED = Collections.unmodifiableMap(all);
    }

    private static Map<String, String> map(String... kv) {
        Map<String, String> m = new HashMap<>();
        for (int i = 0; i + 1 < kv.length; i += 2) {
            m.put(kv[i], kv[i + 1]);
        }
        return Collections.unmodifiableMap(m);
    }

    /**
     * 把前端传来的 sort/order 翻译成 PageHelper 的 orderBy 片段。
     *
     * @param module 模块名，见 {@link #ALLOWED} 的 key
     * @param sort   前端字段名，如 drugName
     * @param order  asc / desc，其他值一律当 asc
     * @return 形如 "drug_name DESC"；字段不在白名单时返回 null（表示不排序）
     */
    public static String build(String module, String sort, String order) {
        if (module == null || sort == null || sort.trim().isEmpty()) {
            return null;
        }
        Map<String, String> columns = ALLOWED.get(module);
        if (columns == null) {
            return null;
        }
        String column = columns.get(sort.trim());
        if (column == null) {
            return null;
        }
        String direction = "desc".equalsIgnoreCase(order) ? "DESC" : "ASC";
        return column + " " + direction;
    }

    /** 供前端/文档查看某模块支持哪些排序字段 */
    public static Map<String, String> columnsOf(String module) {
        return ALLOWED.getOrDefault(module, Collections.emptyMap());
    }

    /** 统一小写比较，避免前端传 Desc 之类 */
    public static boolean isDesc(String order) {
        return order != null && "desc".equals(order.trim().toLowerCase(Locale.ROOT));
    }
}
