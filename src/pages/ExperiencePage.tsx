import React from 'react';
import { Experience } from '../components/Experience';
import { Education } from '../components/Education';
import { Breadcrumb } from '../components/Breadcrumb';

export const ExperiencePage: React.FC = () => {
  return (
    <div className="pt-24 pb-16 space-y-12">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <Breadcrumb currentPage="Experience & Education" />
      </div>
      <Experience />
      <Education />
    </div>
  );
};
