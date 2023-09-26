import java.awt.image.BufferedImage;
// import java.io.File;
// import java.io.IOException;

// import javax.imageio.ImageIO;
// import java.util.Arrays;

public class DWT {
    // Apply the DWT to the input image and return the wavelet coefficients
    public static double[][] dwt(BufferedImage image) {
        // Create a 2D array to hold the wavelet coefficients
        int width = image.getWidth();
        int height = image.getHeight();
        double[][] waveletCoefficients = new double[width][height];

        // Apply the DWT to the input image to obtain the wavelet coefficients
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                // Get the pixel at (i, j)
                int pixel = image.getRGB(i, j);

                // Extract the red, green, and blue components of the pixel
                int r = (pixel >> 16) & 0xff;
                int g = (pixel >> 8) & 0xff;
                int b = pixel & 0xff;

                // Compute the average and difference values for the red, green, and blue components
                int ra = (r + g + b) / 3;
                int rd = (r - g) / 2;
                int ga = (r + g - b) / 3;
                int gd = (g - b) / 2;
                int ba = (r - g + b) / 3;
                int bd = (r + g + 2 * b) / 4;

                // Store the average and difference values in the wavelet coefficients array
                waveletCoefficients[i][j] = ra;
                if(i+width/2 < width) {
                    waveletCoefficients[i + width / 2][j] = rd;
                }
                if(j+height/2 < height) {
                    waveletCoefficients[i][j + height / 2] = ga;
                }
                if (i+width/2 < width && j+height/2 < height) {
                    waveletCoefficients[i + width / 2][j + height / 2] = gd;
                }
                if (j+height < height) {
                    waveletCoefficients[i][j + height] = ba;
                }
                if (i+width/2 < width && j+height < height) {
                    waveletCoefficients[i + width / 2][j + height] = bd;
                }
            }
        }

        return waveletCoefficients;
    }

    // Apply the IDWT to the wavelet coefficients and return the output image
    public static BufferedImage idwt(double[][] waveletCoefficients) {
        // Create a new BufferedImage to hold the output image
        int width = waveletCoefficients.length;
        int height = waveletCoefficients[0].length;
        BufferedImage outputImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

        // Apply the IDWT to the wavelet coefficients to generate the output image
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                // Get the wavelet coefficients at the current position and its neighbors
                double c00 = waveletCoefficients[(i + width - 1) % width][(j + height - 1) % height];
                double c01 = waveletCoefficients[(i + width - 1) % width][j];
                double c02 = waveletCoefficients[(i + width - 1) % width][(j + 1) % height];
                double c10 = waveletCoefficients[i][(j + height - 1) % height];
                double c11 = waveletCoefficients[i][j];
                double c12 = waveletCoefficients[i][(j + 1) % height];
                double c20 = waveletCoefficients[(i + 1) % width][(j + height - 1) % height];
                double c21 = waveletCoefficients[(i + 1) % width][j];
                double c22 = waveletCoefficients[(i + 1) % width][(j + 1) % height];

                // Apply the IDWT using the scaling and wavelet functions of the Haar wavelet transform
                int r = (int) (c11 + (c00 + c22) / 2 + (c10 + c21) / 2);
                int g = (int) (c11 + (c02 + c20) / 2 + (c01 + c12) / 2);
                int b = (int) (c11 + (c00 + c22) / 2 - (c10 + c21) / 2);

                // Clamp the red, green, and blue values to the valid range [0, 255]
                r = Math.max(0, Math.min(255, r));
                g = Math.max(0, Math.min(255, g));
                b = Math.max(0, Math.min(255, b));

                // Create a new pixel from the red, green, and blue values
                int pixel = (r << 16) | (g << 8) | b;

                // Set the pixel in the output image
                outputImage.setRGB(i, j, pixel);
            }
        }

        return outputImage;
    }

    // Embed the message in the input image and return the output image
    public static BufferedImage embed(BufferedImage image, String message) {
        // Apply the DWT to the input image to obtain the wavelet coefficients
        double[][] waveletCoefficients = dwt(image);

        // Convert the message string to a byte array
        byte[] messageBytes = message.getBytes();

        // Modify the wavelet coefficients by replacing some of the least significant bits with the bits of the message
        for (int i = 0; i < waveletCoefficients.length; i++) {
            for (int j = 0; j < waveletCoefficients[0].length; j++) {
                // Get the wavelet coefficient at (i, j)
                double waveletCoefficient = waveletCoefficients[i][j];

                // Convert the wavelet coefficient to an integer
                int waveletCoefficientInt = (int) waveletCoefficient;

                // Replace the least significant bit of the wavelet coefficient with the next bit of the message
                waveletCoefficientInt = waveletCoefficientInt & ~1 | messageBytes[i * waveletCoefficients[0].length + j] & 1;

                // Convert the modified wavelet coefficient back to a double and store it in the wavelet coefficients array
                waveletCoefficients[i][j] = (double) waveletCoefficientInt;
            }
        }

        // Apply the IDWT to the modified wavelet coefficients to generate the output image
        BufferedImage outputImage = idwt(waveletCoefficients);

        return outputImage;
    }
    
    // Extract the message from the output image
    public static String extract(BufferedImage image) {
        // Apply the DWT to the output image to obtain the wavelet coefficients
        double[][] waveletCoefficients = dwt(image);

        // Retrieve the hidden message by extracting the bits that were replaced in the wavelet coefficients during the embed process
        byte[] messageBytes = new byte[waveletCoefficients.length * waveletCoefficients[0].length];
        for (int i = 0; i < waveletCoefficients.length; i++) {
            for (int j = 0; j < waveletCoefficients[0].length; j++) {
                // Get the wavelet coefficient at (i, j)
                double waveletCoefficient = waveletCoefficients[i][j];

                // Extract the least significant bit of the wavelet coefficient and store it in the message byte array
                messageBytes[i * waveletCoefficients[0].length + j] = (byte) ((int)waveletCoefficient & 1);
            }
        }

        // Convert the message byte array to a string
        String message = new String(messageBytes);

        return message;
    } 
}



// public class DWT2 {
//     // Embed the given text in the given image
//     public BufferedImage embedText(BufferedImage image, String text) {
//         // Perform the DWT on the image to generate the DWT coefficients
//         int[][] dwt = performDWT(image);

//         // Embed the text in the DWT coefficients
//         dwt = embedTextInCoefficients(dwt, text);

//         // Perform the iDWT on the modified DWT coefficients to generate the stego-image
//         // with the hidden text
//         BufferedImage stegoImage = inverseDWT(dwt);

//         return stegoImage;
//     }

//     // Extract the hidden text from the given stego-image
//     public String extractText(BufferedImage stegoImage) {
//         // Perform the DWT on the stego-image to generate the DWT coefficients
//         int[][] dwt = performDWT(stegoImage);

//         // Extract the text from the DWT coefficients
//         String extractedText = extractTextFromCoefficients(dwt);

//         return extractedText;
//     }

//     // Perform the discrete wavelet transform (DWT) on the given image
//     public int[][] performDWT(BufferedImage image) {
//         // Get the dimensions of the image
//         int width = image.getWidth();
//         int height = image.getHeight();

//         // Initialize the DWT array with the same dimensions as the image
//         int[][] dwt = new int[height][width];

//         // Loop over the image and perform the DWT on each pixel
//         for (int i = 0; i < height - 1; i++) {
//             for (int j = 0; j < width - 1; j++) {
//                 // Get the 4 neighboring pixels of the current pixel
//                 int p1 = image.getRGB(j, i);
//                 int p2 = image.getRGB(j + 1, i);
//                 int p3 = image.getRGB(j, i + 1);
//                 int p4 = image.getRGB(j + 1, i + 1);

//                 // Calculate the average of the 4 neighboring pixels
//                 int avg = (p1 + p2 + p3 + p4) / 4;

//                 // Calculate the difference between each pixel and the average
//                 int d1 = p1 - avg;
//                 int d2 = p2 - avg;
//                 int d3 = p3 - avg;
//                 int d4 = p4 - avg;

//                 // Store the calculated values in the DWT array
//                 dwt[i][j] = avg;
//                 dwt[i][j + 1] = d1;
//                 dwt[i + 1][j] = d2;
//                 dwt[i + 1][j + 1] = d3;
//             }
//         }

//         return dwt;
//     }

//     // Perform the inverse discrete wavelet transform (IDWT) on the given DWT array
//     public BufferedImage inverseDWT(int[][] dwt) {
//         // Get the dimensions of the DWT array
//         int height = dwt.length;
//         int width = dwt[0].length;

//         // Initialize the output image with the same dimensions as the DWT array
//         BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

//         // Loop over the DWT array and perform the IDWT on each pixel
//         for (int i = 0; i < height - 1; i++) {
//             for (int j = 0; j < width - 1; j++) {
//                 // Get the 4 neighboring pixels of the current pixel
//                 int avg = dwt[i][j];
//                 int d1 = dwt[i][j + 1];
//                 int d2 = dwt[i + 1][j];
//                 int d3 = dwt[i + 1][j + 1];

//                 // Calculate the original pixel values by adding the average and the differences
//                 int p1 = avg + d1;
//                 int p2 = avg + d2;
//                 int p3 = avg + d3;
//                 int p4 = avg + d1 + d2 + d3;

//                 // Store the calculated pixel values in the output image
//                 image.setRGB(j, i, p1);
//                 image.setRGB(j + 1, i, p2);
//                 image.setRGB(j, i + 1, p3);
//                 image.setRGB(j + 1, i + 1, p4);
//             }
//         }

//         return image;
//     }

//     // Embed the given text in the given DWT array
//     public int[][] embedTextInCoefficients(int[][] dwt, String text) {
//         // Get the dimensions of the DWT array
//         int height = dwt.length;
//         int width = dwt[0].length;

//         // Initialize the index variables
//         int i = 0;
//         int j = 0;

//         // Loop over the characters of the text to be embedded
//         for (int k = 0; k < text.length(); k++) {
//             // Get the current character
//             char c = text.charAt(k);

//             // Loop over the bits of the current character
//             for (int bit = 0; bit < 8; bit++) {
//                 // Get the 4 neighboring pixels of the current pixel
//                 int avg = dwt[i][j];
//                 int d1 = dwt[i][j + 1];
//                 int d2 = dwt[i + 1][j];
//                 int d3 = dwt[i + 1][j + 1];

//                 // Calculate the average value of the 4 neighboring pixels
//                 int avg2 = (d1 + d2 + d3 + avg) / 4;

//                 // Check if the current bit is 1 or 0
//                 if (((c >> bit) & 1) == 1) {
//                     // If the current bit is 1, calculate the new pixel values by adding the average
//                     // value
//                     int newAvg = avg + avg2;
//                     int newD1 = d1 + avg2;
//                     int newD2 = d2 + avg2;
//                     int newD3 = d3 + avg2;

//                     // Store the new pixel values in the DWT array
//                     dwt[i][j] = newAvg;
//                     dwt[i][j + 1] = newD1;
//                     dwt[i + 1][j] = newD2;
//                     dwt[i + 1][j + 1] = newD3;
//                 } else {
//                     // If the current bit is 0, calculate the new pixel values by subtracting the
//                     // average value
//                     int newAvg = avg - avg2;
//                     int newD1 = d1 - avg2;
//                     int newD2 = d2 - avg2;
//                     int newD3 = d3 - avg2;

//                     // Store the new pixel values in the DWT array
//                     dwt[i][j] = newAvg;
//                     dwt[i][j + 1] = newD1;
//                     dwt[i + 1][j] = newD2;
//                     dwt[i + 1][j + 1] = newD3;
//                 }

//                 // Increment the index variables to move to the next pixel
//                 i++;
//                 if (i >= height - 1) {
//                     i = 0;
//                     j += 2;
//                 }
//             }
//         }
//         return dwt;
//     }

//     // Extract the embedded text from the given DWT array
//     public String extractTextFromCoefficients(int[][] dwt) {
//         // Get the dimensions of the DWT array
//         int height = dwt.length;
//         int width = dwt[0].length;

//         // Initialize the output string
//         String text = "";

//         // Initialize the index variables
//         int i = 0;
//         int j = 0;

//         // Loop until the end of the DWT array is reached
//         while (i < height - 1 && j < width - 1) {
//             // Initialize the current character
//             char c = 0;

//             // Loop over the bits of the current character
//             for (int bit = 0; bit < 8; bit++) {
//                 // Get the 4 neighboring pixels of the current pixel
//                 int avg = dwt[i][j];
//                 int d1 = dwt[i][j + 1];
//                 int d2 = dwt[i + 1][j];
//                 int d3 = dwt[i + 1][j + 1];

//                 // Calculate the average value of the 4 neighboring pixels
//                 int avg2 = (d1 + d2 + d3 + avg) / 4;

//                 // Calculate the bit value by checking if the average is greater than or equal
//                 // to the original average value
//                 int b = (avg2 >= avg) ? 1 : 0;

//                 // Set the current bit position of the current character to the calculated bit
//                 // value
//                 c |= b << bit;

//                 // Increment the index variables to move to the next pixel
//                 i++;
//                 if (i >= height - 1) {
//                     i = 0;
//                     j += 2;
//                 }
//             }

//             // Add the current character to the output string
//             text += c;
//         }

//         return text;
//     }

// }


// import java.io.InputStream;

// public class DWT2 {

//   // Embeds a message into an image using the DWT
//   public BufferedImage embedText(BufferedImage image, String message){
//     // Perform the DWT on the image
//     BufferedImage dwtImage = dwt(image);

//     // Hide the message in the DWT coefficients of the image
//     hideMessage(dwtImage, message);
//     return dwtImage;
//   }

//   // Extracts a message from an image using the DWT
//   public String extractText(BufferedImage image){
//     // Perform the DWT on the image
//     BufferedImage dwtImage = dwt(image);

//     // Retrieve the hidden message from the DWT coefficients of the image
//     return retrieveMessage(dwtImage);
//   }

//   // Performs the DWT on an image
// private BufferedImage dwt(BufferedImage image) {
//     // Get the width and height of the image
//     int width = image.getWidth();
//     int height = image.getHeight();
  
//     // Create a new image for the DWT coefficients
//     BufferedImage dwtImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
  
//     // Perform the DWT on the image using a Haar wavelet
//     for (int i = 0; i < width; i++) {
//       for (int j = 0; j < height; j++) {
//         // Get the pixel value at (i, j)
//         int pixel = image.getRGB(i, j);
  
//         // Separate the pixel into its RGB color channels
//         int r = (pixel >> 16) & 0xff;
//         int g = (pixel >> 8) & 0xff;
//         int b = pixel & 0xff;
  
//         // Perform the DWT on each color channel
//         int[] dwtR = dwt1D(r);
//         int[] dwtG = dwt1D(g);
//         int[] dwtB = dwt1D(b);
  
//         // Combine the DWT coefficients of the three color channels into a single pixel
//         int dwtPixel = (dwtR[0] << 16) | (dwtG[0] << 8) | dwtB[0];
  
//         // Set the DWT coefficient at (i, j) in the new image
//         dwtImage.setRGB(i, j, dwtPixel);
//       }
//     }
  
//     return dwtImage;
//   }
  
//   // Performs the 1D DWT on a 1D array of values using a Haar wavelet
//   private int[] dwt1D(int[] values) {
//     int n = values.length;
//     int[] dwt = new int[n];
  
//     for (int i = 0; i < n; i++) {
//       int average = (values[i] + values[i + 1]) / 2;
//       int detail = values[i] - average;
//       dwt[i] = average;
//       dwt[i + 1] = detail;
//     }
  
//     return dwt;
//   }
  

//   // Hides a message in the DWT coefficients of an image
//   private void hideMessage(BufferedImage image, String message) {
//     // TODO: Implement the message hiding here
//   }

//   // Retrieves a hidden message from the DWT coefficients of an image
//   private String retrieveMessage(BufferedImage image) {
//     // TODO: Implement the message retrieval here
//   }
// }
