import React from 'react';
import { Certificates } from '../components/Certificates';
import { Breadcrumb } from '../components/Breadcrumb';

export const CertificatesPage: React.FC = () => {
  return (
    <div className="pt-24 pb-16">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 mb-6">
        <Breadcrumb currentPage="Certificates" />
      </div>
      <Certificates />
    </div>
  );
};
