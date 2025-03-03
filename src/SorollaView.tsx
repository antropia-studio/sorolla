import { useRef, useState } from 'react';
import { Dimensions, View } from 'react-native';

import { IconButton } from './component/IconButton';
import { Tools } from './component/tool';
import { Icon } from './icon';
import {
  Commands,
  type Mode,
  type NativeProps,
  default as NativeSorollaView,
} from './SorollaViewNativeComponent';

export interface SorollaViewProps extends Omit<NativeProps, 'mode'> {}

export const SorollaView = ({ style, ...props }: SorollaViewProps) => {
  const nativeRef = useRef<InstanceType<typeof NativeSorollaView> | null>(null);
  const [mode, setMode] = useState<Mode>('none');

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
        style={[
          {
            height: Dimensions.get('screen').height * 0.7,
            width: Dimensions.get('screen').width,
          },
          style,
        ]}
        {...props}
      />

      <View
        style={{
          flexDirection: 'row',
          gap: 16,
          justifyContent: 'space-between',
          paddingHorizontal: 8,
        }}
      >
        {mode === 'transform' && (
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

        {mode === 'none' ? (
          <Tools.Edit
            onPaintPress={() => {}}
            onSettingsPress={() => {}}
            onTransformPress={() => setMode('transform')}
          />
        ) : (
          <Tools.Transform
            onAspectRatioPress={() => {}}
            onMirrorPress={() => {}}
            onRotatePress={() => {}}
          />
        )}

        {mode === 'transform' && (
          <IconButton onPress={() => setMode('none')}>
            <Icon.Accept />
          </IconButton>
        )}
      </View>
    </View>
  );
};
