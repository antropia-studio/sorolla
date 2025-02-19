import { SorollaView } from '@antropia/sorolla';
import { StyleSheet, View } from 'react-native';

export default function App() {
  return (
    <View style={styles.container}>
      {/* eslint-disable-next-line @typescript-eslint/no-require-imports */}
      <SorollaView source={require('./cali.jpg')} style={styles.box} />
    </View>
  );
}

const styles = StyleSheet.create({
  box: {
    height: 60,
    marginVertical: 20,
    width: 60,
  },
  container: {
    alignItems: 'center',
    flex: 1,
    justifyContent: 'center',
  },
});
