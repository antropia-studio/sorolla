import Svg, { Path, type SvgProps } from 'react-native-svg';

export const MirrorVerticallyIcon = (props: SvgProps) => (
  <Svg fill="none" height={24} width={24} {...props}>
    <Path
      d="m18 16-6 6-6-6M18 12H6M18 8l-6-6-6 6"
      stroke="#fff"
      strokeLinecap="round"
      strokeLinejoin="round"
      strokeWidth={2}
    />
  </Svg>
);
