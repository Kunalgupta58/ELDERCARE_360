import { useCallback, useEffect, useMemo, useRef, useState } from 'react';
import { BrowserRouter, Link, NavLink, Route, Routes, useNavigate } from 'react-router-dom';
import {
  Activity,
  ArrowRight,
  BellRing,
  CalendarDays,
  FileText,
  HeartPulse,
  Lock,
  LogOut,
  Mic,
  Pill,
  ShieldCheck,
  Sparkles,
  Stethoscope,
  UserRound,
} from 'lucide-react';
import { Area, AreaChart, CartesianGrid, ResponsiveContainer, Tooltip, XAxis, YAxis } from 'recharts';
import axios from 'axios';

const API_BASE = 'http://localhost:8080/api';
const fallbackUser = {
  fullName: 'Eleanor Brooks',
  email: 'elder@example.com',
  role: 'ELDERLY',
};

const fallbackDashboard = {
  medicationCount: 4,
  appointmentCount: 2,
  recordCount: 3,
  nextMedication: 'Aspirin at 08:00 AM',
  nextAppointment: 'Cardiology Review on 2026-11-22T10:30:00Z',
  completionRate: 86,
};

const fallbackMedications = [
  { id: 1, name: 'Aspirin', dosage: '81mg', scheduleTime: '08:00 AM', frequency: 'Daily', status: 'PENDING', notes: 'After breakfast' },
  { id: 2, name: 'Vitamin D', dosage: '1000 IU', scheduleTime: '06:30 PM', frequency: 'Daily', status: 'TAKEN', notes: 'With dinner' },
  { id: 3, name: 'Omega-3', dosage: '1 capsule', scheduleTime: '07:00 PM', frequency: 'Twice a week', status: 'PENDING', notes: 'Take with food' },
];

const fallbackAppointments = [
  { id: 1, title: 'Cardiology Review', doctorName: 'Dr. Patel', appointmentTime: '2026-11-22T10:30:00Z', status: 'SCHEDULED', location: 'City Clinic, Room 2' },
  { id: 2, title: 'Care Plan Check-In', doctorName: 'Nurse Linda', appointmentTime: '2026-11-28T13:00:00Z', status: 'SCHEDULED', location: 'Home Care Team' },
];

const fallbackRecords = [
  { id: 1, title: 'Annual Checkup', documentType: 'Prescription', summary: 'Routine blood pressure and vitamin report.', extractedText: 'Medication: Aspirin 81mg daily.' },
  { id: 2, title: 'Recovery Notes', documentType: 'Report', summary: 'Mobility assessment after PT sessions.', extractedText: 'Mobility improved; continue stretching routines.' },
];

const chartData = [
  { name: 'Mon', meds: 4 },
  { name: 'Tue', meds: 3 },
  { name: 'Wed', meds: 5 },
  { name: 'Thu', meds: 4 },
  { name: 'Fri', meds: 6 },
  { name: 'Sat', meds: 5 },
  { name: 'Sun', meds: 4 },
];

function stripBearer(token) {
  if (!token) return token;
  return token.startsWith('Bearer ') ? token.slice(7) : token;
}

function formatDateTime(value) {
  if (!value) return 'No date';
  try {
    return new Date(value).toLocaleString([], { dateStyle: 'medium', timeStyle: 'short' });
  } catch {
    return value;
  }
}

function App() {
  return (
    <BrowserRouter>
      <AppLayout />
    </BrowserRouter>
  );
}

function AppLayout() {
  const [token, setToken] = useState(() => localStorage.getItem('eldercare-token'));
  const [user, setUser] = useState(null);
  const [dashboard, setDashboard] = useState(fallbackDashboard);
  const [medications, setMedications] = useState(fallbackMedications);
  const [appointments, setAppointments] = useState(fallbackAppointments);
  const [records, setRecords] = useState(fallbackRecords);

  useEffect(() => {
    if (!token) {
      return;
    }

    axios
      .get(`${API_BASE}/auth/me`, { headers: { Authorization: `Bearer ${stripBearer(token)}` } })
      .then((response) => setUser(response.data))
      .catch(() => setUser(fallbackUser));

    Promise.all([
      axios.get(`${API_BASE}/dashboard`, { headers: { Authorization: `Bearer ${stripBearer(token)}` } }).catch(() => ({ data: fallbackDashboard })),
      axios.get(`${API_BASE}/medications`, { headers: { Authorization: `Bearer ${stripBearer(token)}` } }).catch(() => ({ data: fallbackMedications })),
      axios.get(`${API_BASE}/appointments`, { headers: { Authorization: `Bearer ${stripBearer(token)}` } }).catch(() => ({ data: fallbackAppointments })),
      axios.get(`${API_BASE}/records`, { headers: { Authorization: `Bearer ${stripBearer(token)}` } }).catch(() => ({ data: fallbackRecords })),
    ])
      .then(([dashboardRes, medsRes, apptsRes, recordsRes]) => {
        setDashboard(dashboardRes.data);
        setMedications(medsRes.data);
        setAppointments(apptsRes.data);
        setRecords(recordsRes.data);
      })
      .catch(() => {
        setDashboard(fallbackDashboard);
        setMedications(fallbackMedications);
        setAppointments(fallbackAppointments);
        setRecords(fallbackRecords);
      });
  }, [token]);

  const handleLogin = async (payload) => {
    try {
      const response = await axios.post(`${API_BASE}/auth/login`, payload);
      const jwt = response.data.token;
      localStorage.setItem('eldercare-token', jwt);
      setToken(jwt);
      return true;
    } catch {
      const demoToken = 'demo-token';
      localStorage.setItem('eldercare-token', demoToken);
      setToken(demoToken);
      setUser(fallbackUser);
      return true;
    }
  };

  const handleRegister = async (payload) => {
    try {
      const response = await axios.post(`${API_BASE}/auth/register`, payload);
      localStorage.setItem('eldercare-token', response.data.token);
      setToken(response.data.token);
      return true;
    } catch {
      localStorage.setItem('eldercare-token', 'demo-token');
      setToken('demo-token');
      setUser(fallbackUser);
      return true;
    }
  };

  const handleLogout = () => {
    localStorage.removeItem('eldercare-token');
    setToken(null);
    setUser(null);
  };

  return (
    <div className="min-h-screen bg-slate-50 text-slate-800">
      <header className="border-b border-slate-200 bg-white/85 backdrop-blur-sm">
        <div className="mx-auto flex max-w-7xl items-center justify-between px-6 py-4">
          <Link to="/" className="flex items-center gap-3 text-lg font-semibold text-slate-900">
            <div className="flex h-10 w-10 items-center justify-center rounded-xl bg-brand-100 text-brand-700">
              <HeartPulse className="h-5 w-5" />
            </div>
            ElderCare 360
          </Link>
          <nav className="hidden items-center gap-2 md:flex">
            <NavLink className="nav-link" to="/">Home</NavLink>
            <NavLink className="nav-link" to="/dashboard">Dashboard</NavLink>
            <NavLink className="nav-link" to="/medications">Medication</NavLink>
            <NavLink className="nav-link" to="/records">Medical Vault</NavLink>
            <NavLink className="nav-link" to="/appointments">Appointments</NavLink>
          </nav>
          {user ? (
            <div className="flex items-center gap-3">
              <div className="hidden rounded-full bg-slate-100 px-3 py-2 text-sm font-medium text-slate-700 sm:block">
                {user.fullName}
              </div>
              <button onClick={handleLogout} className="inline-flex items-center gap-2 rounded-full border border-slate-200 bg-white px-4 py-2 text-sm font-medium text-slate-700 transition hover:border-slate-300 hover:bg-slate-50">
                <LogOut className="h-4 w-4" />
                Sign out
              </button>
            </div>
          ) : (
            <Link to="/login" className="inline-flex items-center gap-2 rounded-full bg-brand-600 px-4 py-2 text-sm font-semibold text-white shadow-sm transition hover:bg-brand-700">
              <Lock className="h-4 w-4" />
              Login
            </Link>
          )}
        </div>
      </header>

      <Routes>
        <Route path="/" element={<LandingPage />} />
        <Route path="/login" element={<AuthPage onLogin={handleLogin} onRegister={handleRegister} user={user} />} />
        <Route path="/dashboard" element={<DashboardPage user={user} dashboard={dashboard} records={records} />} />
        <Route path="/medications" element={<MedicationPage medications={medications} setMedications={setMedications} token={token} />} />
        <Route path="/records" element={<RecordsPage records={records} setRecords={setRecords} />} />
        <Route path="/appointments" element={<AppointmentsPage appointments={appointments} setAppointments={setAppointments} />} />
      </Routes>
    </div>
  );
}

function LandingPage() {
  return (
    <main>
      <section className="mx-auto max-w-7xl px-6 py-20">
        <div className="grid items-center gap-12 lg:grid-cols-[1.2fr_0.8fr]">
          <div>
            <div className="mb-6 inline-flex items-center gap-2 rounded-full border border-brand-200 bg-brand-50 px-3 py-1 text-sm font-medium text-brand-700">
              <Sparkles className="h-4 w-4" />
              Care coordination for elder wellbeing
            </div>
            <h1 className="max-w-xl text-5xl font-extrabold tracking-tight text-slate-900 sm:text-6xl">
              Smarter routines for safer, calmer living.
            </h1>
            <p className="mt-6 max-w-xl text-lg text-slate-600">
              ElderCare 360 centralizes medication reminders, health records, appointments and family updates in one accessible, secure dashboard built for older adults and caregivers.
            </p>
            <div className="mt-8 flex flex-wrap gap-4">
              <Link to="/login" className="inline-flex items-center gap-2 rounded-full bg-brand-600 px-6 py-3 font-semibold text-white shadow-sm transition hover:bg-brand-700">
                Get started
                <ArrowRight className="h-4 w-4" />
              </Link>
              <a href="#problems" className="inline-flex items-center gap-2 rounded-full border border-slate-200 bg-white px-6 py-3 font-semibold text-slate-700 transition hover:border-slate-300 hover:bg-slate-50">
                Problems we solve
              </a>
            </div>
            <div className="mt-10 grid max-w-lg gap-4 sm:grid-cols-3">
              <StatItem label="Medication reminders" value="24/7" />
              <StatItem label="Doctors tracked" value="12+" />
              <StatItem label="Care updates" value="Live" />
            </div>
          </div>

          <div className="card p-6">
            <div className="mb-5 flex items-center justify-between">
              <div>
                <div className="text-sm font-medium text-slate-500">Today’s care snapshot</div>
                <div className="mt-1 text-2xl font-bold text-slate-900">Eleanor Brooks</div>
              </div>
              <div className="rounded-full bg-emerald-100 px-3 py-1 text-xs font-semibold uppercase tracking-[0.14em] text-emerald-700">
                Safe mode
              </div>
            </div>

            <div className="space-y-4">
              <MiniCard icon={<Pill className="h-5 w-5" />} title="Aspirin" detail="81mg · 08:00 AM" status="Due soon" tone="amber" />
              <MiniCard icon={<CalendarDays className="h-5 w-5" />} title="Cardiology Review" detail="Nov 22 · 10:30 AM" status="Scheduled" tone="sky" />
              <MiniCard icon={<FileText className="h-5 w-5" />} title="Annual Checkup" detail="Prescription uploaded" status="Ready" tone="emerald" />
            </div>
          </div>
        </div>
      </section>

      <section id="problems" className="mx-auto max-w-7xl px-6 pb-20">
        <h2 className="section-title text-center">Problems We Solve</h2>
        <div className="mt-10 grid gap-6 md:grid-cols-2 xl:grid-cols-4">
          <SolutionCard title="Missed medicines" body="Daily reminders and caregiver confirmation reduce missed doses and support more consistent routines." icon={<BellRing className="h-5 w-5" />} />
          <SolutionCard title="Disconnected records" body="Medical files, prescriptions and notes live in one secure vault for quick retrieval by family or clinical staff." icon={<ShieldCheck className="h-5 w-5" />} />
          <SolutionCard title="Care coordination" body="Family members and caregivers see task updates, alerts and schedules without needing to chase updates manually." icon={<UserRound className="h-5 w-5" />} />
          <SolutionCard title="Appointment gaps" body="Easily track upcoming visits and ensure follow-ups are not forgotten or delayed." icon={<Stethoscope className="h-5 w-5" />} />
        </div>
      </section>
    </main>
  );
}

function AuthPage({ onLogin, onRegister, user }) {
  const [mode, setMode] = useState('login');
  const [form, setForm] = useState({ fullName: 'Eleanor Brooks', email: 'elder@example.com', phone: '+1 555 0184', password: 'Password123', role: 'ELDERLY' });
  const navigate = useNavigate();

  const handleChange = (e) => {
    setForm((current) => ({ ...current, [e.target.name]: e.target.value }));
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (mode === 'login') {
      await onLogin({ email: form.email, password: form.password });
    } else {
      await onRegister({ ...form });
    }
    navigate('/dashboard');
  };

  if (user) {
    return (
      <main className="mx-auto max-w-4xl px-6 py-20">
        <div className="card text-center">
          <div className="mx-auto mb-4 flex h-16 w-16 items-center justify-center rounded-full bg-brand-100 text-brand-700">
            <UserRound className="h-8 w-8" />
          </div>
          <h2 className="text-3xl font-bold text-slate-900">Welcome back, {user.fullName}</h2>
          <p className="mt-3 text-slate-600">Your account is active and ready to support your care routine.</p>
          <Link to="/dashboard" className="mt-6 inline-flex items-center gap-2 rounded-full bg-brand-600 px-6 py-3 font-semibold text-white">
            Open dashboard
            <ArrowRight className="h-4 w-4" />
          </Link>
        </div>
      </main>
    );
  }

  return (
    <main className="mx-auto max-w-5xl px-6 py-20">
      <div className="grid gap-8 lg:grid-cols-[0.9fr_1.1fr]">
        <div className="card">
          <div className="mb-6 inline-flex items-center gap-2 rounded-full bg-brand-50 px-3 py-1 text-sm font-semibold text-brand-700">
            <ShieldCheck className="h-4 w-4" />
            Secure access
          </div>
          <h2 className="text-3xl font-bold text-slate-900">Access your care plan</h2>
          <ul className="mt-6 space-y-4 text-slate-600">
            <li>• Medication reminders and confirmation tasks</li>
            <li>• Family and caregiver coordination</li>
            <li>• Secure medical vault and appointment history</li>
          </ul>
        </div>

        <div className="card">
          <div className="mb-6 flex rounded-full bg-slate-100 p-1">
            <button onClick={() => setMode('login')} className={`flex-1 rounded-full px-4 py-2 text-sm font-semibold ${mode === 'login' ? 'bg-white text-slate-900 shadow-sm' : 'text-slate-500'}`}>
              Login
            </button>
            <button onClick={() => setMode('register')} className={`flex-1 rounded-full px-4 py-2 text-sm font-semibold ${mode === 'register' ? 'bg-white text-slate-900 shadow-sm' : 'text-slate-500'}`}>
              Register
            </button>
          </div>
          <form className="space-y-4" onSubmit={handleSubmit}>
            {mode === 'register' && (
              <div>
                <label className="mb-1 block text-sm font-medium text-slate-700">Full name</label>
                <input name="fullName" value={form.fullName} onChange={handleChange} className="w-full rounded-xl border border-slate-200 bg-slate-50 p-3 outline-none focus:border-brand-500" />
              </div>
            )}
            <div>
              <label className="mb-1 block text-sm font-medium text-slate-700">Email address</label>
              <input type="email" name="email" value={form.email} onChange={handleChange} className="w-full rounded-xl border border-slate-200 bg-slate-50 p-3 outline-none focus:border-brand-500" />
            </div>
            {mode === 'register' && (
              <>
                <div>
                  <label className="mb-1 block text-sm font-medium text-slate-700">Phone</label>
                  <input name="phone" value={form.phone} onChange={handleChange} className="w-full rounded-xl border border-slate-200 bg-slate-50 p-3 outline-none focus:border-brand-500" />
                </div>
                <div>
                  <label className="mb-1 block text-sm font-medium text-slate-700">Role</label>
                  <select name="role" value={form.role} onChange={handleChange} className="w-full rounded-xl border border-slate-200 bg-slate-50 p-3 outline-none focus:border-brand-500">
                    <option value="ELDERLY">Elderly</option>
                    <option value="FAMILY_MEMBER">Family member</option>
                    <option value="CAREGIVER">Caregiver</option>
                  </select>
                </div>
              </>
            )}
            <div>
              <label className="mb-1 block text-sm font-medium text-slate-700">Password</label>
              <input type="password" name="password" value={form.password} onChange={handleChange} className="w-full rounded-xl border border-slate-200 bg-slate-50 p-3 outline-none focus:border-brand-500" />
            </div>
            <button type="submit" className="w-full rounded-xl bg-brand-600 px-4 py-3 font-semibold text-white transition hover:bg-brand-700">
              {mode === 'login' ? 'Sign in' : 'Create account'}
            </button>
          </form>
        </div>
      </div>
    </main>
  );
}

function DashboardPage({ user, dashboard, records }) {
  const summary = useMemo(() => [
    { label: 'Medications', value: dashboard.medicationCount, icon: <Pill className="h-5 w-5" /> },
    { label: 'Appointments', value: dashboard.appointmentCount, icon: <CalendarDays className="h-5 w-5" /> },
    { label: 'Records', value: dashboard.recordCount, icon: <FileText className="h-5 w-5" /> },
    { label: 'Completion', value: `${dashboard.completionRate}%`, icon: <Activity className="h-5 w-5" /> },
  ], [dashboard]);

  return (
    <main className="mx-auto max-w-7xl space-y-8 px-6 py-10">
      <div className="flex items-center justify-between gap-4">
        <div>
          <p className="text-sm font-medium uppercase tracking-[0.16em] text-brand-700">Care dashboard</p>
          <h2 className="mt-2 text-3xl font-bold text-slate-900">Hello, {user?.fullName || 'Eleanor Brooks'}</h2>
        </div>
        <div className="rounded-full border border-emerald-200 bg-emerald-50 px-4 py-2 text-sm font-medium text-emerald-700">
          Care status: stable
        </div>
      </div>

      <div className="grid gap-4 md:grid-cols-2 xl:grid-cols-4">
        {summary.map((item) => (
          <div key={item.label} className="metric-card">
            <div className="mb-4 flex h-10 w-10 items-center justify-center rounded-xl bg-brand-50 text-brand-700">
              {item.icon}
            </div>
            <div className="text-3xl font-bold text-slate-900">{item.value}</div>
            <div className="text-sm text-slate-500">{item.label}</div>
          </div>
        ))}
      </div>

      <div className="grid gap-6 xl:grid-cols-[1.2fr_0.8fr]">
        <div className="card p-6">
          <div className="mb-4 flex items-center justify-between">
            <h3 className="text-xl font-semibold text-slate-900">Medication adherence</h3>
            <span className="text-sm text-slate-500">Last 7 days</span>
          </div>
          <div className="h-72">
            <ResponsiveContainer width="100%" height="100%">
              <AreaChart data={chartData} margin={{ top: 10, right: 10, left: 0, bottom: 0 }}>
                <defs>
                  <linearGradient id="colorMeds" x1="0" x2="0" y1="0" y2="1">
                    <stop offset="5%" stopColor="#1ba98a" stopOpacity={0.6} />
                    <stop offset="95%" stopColor="#1ba98a" stopOpacity={0.05} />
                  </linearGradient>
                </defs>
                <CartesianGrid strokeDasharray="3 3" stroke="#e2e8f0" />
                <XAxis dataKey="name" stroke="#64748b" />
                <YAxis stroke="#64748b" />
                <Tooltip />
                <Area type="monotone" dataKey="meds" stroke="#1ba98a" fill="url(#colorMeds)" strokeWidth={3} />
              </AreaChart>
            </ResponsiveContainer>
          </div>
        </div>

        <div className="card p-6">
          <h3 className="text-xl font-semibold text-slate-900">Care priorities</h3>
          <div className="mt-6 space-y-4">
            <PriorityRow title="Next medication" detail={dashboard.nextMedication} />
            <PriorityRow title="Next appointment" detail={dashboard.nextAppointment} />
            <PriorityRow title="Medical vault" detail={`${records.length} files ready`} />
          </div>
        </div>
      </div>
    </main>
  );
}

function MedicationPage({ medications, setMedications, token }) {
  const [form, setForm] = useState({ name: '', dosage: '', scheduleTime: '', frequency: '', notes: '' });
  const [voiceEnabled, setVoiceEnabled] = useState(false);
  const [voiceStatus, setVoiceStatus] = useState('Voice reminders are off.');
  const [isListening, setIsListening] = useState(false);
  const [statusError, setStatusError] = useState('');
  const announcedReminders = useRef(new Set());

  const speakMessage = useCallback((message) => {
    if (!('speechSynthesis' in window) || typeof SpeechSynthesisUtterance === 'undefined') {
      setVoiceStatus('Spoken alerts are not supported in this browser.');
      return;
    }
    window.speechSynthesis.cancel();
    window.speechSynthesis.speak(new SpeechSynthesisUtterance(message));
  }, []);

  const saveMedicationStatus = useCallback(async (medication, status) => {
    setStatusError('');
    if (token && token !== 'demo-token') {
      try {
        await axios.patch(
          `${API_BASE}/medications/${medication.id}/status`,
          null,
          {
            params: { status },
            headers: { Authorization: `Bearer ${stripBearer(token)}` },
          },
        );
      } catch {
        setStatusError('Could not save this confirmation. The dose is still marked pending.');
        setVoiceStatus(`${medication.name} confirmation could not be saved; the dose remains pending.`);
        return false;
      }
    }

    setMedications((current) => current.map((item) => (
      item.id === medication.id ? { ...item, status } : item
    )));
    setVoiceStatus(`${medication.name} marked as taken.`);
    return true;
  }, [setMedications, token]);

  const askForVoiceConfirmation = useCallback((medication) => {
    const Recognition = window.SpeechRecognition || window.webkitSpeechRecognition;
    if (!Recognition) {
      setVoiceStatus('Voice recognition is not supported in this browser. Use the Mark taken button to confirm.');
      return;
    }

    const recognition = new Recognition();
    recognition.lang = 'en-US';
    recognition.continuous = false;
    recognition.interimResults = false;
    let receivedResult = false;
    setIsListening(true);
    setVoiceStatus(`Listening for confirmation for ${medication.name}...`);

    recognition.onresult = async (event) => {
      receivedResult = true;
      const transcript = event.results?.[0]?.[0]?.transcript?.trim().toLowerCase() || '';
      const saysNotTaken = /\b(not|no|haven't|have not|didn't|did not|yet)\b/.test(transcript);
      const confirmsTaken = !saysNotTaken && /\b(yes|yeah|yep|taken|took|done)\b/.test(transcript);

      if (confirmsTaken) {
        const saved = await saveMedicationStatus(medication, 'TAKEN');
        if (saved) {
          speakMessage(`Thank you. ${medication.name} is marked as taken.`);
        } else {
          speakMessage(`I could not save the confirmation. ${medication.name} remains pending.`);
        }
      } else {
        setVoiceStatus(`No clear confirmation heard. ${medication.name} remains pending.`);
        speakMessage(`I did not hear a clear confirmation. ${medication.name} remains pending.`);
      }
    };

    recognition.onerror = () => {
      setVoiceStatus(`Could not hear a confirmation. ${medication.name} remains pending.`);
    };
    recognition.onend = () => {
      setIsListening(false);
      if (!receivedResult) {
        setVoiceStatus(`No confirmation heard. ${medication.name} remains pending.`);
      }
    };

    try {
      recognition.start();
    } catch {
      setIsListening(false);
      setVoiceStatus(`Could not start voice recognition. ${medication.name} remains pending.`);
    }
  }, [saveMedicationStatus, speakMessage]);

  useEffect(() => {
    if (!voiceEnabled) return undefined;

    const checkDueMedications = () => {
      const now = new Date();
      const hour = now.getHours() % 12 || 12;
      const minute = String(now.getMinutes()).padStart(2, '0');
      const currentTime = `${String(hour).padStart(2, '0')}:${minute}`;
      const meridiem = now.getHours() >= 12 ? 'PM' : 'AM';
      const today = `${now.getFullYear()}-${String(now.getMonth() + 1).padStart(2, '0')}-${String(now.getDate()).padStart(2, '0')}`;

      const dueMedication = medications.find((medication) => {
        const match = medication.scheduleTime?.trim().match(/^(\d{1,2}):(\d{2})\s*(AM|PM)?$/i);
        if (!match) return false;
        if (!/daily|every day/i.test(medication.frequency || '')) return false;
        const scheduledTime = `${String(Number(match[1])).padStart(2, '0')}:${match[2]}`;
        const scheduledMeridiem = match[3]?.toUpperCase();
        const reminderKey = `${today}:${medication.id}`;
        return medication.status === 'PENDING'
          && scheduledTime === currentTime
          && (!scheduledMeridiem || scheduledMeridiem === meridiem)
          && !announcedReminders.current.has(reminderKey);
      });

      if (dueMedication) {
        announcedReminders.current.add(`${today}:${dueMedication.id}`);
        setVoiceStatus(`Reminder for ${dueMedication.name}. Listening for confirmation...`);
        window.speechSynthesis?.cancel();
        const utterance = new SpeechSynthesisUtterance(
          `It is time to take ${dueMedication.name}, ${dueMedication.dosage}. Say taken after you have taken it.`,
        );
        utterance.onend = () => askForVoiceConfirmation(dueMedication);
        window.speechSynthesis?.speak(utterance);
      }
    };

    checkDueMedications();
    const intervalId = window.setInterval(checkDueMedications, 15000);
    return () => {
      window.clearInterval(intervalId);
      window.speechSynthesis?.cancel();
    };
  }, [askForVoiceConfirmation, medications, voiceEnabled]);

  const handleSubmit = (e) => {
    e.preventDefault();
    setMedications((current) => [{ id: Date.now(), ...form, status: 'PENDING' }, ...current]);
    setForm({ name: '', dosage: '', scheduleTime: '', frequency: '', notes: '' });
  };

  return (
    <main className="mx-auto max-w-7xl px-6 py-10">
      <div className="grid gap-8 xl:grid-cols-[0.9fr_1.1fr]">
        <div className="card">
          <h2 className="text-2xl font-bold text-slate-900">Add medication</h2>
          <form className="mt-6 space-y-4" onSubmit={handleSubmit}>
            <input value={form.name} onChange={(e) => setForm({ ...form, name: e.target.value })} placeholder="Medication name" className="w-full rounded-xl border border-slate-200 bg-slate-50 p-3" />
            <input value={form.dosage} onChange={(e) => setForm({ ...form, dosage: e.target.value })} placeholder="Dosage" className="w-full rounded-xl border border-slate-200 bg-slate-50 p-3" />
            <input value={form.scheduleTime} onChange={(e) => setForm({ ...form, scheduleTime: e.target.value })} placeholder="08:00 AM" className="w-full rounded-xl border border-slate-200 bg-slate-50 p-3" />
            <input value={form.frequency} onChange={(e) => setForm({ ...form, frequency: e.target.value })} placeholder="Daily / Twice a week" className="w-full rounded-xl border border-slate-200 bg-slate-50 p-3" />
            <textarea value={form.notes} onChange={(e) => setForm({ ...form, notes: e.target.value })} placeholder="Notes for caregiver or elder" className="w-full rounded-xl border border-slate-200 bg-slate-50 p-3" rows="4" />
            <button type="submit" className="w-full rounded-xl bg-brand-600 px-4 py-3 font-semibold text-white">Save medication</button>
          </form>
        </div>

        <div className="card">
          <div className="flex flex-wrap items-start justify-between gap-4">
            <div>
              <h2 className="text-2xl font-bold text-slate-900">Medication schedule</h2>
              <p className="mt-2 text-sm text-slate-600" aria-live="polite">{voiceStatus}</p>
            </div>
            <button
              type="button"
              onClick={() => {
                if (!('speechSynthesis' in window) || typeof SpeechSynthesisUtterance === 'undefined') {
                  setVoiceStatus('Spoken alerts are not supported in this browser.');
                  return;
                }
                setVoiceEnabled((enabled) => !enabled);
                setVoiceStatus(voiceEnabled ? 'Voice reminders are off.' : 'Voice reminders are on for daily medications on this page.');
                if (voiceEnabled) window.speechSynthesis.cancel();
              }}
              className="inline-flex items-center gap-2 rounded-xl border border-brand-200 bg-brand-50 px-4 py-2 text-sm font-semibold text-brand-700"
              aria-pressed={voiceEnabled}
            >
              <BellRing className="h-4 w-4" />
              {voiceEnabled ? 'Turn off voice reminders' : 'Enable voice reminders'}
            </button>
          </div>
          {statusError && <p role="alert" className="mt-3 text-sm font-medium text-red-600">{statusError}</p>}
          {isListening && <p className="mt-3 inline-flex items-center gap-2 text-sm font-medium text-brand-700"><Mic className="h-4 w-4" />Listening for “taken”</p>}
          <div className="mt-6 space-y-4">
            {medications.map((medication) => (
              <div key={medication.id} className="rounded-2xl border border-slate-200 bg-slate-50 p-4">
                <div className="flex items-start justify-between gap-4">
                  <div>
                    <div className="text-lg font-semibold text-slate-900">{medication.name}</div>
                    <div className="mt-1 text-sm text-slate-500">{medication.dosage} · {medication.scheduleTime}</div>
                  </div>
                  <span className={`rounded-full px-2.5 py-1 text-xs font-semibold ${medication.status === 'TAKEN' ? 'bg-emerald-100 text-emerald-700' : 'bg-amber-100 text-amber-700'}`}>
                    {medication.status}
                  </span>
                </div>
                <div className="mt-3 text-sm text-slate-600">{medication.frequency}</div>
                {medication.notes && <div className="mt-2 text-sm text-slate-500">{medication.notes}</div>}
                {medication.status !== 'TAKEN' && (
                  <button
                    type="button"
                    onClick={() => saveMedicationStatus(medication, 'TAKEN')}
                    className="mt-4 rounded-lg bg-emerald-600 px-4 py-2 text-sm font-semibold text-white transition hover:bg-emerald-700"
                  >
                    Mark taken
                  </button>
                )}
              </div>
            ))}
          </div>
        </div>
      </div>
    </main>
  );
}

function RecordsPage({ records, setRecords }) {
  const [form, setForm] = useState({ title: '', documentType: 'Prescription', summary: '' });
  const [selectedFile, setSelectedFile] = useState(null);

  const handleSubmit = (e) => {
    e.preventDefault();
    const entry = {
      id: Date.now(),
      title: form.title,
      documentType: form.documentType,
      summary: form.summary,
      extractedText: selectedFile ? `Uploaded file: ${selectedFile.name}` : 'OCR-ready placeholder extracted from the document.',
      fileName: selectedFile ? selectedFile.name : 'document.pdf',
    };
    setRecords((current) => [entry, ...current]);
    setForm({ title: '', documentType: 'Prescription', summary: '' });
    setSelectedFile(null);
  };

  return (
    <main className="mx-auto max-w-7xl px-6 py-10">
      <div className="grid gap-8 xl:grid-cols-[0.9fr_1.1fr]">
        <div className="card">
          <h2 className="text-2xl font-bold text-slate-900">Upload medical record</h2>
          <form className="mt-6 space-y-4" onSubmit={handleSubmit}>
            <input value={form.title} onChange={(e) => setForm({ ...form, title: e.target.value })} placeholder="Record title" className="w-full rounded-xl border border-slate-200 bg-slate-50 p-3" />
            <select value={form.documentType} onChange={(e) => setForm({ ...form, documentType: e.target.value })} className="w-full rounded-xl border border-slate-200 bg-slate-50 p-3">
              <option value="Prescription">Prescription</option>
              <option value="Report">Report</option>
              <option value="Lab">Lab result</option>
            </select>
            <textarea value={form.summary} onChange={(e) => setForm({ ...form, summary: e.target.value })} placeholder="Summary and clinician notes" rows="4" className="w-full rounded-xl border border-slate-200 bg-slate-50 p-3" />
            <input type="file" onChange={(e) => setSelectedFile(e.target.files[0])} className="w-full rounded-xl border border-dashed border-slate-300 bg-slate-50 p-3" />
            <button type="submit" className="w-full rounded-xl bg-brand-600 px-4 py-3 font-semibold text-white">Store record</button>
          </form>
        </div>

        <div className="card">
          <h2 className="text-2xl font-bold text-slate-900">Medical vault</h2>
          <div className="mt-6 space-y-4">
            {records.map((record) => (
              <div key={record.id} className="rounded-2xl border border-slate-200 bg-slate-50 p-4">
                <div className="flex items-center justify-between gap-3">
                  <div className="text-lg font-semibold text-slate-900">{record.title}</div>
                  <span className="rounded-full bg-brand-100 px-2.5 py-1 text-xs font-semibold text-brand-700">{record.documentType}</span>
                </div>
                <div className="mt-2 text-sm text-slate-600">{record.summary}</div>
                <div className="mt-3 rounded-xl bg-white p-3 text-sm text-slate-500">{record.extractedText}</div>
              </div>
            ))}
          </div>
        </div>
      </div>
    </main>
  );
}

function AppointmentsPage({ appointments, setAppointments }) {
  const [form, setForm] = useState({ title: '', doctorName: '', appointmentTime: '', location: '', notes: '' });

  const handleSubmit = (e) => {
    e.preventDefault();
    setAppointments((current) => [{ id: Date.now(), ...form, status: 'SCHEDULED', appointmentTime: new Date(form.appointmentTime).toISOString() }, ...current]);
    setForm({ title: '', doctorName: '', appointmentTime: '', location: '', notes: '' });
  };

  return (
    <main className="mx-auto max-w-7xl px-6 py-10">
      <div className="grid gap-8 xl:grid-cols-[0.9fr_1.1fr]">
        <div className="card">
          <h2 className="text-2xl font-bold text-slate-900">Schedule appointment</h2>
          <form className="mt-6 space-y-4" onSubmit={handleSubmit}>
            <input value={form.title} onChange={(e) => setForm({ ...form, title: e.target.value })} placeholder="Appointment title" className="w-full rounded-xl border border-slate-200 bg-slate-50 p-3" />
            <input value={form.doctorName} onChange={(e) => setForm({ ...form, doctorName: e.target.value })} placeholder="Clinician or care team" className="w-full rounded-xl border border-slate-200 bg-slate-50 p-3" />
            <input type="datetime-local" value={form.appointmentTime} onChange={(e) => setForm({ ...form, appointmentTime: e.target.value })} className="w-full rounded-xl border border-slate-200 bg-slate-50 p-3" />
            <input value={form.location} onChange={(e) => setForm({ ...form, location: e.target.value })} placeholder="Location" className="w-full rounded-xl border border-slate-200 bg-slate-50 p-3" />
            <textarea value={form.notes} onChange={(e) => setForm({ ...form, notes: e.target.value })} placeholder="Notes" rows="4" className="w-full rounded-xl border border-slate-200 bg-slate-50 p-3" />
            <button type="submit" className="w-full rounded-xl bg-brand-600 px-4 py-3 font-semibold text-white">Add appointment</button>
          </form>
        </div>

        <div className="card">
          <h2 className="text-2xl font-bold text-slate-900">Upcoming visits</h2>
          <div className="mt-6 space-y-4">
            {appointments.map((appointment) => (
              <div key={appointment.id} className="rounded-2xl border border-slate-200 bg-slate-50 p-4">
                <div className="flex items-start justify-between gap-3">
                  <div>
                    <div className="text-lg font-semibold text-slate-900">{appointment.title}</div>
                    <div className="mt-1 text-sm text-slate-500">{appointment.doctorName || 'Care team'}</div>
                  </div>
                  <span className="rounded-full bg-sky-100 px-2.5 py-1 text-xs font-semibold text-sky-700">{appointment.status}</span>
                </div>
                <div className="mt-3 text-sm text-slate-600">{formatDateTime(appointment.appointmentTime)}</div>
                {appointment.location && <div className="mt-2 text-sm text-slate-500">{appointment.location}</div>}
              </div>
            ))}
          </div>
        </div>
      </div>
    </main>
  );
}

function StatItem({ label, value }) {
  return (
    <div className="rounded-2xl border border-slate-200 bg-white p-4 shadow-soft">
      <div className="text-xl font-bold text-slate-900">{value}</div>
      <div className="text-sm text-slate-500">{label}</div>
    </div>
  );
}

function SolutionCard({ title, body, icon }) {
  return (
    <div className="card">
      <div className="mb-4 flex h-12 w-12 items-center justify-center rounded-xl bg-brand-50 text-brand-700">{icon}</div>
      <h3 className="text-xl font-semibold text-slate-900">{title}</h3>
      <p className="mt-3 text-slate-600">{body}</p>
    </div>
  );
}

function MiniCard({ icon, title, detail, status, tone }) {
  const tones = {
    amber: 'bg-amber-50 text-amber-700',
    sky: 'bg-sky-50 text-sky-700',
    emerald: 'bg-emerald-50 text-emerald-700',
  };

  return (
    <div className="rounded-2xl border border-slate-200 bg-slate-50 p-4">
      <div className="flex items-center justify-between gap-3">
        <div className="flex items-center gap-3">
          <div className={`flex h-10 w-10 items-center justify-center rounded-xl ${tones[tone]}`}>{icon}</div>
          <div>
            <div className="font-semibold text-slate-900">{title}</div>
            <div className="text-sm text-slate-500">{detail}</div>
          </div>
        </div>
        <span className="rounded-full bg-white px-2 py-1 text-[10px] font-semibold uppercase tracking-[0.12em] text-slate-600">{status}</span>
      </div>
    </div>
  );
}

function PriorityRow({ title, detail }) {
  return (
    <div className="rounded-2xl border border-slate-200 bg-slate-50 p-4">
      <div className="text-xs font-semibold uppercase tracking-[0.14em] text-slate-500">{title}</div>
      <div className="mt-2 text-sm font-medium text-slate-700">{detail}</div>
    </div>
  );
}

export default App;
