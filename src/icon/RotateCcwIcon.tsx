import Svg, { Path, type SvgProps } from 'react-native-svg';

export const RotateCcwIcon = (props: SvgProps) => (
  <Svg fill="none" height={24} width={24} {...props}>
    <Path
      d="M1 4v6h6"
      stroke="#fff"
      strokeLinecap="round"
      strokeLinejoin="round"
      strokeWidth={2}
    />
    <Path
      d="M3.51 15a9 9 0 1 0 2.13-9.36L1 10"
      stroke="#fff"
      strokeLinecap="round"
      strokeLinejoin="round"
      strokeWidth={2}
    />
  </Svg>
);
