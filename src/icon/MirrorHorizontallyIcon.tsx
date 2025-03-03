import type { SvgProps } from 'react-native-svg';

import Svg, { Path } from 'react-native-svg';

export const MirrorHorizontallyIcon = (props: SvgProps) => (
  <Svg fill="none" height={24} width={24} {...props}>
    <Path
      d="m8 18-6-6 6-6M12 18V6M16 18l6-6-6-6"
      stroke="#fff"
      strokeLinecap="round"
      strokeLinejoin="round"
      strokeWidth={2}
    />
  </Svg>
);
