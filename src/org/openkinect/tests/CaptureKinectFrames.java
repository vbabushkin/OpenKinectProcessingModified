package org.openkinect.tests;
import java.util.*;
import java.awt.image.BufferedImage;
import java.nio.*;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.CvType;
import org.opencv.core.Scalar;
import org.opencv.highgui.Highgui;
import org.opencv.imgproc.Imgproc;
import org.openkinect.processing.Kinect2;
import org.bytedeco.javacv.FFmpegFrameRecorder;

import static org.bytedeco.javacpp.opencv_core.*;
import static org.bytedeco.javacpp.opencv_imgproc.*;
import static org.bytedeco.javacpp.opencv_imgcodecs.*;
import processing.core.PApplet;
import processing.core.PImage;

import org.bytedeco.javacpp.avcodec;
import org.bytedeco.javacpp.avutil;
import org.bytedeco.javacv.FFmpegFrameRecorder;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.FrameRecorder;
import org.bytedeco.javacv.FrameRecorder.Exception;
import org.bytedeco.javacv.OpenCVFrameConverter;
import org.bytedeco.javacpp.opencv_core.*;

public class CaptureKinectFrames extends PApplet{
	public  final String FILENAME = "./images/output.mp4";//mp4";
	public  final String FILENAME1 = "./images/output1.mp4";//mp4";
	public String filepath= ".//images//currentFrame2.jpg";
	Kinect2 kinect2;
	PImage img;
	 
	byte [] bArray;
	int [] iArray;
	int pixCnt1, pixCnt2;
	
	ArrayList<IplImage> frames = new ArrayList<IplImage>();
	 
	public static void main(String[] args) {
		PApplet.main(new String[] { "org.openkinect.tests.CaptureKinectFrames"});
	}
	
	
	public void settings() {
		size(512, 424, P2D);
		}
	
	public void setup() {

		background(0);
		
		// Define and initialize the default capture device.
		kinect2 = new Kinect2(this);
		
		// Start all data
		
		kinect2.initDepth();

		kinect2.initDevice();
		
		
	// Load the OpenCV native library.
	  System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
	  println(Core.VERSION);
	 
	  
	  
	  // pixCnt1 is the number of bytes in the pixel buffer.
	  // pixCnt2 is the number of integers in the PImage pixels buffer.
	  pixCnt1 = width*height*4;
	  pixCnt2 = width*height;
	 
	  // bArray is the temporary byte array buffer for OpenCV cv::Mat.
	  // iArray is the temporary integer array buffer for PImage pixels.
	  bArray = new byte[pixCnt1];
	  iArray = new int[pixCnt2];
	 
	  img = createImage(width, height, ARGB);
	}


	public void draw() {
		// Copy the webcam image to the temporary integer array iArray.
		 
		 PImage currentFrame = kinect2.getVideoImage();

         image(currentFrame, 0, 0);
		 fill(255);
		 text("Framerate: " + (int)(frameRate), 10, 515);
//		 
	      arrayCopy(currentFrame.pixels, iArray);
		 //Define the temporary Java byte and integer buffers. 
		 // They share the same storage.
		  ByteBuffer bBuf = ByteBuffer.allocate(pixCnt1);
		  IntBuffer iBuf = bBuf.asIntBuffer();
		 
		  // Copy the webcam image to the byte buffer iBuf.
		  iBuf.put(iArray);
	
		  // Copy the webcam image to the byte array bArray.
		  bBuf.get(bArray);
	
		  // Create the OpenCV cv::Mat.
//		 System.out.println(kinect2.depthHeight + "   "+ kinect2.depthWidth);
//		 System.out.println(height + "   "+ width);
		  Mat m1 = new Mat(kinect2.depthHeight,kinect2.depthWidth, CvType.CV_8UC4);
		 
		  // Initialise the matrix m1 with content from bArray.
		  m1.put(0, 0, bArray);
		 
		  
		  
		  
		  // Clone m1 into another matrix m2. Now m2 is an exact copy of m1.
		  Mat m2 = m1.clone();
		 
		  
		  Imgproc.cvtColor(m2, m2, Imgproc.COLOR_BGR2GRAY);
		  
		  // Copy content of m2 to the byte array bArray.
		  m2.get(0, 0, bArray);
		  
		  //save the mat:
		  //Highgui.imwrite(".//images//currentFrame.jpg",m2);
		  
		  //save the iplImage
		  IplImage currentIplFrame = processImage(bArray,  kinect2.depthWidth,  kinect2.depthHeight);
		  
		  frames.add(currentIplFrame);
		  //System.out.println(frames.size());
		  
		  //cvSaveImage(filepath, currentIplFrame);
		  //test
		  // Treat bArray as an integer buffer and copy the content to iArray.
		  ByteBuffer.wrap(bArray).asIntBuffer().get(iArray);
		 
		  // Copy the content of iArray to the PImage img pixels buffer.
		  arrayCopy(iArray, img.pixels);
//		  img.updatePixels();
//		 
//		  // Display the new image img.
//		  image(img, 0, 0);
//		  fill(255);
//		  text("Frame Rate: " + round(frameRate), 500, 50);
////		  
//		  try {
//			  saveVideo(frames, FILENAME);
//			  saveFfmpegVideo(frames, FILENAME1);
//		  } catch (Exception e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		  }
	}
	/**
	 * 
	 * @param frames
	 * @throws Exception
	 */
	public void saveVideo(ArrayList<IplImage> frames, String filename) throws Exception{
		FrameRecorder recorder;
		OpenCVFrameConverter.ToIplImage converter = new OpenCVFrameConverter.ToIplImage();
		int width = frames.get(0).width();
		int height  = frames.get(0).height();
		recorder = FrameRecorder.createDefault(filename, width, height);
		recorder.setFrameRate(60);
		recorder.setVideoCodec(avcodec.AV_CODEC_ID_H264); // AV_CODEC_ID_FLV1
        recorder.setVideoOption("preset", "ultrafast");
        recorder.setPixelFormat(avutil.AV_PIX_FMT_YUV420P);
        recorder.setVideoQuality(10.0);
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
	 * @param frames
	 * @throws Exception
	 */
	public void saveFfmpegVideo(ArrayList<IplImage> frames, String filename) throws Exception{
		FFmpegFrameRecorder  recorder;
		OpenCVFrameConverter.ToIplImage converter = new OpenCVFrameConverter.ToIplImage();
		boolean isPreviewOn = false;
		int frameRate = 60;
		int imageWidth = frames.get(0).width();
		int imageHeight  = frames.get(0).height();
		
		recorder = new FFmpegFrameRecorder(filename, width, height,0);
		recorder.setFormat("mp4");
        // Set in the surface changed method
        recorder.setFrameRate(frameRate);
		recorder.start();
		for(IplImage currentIplFrame:frames){
			//make a frame out of IplImage:
			Frame convFrame = converter.convert(currentIplFrame);
			recorder.record(convFrame);
		}
		
		recorder.stop();
		recorder.release();
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
	
	
}
