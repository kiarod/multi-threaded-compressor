import java.io.*;
import java.util.zip.*;
import java.io.*;
import java.util.concurrent.*;

class CompressionWorker implements Callable<byte[]> {
    public final static int BLOCK_SIZE = 131072;
    boolean final_block;
    byte[] blockBuf;
    byte[] cmpBlockBuf = new byte[BLOCK_SIZE * 2];
    Deflater compressor = new Deflater(Deflater.DEFAULT_COMPRESSION, true);

    public CompressionWorker(byte[] blockBuf, int nBytes, byte[] dictBuf, boolean hasDict, boolean final_block){
        this.blockBuf = blockBuf;
        this.final_block = final_block;
        compressor.reset();
        compressor.setInput(blockBuf, 0, nBytes);
        if (hasDict) {
            compressor.setDictionary(dictBuf);
          }   
    }

    public byte[] call() throws Exception, IOException{
        return compress(blockBuf);
         
    }

    public byte[] compress(byte[] buf) throws IOException{
        if(!final_block){
            int deflatedBytes = compressor.deflate(
                    cmpBlockBuf, 0, cmpBlockBuf.length, Deflater.FULL_FLUSH);
            byte[] processed_buf = new byte[deflatedBytes];
            System.arraycopy(cmpBlockBuf, 0, processed_buf, 0, deflatedBytes);
            compressor.end();
            return processed_buf;
        } else {
            compressor.finish();
            while (!compressor.finished()) {
                int deflatedBytes = compressor.deflate(
                    cmpBlockBuf, 0, cmpBlockBuf.length, Deflater.FULL_FLUSH);
                if (deflatedBytes > 0) {
                    byte[] processed_buf = new byte[deflatedBytes];
                    System.arraycopy(cmpBlockBuf, 0, processed_buf, 0, deflatedBytes);
                    compressor.end();
                    return processed_buf;
                }
            }
            return null;
        }
    }
}