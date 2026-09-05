import React from 'react';
import { Link } from 'react-router-dom';
import { motion } from 'motion/react';
import { 
  ArrowRight, 
  Smartphone, 
  Download, 
  Sparkles, 
  FolderGit2, 
  Wrench, 
  Mail, 
  ExternalLink, 
  Github, 
  CheckCircle2, 
  ShieldCheck,
  ChevronRight,
  Code2
} from 'lucide-react';
import { Hero } from '../components/Hero';
import { PERSONAL_INFO, PROJECTS, SKILLS } from '../data/portfolioData';
import { APPS_CONFIG } from '../data/appsConfig';
import { triggerDirectApkDownload } from '../services/githubReleaseService';
import { ScrollReveal, ScrollStagger } from '../components/ScrollAnimation';

interface HomePageProps {
  onOpenResume: () => void;
  onShowToast: (message: string, type?: 'info' | 'success' | 'warning') => void;
}

export const HomePage: React.FC<HomePageProps> = ({ onOpenResume, onShowToast }) => {
  const toolsApp = APPS_CONFIG[0];
  const featuredProject = PROJECTS.find(p => p.featured) || PROJECTS[0];
  const topSkills = SKILLS.slice(0, 8);

  const handleQuickApkDownload = () => {
    onShowToast(`Downloading ${toolsApp.defaultRelease.apkFileName} (${toolsApp.defaultRelease.apkSize})...`, 'success');
    triggerDirectApkDownload(toolsApp.defaultRelease.downloadUrl, toolsApp.defaultRelease.apkFileName);
  };

  return (
    <div className="space-y-16 pb-12">
      {/* 1. Hero Section */}
      <Hero onOpenResume={onOpenResume} />

      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 space-y-20">
        
        {/* 2. Featured App Spotlight Banner - Flies in from below */}
        <ScrollReveal yOffset={50}>
          <section className="relative rounded-3xl overflow-hidden bg-gradient-to-br from-slate-900 via-slate-900 to-slate-950 dark:from-slate-900 dark:to-slate-950 light:bg-white border-2 border-emerald-500/40 p-6 sm:p-10 shadow-xl shadow-emerald-500/5 transition-all">
            <div className="absolute top-0 right-0 w-96 h-96 bg-emerald-500/10 rounded-full blur-3xl pointer-events-none" />
            
            <div className="relative z-10 flex flex-col lg:flex-row items-start lg:items-center justify-between gap-8">
              <div className="space-y-4 max-w-2xl">
                <div className="inline-flex items-center gap-2 px-3 py-1 rounded-full bg-emerald-500/10 text-emerald-400 text-xs font-semibold uppercase tracking-wider border border-emerald-500/25">
                  <Smartphone className="w-3.5 h-3.5" />
                  <span>Featured Android App</span>
                </div>

                <h2 className="text-2xl sm:text-3xl font-extrabold text-slate-100 dark:text-slate-100 light:text-slate-900">
                  {toolsApp.appName} — Multi-Utility Android App
                </h2>

                <p className="text-sm text-slate-300 dark:text-slate-300 light:text-slate-600 leading-relaxed">
                  Native Android utility suite created by Nahid Hossain. Features include Food Bill Manager, Smart Document Scanner, NID card scanner, thermal printing, and PDF exports.
                </p>

                <div className="flex flex-wrap items-center gap-4 text-xs font-mono text-slate-300 dark:text-slate-300 light:text-slate-700">
                  <span className="bg-slate-950/80 px-2.5 py-1 rounded-lg border border-slate-800">
                    Version: <strong className="text-emerald-400">{toolsApp.defaultRelease.version}</strong>
                  </span>
                  <span className="bg-slate-950/80 px-2.5 py-1 rounded-lg border border-slate-800">
                    APK Size: <strong className="text-slate-100">{toolsApp.defaultRelease.apkSize}</strong>
                  </span>
                  <span className="bg-slate-950/80 px-2.5 py-1 rounded-lg border border-slate-800 flex items-center gap-1">
                    <ShieldCheck className="w-3.5 h-3.5 text-emerald-400" />
                    <span>Direct GitHub Release</span>
                  </span>
                </div>
              </div>

              {/* Actions for this app */}
              <div className="flex flex-col sm:flex-row lg:flex-col gap-3 w-full lg:w-auto shrink-0">
                <button
                  onClick={handleQuickApkDownload}
                  className="inline-flex items-center justify-center gap-2 px-6 py-3.5 rounded-xl bg-emerald-600 hover:bg-emerald-500 text-white font-bold text-sm shadow-lg shadow-emerald-600/30 transition-all duration-200 active:scale-95"
                >
                  <Download className="w-4 h-4" />
                  <span>Download APK ({toolsApp.defaultRelease.apkSize})</span>
                </button>

                <Link
                  to="/apps"
                  className="inline-flex items-center justify-center gap-2 px-5 py-3 rounded-xl bg-slate-950 dark:bg-slate-950 light:bg-slate-100 hover:bg-slate-800 text-slate-200 dark:text-slate-200 light:text-slate-800 text-xs font-semibold border border-slate-800 transition-colors"
                >
                  <span>View Full App Details & Changelog</span>
                  <ArrowRight className="w-3.5 h-3.5" />
                </Link>
              </div>
            </div>
          </section>
        </ScrollReveal>

        {/* 3. Section Teaser Grid: About & Featured Project */}
        <div className="grid grid-cols-1 lg:grid-cols-2 gap-8">
          
          {/* About Teaser */}
          <ScrollReveal yOffset={40} delay={0.05}>
            <div className="h-full p-6 sm:p-8 rounded-3xl bg-slate-900/60 dark:bg-slate-900/60 light:bg-white border border-slate-800/80 dark:border-slate-800/80 light:border-slate-200 flex flex-col justify-between hover:border-slate-700 transition-colors">
              <div className="space-y-4">
                <span className="text-xs font-bold text-blue-400 uppercase tracking-wider">
                  About Nahid Hossain
                </span>
                <h3 className="text-xl font-bold text-slate-100 dark:text-slate-100 light:text-slate-900">
                  Driven by Software & Android Innovation
                </h3>
                <p className="text-xs sm:text-sm text-slate-400 dark:text-slate-400 light:text-slate-600 leading-relaxed">
                  Student and worker from Bangladesh with hands-on experience building practical Android applications, publishing APK releases on GitHub, and crafting clean web interfaces.
                </p>
                
                <div className="space-y-2 pt-2 text-xs text-slate-300 dark:text-slate-300 light:text-slate-700">
                  <div className="flex items-center gap-2">
                    <CheckCircle2 className="w-4 h-4 text-emerald-400 shrink-0" />
                    <span>Passionate about building real, useful utilities</span>
                  </div>
                  <div className="flex items-center gap-2">
                    <CheckCircle2 className="w-4 h-4 text-emerald-400 shrink-0" />
                    <span>Experience with Git, GitHub Actions, and APK pipelines</span>
                  </div>
                  <div className="flex items-center gap-2">
                    <CheckCircle2 className="w-4 h-4 text-emerald-400 shrink-0" />
                    <span>Continuously learning modern Android and web stacks</span>
                  </div>
                </div>
              </div>

              <div className="pt-6 mt-6 border-t border-slate-800/80">
                <Link
                  to="/about"
                  className="inline-flex items-center gap-2 text-xs font-bold text-blue-400 hover:text-blue-300 transition-colors"
                >
                  <span>Read Full Biography & Background</span>
                  <ChevronRight className="w-4 h-4" />
                </Link>
              </div>
            </div>
          </ScrollReveal>

          {/* Featured Project Teaser */}
          <ScrollReveal yOffset={40} delay={0.15}>
            <div className="h-full p-6 sm:p-8 rounded-3xl bg-slate-900/60 dark:bg-slate-900/60 light:bg-white border border-slate-800/80 dark:border-slate-800/80 light:border-slate-200 flex flex-col justify-between hover:border-slate-700 transition-colors">
              <div className="space-y-4">
                <div className="flex items-center justify-between">
                  <span className="text-xs font-bold text-indigo-400 uppercase tracking-wider">
                    Featured Project
                  </span>
                  <span className="text-[10px] font-bold px-2 py-0.5 rounded-full bg-indigo-500/10 text-indigo-400 border border-indigo-500/20">
                    {featuredProject.category}
                  </span>
                </div>

                <h3 className="text-xl font-bold text-slate-100 dark:text-slate-100 light:text-slate-900">
                  {featuredProject.name}
                </h3>

                <p className="text-xs sm:text-sm text-slate-400 dark:text-slate-400 light:text-slate-600 leading-relaxed">
                  {featuredProject.description}
                </p>

                <div className="flex flex-wrap gap-1.5 pt-2">
                  {featuredProject.technologies.slice(0, 5).map(tech => (
                    <span
                      key={tech}
                      className="px-2.5 py-0.5 rounded-lg text-[11px] bg-slate-950 dark:bg-slate-950 light:bg-slate-100 text-slate-300 dark:text-slate-300 light:text-slate-700 border border-slate-800"
                    >
                      {tech}
                    </span>
                  ))}
                </div>
              </div>

              <div className="pt-6 mt-6 border-t border-slate-800/80 flex items-center justify-between">
                <Link
                  to="/projects"
                  className="inline-flex items-center gap-2 text-xs font-bold text-indigo-400 hover:text-indigo-300 transition-colors"
                >
                  <span>Browse All Projects</span>
                  <ChevronRight className="w-4 h-4" />
                </Link>

                {featuredProject.githubUrl && (
                  <a
                    href={featuredProject.githubUrl}
                    target="_blank"
                    rel="noopener noreferrer"
                    className="p-2 rounded-xl bg-slate-950 hover:bg-slate-800 text-slate-300 border border-slate-800 transition-colors"
                    aria-label="GitHub Repo"
                  >
                    <Github className="w-4 h-4" />
                  </a>
                )}
              </div>
            </div>
          </ScrollReveal>

        </div>

        {/* 4. Core Skills Bar */}
        <ScrollReveal yOffset={45}>
          <div className="p-6 sm:p-8 rounded-3xl bg-slate-900/40 dark:bg-slate-900/40 light:bg-white border border-slate-800/80 dark:border-slate-800/80 light:border-slate-200">
            <div className="flex flex-col sm:flex-row items-start sm:items-center justify-between gap-4 mb-6">
              <div>
                <span className="text-xs font-bold text-blue-400 uppercase tracking-wider block mb-1">
                  Technical Expertise
                </span>
                <h3 className="text-lg font-bold text-slate-100 dark:text-slate-100 light:text-slate-900">
                  Core Technologies & Tools
                </h3>
              </div>

              <Link
                to="/skills"
                className="inline-flex items-center gap-1.5 text-xs font-semibold text-blue-400 hover:text-blue-300 transition-colors"
              >
                <span>Explore All {SKILLS.length} Skills</span>
                <ChevronRight className="w-4 h-4" />
              </Link>
            </div>

            <ScrollStagger className="grid grid-cols-2 sm:grid-cols-4 gap-3">
              {topSkills.map((skill) => (
                <div
                  key={skill.name}
                  className="p-3 rounded-xl bg-slate-950/60 dark:bg-slate-950/60 light:bg-slate-50 border border-slate-800/60 dark:border-slate-800/60 light:border-slate-200 flex items-center justify-between hover:border-slate-700 transition-colors"
                >
                  <span className="text-xs font-semibold text-slate-200 dark:text-slate-200 light:text-slate-800">
                    {skill.name}
                  </span>
                  <span className="text-[10px] px-1.5 py-0.5 rounded bg-blue-500/10 text-blue-400 border border-blue-500/20 font-bold">
                    {skill.level}
                  </span>
                </div>
              ))}
            </ScrollStagger>
          </div>
        </ScrollReveal>

        {/* 5. Direct Collaboration Banner */}
        <ScrollReveal yOffset={50}>
          <div className="p-8 sm:p-10 rounded-3xl bg-gradient-to-r from-blue-900/40 via-indigo-900/30 to-slate-900/40 border border-blue-500/30 text-center space-y-4">
            <h3 className="text-2xl sm:text-3xl font-extrabold text-slate-100 dark:text-slate-100 light:text-slate-900">
              Have a project or opportunity in mind?
            </h3>
            <p className="text-xs sm:text-sm text-slate-300 dark:text-slate-300 light:text-slate-600 max-w-lg mx-auto leading-relaxed">
              Feel free to get in touch for software collaboration, Android tool building, or technical discussions.
            </p>
            <div className="pt-2 flex flex-wrap items-center justify-center gap-3">
              <Link
                to="/contact"
                className="inline-flex items-center gap-2 px-6 py-3 rounded-xl bg-blue-600 hover:bg-blue-500 text-white font-bold text-xs shadow-md shadow-blue-600/25 transition-all active:scale-95"
              >
                <Mail className="w-4 h-4" />
                <span>Contact Nahid Directly</span>
              </Link>
              <button
                onClick={onOpenResume}
                className="px-5 py-3 rounded-xl bg-slate-900 hover:bg-slate-800 text-slate-200 text-xs font-semibold border border-slate-800 transition-colors"
              >
                View Resume
              </button>
            </div>
          </div>
        </ScrollReveal>

      </div>
    </div>
  );
};
