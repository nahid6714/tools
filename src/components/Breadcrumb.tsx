import React from 'react';
import { Link } from 'react-router-dom';
import { Home, ChevronRight } from 'lucide-react';

interface BreadcrumbProps {
  currentPage: string;
}

export const Breadcrumb: React.FC<BreadcrumbProps> = ({ currentPage }) => {
  return (
    <nav className="flex items-center gap-2 text-xs text-slate-400 dark:text-slate-400 light:text-slate-500 py-2">
      <Link
        to="/"
        className="flex items-center gap-1 hover:text-blue-400 transition-colors"
      >
        <Home className="w-3.5 h-3.5" />
        <span>Home</span>
      </Link>
      <ChevronRight className="w-3.5 h-3.5 text-slate-600" />
      <span className="font-semibold text-slate-200 dark:text-slate-200 light:text-slate-800">
        {currentPage}
      </span>
    </nav>
  );
};
