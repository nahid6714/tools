import { AppRepoConfig } from '../types/portfolio';

/**
 * Centralized Multi-Repository Android Apps Configuration
 * 
 * To add a new Android Application:
 * 1. Push your APK to GitHub Releases in the target repository.
 * 2. Add an entry below with repoOwner and repoName.
 * 3. The portfolio's release service will automatically detect and fetch the latest
 *    APK asset, version number, release date, and changelog via GitHub's public API.
 */
export const APPS_CONFIG: AppRepoConfig[] = [
  {
    id: 'tools-app',
    appName: 'Tools',
    repoOwner: 'nahid6714',
    repoName: 'tools',
    category: 'Productivity & Utilities',
    description:
      'A collection of useful Android tools and utilities developed as a practical software project. Includes Food Bill Manager, Smart Document & NID Scanner, and PDF Export.',
    icon: 'wrench',
    githubUrl: 'https://github.com/nahid6714/tools',
    status: 'available',
    defaultRelease: {
      version: 'v1.0.184',
      releaseDate: 'August 18, 2026',
      apkSize: '2.44 MB',
      apkFileName: 'app-release.apk',
      downloadUrl: 'https://github.com/nahid6714/tools/releases/download/v1.0.184/app-release.apk',
      whatsNew: [
        'Add drag-to-reorder for quick presets and bill items',
        'Enhanced NID and ID card document scanner contrast',
        'Direct PDF export and thermal printer compatibility',
        'Offline Room database local caching performance optimizations',
      ],
      tagCommit: '4045b45',
    },
  },
  {
    id: 'calculator-app',
    appName: 'Smart Calculator & Unit Converter',
    repoOwner: 'nahid6714',
    repoName: 'calculator-app',
    category: 'Mathematics & Daily Utility',
    description:
      'Modern Android calculator with real-time currency conversion, unit conversions, and calculation history log using Jetpack Compose.',
    icon: 'calculator',
    githubUrl: 'https://github.com/nahid6714/calculator-app',
    status: 'in-development',
    defaultRelease: {
      version: 'v0.9.2-beta',
      releaseDate: 'Coming Soon',
      apkSize: '3.1 MB',
      apkFileName: 'calculator-preview.apk',
      downloadUrl: 'https://github.com/nahid6714/calculator-app/releases',
      whatsNew: [
        'Scientific math formula evaluation',
        'History tape with exportable calculation logs',
        'Dark and Light theme automatic synchronization',
      ],
    },
  },
  {
    id: 'expense-manager',
    appName: 'Daily Expense & Budget Manager',
    repoOwner: 'nahid6714',
    repoName: 'expense-manager',
    category: 'Finance & Budgeting',
    description:
      'Personal finance tracking application built for offline-first privacy. Log daily expenses, categorize transactions, and view clean monthly summaries.',
    icon: 'wallet',
    githubUrl: 'https://github.com/nahid6714/expense-manager',
    status: 'planned',
    defaultRelease: {
      version: 'v0.1.0-alpha',
      releaseDate: 'In Roadmap',
      apkSize: '4.5 MB',
      apkFileName: 'expense-manager.apk',
      downloadUrl: 'https://github.com/nahid6714/expense-manager',
      whatsNew: [
        'Initial prototype architecture with Jetpack Compose',
        'Room database schema for multi-category transactions',
        'Local encrypted backup export to storage',
      ],
    },
  },
];
