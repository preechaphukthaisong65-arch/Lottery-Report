import unittest
from datetime import datetime, timezone
from lottery_verifier import build_report, classify_response, html_to_text

class VerifierTests(unittest.TestCase):
    def test_extracts_visible_text_without_scripts(self):
        text = html_to_text(b"<html><script>fake 123456</script><body><h1>Lottery</h1><p>Result</p></body></html>")
        self.assertIn("Lottery", text)
        self.assertIn("Result", text)
        self.assertNotIn("fake", text)
    def test_rejects_http_error(self):
        self.assertEqual(classify_response(503, b"x" * 500, "content"), "SOURCE_UNAVAILABLE")
    def test_marks_challenge_as_unavailable(self):
        self.assertEqual(classify_response(200, b"x" * 500, "Please verify you are human"), "SOURCE_UNAVAILABLE")
    def test_reachable_page_is_not_verified_result(self):
        self.assertEqual(classify_response(200, b"x" * 500, "visible page content"), "AVAILABLE")
        report = build_report("results", datetime(2026, 11, 11, 9, 0, tzinfo=timezone.utc))
        self.assertEqual(report["overall_status"], "INCOMPLETE")
        self.assertFalse(report["verification_enabled"])
        self.assertIn("validated date/result parsers", report["reason"])

if __name__ == "__main__":
    unittest.main()
