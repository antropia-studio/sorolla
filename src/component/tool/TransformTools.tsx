import { View } from 'react-native';

import { Icon } from '../../icon';
import { IconButton } from '../IconButton';

interface Props {
  onAspectRatioPress: () => void;
  onMirrorPress: () => void;
  onRotatePress: () => void;
}

export const TransformTools = ({
  onAspectRatioPress,
  onMirrorPress,
  onRotatePress,
}: Props) => {
  return (
    <View
      style={{
        flex: 1,
        flexDirection: 'row',
        gap: 24,
        justifyContent: 'center',
      }}
    >
      <IconButton onPress={onRotatePress}>
        <Icon.Rotate />
      </IconButton>

      <IconButton onPress={onMirrorPress}>
        <Icon.Mirror />
      </IconButton>

      <IconButton onPress={onAspectRatioPress}>
        <Icon.AspectRatio />
      </IconButton>
    </View>
  );
};
