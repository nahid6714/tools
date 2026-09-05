import React, { createContext, useContext, useState, useEffect, useRef, useCallback } from 'react';

interface RoadPerspectiveContextType {
  pitch: number; // Rotation around X-axis (degrees). Positive = top tilts away into distance (tapering like a road).
  roll: number;  // Rotation around Y-axis (degrees).
  isEnabled: boolean;
  setIsEnabled: (enabled: boolean) => void;
  toggleEnabled: () => void;
  hasGyro: boolean;
  isGyroActive: boolean;
  sensitivity: number;
  setSensitivity: (val: number) => void;
  calibrate: () => void;
  setStraight: () => void;
  setDepthMode: () => void;
  setRoadView: () => void;
  setPitchManual: (val: number) => void;
  setRollManual: (val: number) => void;
  requestGyroPermission: () => Promise<boolean>;
  activePreset: 'auto' | 'road' | 'depth' | 'straight' | 'manual';
}

const RoadPerspectiveContext = createContext<RoadPerspectiveContextType | undefined>(undefined);

export const RoadPerspectiveProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const [isEnabled, setIsEnabled] = useState(true);
  const [pitch, setPitch] = useState(0);
  const [roll, setRoll] = useState(0);
  const [hasGyro, setHasGyro] = useState(false);
  const [isGyroActive, setIsGyroActive] = useState(false);
  const [sensitivity, setSensitivity] = useState(1.0);
  const [activePreset, setActivePreset] = useState<'auto' | 'road' | 'depth' | 'straight' | 'manual'>('auto');

  // Internal refs for smooth 60fps RAF lerp loop
  const currentPitchRef = useRef(0);
  const currentRollRef = useRef(0);
  const targetPitchRef = useRef(0);
  const targetRollRef = useRef(0);
  const rawBetaRef = useRef(50);
  const rawGammaRef = useRef(0);
  const neutralBetaRef = useRef(50); // Default ergonomic phone holding angle (50° from horizontal)
  const isManualOverrideRef = useRef(false);
  const rafIdRef = useRef<number | null>(null);

  // Calibrate: set the user's current device holding angle as perfectly level / straight (0°)
  const calibrate = useCallback(() => {
    neutralBetaRef.current = rawBetaRef.current;
    targetPitchRef.current = 0;
    targetRollRef.current = 0;
    setActivePreset('straight');
  }, []);

  // Force straight / flat mode (0° Level Canvas)
  const setStraight = useCallback(() => {
    isManualOverrideRef.current = true;
    targetPitchRef.current = 0;
    targetRollRef.current = 0;
    setActivePreset('straight');
    setTimeout(() => {
      isManualOverrideRef.current = false;
    }, 4000);
  }, []);

  // Force 3D Spatial Depth Perspective (+22° pitch)
  const setDepthMode = useCallback(() => {
    isManualOverrideRef.current = true;
    targetPitchRef.current = 22;
    targetRollRef.current = 0;
    setActivePreset('depth');
    setTimeout(() => {
      isManualOverrideRef.current = false;
    }, 6000);
  }, []);

  const setRoadView = setDepthMode;

  const setPitchManual = useCallback((val: number) => {
    isManualOverrideRef.current = true;
    targetPitchRef.current = val;
    setActivePreset('manual');
  }, []);

  const setRollManual = useCallback((val: number) => {
    isManualOverrideRef.current = true;
    targetRollRef.current = val;
    setActivePreset('manual');
  }, []);

  const toggleEnabled = useCallback(() => {
    setIsEnabled((prev) => !prev);
  }, []);

  // Request iOS permission if needed
  const requestGyroPermission = useCallback(async (): Promise<boolean> => {
    if (
      typeof window !== 'undefined' &&
      typeof (DeviceOrientationEvent as unknown as { requestPermission?: () => Promise<string> }).requestPermission === 'function'
    ) {
      try {
        const response = await (DeviceOrientationEvent as unknown as { requestPermission: () => Promise<string> }).requestPermission();
        if (response === 'granted') {
          setIsGyroActive(true);
          return true;
        }
        return false;
      } catch (err) {
        console.warn('DeviceOrientation permission rejected:', err);
        return false;
      }
    }
    return true;
  }, []);

  // DeviceOrientation listener for mobile gyroscope
  useEffect(() => {
    if (!isEnabled) {
      targetPitchRef.current = 0;
      targetRollRef.current = 0;
      return;
    }

    let gyroReceived = false;

    const handleOrientation = (event: DeviceOrientationEvent) => {
      if (event.beta === null || event.gamma === null) return;

      gyroReceived = true;
      setHasGyro(true);
      setIsGyroActive(true);

      rawBetaRef.current = event.beta;
      rawGammaRef.current = event.gamma;

      if (isManualOverrideRef.current) return;

      // When the user tilts phone forward (screen moves towards horizontal, beta drops below neutral):
      // The top tilts away into the distance, tapering like a road going forward (pitch > 0).
      // When the user holds the phone straight upright (beta approaches 85-90°):
      // pitch goes to 0 or tilts backward.
      const deltaBeta = neutralBetaRef.current - event.beta;
      
      // Calculate pitch (-30° to +30°)
      const calculatedPitch = Math.max(-30, Math.min(30, deltaBeta * 0.9 * sensitivity));
      
      // Calculate roll (-22° to +22°)
      const calculatedRoll = Math.max(-22, Math.min(22, event.gamma * 0.7 * sensitivity));

      targetPitchRef.current = calculatedPitch;
      targetRollRef.current = calculatedRoll;
      setActivePreset('auto');
    };

    // Desktop pointer fallback when gyro is not sending events
    const handlePointerMove = (e: PointerEvent) => {
      if (gyroReceived || isManualOverrideRef.current) return;

      // Mouse Y: moving up tilts top away into distance (positive pitch), moving down tilts bottom away (negative pitch)
      const normY = (e.clientY / window.innerHeight) - 0.5; // -0.5 (top) to +0.5 (bottom)
      const normX = (e.clientX / window.innerWidth) - 0.5;  // -0.5 (left) to +0.5 (right)

      // Invert Y so moving mouse towards top creates the road vanishing perspective (top narrower)
      targetPitchRef.current = Math.max(-26, Math.min(26, -normY * 42 * sensitivity));
      targetRollRef.current = Math.max(-20, Math.min(20, normX * 28 * sensitivity));
      setActivePreset('auto');
    };

    window.addEventListener('deviceorientation', handleOrientation, true);
    window.addEventListener('pointermove', handlePointerMove, { passive: true });

    return () => {
      window.removeEventListener('deviceorientation', handleOrientation, true);
      window.removeEventListener('pointermove', handlePointerMove);
    };
  }, [isEnabled, sensitivity]);

  // Smooth 60fps RequestAnimationFrame damping loop
  useEffect(() => {
    let lastTime = performance.now();

    const loop = (time: number) => {
      const dt = Math.min(0.1, (time - lastTime) / 1000);
      lastTime = time;

      // Smooth lerp: spring damping factor
      const factor = isEnabled ? Math.min(1, dt * 14) : 0.2;
      currentPitchRef.current += (targetPitchRef.current - currentPitchRef.current) * factor;
      currentRollRef.current += (targetRollRef.current - currentRollRef.current) * factor;

      // Round to 2 decimal places to minimize unnecessary renders
      const roundedPitch = Math.round(currentPitchRef.current * 100) / 100;
      const roundedRoll = Math.round(currentRollRef.current * 100) / 100;

      setPitch(roundedPitch);
      setRoll(roundedRoll);

      rafIdRef.current = requestAnimationFrame(loop);
    };

    rafIdRef.current = requestAnimationFrame(loop);

    return () => {
      if (rafIdRef.current) {
        cancelAnimationFrame(rafIdRef.current);
      }
    };
  }, [isEnabled]);

  return (
    <RoadPerspectiveContext.Provider
      value={{
        pitch,
        roll,
        isEnabled,
        setIsEnabled,
        toggleEnabled,
        hasGyro,
        isGyroActive,
        sensitivity,
        setSensitivity,
        calibrate,
        setStraight,
        setRoadView,
        setPitchManual,
        setRollManual,
        requestGyroPermission,
        activePreset,
      }}
    >
      {children}
    </RoadPerspectiveContext.Provider>
  );
};

export const useRoadPerspective = () => {
  const context = useContext(RoadPerspectiveContext);
  if (!context) {
    throw new Error('useRoadPerspective must be used within a RoadPerspectiveProvider');
  }
  return context;
};
