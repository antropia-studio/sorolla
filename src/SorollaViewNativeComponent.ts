import type { HostComponent, ViewProps } from 'react-native';
import type {
  BubblingEventHandler,
  WithDefault,
} from 'react-native/Libraries/Types/CodegenTypes';

import React from 'react';
import codegenNativeCommands from 'react-native/Libraries/Utilities/codegenNativeCommands';
import codegenNativeComponent from 'react-native/Libraries/Utilities/codegenNativeComponent';

export type Mode = 'none' | 'transform';

export interface NativeCommands {
  cancelTransform: (
    viewRef: React.ElementRef<HostComponent<NativeProps>>
  ) => void;
}

export const Commands: NativeCommands = codegenNativeCommands<NativeCommands>({
  supportedCommands: ['cancelTransform'],
});

export interface NativeProps extends ViewProps {
  backgroundColor?: string;
  mode?: WithDefault<Mode, 'none'>;
  onEditFinish: BubblingEventHandler<Readonly<EditFinishEvent>>;
  uri: string;
}

interface EditFinishEvent {
  uri: string;
}

export default codegenNativeComponent<NativeProps>('SorollaView');
