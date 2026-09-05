import React from 'react';
import { 
  X, 
  Printer, 
  Download, 
  ExternalLink, 
  Mail, 
  Github, 
  MapPin, 
  CheckCircle2, 
  Briefcase, 
  GraduationCap, 
  Layers, 
  Smartphone,
  Globe
} from 'lucide-react';
import { PERSONAL_INFO, SKILLS, PROJECTS } from '../data/portfolioData';

interface ResumeModalProps {
  isOpen: boolean;
  onClose: () => void;
}

export const ResumeModal: React.FC<ResumeModalProps> = ({ isOpen, onClose }) => {
  if (!isOpen) return null;

  const handlePrint = () => {
    window.print();
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4 sm:p-6 bg-slate-950/80 backdrop-blur-sm animate-in fade-in duration-200">
      <div 
        className="relative w-full max-w-4xl max-h-[90vh] flex flex-col rounded-2xl bg-slate-900 border border-slate-800 shadow-2xl overflow-hidden"
        onClick={(e) => e.stopPropagation()}
      >
        
        {/* Modal Toolbar */}
        <div className="flex items-center justify-between px-6 py-4 border-b border-slate-800 bg-slate-950/90 print:hidden">
          <div className="flex items-center gap-2">
            <span className="font-bold text-slate-200 text-sm">Resume Preview</span>
            <span className="text-xs text-slate-500">• Nahid Hossain</span>
          </div>

          <div className="flex items-center gap-2.5">
            <button
              onClick={handlePrint}
              className="inline-flex items-center gap-1.5 px-3 py-1.5 rounded-lg bg-blue-600 hover:bg-blue-500 text-white text-xs font-semibold shadow-sm transition-colors"
              title="Print or Save as PDF"
            >
              <Printer className="w-3.5 h-3.5" />
              <span>Print / Save PDF</span>
            </button>

            <button
              onClick={onClose}
              className="p-1.5 rounded-lg text-slate-400 hover:text-white bg-slate-900 hover:bg-slate-800 border border-slate-800 transition-colors"
              aria-label="Close modal"
            >
              <X className="w-5 h-5" />
            </button>
          </div>
        </div>

        {/* Printable Resume Document Container */}
        <div className="overflow-y-auto p-6 sm:p-10 space-y-8 text-slate-200 print:text-black print:bg-white print:p-0">
          
          {/* Header */}
          <div className="border-b border-slate-800 print:border-slate-300 pb-6 flex flex-col sm:flex-row items-start sm:items-center justify-between gap-4">
            <div>
              <h1 className="text-3xl font-extrabold tracking-tight text-white print:text-black">
                {PERSONAL_INFO.name}
              </h1>
              <p className="text-sm font-semibold text-blue-400 print:text-blue-700 mt-1">
                {PERSONAL_INFO.subtitle}
              </p>
              <p className="text-xs text-slate-400 print:text-slate-600 mt-0.5">
                AI-Assisted Android & Web Developer
              </p>
            </div>

            <div className="text-xs space-y-1 text-slate-400 print:text-slate-600">
              <div className="flex items-center gap-2">
                <MapPin className="w-3.5 h-3.5 text-rose-400 print:text-black" />
                <span>{PERSONAL_INFO.country}</span>
              </div>
              <div className="flex items-center gap-2">
                <Mail className="w-3.5 h-3.5 text-blue-400 print:text-black" />
                <a href={`mailto:${PERSONAL_INFO.email}`} className="hover:underline">
                  {PERSONAL_INFO.email}
                </a>
              </div>
              <div className="flex items-center gap-2">
                <Github className="w-3.5 h-3.5 text-purple-400 print:text-black" />
                <a href={PERSONAL_INFO.githubUrl} target="_blank" rel="noreferrer" className="hover:underline">
                  github.com/nahid6714
                </a>
              </div>
            </div>
          </div>

          {/* Professional Summary */}
          <div>
            <h2 className="text-xs font-bold uppercase tracking-wider text-blue-400 print:text-blue-800 border-b border-slate-800 print:border-slate-300 pb-1 mb-3">
              Professional Summary
            </h2>
            <p className="text-xs sm:text-sm text-slate-300 print:text-slate-800 leading-relaxed">
              {PERSONAL_INFO.extendedBio}
            </p>
          </div>

          {/* Core Technical Competencies */}
          <div>
            <h2 className="text-xs font-bold uppercase tracking-wider text-blue-400 print:text-blue-800 border-b border-slate-800 print:border-slate-300 pb-1 mb-3">
              Technical Competencies
            </h2>
            <div className="grid grid-cols-1 sm:grid-cols-2 gap-2.5 text-xs text-slate-300 print:text-slate-800">
              <div>
                <strong className="text-slate-100 print:text-black block mb-0.5 font-semibold">
                  • Android & Native Mobile:
                </strong>
                <span className="text-slate-400 print:text-slate-600">
                  Kotlin, Jetpack Compose, Room Database, Android SDK, Gradle, APK Build & Signing
                </span>
              </div>
              <div>
                <strong className="text-slate-100 print:text-black block mb-0.5 font-semibold">
                  • Version Control & CI/CD:
                </strong>
                <span className="text-slate-400 print:text-slate-600">
                  Git, GitHub, GitHub Actions automated APK workflow, Release Asset Deployment
                </span>
              </div>
              <div>
                <strong className="text-slate-100 print:text-black block mb-0.5 font-semibold">
                  • Web Development:
                </strong>
                <span className="text-slate-400 print:text-slate-600">
                  HTML5, CSS3, JavaScript, Responsive Layouts, Netlify Hosting
                </span>
              </div>
              <div>
                <strong className="text-slate-100 print:text-black block mb-0.5 font-semibold">
                  • Tools & Ecosystem:
                </strong>
                <span className="text-slate-400 print:text-slate-600">
                  Android Studio, Material Design 3, Computer Skills, Automation, Firebase
                </span>
              </div>
            </div>
          </div>

          {/* Key Featured Projects */}
          <div>
            <h2 className="text-xs font-bold uppercase tracking-wider text-blue-400 print:text-blue-800 border-b border-slate-800 print:border-slate-300 pb-1 mb-3">
              Featured Software Projects
            </h2>
            <div className="space-y-4">
              {PROJECTS.map((proj) => (
                <div key={proj.id} className="text-xs sm:text-sm">
                  <div className="flex items-center justify-between font-semibold">
                    <span className="text-slate-100 print:text-black flex items-center gap-1.5">
                      <span className="text-blue-400 font-bold">•</span>
                      {proj.name} — <span className="font-normal text-xs text-blue-400 print:text-blue-600">{proj.category}</span>
                    </span>
                    <span className="text-slate-400 print:text-slate-600 text-xs">
                      {proj.technologies.slice(0, 3).join(', ')}
                    </span>
                  </div>
                  <p className="text-xs text-slate-400 print:text-slate-700 mt-1 pl-3 leading-relaxed">
                    {proj.longDescription || proj.description}
                  </p>
                </div>
              ))}
            </div>
          </div>

          {/* Education */}
          <div>
            <h2 className="text-xs font-bold uppercase tracking-wider text-blue-400 print:text-blue-800 border-b border-slate-800 print:border-slate-300 pb-1 mb-3">
              Academic Background
            </h2>
            <div className="text-xs sm:text-sm">
              <div className="flex items-center justify-between font-semibold text-slate-100 print:text-black">
                <span>Computer Technology & Software Foundations</span>
                <span className="text-slate-400 print:text-slate-600 text-xs">2024 – Present</span>
              </div>
              <p className="text-xs text-slate-400 print:text-slate-700 mt-1">
                Student & Independent Developer in Bangladesh. Continuous practical project implementation.
              </p>
            </div>
          </div>

        </div>

      </div>
    </div>
  );
};
