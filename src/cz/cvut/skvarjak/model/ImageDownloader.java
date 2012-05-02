package cz.cvut.skvarjak.model;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.lang.ref.SoftReference;
import java.lang.ref.WeakReference;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Handler;
import android.util.Log;
import android.widget.ImageView;

public class ImageDownloader {
	private static final String TAG = "FacebookClient.ImageDownloader";
	/*protected Hashtable<String, Bitmap> friendsImages; TODO
	protected Hashtable<String, String> positionRequested;
	protected BaseAdapter listener;
	protected int runningCount = 0;
	protected Stack<Item> queue;
	
	// 15 max async tasks at any given time.
	final static int MAX_ALLOWED_TASKS = 15;

	public ImageDownloader() {
		friendsImages = new Hashtable<String, Bitmap>();
		positionRequested = new Hashtable<String, String>();
		queue = new Stack<Item>();
	}

	public void setListener(BaseAdapter listener) {
		this.listener = listener;
		reset();
	}

	public void reset() {
		positionRequested.clear();
		runningCount = 0;
		queue.clear();
	}

	public Bitmap getImage(String uid) {
		Bitmap image = friendsImages.get(uid);
		if (image != null) {
			return image;
		}
		if (!positionRequested.containsKey(uid)) {
			positionRequested.put(uid, "");
			if (runningCount >= MAX_ALLOWED_TASKS) {
				queue.push(new Item(uid));
			} else {
				runningCount++;
				new GetProfilePicAsyncTask().execute(uid);
			}
		}
		return null;
	}
	
	/*public Bitmap getUserPic(String uid) {
	    String imageURL = "http://graph.facebook.com/"+ uid +"/picture?type=square";
	    Bitmap bitmap = null;
	    try {
	        bitmap = BitmapFactory.decodeStream((InputStream)new URL(imageURL).getContent());
	    } catch (Exception e) {
	        e.printStackTrace();
	    }
	    return bitmap;
	}* /
	
	public byte[] getPicture(String uid) {
		String imageURL = "http://graph.facebook.com/"+ uid +"/picture?type=square";
		URL url;
		ByteArrayBuffer byteArray = new ByteArrayBuffer(128);
		try {
			url = new URL(imageURL);
			URLConnection conn = url.openConnection();
			InputStream is = conn.getInputStream();
			BufferedInputStream bis = new BufferedInputStream(is, 128);
			int current = 0;
			while ((current = bis.read()) != -1) {
		        byteArray.append((byte) current);
			}
		} catch (MalformedURLException e) {
			Log.w(TAG, e.getMessage());
		} catch (IOException e) {
			Log.w(TAG, e.getMessage());
		}
		
		return byteArray.toByteArray();
	}

	public void getNextImage() {
		if (!queue.isEmpty()) {
			Item item = queue.pop();
			new GetProfilePicAsyncTask().execute(item.uid);
		}
	}
	
	public static Bitmap getBitmap(String url) {
        Bitmap bm = null;
        InputStream is = null;
        try {
        	URL aURL = new URL(url);
        	URLConnection conn = aURL.openConnection();
            is = conn.getInputStream();
            BufferedInputStream bis = new BufferedInputStream(is);
            bm = BitmapFactory.decodeStream(new FlushedInputStream(is));
            bis.close();
            is.close();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (is != null) {
            	try {
            		is.close();
            	} catch (IOException e) {
				}
            }
        }
        return bm;
    }

	private class GetProfilePicAsyncTask extends AsyncTask<Object, Void, Bitmap> {
		String uid;

		@Override
		protected Bitmap doInBackground(Object... params) {
			this.uid = (String) params[0];
			String url = "http://graph.facebook.com/"+ uid +"/picture";
			return getBitmap(url);
		}

		@Override
		protected void onPostExecute(Bitmap result) {
			runningCount--;
			if (result != null) {
				friendsImages.put(uid, result);
				listener.notifyDataSetChanged();
				getNextImage();
			}
		}
	}

	class Item {
		String uid;

		public Item(String uid) {
			this.uid = uid;
		}
	}*/
	
	/////////////////////////////////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////////////
	
	protected String PROFILE_IMAGE_URL = "http://graph.facebook.com/%s/picture?type=square";;
	
	public void downloadProfilePicture(String uid, ImageView imageView) {
		String url = String.format(PROFILE_IMAGE_URL, uid);
		
		resetPurgeTimer();
		Bitmap bitmap = getBitmapFromCache(url);

		if (bitmap == null) {
			forceDownload(url, imageView);
		} else {
			cancelPotentialDownload(url, imageView);
			imageView.setImageBitmap(bitmap);
		}
	}
	
	public void downloadPicture(String url, ImageView imageView) {
		resetPurgeTimer();
		Bitmap bitmap = getBitmapFromCache(url);

		if (bitmap == null) {
			forceDownload(url, imageView);
		} else {
			cancelPotentialDownload(url, imageView);
			imageView.setImageBitmap(bitmap);
		}
	}
	
	private final Handler purgeHandler = new Handler();
	private final Runnable purger = new Runnable() {
        public void run() {
            clearCache();
        }
    };
	
	private static final int HARD_CACHE_CAPACITY = 100;
    private static final int DELAY_BEFORE_PURGE = 10 * 1000;
	
    private final HashMap<String, Bitmap> sHardBitmapCache =
            new LinkedHashMap<String, Bitmap>(HARD_CACHE_CAPACITY / 2, 0.75f, true) {
			private static final long serialVersionUID = 1L;
			@Override
            protected boolean removeEldestEntry(LinkedHashMap.Entry<String, Bitmap> eldest) {
                if (size() > HARD_CACHE_CAPACITY) {
                    sSoftBitmapCache.put(eldest.getKey(), new SoftReference<Bitmap>(eldest.getValue()));
                    return true;
                } else {
                    return false;
                }
            }
        };
    
	private final static ConcurrentHashMap<String, SoftReference<Bitmap>> sSoftBitmapCache =
	        new ConcurrentHashMap<String, SoftReference<Bitmap>>(HARD_CACHE_CAPACITY / 2);
	
    protected Bitmap getBitmapFromCache(String url) {
        synchronized (sHardBitmapCache) {
            final Bitmap bitmap = sHardBitmapCache.get(url);
            if (bitmap != null) {
                sHardBitmapCache.remove(url);
                sHardBitmapCache.put(url, bitmap);
                return bitmap;
            }
        }

        SoftReference<Bitmap> bitmapReference = sSoftBitmapCache.get(url);
        if (bitmapReference != null) {
            final Bitmap bitmap = bitmapReference.get();
            if (bitmap != null) {
                return bitmap;
            } else {
                sSoftBitmapCache.remove(url);
            }
        }

        return null;
    }
    
    private void forceDownload(String url, ImageView imageView) {
        if (url == null) {
            imageView.setImageDrawable(null);
            return;
        }

        if (cancelPotentialDownload(url, imageView)) {
        	BitmapDownloaderTask task = new BitmapDownloaderTask(imageView);
            DownloadedDrawable downloadedDrawable = new DownloadedDrawable(task);
            imageView.setImageDrawable(downloadedDrawable);
            imageView.setMinimumHeight(156);
            task.execute(url);
        }
    }
    
    class BitmapDownloaderTask extends AsyncTask<String, Void, Bitmap> {
        private String url;
        private final WeakReference<ImageView> imageViewReference;

        public BitmapDownloaderTask(ImageView imageView) {
            imageViewReference = new WeakReference<ImageView>(imageView);
        }

        @Override
        protected Bitmap doInBackground(String... params) {
            url = params[0];
            return downloadBitmap(url);
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            if (isCancelled()) {
                bitmap = null;
            }

            addBitmapToCache(url, bitmap);

            if (imageViewReference != null) {
                ImageView imageView = imageViewReference.get();
                BitmapDownloaderTask bitmapDownloaderTask = getBitmapDownloaderTask(imageView);
                if ((this == bitmapDownloaderTask)) {
                    imageView.setImageBitmap(bitmap);
                }
            }
        }
    }
    
    private void addBitmapToCache(String url, Bitmap bitmap) {
        if (bitmap != null) {
            synchronized (sHardBitmapCache) {
                sHardBitmapCache.put(url, bitmap);
            }
        }
    }
    
    private static BitmapDownloaderTask getBitmapDownloaderTask(ImageView imageView) {
        if (imageView != null) {
            Drawable drawable = imageView.getDrawable();
            if (drawable instanceof DownloadedDrawable) {
                DownloadedDrawable downloadedDrawable = (DownloadedDrawable)drawable;
                return downloadedDrawable.getBitmapDownloaderTask();
            }
        }
        return null;
    }
    
    private static boolean cancelPotentialDownload(String url, ImageView imageView) {
        BitmapDownloaderTask bitmapDownloaderTask = getBitmapDownloaderTask(imageView);

        if (bitmapDownloaderTask != null) {
            String bitmapUrl = bitmapDownloaderTask.url;
            if ((bitmapUrl == null) || (!bitmapUrl.equals(url))) {
                bitmapDownloaderTask.cancel(true);
            } else {
                return false;
            }
        }
        return true;
    }
    
    private Bitmap downloadBitmap(String url) {
        final HttpClient client = new DefaultHttpClient();
        final HttpGet getRequest = new HttpGet(url);

        try {
            HttpResponse response = client.execute(getRequest);
            final int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode != HttpStatus.SC_OK) {
                Log.w(TAG, "Error " + statusCode +
                        " while retrieving bitmap from " + url);
                return null;
            }

            final HttpEntity entity = response.getEntity();
            if (entity != null) {
                InputStream inputStream = null;
                try {
                    inputStream = entity.getContent();
                    return BitmapFactory.decodeStream(new FlushedInputStream(inputStream));
                } finally {
                    if (inputStream != null) {
                        inputStream.close();
                    }
                    entity.consumeContent();
                }
            }
        } catch (IOException e) {
            getRequest.abort();
            Log.w(TAG, "I/O error while retrieving bitmap from " + url, e);
        } catch (IllegalStateException e) {
            getRequest.abort();
            Log.w(TAG, "Incorrect URL: " + url);
        } catch (Exception e) {
            getRequest.abort();
            Log.w(TAG, "Error while retrieving bitmap from " + url, e);
        }
        
        return null;
    }
    
    private void resetPurgeTimer() {
        purgeHandler.removeCallbacks(purger);
        purgeHandler.postDelayed(purger, DELAY_BEFORE_PURGE);
    }
    
    public void clearCache() {
        sHardBitmapCache.clear();
        sSoftBitmapCache.clear();
    }
    
    static class FlushedInputStream extends FilterInputStream {
        public FlushedInputStream(InputStream inputStream) {
            super(inputStream);
        }

        @Override
        public long skip(long n) throws IOException {
            long totalBytesSkipped = 0L;
            while (totalBytesSkipped < n) {
                long bytesSkipped = in.skip(n - totalBytesSkipped);
                if (bytesSkipped == 0L) {
                    int b = read();
                    if (b < 0) {
                        break; // we reached EOF
                    } else {
                        bytesSkipped = 1; // we read one byte
                    }
                }
                totalBytesSkipped += bytesSkipped;
            }
            return totalBytesSkipped;
        }
    }
    
    static class DownloadedDrawable extends ColorDrawable {
        private final WeakReference<BitmapDownloaderTask> bitmapDownloaderTaskReference;

        public DownloadedDrawable(BitmapDownloaderTask bitmapDownloaderTask) {
            super(Color.BLACK);
            bitmapDownloaderTaskReference =
                new WeakReference<BitmapDownloaderTask>(bitmapDownloaderTask);
        }

        public BitmapDownloaderTask getBitmapDownloaderTask() {
            return bitmapDownloaderTaskReference.get();
        }
    }
}