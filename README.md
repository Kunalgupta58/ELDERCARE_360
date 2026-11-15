# ELDERCARE 360

A working MVP for the ElderCare 360 platform with a Java Spring Boot backend, Vite React frontend, and local OCR microservice.
All application pages require authentication; new users can create an account from `/register`.

## Features
- Elderly/family/caregiver registration and login with JWT security
- Medication dashboard with spoken scheduled reminders and optional spoken dose confirmation
- Medical record vault with local file storage
- OCR integration endpoint for prescription processing
- Appointment scheduler
- Responsive UX for elderly care coordination

## Quick start

### 1. Start PostgreSQL (optional but recommended)

```bash
docker compose up -d db
```

Then run the backend in PostgreSQL mode:

```bash
cd backend
SPRING_PROFILES_ACTIVE=postgres mvn spring-boot:run
```

### 2. Start OCR service

```bash
cd ocr-service
python3 -m venv .venv
. .venv/bin/activate
pip install -r requirements.txt
uvicorn main:app --host 0.0.0.0 --port 8001
```

### 3. Start frontend

```bash
cd frontend
npm install
npm run dev -- --host 0.0.0.0
```

The frontend is available at http://localhost:5173 and the backend at http://localhost:8080.
During local development, Vite forwards `/api` requests to the backend. For a separately hosted frontend, set `VITE_API_BASE_URL` to the backend API URL.

## Demo credentials
- Email: elder@example.com
- Password: Password123

## Notes
- The default backend profile uses H2 for local development.
- The production profile uses PostgreSQL when `SPRING_PROFILES_ACTIVE=postgres` is enabled.
- The OCR microservice attempts local OCR processing; if a Tesseract binary is not installed in the current environment, it falls back to structured text extraction without inventing medical details.
- Voice reminders use the browser's speech synthesis and speech recognition APIs. Enable them from the Medication page; keep that page open, and allow microphone access for spoken confirmation. Browser support is required. Daily medication statuses reset to pending the next calendar day when records are loaded; other frequencies remain manual because their weekdays are not configured. A dose remains pending unless a clear voice or button confirmation is saved.
