import React, { useState } from 'react';
import { motion } from 'motion/react';
import { 
  Award, 
  Building2, 
  ShieldCheck, 
  Eye,
  Calendar
} from 'lucide-react';
import { CERTIFICATES } from '../data/portfolioData';
import { CertificateItem } from '../types/portfolio';
import { SectionHeaderReveal, ScrollReveal } from './ScrollAnimation';

export const Certificates: React.FC = () => {
  const [showTemplatePreview, setShowTemplatePreview] = useState(false);

  // Template sample to show how certificate cards render when populated
  const templateSample: CertificateItem = {
    id: 'template-cert',
    name: 'Android App Development & Kotlin Specialization',
    organization: 'Recognized Tech Academy / Platform',
    date: 'Verification Pending',
    credentialUrl: '#',
    isPlaceholder: true,
  };

  const hasCertificates = CERTIFICATES.length > 0;

  return (
    <section id="certificates" className="py-20 bg-transparent overflow-hidden">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        
        {/* Section Header */}
        <SectionHeaderReveal
          badge={
            <div className="inline-flex items-center gap-2 px-3 py-1 rounded-full bg-blue-500/10 text-blue-400 text-xs font-semibold tracking-wide uppercase border border-blue-500/20">
              <Award className="w-3.5 h-3.5" />
              <span>Credentials & Accreditations</span>
            </div>
          }
          title="Certifications"
          description="Verified course certifications, workshops, and technical credentials."
        />

        {/* Dynamic Empty State or Actual Cards */}
        {!hasCertificates ? (
          <ScrollReveal yOffset={40} className="max-w-2xl mx-auto">
            {/* Elegant Modern Empty State */}
            <div className="text-center p-8 sm:p-12 rounded-2xl bg-slate-900/40 dark:bg-slate-900/40 light:bg-white border-2 border-dashed border-slate-800 dark:border-slate-800 light:border-slate-300">
              <div className="w-16 h-16 mx-auto mb-4 rounded-2xl bg-slate-900 dark:bg-slate-900 light:bg-slate-100 flex items-center justify-center text-slate-500 border border-slate-800">
                <Award className="w-8 h-8 text-blue-400/80" />
              </div>

              {/* Exact requested text */}
              <h3 className="text-lg font-bold text-slate-200 dark:text-slate-200 light:text-slate-800 mb-2">
                Certificates will be added here.
              </h3>

              <p className="text-xs sm:text-sm text-slate-400 dark:text-slate-400 light:text-slate-600 max-w-md mx-auto leading-relaxed mb-6">
                Nahid Hossain is currently completing coursework in software technologies and Android application development. Official credentials and certificates will be uploaded upon completion.
              </p>

              <button
                onClick={() => setShowTemplatePreview(!showTemplatePreview)}
                className="inline-flex items-center gap-2 px-4 py-2 rounded-xl bg-slate-900 dark:bg-slate-900 light:bg-slate-100 hover:bg-slate-850 text-xs font-semibold text-blue-400 border border-slate-800 transition-colors"
              >
                <Eye className="w-3.5 h-3.5" />
                <span>{showTemplatePreview ? 'Hide Card Format' : 'Preview Certificate Card Format'}</span>
              </button>
            </div>

            {/* Optional Card Preview Structure if toggled */}
            {showTemplatePreview && (
              <motion.div
                initial={{ opacity: 0, y: 20 }}
                animate={{ opacity: 1, y: 0 }}
                transition={{ duration: 0.3 }}
                className="mt-8"
              >
                <div className="p-6 rounded-2xl bg-slate-900/80 dark:bg-slate-900/80 light:bg-white border border-blue-500/30 shadow-lg">
                  <div className="flex items-center justify-between text-[11px] font-semibold text-blue-400 uppercase tracking-wider mb-4">
                    <span>Card Layout Structure</span>
                    <span className="bg-blue-500/10 px-2 py-0.5 rounded">Ready for PDF / Image</span>
                  </div>

                  <div className="flex flex-col sm:flex-row items-start sm:items-center justify-between gap-4">
                    <div className="flex items-center gap-3.5">
                      <div className="p-3 rounded-xl bg-emerald-500/10 text-emerald-400 border border-emerald-500/20">
                        <ShieldCheck className="w-6 h-6" />
                      </div>
                      <div>
                        <h4 className="text-base font-bold text-slate-100 dark:text-slate-100 light:text-slate-900">
                          {templateSample.name}
                        </h4>
                        <div className="flex items-center gap-3 text-xs text-slate-400 mt-1">
                          <span className="flex items-center gap-1">
                            <Building2 className="w-3 h-3 text-indigo-400" />
                            {templateSample.organization}
                          </span>
                          <span>•</span>
                          <span className="flex items-center gap-1">
                            <Calendar className="w-3 h-3 text-slate-400" />
                            {templateSample.date}
                          </span>
                        </div>
                      </div>
                    </div>
                  </div>
                </div>
              </motion.div>
            )}
          </ScrollReveal>
        ) : (
          <div className="grid grid-cols-1 md:grid-cols-2 gap-6 max-w-4xl mx-auto">
            {CERTIFICATES.map((cert, index) => (
              <motion.div
                key={cert.id}
                initial={{ opacity: 0, y: 45 }}
                whileInView={{ opacity: 1, y: 0 }}
                viewport={{ once: false, amount: 0.15 }}
                transition={{ duration: 0.5, delay: index * 0.1 }}
                className="p-6 rounded-2xl bg-slate-900/60 border border-slate-800"
              >
                <h4 className="font-bold text-slate-100">{cert.name}</h4>
                <p className="text-xs text-slate-400 mt-1">{cert.organization} • {cert.date}</p>
              </motion.div>
            ))}
          </div>
        )}

      </div>
    </section>
  );
};
