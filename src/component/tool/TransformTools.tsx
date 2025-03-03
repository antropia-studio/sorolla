import { View } from 'react-native';

import { Icon } from '../../icon';
import { IconButton } from '../IconButton';

export interface TransformToolsProps {
  onAspectRatioPress: () => void;
  onMirrorHorizontallyPress: () => void;
  onMirrorVerticallyPress: () => void;
  onRotateCcwPress: () => void;
}

export const TransformTools = ({
  onAspectRatioPress,
  onMirrorHorizontallyPress,
  onMirrorVerticallyPress,
  onRotateCcwPress,
}: TransformToolsProps) => {
  return (
    <View
      style={{
        flex: 1,
        flexDirection: 'row',
        gap: 24,
        justifyContent: 'center',
      }}
    >
      <IconButton onPress={onRotateCcwPress}>
        <Icon.RotateCcw />
      </IconButton>

      <IconButton onPress={onMirrorHorizontallyPress}>
        <Icon.MirrorHorizontally />
      </IconButton>

      <IconButton onPress={onMirrorVerticallyPress}>
        <Icon.MirrorVertically />
      </IconButton>

      <IconButton onPress={onAspectRatioPress}>
        <Icon.AspectRatio />
      </IconButton>
    </View>
  );
};
