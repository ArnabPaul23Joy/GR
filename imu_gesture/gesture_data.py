import numpy as np

# Define the class with three NumPy arrays
class ArrayClass:
    def __init__(self, label, imu_data, stft_data):
        self.label = label
        self.imu_data = imu_data
        self.stft_data = stft_data

# Create a list to hold the objects