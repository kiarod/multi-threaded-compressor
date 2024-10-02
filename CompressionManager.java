import java.io.*;
import java.util.zip.*;
import java.util.*;
import java.util.concurrent.*;

class CompressionManager {
  public final static int BLOCK_SIZE = 131072;
  public final static int DICT_SIZE = 32768;
  private final static int GZIP_MAGIC = 0x8b1f;
  private final static int TRAILER_SIZE = 8;

  public ByteArrayOutputStream outStream;
  private CRC32 crc = new CRC32();
  private int num_threads;

  public CompressionManager(int num_threads) {
    this.num_threads = num_threads;
    this.outStream = new ByteArrayOutputStream();
  }

  private void writeHeader() throws IOException {
    outStream.write(new byte[] {
        (byte)GZIP_MAGIC,        // Magic number (short)
        (byte)(GZIP_MAGIC >> 8), // Magic number (short)
        Deflater.DEFLATED,       // Compression method (CM)
        0,                       // Flags (FLG)
        0,                       // Modification time MTIME (int)
        0,                       // Modification time MTIME (int)
        0,                       // Modification time MTIME (int)
        0,                       // Modification time MTIME (int)Sfil
        0,                       // Extra flags (XFLG)
        0                        // Operating system (OS)
    });
  }
  /*
   * Writes GZIP member trailer to a byte array, starting at a given
   * offset.
   */
  private void writeTrailer(long totalBytes, byte[] buf, int offset)
      throws IOException {
    writeInt((int)crc.getValue(), buf, offset); // CRC-32 of uncompr. data
    writeInt((int)totalBytes, buf, offset + 4); // Number of uncompr. bytes
  }
  /*
   * Writes integer in Intel byte order to a byte array, starting at a
   * given offset.
   */
  private void writeInt(int i, byte[] buf, int offset) throws IOException {
    writeShort(i & 0xffff, buf, offset);
    writeShort((i >> 16) & 0xffff, buf, offset + 2);
  }
  /*
   * Writes short integer in Intel byte order to a byte array, starting
   * at a given offset
   */
  private void writeShort(int s, byte[] buf, int offset) throws IOException {
    buf[offset] = (byte)(s & 0xff);
    buf[offset + 1] = (byte)((s >> 8) & 0xff);
  }

  public void compress() throws IOException, InterruptedException, ExecutionException {
    this.writeHeader();//write header 
    this.crc.reset();  //reset crc 

    //create thread manager and Future list to track results 
    ExecutorService thread_manager = Executors.newFixedThreadPool(num_threads);
    ArrayList<Future<byte[]>> results = new ArrayList<Future<byte[]>>(); 

    //initialize vars 
    long totalBytesRead = 0;
    boolean hasDict = false;
    boolean final_block = false;
    int nBytes; 
    byte[] dictBuf = new byte[DICT_SIZE]; //dict needs to survive loop iterations
    //ArrayList<byte[]> dictBufList = new ArrayList<byte[]>();

    byte[] nextBlockBuf = new byte[BLOCK_SIZE];
    int nextNBytes = System.in.read(nextBlockBuf);

    while(0<nextNBytes){
      byte[] blockBuf = new byte[BLOCK_SIZE];
      nBytes = nextNBytes;
      System.arraycopy(nextBlockBuf, 0, blockBuf, 0, nextNBytes);
      totalBytesRead += nBytes;
      crc.update(blockBuf, 0, nBytes);

      nextNBytes = System.in.read(nextBlockBuf);
      if(nextNBytes < 0){
        final_block = true;
      }
      Future<byte[]> pending_buf = thread_manager.submit(new CompressionWorker(blockBuf, nBytes, dictBuf, hasDict, final_block));
      results.add(pending_buf);
      
      if (DICT_SIZE <= nBytes) {
        System.arraycopy(blockBuf, nBytes - DICT_SIZE, dictBuf, 0, DICT_SIZE);
        hasDict = true;
      } else {
        hasDict = false;
      }
    }

    thread_manager.shutdown();

    for (Future<byte[]> b: results){
      outStream.write(b.get());
    }

    /* Finally, write the trailer and then write to STDOUT */
    byte[] trailerBuf = new byte[TRAILER_SIZE];
    writeTrailer(totalBytesRead, trailerBuf, 0);
    outStream.write(trailerBuf);
    outStream.writeTo(System.out);
  }

}
