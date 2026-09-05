import { ProjectItem, SkillItem, ExperienceItem, EducationItem, CertificateItem } from '../types/portfolio';

export const PERSONAL_INFO = {
  name: 'Nahid Hossain',
  subtitle: 'Student • Worker • Technology Enthusiast • Developer',
  tagline: 'AI-Assisted Android & Web Developer',
  introduction:
    'Passionate about technology, software development, Android applications, and building useful digital tools.',
  extendedBio:
    'Based in Bangladesh, I balance roles as a student and worker while channeling my passion into building software tools, native Android applications, and modern web interfaces. I leverage modern development practices, Git version control, and automated APK releases to deliver practical, user-centric software.',
  country: 'Bangladesh',
  role: 'Student + Worker',
  email: 'mdnahidislam6714@gmail.com',
  githubUrl: 'https://github.com/nahid6714',
  facebookUrl: 'https://facebook.com/#',
  linkedinUrl: 'https://linkedin.com/in/#',
  avatarUrl: '/profile.jpg',
  bannerUrl: '/banner.png',
  resumeFileName: 'Nahid_Hossain_Resume.pdf',
  status: 'Open to opportunities & projects',
};

export const INTERESTS = [
  {
    title: 'Software Development',
    description: 'Crafting practical desktop, web, and system utilities designed to solve daily productivity challenges.',
    icon: 'code',
  },
  {
    title: 'Android Applications',
    description: 'Developing native mobile experiences with Kotlin, Jetpack Compose, Room Database, and Material Design.',
    icon: 'smartphone',
  },
  {
    title: 'Web Development',
    description: 'Building clean, fast, and responsive user interfaces using modern web standards and responsive layout principles.',
    icon: 'globe',
  },
  {
    title: 'GitHub & Open Source',
    description: 'Managing source code, continuous integration with GitHub Actions, and public release distribution.',
    icon: 'github',
  },
  {
    title: 'Automation & CI/CD',
    description: 'Automating build pipelines to compile, package, and release signed APK artifacts effortlessly.',
    icon: 'cpu',
  },
  {
    title: 'Digital Tools',
    description: 'Designing scanners, bill calculators, document tools, and offline-first productivity utilities.',
    icon: 'wrench',
  },
];

export const SKILLS: SkillItem[] = [
  // Android & Mobile
  {
    name: 'Android Development',
    category: 'Android & Mobile',
    level: 'Intermediate',
    icon: 'smartphone',
    description: 'Native mobile app architecture, lifecycle management, and Material UI.',
  },
  {
    name: 'Kotlin',
    category: 'Android & Mobile',
    level: 'Intermediate',
    icon: 'code-2',
    description: 'Modern idiomatic Kotlin, coroutines, and null-safe programming.',
  },
  {
    name: 'Jetpack Compose',
    category: 'Android & Mobile',
    level: 'Intermediate',
    icon: 'layers',
    description: 'Declarative UI design system for modern Android interfaces.',
  },
  {
    name: 'Room Database',
    category: 'Android & Mobile',
    level: 'Intermediate',
    icon: 'database',
    description: 'Offline-first SQLite abstraction, entity models, and DAO queries.',
  },
  {
    name: 'APK Build & Release',
    category: 'Android & Mobile',
    level: 'Experienced',
    icon: 'package-check',
    description: 'Gradle packaging, release signing, asset distribution, and version tagging.',
  },

  // Workflow & Tools
  {
    name: 'Git',
    category: 'Workflow & Tools',
    level: 'Experienced',
    icon: 'git-branch',
    description: 'Branch management, commit hygiene, merge strategies, and version control.',
  },
  {
    name: 'GitHub',
    category: 'Workflow & Tools',
    level: 'Experienced',
    icon: 'github',
    description: 'Repository hosting, issues tracking, project boards, and release assets.',
  },
  {
    name: 'GitHub Actions',
    category: 'Workflow & Tools',
    level: 'Intermediate',
    icon: 'play-circle',
    description: 'Automated CI/CD workflows for building and publishing Android APKs.',
  },
  {
    name: 'Android Studio',
    category: 'Workflow & Tools',
    level: 'Intermediate',
    icon: 'laptop',
    description: 'Primary IDE for native Android development, profiling, and debugging.',
  },
  {
    name: 'Computer Skills',
    category: 'Workflow & Tools',
    level: 'Experienced',
    icon: 'monitor',
    description: 'System troubleshooting, OS administration, hardware setups, and tooling.',
  },

  // Web & Frontend
  {
    name: 'Web Development',
    category: 'Web & Frontend',
    level: 'Intermediate',
    icon: 'globe',
    description: 'Modern front-end architecture, semantic structuring, and styling.',
  },
  {
    name: 'HTML',
    category: 'Web & Frontend',
    level: 'Experienced',
    icon: 'file-code',
    description: 'Semantic markup, accessibility (a11y), and clean DOM structures.',
  },
  {
    name: 'CSS',
    category: 'Web & Frontend',
    level: 'Intermediate',
    icon: 'palette',
    description: 'Responsive layouts, Flexbox, CSS Grid, animations, and Tailwind CSS.',
  },
  {
    name: 'JavaScript',
    category: 'Web & Frontend',
    level: 'Intermediate',
    icon: 'braces',
    description: 'ES6+ syntax, asynchronous programming, and DOM interaction.',
  },

  // Cloud & Backend
  {
    name: 'Firebase',
    category: 'Cloud & Backend',
    level: 'Learning',
    icon: 'flame',
    description: 'Cloud Firestore, authentication integration, and cloud services.',
  },
  {
    name: 'Software / Tool Development',
    category: 'Cloud & Backend',
    level: 'Intermediate',
    icon: 'wrench',
    description: 'Designing custom workflows, scanners, converters, and data processing utilities.',
  },
];

export const PROJECTS: ProjectItem[] = [
  {
    id: 'tools',
    name: 'Tools',
    description:
      'A collection of useful Android tools and utilities developed as a practical software project.',
    longDescription:
      'An all-in-one productivity Android application built with Kotlin and Jetpack Compose. Features a Food Bill Manager with drag-to-reorder quick presets, a Smart Document Scanner for NID & ID cards, PDF export, thermal printing support, and offline-first Room Database storage.',
    technologies: ['Kotlin', 'Jetpack Compose', 'Room Database', 'Android SDK', 'GitHub Actions'],
    githubUrl: 'https://github.com/nahid6714/tools',
    apkUrl: 'https://github.com/nahid6714/tools/releases/download/v1.0.184/app-release.apk',
    apkSize: '2.44 MB',
    featured: true,
    category: 'Android App',
    iconType: 'smartphone',
  },
  {
    id: 'anys-beauty-corner',
    name: "Any's Beauty Corner",
    description:
      'A modern responsive beauty and cosmetics storefront website with dynamic catalog showcases.',
    longDescription:
      'Live commercial showcase website featuring a curated beauty product catalog, mobile-first responsive layout, smooth navigation transitions, and clean performance optimization.',
    technologies: ['HTML5', 'CSS3', 'JavaScript', 'Responsive Web Design', 'Netlify'],
    githubUrl: 'https://github.com/nahid6714',
    liveUrl: 'https://anysbeautycorner.netlify.app',
    featured: true,
    category: 'Web Development',
    iconType: 'globe',
  },
  {
    id: 'calculator-converter',
    name: 'Smart Calculator & Unit Converter',
    description:
      'Practical utility application featuring scientific arithmetic, unit conversion engines, and history tape.',
    longDescription:
      'Designed with modern Material Design principles for fast daily computations, currency ratios, and exportable calculation histories.',
    technologies: ['Kotlin', 'Android Studio', 'Material 3', 'Unit Test'],
    githubUrl: 'https://github.com/nahid6714/calculator-app',
    featured: false,
    category: 'Software Tool',
    iconType: 'calculator',
  },
];

export const WORK_EXPERIENCE: ExperienceItem[] = [
  {
    id: 'exp-placeholder',
    position: 'Technology Practitioner & Independent Developer',
    company: 'Work experience details will be added here',
    duration: '2023 – Present',
    location: 'Bangladesh',
    type: 'Student & Worker',
    responsibilities: [
      'Architecting and building practical software tools and Android applications.',
      'Configuring automated CI/CD build pipelines for APK generation on GitHub Actions.',
      'Deploying responsive web frontends and maintaining open-source codebases.',
      'Balancing vocational work commitments alongside continuous self-directed software development studies.',
    ],
    achievements: [
      'Published active releases of Tools app with over 180+ automated CI/CD build iterations.',
      'Established a streamlined APK release pipeline for rapid deployment.',
    ],
    isPlaceholder: true,
  },
];

export const EDUCATION: EducationItem[] = [
  {
    id: 'edu-placeholder',
    institution: 'Educational Institution',
    degree: 'Academic Studies in Computer Technology',
    field: 'Computer Science & Software Development',
    year: 'Ongoing',
    details:
      'Focused on programming fundamentals, software architecture, mobile development, algorithms, and practical application design. (Specific institution details to be updated).',
    isPlaceholder: true,
  },
];

export const CERTIFICATES: CertificateItem[] = [
  // Empty state initially as requested
];
