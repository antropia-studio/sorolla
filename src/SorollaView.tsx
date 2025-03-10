import { useCallback, useRef, useState } from 'react';
import { Dimensions, Pressable, Text, View } from 'react-native';

import type { SettingsName } from './component/tool/SettingsTools';
import type { Mode } from './util/Mode';

import { IconButton } from './component/IconButton';
import { Tools } from './component/tool';
import { Icon } from './icon';
import {
  Commands,
  type NativeProps,
  default as NativeSorollaView,
} from './SorollaViewNativeComponent';

export interface SorollaViewProps
  extends Omit<NativeProps, 'mode' | 'settings'> {}

export const SorollaView = ({ style, ...props }: SorollaViewProps) => {
  const nativeRef = useRef<InstanceType<typeof NativeSorollaView> | null>(null);
  const [mode, setMode] = useState<Mode>('none');
  const [brightness, setBrightness] = useState(0);
  const [saturation, setSaturation] = useState(0);
  const [contrast, setContrast] = useState(0);

  const onSettingsChange = useCallback((name: SettingsName, value: number) => {
    switch (name) {
      case 'Brightness':
        setBrightness(value);
        break;
      case 'Contrast':
        setContrast(value);
        break;
      case 'Saturation':
        setSaturation(value);
        break;
    }
  }, []);

  return (
    <View
      style={{
        alignItems: 'center',
        backgroundColor: '#101010',
        flex: 1,
        flexDirection: 'column',
        gap: 16,
        justifyContent: 'center',
      }}
    >
      <NativeSorollaView
        mode={mode}
        ref={nativeRef}
        settings={{ brightness, contrast, saturation }}
        style={[
          {
            height: Dimensions.get('screen').height * 0.5,
            width: Dimensions.get('screen').width,
          },
          style,
        ]}
        {...props}
      />

      <View
        style={{
          alignItems: 'center',
          flexDirection: 'column',
          gap: 16,
          justifyContent: 'center',
        }}
      >
        <View
          style={{
            flexDirection: 'row',
            gap: 16,
            justifyContent: 'space-between',
            paddingHorizontal: 8,
          }}
        >
          {(mode === 'transform' || mode === 'settings') && (
            <IconButton
              onPress={() => {
                if (nativeRef.current) {
                  Commands.cancelTransform(nativeRef.current);
                }
                setMode('none');
              }}
            >
              <Icon.Cancel />
            </IconButton>
          )}

          <Tools
            mode={mode}
            onAspectRatioPress={() => {}}
            onMirrorHorizontallyPress={() => {
              if (nativeRef.current) {
                Commands.mirrorHorizontally(nativeRef.current);
              }
            }}
            onMirrorVerticallyPress={() => {
              if (nativeRef.current) {
                Commands.mirrorVertically(nativeRef.current);
              }
            }}
            onPaintPress={() => {}}
            onRotateCcwPress={() => {
              if (nativeRef.current) {
                Commands.rotateCcw(nativeRef.current);
              }
            }}
            onSettingsChange={onSettingsChange}
            onSettingsPress={() => setMode('settings')}
            onTransformPress={() => setMode('transform')}
          />

          {(mode === 'transform' || mode === 'settings') && (
            <IconButton onPress={() => setMode('none')}>
              <Icon.Accept />
            </IconButton>
          )}
        </View>

        <Pressable
          onPress={() => {
            if (nativeRef.current) {
              Commands.acceptEdition(nativeRef.current);
            }
          }}
          style={{
            backgroundColor: '#0130FF',
            borderRadius: 8,
            paddingHorizontal: 16,
            paddingVertical: 8,
          }}
        >
          <Text style={{ color: 'white', fontSize: 16, fontWeight: 'bold' }}>
            Accept
          </Text>
        </Pressable>
      </View>
    </View>
  );
};
