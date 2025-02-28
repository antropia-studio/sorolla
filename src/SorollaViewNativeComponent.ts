import type { ViewProps } from 'react-native';
import type { BubblingEventHandler } from 'react-native/Libraries/Types/CodegenTypes';

import codegenNativeComponent from 'react-native/Libraries/Utilities/codegenNativeComponent';

export type Mode = 'none' | 'transform';

export interface NativeProps extends ViewProps {
  onEditFinish: BubblingEventHandler<Readonly<EditFinishEvent>>;
  uri: string;
}

interface EditFinishEvent {
  uri: string;
}

export default codegenNativeComponent<NativeProps>('SorollaView');
