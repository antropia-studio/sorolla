import type { Mode } from '../../util/Mode';

import { EditTools, type EditToolsProps } from './EditTools';
import { SettingsTools, type SettingsToolsProps } from './SettingsTools';
import { TransformTools, type TransformToolsProps } from './TransformTools';

type ModeToProps = {
  none: EditToolsProps;
  settings: SettingsToolsProps;
  transform: TransformToolsProps;
};

/**
 * This convoluted props definition makes sure the Mode type and the Props type
 * in this component are always in sync (adding a new mode will make it fail
 * here unless defined, and misspelling a mode in here will also fail).
 */
type Props = {
  [M in Mode]: ModeToProps[M] & { mode: M };
}[Mode];

export const Tools = (props: Props) => {
  switch (props.mode) {
    case 'none':
      return <EditTools {...props} />;
    case 'settings':
      return <SettingsTools {...props} />;
    case 'transform':
      return <TransformTools {...props} />;
  }
};
