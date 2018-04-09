package videoEncoder;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.*;

import javax.imageio.ImageIO;

public class JPEGCompression {
	
	
	public static double[][][] getRGBPixel(BufferedImage ori){
		// This function use to save RGB values in 2D-matrix
	      int green=0,red=0,blue=0;
	      int imageWidth=ori.getWidth();
	      int imageHeight=ori.getHeight();
	      double[][][] rgbImage = new double[imageWidth][imageHeight][3];
	      for(int i= ori.getMinX();i<imageWidth;i++){
	          for(int j = ori.getMinY();j<imageHeight;j++){
	             Object data =ori.getRaster().getDataElements(i, j, null);//get pixel
	             red = ori.getColorModel().getRed(data);//get red values
	             green = ori.getColorModel().getGreen(data);//get green values
	             blue = ori.getColorModel().getBlue(data);// get blue values
	             rgbImage[i][j][0]=red;
	             rgbImage[i][j][1]=green;
	             rgbImage[i][j][2]=blue;
	          }
	      }
	      return rgbImage;
	}
	
	public static double[] matrixRGBtoYCbCR(double[] rgb) {
		//this method use to calculate YCrCb values from each RGB array
	      double Y = 0.0; double Cb = 0.0; double Cr = 0.0;
	      double R = (double)rgb[0]; double G = (double)rgb[1]; double B = (double)rgb[2];              
	      Y = R*(0.2990) + G*(0.5870) + B*(0.1140);
	      Cb = R*(-0.168736) + G*(-0.331264) + B*(0.5000);
	      Cr = R*(0.5000) + G*(-0.418688) + B*(-0.081312);    
	      Y = Math.max(Math.min(Y, 255), 0);
	      Cb = Math.max(Math.min(Cb, 127.5), -127.5);
	      Cr = Math.max(Math.min(Cr, 127.5), -127.5);        
	      Y = Y - 128.0;
	      Cb = Cb - 0.5;
	      Cr = Cr - 0.5;        
	      return new double[] {Y, Cb, Cr};
	}
	
	public static double[][][] transform(double[][][] rgbimage,int width,int height,int minx,int miny){
		// this method transform each RGB array in pixel into YCrCb color mode
		double[][][] YUVimage = new double[width][height][3];
		for(int y = 0; y < height; y++){
		    for(int x = 0; x < width; x++){ 
		        YUVimage[x][y] = matrixRGBtoYCbCR(rgbimage[x][y]);//
		    }
		 }
		return YUVimage;
	}
	
	public static double[][] sperateY(double[][][] YUVimage,int width,int height,int minx,int miny){
		// separate Y components from YCrCb
		double[][] Y = new double[width][height];
		 for(int y = miny; y < height; y++){
		     for(int x = minx; x < width; x++){ 
		          Y[x][y] = YUVimage[x][y][0];
		     }
		}
		return Y;
	}

	static strictfp double[][] dctTransform(double matrix[][]){	
		int m = 8;
		int n = 8;
		double pi = 3.142857;
        int i, j, k, l;
        // dct will store the discrete cosine transform
        double[][] dct = new double[m][n];
        double ci, cj, dct1, sum;
        for (i = 0; i < m; i++){
            for (j = 0; j < n; j++){
                // ci and cj depends on frequency as well as
                // number of row and columns of specified matrix
                if (i == 0)
                    ci = 1 / Math.sqrt(m);
                else
                    ci = Math.sqrt(2) / Math.sqrt(m);    
                if (j == 0)
                    cj = 1 / Math.sqrt(n);
                else
                    cj = Math.sqrt(2) / Math.sqrt(n);
                // sum will temporarily store the sum of 
                // cosine signals
                sum = 0;
                for (k = 0; k < m; k++) 
                {
                    for (l = 0; l < n; l++) 
                    {
                        dct1 = matrix[k][l] * 
                               Math.cos((2 * k + 1) * i * pi / (2 * m)) * 
                               Math.cos((2 * l + 1) * j * pi / (2 * n));
                        sum = sum + dct1;
                    }
                }
                dct[i][j] = ci * cj * sum;
            }
        }
        return dct;
    }
	
	public static double[][][][] separteBlock(double[][] f){
		// this function separate the matirx into 8x8 sub blocks
		int sizeX = f.length;
	    int sizeY = f[0].length;
	    double[][][][] F = new double[sizeX][sizeY][8][8];
//	    System.out.println("sizeX "+ sizeX);
//	    System.out.println("sizeY "+ sizeY);
	    for(int yy = 0;yy<sizeX;yy += 8) {
	    		for(int xx = 0;xx <sizeY; xx += 8) {
	    			for(int u1 = 0; u1 < 8;u1++) {
	    				for(int v1 = 0; v1 < 8; v1++) {
//	    					System.out.println("xx:"+xx);
//	    					System.out.println("yy:"+yy);
	    					F[xx][yy][u1][v1] =  f[xx+u1][yy+v1];
	    				}
	    			}
	    		}
	    }
		return F;	
	}
	
	public static double[][][][] DCTBlock(double[][][][] f) {
		// this function do the dct transform in each sub blocks
		double[][][][] dctresult = new double[f.length][f[0].length][f[0][0].length][f[0][0][0].length];
		for(int xx = 0;xx<f.length;xx +=8) {
			for(int yy =0;yy<f[0].length;yy +=8) {
				dctresult[xx][yy] = dctTransform(f[xx][yy]);
			}
		}
		return dctresult;
	}
	
	public static void printArray3D(double[][][] F){
		// a test function to print 3D-array
	      int X = F.length;
	      int Y = F[0].length;
	          for(int m = 0; m < 3; m++){
	             for(int y = 0; y < Y; y++){
	                 for(int x = 0; x < X; x++){
	                	 	if(F[x][y][m] != 0) {
	                	 		System.out.print(F[x][y][m] + "  ");
	   	                     System.out.print("\t");
	                	 	}
	                 }
	                 System.out.println("");
	             }   
	          System.out.println("");
	     }
	     System.out.println("");
	 }
	
	public static void printArray2D(double[][] F){
		// a test function to print 2D-array
	      int X = F.length;
	      int Y = F[0].length;
	      for(int y = 0; y < Y; y++){
	          for(int x = 0; x < X; x++){
	               System.out.print(F[x][y] + "  ");
	               System.out.print("\t");
	          }
	          System.out.println("");
	      }   
	      System.out.println("");
	 }
	
	public static int[][][][] quantization(double[][][][] dct) {
		// this function complete the quantization of each sub blocks
		 final int[] QUANTUM_LUMINANCE = { 16,  11,  10,  16,  24,  40,  51,  61, 
                12,  12,  14,  19,  26,  58,  60,  55, 
                14,  13,  16,  24,  40,  57,  69,  56, 
                14,  17,  22,  29,  51,  87,  80,  62, 
                18,  22,  37,  56,  68, 109, 103,  77, 
                24,  35,  55,  64,  81, 104, 113,  92, 
                49,  64,  78,  87, 103, 121, 120, 101, 
                72,  92,  95,  98, 112, 100, 103,  99};
		 int size = dct[0][0].length;
		 int[][][][]  dst = new int[dct.length][dct.length][size][size];
//		 System.out.print(dct[0].length);
		 for (int y = 0; y < dct.length; y += 8) { 
			   for (int x = 0; x < dct[0].length; x +=8) { 
				   for(int n = 0; n < size; n++) {
					   for(int m = 0; m < size;m++) {
						   dst[x][y][m][n] = (int) (dct[x][y][m][n] / QUANTUM_LUMINANCE[ n*8 + m]); 
					   }
				   }
			   } 
		 }
		 return dst;
		 
	}
	
	public static int[] linearizeBlock(int[][] blockData) {
		// this function transfer a 2D array into a 1D list
        int[] linearBlockData = new int[blockData.length * blockData.length]; 
        int index = 0; 
        for (int i=0;i < blockData.length;i++) { 
            for (int j=0;j < blockData[i].length;j++) { 
                linearBlockData[index] = blockData[i][j]; 
                index++; 
            } 
        } 
        return linearBlockData; 
    }
	
	
		int[] ZIGZAG_ORDER = 
			  { 0,  1,  8,  16,  9,  2,  3, 10, 
                17, 24, 32, 25, 18, 11,  4,  5, 
                12, 19, 26, 33, 40, 48, 41, 34, 
                27, 20, 13,  6,  7, 14, 21, 28, 
                35, 42, 49, 56, 57, 50, 43, 36, 
                29, 22, 15, 23, 30, 37, 44, 51, 
                58, 59, 52, 45, 38, 31, 39, 46, 
                53, 60, 61, 54, 47, 55, 62, 63};
	 
	
	public static void main(String args[])throws IOException{
		File image = new File("lenna.png");
		BufferedImage bi = ImageIO.read(image);
		int width = bi.getWidth();
        int height = bi.getHeight();
        int minx = bi.getMinX();
        int miny = bi.getMinY();
//        int size = (Math.max(width, height)/8 + 1)*8;
        
        //the width and height of picture
        System.out.println("width=" + width + ",height=" + height + ".");
        System.out.println("minx=" + minx + ",miniy=" + miny + ".");
        
        double[][][] rgbimage = getRGBPixel(bi);
        // print the rgbmatrix
        for(int i = 0;i<256;i++) {
        		for(int j = 0;j<256;j++) {
        			System.out.print("rgbimage["+i+"]["+j+"]"+" R: "+rgbimage[i][j][0]+" G: "+rgbimage[i][j][1] +" B: "+rgbimage[i][j][0]);
        		}
        		System.out.println("");
        }
        
        double[][][] yuvimage = transform(rgbimage,width,height,minx,miny);
        //print the yuv matrix
        for(int i = 0;i<256;i++) {
    		for(int j = 0;j<256;j++) {
    			System.out.print("yuvimage["+i+"]["+j+"]"+" Y: "+yuvimage[i][j][0]+" Cr: "+yuvimage[i][j][1] +" Cb: "+yuvimage[i][j][0]);
    		}
    		System.out.println("");
    }
        
        double[][] Y = sperateY(yuvimage,width,height,minx,miny);
//        printArray3D(yuvimage);
        double[][][][] separteBlock = separteBlock(Y);
        double[][][][] DCTBlock = DCTBlock(separteBlock);
        //print result of dct sub block[0][0]
        System.out.println("result of dct sub block[0][0]");
        for (int i = 0; i < 8; i++) {
        		for (int j = 0; j < 8; j++) {
        			System.out.print(DCTBlock[0][0][i][j] + " ");
        			System.out.print("\t");
        		}
        	System.out.println("");
        }
        
        int[][][][] dst = quantization(DCTBlock);
        
        //print the quantization result of sub block [0][0]
        System.out.println("quantization result of sub block[0][0]");
        for (int i = 0; i < 8; i++) {
              for (int j = 0; j < 8; j++) {
            	  	System.out.print(dst[0][0][i][j] + " ");
              	System.out.print("\t");
              }
              System.out.println("");
        }
        
        int[] linearblock = linearizeBlock(dst[0][0]);
        
	}
}