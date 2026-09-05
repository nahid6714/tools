export type ThemeMode = 'dark' | 'light';

export interface ProjectItem {
  id: string;
  name: string;
  description: string;
  longDescription?: string;
  technologies: string[];
  githubUrl: string;
  liveUrl?: string;
  apkUrl?: string;
  apkSize?: string;
  featured?: boolean;
  category: 'Android App' | 'Web Development' | 'Software Tool';
  iconType: string;
}

export interface AppReleaseInfo {
  version: string;
  releaseDate: string;
  apkSize: string;
  apkFileName: string;
  downloadUrl: string;
  whatsNew: string[];
  tagCommit?: string;
}

export interface AppRepoConfig {
  id: string;
  appName: string;
  repoOwner: string;
  repoName: string;
  category: string;
  description: string;
  icon: string;
  githubUrl: string;
  status: 'available' | 'in-development' | 'planned';
  defaultRelease: AppReleaseInfo;
}

export interface SkillItem {
  name: string;
  category: 'Android & Mobile' | 'Web & Frontend' | 'Cloud & Backend' | 'Workflow & Tools';
  level: 'Learning' | 'Intermediate' | 'Experienced';
  icon: string;
  description?: string;
}

export interface ExperienceItem {
  id: string;
  position: string;
  company: string;
  duration: string;
  location: string;
  type: string;
  responsibilities: string[];
  achievements?: string[];
  isPlaceholder?: boolean;
}

export interface EducationItem {
  id: string;
  institution: string;
  degree: string;
  field: string;
  year: string;
  details: string;
  isPlaceholder?: boolean;
}

export interface CertificateItem {
  id: string;
  name: string;
  organization: string;
  date: string;
  credentialUrl?: string;
  isPlaceholder?: boolean;
}

export interface ContactFormData {
  name: string;
  email: string;
  subject: string;
  message: string;
}
