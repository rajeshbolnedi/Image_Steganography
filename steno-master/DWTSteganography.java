import java.awt.image.BufferedImage;


public class DWTSteganography {
    // Performs the discrete wavelet transform on the given image
    public static BufferedImage performDWT(BufferedImage image) {
        // Convert the image to a 2D array of pixels
        int[][] pixels = new int[image.getWidth()][image.getHeight()];
        for (int i = 0; i < image.getWidth(); i++) {
            for (int j = 0; j < image.getHeight(); j++) {
                pixels[i][j] = image.getRGB(i, j);
            }
        }

        // Perform the DWT on the 2D array of pixels
        int[][] dwtPixels = dwt2D(pixels);

        // Convert the transformed pixels back to an image
        BufferedImage dwtImage = new BufferedImage(image.getWidth(), image.getHeight(), image.getType());
        for (int i = 0; i < image.getWidth(); i++) {
            for (int j = 0; j < image.getHeight(); j++) {
                dwtImage.setRGB(i, j, dwtPixels[i][j]);
            }
        }
        return dwtImage;
    }

    // Embeds the given message in the given image using DWT steganography
    public static BufferedImage embedMessage(BufferedImage image, String message) {
        // Perform DWT on the image
        BufferedImage dwtImage = performDWT(image);

        // Convert the message to a byte array
        byte[] messageBytes = message.getBytes();

        // Create a two-dimensional array to store the message bytes
        // with a size equal to the number of pixels in the image
        byte[][] messageArray = new byte[dwtImage.getWidth()][dwtImage.getHeight()];

        // Copy the message bytes into the two-dimensional array
        // System.out.println(dwtImage.getHeight());
        for (int i = 0; i < dwtImage.getWidth(); i++) {
            for (int j = 0; j < dwtImage.getHeight(); j++) {
                // System.out.println(i * dwtImage.getHeight() + j);
                // System.out.println(messageBytes.length);
                // if (i * dwtImage.getHeight() + j < messageBytes.length) {
                messageArray[i][j] = messageBytes[(i * dwtImage.getHeight() + j)%messageBytes.length];
            }
        }

        // Iterate through the DWT transformed image and embed the message bits
        // in the least significant bits of the pixels
        for (int i = 0; i < dwtImage.getWidth(); i++) {
            for (int j = 0; j < dwtImage.getHeight(); j++) {
                // Get the current pixel value
                int pixel = dwtImage.getRGB(i, j);

                // Extract the least significant bit of the current message byte
                byte messageBit = (byte)(messageArray[i][j] & 1);

                // Set the least significant bit of the pixel to the message bit
                pixel = (pixel & 0xFFFFFFFE) | messageBit;

                // Set the modified pixel value in the image
                dwtImage.setRGB(i, j, pixel);
            }
        }

        return dwtImage;

    }

    // Extracts the message from the given image using DWT steganography
    public static String extractMessage(BufferedImage image) {
        // Perform DWT on the image
        BufferedImage dwtImage = performDWT(image);

        // Create a byte array to store the extracted message
        byte[] messageBytes = new byte[dwtImage.getWidth() * dwtImage.getHeight()];

        // Iterate through the DWT transformed image and extract the message bits
        // from the least significant bits of the pixels
        for (int i = 0; i < dwtImage.getWidth(); i++) {
            for (int j = 0; j < dwtImage.getHeight(); j++) {
                // Get the current pixel value
                int pixel = dwtImage.getRGB(i, j);

                // Extract the least significant bit of the pixel
                byte messageBit = (byte)(pixel & 1);

                // Set the extracted bit in the message byte array
                messageBytes[i * dwtImage.getHeight() + j] = (byte)((messageBytes[i * dwtImage.getHeight() + j] << 1) | messageBit);
            }
        }

        // Convert the byte array to a string and return it
        return new String(messageBytes);

    }
    public static int[][] dwt2D(int[][] matrix) {
        // Check if the matrix is empty
        if (matrix == null || matrix.length == 0) {
            return null;
        }
        // Perform the 1D DWT on each row of the matrix
        int[][] dwtMatrix = new int[matrix.length][matrix[0].length];
        for (int i = 0; i < matrix.length; i++) {
            dwtMatrix[i] = dwt1D(matrix[i]);
        }

        // Perform the 1D DWT on each column of the matrix
        for (int j = 0; j < matrix[0].length; j++) {
            // Extract the j-th column from the matrix
            int[] column = new int[matrix.length];
            for (int i = 0; i < matrix.length; i++) {
                column[i] = dwtMatrix[i][j];
            }

            // Perform the 1D DWT on the column and store the result back in the matrix
            column = dwt1D(column);
            for (int i = 0; i < matrix.length; i++) {
                dwtMatrix[i][j] = column[i];
            }
        }

        return dwtMatrix;

    }
    public static int[] dwt1D(int[] array) {
        // Check if the array is empty
        if (array == null || array.length == 0) {
            return null;
        }
        // Calculate the length of the DWT transformed array
        int dwtLength = (int)Math.ceil(Math.log(array.length) / Math.log(2));

        // Pad the array with zeros if its length is not a power of 2
        int[] paddedArray = new int[1 << dwtLength];
        System.arraycopy(array, 0, paddedArray, 0, array.length);

        // Perform the DWT on the padded array
        for (int i = 0; i < dwtLength; i++) {
            int[] dwtArray = new int[paddedArray.length];
            for (int j = 0; j < paddedArray.length; j += 2) {
                dwtArray[j] = (paddedArray[j] + paddedArray[j + 1]) / 2;
                dwtArray[j + 1] = (paddedArray[j] - paddedArray[j + 1]) / 2;
            }
            paddedArray = dwtArray;
        }

        // Return the DWT transformed array
        return paddedArray;
    }

}
