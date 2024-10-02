import java.io.*;
import java.util.concurrent.*;

public class Pigzj {
    public static void main(String[] args) throws IOException,InterruptedException,ExecutionException {
        int num_threads;
        Runtime rt = Runtime.getRuntime();
        int num_processors = rt.availableProcessors();
        if(args.length!=0 && args.length !=2){
            System.err.println("wrong number of arguments");
            System.exit(1);
        }
        else if(args.length!=0 && (!args[0].equals("-p") || Integer.parseInt(args[1])<0 || Integer.parseInt(args[1])>(4*num_processors))){
            System.err.println("invalid arguments");
            System.exit(1);
        }

        if(args.length!=0){
            num_threads = Integer.parseInt(args[1]);
        } else {
            num_threads = rt.availableProcessors();
        }

        CompressionManager cmp = new CompressionManager(num_threads);
        cmp.compress();
        
    }   
}