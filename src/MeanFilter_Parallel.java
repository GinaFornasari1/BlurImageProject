import java.io.*; 
import java.util.*;
import java.util.concurrent.ForkJoinPool; 
import java.util.concurrent.RecursiveAction; 
import java.awt.image.BufferedImage; 
import javax.imageio.ImageIO;

/** Class that replaces the pixels within a window with the mean values 
of the pixels using fork-join concurrency
*/

public class MeanFilter_Parallel extends RecursiveAction{
    
    private String inputFileName; 
    private String outputFileName;
    private int windowSize; 
    private BufferedImage imageInput; 
    private BufferedImage imageOutput; 
    private int forkStart; 
    private int forkEnd;
    //the sequential cutoff,it is not static and final due to testing  
    private final int THRESHOLD = 5000;
    
    //variables used to time the core code
    static long startingTime; 
    static long endingTime; 
    
    /** The constructor initializes the variables and creates an object
    */
    public MeanFilter_Parallel(BufferedImage imageIn, BufferedImage imageOut, int start, int end, int width){
        this.imageInput = imageIn; 
        this.imageOutput = imageOut; 
        this.forkStart=start; 
        this.forkEnd=end; 
        this.windowSize=width; 
    }            
    
    /**This is called when the fork join pool invokes the class object. 
    this method uses recursion to split the task into atomic tasks and 
    simultaneously solve them and return the completed solution
    */
    protected void compute(){
        //if the task is smaller than the sequential cutoff
        if(forkEnd-forkStart<THRESHOLD){
            int imageHeight = imageInput.getHeight(); 
            int imageWidth = imageInput.getWidth(); 
            
            int windowRadius = windowSize/2;
            
            //iterating through the image and window
            for(int widthPointer=windowRadius; widthPointer<imageWidth-windowRadius; widthPointer++){
            for(int heightPointer=windowRadius; heightPointer<imageHeight-windowRadius; heightPointer++){
                int alpha=0; int redSum=0; int greenSum=0; int blueSum =0; 
                    
                    for(int innerWindowWidthPointer=widthPointer-windowRadius;innerWindowWidthPointer<widthPointer+windowRadius+1; innerWindowWidthPointer++){
                        for(int innerWindowHeightPointer=heightPointer-windowRadius; innerWindowHeightPointer<heightPointer+windowRadius+1; innerWindowHeightPointer++){
                            int p=imageInput.getRGB(innerWindowWidthPointer,innerWindowHeightPointer);
                            alpha= (p>>24) & 0xff;
                            int r= (p>>16) & 0xff;
                            int g= (p>>8) & 0xff;
                            int b= p & 0xff;
                            
                            redSum += r; 
                            greenSum=g;  
                            blueSum+=b;                            
                        }
                    }
                    
                    int window = windowSize*windowSize; 
                    
                    redSum=redSum/window; 
                    blueSum=blueSum/window; 
                    greenSum = greenSum/window;
                    
                    //setting the output image pixels to the mean of the window's pixels 
                    int pixel = (alpha<<24)|(redSum<<16)|(greenSum<<8)|blueSum;
                    
                    imageOutput.setRGB(widthPointer,heightPointer,pixel);
                }
            }
           }
           //runs recursively 
           else{
               //splitting the task in half 
               MeanFilter_Parallel left = new MeanFilter_Parallel(imageInput,imageOutput,forkStart,(forkStart+forkEnd)/2,windowSize);
               MeanFilter_Parallel right = new MeanFilter_Parallel(imageInput, imageOutput, (forkStart+forkEnd)/2,forkEnd,windowSize);
               
               //sending half of the task to another thread
               left.fork(); 
               //continuing to solve the other half of the task
               right.compute(); 
               //avoiding a race condition and making sure all the threads are ready
               left.join();
           } 
        }
        
        /**methods used to calculate the time taken for a code to run between the 
        call of tick and tock
        */
        private static void tick(){
            startingTime = System.currentTimeMillis();
        }
        
        private static long tock(){
            endingTime = System.currentTimeMillis(); 
            return endingTime -startingTime;
        }
        
        private static BufferedImage CreateBufferedImage(String inputFileName) throws IOException
        {
            File imageFile = new File(inputFileName); 
            BufferedImage inputImage = ImageIO.read(imageFile);
            return inputImage; 
        }
        private static void CreateOutputFile(String fileName, BufferedImage outputImage)
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
        
        public static void main(String[] args)throws IOException{
            try{
                Scanner scInput = new Scanner(System.in);
                System.out.println("Enter the name of the file (including the file extension): ");
                String inputFileName = scInput.next();
                //error handling, giving the user one chance to correct their input before exiting out of the program      
                int count = 0;
                
                //checking input validity 
                while(inputFileName.indexOf(".")<1){
                    count++; 
                    if(count>1){
                        System.out.println("Invalid file entered");
                        System.exit(0);
                    }
                    
                    System.out.println("Please include the file extension.");
                    inputFileName = scInput.next();
                }
                
                count=0;    
                System.out.println("Enter the width of the window (an odd number greater than or equal to 3): ");
                int windowWidth = scInput.nextInt();
                
                //checking input validity 
                while((windowWidth%2==0) || windowWidth<3){
                    count++;
                    if(count>1){
                        System.out.println("Invalid width entered");
                        System.exit(0);
                    }
                    
                    System.out.println("Please use an odd number greater than or equal to 3.");
                    windowWidth = scInput.nextInt();
                }
                BufferedImage inputImage = CreateBufferedImage(inputFileName); 
                BufferedImage outputImage = CreateBufferedImage(inputFileName);
                int imageWidth = inputImage.getWidth();
                int imageHeight = inputImage.getHeight(); 
                //creating the pool of threads to assign in compute
                ForkJoinPool pool = new ForkJoinPool(); 
                //creating a call object
                MeanFilter_Parallel mean = new MeanFilter_Parallel(inputImage,outputImage,0,imageHeight*imageWidth,windowWidth);
                //starting the timer 
                tick();
                //calling compute on the class object
                pool.invoke(mean);
                long timeTaken = tock();
                System.out.println("The algorithm took " + timeTaken + " milliseconds to run");
                String outputFileName = inputFileName.substring(0,inputFileName.lastIndexOf("."))+"_MeanFilter_Parallel.jpeg";
                CreateOutputFile(outputFileName, outputImage);
            }catch(IOException e){
                System.out.println("File not found");
            }
        }
    }