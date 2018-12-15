package com.github.kdvolder.mandelbrot;

import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.concurrent.atomic.AtomicBoolean;

import io.humble.video.Codec;
import io.humble.video.Encoder;
import io.humble.video.MediaPacket;
import io.humble.video.MediaPicture;
import io.humble.video.Muxer;
import io.humble.video.MuxerFormat;
import io.humble.video.PixelFormat;
import io.humble.video.PixelFormat.Type;
import io.humble.video.Rational;
import io.humble.video.awt.MediaPictureConverter;
import io.humble.video.awt.MediaPictureConverterFactory;

public class VideoFileWriter implements AutoCloseable {

	private String formatName = "mp4";
	private final Dimension bounds;
	private final Muxer muxer;
	private final MuxerFormat format;
	private final Codec codec;
	private final Encoder encoder;
//	private Rational frameRate;
	private MediaPicture picture;
	private MediaPacket mediaPacket;
	private AtomicBoolean closed = new AtomicBoolean(false);
	private BufferedImage conversionBuffer;
	private MediaPictureConverter converter;
	private int frameCounter = 0;
	private File file;

	public VideoFileWriter(File file, int width, int heigth, int framesPerSecond) throws Exception {
		this.file = file;
		bounds = new Dimension(width, heigth);
//		System.out.println("frameRate = " + frameRate +" ["+frameRate.getDouble()+ "]");
		muxer = Muxer.make(file.getAbsolutePath(), null, formatName);
		format = muxer.getFormat();
		codec = Codec.findEncodingCodec(format.getDefaultVideoCodecId());
		encoder = Encoder.make(codec);
		encoder.setWidth(width);
		encoder.setHeight(heigth);
		Type pixelFormat = PixelFormat.Type.PIX_FMT_YUV420P;
		encoder.setPixelFormat(pixelFormat);
		encoder.setTimeBase(Rational.make(1, framesPerSecond));
		if (format.getFlag(MuxerFormat.Flag.GLOBAL_HEADER)) {
			encoder.setFlag(Encoder.Flag.FLAG_GLOBAL_HEADER, true);
		}
		encoder.open(null, null);
		muxer.addNewStream(encoder);
		muxer.open(null, null);

		picture = MediaPicture.make(
				encoder.getWidth(),
				encoder.getHeight(),
				pixelFormat);
		picture.setTimeBase(Rational.make(1, framesPerSecond));
		mediaPacket = MediaPacket.make();
	}

	public void addFrame(BufferedImage image) {
		image = convertToType(image, BufferedImage.TYPE_3BYTE_BGR);
		if (converter == null) {
			converter = MediaPictureConverterFactory.createConverter(image, picture);
		}
		converter.toPicture(picture, image, nextTimestamp());

		do {
			encoder.encode(mediaPacket, picture);
			if (mediaPacket.isComplete())
				muxer.write(mediaPacket, false);
		} while (mediaPacket.isComplete());
	}

	private long nextTimestamp() {
		return frameCounter++;
	}

	@Override
	public void close() {
		if (!closed.getAndSet(true)) {
			do {
				encoder.encode(mediaPacket, null);
				if (mediaPacket.isComplete())
					muxer.write(mediaPacket,  false);
			} while (mediaPacket.isComplete());
			muxer.close();
		}
	}

	private BufferedImage convertToType(BufferedImage sourceImage, int targetType) {
		if (sourceImage.getType() == targetType) {
			return sourceImage;
		} else {
			if (conversionBuffer==null) {
				conversionBuffer = new BufferedImage(sourceImage.getWidth(),
						sourceImage.getHeight(), targetType);
			}
			conversionBuffer.getGraphics().drawImage(sourceImage, 0, 0, null);
			return conversionBuffer;
		}
	}

	public void writeCompanionTextFile(double centerX, double centerY) throws IOException {
		String name = file.getName();
		if (name.endsWith(".mp4")) {
			name = name.substring(0, name.length() - ".mp4".length()) + ".txt";
		}
		try (PrintWriter writer = new PrintWriter(new File(file.getParent(), name))) {
			writer.println("centerX = "+centerX);
			writer.println("centerY = "+centerY);
		}
	}
}
