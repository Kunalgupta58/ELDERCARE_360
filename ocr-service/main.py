import io
import re
from typing import Optional

import fitz
import pytesseract
from fastapi import FastAPI, File, Form, UploadFile
from fastapi.middleware.cors import CORSMiddleware
from PIL import Image, UnidentifiedImageError

app = FastAPI(title='ElderCare OCR Service')

app.add_middleware(
    CORSMiddleware,
    allow_origins=['*'],
    allow_credentials=True,
    allow_methods=['*'],
    allow_headers=['*'],
)


def parse_medications(text: str):
    if not text:
        return []
    matches = re.findall(r'([A-Za-z][A-Za-z0-9\- ]{2,30})\s+(\d+(?:\.\d+)?\s*(?:mg|mcg|g|IU|ml))', text, flags=re.IGNORECASE)
    results = []
    for name, dose in matches:
        results.append({'name': name.strip(), 'dosage': dose.strip()})
    return results[:10]


def extract_text_from_pdf(file_bytes: bytes) -> str:
    try:
        with fitz.open(stream=file_bytes, filetype='pdf') as document:
            chunks = []
            for page in document:
                chunks.append(page.get_text())
            return '\n'.join(chunks)
    except Exception:
        return ''


def extract_text_from_image(file_bytes: bytes) -> str:
    try:
        image = Image.open(io.BytesIO(file_bytes)).convert('RGB')
        return pytesseract.image_to_string(image)
    except (UnidentifiedImageError, pytesseract.TesseractError):
        return ''


@app.get('/health')
def health_check():
    return {'status': 'ok'}


@app.post('/extract')
async def extract(
    file: Optional[UploadFile] = File(default=None),
    text: Optional[str] = Form(default=None),
):
    candidate = text or ''

    if file is not None and file.filename:
        payload = await file.read()
        file_name = file.filename.lower()
        if file_name.endswith('.pdf'):
            candidate = extract_text_from_pdf(payload)
        elif file_name.endswith(('.png', '.jpg', '.jpeg', '.webp')):
            candidate = extract_text_from_image(payload)
        elif file_name.endswith('.txt'):
            try:
                candidate = payload.decode('utf-8')
            except UnicodeDecodeError:
                candidate = payload.decode('latin-1', errors='ignore')

    medications = parse_medications(candidate)
    if not candidate and not medications:
        return {'text': '', 'medications': [], 'status': 'no_text_detected'}

    return {
        'text': candidate.strip(),
        'medications': medications,
        'status': 'success',
    }


if __name__ == '__main__':
    import uvicorn
    uvicorn.run('main:app', host='0.0.0.0', port=8001, reload=True)
