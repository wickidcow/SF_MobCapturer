"""Regression coverage for the Java 21 distributable-class ceiling."""

from pathlib import Path
import tempfile
import unittest
import warnings
from zipfile import ZipFile

from verify_release_bytecode import MAIN_CLASS, verify


def class_header(major: int) -> bytes:
    return bytes.fromhex("cafebabe0000") + major.to_bytes(2, "big")


class ReleaseBytecodeTest(unittest.TestCase):
    def setUp(self):
        self.temp = tempfile.TemporaryDirectory()
        self.addCleanup(self.temp.cleanup)
        self.jar = Path(self.temp.name) / "MobCapturer.jar"

    def write(self, *, main=class_header(65), other=(), include_plugin=True, duplicate=False):
        with warnings.catch_warnings():
            warnings.simplefilter("ignore", UserWarning)
            with ZipFile(self.jar, "w") as z:
                if include_plugin:
                    z.writestr("plugin.yml", "name: MobCapturer\\n")
                z.writestr(MAIN_CLASS, main)
                for name, data in other:
                    z.writestr(name, data)
                if duplicate:
                    z.writestr(MAIN_CLASS, main)

    def test_accepts_java21_and_older_shaded_classes(self):
        self.write(other=(("pkg/Older.class", class_header(49)),))
        self.assertEqual(2, verify(self.jar))

    def test_rejects_newer_shaded_class_even_when_entry_point_is_java21(self):
        self.write(other=(("pkg/Newer.class", class_header(66)),))
        with self.assertRaisesRegex(ValueError, r"exceeds Java 21"):
            verify(self.jar)

    def test_rejects_malformed_class_and_duplicate_entries(self):
        self.write(main=b"broken")
        with self.assertRaisesRegex(ValueError, "Invalid class header"):
            verify(self.jar)

        self.write(duplicate=True)
        with self.assertRaisesRegex(ValueError, "Duplicate JAR entry"):
            verify(self.jar)

    def test_requires_plugin_manifest(self):
        self.write(include_plugin=False)
        with self.assertRaisesRegex(ValueError, "missing"):
            verify(self.jar)


if __name__ == "__main__":
    unittest.main()
