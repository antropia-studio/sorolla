import { SorollaView } from '@antropia/sorolla';
import { useCallback, useState } from 'react';
import {
  Dimensions,
  Image,
  Pressable,
  SafeAreaView,
  Text,
  View,
} from 'react-native';
import { launchImageLibrary } from 'react-native-image-picker';

export default function App() {
  const [imageUri, setImageUri] = useState<string | undefined>();
  const [editedImageUri, setEditedImageUri] = useState<string | undefined>();

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
        backgroundColor: '#101010',
        flex: 1,
        justifyContent: 'center',
      }}
    >
      {editedImageUri ? (
        <Pressable
          onPress={() => {
            setEditedImageUri(undefined);
            setImageUri(undefined);
          }}
          style={{ flex: 1 }}
        >
          <Image
            height={Dimensions.get('screen').height * 0.8}
            source={{ uri: editedImageUri }}
            style={{ resizeMode: 'contain' }}
            width={Dimensions.get('screen').width * 0.8}
          />
        </Pressable>
      ) : imageUri ? (
        <View
          style={{
            alignItems: 'center',
            flexDirection: 'column',
            justifyContent: 'space-between',
          }}
        >
          <SorollaView
            backgroundColor="#101010"
            onEditFinish={({ nativeEvent }) => {
              setEditedImageUri(nativeEvent.uri);
            }}
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
