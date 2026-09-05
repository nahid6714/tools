import React from 'react';

interface CurvedRollItemProps {
  children: React.ReactNode;
  className?: string;
  intensityMultiplier?: number;
}

/**
 * CurvedRollItem:
 * Normal clean container - cylinder/drum scroll rotation removed per user request:
 * "আমি চাইনা গোল গোল হয়ে স্ক্রল হোক"
 */
export const CurvedRollItem: React.FC<CurvedRollItemProps> = ({
  children,
  className = '',
}) => {
  return <div className={className}>{children}</div>;
};
