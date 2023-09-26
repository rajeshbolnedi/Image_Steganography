import java.awt.image.BufferedImage;

public class Steganography {
    // Embeds the text in the DWT coefficients of the input image and returns the
    // stego image
    // Embeds the given text in the given image and returns the modified image
    public BufferedImage embedText(BufferedImage image, String text) {
        // Perform the DWT on the input image
        int[][] dwt = performDWT(image);
    
        // Embed the text in the d1, d2, d3, and d4 coefficients
        int index = 0;
        for (int i = 0; i < image.getHeight()-1; i += 2) {
            for (int j = 0; j < image.getWidth()-1; j += 2) {
                // Extract the text from the d1, d2, d3, and d4 coefficients
                if (index >= text.length()) {
                    break;
                }
                String Subtext = Integer.toBinaryString(text.charAt(index++));
                while (Subtext.length() < 8) {
                    // pad with 0s
                    Subtext = "0" + Subtext;
                }
                System.out.println(Subtext);
                System.out.println("d1: " + dwt[j][i] + " d2: " + dwt[j+1][i] + " d3: " + dwt[j][i+1] + " d4: " + dwt[j+1][i+1]);

                int[] coefficients = embedText(dwt[j][i], dwt[j + 1][i], dwt[j][i + 1], dwt[j + 1][i + 1], Subtext);
                // Store the modified d1, d2, d3, and d4 coefficients in the DWT array
                dwt[j][i] = coefficients[0];
                dwt[j+1][i] = coefficients[1];
                dwt[j][i+1] = coefficients[2];
                dwt[j+1][i+1] = coefficients[3];
                System.out.println("d1: " + dwt[j][i] + " d2: " + dwt[j+1][i] + " d3: " + dwt[j][i+1] + " d4: " + dwt[j+1][i+1]);
            }
        }
    
        // Initialize the output image
        BufferedImage outputImage = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_RGB);
        int c=0;
    
        // Perform the inverse DWT on the modified DWT coefficients
        for (int i = 0; i < image.getHeight()-1; i += 2) {
            for (int j = 0; j < image.getWidth()-1; j += 2) {
                // Extract the modified d1, d2, d3, and d4 coefficients
                int d1 = dwt[j][i];
                int d2 = dwt[j + 1][i];
                int d3 = dwt[j][i + 1];
                int d4 = dwt[j + 1][i + 1];
        
                // Calculate the modified pixels
                // int p1 = (d1 + d2 + d3 + d4)/4;
                // int p2 = (d1 - d2 + d3 - d4)/4;
                // int p3 = (d1 + d2 - d3 - d4)/4;
                // int p4 = (d1 - d2 - d3 + d4)/4;

                int p1 = d1 + d3 + d4 - 2*d2;
                int p2 = d2 + d4 + d1 - 2*d3;
                int p3 = d3 + d1 + d2 - 2*d4;
                int p4 = d4 + d2 + d3 - 2*d1;
                
                if(c<4){
                    System.out.println("p1: " + p1 + " p2: " + p2 + " p3: " + p3 + " p4: " + p4);
                    c++;
                }
        
                // Set the modified pixels in the output image
                outputImage.setRGB(j, i, p1);
                outputImage.setRGB(j + 1, i, p2);
                outputImage.setRGB(j, i + 1, p3);
                outputImage.setRGB(j + 1, i + 1, p4);
            }
        }
        System.out.println(outputImage.getRGB(1, 0));
    
        // Return the modified image
        return outputImage;
    }
  

    // Extracts the text from the DWT coefficients of the input image and returns
    // the extracted text
    public String extractText(BufferedImage image) {
        // Perform the DWT on the input image
        int[][] dwt = performDWT(image);

        String extractedText = "";
        int c=0;

        // Loop over the image and extract the text one character at a time
        for (int i = 0; i < image.getHeight()-1; i += 2) {
            for (int j = 0; j < image.getWidth()-1; j += 2) {
                // Extract the text from the d1, d2, d3, and d4 coefficients
                if(c==6){
                    return extractedText;
                }
                String text = extractText(dwt[j][i], dwt[j + 1][i], dwt[j][i + 1], dwt[j + 1][i + 1]);
                System.out.println("d1: " + dwt[j][i] + " d2: " + dwt[j+1][i] + " d3: " + dwt[j][i+1] + " d4: " + dwt[j+1][i+1]);
                if (text.equals("00000000")){
                    System.out.println("text: " + text);
                    return extractedText;
                }

                // Convert the binary text to an integer
                int value = Integer.parseInt(text, 2);

                // Convert the integer to a string character
                char ch = (char) value;

                // Append the character to the extracted text
                extractedText += ch;
                c++;
            }
        }

        // Return the extracted text
        return extractedText;

    }

    // Extracts the text from the given d1, d2, d3, and d4 coefficients and returns
    // the extracted text
    public String extractText(int d1, int d2, int d3, int d4) {
        // Calculate the average value of the d1, d2, d3, and d4 coefficients
        // int avg = (d1 + d2 + d3 + d4) / 4;

        // Initialize the extracted text string
        String extractedText = "";

        // Extract the text one character at a time
        for (int i = 0; i < 8; i++) {
            // Extract the i-th bit of the d1, d2, d3, and d4 coefficients
            int b1 = (d1 >> i) & 1;
            int b2 = (d2 >> i) & 1;
            int b3 = (d3 >> i) & 1;
            int b4 = (d4 >> i) & 1;

            // Calculate the value of the i-th bit of the extracted text
            int bit = (b1 + b2 + b3 + b4) % 2;

            // Append the extracted bit to the extracted text string
            extractedText += bit;
        }

        // Return the extracted text
        return extractedText;
    }

    // Embeds the given text in the given extracted text and returns the d1, d2, d3,
    // and d4 coefficients
    public int[] embedText(int d1, int d2, int d3, int d4, String text) {
        // Calculate the average value of the d1, d2, d3, and d4 coefficients
        // int avg = (d1 + d2 + d3 + d4) / extractedText.length();

        // Initialize the array for storing the d1, d2, d3, and d4 coefficients
        int[] coefficients = new int[4];

        // Embed the text one character at a time
        for (int i = 0; i < 8; i++) {
            // Extract the i-th bit of the text
            int bit = text.charAt(i) - '0';

            // Calculate the value of the i-th bit of the d1 coefficient
            int b1 = (d1 >> i) & 1;
            int d1Bit = (b1 + bit) % 2;

            // Calculate the value of the i-th bit of the d2 coefficient
            int b2 = (d2 >> i) & 1;
            int d2Bit = (b2 + bit) % 2;

            // Calculate the value of the i-th bit of the d3 coefficient
            int b3 = (d3 >> i) & 1;
            int d3Bit = (b3 + bit) % 2;

            // Calculate the value of the i-th bit of the d4 coefficient
            int b4 = (d4 >> i) & 1;
            int d4Bit = (b4 + bit) % 2;

            // Set the i-th bit of the d1, d2, d3, and d4 coefficients
            d1 = (d1 & ~(1 << i)) | (d1Bit << i);
            d2 = (d2 & ~(1 << i)) | (d2Bit << i);
            d3 = (d3 & ~(1 << i)) | (d3Bit << i);
            d4 = (d4 & ~(1 << i)) | (d4Bit << i);
        }

        // Store the d1, d2, d3, and d4 coefficients in the array
        coefficients[0] = d1;
        coefficients[1] = d2;
        coefficients[2] = d3;
        coefficients[3] = d4;

        // Return the d1, d2, d3, and d4 coefficients
        return coefficients;
    }

    public int[][] performDWT(BufferedImage image) {
        // Initialize the array for storing the DWT coefficients
        int[][] dwt = new int[image.getWidth()][image.getHeight()];
    
        // Perform the DWT on the input image
        int c=0;
        System.out.println(image.getRGB(0, 0));
        for (int i = 0; i < image.getHeight()-1; i += 2) {
          for (int j = 0; j < image.getWidth()-1; j += 2) {
            // Extract the pixels from the input image
            int p1 = image.getRGB(j, i);
            int p2 = image.getRGB(j + 1, i);
            int p3 = image.getRGB(j, i + 1);
            int p4 = image.getRGB(j + 1, i + 1);
            if(c<4){
                System.out.println("p1: " + p1 + " p2: " + p2 + " p3: " + p3 + " p4: " + p4);
                c++;
            }
    
            // Calculate the d1, d2, d3, and d4 coefficients
            int d1 = (p1 + p2 + p3 + p4);
            int d2 = (p1 - p2 + p3 - p4);
            int d3 = (p1 + p2 - p3 - p4);
            int d4 = (p1 - p2 - p3 + p4);
    
            // Store the d1, d2, d3, and d4 coefficients in the DWT array
            dwt[j][i] = d1;
            dwt[j + 1][i] = d2;
            dwt[j][i + 1] = d3;
            dwt[j + 1][i + 1] = d4;
          }
        }
    
        // Return the DWT coefficients
        return dwt;
      }

}
