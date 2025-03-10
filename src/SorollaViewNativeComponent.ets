import type { HostComponent, ViewProps } from 'react-native';
import type {
  BubblingEventHandler,
  Float,
  WithDefault,
} from 'react-native/Libraries/Types/CodegenTypes';

import React from 'react';
import codegenNativeCommands from 'react-native/Libraries/Utilities/codegenNativeCommands';
import codegenNativeComponent from 'react-native/Libraries/Utilities/codegenNativeComponent';

<%- disclaimer %>

<%- modeType %>
export interface NativeCommands {
  acceptEdition: (
    viewRef: React.ElementRef<HostComponent<NativeProps>>
  ) => void;
  cancelTransform: (
    viewRef: React.ElementRef<HostComponent<NativeProps>>
  ) => void;
  mirrorHorizontally: (
    viewRef: React.ElementRef<HostComponent<NativeProps>>
  ) => void;
  mirrorVertically: (
    viewRef: React.ElementRef<HostComponent<NativeProps>>
  ) => void;
  rotateCcw: (viewRef: React.ElementRef<HostComponent<NativeProps>>) => void;
}

export const Commands: NativeCommands = codegenNativeCommands<NativeCommands>({
  supportedCommands: [
    'acceptEdition',
    'cancelTransform',
    'mirrorHorizontally',
    'mirrorVertically',
    'rotateCcw',
  ],
});

export interface NativeProps extends ViewProps {
  backgroundColor?: string;
  mode?: WithDefault<Mode, 'none'>;
  onEditFinish: BubblingEventHandler<Readonly<EditFinishEvent>>;
  settings: {
    brightness: Float;
    contrast: Float;
    saturation: Float;
  };
  uri: string;
}

interface EditFinishEvent {
  uri: string;
}

export default codegenNativeComponent<NativeProps>('SorollaView');
