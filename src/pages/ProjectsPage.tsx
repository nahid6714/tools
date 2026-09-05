import React from 'react';
import { useNavigate } from 'react-router-dom';
import { Projects } from '../components/Projects';
import { Breadcrumb } from '../components/Breadcrumb';

export const ProjectsPage: React.FC = () => {
  const navigate = useNavigate();

  return (
    <div className="pt-24 pb-16">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 mb-6">
        <Breadcrumb currentPage="Projects" />
      </div>
      <Projects 
        onOpenAppSection={() => {
          navigate('/apps');
        }}
      />
    </div>
  );
};
