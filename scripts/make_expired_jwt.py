"""生成一个已过期的 JWT（与 JwtUtil 同算法同密钥），用于验证 10006 过期分支。"""
import base64
import hashlib
import hmac
import json
import sys
import time

SECRET = b"huiyi-medical-jwt-secret-key-2026-zhangwei"


def b64url(raw: bytes) -> str:
    return base64.urlsafe_b64encode(raw).rstrip(b"=").decode()


def main() -> int:
    now = int(time.time())
    header = {"typ": "JWT", "alg": "HS256"}
    payload = {
        "sub": "medical-user",
        "iat": now - 7200,
        "exp": now - 3600,  # 一小时前就过期了
        "id": 1,
        "uname": "admin_1",
        "role": "ROLE_1",
    }
    signing_input = f"{b64url(json.dumps(header, separators=(',', ':')).encode())}." \
                    f"{b64url(json.dumps(payload, separators=(',', ':')).encode())}"
    signature = hmac.new(SECRET, signing_input.encode(), hashlib.sha256).digest()
    print(f"{signing_input}.{b64url(signature)}")
    return 0


if __name__ == "__main__":
    sys.exit(main())
