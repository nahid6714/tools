import React from 'react';
import { Contact } from '../components/Contact';
import { Breadcrumb } from '../components/Breadcrumb';

interface ContactPageProps {
  onShowToast: (message: string, type?: 'info' | 'success' | 'warning') => void;
}

export const ContactPage: React.FC<ContactPageProps> = ({ onShowToast }) => {
  return (
    <div className="pt-24 pb-16">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 mb-6">
        <Breadcrumb currentPage="Contact" />
      </div>
      <Contact onShowToast={onShowToast} />
    </div>
  );
};
