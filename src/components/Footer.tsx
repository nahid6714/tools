import React from 'react';
import { Link } from 'react-router-dom';
import { 
  ArrowUp, 
  Github, 
  Facebook, 
  Linkedin, 
  Mail, 
  Smartphone, 
  Heart,
  Code2
} from 'lucide-react';
import { PERSONAL_INFO } from '../data/portfolioData';

export const Footer: React.FC = () => {
  const scrollToTop = () => {
    window.scrollTo({ top: 0, behavior: 'smooth' });
  };

  const navLinks = [
    { name: 'Home', path: '/' },
    { name: 'About', path: '/about' },
    { name: 'Skills', path: '/skills' },
    { name: 'Projects', path: '/projects' },
    { name: 'Apps', path: '/apps' },
    { name: 'Experience', path: '/experience' },
    { name: 'Certificates', path: '/certificates' },
    { name: 'Contact', path: '/contact' },
  ];

  return (
    <footer className="bg-slate-950 dark:bg-slate-950 light:bg-white text-slate-400 dark:text-slate-400 light:text-slate-600 border-t border-slate-900 dark:border-slate-900 light:border-slate-200">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-14">
        
        <div className="grid grid-cols-1 md:grid-cols-12 gap-8 items-start pb-12 border-b border-slate-900 dark:border-slate-900 light:border-slate-200">
          
          {/* Brand & Identity */}
          <div className="md:col-span-5 space-y-3">
            <Link to="/" className="flex items-center gap-3 group">
              <div className="w-8 h-8 rounded-xl bg-gradient-to-tr from-blue-600 to-indigo-600 flex items-center justify-center text-white font-bold text-xs shadow-md group-hover:scale-105 transition-transform">
                NH
              </div>
              <span className="font-extrabold text-slate-100 dark:text-slate-100 light:text-slate-900 text-lg tracking-tight group-hover:text-blue-400 transition-colors">
                {PERSONAL_INFO.name}
              </span>
            </Link>

            {/* Requested tagline */}
            <p className="text-sm font-semibold text-blue-400">
              Student • Worker • Developer
            </p>

            <p className="text-xs text-slate-400 dark:text-slate-400 light:text-slate-600 max-w-sm leading-relaxed">
              Passionate about technology, software development, Android applications, and building useful digital tools from Bangladesh.
            </p>
          </div>

          {/* Navigation Links */}
          <div className="md:col-span-4 space-y-2">
            <span className="text-xs font-bold uppercase tracking-wider text-slate-200 dark:text-slate-200 light:text-slate-900 block mb-3">
              Pages
            </span>
            <div className="grid grid-cols-2 sm:grid-cols-3 gap-2 text-xs">
              {navLinks.map((item) => (
                <Link
                  key={item.name}
                  to={item.path}
                  className="hover:text-blue-400 transition-colors py-1"
                >
                  {item.name}
                </Link>
              ))}
            </div>
          </div>

          {/* Social Profiles & Back to Top */}
          <div className="md:col-span-3 flex flex-col items-start md:items-end justify-between space-y-4">
            <div>
              <span className="text-xs font-bold uppercase tracking-wider text-slate-200 dark:text-slate-200 light:text-slate-900 block mb-3 md:text-right">
                Connect Online
              </span>
              <div className="flex items-center gap-2">
                <a
                  href={PERSONAL_INFO.githubUrl}
                  target="_blank"
                  rel="noopener noreferrer"
                  className="p-2 rounded-xl bg-slate-900 dark:bg-slate-900 light:bg-slate-100 hover:bg-slate-800 text-slate-300 dark:text-slate-300 light:text-slate-700 hover:text-white border border-slate-800 transition-colors"
                  aria-label="GitHub"
                >
                  <Github className="w-4 h-4" />
                </a>

                <a
                  href={PERSONAL_INFO.linkedinUrl}
                  target="_blank"
                  rel="noopener noreferrer"
                  className="p-2 rounded-xl bg-slate-900 dark:bg-slate-900 light:bg-slate-100 hover:bg-slate-800 text-slate-300 dark:text-slate-300 light:text-slate-700 hover:text-white border border-slate-800 transition-colors"
                  aria-label="LinkedIn"
                >
                  <Linkedin className="w-4 h-4" />
                </a>

                <a
                  href={PERSONAL_INFO.facebookUrl}
                  target="_blank"
                  rel="noopener noreferrer"
                  className="p-2 rounded-xl bg-slate-900 dark:bg-slate-900 light:bg-slate-100 hover:bg-slate-800 text-slate-300 dark:text-slate-300 light:text-slate-700 hover:text-white border border-slate-800 transition-colors"
                  aria-label="Facebook"
                >
                  <Facebook className="w-4 h-4" />
                </a>

                <a
                  href={`mailto:${PERSONAL_INFO.email}`}
                  className="p-2 rounded-xl bg-slate-900 dark:bg-slate-900 light:bg-slate-100 hover:bg-slate-800 text-slate-300 dark:text-slate-300 light:text-slate-700 hover:text-white border border-slate-800 transition-colors"
                  aria-label="Email"
                >
                  <Mail className="w-4 h-4" />
                </a>
              </div>
            </div>

            <button
              onClick={scrollToTop}
              className="inline-flex items-center gap-1.5 text-xs font-semibold px-3 py-1.5 rounded-lg bg-slate-900 hover:bg-slate-800 text-slate-300 border border-slate-800 transition-colors group"
            >
              <span>Back to Top</span>
              <ArrowUp className="w-3.5 h-3.5 group-hover:-translate-y-0.5 transition-transform" />
            </button>
          </div>

        </div>

        {/* Bottom Copyright bar */}
        <div className="pt-8 flex flex-col sm:flex-row items-center justify-between gap-4 text-xs text-slate-500">
          <p>© 2026 Nahid Hossain. All rights reserved.</p>
          
          <div className="flex items-center gap-3">
            <span>Portfolio of Nahid Hossain</span>
          </div>
        </div>

      </div>
    </footer>
  );
};
