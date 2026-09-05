import React, { useState } from 'react';
import { Smartphone, Compass, Sliders, Check, RefreshCw, Eye, Sparkles, X, Power } from 'lucide-react';
import { useRoadPerspective } from '../context/RoadPerspectiveContext';

export const RoadTiltHud: React.FC = () => {
  const {
    pitch,
    roll,
    isEnabled,
    toggleEnabled,
    hasGyro,
    isGyroActive,
    calibrate,
    setStraight,
    setRoadView,
    setPitchManual,
    requestGyroPermission,
    activePreset,
  } = useRoadPerspective();

  const [isOpen, setIsOpen] = useState(false);

  // Professional Status label
  const angleLabel = Math.abs(pitch) < 2 
    ? '0° (Level)' 
    : pitch > 0 
      ? `+${Math.round(pitch)}° (Depth)` 
      : `${Math.round(pitch)}° (Tilt)`;

  return (
    <div className="fixed bottom-5 left-5 z-50 font-sans pointer-events-auto">
      {/* Floating Pill Trigger */}
      <button
        onClick={() => setIsOpen(!isOpen)}
        className="flex items-center gap-2 px-3.5 py-2 rounded-full bg-slate-900/90 dark:bg-slate-900/90 light:bg-white/90 backdrop-blur-md border border-slate-700/70 dark:border-slate-700/70 light:border-slate-300 shadow-xl text-xs font-semibold text-slate-200 dark:text-slate-200 light:text-slate-800 hover:border-blue-500/60 hover:shadow-blue-500/10 transition-all group"
        title="3D Spatial Perspective Settings"
        aria-label="Toggle 3D Perspective Settings"
      >
        <div 
          className="w-4 h-4 rounded-full flex items-center justify-center bg-blue-500/20 text-blue-400 transition-transform duration-200"
          style={{ transform: `rotate(${-roll}deg)` }}
        >
          <Compass className="w-3 h-3 animate-spin-slow" />
        </div>
        <span className="text-[11px] text-slate-400 hidden sm:inline">3D Perspective:</span>
        <span className="text-[11px] font-bold text-blue-400">
          {isEnabled ? angleLabel : 'Off'}
        </span>
        <Sliders className="w-3 h-3 text-slate-400 group-hover:text-blue-400 transition-colors" />
      </button>

      {/* Popover Control Modal */}
      {isOpen && (
        <>
          <div 
            className="fixed inset-0 z-40 bg-transparent" 
            onClick={() => setIsOpen(false)} 
          />
          <div className="absolute bottom-12 left-0 z-50 w-80 p-4 rounded-2xl bg-slate-900/95 dark:bg-slate-900/95 light:bg-white/95 backdrop-blur-xl border border-slate-800 dark:border-slate-800 light:border-slate-200 shadow-2xl space-y-3.5 animate-in fade-in slide-in-from-bottom-2 duration-200">
            {/* Header */}
            <div className="flex items-center justify-between pb-2.5 border-b border-slate-800 dark:border-slate-800 light:border-slate-200">
              <div className="flex items-center gap-2">
                <div className="w-6 h-6 rounded-lg bg-blue-500/20 flex items-center justify-center text-blue-400">
                  <Smartphone className="w-3.5 h-3.5" />
                </div>
                <div>
                  <h4 className="text-xs font-bold text-slate-100 dark:text-slate-100 light:text-slate-900 leading-none">
                    3D Spatial Perspective
                  </h4>
                  <p className="text-[10px] text-slate-400 mt-0.5">
                    Dynamic Gyroscope & Motion Tilt
                  </p>
                </div>
              </div>
              <button
                onClick={() => setIsOpen(false)}
                className="text-slate-400 hover:text-slate-200 p-1 rounded-lg hover:bg-slate-800"
              >
                <X className="w-3.5 h-3.5" />
              </button>
            </div>

            {/* Live Angle Monitor */}
            <div className="grid grid-cols-2 gap-2 p-2.5 rounded-xl bg-slate-950/60 dark:bg-slate-950/60 light:bg-slate-100 border border-slate-800/80 dark:border-slate-800/80 light:border-slate-300 text-center">
              <div>
                <span className="block text-[10px] text-slate-400 uppercase tracking-wider font-semibold">
                  Pitch (X-Axis)
                </span>
                <span className="text-sm font-bold text-blue-400">
                  {pitch > 0 ? `+${pitch.toFixed(1)}°` : `${pitch.toFixed(1)}°`}
                </span>
                <span className="block text-[9px] text-slate-500">
                  {pitch > 2 ? 'Depth Mode' : pitch < -2 ? 'Reverse Tilt' : 'Level (0°)'}
                </span>
              </div>
              <div>
                <span className="block text-[10px] text-slate-400 uppercase tracking-wider font-semibold">
                  Roll (Y-Axis)
                </span>
                <span className="text-sm font-bold text-indigo-400">
                  {roll > 0 ? `+${roll.toFixed(1)}°` : `${roll.toFixed(1)}°`}
                </span>
                <span className="block text-[9px] text-slate-500">
                  {Math.abs(roll) < 2 ? 'Horizontal Level' : roll > 0 ? 'Right Tilt' : 'Left Tilt'}
                </span>
              </div>
            </div>

            {/* Sensor Status & iOS Permission */}
            <div className="flex items-center justify-between text-[11px] px-1 text-slate-400">
              <span>সেন্সর স্ট্যাটাস:</span>
              <span className={`font-semibold flex items-center gap-1 ${isGyroActive ? 'text-emerald-400' : 'text-amber-400'}`}>
                <span className={`w-1.5 h-1.5 rounded-full ${isGyroActive ? 'bg-emerald-400 animate-ping' : 'bg-amber-400'}`} />
                {isGyroActive ? 'জাইরোস্কোপ সক্রিয় (Active)' : 'পয়েন্টার / অটো মোড'}
              </span>
            </div>

            {/* Quick Action Presets */}
            <div className="space-y-1.5">
              {/* Preset 1: Spatial Depth Perspective */}
              <button
                onClick={setRoadView}
                className={`w-full flex items-center justify-between p-2 rounded-xl text-left transition-all ${
                  activePreset === 'road' || pitch >= 15
                    ? 'bg-blue-600/20 border border-blue-500/40 text-blue-300'
                    : 'hover:bg-slate-800/70 text-slate-300 border border-transparent'
                }`}
              >
                <div className="flex items-center gap-2">
                  <Sparkles className="w-3.5 h-3.5 text-blue-400 shrink-0" />
                  <div>
                    <span className="text-xs font-semibold block leading-tight">
                      Spatial Depth Mode (+22°)
                    </span>
                    <span className="text-[10px] text-slate-400 block leading-tight">
                      স্বাভাবিক ও প্রফেশনাল থ্রি-ডি পার্সপেক্টিভ ডেপথ ইফেক্ট
                    </span>
                  </div>
                </div>
                {(activePreset === 'road' || pitch >= 15) && <Check className="w-3.5 h-3.5 text-blue-400 shrink-0" />}
              </button>

              {/* Preset 2: Standard Flat View */}
              <button
                onClick={setStraight}
                className={`w-full flex items-center justify-between p-2 rounded-xl text-left transition-all ${
                  activePreset === 'straight' || Math.abs(pitch) < 2
                    ? 'bg-emerald-600/20 border border-emerald-500/40 text-emerald-300'
                    : 'hover:bg-slate-800/70 text-slate-300 border border-transparent'
                }`}
              >
                <div className="flex items-center gap-2">
                  <Smartphone className="w-3.5 h-3.5 text-emerald-400 shrink-0" />
                  <div>
                    <span className="text-xs font-semibold block leading-tight">
                      Standard Flat View (0° Level)
                    </span>
                    <span className="text-[10px] text-slate-400 block leading-tight">
                      স্বাভাবিক সমতল টু-ডি ডিসপ্লে (Flat Canvas)
                    </span>
                  </div>
                </div>
                {(activePreset === 'straight' || Math.abs(pitch) < 2) && <Check className="w-3.5 h-3.5 text-emerald-400 shrink-0" />}
              </button>

              {/* Action: Calibrate Current Posture */}
              <button
                onClick={calibrate}
                className="w-full flex items-center justify-between p-2 rounded-xl text-left hover:bg-slate-800/70 text-slate-300 transition-colors border border-transparent"
              >
                <div className="flex items-center gap-2">
                  <RefreshCw className="w-3.5 h-3.5 text-cyan-400 shrink-0" />
                  <div>
                    <span className="text-xs font-semibold block leading-tight">
                      Calibrate Neutral Angle (বেসলাইন সেট)
                    </span>
                    <span className="text-[10px] text-slate-400 block leading-tight">
                      আপনার বর্তমান হোল্ডিং পজিশনকে লেভেল (0°) হিসেবে সেট করবে
                    </span>
                  </div>
                </div>
              </button>
            </div>

            {/* Manual Pitch Angle Slider */}
            <div className="pt-2 border-t border-slate-800 dark:border-slate-800 light:border-slate-200 space-y-1">
              <div className="flex justify-between text-[10px] text-slate-400">
                <span>ম্যানুয়াল টিল্ট অ্যাডজাস্টমেন্ট (Manual Tilt):</span>
                <span className="font-semibold text-slate-200">{pitch}°</span>
              </div>
              <input
                type="range"
                min="-26"
                max="26"
                step="1"
                value={Math.round(pitch)}
                onChange={(e) => setPitchManual(Number(e.target.value))}
                className="w-full h-1.5 bg-slate-700 rounded-lg appearance-none cursor-pointer accent-blue-500"
              />
              <div className="flex justify-between text-[9px] text-slate-500">
                <span>Inverted (-26°)</span>
                <span>Level (0°)</span>
                <span>Spatial Depth (+26°)</span>
              </div>
            </div>

            {/* Toggle On/Off & iOS Permission */}
            <div className="pt-1 flex items-center justify-between">
              {!hasGyro && (
                <button
                  onClick={requestGyroPermission}
                  className="text-[10px] text-blue-400 hover:underline flex items-center gap-1"
                >
                  <Eye className="w-3 h-3" />
                  <span>আইফোন জাইরো অ্যাক্সেস</span>
                </button>
              )}
              <button
                onClick={toggleEnabled}
                className={`ml-auto flex items-center gap-1.5 px-3 py-1.5 rounded-lg text-xs font-semibold transition-colors ${
                  isEnabled
                    ? 'bg-blue-600/20 text-blue-300 border border-blue-500/30'
                    : 'bg-slate-800 text-slate-400'
                }`}
              >
                <Power className="w-3 h-3" />
                <span>{isEnabled ? '3D মোশন সক্রিয়' : '3D মোশন বন্ধ'}</span>
              </button>
            </div>
          </div>
        </>
      )}
    </div>
  );
};
