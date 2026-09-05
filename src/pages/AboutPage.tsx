import React from 'react';
import { About } from '../components/About';
import { Breadcrumb } from '../components/Breadcrumb';

export const AboutPage: React.FC = () => {
  return (
    <div className="pt-24 pb-16">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 mb-6">
        <Breadcrumb currentPage="About" />
      </div>
      <About />
    </div>
  );
};
