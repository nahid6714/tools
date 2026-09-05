import React, { useState, useEffect } from 'react';
import { motion } from 'motion/react';
import { 
  Smartphone, 
  Download, 
  Github, 
  RefreshCw, 
  Calendar, 
  HardDrive, 
  Tag, 
  Sparkles, 
  CheckCircle2, 
  AlertCircle, 
  ChevronDown, 
  ChevronUp, 
  ExternalLink,
  ShieldCheck,
  GitPullRequest,
  Layers,
  ArrowRight,
  Info
} from 'lucide-react';
import { APPS_CONFIG } from '../data/appsConfig';
import { AppRepoConfig, AppReleaseInfo } from '../types/portfolio';
import { fetchLatestRelease, triggerDirectApkDownload } from '../services/githubReleaseService';
import { SectionHeaderReveal, ScrollReveal, CurvedRollItem } from './ScrollAnimation';

interface AppsSectionProps {
  onShowToast: (message: string, type?: 'info' | 'success' | 'warning') => void;
}

export const AppsSection: React.FC<AppsSectionProps> = ({ onShowToast }) => {
  // Map of repo ID to loaded release info
  const [appReleases, setAppReleases] = useState<Record<string, AppReleaseInfo>>(() => {
    const initial: Record<string, AppReleaseInfo> = {};
    APPS_CONFIG.forEach((app) => {
      initial[app.id] = app.defaultRelease;
    });
    return initial;
  });

  const [loadingState, setLoadingState] = useState<Record<string, boolean>>({});
  const [liveSyncedState, setLiveSyncedState] = useState<Record<string, boolean>>({});
  const [expandedNotes, setExpandedNotes] = useState<Record<string, boolean>>({
    'tools-app': true, // Open Tools changelog by default
  });
  const [isRefreshingAll, setIsRefreshingAll] = useState(false);

  // Auto-fetch latest release on mount for active repositories (e.g. tools)
  useEffect(() => {
    fetchReleaseForApp(APPS_CONFIG[0], false);
  }, []);

  const fetchReleaseForApp = async (app: AppRepoConfig, showToastOnComplete = true) => {
    setLoadingState((prev) => ({ ...prev, [app.id]: true }));

    try {
      const { release, isLive, error } = await fetchLatestRelease(
        app.repoOwner,
        app.repoName,
        app.defaultRelease
      );

      setAppReleases((prev) => ({ ...prev, [app.id]: release }));
      setLiveSyncedState((prev) => ({ ...prev, [app.id]: isLive }));

      if (showToastOnComplete) {
        if (isLive) {
          onShowToast(`Latest release ${release.version} fetched directly from GitHub!`, 'success');
        } else {
          onShowToast(`Using cached release configuration: ${error || 'Offline'}`, 'info');
        }
      }
    } catch (err) {
      console.error('Error fetching release:', err);
      if (showToastOnComplete) {
        onShowToast('Could not reach GitHub API, using local release cache', 'warning');
      }
    } finally {
      setLoadingState((prev) => ({ ...prev, [app.id]: false }));
    }
  };

  const handleRefreshAll = async () => {
    setIsRefreshingAll(true);
    for (const app of APPS_CONFIG) {
      await fetchReleaseForApp(app, false);
    }
    setIsRefreshingAll(false);
    onShowToast('GitHub repositories synchronization complete!', 'success');
  };

  const handleDownloadApk = (app: AppRepoConfig) => {
    const release = appReleases[app.id] || app.defaultRelease;

    if (app.status !== 'available') {
      onShowToast(
        `${app.appName} is currently in development. Pre-release builds will be published to GitHub soon!`,
        'info'
      );
      return;
    }

    onShowToast(`Initiating direct download for ${release.apkFileName} (${release.apkSize})...`, 'success');
    triggerDirectApkDownload(release.downloadUrl, release.apkFileName);
  };

  const toggleNotes = (appId: string) => {
    setExpandedNotes((prev) => ({
      ...prev,
      [appId]: !prev[appId],
    }));
  };

  return (
    <section
      id="apps"
      className="py-24 bg-gradient-to-b from-slate-950 via-slate-900/60 to-slate-950 dark:from-slate-950 dark:via-slate-900/60 dark:to-slate-950 light:from-white light:via-slate-50 light:to-white border-t border-b border-slate-900 dark:border-slate-900 light:border-slate-200 relative overflow-hidden"
    >
      {/* Background Decorative Accents */}
      <div className="absolute top-1/2 left-0 w-96 h-96 bg-blue-600/5 rounded-full blur-3xl pointer-events-none -translate-y-1/2" />
      <div className="absolute bottom-10 right-0 w-96 h-96 bg-emerald-600/5 rounded-full blur-3xl pointer-events-none" />

      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 relative z-10">
        
        {/* Section Header */}
        <SectionHeaderReveal
          badge={
            <div className="inline-flex items-center gap-2 px-3.5 py-1 rounded-full bg-emerald-500/10 text-emerald-400 text-xs font-semibold tracking-wide uppercase border border-emerald-500/25">
              <Smartphone className="w-3.5 h-3.5" />
              <span>Dedicated Android Distribution</span>
            </div>
          }
          title="My Apps & APK Releases"
          description="Native Android applications developed by Nahid Hossain with direct APK downloads, automated GitHub release detection, and live changelog tracking."
        />

        {/* Real-time GitHub sync bar */}
        <ScrollReveal yOffset={25} className="mt-6 mb-16 flex flex-wrap items-center justify-center gap-3">
          <button
            id="refresh-github-releases-btn"
            onClick={handleRefreshAll}
            disabled={isRefreshingAll}
            className="inline-flex items-center gap-2 px-4 py-2 rounded-xl bg-slate-900/90 dark:bg-slate-900/90 light:bg-white hover:bg-slate-800 text-slate-200 dark:text-slate-200 light:text-slate-800 text-xs font-semibold border border-slate-800 dark:border-slate-800 light:border-slate-300 shadow-sm transition-all hover:border-slate-700 disabled:opacity-60"
          >
            <RefreshCw className={`w-3.5 h-3.5 text-blue-400 ${isRefreshingAll ? 'animate-spin' : ''}`} />
            <span>{isRefreshingAll ? 'Checking GitHub API...' : 'Check Live GitHub Releases'}</span>
          </button>

          <div className="inline-flex items-center gap-1.5 text-xs text-slate-400 dark:text-slate-400 light:text-slate-600 bg-slate-950/60 dark:bg-slate-950/60 light:bg-slate-100 px-3 py-2 rounded-xl border border-slate-800/80 dark:border-slate-800/80 light:border-slate-200">
            <ShieldCheck className="w-4 h-4 text-emerald-400" />
            <span>Direct APK Download (No GitHub Account Required)</span>
          </div>
        </ScrollReveal>

        {/* Apps Cards Grid with 3D Curvature */}
        <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">
          {APPS_CONFIG.map((app, index) => {
            const release = appReleases[app.id] || app.defaultRelease;
            const isLoading = loadingState[app.id] || false;
            const isLive = liveSyncedState[app.id] || false;
            const isNotesExpanded = expandedNotes[app.id] || false;
            const isTools = app.id === 'tools-app';

            return (
              <CurvedRollItem key={app.id}>
                <motion.div
                  initial={{ opacity: 0, y: 55, scale: 0.96 }}
                  whileInView={{ opacity: 1, y: 0, scale: 1 }}
                  viewport={{ once: false, amount: 0.12 }}
                  transition={{
                    duration: 0.55,
                    delay: index * 0.12,
                    ease: [0.25, 1, 0.5, 1],
                  }}
                  className={`relative rounded-2xl flex flex-col justify-between transition-all duration-300 h-full ${
                    isTools
                      ? 'bg-slate-900/90 dark:bg-slate-900/90 light:bg-white border-2 border-emerald-500/50 shadow-xl shadow-emerald-500/10'
                      : 'bg-slate-900/60 dark:bg-slate-900/60 light:bg-white border border-slate-800/80 dark:border-slate-800/80 light:border-slate-200 shadow-sm'
                  }`}
                >
                {/* Header ribbon for active release */}
                {isTools && (
                  <div className="bg-gradient-to-r from-emerald-600 to-teal-600 px-4 py-1.5 rounded-t-2xl flex items-center justify-between text-white text-[11px] font-bold tracking-wide">
                    <span className="flex items-center gap-1.5">
                      <Sparkles className="w-3.5 h-3.5" />
                      ACTIVE REPOSITORY RELEASE
                    </span>
                    <span className="bg-black/25 px-2 py-0.5 rounded text-[10px]">
                      {isLive ? 'LIVE GITHUB SYNC' : 'LATEST BUILD'}
                    </span>
                  </div>
                )}

                <div className="p-6">
                  
                  {/* App Icon & Basic Identification */}
                  <div className="flex items-start justify-between gap-4 mb-5">
                    <div className="flex items-center gap-3.5">
                      {/* App Icon */}
                      <div className="w-14 h-14 rounded-2xl bg-gradient-to-br from-emerald-500 via-teal-600 to-blue-600 p-0.5 shadow-md shadow-emerald-500/20 flex items-center justify-center">
                        <div className="w-full h-full rounded-[14px] bg-slate-950 flex items-center justify-center">
                          <Smartphone className="w-7 h-7 text-emerald-400" />
                        </div>
                      </div>

                      <div>
                        <div className="flex items-center gap-2">
                          <h3 className="text-xl font-bold text-slate-100 dark:text-slate-100 light:text-slate-900">
                            {app.appName}
                          </h3>
                        </div>
                        <p className="text-xs text-emerald-400 font-medium">
                          Android Application
                        </p>
                        <p className="text-[11px] text-slate-400 dark:text-slate-400 light:text-slate-500 mt-0.5">
                          {app.category}
                        </p>
                      </div>
                    </div>

                    {/* Status Badge */}
                    <span
                      className={`text-[10px] font-bold px-2 py-0.5 rounded-full border uppercase tracking-wider shrink-0 ${
                        app.status === 'available'
                          ? 'bg-emerald-500/10 text-emerald-400 border-emerald-500/30'
                          : app.status === 'in-development'
                          ? 'bg-blue-500/10 text-blue-400 border-blue-500/30'
                          : 'bg-slate-800 text-slate-400 border-slate-700'
                      }`}
                    >
                      {app.status === 'available' ? 'Available' : 'In Dev'}
                    </span>
                  </div>

                  {/* App Description */}
                  <p className="text-xs text-slate-300 dark:text-slate-300 light:text-slate-600 leading-relaxed mb-5 min-h-[48px]">
                    {app.description}
                  </p>

                  {/* Technical Metadata Box (Direct format matching requested card) */}
                  <div className="p-4 rounded-xl bg-slate-950/80 dark:bg-slate-950/80 light:bg-slate-50 border border-slate-800/80 dark:border-slate-800/80 light:border-slate-200 mb-5 space-y-2.5 font-mono text-xs">
                    
                    {/* Latest Version */}
                    <div className="flex items-center justify-between text-slate-300 dark:text-slate-300 light:text-slate-700">
                      <span className="flex items-center gap-1.5 font-sans text-slate-400 dark:text-slate-400 light:text-slate-500 text-[11px]">
                        <Tag className="w-3.5 h-3.5 text-blue-400" />
                        Latest Version:
                      </span>
                      <span className="font-bold text-emerald-400 dark:text-emerald-400 light:text-emerald-700 bg-emerald-500/10 px-2 py-0.5 rounded">
                        {release.version}
                      </span>
                    </div>

                    {/* Release Date */}
                    <div className="flex items-center justify-between text-slate-300 dark:text-slate-300 light:text-slate-700">
                      <span className="flex items-center gap-1.5 font-sans text-slate-400 dark:text-slate-400 light:text-slate-500 text-[11px]">
                        <Calendar className="w-3.5 h-3.5 text-indigo-400" />
                        Updated:
                      </span>
                      <span className="text-slate-200 dark:text-slate-200 light:text-slate-800">
                        {release.releaseDate}
                      </span>
                    </div>

                    {/* APK Size */}
                    <div className="flex items-center justify-between text-slate-300 dark:text-slate-300 light:text-slate-700">
                      <span className="flex items-center gap-1.5 font-sans text-slate-400 dark:text-slate-400 light:text-slate-500 text-[11px]">
                        <HardDrive className="w-3.5 h-3.5 text-teal-400" />
                        APK Size:
                      </span>
                      <span className="font-semibold text-slate-200 dark:text-slate-200 light:text-slate-800">
                        {release.apkSize}
                      </span>
                    </div>

                    {/* Repository indicator */}
                    <div className="pt-2 border-t border-slate-800/60 dark:border-slate-800/60 light:border-slate-200 flex items-center justify-between text-[10px] font-sans text-slate-500">
                      <span>Repo: {app.repoOwner}/{app.repoName}</span>
                      {release.tagCommit && <span>commit: {release.tagCommit}</span>}
                    </div>
                  </div>

                  {/* What's New / Changelog Accordion */}
                  <div className="mb-5">
                    <button
                      onClick={() => toggleNotes(app.id)}
                      className="w-full flex items-center justify-between py-2 px-3 rounded-lg bg-slate-950/40 dark:bg-slate-950/40 light:bg-slate-100 hover:bg-slate-950/70 text-xs font-semibold text-slate-300 dark:text-slate-300 light:text-slate-700 border border-slate-800/50 dark:border-slate-800/50 light:border-slate-200 transition-colors"
                    >
                      <span className="flex items-center gap-1.5">
                        <Info className="w-3.5 h-3.5 text-blue-400" />
                        What's New in {release.version}
                      </span>
                      {isNotesExpanded ? (
                        <ChevronUp className="w-3.5 h-3.5 text-slate-500" />
                      ) : (
                        <ChevronDown className="w-3.5 h-3.5 text-slate-500" />
                      )}
                    </button>

                    {isNotesExpanded && (
                      <div className="mt-2 p-3 rounded-xl bg-slate-950/60 dark:bg-slate-950/60 light:bg-slate-50 border border-slate-800/60 dark:border-slate-800/60 light:border-slate-200 text-xs space-y-1.5">
                        {release.whatsNew.map((item, idx) => (
                          <div key={idx} className="flex items-start gap-2 text-slate-300 dark:text-slate-300 light:text-slate-600 leading-snug">
                            <span className="text-emerald-400 font-bold mt-0.5">•</span>
                            <span>{item}</span>
                          </div>
                        ))}
                      </div>
                    )}
                  </div>

                </div>

                {/* Primary Action Buttons: [Download APK] and [GitHub] */}
                <div className="p-6 pt-0 space-y-2.5">
                  <button
                    id={`download-apk-${app.id}`}
                    onClick={() => handleDownloadApk(app)}
                    disabled={app.status !== 'available'}
                    className={`w-full py-3 px-4 rounded-xl text-xs sm:text-sm font-bold flex items-center justify-center gap-2 transition-all shadow-md ${
                      app.status === 'available'
                        ? 'bg-emerald-600 hover:bg-emerald-500 text-white shadow-emerald-600/25 active:scale-[0.98]'
                        : 'bg-slate-800 text-slate-400 cursor-not-allowed border border-slate-700'
                    }`}
                  >
                    <Download className="w-4 h-4" />
                    <span>
                      {app.status === 'available' ? 'Download APK' : 'Coming Soon'}
                    </span>
                  </button>

                  <div className="flex items-center gap-2">
                    <a
                      href={app.githubUrl}
                      target="_blank"
                      rel="noopener noreferrer"
                      className="flex-1 py-2 px-3 rounded-xl bg-slate-950 dark:bg-slate-950 light:bg-slate-100 hover:bg-slate-800 text-slate-200 dark:text-slate-200 light:text-slate-800 text-xs font-semibold border border-slate-800 dark:border-slate-800 light:border-slate-300 flex items-center justify-center gap-1.5 transition-colors"
                    >
                      <Github className="w-3.5 h-3.5" />
                      <span>GitHub</span>
                    </a>

                    <button
                      onClick={() => fetchReleaseForApp(app, true)}
                      disabled={isLoading}
                      className="py-2 px-3 rounded-xl bg-slate-950 dark:bg-slate-950 light:bg-slate-100 hover:bg-slate-800 text-slate-400 hover:text-slate-200 text-xs font-semibold border border-slate-800 dark:border-slate-800 light:border-slate-300 flex items-center justify-center gap-1 transition-colors"
                      title="Refresh this repository from GitHub"
                    >
                      <RefreshCw className={`w-3.5 h-3.5 ${isLoading ? 'animate-spin text-blue-400' : ''}`} />
                      <span className="hidden sm:inline">Sync</span>
                    </button>
                  </div>
                </div>

              </motion.div>
            </CurvedRollItem>
          );
        })}
        </div>

        {/* Architecture Spotlight: Multiple GitHub Repositories -> Auto-release */}
        <ScrollReveal yOffset={40} className="mt-16">
          <div className="p-6 sm:p-8 rounded-2xl bg-slate-900/50 dark:bg-slate-900/50 light:bg-white border border-slate-800/80 dark:border-slate-800/80 light:border-slate-200">
            <div className="flex flex-col md:flex-row items-start md:items-center justify-between gap-6 mb-6">
              <div>
                <div className="inline-flex items-center gap-1.5 text-xs font-bold text-blue-400 uppercase tracking-wider mb-1">
                  <GitPullRequest className="w-3.5 h-3.5" />
                  <span>Automated Distribution Architecture</span>
                </div>
                <h3 className="text-xl font-bold text-slate-100 dark:text-slate-100 light:text-slate-900">
                  Multi-Repository GitHub & APK Release Pipeline
                </h3>
              </div>
              <span className="px-3 py-1 rounded-full text-xs font-semibold bg-blue-500/10 text-blue-400 border border-blue-500/20 shrink-0">
                Future-Proof Design
              </span>
            </div>

            <p className="text-xs sm:text-sm text-slate-300 dark:text-slate-300 light:text-slate-600 leading-relaxed mb-6">
              This portfolio is architecturally configured to decouple app repositories (<code className="text-emerald-400 font-mono text-[11px] bg-slate-950 px-1.5 py-0.5 rounded">tools</code>, <code className="text-emerald-400 font-mono text-[11px] bg-slate-950 px-1.5 py-0.5 rounded">calculator-app</code>, <code className="text-emerald-400 font-mono text-[11px] bg-slate-950 px-1.5 py-0.5 rounded">expense-manager</code>) from the web presentation layer (<code className="text-blue-400 font-mono text-[11px] bg-slate-950 px-1.5 py-0.5 rounded">portfolio-web</code>). Whenever Nahid publishes a new GitHub Release with an APK asset, the portfolio detects the update, recalculates size and versioning, and allows visitors to initiate direct APK downloads without manual page updates.
            </p>

            {/* Workflow Steps */}
            <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-3 text-xs">
              <div className="p-3.5 rounded-xl bg-slate-950/60 dark:bg-slate-950/60 light:bg-slate-50 border border-slate-800/60 dark:border-slate-800/60 light:border-slate-200">
                <span className="font-bold text-blue-400 block mb-1">1. Build & Push</span>
                <span className="text-slate-400 dark:text-slate-400 light:text-slate-600">
                  Developer writes Kotlin code and compiles release APK via Android Studio or GitHub Actions.
                </span>
              </div>

              <div className="p-3.5 rounded-xl bg-slate-950/60 dark:bg-slate-950/60 light:bg-slate-50 border border-slate-800/60 dark:border-slate-800/60 light:border-slate-200">
                <span className="font-bold text-indigo-400 block mb-1">2. GitHub Release</span>
                <span className="text-slate-400 dark:text-slate-400 light:text-slate-600">
                  New git tag and release is published with attached <code className="text-slate-300 font-mono">app-release.apk</code> asset.
                </span>
              </div>

              <div className="p-3.5 rounded-xl bg-slate-950/60 dark:bg-slate-950/60 light:bg-slate-50 border border-slate-800/60 dark:border-slate-800/60 light:border-slate-200">
                <span className="font-bold text-teal-400 block mb-1">3. Auto Detection</span>
                <span className="text-slate-400 dark:text-slate-400 light:text-slate-600">
                  Portfolio queries GitHub public API, updating version tags, file sizes, and release notes automatically.
                </span>
              </div>

              <div className="p-3.5 rounded-xl bg-slate-950/60 dark:bg-slate-950/60 light:bg-slate-50 border border-slate-800/60 dark:border-slate-800/60 light:border-slate-200">
                <span className="font-bold text-emerald-400 block mb-1">4. Direct Download</span>
                <span className="text-slate-400 dark:text-slate-400 light:text-slate-600">
                  Visitors click Download APK to instantly receive the package directly on mobile or desktop.
                </span>
              </div>
            </div>
          </div>
        </ScrollReveal>

      </div>
    </section>
  );
};
