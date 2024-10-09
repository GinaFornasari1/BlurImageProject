import java.io.*; 
import java.util.*; 
import java.awt.image.BufferedImage; 
import javax.imageio.ImageIO; 
import java.util.concurrent.ForkJoinPool; 
import java.util.concurrent.RecursiveAction; 

/** Class that replaces the pixels within a certain window with the 
middle pixel's values using fork-join concurrency
*/

public class MedianFilter_Parallel extends RecursiveAction{
    
    private String inputFileName; 
    private String outputFileName; 
    private int windowWidth; 
    private BufferedImage inputImage; 
    private BufferedImage outputImage; 
    
    private int forkStart; 
    private int forkEnd; 
    //the sequential cut-off. It is not static and final as it was using in testing and was constantly changing 
    private final int THRESHOLD = 5000;
    
    //variables used to time the core code
    static long startingTime; 
    static long endingTime;
    
    /** the constructor that initializes variables and creates a class object
    */
    public MedianFilter_Parallel(BufferedImage imageIn, BufferedImage imageOut, int start, int end, int width) {
        this.inputImage = imageIn; 
        this.outputImage = imageOut; 
        this.forkStart = start; 
        this.forkEnd = end; 
        this.windowWidth = width; 
    }
    
    /**method that runs when the fork pool calls invoke on the class object. 
    This method recursively divides up the task into atomic pieces and 
    simultaneously processes them.
    */
    protected void compute(){
        //if the task is smaller than the sequential cutoff
        if(forkEnd-forkStart<THRESHOLD){
            int imageHeight = inputImage.getHeight(); 
            int imageWidth = inputImage.getWidth(); 
            
            int imageRadius = windowWidth/2; 
            
            //iterating through the image and window windowWidth
            for(int widthPointer=imageRadius; widthPointer<imageWidth-imageRadius;widthPointer++){
                for(int heightPointer=imageRadius; heightPointer<imageHeight-imageRadius; heightPointer++){
                    int p=0,a=0,r=0,g=0,b=0;
                    ArrayList<Integer> reds = new ArrayList<Integer>();
                    ArrayList<Integer> greens = new ArrayList<Integer>();
                    ArrayList<Integer> blues = new ArrayList<Integer>();
                    
                    for(int innerWindowWidthPointer=widthPointer-imageRadius; innerWindowWidthPointer<widthPointer+imageRadius+1;innerWindowWidthPointer++){
                        for(int innerWindowHeightPointer=heightPointer-imageRadius; innerWindowHeightPointer<heightPointer+imageRadius+1;innerWindowHeightPointer++){
                            p=inputImage.getRGB(innerWindowWidthPointer,innerWindowHeightPointer);
                            a=(p>>24) & 0xff;
                            r=(p>>16) & 0xff;
                            g=(p>>8) & 0xff;
                            b= p & 0xff;
                            
                            reds.add(r);
                            greens.add(g); 
                            blues.add(b);
                        }
                    }
                    
                    reds = order(reds); 
                    greens = order(greens); 
                    blues = order(blues); 
                    
                    //getting the middle pixel 
                    int leng = (reds.size()/2)+1; 
                    
                    int midR = (int) reds.get(leng);
                    int midG = (int) greens.get(leng);
                    int midB = (int) blues.get(leng);
                    
                    //setting the pixel values of the pixels involved to the middle pixel's values
                    p = (a<<24)|(midR<<16)|(midG<<8)|midB;
                    outputImage.setRGB(widthPointer,heightPointer,p);
                }
            }
        }
        //while the task is too big
            else{
                //splitting the task in half
                MedianFilter_Parallel left = new MedianFilter_Parallel(inputImage, outputImage, forkStart, (forkStart+forkEnd)/2,windowWidth);
                MedianFilter_Parallel right = new MedianFilter_Parallel(inputImage, outputImage, (forkStart+forkEnd)/2, forkEnd, windowWidth);
                
                //sending one half to another thread to be processed
                left.fork(); 
                //continue to calculate the current half  
                right.compute(); 
                //avoid a race condition by making the threads sent off to wait for the other threads to finish
                left.join();
            }
        }
        
        /**helper methods used to order the array list recurively
        */
        public ArrayList<Integer> order(ArrayList<Integer> list){
            boolean changed = false; 
            
            for(int i=0; i<list.size()-1; i++){
                if(list.get(i)>list.get(i+1)){
                    changed = true; 
                    swap(list,i,i+1);
                }
            }
            if(changed){
                return order(list);
            }
            else{
                return list;
            }
        }
        
        public void swap(ArrayList<Integer> list, int one, int two){
            int swap = list.get(one); 
            list.set(one, list.get(two));
            list.set(two,swap);
        }
        
        /**methods used to calculate the time taken for the core 
        code to run
        */
        private static void tick(){
            startingTime = System.currentTimeMillis();
        }
        
        private static long tock(){
            endingTime = System.currentTimeMillis(); 
            return endingTime - startingTime;
        }
        
        public static void main(String[] args){
            try{
                Scanner scInput = new Scanner(System.in); 
                System.out.println("Enter the name of the input file (including the file extension): "); 
                String inFile = scInput.next(); 
                
                //error handling, giving the user one chance to correct thier input before the program closes
                int count=0; 
                //validating input
                while(inFile.indexOf(".")<1){
                    count++; 
                    if(count>1){
                        System.out.println("Invalid file entered");
                        System.exit(0);
                    }
                    
                    System.out.println("Please include the file extension");
                    inFile = scInput.next();
                }
                
                count=0; 
                System.out.println("Enter the width of the window: (An odd number greater than or equal to 3) "); 
                int width = scInput.nextInt();
                
                //validating input
                while((width%2==0)||width<3){
                    count++; 
                    if(count>1){
                        System.out.println("Invalid width entered");
                        System.exit(0);
                    }
                    System.out.println("Please enter an odd number greater than or equal to 3");
                    width = scInput.nextInt();
                }
                //initializing the input image object
                File file1 = new File(inFile);
                BufferedImage inIm = ImageIO.read(file1);
                
                int imageWidth = inIm.getWidth();
                int imageHeight = inIm.getHeight();
                
                //using the inout image as a template for the output image
                BufferedImage outputImg = ImageIO.read(file1);
                
                //creating a pool of threads that will be assigned tasks
                ForkJoinPool pool = new ForkJoinPool();
                
                //creating a class object
                MedianFilter_Parallel median = new MedianFilter_Parallel(inIm,outputImg,0,imageHeight*imageWidth,width);
                
                //starting the timer
                tick();
                //calling the concurrent computation
                pool.invoke(median);
                long timeTaken = tock();
                
                System.out.println("The algorithm took "+timeTaken+" milliseconds to run");
                
                //creating the output image
                String outFile = inFile.substring(0, inFile.lastIndexOf(".")) + "_MedianFilter.Parallel.jpeg";
                File file2 = new File(outFile);
                ImageIO.write(outputImg,"jpeg",file2);                
                
            }catch(IOException e){
                System.out.println("File not found");
            
            }
        }
        
    }