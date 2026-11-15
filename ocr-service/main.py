import io
import re
import shutil
from typing import Optional

import fitz
import pytesseract
from fastapi import FastAPI, File, Form, HTTPException, UploadFile
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
        cleaned_name = re.sub(
            r'^(?:(?:take|medication|medicine|tablet|tab|capsule|prescription)\s+)+',
            '',
            name.strip(),
            flags=re.IGNORECASE,
        )
        results.append({'name': cleaned_name, 'dosage': dose.strip()})
    return results[:10]


def extract_text_from_pdf(file_bytes: bytes) -> str:
    try:
        with fitz.open(stream=file_bytes, filetype='pdf') as document:
            text_chunks = []
            for page in document:
                text_chunks.append(page.get_text())
            text = '\n'.join(text_chunks).strip()
            if text:
                return text
            require_tesseract()
            image_chunks = []
            for page in document:
                pixmap = page.get_pixmap(matrix=fitz.Matrix(2, 2), alpha=False)
                image = Image.open(io.BytesIO(pixmap.tobytes('png'))).convert('RGB')
                image_chunks.append(pytesseract.image_to_string(image))
            return '\n'.join(image_chunks)
    except fitz.FileDataError as error:
        raise HTTPException(status_code=422, detail='The uploaded PDF could not be read') from error


def extract_text_from_image(file_bytes: bytes) -> str:
    require_tesseract()
    try:
        image = Image.open(io.BytesIO(file_bytes)).convert('RGB')
        return pytesseract.image_to_string(image)
    except UnidentifiedImageError as error:
        raise HTTPException(status_code=422, detail='The uploaded image could not be read') from error
    except pytesseract.TesseractError as error:
        raise HTTPException(status_code=422, detail='Text could not be read from this image') from error


def require_tesseract():
    if not shutil.which('tesseract'):
        raise HTTPException(status_code=503, detail='Tesseract OCR is not installed in the OCR service')


@app.get('/health')
def health_check():
    return {'status': 'ok', 'tesseract_available': bool(shutil.which('tesseract'))}


@app.post('/extract')
async def extract(
    file: Optional[UploadFile] = File(default=None),
    text: Optional[str] = Form(default=None),
):
    candidate = text or ''

    if file is not None and file.filename:
        payload = await file.read()
        if not payload:
            raise HTTPException(status_code=400, detail='The uploaded file is empty')
        file_name = file.filename.lower()
        if file_name.endswith('.pdf'):
            candidate = extract_text_from_pdf(payload)
        elif file_name.endswith(('.png', '.jpg', '.jpeg', '.webp')):
            candidate = extract_text_from_image(payload)
        else:
            raise HTTPException(status_code=415, detail='Use a PDF, PNG, JPG, or WEBP file')

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
