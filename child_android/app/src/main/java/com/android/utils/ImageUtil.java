package com.android.utils;

import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.media.ExifInterface;
import android.media.ThumbnailUtils;
import android.text.TextUtils;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * 图片工具类
 */
public class ImageUtil {

	public static final String CACHE_DIR = "cache";
	public static final String IMAGES_DIR = "images";
	public static final String FILES_DIR = "files";
	public static final String MEDIA_DIR = "medias";
	public static final String DB_DIR = "db";

	/**
	 * 通过名称获取文件
	 * @param name
	 * @return
	 */
	public static File getDir(String name) {
		String sdState = android.os.Environment.getExternalStorageState();
		if (sdState.equals(android.os.Environment.MEDIA_MOUNTED)) {
			File file = new File("/sdcard/" + CS.APP_TAG + "/" + name);
			if (false == file.exists()) {
				file.mkdirs();
			}
			return file;
		} else {
			return SPUtils.INSTANCE.getCacheDir();
		}
	}

	/**
	 * 获取文件
	 * @param dirName
	 * @param name
	 * @return
	 */
	public static File getFileInDir(String dirName, String name) {
		return new File(getDir(dirName), name);
	}

	public static Bitmap getCachePhotoById(int userId) {
		File headPhoto = new File(getDir(IMAGES_DIR), userId + ".png");// TODO
																		// NO
																		// SUFFIX
		if (headPhoto.exists()) {
			BitmapFactory.Options options = new BitmapFactory.Options();
			return BitmapFactory.decodeFile(headPhoto.getAbsolutePath(),
					options);
		}
		return null;
	}

	private static final float SIDE_MAX = 140.0F;

	public static void saveLocalChatImage(Bitmap raw, String msgId) {
		saveToLocal(msgId + ImageSuffix.RAW.getValue(), raw);
		Bitmap thumbnail = scaleImage(raw);
		saveToLocal(msgId + ImageSuffix.THUMBNAIL.getValue(), thumbnail);
	}

	public enum ImageSuffix {
		RAW(".jpg"), THUMBNAIL("_s.jpg");
		private final String value;

		ImageSuffix(String value) {
			this.value = value;
		}

		public String getValue() {
			return value;
		}
	}

	public static Bitmap getLocalChatImageBy(String msgId, ImageSuffix suffix) {
		File file = new File(getDir(IMAGES_DIR), msgId + suffix.getValue());
		if (file.exists()) {
			BitmapFactory.Options options = new BitmapFactory.Options();
			return BitmapFactory.decodeFile(file.getAbsolutePath(), options);
		}
		return null;
	}

	public static byte[] getBytesByMsgId(String msgId) {
		File file = new File(getDir(IMAGES_DIR), msgId + "_s.jpg");
		if (file.exists()) {
			try {
				return FileUtils.readFileToByteArray(file);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return null;
	}

	public static byte[] getBytesByFilePath(String path) {
		File file = new File(path);
		if (file.exists()) {
			try {
				return FileUtils.readFileToByteArray(file);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return null;
	}

	// 缩小,dst_w目标文件宽

	public static Bitmap scaleImage(Bitmap bitmap) {
		int src_w = bitmap.getWidth();
		int src_h = bitmap.getHeight();

		float scale_w = 1.0f;
		float scale_h = 1.0f;

		if (src_w > SIDE_MAX) {
			scale_w = SIDE_MAX / src_w;
		}

		if (src_h > SIDE_MAX) {
			scale_h = SIDE_MAX / src_h;
		}

		float sclae = scale_w > scale_h ? scale_h : scale_w;

		Matrix matrix = new Matrix();
		matrix.postScale(sclae, sclae);

		return Bitmap.createBitmap(bitmap, 0, 0, src_w, src_h, matrix, true);
	}

	// 得到圆角图片,圆角定为10pix
	@Deprecated
	public static Bitmap toRoundCorner(Bitmap bitmap) {
		int side = bitmap.getWidth() > bitmap.getHeight() ? bitmap.getHeight()
				: bitmap.getWidth();
		Bitmap output = Bitmap.createBitmap(side, side, Config.ARGB_8888);
		Canvas canvas = new Canvas(output);

		final int color = 0xFFFFFFFF;
		final Paint paint = new Paint();
		final Rect rect = new Rect(0, 0, side, side);
		// final RectF rectF = new RectF(rect);
		// final float roundPx = 0;

		paint.setAntiAlias(true);
		canvas.drawARGB(0, 0, 0, 0);
		paint.setColor(color);
		canvas.drawRect(rect, paint);
		// canvas.drawRoundRect(rectF, roundPx, roundPx, paint);

		paint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));
		canvas.drawBitmap(bitmap, rect, rect, paint);

		return output;
	}

	public static File createImgByInputStream(String fileName,
			InputStream stream) {
		File imgFile = new File(getDir(IMAGES_DIR), fileName);
		if (imgFile.exists() && imgFile.length() > 0) {
			return imgFile;
		}
		try {
			OutputStream out = new FileOutputStream(imgFile);
			int read = 0;
			byte[] bytes = new byte[1024];
			while ((read = stream.read(bytes)) != -1) {
				out.write(bytes, 0, read);
			}
			stream.close();
			out.flush();
			out.close();
		} catch (Exception e) {
			e.printStackTrace();
			ALog.i(e.getMessage());
		}
		return imgFile;
	}

	public static File saveLocalImageByStream(InputStream imageStream,
			String fileBaseName) {
		File sourceFile = createImgByInputStream(
				fileBaseName + ImageSuffix.RAW.getValue(), imageStream);
		Bitmap bitmap = getMaxSizeBitmap(sourceFile);
		if (null != bitmap) {
			Bitmap thumbnail = scaleImage(bitmap);
			saveToLocal(fileBaseName + ImageSuffix.THUMBNAIL.getValue(),
					thumbnail);
			if (thumbnail.isRecycled() == false) {
				thumbnail.recycle();
			}
			if (bitmap.isRecycled() == false) {
				bitmap.recycle();
			}
		}
		return sourceFile;
	}

	public static Bitmap getMaxSizeBitmap(File f) {
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		// 获取这个图片的宽和高
		Bitmap bitmap = BitmapFactory.decodeFile(f.getAbsolutePath(), options); // 此时返回bm为空
		// 计算缩放比
		int be = (int) (options.outHeight / (float) MAX_HEIGHT_BIG);
		if (be <= 0)
			be = 1;
		options.inSampleSize = be;
		// 重新读入图片，注意这次要把options.inJustDecodeBounds 设为 false哦
		options.inJustDecodeBounds = false;
		try {
			bitmap = BitmapFactory.decodeFile(f.getAbsolutePath(), options);
		} catch (OutOfMemoryError e) {
		}
		return bitmap;
	}

	public static int computeSampleSize(BitmapFactory.Options options,
			int minSideLength, int maxNumOfPixels) {
		int initialSize = computeInitialSampleSize(options, minSideLength,
				maxNumOfPixels);
		int roundedSize;
		if (initialSize <= 8) {
			roundedSize = 1;
			while (roundedSize < initialSize) {
				roundedSize <<= 1;
			}
		} else {
			roundedSize = (initialSize + 7) / 8 * 8;
		}
		return roundedSize;
	}

	private static int computeInitialSampleSize(BitmapFactory.Options options,
			int minSideLength, int maxNumOfPixels) {
		double w = options.outWidth;
		double h = options.outHeight;

		int lowerBound = (maxNumOfPixels == -1) ? 1 : (int) Math.ceil(Math
				.sqrt(w * h / maxNumOfPixels));
		int upperBound = (minSideLength == -1) ? 128 : (int) Math.min(
				Math.floor(w / minSideLength), Math.floor(h / minSideLength));
		if (upperBound < lowerBound) {
			// return the larger one when there is no overlapping zone.
			return lowerBound;
		}
		if ((maxNumOfPixels == -1) && (minSideLength == -1)) {
			return 1;
		} else if (minSideLength == -1) {
			return lowerBound;
		} else {
			return upperBound;
		}
	}

	// TODO 是否判断存储卡剩余空间大小
	public static File saveToLocal(String fileName, Bitmap bitmap) {
		return saveToLocal(ImageUtil.IMAGES_DIR, fileName, bitmap);
	}
	
	public static File saveToLocal(String dir, String fileName, Bitmap bitmap) {
		File file = new File(getDir(dir), fileName);
		try {
			if (!file.exists()) {
				file.createNewFile();
			}
			OutputStream outStream = new FileOutputStream(file);
			if (FilenameUtils.getExtension(fileName).equalsIgnoreCase("png")) {
				bitmap.compress(Bitmap.CompressFormat.PNG, 100, outStream);
			} else {
				bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outStream);
			}
			outStream.flush();
			outStream.close();
			System.out.println("Image saved tosd");
			return file;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public static Bitmap getBitmapByPath(String path) {
		File file = new File(path);
		try {
			if (file.exists()) {
				BitmapFactory.Options options = new BitmapFactory.Options();
				options.inJustDecodeBounds = true;
				return BitmapFactory.decodeFile(file.getAbsolutePath(), options);
			}
			return null;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public static int MAX_HEIGHT_BIG = 800;
	public static int MAX_WIDTH_BIG = 480;

	public static int THUMBNAIL_WIDTH = 100;
	public static int THUMBNAIL_HEIGHT = 100;

	public static Bitmap getMaxSizeBitmap(File f, int sampleSize) {
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		Bitmap bitmap = BitmapFactory.decodeFile(f.getAbsolutePath(), options); // 此时返回bm为空

		options.inJustDecodeBounds = false;
		// 计算缩放比
		if (0 != sampleSize) {
			sampleSize = 3;
			options.inSampleSize = sampleSize;
		} else {
			int be = (((int) (options.outHeight / (float) ImageUtil.MAX_HEIGHT_BIG)) + ((int) (options.outWidth / (float) ImageUtil.MAX_WIDTH_BIG))) / 2 + 1;
			if (be <= 0)
				be = 1;
			options.inSampleSize = be;
		}
		bitmap = BitmapFactory.decodeFile(f.getAbsolutePath(), options);
		return bitmap;
	}

	public static Bitmap getThumbnailSizeBitmap(File f) {
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		Bitmap bitmap = BitmapFactory.decodeFile(f.getAbsolutePath(), options); // 此时返回bm为空

		options.inJustDecodeBounds = false;
		// 计算缩放比
		int be = (((int) (options.outHeight / (float) ImageUtil.THUMBNAIL_HEIGHT)) + ((int) (options.outWidth / (float) ImageUtil.THUMBNAIL_WIDTH))) / 2 + 1;
		if (be <= 0)
			be = 1;
		options.inSampleSize = be;
		bitmap = BitmapFactory.decodeFile(f.getAbsolutePath(), options);
		return bitmap;
	}

	public static Bitmap getVideoThumbnail(String videoPath, int width, int height, int kind) {
		Bitmap bitmap = null;
		// 获取视频的缩略图
		bitmap = ThumbnailUtils.createVideoThumbnail(videoPath, kind);
		System.out.println("w"+bitmap.getWidth());
		System.out.println("h"+bitmap.getHeight());
		bitmap = ThumbnailUtils.extractThumbnail(bitmap, width, height, ThumbnailUtils.OPTIONS_RECYCLE_INPUT);
		return bitmap;
	}


	// public static Bitmap loadImageFromLocal(String filePath) {
	// if (TextUtils.isEmpty(filePath)) {
	// return null;
	// }
	// final File file = new File(filePath);
	// if (file.exists()) {
	// InputStream stream = null;
	// try {
	// stream = new FileInputStream(file);
	// return BitmapFactory.decodeStream(stream, null, null);
	// } catch (FileNotFoundException e) {
	// } catch (OutOfMemoryError e) {
	// } finally {
	// AppIOUtils.closeStream(stream);
	// }
	// }
	// return null;
	// }

	public static Bitmap loadImageFromLocal(String filePath) {
		if (TextUtils.isEmpty(filePath)) {
			return null;
		}
		final File file = new File(filePath);
		if (file.exists()) {
			try {
				BitmapFactory.Options options = new BitmapFactory.Options();
				options.inJustDecodeBounds = true;
				Bitmap bm = BitmapFactory.decodeFile(filePath, options);
				options.inJustDecodeBounds = false;
				// 计算缩放比
				int be = (((int) (options.outHeight / 1000f)) + ((int) (options.outWidth / 1000f))) / 2;
				if (be <= 0)
					be = 1;
				options.inSampleSize = be;
				bm = BitmapFactory.decodeFile(filePath, null);
				return bm;
			} catch (OutOfMemoryError e) {
				System.gc();
			}
		}
		return null;
	}

	public static Bitmap loadImageFromLocal2Serv(String filePath) {
		if (TextUtils.isEmpty(filePath)) {
			return null;
		}
		final File file = new File(filePath);
		if (file.exists()) {
			InputStream stream = null;
			try {
				BitmapFactory.Options options = new BitmapFactory.Options();
				options.inJustDecodeBounds = true;
				Bitmap bm = BitmapFactory.decodeFile(filePath, options);
				options.inJustDecodeBounds = false;
				// 计算缩放比
				int be = (((int) (options.outHeight
						/ (float) ImageUtil.MAX_HEIGHT_BIG * 1.5)) + ((int) (options.outWidth / (float) ImageUtil.MAX_HEIGHT_BIG))) / 2 + 1;
				if (be <= 0)
					be = 1;
				options.inSampleSize = be;
				bm = BitmapFactory.decodeFile(filePath, options);
				return bm;
			} catch (OutOfMemoryError e) {
				System.gc();
			} finally {
				try {
					if (stream != null) {
						stream.close();
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return null;
	}

	public static int getBitmapDegree(String path) {
		int degree = 0;
		try {
			// 从指定路径下读取图片，并获取其EXIF信息
			ExifInterface exifInterface = new ExifInterface(path);
			// 获取图片的旋转信息
			int orientation = exifInterface.getAttributeInt(
					ExifInterface.TAG_ORIENTATION,
					ExifInterface.ORIENTATION_NORMAL);
			switch (orientation) {
			case ExifInterface.ORIENTATION_ROTATE_90:
				degree = 90;
				break;
			case ExifInterface.ORIENTATION_ROTATE_180:
				degree = 180;
				break;
			case ExifInterface.ORIENTATION_ROTATE_270:
				degree = 270;
				break;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return degree;
	}

	/**
	 * 将图片按照某个角度进行旋转
	 * 
	 * @param bm
	 *            需要旋转的图片
	 * @param degree
	 *            旋转角度
	 * @return 旋转后的图片
	 */
	public static Bitmap rotateBitmapByDegree(Bitmap bm, int degree) {
		Bitmap returnBm = null;
		// 根据旋转角度，生成旋转矩阵
		Matrix matrix = new Matrix();
		matrix.postRotate(degree);
		try {
			// 将原始图片按照旋转矩阵进行旋转，并得到新的图片
			returnBm = Bitmap.createBitmap(bm, 0, 0, bm.getWidth(),
					bm.getHeight(), matrix, true);
		} catch (OutOfMemoryError e) {
		}
		if (returnBm == null) {
			returnBm = bm;
		}
		if (bm != returnBm) {
			bm.recycle();
		}
		return returnBm;
	}

	public static Bitmap compressImage(Bitmap image) {
		if (image == null) {
			return image;
		}
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		image.compress(Bitmap.CompressFormat.JPEG, 100, baos);// 质量压缩方法，这里100表示不压缩，把压缩后的数据存放到baos中
		int options = 100;
		while (baos.toByteArray().length / 1024 > 200) { // 循环判断如果压缩后图片是否大于200KB,大于继续压缩
			baos.reset();// 重置baos即清空baos
			options -= 10;// 每次都减少10
			image.compress(Bitmap.CompressFormat.JPEG, options, baos);// 这里压缩options%，把压缩后的数据存放到baos中
		}
		ByteArrayInputStream isBm = new ByteArrayInputStream(baos.toByteArray());// 把压缩后的数据baos存放到ByteArrayInputStream中
		try {
			Bitmap bitmap = BitmapFactory.decodeStream(isBm, null, null);// 把ByteArrayInputStream数据生成图片
			return bitmap;
		} catch (OutOfMemoryError e) {
			return image;
		}
	}
	
	public static File getFile4Byte(String filePath, byte[] data) {
		File file = ImageUtil.getFileInDir(ImageUtil.CACHE_DIR, new MD5Util().getMD5ofStr(filePath));
		if (file != null && file.exists() && file.length() > 0) {
			return file;
		}
		BufferedOutputStream stream = null;
		FileOutputStream fstream = null;
		try {
			fstream = new FileOutputStream(file);
			stream = new BufferedOutputStream(fstream);
			stream.write(data);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if (stream != null) {
					stream.close();
				}
				if (null != fstream) {
					fstream.close();
				}
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		}
		return file;
	}
}