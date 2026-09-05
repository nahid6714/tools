import React, { useEffect } from 'react';
import { useLocation } from 'react-router-dom';

export const ScrollToTop: React.FC = () => {
  const { pathname } = useLocation();

  useEffect(() => {
    window.scrollTo({ top: 0, left: 0, behavior: 'smooth' });
    const roadScroll = document.getElementById('road-scroll-view');
    if (roadScroll) {
      roadScroll.scrollTo({ top: 0, left: 0, behavior: 'smooth' });
    }
    window.dispatchEvent(new CustomEvent('app-scroll-to-top'));
  }, [pathname]);

  return null;
};
