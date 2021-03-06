package org.sinfile.parsers.v4;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.GZIPInputStream;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.rauschig.jarchivelib.IOUtils;
import com.Ostermiller.util.CircularByteBuffer;


public class SinParser {

	private File sinfile;
	
	static final Logger logger = LogManager.getLogger(SinParser.class);
	Map databuffer = new HashMap<String, CircularByteBuffer>();
	
	public SinParser(File f) throws Exception {
		this.sinfile=f;
		if (!isTared() && !isGZipped() ) throw new Exception("Not a sin file");
	}

	public boolean isTared() {
		try {
			TarArchiveInputStream tarIn = new TarArchiveInputStream(new FileInputStream(sinfile));
			try {
				while ((tarIn.getNextTarEntry()) != null) {
					break;
				}
				tarIn.close();
				return true;
			} catch (IOException ioe) {
				try { tarIn.close(); } catch (Exception e) {}
				return false;
			}
		} catch (FileNotFoundException fne) {
			return false;
		}
	}
	
	public boolean isGZipped() {
		try {
		InputStream in = new FileInputStream(sinfile);
		  if (!in.markSupported()) {
		   in = new BufferedInputStream(in);
		  }
		  in.mark(2);
		  int magic = 0;
		  try {
		   magic = in.read() & 0xff | ((in.read() << 8) & 0xff00);
		   in.close();
		  } catch (IOException ioe) {
			  try {
				  in.close();
			  } catch (Exception e) {}
			  return false;
		  }
		  return magic == GZIPInputStream.GZIP_MAGIC;
		} catch (FileNotFoundException fne) {
			return false;
		}
	}
	
	public byte[] getHeader() {
		try {
			TarArchiveEntry entry=null;
			TarArchiveInputStream tarIn=null;
			if (isGZipped())
				tarIn = new TarArchiveInputStream(new GzipCompressorInputStream(new FileInputStream(sinfile)));
			else
				tarIn = new TarArchiveInputStream(new FileInputStream(sinfile));
			ByteArrayOutputStream bout = new ByteArrayOutputStream();
			while ((entry = tarIn.getNextTarEntry()) != null) {
				if (entry.getName().endsWith("cms")) {
					IOUtils.copy(tarIn, bout);
					break;
				}
			}
			tarIn.close();
			return bout.toByteArray();
		} catch (Exception e) {
			return null;
		}
	}
	
	public void dumpImage() {
		try {
			TarArchiveEntry entry=null;
			TarArchiveInputStream tarIn=null;
			if (isGZipped())
				tarIn = new TarArchiveInputStream(new GzipCompressorInputStream(new FileInputStream(sinfile)));
			else
				tarIn = new TarArchiveInputStream(new FileInputStream(sinfile));
			FileOutputStream fout = new FileOutputStream(new File("D:\\test.ext4"));
			while ((entry = tarIn.getNextTarEntry()) != null) {
				if (!entry.getName().endsWith("cms")) {
					IOUtils.copy(tarIn, fout);
				}
			}
			tarIn.close();
			fout.flush();
			fout.close();
			logger.info("Extraction finished to "+"D:\\test.ext4");
		} catch (Exception e) {}		
	}

}