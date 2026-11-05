"""解析并校验 T4 签发的 JWT 载荷（不验签，只看 claim）。"""
import base64
import json
import sys


def b64url_decode(seg: str) -> bytes:
    seg += "=" * (-len(seg) % 4)
    return base64.urlsafe_b64decode(seg)


def main() -> int:
    token = sys.argv[1]
    parts = token.split(".")
    if len(parts) != 3:
        print(f"  token 段数异常: {len(parts)}")
        return 1
    header = json.loads(b64url_decode(parts[0]))
    payload = json.loads(b64url_decode(parts[1]))
    print(f"  header  = {json.dumps(header, ensure_ascii=False)}")
    print(f"  payload = {json.dumps(payload, ensure_ascii=False)}")
    ok = (
        header.get("alg") == "HS256"
        and payload.get("sub") == "medical-user"
        and payload.get("uname") == "admin_1"
        and payload.get("role") == "ROLE_1"
        and payload.get("id") is not None
        and payload.get("exp") is not None
    )
    print(f"  claim 校验: {'通过' if ok else '不通过'}")
    return 0 if ok else 1


if __name__ == "__main__":
    sys.exit(main())
