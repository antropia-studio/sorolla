import { useState } from 'react';
import { Dimensions, View } from 'react-native';

import { IconButton } from './component/IconButton';
import { Tools } from './component/tool';
import { Icon } from './icon';
import {
  type Mode,
  type NativeProps,
  default as NativeSorollaView,
} from './SorollaViewNativeComponent';

export interface SorollaViewProps extends NativeProps {}

export const SorollaView = ({ style, ...props }: SorollaViewProps) => {
  const [mode, setMode] = useState<Mode>('none');

  return (
    <View
      style={{
        alignItems: 'center',
        flex: 1,
        flexDirection: 'column',
        gap: 16,
        justifyContent: 'center',
      }}
    >
      <NativeSorollaView
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
          <IconButton onPress={() => setMode('none')}>
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
