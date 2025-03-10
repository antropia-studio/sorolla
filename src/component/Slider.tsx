import { useMemo, useRef, useState } from 'react';
import {
  Animated,
  Dimensions,
  PanResponder,
  StyleSheet,
  Text,
  View,
} from 'react-native';

import type { Range } from '../util/Range';

import { getRangeMidValue, isWithin, lerp } from '../util/math';
import { theme } from './theme';

export type SlideEvent = { name: string } & (
  | { type: 'change'; value: number }
  | { type: 'end' }
  | { type: 'start' }
);

export interface SliderProps {
  name: string;
  onSlide: (event: SlideEvent) => void;
  range: Range;
}

export type SlideState = 'change' | 'end' | 'start';

const PADDING = 20;

export const Slider = ({ name, onSlide, range }: SliderProps) => {
  const [value, setValue] = useState(getRangeMidValue(range));
  const componentWidth = useMemo(
    () => Dimensions.get('screen').width - PADDING * 2,
    []
  );
  const handlePanX = useRef(new Animated.Value(1)).current;
  const activeSliderLeft = useRef(new Animated.Value(0.5)).current;
  const activeSliderRight = useRef(new Animated.Value(0.5)).current;

  const panResponder = useMemo(
    () =>
      PanResponder.create({
        onMoveShouldSetPanResponder: (_evt, gestureState) =>
          Math.abs(gestureState.dx) > Math.abs(gestureState.dy),
        onMoveShouldSetPanResponderCapture: (_evt, gestureState) =>
          Math.abs(gestureState.dx) > Math.abs(gestureState.dy),
        onPanResponderGrant: () => {
          handlePanX.extractOffset();
          onSlide({ name, type: 'start' });
        },
        onPanResponderMove: (_, gestureState) => {
          const percentage = (gestureState.moveX - PADDING) / componentWidth;

          if (percentage < 0 || percentage > 1) return;

          handlePanX.setValue(gestureState.dx);
          activeSliderLeft.setValue(Math.min(percentage, 0.5));
          activeSliderRight.setValue(Math.max(percentage, 0.5));
          setValue(lerp(percentage, range));
          onSlide({ name, type: 'change', value: lerp(percentage, range) });
        },
        onPanResponderRelease: () => {
          handlePanX.flattenOffset();
          onSlide({ name, type: 'end' });
        },
        onPanResponderTerminationRequest: () => false,
        onStartShouldSetPanResponder: () => true,
      }),
    [range]
  );

  const activeSliderLeftValue = activeSliderLeft.interpolate({
    inputRange: [0, 1],
    outputRange: ['0%', '100%'],
  });

  const activeSliderRightValue = activeSliderRight.interpolate({
    inputRange: [0, 1],
    outputRange: ['100%', '0%'],
  });

  return (
    <View style={styles.container}>
      <View style={styles.topRow}>
        <Text style={styles.name}>{name}</Text>

        {!isWithin(value, getRangeMidValue(range)) && (
          <Text
            style={styles.handleValueText}
          >{`${value < 0 ? '-' : '+'} ${Math.abs(value).toFixed(2)}`}</Text>
        )}
      </View>
      <View style={styles.sliderContainer}>
        <View style={styles.inactiveSliderLine} />
        <View style={styles.centerMarker} />
        <Animated.View
          style={[
            styles.activeSliderLine,
            { left: activeSliderLeftValue, right: activeSliderRightValue },
          ]}
        />
        <Animated.View
          style={[styles.handle, { transform: [{ translateX: handlePanX }] }]}
          {...panResponder.panHandlers}
        />
      </View>
    </View>
  );
};

const styles = StyleSheet.create({
  activeSliderLine: {
    backgroundColor: theme.color.white,
    bottom: 0,
    height: 3,
    position: 'absolute',
    top: 9,
  },
  centerMarker: {
    backgroundColor: theme.color.white,
    borderRadius: 1,
    height: 12,
    left: '50%',
    position: 'absolute',
    width: 4,
  },
  container: {
    alignItems: 'flex-start',
    flexDirection: 'column',
    justifyContent: 'flex-start',
  },
  handle: {
    backgroundColor: theme.color.white,
    borderRadius: 10,
    bottom: 0,
    height: 20,
    left: '48%',
    position: 'absolute',
    top: 0,
    width: 20,
    zIndex: 1,
  },
  handleValueText: {
    color: theme.color.white,
  },
  inactiveSliderLine: {
    backgroundColor: theme.color.gray,
    borderRadius: 4,
    bottom: 0,
    height: 2,
    left: 0,
    position: 'absolute',
    right: 0,
    top: 9,
    width: '100%',
  },
  name: {
    color: theme.color.white,
    marginBottom: 4,
  },
  rangeContainer: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    width: '100%',
  },
  rangeText: {
    color: theme.color.white,
  },
  sliderContainer: {
    alignItems: 'center',
    flexDirection: 'row',
    height: 20,
    width: '100%',
  },
  topRow: {
    alignItems: 'center',
    flexDirection: 'row',
    justifyContent: 'space-between',
    width: '100%',
  },
});
