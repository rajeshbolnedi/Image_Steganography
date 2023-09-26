import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

public class DWTSteganography2 {

    // Perform the DWT transformation on the given image block
    public static double[][] dwtTransform(double[][] block) {
        int N = block.length;
        int M = block[0].length;

        // Perform the 1D DWT on each row
        for (int i = 0; i < N; i++) {
            block[i] = dwt1D(block[i]);
        }

        // Perform the 1D DWT on each column
        for (int j = 0; j < M; j++) {
            double[] col = new double[N];
            for (int i = 0; i < N; i++) {
                col[i] = block[i][j];
            }
            col = dwt1D(col);
            for (int i = 0; i < N; i++) {
                block[i][j] = col[i];
            }
        }

        return block;
    }

    // Perform the inverse DWT transformation on the given image block
    public static double[][] inverseDwtTransform(double[][] block) {
        int N = block.length;
        int M = block[0].length;

        // Perform the inverse 1D DWT on each column
        for (int j = 0; j < M; j++) {
            double[] col = new double[N];
            for (int i = 0; i < N; i++) {
                col[i] = block[i][j];
            }
            col = inverseDwt1D(col);
            for (int i = 0; i < N; i++) {
                block[i][j] = col[i];
            }
        }

        // Perform the inverse 1D DWT on each row
        for (int i = 0; i < N; i++) {
            block[i] = inverseDwt1D(block[i]);
        }

        return block;
    }

    // Perform the 1D DWT on the given signal
    private static double[] dwt1D(double[] signal) {
        int N = signal.length;
        double[] result = new double[N];

        // Calculate the approximation and detail coefficients
        for (int i = 0; i < N / 2; i++) {
            double cA = (signal[2 * i] + signal[2 * i + 1]) / 2.0;
            double cD = (signal[2 * i] - signal[2 * i + 1]) / 2.0;
            result[i] = cA;
            result[i + N / 2] = cD;
        }

        return result;
    }

    // Perform the inverse 1D DWT on the given signal
    private static double[] inverseDwt1D(double[] signal) {
        int N = signal.length;
        double[] result = new double[N];

        // Calculate the original signal from the approximation and detail coefficients
        for (int i = 0; i < N / 2; i++) {
            double s1 = signal[i];
            double s2 = signal[i + N / 2];
            result[2 * i] = s1 + s2;
            result[2 * i + 1] = s1 - s2;
        }

        return result;
    }

    // Embed the given text in the given cover image
    public static double[][] embedText(double[][] cover, String text) {
        // Perform the DWT on the cover image
        cover = dwtTransform(cover);

        // Convert the text to an array of doubles
        double[] signal = textToArray(text);

        // Embed the text in the cover image by modifying the approximation coefficients
        int N = cover.length;
        for (int i = 0; i < signal.length; i++) {
            cover[i][0] = signal[i];
        }

        return cover;
    }

       

    // Extract the text from the given stego-image
    public static String extractText(double[][] stego) {
        // Perform the inverse DWT on the stego-image
        stego = inverseDwtTransform(stego);

        // Extract the text from the approximation coefficients of the stego-image
        int N = stego.length;
        double[] signal = new double[N];
        for (int i = 0; i < N; i++) {
            signal[i] = stego[i][0];
        }

        // Convert the extracted signal back to text
        return arrayToText(signal);
    }

    // Convert the given image block to a 2D array of doubles
    public static double[][] imageToBlock(BufferedImage image) {
        int N = image.getWidth();
        int M = image.getHeight();
        double[][] block = new double[N][M];

        for (int i = 0; i < N; i++) {
            for (int j = 0; j < M; j++) {
                block[i][j] = image.getRGB(i, j);
            }
        }

        return block;
    }


    // Convert the given 2D array of doubles to an image
    public static BufferedImage blockToImage(double[][] block) {
        int N = block.length;
        int M = block[0].length;
        BufferedImage image = new BufferedImage(N, N, BufferedImage.TYPE_INT_RGB);

        for (int i = 0; i < N; i++) {
            for (int j = 0; j < M; j++) {
                image.setRGB(i, j, (int)block[i][j]);
            }
        }

        return image;
    }

    // Save the given image to the specified file
    public static void saveImage(BufferedImage image, String fileName) throws IOException {
        File outputFile = new File(fileName);
        ImageIO.write(image, "png", outputFile);
    }

    // Load the image from the specified file
    public static BufferedImage loadImage(String fileName) throws IOException {
        File inputFile = new File(fileName);
        return ImageIO.read(inputFile);
    }

    public static String arrayToText(double[] signal) {
        StringBuilder text = new StringBuilder();
        for (double value : signal) {
          // Convert the value to a character by scaling it to the range 0-255
          // and then casting it to a byte (which has the same range)
          char c = (char)(value * 255);
          text.append(c);
        }
        return text.toString();
    }

    public static double[] textToArray(String text) {
        double[] signal = new double[text.length()];
        for (int i = 0; i < text.length(); i++) {
          // Convert the character to a value by scaling it to the range 0.0-1.0
          // (which is the same range as a double precision floating point value)
          char c = text.charAt(i);
          signal[i] = (double)c / 255;
        }
        return signal;
    }
      
      

    // public static void main(String[] args) throws IOException {
    //     // Load the cover image and the secret message
    //     BufferedImage coverImage = loadImage("cover.png");

    //     // Convert the images to blocks of pixels
    //     double[][] cover = imageToBlock(coverImage);
    //     String secret = "test";

    //     // Embed the secret message in the cover image
    //     double[][] stego = embedText(cover, secret);

    //     // Save the stego-image to a file
    //     BufferedImage stegoImage = blockToImage(stego);
    //     saveImage(stegoImage, "stego.png");

    //     // Extract the secret message from the stego-image
    //     String extracted = extractText(stego);
    //     System.out.println(extracted);

    // }
}

