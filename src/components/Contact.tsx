import React, { useState } from 'react';
import { motion } from 'motion/react';
import { 
  Mail, 
  Send, 
  Github, 
  Linkedin, 
  Facebook, 
  Check, 
  Copy, 
  MapPin, 
  MessageSquare, 
  Clock, 
  Sparkles, 
  ExternalLink, 
  AlertCircle, 
  CheckCircle2 
} from 'lucide-react';
import { PERSONAL_INFO } from '../data/portfolioData';
import { ContactFormData } from '../types/portfolio';
import { SectionHeaderReveal, ScrollReveal } from './ScrollAnimation';

interface ContactProps {
  onShowToast: (message: string, type?: 'info' | 'success' | 'warning') => void;
}

export const Contact: React.FC<ContactProps> = ({ onShowToast }) => {
  const [formData, setFormData] = useState<ContactFormData>({
    name: '',
    email: '',
    subject: '',
    message: '',
  });

  const [errors, setErrors] = useState<Partial<ContactFormData>>({});
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [isSubmitted, setIsSubmitted] = useState(false);
  const [isCopied, setIsCopied] = useState(false);

  const validate = (): boolean => {
    const newErrors: Partial<ContactFormData> = {};

    if (!formData.name.trim()) {
      newErrors.name = 'Please enter your name.';
    }

    if (!formData.email.trim()) {
      newErrors.email = 'Please enter your email address.';
    } else if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(formData.email)) {
      newErrors.email = 'Please provide a valid email address.';
    }

    if (!formData.subject.trim()) {
      newErrors.subject = 'Please enter a subject.';
    }

    if (!formData.message.trim() || formData.message.length < 10) {
      newErrors.message = 'Please provide a message with at least 10 characters.';
    }

    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  const handleChange = (e: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement>) => {
    const { name, value } = e.target;
    setFormData((prev) => ({ ...prev, [name]: value }));
    if (errors[name as keyof ContactFormData]) {
      setErrors((prev) => ({ ...prev, [name]: undefined }));
    }
  };

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();

    if (!validate()) {
      onShowToast('Please complete the required form fields', 'warning');
      return;
    }

    setIsSubmitting(true);

    // Simulate reliable dispatch
    setTimeout(() => {
      setIsSubmitting(false);
      setIsSubmitted(true);
      onShowToast('Your message has been sent successfully!', 'success');
    }, 800);
  };

  const handleCopyEmail = () => {
    navigator.clipboard.writeText(PERSONAL_INFO.email);
    setIsCopied(true);
    onShowToast(`Email address copied: ${PERSONAL_INFO.email}`, 'success');
    setTimeout(() => setIsCopied(false), 3000);
  };

  const openEmailClient = () => {
    const mailto = `mailto:${PERSONAL_INFO.email}?subject=${encodeURIComponent(
      formData.subject || 'Project Inquiry'
    )}&body=${encodeURIComponent(
      `Hi Nahid,\n\n${formData.message}\n\nFrom: ${formData.name} (${formData.email})`
    )}`;
    window.location.href = mailto;
  };

  return (
    <section
      id="contact"
      className="py-24 bg-slate-950/70 dark:bg-slate-950/70 light:bg-slate-50 border-t border-slate-900 dark:border-slate-900 light:border-slate-200 relative"
    >
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 relative z-10">
        
        {/* Section Header */}
        <SectionHeaderReveal
          badge={
            <div className="inline-flex items-center gap-2 px-3 py-1 rounded-full bg-blue-500/10 text-blue-400 text-xs font-semibold tracking-wide uppercase border border-blue-500/20">
              <Mail className="w-3.5 h-3.5" />
              <span>Direct Inquiries</span>
            </div>
          }
          title="Let's Work Together"
          description='"Have a project, idea, or opportunity? Feel free to get in touch."'
        />

        <div className="grid grid-cols-1 lg:grid-cols-12 gap-12 items-start">
          
          {/* Left Column: Direct Info & Social Channels */}
          <ScrollReveal yOffset={45} className="lg:col-span-5 space-y-6">
            
            <div className="p-6 rounded-2xl bg-slate-900/60 dark:bg-slate-900/60 light:bg-white border border-slate-800/80 dark:border-slate-800/80 light:border-slate-200 shadow-sm space-y-5">
              <h3 className="text-lg font-bold text-slate-100 dark:text-slate-100 light:text-slate-900">
                Contact Channels
              </h3>

              {/* Email Card with Copy Button */}
              <div className="p-4 rounded-xl bg-slate-950/70 dark:bg-slate-950/70 light:bg-slate-50 border border-slate-800/80 dark:border-slate-800/80 light:border-slate-200">
                <span className="text-[11px] font-semibold uppercase tracking-wider text-slate-400 block mb-1">
                  Primary Email
                </span>
                <div className="flex items-center justify-between gap-2">
                  <a
                    href={`mailto:${PERSONAL_INFO.email}`}
                    className="text-xs sm:text-sm font-semibold text-blue-400 hover:underline break-all"
                  >
                    {PERSONAL_INFO.email}
                  </a>
                  <button
                    onClick={handleCopyEmail}
                    className="p-1.5 rounded-lg bg-slate-900 dark:bg-slate-900 light:bg-slate-200 hover:bg-slate-800 text-slate-300 dark:text-slate-300 light:text-slate-700 hover:text-white transition-colors shrink-0"
                    title="Copy email to clipboard"
                    aria-label="Copy email"
                  >
                    {isCopied ? <Check className="w-4 h-4 text-emerald-400" /> : <Copy className="w-4 h-4" />}
                  </button>
                </div>
              </div>

              {/* Location Card */}
              <div className="p-4 rounded-xl bg-slate-950/70 dark:bg-slate-950/70 light:bg-slate-50 border border-slate-800/80 dark:border-slate-800/80 light:border-slate-200">
                <span className="text-[11px] font-semibold uppercase tracking-wider text-slate-400 block mb-1">
                  Location & Timezone
                </span>
                <div className="flex items-center gap-2 text-xs sm:text-sm font-medium text-slate-200 dark:text-slate-200 light:text-slate-800">
                  <MapPin className="w-4 h-4 text-rose-400 shrink-0" />
                  <span>Bangladesh (BST, UTC+6)</span>
                </div>
              </div>

              {/* Social Channels Row */}
              <div>
                <span className="text-[11px] font-semibold uppercase tracking-wider text-slate-400 block mb-3">
                  Online Profiles
                </span>
                <div className="grid grid-cols-2 gap-3">
                  <a
                    href={PERSONAL_INFO.githubUrl}
                    target="_blank"
                    rel="noopener noreferrer"
                    className="flex items-center gap-2.5 p-3 rounded-xl bg-slate-950/70 dark:bg-slate-950/70 light:bg-slate-50 hover:bg-slate-800/80 border border-slate-800/80 dark:border-slate-800/80 light:border-slate-200 text-xs font-semibold text-slate-300 dark:text-slate-300 light:text-slate-700 hover:text-white transition-colors"
                  >
                    <Github className="w-4 h-4 text-purple-400" />
                    <span>GitHub</span>
                  </a>

                  <a
                    href={PERSONAL_INFO.linkedinUrl}
                    target="_blank"
                    rel="noopener noreferrer"
                    className="flex items-center gap-2.5 p-3 rounded-xl bg-slate-950/70 dark:bg-slate-950/70 light:bg-slate-50 hover:bg-slate-800/80 border border-slate-800/80 dark:border-slate-800/80 light:border-slate-200 text-xs font-semibold text-slate-300 dark:text-slate-300 light:text-slate-700 hover:text-white transition-colors"
                  >
                    <Linkedin className="w-4 h-4 text-sky-400" />
                    <span>LinkedIn</span>
                  </a>

                  <a
                    href={PERSONAL_INFO.facebookUrl}
                    target="_blank"
                    rel="noopener noreferrer"
                    className="flex items-center gap-2.5 p-3 rounded-xl bg-slate-950/70 dark:bg-slate-950/70 light:bg-slate-50 hover:bg-slate-800/80 border border-slate-800/80 dark:border-slate-800/80 light:border-slate-200 text-xs font-semibold text-slate-300 dark:text-slate-300 light:text-slate-700 hover:text-white transition-colors"
                  >
                    <Facebook className="w-4 h-4 text-blue-500" />
                    <span>Facebook</span>
                  </a>

                  <a
                    href={`mailto:${PERSONAL_INFO.email}`}
                    className="flex items-center gap-2.5 p-3 rounded-xl bg-slate-950/70 dark:bg-slate-950/70 light:bg-slate-50 hover:bg-slate-800/80 border border-slate-800/80 dark:border-slate-800/80 light:border-slate-200 text-xs font-semibold text-slate-300 dark:text-slate-300 light:text-slate-700 hover:text-white transition-colors"
                  >
                    <Mail className="w-4 h-4 text-emerald-400" />
                    <span>Email Direct</span>
                  </a>
                </div>
              </div>
            </div>

          </ScrollReveal>

          {/* Right Column: Contact Form */}
          <ScrollReveal yOffset={45} delay={0.12} className="lg:col-span-7">
            <div className="p-6 sm:p-8 rounded-2xl bg-slate-900/60 dark:bg-slate-900/60 light:bg-white border border-slate-800/80 dark:border-slate-800/80 light:border-slate-200 shadow-sm">
              
              {isSubmitted ? (
                <div className="py-12 text-center space-y-4 animate-in fade-in zoom-in-95 duration-300">
                  <div className="w-14 h-14 mx-auto rounded-full bg-emerald-500/10 text-emerald-400 flex items-center justify-center border border-emerald-500/20">
                    <CheckCircle2 className="w-8 h-8" />
                  </div>
                  <h3 className="text-xl font-bold text-slate-100 dark:text-slate-100 light:text-slate-900">
                    Message Sent Successfully!
                  </h3>
                  <p className="text-sm text-slate-400 dark:text-slate-400 light:text-slate-600 max-w-md mx-auto">
                    Thank you for reaching out, <strong>{formData.name}</strong>. Nahid Hossain will respond to your inquiry at <strong>{formData.email}</strong> shortly.
                  </p>
                  <div className="pt-4 flex flex-wrap items-center justify-center gap-3">
                    <button
                      onClick={openEmailClient}
                      className="inline-flex items-center gap-2 px-4 py-2 rounded-xl bg-blue-600 hover:bg-blue-500 text-white text-xs font-semibold shadow-md transition-colors"
                    >
                      <ExternalLink className="w-3.5 h-3.5" />
                      <span>Open in Email App</span>
                    </button>
                    <button
                      onClick={() => {
                        setIsSubmitted(false);
                        setFormData({ name: '', email: '', subject: '', message: '' });
                      }}
                      className="px-4 py-2 rounded-xl bg-slate-800 hover:bg-slate-700 text-slate-300 text-xs font-semibold transition-colors"
                    >
                      Send Another Note
                    </button>
                  </div>
                </div>
              ) : (
                <form onSubmit={handleSubmit} noValidate className="space-y-4">
                  <h3 className="text-lg font-bold text-slate-100 dark:text-slate-100 light:text-slate-900 mb-2">
                    Send a Message
                  </h3>

                  <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
                    {/* Name Field */}
                    <div>
                      <label htmlFor="contact-name" className="block text-xs font-semibold text-slate-300 dark:text-slate-300 light:text-slate-700 mb-1.5">
                        Your Name <span className="text-rose-400">*</span>
                      </label>
                      <input
                        id="contact-name"
                        type="text"
                        name="name"
                        value={formData.name}
                        onChange={handleChange}
                        placeholder="John Doe"
                        className={`w-full px-3.5 py-2.5 rounded-xl bg-slate-950/80 dark:bg-slate-950/80 light:bg-slate-50 border text-slate-100 dark:text-slate-100 light:text-slate-900 text-sm focus:outline-none focus:ring-2 focus:ring-blue-500 transition-colors ${
                          errors.name
                            ? 'border-rose-500/80'
                            : 'border-slate-800 dark:border-slate-800 light:border-slate-300'
                        }`}
                      />
                      {errors.name && (
                        <p className="mt-1 text-[11px] text-rose-400 flex items-center gap-1">
                          <AlertCircle className="w-3 h-3" />
                          <span>{errors.name}</span>
                        </p>
                      )}
                    </div>

                    {/* Email Field */}
                    <div>
                      <label htmlFor="contact-email" className="block text-xs font-semibold text-slate-300 dark:text-slate-300 light:text-slate-700 mb-1.5">
                        Your Email <span className="text-rose-400">*</span>
                      </label>
                      <input
                        id="contact-email"
                        type="email"
                        name="email"
                        value={formData.email}
                        onChange={handleChange}
                        placeholder="john@example.com"
                        className={`w-full px-3.5 py-2.5 rounded-xl bg-slate-950/80 dark:bg-slate-950/80 light:bg-slate-50 border text-slate-100 dark:text-slate-100 light:text-slate-900 text-sm focus:outline-none focus:ring-2 focus:ring-blue-500 transition-colors ${
                          errors.email
                            ? 'border-rose-500/80'
                            : 'border-slate-800 dark:border-slate-800 light:border-slate-300'
                        }`}
                      />
                      {errors.email && (
                        <p className="mt-1 text-[11px] text-rose-400 flex items-center gap-1">
                          <AlertCircle className="w-3 h-3" />
                          <span>{errors.email}</span>
                        </p>
                      )}
                    </div>
                  </div>

                  {/* Subject Field */}
                  <div>
                    <label htmlFor="contact-subject" className="block text-xs font-semibold text-slate-300 dark:text-slate-300 light:text-slate-700 mb-1.5">
                      Subject <span className="text-rose-400">*</span>
                    </label>
                    <input
                      id="contact-subject"
                      type="text"
                      name="subject"
                      value={formData.subject}
                      onChange={handleChange}
                      placeholder="Project Collaboration / Inquiries"
                      className={`w-full px-3.5 py-2.5 rounded-xl bg-slate-950/80 dark:bg-slate-950/80 light:bg-slate-50 border text-slate-100 dark:text-slate-100 light:text-slate-900 text-sm focus:outline-none focus:ring-2 focus:ring-blue-500 transition-colors ${
                        errors.subject
                          ? 'border-rose-500/80'
                          : 'border-slate-800 dark:border-slate-800 light:border-slate-300'
                      }`}
                    />
                    {errors.subject && (
                      <p className="mt-1 text-[11px] text-rose-400 flex items-center gap-1">
                        <AlertCircle className="w-3 h-3" />
                        <span>{errors.subject}</span>
                      </p>
                    )}
                  </div>

                  {/* Message Field */}
                  <div>
                    <label htmlFor="contact-message" className="block text-xs font-semibold text-slate-300 dark:text-slate-300 light:text-slate-700 mb-1.5">
                      Message <span className="text-rose-400">*</span>
                    </label>
                    <textarea
                      id="contact-message"
                      name="message"
                      rows={5}
                      value={formData.message}
                      onChange={handleChange}
                      placeholder="Write your note, idea, or questions here..."
                      className={`w-full px-3.5 py-2.5 rounded-xl bg-slate-950/80 dark:bg-slate-950/80 light:bg-slate-50 border text-slate-100 dark:text-slate-100 light:text-slate-900 text-sm focus:outline-none focus:ring-2 focus:ring-blue-500 transition-colors resize-y ${
                        errors.message
                          ? 'border-rose-500/80'
                          : 'border-slate-800 dark:border-slate-800 light:border-slate-300'
                      }`}
                    />
                    {errors.message && (
                      <p className="mt-1 text-[11px] text-rose-400 flex items-center gap-1">
                        <AlertCircle className="w-3 h-3" />
                        <span>{errors.message}</span>
                      </p>
                    )}
                  </div>

                  <button
                    id="contact-submit-btn"
                    type="submit"
                    disabled={isSubmitting}
                    className="w-full py-3 px-6 rounded-xl bg-blue-600 hover:bg-blue-500 disabled:bg-blue-800 text-white font-bold text-sm shadow-md shadow-blue-600/25 flex items-center justify-center gap-2 transition-all duration-200 hover:-translate-y-0.5"
                  >
                    <Send className={`w-4 h-4 ${isSubmitting ? 'animate-pulse' : ''}`} />
                    <span>{isSubmitting ? 'Sending Message...' : 'Send Message'}</span>
                  </button>
                </form>
              )}

            </div>
          </ScrollReveal>

        </div>

      </div>
    </section>
  );
};
