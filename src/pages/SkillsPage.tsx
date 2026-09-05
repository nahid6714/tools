import React from 'react';
import { Skills } from '../components/Skills';
import { Breadcrumb } from '../components/Breadcrumb';

export const SkillsPage: React.FC = () => {
  return (
    <div className="pt-24 pb-16">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 mb-6">
        <Breadcrumb currentPage="Skills & Technologies" />
      </div>
      <Skills />
    </div>
  );
};
