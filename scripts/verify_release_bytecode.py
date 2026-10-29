#!/usr/bin/env python3
"""Reject distributable MobCapturer JARs containing bytecode newer than Java 21."""

from pathlib import Path
import sys
from zipfile import BadZipFile, ZipFile

MAX_CLASS_MAJOR = 65  # Java 21
MAIN_CLASS = "io/github/thebusybiscuit/mobcapturer/MobCapturer.class"


def verify(jar_path: Path) -> int:
    """Return checked class count; raise ValueError on an unsafe package."""
    seen = set()
    checked = 0
    with ZipFile(jar_path) as jar:
        for entry in jar.infolist():
            name = entry.filename
            if entry.is_dir():
                continue
            if name in seen:
                raise ValueError(f"Duplicate JAR entry: {name}")
            seen.add(name)
            if not name.endswith(".class"):
                continue

            header = jar.read(entry)[:8]
            if len(header) != 8 or header[:4] != bytes.fromhex("cafebabe"):
                raise ValueError(f"Invalid class header: {name}")
            major = int.from_bytes(header[6:8], byteorder="big")
            if major > MAX_CLASS_MAJOR:
                raise ValueError(f"Java {major} class version exceeds Java 21 ({MAX_CLASS_MAJOR}): {name}")
            checked += 1

    if MAIN_CLASS not in seen or "plugin.yml" not in seen:
        raise ValueError("JAR is missing the MobCapturer entry point or plugin.yml")
    if checked == 0:
        raise ValueError("JAR contains no Java classes")
    return checked


def main() -> int:
    if len(sys.argv) != 2:
        print("Usage: verify_release_bytecode.py <raw-plugin.jar>", file=sys.stderr)
        return 2
    try:
        count = verify(Path(sys.argv[1]))
    except (BadZipFile, ValueError, OSError) as exc:
        print(f"Release bytecode validation failed: {exc}", file=sys.stderr)
        return 1
    print(f"Validated {count} class files: all Java 21 compatible, unique JAR entries")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
