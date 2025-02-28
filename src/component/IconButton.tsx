import { Pressable, type PressableProps, type ViewProps } from 'react-native';

export const IconButton = ({
  children,
  style,
  ...props
}: Pick<ViewProps, 'style'> & PressableProps) => {
  return (
    <Pressable
      style={[
        {
          alignItems: 'center',
          height: 48,
          justifyContent: 'center',
          width: 48,
        },
        style,
      ]}
      {...props}
    >
      {children}
    </Pressable>
  );
};
