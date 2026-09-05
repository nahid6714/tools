import React from 'react';
import { AppsSection } from '../components/AppsSection';
import { Breadcrumb } from '../components/Breadcrumb';

interface AppsPageProps {
  onShowToast: (message: string, type?: 'info' | 'success' | 'warning') => void;
}

export const AppsPage: React.FC<AppsPageProps> = ({ onShowToast }) => {
  return (
    <div className="pt-24 pb-16">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 mb-6">
        <Breadcrumb currentPage="My Apps & APK Releases" />
      </div>
      <AppsSection onShowToast={onShowToast} />
    </div>
  );
};
