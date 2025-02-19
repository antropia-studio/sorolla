import type { ViewProps } from 'react-native';
import type { Int32 } from 'react-native/Libraries/Types/CodegenTypes';

import codegenNativeComponent from 'react-native/Libraries/Utilities/codegenNativeComponent';

interface NativeProps extends ViewProps {
  /**
   * We only accept resolved images to simplify communication with native
   * components. The number required here is the result of calling
   * require('...').
   */
  source: Int32;
}

export default codegenNativeComponent<NativeProps>('SorollaView');
