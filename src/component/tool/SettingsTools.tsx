import { useCallback, useMemo, useState } from 'react';
import { FlatList, StyleSheet } from 'react-native';

import { type SlideEvent, Slider, type SliderProps } from '../Slider';

export type SettingsName = 'Brightness' | 'Contrast' | 'Saturation';

export interface SettingsToolsProps {
  onSettingsChange: (name: SettingsName, value: number) => void;
}

type Item = Omit<SliderProps, 'onSlide'> & { name: SettingsName };

export const SettingsTools = ({ onSettingsChange }: SettingsToolsProps) => {
  const [allowScroll, setAllowScroll] = useState(false);

  const items: Item[] = useMemo(
    () => [
      { name: 'Brightness', range: { max: 1, min: -1 } },
      { name: 'Contrast', range: { max: 1, min: -1 } },
      { name: 'Saturation', range: { max: 1, min: -1 } },
    ],
    []
  );

  const onSlide = useCallback(
    (event: SlideEvent) => {
      switch (event.type) {
        case 'change':
          onSettingsChange(event.name as SettingsName, event.value);
          break;
        case 'end':
          setAllowScroll(true);
          break;
        case 'start':
          setAllowScroll(false);
          break;
      }
    },
    [onSettingsChange]
  );

  return (
    <FlatList
      contentContainerStyle={styles.scrollViewContent}
      data={items}
      renderItem={({ item }) => (
        <Slider name={item.name} onSlide={onSlide} range={item.range} />
      )}
      scrollEnabled={allowScroll}
      style={styles.scrollView}
    />
  );
};

const styles = StyleSheet.create({
  scrollView: { flex: 1 },
  scrollViewContent: { gap: 16, paddingBottom: 24, paddingHorizontal: 16 },
});
