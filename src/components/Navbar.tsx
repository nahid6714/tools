import React, { useState, useEffect, useRef } from 'react';
import { NavLink, Link, useLocation } from 'react-router-dom';
import { motion, AnimatePresence } from 'motion/react';
import { 
  Sun, 
  Moon, 
  Smartphone, 
  FileText, 
  Github, 
  ExternalLink,
  ChevronRight,
  Home,
  User,
  Wrench,
  FolderGit2,
  Briefcase,
  Award,
  Mail,
  Sparkles
} from 'lucide-react';
import { PERSONAL_INFO } from '../data/portfolioData';

interface NavbarProps {
  theme: 'dark' | 'light';
  onToggleTheme: () => void;
  onOpenResume: () => void;
}

interface NavItem {
  name: string;
  path: string;
  badge?: string;
  icon?: React.ReactNode;
}

const NAV_LINKS: NavItem[] = [
  { name: 'Home', path: '/', icon: <Home className="w-4 h-4" /> },
  { name: 'About', path: '/about', icon: <User className="w-4 h-4" /> },
  { name: 'Skills', path: '/skills', icon: <Wrench className="w-4 h-4" /> },
  { name: 'Projects', path: '/projects', icon: <FolderGit2 className="w-4 h-4" /> },
  { name: 'Apps', path: '/apps', badge: 'APK', icon: <Smartphone className="w-4 h-4" /> },
  { name: 'Experience', path: '/experience', icon: <Briefcase className="w-4 h-4" /> },
  { name: 'Certificates', path: '/certificates', icon: <Award className="w-4 h-4" /> },
  { name: 'Contact', path: '/contact', icon: <Mail className="w-4 h-4" /> },
];

export const Navbar: React.FC<NavbarProps> = ({ theme, onToggleTheme, onOpenResume }) => {
  const [isMobileMenuOpen, setIsMobileMenuOpen] = useState(false);
  const location = useLocation();
  const navRef = useRef<HTMLElement>(null);

  useEffect(() => {
    const handleKeyDown = (e: KeyboardEvent) => {
      if (e.key === 'Escape' && isMobileMenuOpen) {
        setIsMobileMenuOpen(false);
      }
    };

    window.addEventListener('keydown', handleKeyDown);
    return () => {
      window.removeEventListener('keydown', handleKeyDown);
    };
  }, [isMobileMenuOpen]);

  // Close mobile menu on page navigation
  useEffect(() => {
    setIsMobileMenuOpen(false);
  }, [location.pathname]);

  return (
    <header
      id="main-navigation"
      ref={navRef}
      className="fixed top-0 left-0 right-0 z-50 py-3 bg-slate-950/95 dark:bg-slate-950/95 light:bg-white/95 backdrop-blur-md border-b border-slate-800/80 dark:border-slate-800/80 light:border-slate-200 shadow-sm transition-colors duration-200"
    >
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 flex items-center justify-between">
        
        {/* Brand Logo */}
        <Link
          to="/"
          className="flex items-center gap-3 group focus:outline-none focus-visible:ring-2 focus-visible:ring-blue-500 rounded-lg p-1"
          aria-label="Nahid Hossain - Home"
        >
          <div className="relative w-9 h-9 rounded-xl bg-gradient-to-tr from-blue-600 via-indigo-600 to-sky-500 flex items-center justify-center text-white font-bold text-sm shadow-md shadow-blue-500/20 group-hover:scale-105 transition-transform duration-200">
            NH
            <span className="absolute -bottom-0.5 -right-0.5 w-2.5 h-2.5 bg-emerald-500 border-2 border-slate-950 rounded-full" title="Available for projects"></span>
          </div>
          <div className="flex flex-col">
            <span className="font-semibold text-slate-100 dark:text-slate-100 light:text-slate-900 tracking-tight text-base group-hover:text-blue-400 transition-colors">
              {PERSONAL_INFO.name}
            </span>
            <span className="text-[11px] font-medium text-slate-400 dark:text-slate-400 light:text-slate-500">
              Student & Developer
            </span>
          </div>
        </Link>

        {/* Desktop & Large Screen Navigation Links */}
        <nav className="hidden lg:flex items-center gap-1 bg-slate-900/70 dark:bg-slate-900/70 light:bg-slate-100/90 px-3 py-1.5 rounded-full border border-slate-800/70 dark:border-slate-800 light:border-slate-200 backdrop-blur-md">
          {NAV_LINKS.map((link) => {
            const isActive = location.pathname === link.path;
            return (
              <NavLink
                key={link.name}
                to={link.path}
                className={`relative px-3 py-1.5 text-xs font-semibold rounded-full transition-all duration-200 flex items-center gap-1.5 ${
                  isActive
                    ? 'text-white bg-blue-600 shadow-sm shadow-blue-500/25'
                    : 'text-slate-300 dark:text-slate-300 light:text-slate-700 hover:text-white dark:hover:text-white light:hover:text-slate-950 hover:bg-slate-800/60'
                }`}
              >
                <span>{link.name}</span>
                {link.badge && (
                  <span className={`text-[9px] px-1.5 py-0.2 rounded-full font-bold uppercase ${
                    isActive ? 'bg-white/25 text-white' : 'bg-emerald-500/20 text-emerald-400'
                  }`}>
                    {link.badge}
                  </span>
                )}
              </NavLink>
            );
          })}
        </nav>

        {/* Action Controls: Theme toggle + Resume button + Mobile Dropdown Menu Toggle */}
        <div className="flex items-center gap-2 sm:gap-2.5">
          {/* Quick Resume CTA */}
          <button
            id="nav-resume-btn"
            onClick={onOpenResume}
            className="hidden sm:inline-flex items-center gap-1.5 px-3.5 py-2 rounded-xl text-xs font-semibold bg-slate-900 dark:bg-slate-900 light:bg-slate-100 hover:bg-slate-800 text-slate-200 dark:text-slate-200 light:text-slate-800 border border-slate-800 dark:border-slate-800 light:border-slate-300 transition-all duration-200 hover:-translate-y-0.5"
            title="Preview and download printable resume"
          >
            <FileText className="w-3.5 h-3.5 text-blue-400" />
            <span>Resume</span>
          </button>

          {/* Theme Toggle Button */}
          <button
            id="theme-toggle-btn"
            onClick={onToggleTheme}
            className="p-2 rounded-xl bg-slate-900/80 dark:bg-slate-900/80 light:bg-slate-100 hover:bg-slate-800 text-slate-300 dark:text-slate-300 light:text-slate-700 hover:text-white border border-slate-800/80 dark:border-slate-800 light:border-slate-300 transition-all duration-200"
            title={`Switch to ${theme === 'dark' ? 'Light' : 'Dark'} mode`}
            aria-label="Toggle theme"
          >
            {theme === 'dark' ? (
              <Sun className="w-4 h-4 text-amber-400" />
            ) : (
              <Moon className="w-4 h-4 text-blue-400" />
            )}
          </button>

          {/* Mobile-Only Dropdown Menu Toggle Button (Hidden on Desktop/Large Screens) */}
          <button
            id="mobile-menu-toggle-btn"
            onClick={() => setIsMobileMenuOpen(!isMobileMenuOpen)}
            className={`lg:hidden p-2 rounded-xl transition-all duration-200 border ${
              isMobileMenuOpen
                ? 'bg-blue-600 text-white border-blue-500 shadow-md shadow-blue-500/25'
                : 'bg-slate-900/80 dark:bg-slate-900/80 light:bg-slate-100 hover:bg-slate-800 text-slate-300 dark:text-slate-300 light:text-slate-700 hover:text-white border-slate-800/80 dark:border-slate-800 light:border-slate-300'
            }`}
            aria-label="Toggle Navigation Menu"
            aria-expanded={isMobileMenuOpen}
            title={isMobileMenuOpen ? 'মেনু বন্ধ করুন' : 'মেনু খুলুন'}
          >
            <div className="w-5 h-5 flex flex-col justify-center items-center gap-1 relative">
              <motion.span
                animate={isMobileMenuOpen ? { rotate: 45, y: 5 } : { rotate: 0, y: 0 }}
                transition={{ duration: 0.22 }}
                className="w-4 h-0.5 bg-current rounded-full origin-center"
              />
              <motion.span
                animate={isMobileMenuOpen ? { opacity: 0, x: -4 } : { opacity: 1, x: 0 }}
                transition={{ duration: 0.18 }}
                className="w-4 h-0.5 bg-current rounded-full"
              />
              <motion.span
                animate={isMobileMenuOpen ? { rotate: -45, y: -5 } : { rotate: 0, y: 0 }}
                transition={{ duration: 0.22 }}
                className="w-4 h-0.5 bg-current rounded-full origin-center"
              />
            </div>
          </button>
        </div>
      </div>

      {/* Top Menu Dropdown Drawer with Rich Slide-Down Animation */}
      <AnimatePresence>
        {isMobileMenuOpen && (
          <motion.div
            key="top-menu-dropdown"
            initial={{ opacity: 0, height: 0, y: -24 }}
            animate={{ 
              opacity: 1, 
              height: 'auto', 
              y: 0,
              transition: {
                height: { duration: 0.38, ease: [0.16, 1, 0.3, 1] },
                opacity: { duration: 0.25 },
                y: { duration: 0.38, ease: [0.16, 1, 0.3, 1] }
              }
            }}
            exit={{ 
              opacity: 0, 
              height: 0, 
              y: -20,
              transition: {
                height: { duration: 0.28, ease: 'easeInOut' },
                opacity: { duration: 0.18 },
                y: { duration: 0.25 }
              }
            }}
            className="lg:hidden overflow-hidden bg-slate-950/95 dark:bg-slate-950/95 light:bg-white/95 backdrop-blur-2xl border-b border-slate-800/80 dark:border-slate-800 light:border-slate-200 shadow-2xl"
          >
            {/* Subtle luminous accent bar along top */}
            <div className="h-0.5 w-full bg-gradient-to-r from-transparent via-blue-500/50 to-transparent" />

            <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 pt-3.5 pb-6 space-y-1.5">
              <motion.div
                initial={{ opacity: 0, y: -8 }}
                animate={{ opacity: 1, y: 0 }}
                transition={{ duration: 0.22, delay: 0.04 }}
                className="flex items-center justify-between px-3 py-1 text-[11px] font-bold uppercase tracking-wider text-slate-400 dark:text-slate-400 light:text-slate-500"
              >
                <div className="flex items-center gap-1.5">
                  <Sparkles className="w-3 h-3 text-blue-400" />
                  <span>মেনু</span>
                </div>
                <span className="text-[10px] text-blue-400 font-medium">পেজ নির্বাচন করুন</span>
              </motion.div>

              <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-2">
                {NAV_LINKS.map((link, index) => {
                  const isActive = location.pathname === link.path;
                  return (
                    <motion.div
                      key={link.name}
                      initial={{ opacity: 0, y: -14, x: -4 }}
                      animate={{ opacity: 1, y: 0, x: 0 }}
                      transition={{
                        duration: 0.28,
                        delay: 0.03 + index * 0.028,
                        ease: [0.16, 1, 0.3, 1],
                      }}
                    >
                      <NavLink
                        to={link.path}
                        onClick={() => setIsMobileMenuOpen(false)}
                        className={`flex items-center justify-between px-3.5 py-2.5 rounded-xl text-sm font-medium transition-all duration-200 border ${
                          isActive
                            ? 'text-white bg-blue-600 font-bold border-blue-500 shadow-md shadow-blue-500/20 translate-x-1'
                            : 'text-slate-300 dark:text-slate-300 light:text-slate-700 bg-slate-900/40 dark:bg-slate-900/40 light:bg-slate-100/70 border-slate-800/60 dark:border-slate-800/60 light:border-slate-200 hover:bg-slate-800/80 hover:text-white dark:hover:text-white light:hover:text-slate-950'
                        }`}
                      >
                        <div className="flex items-center gap-3">
                          <span className={isActive ? 'text-white' : 'text-blue-400'}>
                            {link.icon}
                          </span>
                          <span>{link.name}</span>
                        </div>
                        <div className="flex items-center gap-2">
                          {link.badge && (
                            <span className={`text-[10px] px-2 py-0.5 rounded-full font-bold uppercase ${
                              isActive ? 'bg-white/25 text-white' : 'bg-emerald-500/20 text-emerald-400'
                            }`}>
                              {link.badge}
                            </span>
                          )}
                          <ChevronRight className={`w-4 h-4 transition-transform duration-200 ${
                            isActive ? 'text-white translate-x-0.5' : 'text-slate-500'
                          }`} />
                        </div>
                      </NavLink>
                    </motion.div>
                  );
                })}
              </div>

              {/* Quick Actions Footer */}
              <motion.div
                initial={{ opacity: 0, y: -10 }}
                animate={{ opacity: 1, y: 0 }}
                transition={{
                  duration: 0.3,
                  delay: 0.05 + NAV_LINKS.length * 0.025,
                  ease: [0.16, 1, 0.3, 1],
                }}
                className="pt-4 mt-3 border-t border-slate-800/80 dark:border-slate-800 light:border-slate-200 flex flex-col sm:flex-row items-center gap-2.5"
              >
                <button
                  onClick={() => {
                    setIsMobileMenuOpen(false);
                    onOpenResume();
                  }}
                  className="w-full sm:w-auto flex-1 py-2.5 px-4 rounded-xl text-xs font-semibold bg-blue-600 hover:bg-blue-500 text-white flex items-center justify-center gap-2 shadow-sm transition-colors"
                >
                  <FileText className="w-4 h-4" />
                  <span>Preview & Print Resume</span>
                </button>

                <a
                  href={PERSONAL_INFO.githubUrl}
                  target="_blank"
                  rel="noopener noreferrer"
                  className="w-full sm:w-auto flex-1 py-2.5 px-4 rounded-xl text-xs font-semibold bg-slate-900 hover:bg-slate-800 dark:bg-slate-900 dark:hover:bg-slate-800 light:bg-slate-100 light:hover:bg-slate-200 text-slate-200 dark:text-slate-200 light:text-slate-800 border border-slate-800 dark:border-slate-800 light:border-slate-200 flex items-center justify-center gap-2 transition-colors"
                >
                  <Github className="w-4 h-4 text-purple-400" />
                  <span>GitHub Profile</span>
                  <ExternalLink className="w-3.5 h-3.5 text-slate-400" />
                </a>
              </motion.div>
            </div>
          </motion.div>
        )}
      </AnimatePresence>
    </header>
  );
};
