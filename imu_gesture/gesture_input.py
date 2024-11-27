import numpy as np
import matplotlib.pyplot as plt
from scipy.signal import stft
class GestureInput:
    def getInputData(self, file_name, is_shuffle):
        imu_data = np.genfromtxt(file_name, dtype=float, encoding=None, delimiter=",")
        data_size = imu_data.shape[0]

        # print(imu_data[:,:])
        if(is_shuffle):
            np.random.shuffle(imu_data)

        label_data = imu_data[:, 0]
        input_data = imu_data[:, 1:]
        # Reshape each row to 6x100
        imu_data_reshaped = np.array([row.reshape(100, 6) for row in input_data])
        imu_data_reshaped = self.scale_array(imu_data_reshaped, data_size)
        
        # If you want to maintain a 3D array (rows, 6, 100)
        # print(imu_data_reshaped)
        print(imu_data_reshaped.shape)
        return label_data, imu_data_reshaped
    
    def getSTFT(self, inpt_data):
        # Example signal: a sine wave that changes frequency over time
        fs = 100  # Sampling frequency
        # t = np.arange(0, 1, 1/fs)  # Time array
        # print(t.size)
        x = np.sin(2*np.pi*0.5*inpt_data) + np.sin(2*np.pi*10*inpt_data) + np.sin(2*np.pi*20*inpt_data) + np.sin(2*np.pi*50*inpt_data)  # Signal: 50Hz and 120Hz components

        # Perform STFT
        f, t_stft, Zxx = stft(x, fs, nperseg=64)

        # self.plot_stft(f, t_stft, Zxx)
        # Plot the STFT magnitude
        
        return f, t_stft, Zxx
    def scale_array(self, data, rows):
        for i in range(rows):
            for j in range(6):
                temp_imu_data_reshaped = data[i, :, j]
                min_val = np.min(temp_imu_data_reshaped)
                max_val = np.max(temp_imu_data_reshaped)
                temp_imu_data_reshaped = (temp_imu_data_reshaped - min_val) / (max_val - min_val)
                data[i, :, j] = temp_imu_data_reshaped
        return data
    def plot_stft(self, f, t_stft, Zxx):
        plt.figure(figsize=(100, 60))
        plt.pcolormesh(t_stft, f, np.abs(Zxx), shading='gouraud')
        plt.title('STFT Magnitude')
        plt.ylabel('Frequency [Hz]')
        plt.xlabel('Time [sec]')
        plt.colorbar(label='Magnitude')
        plt.show()
    def scale_stft_data(self, stft_data, rows):
        temp_stft_data_reshaped = stft_data
        min_val = np.min(temp_stft_data_reshaped)
        max_val = np.max(temp_stft_data_reshaped)
        temp_stft_data_reshaped = (temp_stft_data_reshaped - min_val) / (max_val - min_val)
        stft_data = temp_stft_data_reshaped
        return stft_data