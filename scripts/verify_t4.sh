#!/usr/bin/env bash
# T4 验证脚本：启动 jar -> 跑登录/JWT/Swagger 用例 -> 关掉
set -u
PROJ="/d/HermesWorkSpace/Digital Medical Application System"
OUT="$PROJ/.dev/verify-t4.txt"
LOG="$PROJ/.dev/app-verify.log"
TMP="$PROJ/.dev/tmp-t4"
TMPW="D:/HermesWorkSpace/Digital Medical Application System/.dev/tmp-t4"
mkdir -p "$TMP"
: > "$OUT"

cd "$PROJ/backend" || { echo "no backend dir" >> "$OUT"; exit 1; }
source "$PROJ/scripts/env.sh" >/dev/null 2>&1

: > "$LOG"
java -jar target/medicai-1.0-SNAPSHOT.jar > "$LOG" 2>&1 &
APP_PID=$!
echo "== 启动 java -jar (pid=$APP_PID) ==" >> "$OUT"

ready=0
for i in $(seq 1 90); do
  code=$(curl.exe --max-time 2 -s -o /dev/null -w "%{http_code}" "http://127.0.0.1:8080/swagger-resources" 2>/dev/null)
  if [ "$code" = "200" ]; then ready=1; break; fi
  sleep 1
done
if [ "$ready" != "1" ]; then
  echo "启动失败，日志尾部：" >> "$OUT"
  tail -30 "$LOG" >> "$OUT"
  kill $APP_PID 2>/dev/null
  exit 1
fi
echo "启动成功，用时约 ${i}s" >> "$OUT"
echo "" >> "$OUT"

BASE="http://127.0.0.1:8080"

# 1) 管理员登录
echo "== 1) POST /api/login admin_1/123456 ==" >> "$OUT"
curl.exe --max-time 10 -s -X POST "$BASE/api/login" -d "username=admin_1&password=123456" -o "$TMPW/login_admin.json"
cat "$TMP/login_admin.json" >> "$OUT"; echo "" >> "$OUT"
echo "" >> "$OUT"

# 2) 密码错误
echo "== 2) POST /api/login 错误密码 ==" >> "$OUT"
curl.exe --max-time 10 -s -X POST "$BASE/api/login" -d "username=admin_1&password=wrong-pwd" -o "$TMPW/login_bad.json"
cat "$TMP/login_bad.json" >> "$OUT"; echo "" >> "$OUT"
echo "" >> "$OUT"

# 3) 医师登录（中文登录名，UTF-8 百分号编码）
echo "== 3) POST /api/login 王小明4125/123456 ==" >> "$OUT"
curl.exe --max-time 10 -s -X POST "$BASE/api/login" \
  -d "username=%E7%8E%8B%E5%B0%8F%E6%98%8E4125&password=123456" -o "$TMPW/login_doctor.json"
cat "$TMP/login_doctor.json" >> "$OUT"; echo "" >> "$OUT"
echo "" >> "$OUT"

# 4) 无 token 访问受保护路径
echo "== 4) GET /api/permissions 无 token ==" >> "$OUT"
c=$(curl.exe --max-time 10 -s -o "$TMPW/no_token.json" -w "%{http_code}" "$BASE/api/permissions")
echo "http=$c" >> "$OUT"
cat "$TMP/no_token.json" >> "$OUT"; echo "" >> "$OUT"
echo "" >> "$OUT"

# 5) 伪造 token
echo "== 5) GET /api/permissions 伪造 token ==" >> "$OUT"
c=$(curl.exe --max-time 10 -s -o "$TMPW/bad_token.json" -w "%{http_code}" -H "Authorization: Bearer abc.def.ghi" "$BASE/api/permissions")
echo "http=$c" >> "$OUT"
cat "$TMP/bad_token.json" >> "$OUT"; echo "" >> "$OUT"
echo "" >> "$OUT"

# 6) 带真实 token（接口还没实现，期望 404 = 已通过认证）
TOKEN=$(sed -n 's/.*"token":"\([^"]*\)".*/\1/p' "$TMP/login_admin.json")
echo "== 6) GET /api/permissions 带管理员 token（接口未实现，404 即认证通过）==" >> "$OUT"
c=$(curl.exe --max-time 10 -s -o "$TMPW/with_token.json" -w "%{http_code}" -H "Authorization: Bearer $TOKEN" "$BASE/api/permissions")
echo "http=$c" >> "$OUT"
head -c 200 "$TMP/with_token.json" >> "$OUT"; echo "" >> "$OUT"
echo "" >> "$OUT"

# 7) Swagger 放行
echo "== 7) Swagger 放行 ==" >> "$OUT"
for p in "/swagger-ui.html/" "/swagger-resources" "/v2/api-docs?group=Medical-API" "/webjars/springfox-swagger-ui/swagger-ui.css"; do
  c=$(curl.exe --max-time 10 -s -o /dev/null -w "%{http_code}" "$BASE$p")
  echo "  $p -> $c" >> "$OUT"
done
curl.exe --max-time 10 -s "$BASE/v2/api-docs?group=Medical-API" -o "$TMPW/v2.json"
echo "  v2/api-docs 字节数: $(wc -c < "$TMP/v2.json")" >> "$OUT"
echo "" >> "$OUT"

# 8) /image/** 放行（WebConfig 的资源映射在 T6 才加，这里只验证不被安全层拦成 10006）
echo "== 8) GET /image/not-exist.jpg（放行路径，期望 404 而不是 10006）==" >> "$OUT"
c=$(curl.exe --max-time 10 -s -o "$TMPW/img.json" -w "%{http_code}" "$BASE/image/not-exist.jpg")
echo "http=$c" >> "$OUT"
head -c 200 "$TMP/img.json" >> "$OUT"; echo "" >> "$OUT"
echo "" >> "$OUT"

# 9) token 载荷
echo "== 9) token 载荷解析 ==" >> "$OUT"
python3 "$PROJ/scripts/verify_t4_jwt.py" "$TOKEN" >> "$OUT" 2>&1
echo "" >> "$OUT"

# 10) 应用日志里的异常
echo "== 10) 日志异常检查 ==" >> "$OUT"
grep -c "ERROR" "$LOG" | sed 's/^/  ERROR 行数: /' >> "$OUT"
grep -E "Exception|ERROR" "$LOG" | grep -v "BadCredentials" | head -10 >> "$OUT"
echo "" >> "$OUT"

kill $APP_PID 2>/dev/null
sleep 2
echo "== 已停止应用 ==" >> "$OUT"
