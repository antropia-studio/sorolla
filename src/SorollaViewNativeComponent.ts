import type { ViewProps } from 'react-native';
import type { BubblingEventHandler } from 'react-native/Libraries/Types/CodegenTypes';

import codegenNativeComponent from 'react-native/Libraries/Utilities/codegenNativeComponent';

interface EditFinishEvent {
  uri: string;
}

interface NativeProps extends ViewProps {
  onEditFinish: BubblingEventHandler<Readonly<EditFinishEvent>>;
  uri: string;
}

export default codegenNativeComponent<NativeProps>('SorollaView');
