import gesture_input
import gesture_data
import gesture_model as GM
import numpy as np
import random
import tensorflow as tf
from tensorflow.keras import layers, models

ges_inpt = gesture_input.GestureInput()

label_data, input_data= ges_inpt.getInputData('prediction_input.txt', False)
stft_data = np.random.rand(len(label_data), 33, 5, 2, 6)
for i in range(len(label_data)):
    for j in range(6):
        f, t_stft, Zxx = ges_inpt.getSTFT(input_data[i, :, j])
        print("np.real(Zxx).shape:  ", np.real(Zxx).shape)
        stft_data[i, :, :, 0, j] = ges_inpt.scale_stft_data(np.real(Zxx), len(label_data))
        stft_data[i, :, :, 1, j] = ges_inpt.scale_stft_data(np.imag(Zxx), len(label_data))
print("stft_data.shape:   ", stft_data.shape)
print("input_data.shape:   ", input_data.shape)
model = tf.keras.models.load_model('imu_gesture_model.keras')
# model.summary()
predictions = model.predict([input_data, stft_data])
np.set_printoptions(suppress = True)
predictions = np.argmax(predictions, axis=1)
label_data = label_data.astype(int)
print("predictions:", predictions)
print("label_data: ", label_data)
count = 0
for i in range(len(label_data)):
    if(predictions[i] != label_data[i]):
        count+=1
print("number of inaccuracies: ", count)
count = ((len(label_data)-count)/(len(label_data) * 1.0)) * 100
print("accuracy: % ", count)


# try the tflite model

interpreter = tf.lite.Interpreter(model_path="imu_gesture_lite_model.tflite")
interpreter.allocate_tensors()

# Get input and output tensors.
input_details = interpreter.get_input_details()
output_details = interpreter.get_output_details()

# Test model on some input data.
input_shape = input_details[0]['shape']
input_shape1 = input_details[1]['shape']

acc=0
for i in range(len(label_data)):
    input_text = np.array(input_data[i].reshape(input_shape), dtype=np.float32)
    interpreter.set_tensor(input_details[0]['index'], input_text)

    input_intent= np.array(stft_data[i].reshape(input_shape1), dtype=np.float32)
    interpreter.set_tensor(input_details[1]['index'], input_intent)
  
    interpreter.invoke()
    output_data = interpreter.get_tensor(output_details[0]['index'])
    output_data = np.argmax(output_data)
    print("output_data:  ", output_data)
    if(output_data == label_data[i]):
        acc+=1
acc = acc/(len(label_data) * 1.0)
print("tflite acc:  ", acc*100)