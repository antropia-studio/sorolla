import { View } from 'react-native';

import { Icon } from '../../icon';
import { IconButton } from '../IconButton';

export interface EditToolsProps {
  onPaintPress: () => void;
  onSettingsPress: () => void;
  onTransformPress: () => void;
}

export const EditTools = ({
  onPaintPress,
  onSettingsPress,
  onTransformPress,
}: EditToolsProps) => {
  return (
    <View
      style={{
        flex: 1,
        flexDirection: 'row',
        gap: 24,
        justifyContent: 'center',
      }}
    >
      <IconButton onPress={onTransformPress}>
        <Icon.Transform />
      </IconButton>

      <IconButton onPress={onPaintPress}>
        <Icon.Paint />
      </IconButton>

      <IconButton onPress={onSettingsPress}>
        <Icon.Settings />
      </IconButton>
    </View>
  );
};
