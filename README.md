# ELDERCARE 360

A working MVP for the ElderCare 360 platform with a Java Spring Boot backend, Vite React frontend, and local OCR microservice.
The landing page is public. Medication, records, appointments, and dashboard features require an account; new users can register at `/register`.

## Features
- Elderly/family/caregiver registration and login with JWT security
- Medication dashboard with independently tracked daily doses, browser voice reminders, and optional spoken confirmation
- Private medical-record uploads and owner-authorized downloads
- Local OCR review drafts from prescription or medicine-label photos/PDFs; detected values are never saved automatically
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

The OCR service needs the Tesseract executable in addition to its Python packages. The included Dockerfile installs it; for a local install, install `tesseract-ocr` through the operating system package manager first.

```bash
cd ocr-service
python3 -m venv .venv
. .venv/bin/activate
pip install -r requirements.txt
python -m unittest test_main.py
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
- The default backend profile uses a file-backed H2 database at `${user.home}/eldercare360`; accounts and records survive backend restarts.
- The production profile uses PostgreSQL when `SPRING_PROFILES_ACTIVE=postgres` is enabled.
- The OCR service reports an explicit error when Tesseract is unavailable. OCR results are editable form suggestions only and require user review before saving.
- Medical files are stored under `${user.home}/eldercare-files` by default, with private generated storage names and downloads restricted to the owning account. The upload limit is 10 MB.
- Voice reminders use browser speech synthesis while the authenticated website is open. The Medication page includes a test-voice button. Spoken replies require a browser with speech recognition and microphone permission; manual dose confirmation remains available.
- Daily dose times use the user's local browser time and can be added independently (for example, morning and evening). Each scheduled time has its own saved confirmation. Frequencies other than daily are tracked manually because weekdays are not configured.
