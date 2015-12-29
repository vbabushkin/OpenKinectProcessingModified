package org.openkinect.tests;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.io.Serializable;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;

import org.bytedeco.javacpp.avcodec;
import org.bytedeco.javacpp.avutil;
import org.bytedeco.javacpp.opencv_core.IplImage;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.FrameRecorder;
import org.bytedeco.javacv.Java2DFrameConverter;
import org.bytedeco.javacv.OpenCVFrameConverter;
import org.bytedeco.javacv.FrameRecorder.Exception;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.highgui.Highgui;
import org.opencv.imgproc.Imgproc;
import org.openkinect.processing.Kinect2;

import processing.core.PApplet;
import processing.core.PImage;

public class CaptureKinectFrames_v3_BACKGROUND_NoLight extends PApplet implements Serializable{
	public  final String depthFilename = "outDepth";
	public  final String depthByteFilename = "outDepthByte";
	public  final String irFilename = "outIR";
	public  final String videoFilenameGray = "outVideoGray";
	public  final String videoFilenameRGB = "outVideoRGB";
	public  final String registredFilename = "outRegistred";
	
	public int frameCount=0;
	public int currentDevice =Settings.currentDevice;
	int currentNum=Settings.currentNumber;
	int mode = Settings.mode;
	
	public final String mainDirectory="./images/"+currentNum+"/";
	public String resDirectory =mainDirectory+currentDevice+"_BACKGROUND_"+currentNum+"/";
	public String depthDataDirectory =resDirectory+"/depthDataNoLight/";
	
	
	Kinect2 kinect2;
	PImage img;
	 
	byte [] bArray;
	int [] iArray;
	int pixCnt1, pixCnt2;
	
	ArrayList<Frame> depthFrames = new ArrayList<Frame>();
	ArrayList<Frame> videoFramesGray = new ArrayList<Frame>();
	//ArrayList<Frame> videoFramesGrayLight = new ArrayList<Frame>();
	ArrayList<Frame> videoFramesRGB = new ArrayList<Frame>();
	//ArrayList<Frame> videoFramesRGBLight = new ArrayList<Frame>();
	public static void main(String[] args) {
		PApplet.main(new String[] { "org.openkinect.tests.CaptureKinectFrames_v3_BACKGROUND_NoLight"});
	}
	
	public void settings() {
		size(512*2, 424*2, P2D);		
	}
	
	public void setup() {
		kinect2 = new Kinect2(this);
		// Start all data
		kinect2.initVideo();
		kinect2.initDepth();
		kinect2.initDevice();

		// Load the OpenCV native library.
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
		println(Core.VERSION);
		File file = new File(mainDirectory);
		if (!file.exists()) {
			if (file.mkdir()) {
				System.out.println(mainDirectory + "     Directory is created!");
			} else {
				System.out.println("Failed to create directory!");
			}
		}
		File file1 = new File(resDirectory);
		if (!file1.exists()) {
			if (file1.mkdir()) {
				System.out.println(resDirectory + "     Directory is created!");
			} else {
				System.out.println("Failed to create directory!");
			}
		}
		if(mode==1){
			File file2 = new File(depthDataDirectory);
			if (!file2.exists()) {
				if (file2.mkdir()) {
					System.out.println(depthDataDirectory + "     Directory is created!");
				} else {
					System.out.println("Failed to create directory!");
				}
			}
		}
	}


	public void draw() {
		background(0);
		PImage currentDepthFrame = kinect2.getDepthImage();
		PImage currentVideoFrame =kinect2.getVideoImage();
		if(mode==1){
			//get depth data
			int[] rawDepth = kinect2.getRawDepth();
			//record depth data
			try
			{
			    PrintWriter pr = new PrintWriter(depthDataDirectory+depthByteFilename+"_"+frameCount);    

			    for (int i=0; i<rawDepth.length ; i++)
			    {
			        pr.println(rawDepth[i]);
			    }
			    pr.close();
			}
			catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}//end of if
		
		
		
		
//		PImage currentIRFrame =kinect2.getIrImage();
//		PImage currentRegistredFrame = kinect2.getRegisteredImage();
		
		if(mode == 2)
			workWithCurrentGrayFrame(currentDepthFrame,depthFrames,kinect2.depthWidth,kinect2.depthHeight );
		workWithCurrentGrayFrame(currentVideoFrame,videoFramesGray,kinect2.colorWidth,kinect2.colorHeight );
		workWithCurrentColorFrame(currentVideoFrame,videoFramesRGB,kinect2.colorWidth,kinect2.colorHeight );
		
		image(currentDepthFrame, 0, 0);
		//image(currentVideoFrame, kinect2.depthWidth, 0, kinect2.colorWidth*0.25f, kinect2.colorHeight*0.25f);
		
		fill(255);
		text("Framerate: " + (int)(frameRate), 10, 515);
		frameCount++;
	}
	
	public void dispose(){
		System.out.println("Stopping the video capture");
		  try {
			  if(mode==2)
				  saveVideoFromFrames(depthFrames, depthFilename);
			  saveVideoFromFrames(videoFramesGray, videoFilenameGray);
			  saveVideoFromFrames(videoFramesRGB, videoFilenameRGB);
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
	public void workWithCurrentGrayFrame(PImage currentFrame,ArrayList<Frame> frames, int currentWidth, int currentHeight){
		
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
		  
		  
		  BufferedImage curretBufferedFrame = Mat2BufferedImage(m2);
		  Java2DFrameConverter fconv = new Java2DFrameConverter();
		  Frame thisFrame = fconv.getFrame(curretBufferedFrame);
		  frames.add(thisFrame);
	}//endof workWithCurrentFrame
	
	/**
	 * 
	 * @param currentFrame
	 * @param frames
	 * @param filename
	 * @param currentWidth
	 * @param currentHeight
	 */
	public void workWithCurrentColorFrame(PImage currentFrame,ArrayList<Frame> frames, int currentWidth, int currentHeight){
		
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
		  Mat m0 = new Mat(currentHeight,currentWidth, CvType.CV_8UC1);
		
//		  m0.put(0, 0, iArray);
//		  m0.dump();
		  Mat m1 = new Mat(currentHeight,currentWidth, CvType.CV_8UC4);
		 
		  // Initialize the matrix m1 with content from bArray.
		  m1.put(0, 0, bArray);
		
		  
		 // Clone m1 into another matrix m2. Now m2 is an exact copy of m1.
		  Mat m2 = m1.clone();
		 
//		  System.out.println(m2.toString());
//		  
		  
		  Mat mbgra = new Mat(currentHeight,currentWidth, CvType.CV_8UC3);
		  //extract correct data. In the case of Kinect_v2 first cell of resData is 255,
		  //therefore we need to start with second cell, i.e. data[1]. To make it BGR we need to
		  //use them in inverse order -- data[3], data[2], data[1]
		  for (int i = 0; i < currentHeight; i++)
		      for (int j = 0; j < currentWidth; j++) {
		          double[] data = m2.get(i, j);
		          double resData[] = new double[3];
		          resData[0] = data[3];
		          resData[1] = data[2];
		          resData[2] = data[1];
		          mbgra.put(i, j, resData);
		      }
		  
		
//		  
//		  Highgui.imwrite(".//images//currentFrame.jpg",mbgra);
		  
		 //first convert to frame:
		  
//		  OpenCVFrameConverter.ToMat converterToMat = new OpenCVFrameConverter.ToMat();
		  //there is no straightforward way to convert to IplImage. Instead we convert it 
		  //to BufferedImage and then to Frame
		  BufferedImage curretBufferedFrame = Mat2BufferedImage(mbgra);
		  Java2DFrameConverter fconv = new Java2DFrameConverter();
		  Frame thisFrame = fconv.getFrame(curretBufferedFrame);
		  
		  
//		  OpenCVFrameConverter.ToIplImage  converter = new OpenCVFrameConverter.ToIplImage();
//		  
		  
//		//convert to the iplImage
//		  IplImage currentIplFrame = converter.convert(thisFrame);
//		  
//		  System.out.println(currentIplFrame.toString());
		  
		  
		  frames.add(thisFrame);
		 
		  

	}//endof workWithCurrentFrame
	
	/**
	 * 
	 * @param frames
	 * @param filename
	 */
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
	public void saveVideoFromFrames(ArrayList<Frame> frames, String filename) throws Exception{
		System.out.println(frames.size());
		FrameRecorder recorder;
		OpenCVFrameConverter.ToIplImage converter = new OpenCVFrameConverter.ToIplImage();
		int width = frames.get(0).imageWidth;
		int height  = frames.get(0).imageHeight;
		
		
		
		recorder = FrameRecorder.createDefault(resDirectory+filename+".mp4", width, height);
		recorder.setFrameRate(60);
		recorder.setVideoCodec(avcodec.AV_CODEC_ID_H264); // AV_CODEC_ID_FLV1
        recorder.setVideoOption("preset", "ultrafast");
        recorder.setPixelFormat(avutil.AV_PIX_FMT_YUV420P);
		recorder.setVideoQuality(5.0);
		recorder.start();
		for(Frame convFrame:frames){
			recorder.record(convFrame);
		}
		recorder.stop();
	}
	
	
	
//	/**
//	 * 
//	 * @param frames
//	 * @throws Exception
//	 */
//	public void saveVideo(ArrayList<IplImage> frames, String filename) throws Exception{
//		System.out.println(frames.size());
//		FrameRecorder recorder;
//		OpenCVFrameConverter.ToIplImage converter = new OpenCVFrameConverter.ToIplImage();
//		int width = frames.get(0).width();
//		int height  = frames.get(0).height();
//		
//		
//		recorder = FrameRecorder.createDefault(resDirectory+filename+".mp4", width, height);
//		recorder.setFrameRate(60);
//		recorder.setVideoCodec(avcodec.AV_CODEC_ID_H264); // AV_CODEC_ID_FLV1
//        recorder.setVideoOption("preset", "ultrafast");
//        recorder.setPixelFormat(avutil.AV_PIX_FMT_YUV420P);
//		recorder.setVideoQuality(5.0);
//		recorder.start();
//		for(IplImage currentIplFrame:frames){
//			//make a frame out of IplImage:
//			Frame convFrame = converter.convert(currentIplFrame);
//			recorder.record(convFrame);
//		}
//		recorder.stop();
//	}
//	
	
	/**
//	 * 
//	 * @param data
//	 * @param width
//	 * @param height
//	 * @return
//	 */
//	protected IplImage processImage(byte[] data, int width, int height) {
//	    
//
//	    // First, downsample our image and convert it into a grayscale IplImage
//	    IplImage grayImage = IplImage.create(width , height, org.bytedeco.javacpp.opencv_core.IPL_DEPTH_8U,1);
//
//	    int imageWidth = grayImage.width();
//	    int imageHeight = grayImage.height();
//	    int dataStride = width;
//	    int imageStride = grayImage.widthStep();
//	    ByteBuffer imageBuffer = grayImage.getByteBuffer();
//	    for (int y = 0; y < imageHeight; y++) {
//	        int dataLine = y * dataStride;
//	        int imageLine = y * imageStride;
//	        for (int x = 0; x < imageWidth; x++) {
//	            imageBuffer.put(imageLine + x, data[dataLine +  x]);
//	        }
//	    }
//
//	    return grayImage;
//	}
	
	
	/**
	 * Mat2BufferedImage
	 * @param Mat m
	 * @return BufferedImage
	 */
	
	public static BufferedImage Mat2BufferedImage(Mat m){
		// source: http://answers.opencv.org/question/10344/opencv-java-load-image-to-gui/
		// Fastest code
		// The output can be assigned either to a BufferedImage or to an Image

		    int type = BufferedImage.TYPE_BYTE_GRAY;
		    if ( m.channels() > 1 ) {
		        type = BufferedImage.TYPE_3BYTE_BGR;
		    }
		    int bufferSize = m.channels()*m.cols()*m.rows();
		    byte [] b = new byte[bufferSize];
		    m.get(0,0,b); // get all the pixels
		    BufferedImage image = new BufferedImage(m.cols(),m.rows(), type);
		    final byte[] targetPixels = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();
		    System.arraycopy(b, 0, targetPixels, 0, b.length);  
		    return image;

		}
	
	
}
