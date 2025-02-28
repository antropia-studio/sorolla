import type { SvgProps } from 'react-native-svg';

import Svg, { Path } from 'react-native-svg';

export const AcceptIcon = (props: SvgProps) => (
  <Svg fill="none" height={24} width={24} {...props}>
    <Path
      d="M22 11.08V12a10 10 0 1 1-5.93-9.14"
      stroke="#fff"
      strokeLinecap="round"
      strokeLinejoin="round"
      strokeWidth={2}
    />
    <Path
      d="M22 4 12 14.01l-3-3"
      stroke="#fff"
      strokeLinecap="round"
      strokeLinejoin="round"
      strokeWidth={2}
    />
  </Svg>
);
