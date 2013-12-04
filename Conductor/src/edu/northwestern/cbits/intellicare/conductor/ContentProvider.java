package edu.northwestern.cbits.intellicare.conductor;

import java.io.EOFException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.URLConnection;
import java.net.UnknownHostException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import javax.net.ssl.SSLException;

import android.content.Context;
import android.net.Uri;
import android.os.StatFs;
import edu.northwestern.cbits.intellicare.logging.LogManager;

public class ContentProvider 
{
    private static HashMap<String, String> _cachedHashes = new HashMap<String, String>();

    private static String createHash(Context context, String string)
    {
        if (string == null)
            return null;
        
        String hash = ContentProvider._cachedHashes.get(string);
        
        if (hash != null)
            return hash;

        try
        {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] digest = md.digest(string.getBytes("UTF-8"));

            hash = (new BigInteger(1, digest)).toString(16);

            while (hash.length() < 32)
            {
                    hash = "0" + hash;
            }
        }
        catch (NoSuchAlgorithmException e)
        {
        	LogManager.getInstance(context).logException(e);
        }
        catch (UnsupportedEncodingException e)
        {
        	LogManager.getInstance(context).logException(e);
        }
        
        if (ContentProvider._cachedHashes.size() > 256)
            ContentProvider._cachedHashes.clear();
        
        ContentProvider._cachedHashes.put(string, hash);

        return hash;
    }

    private static File fetchCacheDir(Context context)
    {
    	File cache = context.getCacheDir();
    
        if (cache.exists() == false)
            cache.mkdirs();
            
        return cache;
    }

    @SuppressWarnings("deprecation")
	public static synchronized long cacheSpace(Context context)
    {
        File cache = context.getCacheDir();
        
        if (cache.exists() == false)
        	cache.mkdirs();
        
        StatFs stat = new StatFs(cache.getAbsolutePath());
        
        return ((long) stat.getBlockSize()) * ((long) stat.getAvailableBlocks());
    }

    public static synchronized void cleanCache(Context context) 
    {
        File cache = context.getCacheDir();
        
        int cacheSize = 16 * 1024 * 1024;

        if (cache.exists() == false)
            cache.mkdirs();
        
        synchronized(context.getApplicationContext())
        {
            List<File> files = new ArrayList<File>();
            
            for (File f : cache.listFiles())
            {
                files.add(f);
            }

            try
            {
                Collections.sort(files, new Comparator<File>()
                {
                    public int compare(File first, File second) 
                    {
                        if (first.lastModified() < second.lastModified())
                            return 1;
                        else if (first.lastModified() > second.lastModified())
                            return -1;

                        return first.getName().compareTo(second.getName());
                    }
                });
            }
            catch (IllegalArgumentException e)
            {
            	LogManager.getInstance(context).logException(e);
            }
            
            List<File> toRemove = new ArrayList<File>();
            
            long totalCached = 0;
            
            for (File f : files)
            {
                if (totalCached < cacheSize)
                    totalCached += f.length();
                else
                    toRemove.add(f);
            }
            
            for (File f : toRemove)
            {
                f.delete();
            }
        }
    }

    public static Uri fetchCachedUri(final Context context, final Uri uri, final Runnable next) 
    {
        if (uri == null || uri.getScheme() == null)
            return null;
        
        final String hashString = ContentProvider.createHash(context, uri.toString());
        
        final File cacheDir = ContentProvider.fetchCacheDir(context);
        final File cachedFile = new File(cacheDir, hashString);
        
        if (cachedFile.exists() && cachedFile.length() > 0)
        {
            cachedFile.setLastModified(System.currentTimeMillis());
            return Uri.fromFile(cachedFile);
        }
        
        Runnable r = new Runnable()
        {
            public void run() 
            {
                File tempFile = null;
                
                try 
                {
                    long freeSpace = ContentProvider.cacheSpace(context);
                    
                    if (freeSpace < 4096 * 4096)
                    {
                        ContentProvider.cleanCache(context);
                    }
                    
                    freeSpace = ContentProvider.cacheSpace(context);

                    if (freeSpace > 2048 * 2048)
                    {
                        URL u = new URL(uri.toString());
                        
                        URLConnection conn = u.openConnection();
                        InputStream in = conn.getInputStream();
                        
                        tempFile = File.createTempFile(hashString, "tmp", cacheDir);

                        FileOutputStream out = new FileOutputStream(tempFile);

                        byte[] buffer = new byte[8192];
                        int read = 0;
                        
                        while ((read = in.read(buffer, 0, buffer.length)) != -1)
                        {
                            out.write(buffer, 0, read);
                        }
                        
                        out.close();
                        
                        tempFile.renameTo(cachedFile);
                    }
                } 
                catch (SocketTimeoutException e)
                {

                }
                catch (SocketException e)
                {

                }
                catch (SSLException e)
                {

                }
                catch (UnknownHostException e)
                {

                }
                catch (EOFException e)
                {

                }
                catch (FileNotFoundException e) 
                {
                	LogManager.getInstance(context).logException(e);
                }
                catch (IOException e) 
                {
                	LogManager.getInstance(context).logException(e);
                }
                finally
                {
                    if (tempFile != null && tempFile.exists())
                        tempFile.delete();
                }

                if (next != null)
                	next.run();
            }
        };
        
        Thread t = new Thread(r);
        t.start();

        return null;
    }
}
