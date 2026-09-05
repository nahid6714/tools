import React from 'react';
import { motion } from 'motion/react';
import { CurvedRollItem } from './CurvedRollItem';

export { CurvedRollItem };

interface ScrollRevealProps {
  children: React.ReactNode;
  delay?: number;
  duration?: number;
  yOffset?: number;
  className?: string;
  once?: boolean;
  enableCurve?: boolean;
}

/**
 * ScrollReveal animates elements as they enter the viewport:
 * - Floating/flying in from below (yOffset -> 0)
 * - Fading in (opacity: 0 -> 1)
 * - 3D Vertical Cylinder curvature: tilts backwards and vanishes into the top/bottom rim as requested
 */
export const ScrollReveal: React.FC<ScrollRevealProps> = ({
  children,
  delay = 0,
  duration = 0.65,
  yOffset = 45,
  className = '',
  once = false,
  enableCurve = true,
}) => {
  const content = (
    <motion.div
      initial={{ opacity: 0, y: yOffset, filter: 'blur(4px)' }}
      whileInView={{ opacity: 1, y: 0, filter: 'blur(0px)' }}
      viewport={{ once, amount: 0.15 }}
      transition={{
        duration,
        delay,
        ease: [0.25, 1, 0.5, 1],
      }}
    >
      {children}
    </motion.div>
  );

  if (enableCurve) {
    return (
      <CurvedRollItem className={className}>
        {content}
      </CurvedRollItem>
    );
  }

  return <div className={className}>{content}</div>;
};

interface ScrollStaggerProps {
  children: React.ReactNode;
  className?: string;
  staggerDelay?: number;
}

export const ScrollStagger: React.FC<ScrollStaggerProps> = ({
  children,
  className = '',
}) => {
  return (
    <div className={className}>
      {React.Children.map(children, (child, idx) => {
        if (!React.isValidElement(child)) return child;
        return (
          <CurvedRollItem>
            <motion.div
              initial={{ opacity: 0, y: 40, scale: 0.96 }}
              whileInView={{ opacity: 1, y: 0, scale: 1 }}
              viewport={{ once: false, amount: 0.15 }}
              transition={{
                duration: 0.55,
                delay: idx * 0.08,
                ease: [0.25, 1, 0.5, 1],
              }}
            >
              {child}
            </motion.div>
          </CurvedRollItem>
        );
      })}
    </div>
  );
};

export const SectionHeaderReveal: React.FC<{
  badge: React.ReactNode;
  title: string;
  description?: string;
  className?: string;
}> = ({ badge, title, description, className = '' }) => {
  return (
    <CurvedRollItem className={`flex flex-col items-center text-center mb-16 ${className}`}>
      <motion.div
        initial={{ opacity: 0, y: 25, scale: 0.9 }}
        whileInView={{ opacity: 1, y: 0, scale: 1 }}
        viewport={{ once: false, amount: 0.2 }}
        transition={{ duration: 0.5, ease: 'easeOut' }}
        className="mb-3"
      >
        {badge}
      </motion.div>

      <motion.h2
        initial={{ opacity: 0, y: 35, filter: 'blur(6px)' }}
        whileInView={{ opacity: 1, y: 0, filter: 'blur(0px)' }}
        viewport={{ once: false, amount: 0.2 }}
        transition={{ duration: 0.65, delay: 0.1, ease: [0.22, 1, 0.36, 1] }}
        className="text-3xl sm:text-4xl font-extrabold tracking-tight text-slate-100 dark:text-slate-100 light:text-slate-900"
      >
        {title}
      </motion.h2>

      <motion.div
        initial={{ scaleX: 0, opacity: 0 }}
        whileInView={{ scaleX: 1, opacity: 1 }}
        viewport={{ once: false, amount: 0.2 }}
        transition={{ duration: 0.5, delay: 0.2 }}
        className="w-16 h-1 bg-blue-500 rounded-full mt-3 mb-4"
      />

      {description && (
        <motion.p
          initial={{ opacity: 0, y: 20 }}
          whileInView={{ opacity: 1, y: 0 }}
          viewport={{ once: false, amount: 0.2 }}
          transition={{ duration: 0.6, delay: 0.25 }}
          className="max-w-2xl text-slate-400 dark:text-slate-400 light:text-slate-600 text-sm sm:text-base leading-relaxed"
        >
          {description}
        </motion.p>
      )}
    </CurvedRollItem>
  );
};
