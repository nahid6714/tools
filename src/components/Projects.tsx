import React from 'react';
import { motion } from 'motion/react';
import { 
  FolderGit2, 
  Github, 
  ExternalLink, 
  Smartphone, 
  Download, 
  Globe, 
  Layers, 
  ChevronRight, 
  Sparkles 
} from 'lucide-react';
import { PROJECTS } from '../data/portfolioData';
import { SectionHeaderReveal, CurvedRollItem } from './ScrollAnimation';

interface ProjectsProps {
  onSelectProjectForApk?: (projectId: string) => void;
  onOpenAppSection?: () => void;
}

export const Projects: React.FC<ProjectsProps> = ({ onOpenAppSection }) => {
  return (
    <section id="projects" className="py-20 bg-slate-950/40 dark:bg-slate-950/40 light:bg-slate-50 border-t border-slate-900 dark:border-slate-900 light:border-slate-200 overflow-hidden">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        
        {/* Section Header */}
        <SectionHeaderReveal
          badge={
            <div className="inline-flex items-center gap-2 px-3 py-1 rounded-full bg-blue-500/10 text-blue-400 text-xs font-semibold tracking-wide uppercase border border-blue-500/20">
              <FolderGit2 className="w-3.5 h-3.5" />
              <span>Featured Creations</span>
            </div>
          }
          title="Projects Showcase"
          description="Highlighted Android software utilities, open-source repositories, and responsive web applications built with practical functionality in mind."
        />

        {/* Projects Grid with scroll fly-in & curved roll */}
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
          {PROJECTS.map((project, index) => {
            const isTools = project.id === 'tools';
            return (
              <CurvedRollItem key={project.id}>
                <motion.div
                  initial={{ opacity: 0, y: 50, scale: 0.96 }}
                  whileInView={{ opacity: 1, y: 0, scale: 1 }}
                  viewport={{ once: false, amount: 0.15 }}
                  transition={{
                    duration: 0.55,
                    delay: (index % 3) * 0.1,
                    ease: [0.25, 1, 0.5, 1],
                  }}
                  className={`relative rounded-2xl p-6 flex flex-col justify-between transition-all duration-300 hover:-translate-y-1 group h-full ${
                    isTools
                      ? 'bg-gradient-to-b from-slate-900/90 via-slate-900/70 to-slate-950/90 dark:from-slate-900/90 dark:to-slate-950/90 light:bg-white border-2 border-blue-500/40 shadow-lg shadow-blue-500/5'
                      : 'bg-slate-900/60 dark:bg-slate-900/60 light:bg-white border border-slate-800/80 dark:border-slate-800/80 light:border-slate-200 shadow-sm'
                  }`}
                >
                {/* Featured Badge if applicable */}
                {project.featured && (
                  <div className="absolute top-4 right-4 inline-flex items-center gap-1 px-2.5 py-0.5 rounded-full text-[10px] font-bold bg-blue-500/20 text-blue-400 border border-blue-500/30">
                    <Sparkles className="w-3 h-3" />
                    <span>Featured</span>
                  </div>
                )}

                <div>
                  {/* Category & Icon */}
                  <div className="flex items-center gap-3 mb-4">
                    <div className="p-3 rounded-xl bg-slate-950 dark:bg-slate-950 light:bg-slate-100 border border-slate-800/80 text-blue-400 group-hover:scale-105 transition-transform">
                      {project.category === 'Android App' ? (
                        <Smartphone className="w-6 h-6 text-emerald-400" />
                      ) : project.category === 'Web Development' ? (
                        <Globe className="w-6 h-6 text-sky-400" />
                      ) : (
                        <FolderGit2 className="w-6 h-6 text-indigo-400" />
                      )}
                    </div>
                    <div>
                      <span className="text-[11px] font-semibold uppercase tracking-wider text-slate-500 dark:text-slate-500 light:text-slate-600 block">
                        {project.category}
                      </span>
                      <h3 className="text-lg font-bold text-slate-100 dark:text-slate-100 light:text-slate-900 group-hover:text-blue-400 transition-colors">
                        {project.name}
                      </h3>
                    </div>
                  </div>

                  {/* Description */}
                  <p className="text-sm text-slate-300 dark:text-slate-300 light:text-slate-700 leading-relaxed mb-4">
                    {project.description}
                  </p>

                  {/* Long description / features preview if available */}
                  {project.longDescription && (
                    <p className="text-xs text-slate-400 dark:text-slate-400 light:text-slate-600 leading-relaxed mb-4 bg-slate-950/40 dark:bg-slate-950/40 light:bg-slate-50 p-3 rounded-xl border border-slate-800/50 dark:border-slate-800/50 light:border-slate-200">
                      {project.longDescription}
                    </p>
                  )}

                  {/* Technologies Tags */}
                  <div className="flex flex-wrap gap-1.5 mb-6">
                    {project.technologies.map((tech) => (
                      <span
                        key={tech}
                        className="px-2.5 py-1 rounded-lg text-[11px] font-medium bg-slate-950/70 dark:bg-slate-950/70 light:bg-slate-100 text-slate-300 dark:text-slate-300 light:text-slate-700 border border-slate-800/60 dark:border-slate-800/60 light:border-slate-200"
                      >
                        {tech}
                      </span>
                    ))}
                  </div>
                </div>

                {/* Actions Footer */}
                <div className="pt-4 border-t border-slate-800/80 dark:border-slate-800/80 light:border-slate-200 flex flex-wrap items-center gap-2">
                  
                  {/* GitHub Button */}
                  {project.githubUrl && (
                    <a
                      href={project.githubUrl}
                      target="_blank"
                      rel="noopener noreferrer"
                      className="flex-1 min-w-[100px] inline-flex items-center justify-center gap-1.5 px-3 py-2 rounded-xl text-xs font-semibold bg-slate-950 dark:bg-slate-950 light:bg-slate-100 hover:bg-slate-800 dark:hover:bg-slate-800 light:hover:bg-slate-200 text-slate-200 dark:text-slate-200 light:text-slate-800 border border-slate-800 dark:border-slate-800 light:border-slate-300 transition-colors"
                    >
                      <Github className="w-3.5 h-3.5" />
                      <span>GitHub</span>
                    </a>
                  )}

                  {/* Live Demo Button if available */}
                  {project.liveUrl && (
                    <a
                      href={project.liveUrl}
                      target="_blank"
                      rel="noopener noreferrer"
                      className="flex-1 min-w-[110px] inline-flex items-center justify-center gap-1.5 px-3 py-2 rounded-xl text-xs font-semibold bg-sky-600 hover:bg-sky-500 text-white shadow-sm shadow-sky-600/20 transition-colors"
                    >
                      <ExternalLink className="w-3.5 h-3.5" />
                      <span>Live Demo</span>
                    </a>
                  )}

                  {/* APK Button if applicable */}
                  {project.apkUrl && (
                    <a
                      href={project.apkUrl}
                      download="app-release.apk"
                      className="flex-1 min-w-[120px] inline-flex items-center justify-center gap-1.5 px-3 py-2 rounded-xl text-xs font-semibold bg-emerald-600 hover:bg-emerald-500 text-white shadow-sm shadow-emerald-600/20 transition-colors"
                      title="Download APK Directly"
                    >
                      <Download className="w-3.5 h-3.5" />
                      <span>Download APK</span>
                    </a>
                  )}

                  {/* Deep dive into My Apps link */}
                  {isTools && (
                    <button
                      onClick={onOpenAppSection}
                      className="w-full mt-2 inline-flex items-center justify-center gap-1 text-[11px] font-semibold text-blue-400 hover:text-blue-300 transition-colors"
                    >
                      <span>View releases & details in My Apps section</span>
                      <ChevronRight className="w-3.5 h-3.5" />
                    </button>
                  )}
                </div>
              </motion.div>
            </CurvedRollItem>
          );
        })}
        </div>

      </div>
    </section>
  );
};
