import Svg, { G, Mask, Path, type SvgProps } from 'react-native-svg';

export const PaintIcon = (props: SvgProps) => (
  <Svg fill="none" height={24} width={24} {...props}>
    <Mask
      height={22}
      id="a"
      maskUnits="userSpaceOnUse"
      style={{
        maskType: 'alpha',
      }}
      width={22}
      x={1}
      y={1}
    >
      <Path
        d="M12 23c6.075 0 11-4.925 11-11S18.075 1 12 1 1 5.925 1 12s4.925 11 11 11"
        fill="#D9D9D9"
      />
    </Mask>
    <G
      mask="url(#a)"
      stroke="#fff"
      strokeLinecap="round"
      strokeLinejoin="round"
      strokeWidth={2}
    >
      <Path d="M12 22c5.523 0 10-4.477 10-10S17.523 2 12 2 2 6.477 2 12s4.477 10 10 10" />
      <Path d="M14.743 32.042a2.829 2.829 0 0 1-5.657 0V12.95L11.914 8l2.829 4.95z" />
    </G>
  </Svg>
);
