import React from 'react';
import { motion } from 'motion/react';
import { 
  User, 
  MapPin, 
  Briefcase, 
  Code, 
  Smartphone, 
  Globe, 
  Github, 
  Cpu, 
  Wrench, 
  Sparkles, 
  Terminal, 
  CheckCircle2 
} from 'lucide-react';
import { PERSONAL_INFO, INTERESTS } from '../data/portfolioData';
import { SectionHeaderReveal, ScrollReveal, ScrollStagger } from './ScrollAnimation';

const iconMap: Record<string, React.ReactNode> = {
  code: <Code className="w-5 h-5 text-blue-400" />,
  smartphone: <Smartphone className="w-5 h-5 text-emerald-400" />,
  globe: <Globe className="w-5 h-5 text-sky-400" />,
  github: <Github className="w-5 h-5 text-purple-400" />,
  cpu: <Cpu className="w-5 h-5 text-amber-400" />,
  wrench: <Wrench className="w-5 h-5 text-rose-400" />,
};

export const About: React.FC = () => {
  return (
    <section id="about" className="py-20 bg-slate-950/60 dark:bg-slate-950/60 light:bg-slate-50 border-t border-b border-slate-900 dark:border-slate-900 light:border-slate-200 overflow-hidden">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        
        {/* Section Header with dynamic scroll entrance & vanish */}
        <SectionHeaderReveal
          badge={
            <div className="inline-flex items-center gap-2 px-3 py-1 rounded-full bg-blue-500/10 text-blue-400 text-xs font-semibold tracking-wide uppercase border border-blue-500/20">
              <User className="w-3.5 h-3.5" />
              <span>Profile & Background</span>
            </div>
          }
          title="About Me"
          description="A dedicated technology enthusiast balancing academic pursuits and professional work to build impactful Android applications and software tools."
        />

        <div className="grid grid-cols-1 lg:grid-cols-12 gap-10 items-start">
          
          {/* Biography Narrative - flies in from left/bottom */}
          <ScrollReveal yOffset={45} className="lg:col-span-6 space-y-6">
            <div className="p-6 sm:p-8 rounded-2xl bg-slate-900/70 dark:bg-slate-900/70 light:bg-white border border-slate-800/80 dark:border-slate-800/80 light:border-slate-200 shadow-sm space-y-4">
              <h3 className="text-xl font-bold text-slate-100 dark:text-slate-100 light:text-slate-900 flex items-center gap-2.5">
                <Terminal className="w-5 h-5 text-blue-400" />
                <span>Developer, Student & Worker</span>
              </h3>

              <p className="text-slate-300 dark:text-slate-300 light:text-slate-700 text-sm sm:text-base leading-relaxed">
                Hello! My name is <strong className="text-white dark:text-white light:text-slate-950 font-semibold">{PERSONAL_INFO.name}</strong>. I am a student and worker living in <strong className="text-white dark:text-white light:text-slate-950 font-semibold">{PERSONAL_INFO.country}</strong> who is genuinely passionate about computer technology, software development, Android applications, and building useful digital utilities.
              </p>

              <p className="text-slate-300 dark:text-slate-300 light:text-slate-700 text-sm sm:text-base leading-relaxed">
                I spend my time developing practical software solutions—combining native Android technologies like Kotlin, Jetpack Compose, and Room Database with automated GitHub Actions workflows for continuous APK build and release management.
              </p>

              <p className="text-slate-300 dark:text-slate-300 light:text-slate-700 text-sm sm:text-base leading-relaxed">
                Rather than theoretical mockups, my focus is on functional, real-world utility: building tools that assist with document scanning, expense tracking, daily calculations, and streamlined web experiences.
              </p>

              {/* Verified Profile Matrix */}
              <div className="grid grid-cols-2 sm:grid-cols-3 gap-3 pt-4 border-t border-slate-800/80 dark:border-slate-800/80 light:border-slate-200">
                <div className="p-3 rounded-xl bg-slate-950/70 dark:bg-slate-950/70 light:bg-slate-50 border border-slate-800/60 dark:border-slate-800/60 light:border-slate-200">
                  <span className="text-[11px] uppercase tracking-wider text-slate-500 font-semibold block mb-1">
                    Location
                  </span>
                  <div className="flex items-center gap-1.5 text-xs font-semibold text-slate-200 dark:text-slate-200 light:text-slate-800">
                    <MapPin className="w-3.5 h-3.5 text-rose-400 shrink-0" />
                    <span>{PERSONAL_INFO.country}</span>
                  </div>
                </div>

                <div className="p-3 rounded-xl bg-slate-950/70 dark:bg-slate-950/70 light:bg-slate-50 border border-slate-800/60 dark:border-slate-800/60 light:border-slate-200">
                  <span className="text-[11px] uppercase tracking-wider text-slate-500 font-semibold block mb-1">
                    Current Role
                  </span>
                  <div className="flex items-center gap-1.5 text-xs font-semibold text-slate-200 dark:text-slate-200 light:text-slate-800">
                    <Briefcase className="w-3.5 h-3.5 text-blue-400 shrink-0" />
                    <span>{PERSONAL_INFO.role}</span>
                  </div>
                </div>

                <div className="p-3 rounded-xl bg-slate-950/70 dark:bg-slate-950/70 light:bg-slate-50 border border-slate-800/60 dark:border-slate-800/60 light:border-slate-200 col-span-2 sm:col-span-1">
                  <span className="text-[11px] uppercase tracking-wider text-slate-500 font-semibold block mb-1">
                    Core Focus
                  </span>
                  <div className="flex items-center gap-1.5 text-xs font-semibold text-emerald-400">
                    <CheckCircle2 className="w-3.5 h-3.5 shrink-0" />
                    <span>Android & Tools</span>
                  </div>
                </div>
              </div>
            </div>
          </ScrollReveal>

          {/* Currently Exploring / Interests Cards - staggered fly-in */}
          <div className="lg:col-span-6 space-y-4">
            <ScrollReveal yOffset={30}>
              <div className="flex items-center justify-between mb-2">
                <h3 className="text-lg font-bold text-slate-100 dark:text-slate-100 light:text-slate-900 flex items-center gap-2">
                  <Sparkles className="w-4 h-4 text-amber-400" />
                  <span>Currently Exploring & Core Interests</span>
                </h3>
                <span className="text-xs text-slate-500 font-medium">6 Focus Areas</span>
              </div>
            </ScrollReveal>

            <ScrollStagger className="grid grid-cols-1 sm:grid-cols-2 gap-3.5">
              {INTERESTS.map((item) => (
                <div
                  key={item.title}
                  className="p-4 rounded-xl bg-slate-900/60 dark:bg-slate-900/60 light:bg-white border border-slate-800/70 dark:border-slate-800/70 light:border-slate-200 hover:border-slate-700 transition-all duration-200 hover:-translate-y-0.5 group"
                >
                  <div className="flex items-center gap-3 mb-2">
                    <div className="p-2 rounded-lg bg-slate-950 dark:bg-slate-950 light:bg-slate-100 border border-slate-800/80 group-hover:scale-105 transition-transform">
                      {iconMap[item.icon] || <Code className="w-5 h-5 text-blue-400" />}
                    </div>
                    <h4 className="text-sm font-semibold text-slate-200 dark:text-slate-200 light:text-slate-800 group-hover:text-blue-400 transition-colors">
                      {item.title}
                    </h4>
                  </div>
                  <p className="text-xs text-slate-400 dark:text-slate-400 light:text-slate-600 leading-relaxed pl-1">
                    {item.description}
                  </p>
                </div>
              ))}
            </ScrollStagger>
          </div>

        </div>
      </div>
    </section>
  );
};
