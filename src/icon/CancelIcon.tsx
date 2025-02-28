import Svg, { Path, type SvgProps } from 'react-native-svg';

export const CancelIcon = (props: SvgProps) => (
  <Svg fill="none" height={24} width={24} {...props}>
    <Path
      d="M12 22c5.523 0 10-4.477 10-10S17.523 2 12 2 2 6.477 2 12s4.477 10 10 10M15 9l-6 6M9 9l6 6"
      stroke="#fff"
      strokeLinecap="round"
      strokeLinejoin="round"
      strokeWidth={2}
    />
  </Svg>
);
