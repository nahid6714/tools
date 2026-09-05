import React, { useState } from 'react';
import { motion } from 'motion/react';
import { 
  Briefcase, 
  Calendar, 
  MapPin, 
  CheckCircle2, 
  HelpCircle, 
  Edit3 
} from 'lucide-react';
import { WORK_EXPERIENCE, PERSONAL_INFO } from '../data/portfolioData';
import { SectionHeaderReveal, ScrollReveal } from './ScrollAnimation';

export const Experience: React.FC = () => {
  const [showHelper, setShowHelper] = useState(false);

  return (
    <section id="experience" className="py-20 bg-transparent overflow-hidden">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        
        {/* Section Header */}
        <SectionHeaderReveal
          badge={
            <div className="inline-flex items-center gap-2 px-3 py-1 rounded-full bg-blue-500/10 text-blue-400 text-xs font-semibold tracking-wide uppercase border border-blue-500/20">
              <Briefcase className="w-3.5 h-3.5" />
              <span>Career Pathway</span>
            </div>
          }
          title="Professional Experience"
          description='"Work experience details will be added here."'
        />

        <ScrollReveal yOffset={20} className="flex flex-col items-center mb-12 -mt-4">
          <div className="flex items-center gap-3">
            <span className="text-xs text-slate-500">
              Role status: <strong className="text-slate-300 dark:text-slate-300 light:text-slate-700">{PERSONAL_INFO.role}</strong>
            </span>
            <button
              onClick={() => setShowHelper(!showHelper)}
              className="text-xs text-blue-400 hover:text-blue-300 inline-flex items-center gap-1 underline underline-offset-4"
            >
              <HelpCircle className="w-3.5 h-3.5" />
              <span>{showHelper ? 'Hide formatting details' : 'How to update this section'}</span>
            </button>
          </div>
        </ScrollReveal>

        {/* Developer Guide Callout if toggled */}
        {showHelper && (
          <ScrollReveal yOffset={20}>
            <div className="max-w-3xl mx-auto mb-10 p-4 rounded-xl bg-blue-950/40 dark:bg-blue-950/40 light:bg-blue-50 border border-blue-800/60 dark:border-blue-800/60 light:border-blue-200 text-xs text-slate-300 dark:text-slate-300 light:text-blue-950 space-y-2">
              <div className="flex items-center gap-2 font-bold text-blue-400">
                <Edit3 className="w-4 h-4" />
                <span>Updating Experience Information</span>
              </div>
              <p>
                To update employment history or internships, simply edit <code className="font-mono bg-blue-900/40 px-1 py-0.5 rounded text-blue-300">src/data/portfolioData.ts</code> in the <code className="font-mono bg-blue-900/40 px-1 py-0.5 rounded text-blue-300">WORK_EXPERIENCE</code> array. Add real job titles, companies, durations, and key responsibilities.
              </p>
            </div>
          </ScrollReveal>
        )}

        {/* Experience Timeline Cards with scroll fly-in */}
        <div className="max-w-4xl mx-auto space-y-6">
          {WORK_EXPERIENCE.map((exp, index) => (
            <motion.div
              key={exp.id}
              initial={{ opacity: 0, y: 50, scale: 0.97 }}
              whileInView={{ opacity: 1, y: 0, scale: 1 }}
              viewport={{ once: false, amount: 0.15 }}
              transition={{
                duration: 0.55,
                delay: index * 0.15,
                ease: [0.25, 1, 0.5, 1],
              }}
              className="relative p-6 sm:p-8 rounded-2xl bg-slate-900/60 dark:bg-slate-900/60 light:bg-white border border-slate-800/80 dark:border-slate-800/80 light:border-slate-200 shadow-sm hover:border-slate-700 transition-colors"
            >
              {/* Header badge for status */}
              <div className="flex flex-wrap items-center justify-between gap-3 mb-4">
                <div className="flex items-center gap-2.5">
                  <div className="p-2.5 rounded-xl bg-blue-600/10 text-blue-400 border border-blue-500/20">
                    <Briefcase className="w-5 h-5" />
                  </div>
                  <div>
                    <h3 className="text-lg font-bold text-slate-100 dark:text-slate-100 light:text-slate-900">
                      {exp.position}
                    </h3>
                    <p className="text-sm font-semibold text-blue-400">
                      {exp.company}
                    </p>
                  </div>
                </div>

                <div className="flex flex-col sm:flex-end text-left sm:text-right">
                  <div className="inline-flex items-center gap-1.5 text-xs text-slate-400 dark:text-slate-400 light:text-slate-600 font-medium">
                    <Calendar className="w-3.5 h-3.5 text-indigo-400" />
                    <span>{exp.duration}</span>
                  </div>
                  <div className="inline-flex items-center gap-1.5 text-xs text-slate-500 mt-0.5">
                    <MapPin className="w-3.5 h-3.5 text-rose-400" />
                    <span>{exp.location}</span>
                  </div>
                </div>
              </div>

              {/* Responsibilities */}
              <div className="space-y-3 mt-4 pt-4 border-t border-slate-800/80 dark:border-slate-800/80 light:border-slate-200">
                <h4 className="text-xs font-bold uppercase tracking-wider text-slate-400 dark:text-slate-400 light:text-slate-600">
                  Responsibilities
                </h4>
                <ul className="space-y-2 text-xs sm:text-sm text-slate-300 dark:text-slate-300 light:text-slate-700">
                  {exp.responsibilities.map((resp, idx) => (
                    <li key={idx} className="flex items-start gap-2.5">
                      <span className="text-blue-400 font-bold mt-0.5">•</span>
                      <span>{resp}</span>
                    </li>
                  ))}
                </ul>
              </div>

              {/* Achievements if any */}
              {exp.achievements && exp.achievements.length > 0 && (
                <div className="space-y-2 mt-4 pt-3 border-t border-slate-800/60 dark:border-slate-800/60 light:border-slate-100">
                  <h4 className="text-xs font-bold uppercase tracking-wider text-slate-400 dark:text-slate-400 light:text-slate-600">
                    Achievements & Highlights
                  </h4>
                  <ul className="space-y-1.5 text-xs sm:text-sm text-slate-300 dark:text-slate-300 light:text-slate-700">
                    {exp.achievements.map((ach, idx) => (
                      <li key={idx} className="flex items-start gap-2.5">
                        <CheckCircle2 className="w-4 h-4 text-emerald-400 shrink-0 mt-0.5" />
                        <span>{ach}</span>
                      </li>
                    ))}
                  </ul>
                </div>
              )}

              {/* Notice tag */}
              {exp.isPlaceholder && (
                <div className="mt-4 pt-3 border-t border-slate-800/40 flex items-center justify-between text-[11px] text-slate-500">
                  <span>Structured placeholder ready for verified employer/organization details</span>
                  <span className="text-blue-400 font-semibold">Editable in portfolioData.ts</span>
                </div>
              )}
            </motion.div>
          ))}
        </div>

      </div>
    </section>
  );
};
