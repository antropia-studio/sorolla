import type { SvgProps } from 'react-native-svg';

import Svg, { ClipPath, Defs, G, Path } from 'react-native-svg';

export const TransformIcon = (props: SvgProps) => (
  <Svg fill="none" height={24} width={24} {...props}>
    <G
      clipPath="url(#a)"
      stroke="#fff"
      strokeLinecap="round"
      strokeLinejoin="round"
      strokeWidth={2}
    >
      <Path d="M6.13 1 6 16a2 2 0 0 0 2 2h15" />
      <Path d="M1 6.13 16 6a2 2 0 0 1 2 2v15" />
    </G>
    <Defs>
      <ClipPath id="a">
        <Path d="M0 0h24v24H0z" fill="#fff" />
      </ClipPath>
    </Defs>
  </Svg>
);
