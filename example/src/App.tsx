import { SorollaView } from '@antropia/sorolla';
import { useState } from 'react';
import { Pressable, StyleSheet, Text, View } from 'react-native';
import { launchImageLibrary } from 'react-native-image-picker';

export default function App() {
  const [imageUri, setImageUri] = useState<string | undefined>();

  return (
    <View style={styles.container}>
      {!imageUri && (
        <Pressable
          onPress={async () => {
            const response = await launchImageLibrary({
              mediaType: 'photo',
              selectionLimit: 1,
            });

            const assets = response.assets ?? [];

            if (assets.length > 0) {
              setImageUri(assets[0]!.uri);
            }
          }}
        >
          <Text>Select image</Text>
        </Pressable>
      )}
      {imageUri && <SorollaView style={styles.box} uri={imageUri} />}
    </View>
  );
}

const styles = StyleSheet.create({
  box: {
    height: '100%',
    marginVertical: 20,
    width: '100%',
  },
  container: {
    alignItems: 'center',
    flex: 1,
    justifyContent: 'center',
  },
});
