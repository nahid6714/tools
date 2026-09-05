import React, { useRef, useEffect } from 'react';
import { useRoadPerspective } from '../context/RoadPerspectiveContext';

interface RoadPerspectiveStageProps {
  children: React.ReactNode;
}

export const RoadPerspectiveStage: React.FC<RoadPerspectiveStageProps> = ({ children }) => {
  const { pitch, roll, isEnabled } = useRoadPerspective();
  const scrollContainerRef = useRef<HTMLDivElement>(null);

  // Expose scroll container globally so ScrollToTop works smoothly
  useEffect(() => {
    const handleCustomScrollTop = () => {
      if (scrollContainerRef.current) {
        scrollContainerRef.current.scrollTo({ top: 0, left: 0, behavior: 'smooth' });
      }
    };

    window.addEventListener('app-scroll-to-top', handleCustomScrollTop);
    return () => {
      window.removeEventListener('app-scroll-to-top', handleCustomScrollTop);
    };
  }, []);

  return (
    <div
      id="road-perspective-viewport"
      className="fixed inset-0 overflow-hidden bg-slate-950 dark:bg-slate-950 light:bg-slate-100"
      style={{
        perspective: '920px',
        perspectiveOrigin: '50% 50%',
      }}
    >
      {/* Background 3D Horizon Atmosphere */}
      <div 
        className="absolute inset-0 pointer-events-none opacity-40 transition-opacity duration-300"
        aria-hidden="true"
      >
        {/* Subtle Horizon Depth Glow */}
        <div 
          className="absolute top-0 left-1/2 -translate-x-1/2 w-full max-w-5xl h-36 bg-gradient-to-b from-blue-600/10 via-indigo-600/5 to-transparent blur-2xl"
          style={{
            opacity: Math.max(0, Math.min(1, pitch / 15)),
          }}
        />
      </div>

      {/* The 3D Tilted Plane (Includes Header + Main Content + Footer) */}
      <div
        id="road-tilt-plane"
        className="w-full h-full"
        style={{
          transform: isEnabled
            ? `rotateX(${pitch.toFixed(2)}deg) rotateY(${roll.toFixed(2)}deg)`
            : 'none',
          transformOrigin: '50% 50%',
          transformStyle: 'preserve-3d',
          willChange: 'transform',
        }}
      >
        {/* The Native Scroll Container */}
        <div
          id="road-scroll-view"
          ref={scrollContainerRef}
          className="w-full h-full overflow-y-auto overflow-x-hidden relative"
          style={{
            WebkitOverflowScrolling: 'touch',
          }}
        >
          {/* Top Atmospheric Horizon Vignette when tilted forward like a highway */}
          {isEnabled && pitch > 3 && (
            <div
              className="pointer-events-none fixed top-0 left-0 right-0 h-28 z-40 transition-opacity duration-200 bg-gradient-to-b from-slate-950/40 via-slate-950/10 to-transparent dark:from-slate-950/40 light:from-slate-200/40"
              style={{
                opacity: Math.min(1, (pitch - 3) / 18),
              }}
            />
          )}

          {/* Bottom Horizon Vignette when tilted backward */}
          {isEnabled && pitch < -3 && (
            <div
              className="pointer-events-none fixed bottom-0 left-0 right-0 h-28 z-40 transition-opacity duration-200 bg-gradient-to-t from-slate-950/40 via-slate-950/10 to-transparent dark:from-slate-950/40 light:from-slate-200/40"
              style={{
                opacity: Math.min(1, (-pitch - 3) / 18),
              }}
            />
          )}

          {/* Page Content (Navbar, Main Route, Footer) */}
          <div className="relative min-h-full flex flex-col">
            {children}
          </div>
        </div>
      </div>
    </div>
  );
};
