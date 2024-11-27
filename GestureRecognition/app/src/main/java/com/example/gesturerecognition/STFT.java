package com.example.gesturerecognition;

import org.apache.commons.math3.complex.Complex;
import org.apache.commons.math3.transform.DftNormalization;
import org.apache.commons.math3.transform.FastFourierTransformer;
import org.apache.commons.math3.transform.TransformType;
import org.jtransforms.fft.DoubleFFT_1D;

import java.util.Arrays;
import java.util.IntSummaryStatistics;

public class STFT {

    private int windowSize;
    private int hopSize;
    private FastFourierTransformer fft;

    public STFT(int windowSize, int hopSize) {
        this.windowSize = windowSize;
        this.hopSize = hopSize;
        this.fft = new FastFourierTransformer(DftNormalization.STANDARD);
    }

    public Complex[][] transform(double[] signal) {
        // Calculate the number of windows
        int numWindows = (signal.length - windowSize) / hopSize + 1;

        // Create an array to store the result
        Complex[][] stftResult = new Complex[numWindows][];

        // Loop over the signal in windowSize steps
        for (int i = 0; i < numWindows; i++) {
            // Get the current window slice
            double[] window = new double[windowSize];
            System.arraycopy(signal, i * hopSize, window, 0, windowSize);

            // Apply the FFT to the current window
            Complex[] fftResult = fft.transform(window, TransformType.FORWARD);

            // Store the result
            stftResult[i] = fftResult;
        }

        return stftResult;
    }

    public static float[][][] prepareData(float[] args, int sampleRate) {
        // Example signal (sine wave)

        double[] signal = new double[sampleRate];
        for(int i=0 ; i<sampleRate; i++)
            signal[i] = 0.0;
        for(int i=0 ; i<sampleRate; i++)
            signal[i] = args[i];

        for (int i = 0; i < sampleRate; i++) {
            signal[i] = Math.sin(2 * Math.PI * 0.5 * signal[i]) + Math.sin(2 * Math.PI * 10 * signal[i]) + Math.sin(2 * Math.PI * 20 * signal[i]) + Math.sin(2 * Math.PI * 50 * signal[i]);
        }

        int windowSize = 64;
        int hopSize = 9;
        int nfft = 64;// Replace with your actual signal

// Compute STFT
        ComplexNumber[][] result = computeSTFT(signal, windowSize, hopSize, nfft);
// Display STFT dimensions
        float[][][] stftResult = new float[result.length][result[0].length][2];

        for (int i = 0; i < result.length; i++)
            for (int j =0; j < result[0].length; j++)
                for(int k=0 ; k< 2; k++)
                    stftResult[i][j][k] = 0.0f;
        // Print the result
        float minReal = (float) result[0][0].real;
        float minImag = (float) result[0][0].imag;
        float maxReal = (float) result[0][0].real;
        float maxImag = (float) result[0][0].imag;
//        System.out.println("result.length " + result.length);
//        System.out.println("result.length0 " + result[0].length);
        for (int i = 0; i < result.length; i++) {
            for (int j =0; j < result[0].length; j++) {
                stftResult[i][j][0] = (float)result[i][j].real;
                stftResult[i][j][1] = (float)result[i][j].imag;
                if(stftResult[i][j][0] < minReal)
                    minReal = stftResult[i][j][0];
                else if(stftResult[i][j][0] > maxReal)
                    maxReal = stftResult[i][j][0];
                if(stftResult[i][j][1] < minImag)
                    minImag = stftResult[i][j][1];
                else if(stftResult[i][j][1] > maxImag)
                    maxImag = stftResult[i][j][1];
            }
        }
        for (int i = 0; i < result.length; i++) {
            for (int j =0; j < result[0].length; j++) {
                stftResult[i][j][0] = (stftResult[i][j][0] - minReal) / (maxReal - minReal);
                stftResult[i][j][1] = (stftResult[i][j][0] - minImag) / (maxImag - minImag);
            }
        }
        return stftResult;




        // Create STFT instance
//        int windowSize = 64;
//        int hopSize = 9;
//        STFT stft = new STFT(windowSize, hopSize);

        // Apply STFT
//        Complex[][]  result = stft.transform(signal);
//        float[][][] stftResult = new float[result.length][result[0].length][2];
//        // Print the result
//        float minReal = (float) result[0][0].getReal();
//        float minImag = (float) result[0][0].getImaginary();
//        float maxReal = (float) result[0][0].getReal();
//        float maxImag = (float) result[0][0].getImaginary();
//        System.out.println("result.length " + result.length);
//        System.out.println("result.length0 " + result[0].length);
//        for (int i = 0; i < result.length; i++) {
//            System.out.println("Window " + i + ": ");
//            for (int j =0; j < result[i].length; j++) {
//                stftResult[i][j][0] = (float)result[i][j].getReal();
//                stftResult[i][j][1] = (float)result[i][j].getImaginary();
//                if(stftResult[i][j][0] < minReal)
//                    minReal = stftResult[i][j][0];
//                else if(stftResult[i][j][0] > maxReal)
//                    maxReal = stftResult[i][j][0];
//                if(stftResult[i][j][1] < minImag)
//                    minImag = stftResult[i][j][1];
//                else if(stftResult[i][j][1] > maxImag)
//                    maxImag = stftResult[i][j][1];
//            }
//        }
//        for (int i = 0; i < result.length; i++) {
//            System.out.println("Window " + i + ": ");
//            for (int j =0; j < result[i].length; j++) {
//                stftResult[i][j][0] = (stftResult[i][j][0] - minReal) / (maxReal - minReal);
//                stftResult[i][j][1] = (stftResult[i][j][0] - minImag) / (maxImag - minImag);
//            }
//        }
//        return stftResult;
    }
    static class ComplexNumber {
        double real;
        double imag;
        ComplexNumber(double real, double imag) {
            this.real = real;
            this.imag = imag;
        }
        @Override
        public String toString() {
            return "(" + real + " + " + imag + "i)";
        }
    }
    // Hann window function
    private static double[] hannWindow(int size) {
        double[] window = new double[size];
        for (int i = 0; i < size; i++) {
            window[i] = 0.5 * (1 - Math.cos(2 * Math.PI * i / (size - 1)));
        }
        return window;
    }
    // STFT computation method
    public static ComplexNumber[][] computeSTFT(double[] signal, int windowSize, int hopSize, int nfft) {
        // Calculate number of frames
        int numFrames = (int) Math.ceil((double) (signal.length - windowSize) / hopSize) + 1;
        // Initialize STFT result matrix
        int freqBins = nfft / 2 + 1;
        ComplexNumber[][] stftMatrix = new ComplexNumber[freqBins][numFrames];
        // Create Hann window
        double[] window = hannWindow(windowSize);
        // Initialize FFT transformer
        DoubleFFT_1D fft = new DoubleFFT_1D(nfft);
        for (int frame = 0; frame < numFrames; frame++) {
            int startIdx = frame * hopSize;
            double[] frameData = new double[nfft * 2]; // Real and Imaginary parts
            // Apply window and zero-padding
            for (int i = 0; i < windowSize; i++) {
                if (startIdx + i < signal.length) {
                    frameData[2 * i] = signal[startIdx + i] * window[i]; // Real part
                    frameData[2 * i + 1] = 0.0; // Imaginary part
                } else {
                    frameData[2 * i] = 0.0;
                    frameData[2 * i + 1] = 0.0;
                }
            }
            // Perform FFT
            fft.complexForward(frameData);
            // Store positive frequency bins
            for (int k = 0; k < freqBins; k++) {
                double real = frameData[2 * k];
                double imag = frameData[2 * k + 1];
                stftMatrix[k][frame] = new ComplexNumber(real, imag);
            }
        }
        return stftMatrix;
    }

}