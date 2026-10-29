#!/usr/bin/env python3
"""Require an exact producer-to-consumer SHA-256 match for a raw plugin JAR."""

import argparse
import hashlib
import hmac
from pathlib import Path
import re


def verify_raw_jar_digest(jar_path: Path, expected_sha256: str) -> str:
    """Return the actual digest or reject missing/invalid/mismatched input."""
    if not re.fullmatch(r"[0-9a-f]{64}", expected_sha256):
        raise ValueError("Missing or malformed original raw JAR SHA-256")

    with jar_path.open("rb") as stream:
        actual_sha256 = hashlib.file_digest(stream, "sha256").hexdigest()
    if not hmac.compare_digest(actual_sha256, expected_sha256):
        raise ValueError(
            f"Raw JAR SHA-256 differs across build and download: "
            f"expected {expected_sha256}, received {actual_sha256}"
        )
    return actual_sha256


def main() -> int:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("jar", type=Path)
    parser.add_argument("expected_sha256")
    args = parser.parse_args()
    try:
        actual = verify_raw_jar_digest(args.jar, args.expected_sha256)
    except (ValueError, OSError) as exc:
        parser.exit(1, f"Raw JAR transfer verification failed: {exc}\n")
    print(f"Verified byte-identical raw JAR transfer: SHA-256 {actual}")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
