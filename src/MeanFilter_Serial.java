import javax.imageio.ImageIO;
import java.util.*; 
import java.io.*; 
import java.awt.image.BufferedImage; 

public class MeanFilter_Serial
{
    private String inputFileName; 
    private String outputFileName; 
    private int windowSize; 
    private BufferedImage imageInput;
    private BufferedImage imageOutput; 
    static long startingTime; 
    static long endingTime;

    public MeanFilter_Serial(String inputFileName, int windowSize) throws IOException
    {
        this.inputFileName = inputFileName;
        this.outputFileName = inputFileName.substring(0,inputFileName.indexOf(".")) + "_MeanFilter_Serial.jpeg";
        this.windowSize = windowSize;
        try
        {
            this.imageInput = CreateBufferedImage(inputFileName); 
            tick(); 
            this.imageOutput = GetOutputImage(imageInput, windowSize);
            long timeTaken = tock(); 
            System.out.println("The algorithm took " + timeTaken + " milliseconds to run");
            CreateOutputFile(outputFileName, imageOutput);
        }
        catch(Exception ex)
        {
            System.out.println("Could not make buffered image: "+ ex);
        }
    }

    private BufferedImage CreateBufferedImage(String inputFileName) throws IOException
    {
        File imageFile = new File(inputFileName); 
        BufferedImage inputImage = ImageIO.read(imageFile);
        return inputImage; 
    }

    private BufferedImage GetOutputImage(BufferedImage outputImage, int windowSize) throws IOException
    {
        int windowRadius = windowSize/2; 
        int imageWidth = outputImage.getWidth(); 
        int imageHeight = outputImage.getHeight(); 
        /*
        changing the middle pixel of the window, therefore we need at least
        half the window size (the window radius) on each side, therefore i starts
        at the window Radius and ends with a radius length on the left
        */
        for(int widthPointer=windowRadius; widthPointer<imageWidth-windowRadius; widthPointer++)
        {
            //same as width
            for(int heightPointer=windowRadius; heightPointer<imageHeight-windowRadius; heightPointer++)
            {
                //now the counters sit on a target pixel to change
                int alpha=0; int redSum=0; int greenSum=0; int blueSum =0; 
                /*
                  get the target pixel's surrounding pixels colors
                 */
               for(int innerWindowWidthPointer=widthPointer-windowRadius;innerWindowWidthPointer<widthPointer+windowRadius+1; innerWindowWidthPointer++)
               {
                for(int innerWindowHeightPointer=heightPointer-windowRadius; innerWindowHeightPointer<heightPointer+windowRadius+1; innerWindowHeightPointer++)
                {
                    int pixel = outputImage.getRGB(innerWindowWidthPointer,innerWindowHeightPointer); 
                    alpha = (pixel>>24) & 0xff; 
                    redSum += (pixel>>16) & 0xff; 
                    greenSum += (pixel>>8) & 0xff; 
                    blueSum += pixel & 0xff;  
                }
               } 
               int totalPixels = windowSize*windowSize;
               int meanR =redSum/totalPixels; 
               int meanG =greenSum/totalPixels; 
               int meanB =blueSum/totalPixels; 
               int newPixel = (alpha<<24)|(meanR<<16)|(meanG<<8)|meanB;
               outputImage.setRGB(widthPointer, heightPointer, newPixel);
            }
        }
        return outputImage; 
    }
    private static void tick(){
        startingTime = System.currentTimeMillis();
    }
    private static long tock(){
        endingTime = System.currentTimeMillis(); 
        return endingTime - startingTime;
    }

    private void CreateOutputFile(String fileName, BufferedImage outputImage)
    {
        try
        {
            File outputFile = new File(fileName); 
            ImageIO.write(outputImage, "jpeg", outputFile);
        }
        catch(Exception ex)
        {
            System.out.println("Could not return buffered image file: "+ ex); 
        }
    }

    public static void main(String[] args)
    {
        try
        {
            Scanner scInput = new Scanner(System.in); 
            System.out.println("Enter the name of the file (including the file extension): ");
            String inputName = scInput.next();
            
             //testing if the extension was included in the input 
             while(inputName.indexOf(".")<1)
             {
                System.out.println("Invalid file entered, please include the file extension");
                inputName = scInput.next();
            }
            System.out.println("Enter the width of the window:(odd integer >=3)"); 
            int windowWidth=0; 
            try
            {
                windowWidth = scInput.nextInt(); 
            }
            catch(Exception e)
            {
                System.out.println("Invalid entry. Exiting..."); 
                scInput.close();
                System.exit(0); 
            }
            
            while(windowWidth%2==0 || windowWidth<3)
            {
                if(windowWidth!=0){System.out.println("Invalid input"); }
                scInput.close();
                System.exit(0);
            }
            MeanFilter_Serial filter = new MeanFilter_Serial(inputName, windowWidth);
            scInput.close();
        }
        catch(IOException e)
        {
            System.out.println("File not found: " + e);
        }
    }


}
