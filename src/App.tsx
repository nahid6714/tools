import React, { useState, useEffect } from 'react';
import { HashRouter, Routes, Route, Navigate } from 'react-router-dom';
import { Navbar } from './components/Navbar';
import { Footer } from './components/Footer';
import { ScrollToTop } from './components/ScrollToTop';
import { ResumeModal } from './components/ResumeModal';
import { Toast, ToastMessage } from './components/Toast';
import { RoadPerspectiveProvider } from './context/RoadPerspectiveContext';
import { RoadPerspectiveStage } from './components/RoadPerspectiveStage';
import { RoadTiltHud } from './components/RoadTiltHud';

// Dedicated Separate Pages
import { HomePage } from './pages/HomePage';
import { AboutPage } from './pages/AboutPage';
import { SkillsPage } from './pages/SkillsPage';
import { ProjectsPage } from './pages/ProjectsPage';
import { AppsPage } from './pages/AppsPage';
import { ExperiencePage } from './pages/ExperiencePage';
import { CertificatesPage } from './pages/CertificatesPage';
import { ContactPage } from './pages/ContactPage';

export default function App() {
  const [theme, setTheme] = useState<'dark' | 'light'>('dark');
  const [isResumeOpen, setIsResumeOpen] = useState(false);
  const [toasts, setToasts] = useState<ToastMessage[]>([]);

  // Initialize theme from localStorage or system preference (dark-first by default)
  useEffect(() => {
    try {
      const savedTheme = localStorage.getItem('nh_portfolio_theme') as 'dark' | 'light' | null;
      if (savedTheme) {
        setTheme(savedTheme);
      } else {
        setTheme('dark');
      }
    } catch {
      setTheme('dark');
    }
  }, []);

  // Update DOM class when theme changes
  useEffect(() => {
    const root = document.documentElement;
    if (theme === 'dark') {
      root.classList.add('dark');
      root.classList.remove('light');
      document.body.classList.add('bg-slate-950', 'text-slate-100');
      document.body.classList.remove('bg-slate-50', 'text-slate-900');
    } else {
      root.classList.remove('dark');
      root.classList.add('light');
      document.body.classList.remove('bg-slate-950', 'text-slate-100');
      document.body.classList.add('bg-slate-50', 'text-slate-900');
    }
    try {
      localStorage.setItem('nh_portfolio_theme', theme);
    } catch {
      // Ignore storage errors
    }
  }, [theme]);

  const toggleTheme = () => {
    setTheme((prev) => (prev === 'dark' ? 'light' : 'dark'));
  };

  const showToast = (message: string, type: 'info' | 'success' | 'warning' = 'info') => {
    const id = Date.now().toString() + Math.random().toString(36).substring(2, 5);
    const newToast: ToastMessage = { id, message, type };

    setToasts((prev) => [...prev.slice(-3), newToast]);

    setTimeout(() => {
      setToasts((prev) => prev.filter((t) => t.id !== id));
    }, 4500);
  };

  const dismissToast = (id: string) => {
    setToasts((prev) => prev.filter((t) => t.id !== id));
  };

  return (
    <HashRouter>
      <RoadPerspectiveProvider>
        <ScrollToTop />

        <RoadPerspectiveStage>
          {/* Consistent Top Navigation Across All Pages - Tilts in 3D along with header */}
          <Navbar
            theme={theme}
            onToggleTheme={toggleTheme}
            onOpenResume={() => setIsResumeOpen(true)}
          />

          {/* Dedicated Route Views */}
          <main className="flex-grow">
            <Routes>
              <Route
                path="/"
                element={
                  <HomePage
                    onOpenResume={() => setIsResumeOpen(true)}
                    onShowToast={showToast}
                  />
                }
              />
              <Route path="/about" element={<AboutPage />} />
              <Route path="/skills" element={<SkillsPage />} />
              <Route path="/projects" element={<ProjectsPage />} />
              <Route path="/apps" element={<AppsPage onShowToast={showToast} />} />
              <Route path="/experience" element={<ExperiencePage />} />
              <Route path="/certificates" element={<CertificatesPage />} />
              <Route path="/contact" element={<ContactPage onShowToast={showToast} />} />
              <Route path="*" element={<Navigate to="/" replace />} />
            </Routes>
          </main>

          {/* Consistent Footer Across All Pages */}
          <Footer />
        </RoadPerspectiveStage>

        {/* Global Floating 3D Road Tilt Controller & Sensor Status */}
        <RoadTiltHud />

        {/* Global Printable Resume Modal */}
        <ResumeModal
          isOpen={isResumeOpen}
          onClose={() => setIsResumeOpen(false)}
        />

        {/* Global Interactive Notification Toasts */}
        <Toast toasts={toasts} onDismiss={dismissToast} />
      </RoadPerspectiveProvider>
    </HashRouter>
  );
}
