#!/usr/bin/env python3
"""Conservative source monitor. Reachability is not result verification."""
from __future__ import annotations
import argparse, hashlib, json, os, sys, urllib.error, urllib.request
from datetime import datetime, timezone
from html.parser import HTMLParser
from pathlib import Path
from typing import Any

ROOT = Path(__file__).resolve().parents[2]
OUTPUT = ROOT / "data" / "verification"
USER_AGENT = "Lottery-Report-VerificationBot/0.1 (+https://github.com/preechaphukthaisong65-arch/Lottery-Report)"
SOURCES = {
    "glo_results": ("results", "official", "https://www.glo.or.th/mission/reward-payment/check-reward"),
    "sanook": ("results", "secondary", "https://news.sanook.com/lotto/"),
    "thairath": ("results", "secondary", "https://en.thairath.co.th/lottery/latest-result"),
    "kapook": ("results", "secondary", "https://lottery.kapook.com/"),
    "glo_draw_listing": ("calendar", "official", "https://www.glo.or.th/mission/awarding/orderby-time"),
    "glo_award_faq": ("calendar", "official", "https://www.glo.or.th/service/faq/award"),
}
CHALLENGE_MARKERS = ("captcha", "verify you are human", "access denied", "cloudflare ray id", "checking your browser", "request blocked")

class TextExtractor(HTMLParser):
    def __init__(self):
        super().__init__(convert_charrefs=True)
        self.parts, self.skip_depth = [], 0
    def handle_starttag(self, tag, attrs):
        if tag in {"script", "style", "noscript", "svg"}: self.skip_depth += 1
    def handle_endtag(self, tag):
        if tag in {"script", "style", "noscript", "svg"} and self.skip_depth: self.skip_depth -= 1
    def handle_data(self, data):
        value = " ".join(data.split())
        if not self.skip_depth and value: self.parts.append(value)

def html_to_text(body: bytes) -> str:
    parser = TextExtractor()
    parser.feed(body.decode("utf-8", errors="replace"))
    return " ".join(parser.parts)

def classify_response(status_code: int, body: bytes, text: str) -> str:
    if status_code < 200 or status_code >= 300: return "SOURCE_UNAVAILABLE"
    if len(body) < 300 or not text.strip(): return "INCOMPLETE"
    if any(marker in text.lower() for marker in CHALLENGE_MARKERS): return "SOURCE_UNAVAILABLE"
    return "AVAILABLE"

def fetch_source(name: str, config: tuple[str, str, str], retrieved_at: str) -> dict[str, Any]:
    group, authority, url = config
    req = urllib.request.Request(url, headers={
        "User-Agent": USER_AGENT,
        "Accept": "text/html,application/xhtml+xml,application/json;q=0.9,*/*;q=0.8",
        "Accept-Language": "th-TH,th;q=0.9,en;q=0.7",
        "Cache-Control": "no-cache",
    })
    try:
        with urllib.request.urlopen(req, timeout=20) as response:
            body = response.read(4_000_001)
            code, final_url = response.status, response.geturl()
            content_type = response.headers.get("Content-Type", "")
        if len(body) > 4_000_000: raise ValueError("Response exceeded 4 MB safety limit")
        text = html_to_text(body)
        return {
            "name": name, "group": group, "authority": authority, "url": url,
            "final_url": final_url, "retrieved_at": retrieved_at,
            "status": classify_response(code, body, text), "http_status": code,
            "content_type": content_type, "sha256": hashlib.sha256(body).hexdigest(),
            "text_excerpt": text[:1200], "bytes_read": len(body),
            "parser_version": "source-probe-0.1.0",
        }
    except (urllib.error.URLError, TimeoutError, OSError, ValueError) as exc:
        return {
            "name": name, "group": group, "authority": authority, "url": url,
            "retrieved_at": retrieved_at, "status": "SOURCE_UNAVAILABLE",
            "error": f"{type(exc).__name__}: {str(exc)[:300]}",
            "parser_version": "source-probe-0.1.0",
        }

def build_report(mode: str, now: datetime | None = None) -> dict[str, Any]:
    current = now or datetime.now(timezone.utc)
    stamp = current.astimezone(timezone.utc).isoformat(timespec="seconds").replace("+00:00", "Z")
    checks = [fetch_source(name, cfg, stamp) for name, cfg in SOURCES.items() if cfg[0] == mode]
    unavailable = [x["name"] for x in checks if x["status"] == "SOURCE_UNAVAILABLE"]
    incomplete = [x["name"] for x in checks if x["status"] == "INCOMPLETE"]
    return {
        "schema_version": 1, "bot_version": "0.1.0", "mode": mode,
        "checked_at": stamp, "timezone": "UTC", "overall_status": "INCOMPLETE",
        "verification_enabled": False,
        "reason": "Reachability is monitored, but validated date/result parsers are not enabled. No data is approved or written into the app database.",
        "source_summary": {
            "total": len(checks), "available": sum(x["status"] == "AVAILABLE" for x in checks),
            "incomplete": len(incomplete), "unavailable": len(unavailable),
            "unavailable_sources": unavailable, "incomplete_sources": incomplete,
        },
        "checks": checks,
    }

def write_report(report: dict[str, Any]) -> Path:
    OUTPUT.mkdir(parents=True, exist_ok=True)
    encoded = json.dumps(report, ensure_ascii=False, indent=2) + "\n"
    latest = OUTPUT / f'{report["mode"]}-latest.json'
    latest.write_text(encoded, encoding="utf-8")
    history = OUTPUT / "history"
    history.mkdir(parents=True, exist_ok=True)
    stamp = report["checked_at"].replace(":", "").replace("-", "")
    (history / f'{report["mode"]}-{stamp}.json').write_text(encoded, encoding="utf-8")
    return latest

def main() -> int:
    parser = argparse.ArgumentParser()
    parser.add_argument("--mode", choices=("calendar", "results"), default="auto")
    mode = parser.parse_args().mode
    if mode == "auto": mode = os.environ.get("LOTTERY_CHECK_MODE", "calendar")
    report = build_report(mode)
    path = write_report(report)
    print(json.dumps({"mode": mode, "status": report["overall_status"], "available": report["source_summary"]["available"], "total": report["source_summary"]["total"], "output": str(path.relative_to(ROOT))}, ensure_ascii=False))
    return 0

if __name__ == "__main__": sys.exit(main())
