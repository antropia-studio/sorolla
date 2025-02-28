import type { SvgProps } from 'react-native-svg';

import Svg, { Path } from 'react-native-svg';

export const AspectRatioIcon = (props: SvgProps) => (
  <Svg fill="none" height={24} width={24} {...props}>
    <Path
      d="M19 3H5a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2V5a2 2 0 0 0-2-2M3 9h18M9 21V9"
      stroke="#fff"
      strokeLinecap="round"
      strokeLinejoin="round"
      strokeWidth={2}
    />
  </Svg>
);
