import unittest
from unittest.mock import patch

from fastapi import HTTPException

from main import extract_text_from_image, parse_medications


class OcrServiceTests(unittest.TestCase):
    def test_parser_removes_instruction_prefix_from_detected_name(self):
        self.assertEqual(
            parse_medications("Take Aspirin 81mg daily"),
            [{"name": "Aspirin", "dosage": "81mg"}],
        )

    def test_image_ocr_reports_missing_tesseract(self):
        with patch("main.shutil.which", return_value=None):
            with self.assertRaises(HTTPException) as error:
                extract_text_from_image(b"image bytes")
        self.assertEqual(error.exception.status_code, 503)


if __name__ == "__main__":
    unittest.main()
