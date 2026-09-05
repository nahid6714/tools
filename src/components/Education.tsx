import React from 'react';
import { 
  GraduationCap, 
  Calendar, 
  BookOpen, 
  School, 
  Sparkles,
  Award
} from 'lucide-react';
import { EDUCATION } from '../data/portfolioData';

export const Education: React.FC = () => {
  return (
    <section id="education" className="py-20 bg-slate-950/40 dark:bg-slate-950/40 light:bg-slate-50 border-t border-slate-900 dark:border-slate-900 light:border-slate-200">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        
        {/* Section Header */}
        <div className="flex flex-col items-center text-center mb-16">
          <div className="inline-flex items-center gap-2 px-3 py-1 rounded-full bg-blue-500/10 text-blue-400 text-xs font-semibold tracking-wide uppercase mb-3 border border-blue-500/20">
            <GraduationCap className="w-3.5 h-3.5" />
            <span>Academic Background</span>
          </div>
          <h2 className="text-3xl sm:text-4xl font-bold tracking-tight text-slate-100 dark:text-slate-100 light:text-slate-900">
            Education
          </h2>
          <div className="w-12 h-1 bg-blue-500 rounded-full mt-3 mb-4"></div>
          <p className="max-w-2xl text-slate-400 dark:text-slate-400 light:text-slate-600 text-sm sm:text-base leading-relaxed">
            Formal studies in computer technology, software fundamentals, and practical mobile application development.
          </p>
        </div>

        {/* Education Cards */}
        <div className="max-w-3xl mx-auto space-y-6">
          {EDUCATION.map((edu) => (
            <div
              key={edu.id}
              className="p-6 sm:p-8 rounded-2xl bg-slate-900/60 dark:bg-slate-900/60 light:bg-white border border-slate-800/80 dark:border-slate-800/80 light:border-slate-200 shadow-sm hover:border-slate-700 transition-colors"
            >
              <div className="flex flex-wrap items-start justify-between gap-4 mb-4">
                <div className="flex items-center gap-3.5">
                  <div className="p-3 rounded-xl bg-blue-600/10 text-blue-400 border border-blue-500/20">
                    <School className="w-6 h-6" />
                  </div>
                  <div>
                    <h3 className="text-lg font-bold text-slate-100 dark:text-slate-100 light:text-slate-900">
                      {edu.degree}
                    </h3>
                    <p className="text-sm font-semibold text-blue-400">
                      {edu.institution}
                    </p>
                    <p className="text-xs text-slate-500 dark:text-slate-500 light:text-slate-600 mt-0.5">
                      Field: {edu.field}
                    </p>
                  </div>
                </div>

                <div className="inline-flex items-center gap-1.5 px-3 py-1 rounded-full bg-slate-950 dark:bg-slate-950 light:bg-slate-100 border border-slate-800 text-xs font-medium text-slate-300 dark:text-slate-300 light:text-slate-700">
                  <Calendar className="w-3.5 h-3.5 text-indigo-400" />
                  <span>{edu.year}</span>
                </div>
              </div>

              {/* Details */}
              <div className="mt-4 pt-4 border-t border-slate-800/80 dark:border-slate-800/80 light:border-slate-200">
                <h4 className="text-xs font-bold uppercase tracking-wider text-slate-400 dark:text-slate-400 light:text-slate-600 mb-2">
                  Academic Focus & Coursework
                </h4>
                <p className="text-xs sm:text-sm text-slate-300 dark:text-slate-300 light:text-slate-700 leading-relaxed">
                  {edu.details}
                </p>
              </div>

              {edu.isPlaceholder && (
                <div className="mt-4 pt-3 border-t border-slate-800/50 flex items-center justify-between text-[11px] text-slate-500">
                  <span>Structured placeholder fields ready for your institution name</span>
                  <span className="text-blue-400 font-medium">Editable in portfolioData.ts</span>
                </div>
              )}
            </div>
          ))}
        </div>

      </div>
    </section>
  );
};
