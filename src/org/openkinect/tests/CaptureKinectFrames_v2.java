package org.openkinect.tests;
import java.util.*;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.awt.image.RenderedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.nio.*;

import javax.imageio.ImageIO;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.CvType;
import org.opencv.imgproc.Imgproc;
import org.openkinect.processing.Kinect2;
import org.bytedeco.javacpp.avcodec;
import org.bytedeco.javacpp.avutil;
import org.bytedeco.javacpp.opencv_core.IplImage;
import org.bytedeco.javacv.FFmpegFrameRecorder;

import processing.core.PApplet;
import processing.core.PImage;

import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.FrameRecorder;
import org.bytedeco.javacv.FrameRecorder.Exception;
import org.bytedeco.javacv.OpenCVFrameConverter;

public class CaptureKinectFrames_v2 extends PApplet implements Serializable{
	public  final String depthFilename = "outDepth";
	public  final String irFilename = "outIR";
	public  final String videoFilename = "outVideo";
	public  final String registredFilename = "outRegistred";
	public final String mainDirectory="./images/";
	//long currentTime = System.currentTimeMillis();
	int currentNum=5656;
	public String resDirectory =mainDirectory+"GRAY"+currentNum+"/";
	
	
	
	Kinect2 kinect2;
	PImage img;
	 
	byte [] bArray;
	int [] iArray;
	int pixCnt1, pixCnt2;
	
	ArrayList<IplImage> depthFrames = new ArrayList<IplImage>();
	ArrayList<IplImage> irFrames = new ArrayList<IplImage>();
	ArrayList<IplImage> videoFrames = new ArrayList<IplImage>();
	ArrayList<IplImage> registredFrames = new ArrayList<IplImage>();
	//ArrayList<byte[]> byteDepthData = new ArrayList<byte[]>();
	
	
	public static void main(String[] args) {
		PApplet.main(new String[] { "org.openkinect.tests.CaptureKinectFrames_v2"});
	}
	
	public void settings() {
		size(512*2, 424*2, P2D);		
	}
	
	public void setup() {
		kinect2 = new Kinect2(this);
		// Start all data
		kinect2.initVideo();
		kinect2.initDepth();
		kinect2.initIR();
		kinect2.initRegistered();
		kinect2.initDevice();
		// Load the OpenCV native library.
		  System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
		  println(Core.VERSION);

			File file = new File(resDirectory);
			if (!file.exists()) {
				if (file.mkdir()) {
					System.out.println(resDirectory + "     Directory is created!");
				} else {
					System.out.println("Failed to create directory!");
				}
			}
	}


	public void draw() {
		background(0);
		//image(kinect2.getVideoImage(), 0, 0, width, height);
		PImage currentDepthFrame = kinect2.getDepthImage();
		PImage currentVideoFrame =kinect2.getVideoImage();
		PImage currentIRFrame =kinect2.getIrImage();
		PImage currentRegistredFrame = kinect2.getRegisteredImage();
		
		workWithCurrentFrame(currentDepthFrame,depthFrames,kinect2.depthWidth,kinect2.depthHeight );
		workWithCurrentFrame(currentIRFrame,irFrames,kinect2.depthWidth,kinect2.depthHeight );
		workWithCurrentFrame(currentVideoFrame,videoFrames,kinect2.colorWidth,kinect2.colorHeight );
		workWithCurrentFrame(currentRegistredFrame,registredFrames,kinect2.depthWidth,kinect2.depthHeight );
		
		image(currentDepthFrame, 0, 0);
		image(currentVideoFrame, kinect2.depthWidth, 0, kinect2.colorWidth*0.25f, kinect2.colorHeight*0.25f);
		image(currentIRFrame, 0, kinect2.depthHeight);
		image(currentRegistredFrame, kinect2.depthWidth, kinect2.depthHeight);
		fill(255);
		text("Framerate: " + (int)(frameRate), 10, 515);
	
	}
	
	public void dispose(){
		System.out.println("Stopping the video capture");
		  try {
//			  saveByteData(byteDepthData,depthFilename);
			  saveVideo(depthFrames, depthFilename);
			  saveVideo(irFrames, irFilename);
			  saveVideo(videoFrames, videoFilename);
			  saveVideo(registredFrames, registredFilename);
			  kinect2.stopDevice();
		  } catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		  }
	}
	
	
	
	/**
	 * 
	 * @param currentFrame
	 * @param frames
	 * @param filename
	 * @param currentWidth
	 * @param currentHeight
	 */
	public void workWithCurrentFrame(PImage currentFrame,ArrayList<IplImage> frames, int currentWidth, int currentHeight){
		
		// pixCnt1 is the number of bytes in the pixel buffer.
		  // pixCnt2 is the number of integers in the PImage pixels buffer.
		  pixCnt1 = currentWidth*currentHeight*4;
		  pixCnt2 = currentWidth*currentHeight;
		 
		  // bArray is the temporary byte array buffer for OpenCV cv::Mat.
		  // iArray is the temporary integer array buffer for PImage pixels.
		  bArray = new byte[pixCnt1];
		  iArray = new int[pixCnt2];
	     arrayCopy(currentFrame.pixels, iArray);
		 //Define the temporary Java byte and integer buffers. 
		 // They share the same storage.
		  ByteBuffer bBuf = ByteBuffer.allocate(pixCnt1);
		  IntBuffer iBuf = bBuf.asIntBuffer();
		 
		  // Copy the webcam image to the byte buffer iBuf.
		  iBuf.put(iArray);
	
		  // Copy the webcam image to the byte array bArray.
		  bBuf.get(bArray);
		  
		  
		  //byteDepthData.add(bArray);
		  // Create the OpenCV cv::Mat.
		 
		  Mat m1 = new Mat(currentHeight,currentWidth, CvType.CV_8UC4);
		 
		  // Initialize the matrix m1 with content from bArray.
		  m1.put(0, 0, bArray);
		 // Clone m1 into another matrix m2. Now m2 is an exact copy of m1.
		  Mat m2 = m1.clone();
		 
		  
		  Imgproc.cvtColor(m2, m2, Imgproc.COLOR_BGR2GRAY);
		  
		  // Copy content of m2 to the byte array bArray.
		  m2.get(0, 0, bArray);
		  
		//convert to the iplImage
		  IplImage currentIplFrame = processImage(bArray,  currentWidth,  currentHeight);
		  
		  frames.add(currentIplFrame);
		 
		  

	}//endof workWithCurrentFrame
	
	
	public void saveByteData(ArrayList<byte[]> frames, String filename){
		try{
	        FileOutputStream fos= new FileOutputStream(resDirectory+filename);
	        ObjectOutputStream oos= new ObjectOutputStream(fos);
	        oos.writeObject(frames);
	        oos.close();
	        fos.close();
	      }catch(IOException ioe){
	           ioe.printStackTrace();
	      }
	}
	
	/**
	 * 
	 * @param frames
	 * @throws Exception
	 */
	public void saveVideo(ArrayList<IplImage> frames, String filename) throws Exception{
		System.out.println(frames.size());
		FrameRecorder recorder;
		OpenCVFrameConverter.ToIplImage converter = new OpenCVFrameConverter.ToIplImage();
		int width = frames.get(0).width();
		int height  = frames.get(0).height();
		
		
		recorder = FrameRecorder.createDefault(resDirectory+filename+".mp4", width, height);
		recorder.setFrameRate(60);
		recorder.setVideoCodec(avcodec.AV_CODEC_ID_H264); // AV_CODEC_ID_FLV1
        recorder.setVideoOption("preset", "ultrafast");
        recorder.setPixelFormat(avutil.AV_PIX_FMT_YUV420P);
		recorder.setVideoQuality(5.0);
		recorder.start();
		for(IplImage currentIplFrame:frames){
			//make a frame out of IplImage:
			Frame convFrame = converter.convert(currentIplFrame);
			recorder.record(convFrame);
		}
		recorder.stop();
	}
	
	
	/**
	 * 
	 * @param data
	 * @param width
	 * @param height
	 * @return
	 */
	protected IplImage processImage(byte[] data, int width, int height) {
	    

	    // First, downsample our image and convert it into a grayscale IplImage
	    IplImage grayImage = IplImage.create(width , height, org.bytedeco.javacpp.opencv_core.IPL_DEPTH_8U,1);

	    int imageWidth = grayImage.width();
	    int imageHeight = grayImage.height();
	    int dataStride = width;
	    int imageStride = grayImage.widthStep();
	    ByteBuffer imageBuffer = grayImage.getByteBuffer();
	    for (int y = 0; y < imageHeight; y++) {
	        int dataLine = y * dataStride;
	        int imageLine = y * imageStride;
	        for (int x = 0; x < imageWidth; x++) {
	            imageBuffer.put(imageLine + x, data[dataLine +  x]);
	        }
	    }

	    return grayImage;
	}
	
	//helpers
	
	

	
	
	

}
