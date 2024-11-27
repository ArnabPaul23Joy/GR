import numpy as np
import tensorflow as tf
from tensorflow.keras import layers, models
class Model:
    def getModel(self):
        # Time-series Head
        input_ts = tf.keras.Input(shape=(100, 6))
        input_ts = tf.reshape(input_ts, (1, 100, 6))
        x_ts = layers.Conv1D(64, 3, padding='same', activation='relu')(input_ts)
        x_ts = layers.BatchNormalization()(x_ts)
        print("x_ts.shape):   ", x_ts.shape)

        x_ts = layers.Conv1D(64, 5, padding='same', activation='relu')(x_ts)
        print("x_ts.shape):   ", x_ts.shape)
        x_ts = layers.BatchNormalization()(x_ts)

        x_ts = layers.Conv1D(64, 7, padding='same', activation='relu')(x_ts)
        x_ts = layers.BatchNormalization()(x_ts)
        print("x_ts.shape):   ", x_ts.shape)

        x_ts = layers.GlobalAveragePooling1D()(x_ts)
        print("x_ts.shape):   ", x_ts.shape)

        # Spectrogram Head
        input_spec = tf.keras.Input(shape=(33, 5, 2, 6))
        input_spec = tf.reshape(input_spec, (1, 33, 5, 2, 6))
        x_spec = layers.Conv2D(64, (3, 3), padding='same', activation='relu')(input_spec)
        
        x_spec = layers.BatchNormalization()(x_spec)
        x_spec = layers.MaxPooling3D(pool_size=(2, 2, 2), strides = (2, 2, 2))(x_spec)
        print("x_spec.shape):   ", x_spec.shape)

        x_spec = layers.Conv2D(64, (5, 5), padding='same', activation='relu')(x_spec)
        x_spec = layers.BatchNormalization()(x_spec)
        print("x_spec.shape):   ", x_spec.shape)
        
        x_spec = layers.MaxPooling3D(pool_size=(2, 2, 1), strides = (2, 2, 1))(x_spec)
        x_spec = layers.Conv2D(64, (7, 7), padding='same', activation='relu')(x_spec)
        x_spec = layers.BatchNormalization()(x_spec)
        print("x_spec.shape):   ", x_spec.shape)

        x_spec = layers.MaxPooling3D(pool_size=(2, 1, 1), strides = (2, 1, 1))(x_spec)
        x_spec = layers.Conv2D(64, (7, 7), padding='same', activation='relu')(x_spec)
        x_spec = layers.BatchNormalization()(x_spec)
        print("x_spec.shape):   ", x_spec.shape)

        x_spec = layers.MaxPooling3D(pool_size=(2, 1, 1), strides = (2, 1, 1))(x_spec)
        print("x_spec.shape):   ", x_spec.shape)

        x_spec = layers.GlobalAveragePooling3D()(x_spec)
        print("x_spec.shape):   ", x_spec.shape)

        # Concatenate and Final Output
        print("x_ts.shape):   ", x_ts.shape)
        print("x_spec.shape):   ", x_spec.shape)
        concatenated = tf.concat([x_ts, x_spec], axis = 1)
        print("concatenated:   ", concatenated.shape)
        x = layers.ReLU()(concatenated)
        print("x:   ", x.shape)
        output = layers.Dense(8, activation='softmax')(x)

        # Model
        model = models.Model(inputs=[input_ts, input_spec], outputs=output)

        # Compile the model
        model.compile(optimizer='adam', loss='categorical_crossentropy', metrics=['accuracy'])

        model.summary()
        return model

    def trainModel(self, model, X1, X2, y):
        model.fit([X1, X2], y, epochs = 30, batch_size = 15, validation_split = 0.2)
        return model

    def testModel(self, model, X1, X2, y):
        val_loss, val_accuracy = model.evaluate([X1, X2], y)
        print("val_loss, val_accuracy:   ", val_loss, "   ", val_accuracy)
    
    def saveModel(self, model):
        model.save('imu_gesture_model_2.0.keras')

        converter = tf.lite.TFLiteConverter.from_keras_model(model)
        converter.target_spec.supported_ops = [
            tf.lite.OpsSet.TFLITE_BUILTINS, # enable LiteRT ops.
            tf.lite.OpsSet.SELECT_TF_OPS # enable TensorFlow ops.
        ]
        tflite_model = converter.convert()

        # Step 3: Save the TFLite model to a file
        with open('imu_gesture_lite_model_2.0.tflite', 'wb') as f:
            f.write(tflite_model)
        # f.close()
        print("Model successfully converted to TFLite and saved as 'model.tflite'")
    
    def reshape_tensor(self, x_tensor, new_shape):
        d = layers.Lambda(lambda arg: tf.keras.backend.mean(arg, axis=0), output_shape = x_tensor.shape)
        e = d(x_tensor)
        x_tensor = layers.Reshape(new_shape)(e)
        print(x_tensor.shape)
        return x_tensor