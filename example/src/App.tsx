import { SorollaView } from '@antropia/sorolla';
import { useCallback, useState } from 'react';
import { Pressable, SafeAreaView, Text, View } from 'react-native';
import { launchImageLibrary } from 'react-native-image-picker';

export default function App() {
  const [imageUri, setImageUri] = useState<string | undefined>();

  const pickImage = useCallback(async () => {
    const response = await launchImageLibrary({
      mediaType: 'photo',
      selectionLimit: 1,
    });

    const assets = response.assets ?? [];
    if (assets.length === 0) return;

    setImageUri(assets[0]!.uri);
  }, []);

  return (
    <SafeAreaView
      style={{
        alignItems: 'center',
        backgroundColor: '#0e0e0e',
        flex: 1,
        justifyContent: 'center',
      }}
    >
      {imageUri ? (
        <View
          style={{
            alignItems: 'center',
            flexDirection: 'column',
            height: '100%',
            justifyContent: 'center',
            width: '100%',
          }}
        >
          <SorollaView
            style={{ height: '90%', width: '100%' }}
            uri={imageUri}
          />

          <Pressable
            onPress={pickImage}
            style={{
              backgroundColor: '#F06970',
              borderRadius: 8,
              paddingHorizontal: 16,
              paddingVertical: 8,
            }}
          >
            <Text style={{ fontSize: 16, fontWeight: 'bold' }}>
              Select image
            </Text>
          </Pressable>
        </View>
      ) : (
        <Pressable
          onPress={pickImage}
          style={{
            backgroundColor: '#F06970',
            borderRadius: 8,
            paddingHorizontal: 16,
            paddingVertical: 8,
          }}
        >
          <Text style={{ fontSize: 24, fontWeight: 'bold' }}>Select image</Text>
        </Pressable>
      )}
    </SafeAreaView>
  );
}
