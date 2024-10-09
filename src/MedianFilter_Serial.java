import java.io.*; 
import java.util.*; 
import java.awt.image.BufferedImage; 
import javax.imageio.ImageIO; 
/** Class that takes in an image and replaces takes the 
pixel values with the median pixel values, returning an edited image
*/ 

public class MedianFilter_Serial{
    
    private String inputFileName; 
    private String outputFileName; 
    private int windowWidth; 
    private BufferedImage inputImage; 
    private BufferedImage outputImage; 
    
    //variables used for testing 
    static long startingTime; 
    static long endingTime;
    
    /**The constructor takes in the inputted values and initizes the values
    */    
    public MedianFilter_Serial(String inputFileName, String outputFileName, int windowWidth)throws IOException{
        this.inputFileName = inputFileName; 
        this.outputFileName = outputFileName; 
        this.windowWidth = windowWidth; 
        //initializing the images
        BufferedImage imageIn = null; 
        BufferedImage imageOut = null; 
        
        try{
            //reading the inputted image 
            File file = new File(inputFileName); 
            imageIn = ImageIO.read(file);
            //using the imputted image as a template for the output image 
            imageOut = ImageIO.read(file);
            
            int windowRadius = windowWidth/2;
            int imageWidth = imageIn.getWidth();
            int imageHeight = imageIn.getHeight();
            
            //storing the current time in milliseconds used for testing 
            tick();
            
            //iterating though the image and the window
            for(int widthPointer=windowRadius; widthPointer<imageWidth-windowRadius; widthPointer++)
            {
                //same as width
                for(int heightPointer=windowRadius; heightPointer<imageHeight-windowRadius; heightPointer++)
                {
                    int p=0,a=0,r=0,g=0,b=0;
                    ArrayList reds = new ArrayList<Integer>(); 
                    ArrayList greens = new ArrayList<Integer>();
                    ArrayList blues = new ArrayList<Integer>();
                    
                    for(int innerWindowWidthPointer=widthPointer-windowRadius;innerWindowWidthPointer<widthPointer+windowRadius+1; innerWindowWidthPointer++)
               {
                for(int innerWindowHeightPointer=heightPointer-windowRadius; innerWindowHeightPointer<heightPointer+windowRadius+1; innerWindowHeightPointer++)
                {
                            p = imageIn.getRGB(innerWindowWidthPointer,innerWindowHeightPointer);
                            a = (p>>24) & 0xff; 
                            r = (p>>16) & 0xff;
                            g = (p>>8) & 0xff;
                            b = p & 0xff;
                            
                            reds.add(r);
                            greens.add(g); 
                            blues.add(b);
                        }
                    }
                    
                    reds = order(reds);
                    greens = order(greens); 
                    blues = order(blues);
                    
                    //getting the middle value in the array of values 
                    int leng = (reds.size()/2)+1;
                    
                    int midR=(int)reds.get(leng); 
                    int midG=(int)greens.get(leng); 
                    int midB=(int)blues.get(leng);                  
                   
                    //setting the output image pixels to the median pixel values           
                    p=(a<<24)|(midR<<16)|(midG<<8)|midB;
                    imageOut.setRGB(widthPointer,heightPointer,p);
                }
            }
            
            //creating an image object with the output 
            File file2 = new File(outputFileName); 
            ImageIO.write(imageOut,"jpeg",file2);
            
            }catch(IOException e){
                System.out.println("File not found");
                //System.out.println(e);
            }
            
            //getting the time taken for the core of the program to run
            long timeTaken = tock();
            System.out.println("The algorithm took "+timeTaken + " milliseconds");
           }
           
           /**helper methods used to sort the Array lists recursively
           */
           public ArrayList<Integer> order(ArrayList<Integer> list){
               boolean changed=false; 
               
               for(int i=0; i<list.size()-1;i++){
                  if(list.get(i)>list.get(i+1)){
                      changed = true; 
                      swap(list, i,i+1);
                  }
               }
               if(changed){
                   return order(list);
               }
               else{
                   return list;
               }
               
           }
           
           public void swap(ArrayList<Integer> list,int one, int two){
               int swap = list.get(one); 
               list.set(one,list.get(two)); 
               list.set(two,swap);
           }
          /**methods used to get the time and return the time between the
          call of tick and tock
          */
          
          private static void tick(){
           startingTime = System.currentTimeMillis();
          }
          
          private static long tock(){
            endingTime = System.currentTimeMillis();
            return endingTime - startingTime;
          }
          
           public static void main(String[] args) throws IOException{
               try{
                   Scanner scInput = new Scanner(System.in); 
                   System.out.println("Enter the name of the input file (including the file extension): "); 
                   String inFile = scInput.next(); 
                   //error handling, giving the user one change to fix thier input before exiting out of the program
                   int count = 0; 
                   
                   while(inFile.indexOf(".")<1){
                    count++;
                    if(count>1){
                     System.out.println("Invalid file entered");
                     System.exit(0);
                    }
                    System.out.println("Please include the file extension.");
                    inFile = scInput.next();
                   }
                   
                   count=0;
                   System.out.println("Enter the width of the window: "); 
                   int width = scInput.nextInt();
                    
                   //validating input
                   while((width%2==0) || width<3){
                    count++; 
                    if(count>1){
                     System.out.println("Invalid width entered");
                     System.exit(0);
                    }
                    System.out.println("Please enter an odd number greater than or equal to 3");
                   }
                   String outFile = inFile.substring(0, inFile.lastIndexOf(".")) + "_MedianFilter_Serial.jpeg"; 
                   MedianFilter_Serial filter = new MedianFilter_Serial(inFile, outFile, width);
               
               }catch(IOException e){
                   System.out.println("File not found");
                   
               }
           } 
       }     