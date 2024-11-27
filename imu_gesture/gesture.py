import gesture_input
import gesture_data
import gesture_model as GM
import numpy as np
import random
from tensorflow.keras.utils import to_categorical

ges_inpt = gesture_input.GestureInput()

label_data, input_data= ges_inpt.getInputData('imu_data.txt', True)
print("len(label_data):  ", len(label_data))
# print(input_data[0, :, :].shape)
stft_data = np.random.rand(len(label_data), 33, 5, 2, 6)
for i in range(len(label_data)):
    for j in range(6):
        f, t_stft, Zxx = ges_inpt.getSTFT(input_data[i, :, j])
        stft_data[i, :, :, 0, j] = ges_inpt.scale_stft_data(np.real(Zxx), len(label_data))
        stft_data[i, :, :, 1, j] = ges_inpt.scale_stft_data(np.imag(Zxx), len(label_data))
print("stft_data.shape:   ", stft_data.shape)

# f, t_stft, Zxx = ges_inpt.getSTFT(input_data[0, 5, :])

object_list = []

# Create 1000 objects and append them to the list
for i in range(len(label_data)):
    array1 = label_data[i]  # Example random array1
    array2 = input_data[i, :, :]  # Example random array2
    array3 = stft_data[i, :, :, :, :]  # Example random array3
    obj = gesture_data.ArrayClass(array1, array2, array3)
    object_list.append(obj)

# Shuffle the list randomly
random.shuffle(object_list)
entire_stack = object_list[:]

split_index = int(0.75 * 1006)
train_stack = object_list[: split_index]
test_stack = object_list[split_index : ]
# Initialize three empty lists to collect the arrays
all_array1 = []
all_array2 = []
all_array3 = []

# Extract arrays from each object and append them to the corresponding list
for obj in entire_stack:
    all_array1.append(obj.label)
    all_array2.append(obj.imu_data)
    all_array3.append(obj.stft_data)

# Convert the lists of arrays to NumPy arrays

entire_label_data = np.array(all_array1)
entire_input_data = np.array(all_array2)
entire_stft_data = np.array(all_array3)

entire_label_data = entire_label_data.astype(int)
entire_label_data = to_categorical(entire_label_data)



# train_label_data = np.array(all_array1)
# train_input_data = np.array(all_array2)
# train_stft_data = np.array(all_array3)

# train_label_data = train_label_data.astype(int)

# print("Array 1 shape:", train_label_data.shape)
# print("Array 2 shape:", train_input_data.shape)
# print("Array 3 shape:", train_stft_data.shape)


# all_array1 = []
# all_array2 = []
# all_array3 = []
# for obj in test_stack:
#     all_array1.append(obj.label)
#     all_array2.append(obj.imu_data)
#     all_array3.append(obj.stft_data)

# # Convert the lists of arrays to NumPy arrays
# test_label_data = np.array(all_array1)
# test_input_data = np.array(all_array2)
# test_stft_data = np.array(all_array3)

# test_label_data = test_label_data.astype(int)

gm_model = GM.Model()
model = gm_model.getModel()
model = gm_model.trainModel(model, entire_input_data, entire_stft_data, entire_label_data)
# gm_model.testModel(model, test_input_data, test_stft_data, test_label_data)
gm_model.saveModel(model)

# Print the shapes to confirm they have been stacked properly
# print("Array 1 shape:", test_label_data.shape)
# print("Array 2 shape:", test_input_data.shape)    
# print("Array 3 shape:", test_input_data.shape)



# print("Array 2 shape:\n", train_label_data)


# print("Array 2 shape:\n", test_label_data)