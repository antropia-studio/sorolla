import type { Range } from './Range';

/**
 * Calculates the middle value of a given range by averaging its maximum and minimum values.
 */
export const getRangeMidValue = ({ max, min }: Range): number =>
  (max + min) / 2;

/**
 * Calculates the percentage representation of a given value within a specified range.
 * The result is clamped between 0 and 1 to ensure it remains within the valid percentage bounds.
 */
export const getRangePercentage = (
  value: number,
  { max, min }: Range
): number => {
  const percentage = (value - min) / (max - min);

  return clamp(percentage, { max: 1, min: 0 });
};

/**
 * Performs a linear interpolation (lerp) on a given value within a specified range.
 *
 * The function calculates the interpolated value based on the normalized input and
 * ensures the result is clamped within the provided range.
 */
export const lerp = (value: number, { max, min }: Range): number => {
  const interpolatedValue = min + (max - min) * value;

  return clamp(interpolatedValue, { max, min });
};

/**
 * Clamps a given number within the boundaries of a specified range.
 */
export const clamp = (value: number, { max, min }: Range): number =>
  Math.max(min, Math.min(value, max));

/**
 * Checks if a given value is within a specified precision range of a reference value.
 * The range is defined as [referenceValue - precision, referenceValue + precision].
 */
export const isWithin = (
  value: number,
  referenceValue: number,
  precision: number = 0.01
): boolean => {
  return (
    value >= referenceValue - precision && value <= referenceValue + precision
  );
};
