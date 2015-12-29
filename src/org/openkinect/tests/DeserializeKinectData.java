package org.openkinect.tests;
import java.io.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.imageio.ImageIO;

import org.bytedeco.javacpp.opencv_core.IplImage;
public class DeserializeKinectData 
{
	public  final static String depthFilename = "./images/outDepth";
	public  final String irFilename = "./images/outIR";
	public  final String videoFilename = "./images/outVideo";
	
	
	
	
    public static void main(String [] args) throws ClassNotFoundException, IOException
    {
    	ArrayList<byte[]> depthImages= new ArrayList<byte[]>();
        try
        {
            FileInputStream fis = new FileInputStream(depthFilename);
            ObjectInputStream ois = new ObjectInputStream(fis);
            depthImages = (ArrayList) ois.readObject();
            System.out.println(depthImages.size());
            ois.close();
            fis.close();
         }catch(IOException ioe){
             ioe.printStackTrace();
             return;
          }catch(ClassNotFoundException c){
             System.out.println("Class not found");
             c.printStackTrace();
             return;
          }
       
    }
}

