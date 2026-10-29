"""Regression tests for cross-job byte-for-byte raw JAR delivery."""

import hashlib
from pathlib import Path
import subprocess
import sys
import tempfile
import unittest

from verify_raw_jar_digest import verify_raw_jar_digest

CHECKER = Path(__file__).with_name("verify_raw_jar_digest.py")


class RawJarDigestTest(unittest.TestCase):
    def setUp(self):
        self.temp = tempfile.TemporaryDirectory()
        self.addCleanup(self.temp.cleanup)
        self.jar = Path(self.temp.name) / "SF_MobCapturer1.0.7.jar"
        self.jar.write_bytes(b"PK\\x03\\x04test-candidate-bytes")
        self.original = self.jar.read_bytes()
        self.expected = hashlib.sha256(self.original).hexdigest()

    def cli(self, expected):
        return subprocess.run(
            [sys.executable, str(CHECKER), str(self.jar), expected],
            capture_output=True, text=True, timeout=10, check=False,
        )

    def test_exact_download_passes_without_rewriting_artifact(self):
        self.assertEqual(self.expected, verify_raw_jar_digest(self.jar, self.expected))
        result = self.cli(self.expected)
        self.assertEqual(0, result.returncode, result.stderr)
        self.assertIn("byte-identical", result.stdout)
        self.assertEqual(self.original, self.jar.read_bytes())

    def test_single_byte_changed_download_is_rejected(self):
        self.jar.write_bytes(self.original + b"x")
        with self.assertRaisesRegex(ValueError, "differs across"):
            verify_raw_jar_digest(self.jar, self.expected)
        result = self.cli(self.expected)
        self.assertNotEqual(0, result.returncode)
        self.assertIn("differs across", result.stderr)
        self.assertEqual(self.original + b"x", self.jar.read_bytes())

    def test_different_valid_digest_rejected(self):
        wrong = hashlib.sha256(b"other artifact").hexdigest()
        self.assertNotEqual(0, self.cli(wrong).returncode)

    def test_missing_or_malformed_build_output_fails_closed(self):
        for digest in ("", "0" * 63, "not-a-sha256", self.expected.upper()):
            with self.subTest(digest=digest):
                with self.assertRaisesRegex(ValueError, "Missing or malformed"):
                    verify_raw_jar_digest(self.jar, digest)
                self.assertNotEqual(0, self.cli(digest).returncode)

    def test_missing_download_does_not_pass(self):
        self.jar.unlink()
        self.assertNotEqual(0, self.cli(self.expected).returncode)


if __name__ == "__main__":
    unittest.main()
