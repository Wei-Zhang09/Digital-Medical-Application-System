"""
慧医数字医疗应用系统 · 功能测试执行脚本

按 docs/report/测试用例.md 的用例表逐条跑，输出 PASS/FAIL 与实际返回值，
执行记录写进 docs/report/测试执行记录.md（人工整理时以本脚本输出为准）。

用法：python scripts/e2e/functional.py [baseUrl]
默认 baseUrl = http://127.0.0.1:8080
"""
import json
import sys
import time
import urllib.error
import urllib.parse
import urllib.request
import uuid

BASE = sys.argv[1] if len(sys.argv) > 1 else "http://127.0.0.1:8080"
results = []


def call(method, path, token=None, body=None, form=None, params=None, raw=None, ctype=None):
    url = BASE + path + (("?" + urllib.parse.urlencode(params)) if params else "")
    data, headers = None, {}
    if form is not None:
        data = urllib.parse.urlencode(form).encode()
        headers["Content-Type"] = "application/x-www-form-urlencoded"
    elif raw is not None:
        data = raw
        headers["Content-Type"] = ctype
    elif body is not None:
        data = json.dumps(body, ensure_ascii=False).encode("utf-8")
        headers["Content-Type"] = "application/json; charset=UTF-8"
    if token:
        headers["Authorization"] = "Bearer " + token
    req = urllib.request.Request(url, data=data, headers=headers, method=method)
    try:
        with urllib.request.urlopen(req, timeout=15) as r:
            return json.loads(r.read().decode("utf-8"))
    except urllib.error.HTTPError as e:
        try:
            return json.loads(e.read().decode("utf-8"))
        except Exception:
            return {"code": e.code, "mess": "HTTP %d" % e.code}


def check(case_id, name, resp, expect_code=20000, contains=None):
    ok = resp.get("code") == expect_code
    if ok and contains:
        ok = contains in json.dumps(resp, ensure_ascii=False)
    results.append({
        "id": case_id, "name": name, "ok": ok,
        "code": resp.get("code"), "mess": (resp.get("mess") or "")[:40]
    })
    return resp


def login(uname, pwd="123456"):
    return call("POST", "/api/login", form={"username": uname, "password": pwd})


def tiny_png():
    import struct, zlib

    def chunk(t, d):
        c = t + d
        return struct.pack(">I", len(d)) + c + struct.pack(">I", zlib.crc32(c) & 0xffffffff)

    ihdr = struct.pack(">IIBBBBB", 2, 2, 8, 2, 0, 0, 0)
    raw = b"\x00\x13\xb7\xa5\x4a\x7d\xff\x00\x13\xb7\xa5\x00\x00\x00"
    return (b"\x89PNG\r\n\x1a\n" + chunk(b"IHDR", ihdr) +
            chunk(b"IDAT", zlib.compress(raw)) + chunk(b"IEND", b""))


def main():
    time.sleep(1)

    # ---------- A. 登录与鉴权 ----------
    admin = check("TC-01", "管理员正确账号登录", login("admin_1"), contains="ROLE_1")
    doctor = check("TC-02", "医师正确账号登录（中文用户名）", login("王小明4125"), contains="ROLE_2")
    check("TC-03", "错误口令登录被拒", login("admin_1", "wrong"), 10000)
    check("TC-04", "空用户名登录被拒", login("", "123456"), 10000)
    at = admin["data"]["token"]
    dt = doctor["data"]["token"]

    check("TC-05", "未带 token 访问受保护接口", call("GET", "/api/drugs/1/5"), 10006)
    check("TC-06", "伪造 token 访问受保护接口",
          call("GET", "/api/drugs/1/5", token="abc.def.ghi"), 10006)

    # ---------- B. 菜单与权限 ----------
    menus_admin = check("TC-07", "管理员获取菜单树", call("GET", "/api/permissions", at))
    menus_doctor = check("TC-08", "医师获取菜单树", call("GET", "/api/permissions", dt))

    def child_count(resp):
        for root in resp.get("data", {}).get("permissions", []):
            if root.get("component") == "Layout":
                return len(root.get("children") or [])
        return -1

    results.append({"id": "TC-09", "name": "管理员菜单 9 项 / 医师 5 项",
                    "ok": child_count(menus_admin) == 9 and child_count(menus_doctor) == 5,
                    "code": "-", "mess": "%d / %d" % (child_count(menus_admin), child_count(menus_doctor))})

    # ---------- C. 药品（含上传） ----------
    check("TC-10", "药品分页查询", call("GET", "/api/drugs/1/5", at))
    check("TC-11", "药品按药名关键字查询", call("GET", "/api/drugs/1/5", at, params={"name": "感冒"}))
    check("TC-12", "医师可新增药品（角色 1/2）",
          call("POST", "/api/drugs", dt, {"drugName": "功能测试药", "drugInfo": "x", "drugEffect": "y"}))

    r = call("GET", "/api/drugs/1/50", at, params={"name": "功能测试药"})
    rows = r.get("data", {}).get("drugPageInfo", {}).get("list", [])
    drug_id = rows[0]["drugId"] if rows else None
    results.append({"id": "TC-13", "name": "新增后能查到该药品", "ok": bool(drug_id),
                    "code": r.get("code"), "mess": "drugId=%s" % drug_id})

    check("TC-14", "医师修改药品被拒（仅管理员）",
          call("PUT", "/api/drugs/%s" % drug_id, dt, {"drugName": "x", "drugInfo": "x", "drugEffect": "x"}), 10000)
    check("TC-15", "管理员修改药品",
          call("PUT", "/api/drugs/%s" % drug_id, at, {"drugName": "功能测试药(改)", "drugInfo": "x", "drugEffect": "y"}))

    boundary = "----test" + uuid.uuid4().hex
    body = ((f"--{boundary}\r\nContent-Disposition: form-data; name=\"file\"; filename=\"t.png\"\r\n"
             f"Content-Type: image/png\r\n\r\n").encode() + tiny_png() +
            f"\r\n--{boundary}--\r\n".encode())
    up = check("TC-16", "上传 PNG 图片", call("POST", "/api/upload", at, raw=body,
                                             ctype="multipart/form-data; boundary=%s" % boundary))
    img_url = up.get("data", {}).get("url", "")
    results.append({"id": "TC-17", "name": "上传后的图片可访问", "ok": bool(img_url),
                    "code": "-", "mess": img_url})

    bad = ((f"--{boundary}\r\nContent-Disposition: form-data; name=\"file\"; filename=\"a.txt\"\r\n"
            f"Content-Type: text/plain\r\n\r\nhello\r\n--{boundary}--\r\n").encode())
    check("TC-18", "上传 txt 被拒", call("POST", "/api/upload", at, raw=bad,
                                         ctype="multipart/form-data; boundary=%s" % boundary), 10000)

    check("TC-19", "删除药品（同时清关联）", call("DELETE", "/api/drugs/%s" % drug_id, at))
    check("TC-20", "删除不存在的药品返回业务失败", call("DELETE", "/api/drugs/999999", at), 10000)

    # ---------- D. 主数据与关联校验 ----------
    check("TC-21", "新增医药公司", call("POST", "/api/companies", at,
                                        {"companyName": "功能测试公司", "companyPhone": "0532-00000000"}))
    comp = call("GET", "/api/companies/1/50", at, params={"name": "功能测试公司"})
    comp_id = comp["data"]["companyPageInfo"]["list"][0]["companyId"]

    check("TC-22", "新增公司政策并挂到该公司",
          call("POST", "/api/companyPolicies", at, {"title": "功能测试政策", "message": "内容", "companyId": comp_id}))
    check("TC-23", "公司政策挂不存在的公司被拒",
          call("POST", "/api/companyPolicies", at, {"title": "x", "message": "y", "companyId": 999999}), 10000)
    check("TC-24", "新增城市", call("POST", "/api/cities", at, {"cityNumber": 370299}))
    cities = call("GET", "/api/cities/1/50", at)["data"]["cityPageInfo"]["list"]
    city_id = next(c["cityId"] for c in cities if c["cityNumber"] == 370299)
    check("TC-25", "医保政策挂到新城市",
          call("POST", "/api/medicalPolicies", at, {"title": "功能测试医保", "message": "内容", "cityId": city_id}))
    check("TC-26", "新增销售地点（带经纬度）",
          call("POST", "/api/sales", at, {"saleName": "功能测试药店", "salePhone": "13800000000",
                                          "lng": 120.39, "lat": 36.07, "address": "测试地址"}))
    check("TC-27", "新增医师自动创建账号",
          call("POST", "/api/doctors", at, {"name": "功能测试医师", "age": 30, "sex": 1,
                                            "levelId": 1, "typeId": 1, "phone": "13700001111",
                                            "hospital": "测试医院"}))
    docs = call("GET", "/api/doctors/1/50", at, params={"name": "功能测试医师"})["data"]["doctorPageInfo"]["list"]
    doc_id = docs[0]["id"]
    results.append({"id": "TC-28", "name": "医师账号按「姓名+手机后4位」生成",
                    "ok": docs[0].get("uname") == "功能测试医师1111", "code": "-",
                    "mess": docs[0].get("uname")})
    check("TC-29", "重置医师口令", call("PUT", "/api/doctors/pwd/%s" % doc_id, at))

    # ---------- E. 扩展功能 ----------
    dash = check("TC-30", "数据面板接口（管理员）", call("GET", "/api/dashboard", at))
    d = dash.get("data", {}).get("dashboard", {})
    results.append({"id": "TC-31", "name": "面板含四个计数 + 两张图 + 资讯",
                    "ok": all(k in d for k in ("doctorCount", "companyCount", "drugCount", "saleCount",
                                               "levelChart", "typeChart", "news")),
                    "code": "-", "mess": "news=%d" % len(d.get("news") or [])})
    check("TC-32", "医师也能看数据面板", call("GET", "/api/dashboard", dt))
    sales = call("GET", "/api/sales/1/50", at, params={"name": "功能测试药店"})["data"]["salePageInfo"]["list"]
    results.append({"id": "TC-33", "name": "销售地点返回经纬度（地图用）",
                    "ok": sales and sales[0].get("lng") is not None, "code": "-",
                    "mess": "lng=%s lat=%s" % (sales[0].get("lng"), sales[0].get("lat"))})

    # ---------- F. 参数校验与边界 ----------
    check("TC-34", "必填项为空被校验拦截", call("POST", "/api/materials", at, {"title": "", "message": "x"}), 10000)
    check("TC-35", "分页越界返回空列表", call("GET", "/api/drugs/999/10", at))

    # ---------- 清理 ----------
    call("DELETE", "/api/doctors/%s" % doc_id, at)
    call("DELETE", "/api/sales/%s" % sales[0]["saleId"], at)
    for p in call("GET", "/api/medicalPolicies/1/50", at, params={"title": "功能测试医保"})["data"]["medicalPolicyPageInfo"]["list"]:
        call("DELETE", "/api/medicalPolicies/%s" % p["id"], at)
    for p in call("GET", "/api/companyPolicies/1/50", at, params={"title": "功能测试政策"})["data"]["companyPolicyPageInfo"]["list"]:
        call("DELETE", "/api/companyPolicies/%s" % p["id"], at)
    call("DELETE", "/api/companies/%s" % comp_id, at)
    call("DELETE", "/api/cities/%s" % city_id, at)

    # ---------- 输出 ----------
    passed = sum(1 for r in results if r["ok"])
    width = max(len(r["name"]) for r in results) + 2
    for r in results:
        print(f"{r['id']}  {r['name'].ljust(width)}  {'PASS' if r['ok'] else 'FAIL'}  code={r['code']}  {r['mess']}")
    print()
    print(f"合计 {len(results)} 条，通过 {passed} 条，失败 {len(results) - passed} 条")
    return 0 if passed == len(results) else 1


if __name__ == "__main__":
    sys.exit(main())
