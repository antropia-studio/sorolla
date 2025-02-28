import type { SvgProps } from 'react-native-svg';

import Svg, { Path } from 'react-native-svg';

export const SettingsIcon = (props: SvgProps) => (
  <Svg fill="none" height={24} width={24} {...props}>
    <Path
      d="M4 21v-7M4 10V3M12 21v-9M12 8V3M20 21v-5M20 12V3M1 14h6M9 8h6M17 16h6"
      stroke="#fff"
      strokeLinecap="round"
      strokeLinejoin="round"
      strokeWidth={2}
    />
  </Svg>
);
