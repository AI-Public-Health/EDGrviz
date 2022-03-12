package edu.pitt.isp.sverchkov.edgr;

import edu.pitt.isp.sverchkov.bn.BayesNet;
import edu.pitt.isp.sverchkov.exec.ArgParser;
import edu.pitt.isp.sverchkov.smile.SMILEBayesNet;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 *
 * @author yus24
 * @param <Variable>
 * @param <Value>
 */
public class EDGr2<Variable,Value> {

    /**
     * @param args the command line arguments
     * @throws java.io.IOException
     * @throws java.lang.InterruptedException
     * @throws java.util.concurrent.ExecutionException
     */
    public static void main(String[] args) throws IOException, InterruptedException, ExecutionException {
        ArgParser parser = new ArgParser(args);
        ArgParser.Parcel<File>
                netFile = parser.file("net"),
                outFolder = parser.file("out");
        ArgParser.Parcel<String>
                contrast = parser.string("contrast");
        parser.fill();

        // Read ProbNet
        SMILEBayesNet bayesNet = new SMILEBayesNet( netFile.get(), false );
        //bayesNet.setInferenceAlgorithm(SMILEBayesNet.Algorithm.IMPORTANCE);

        EDGr2<String,String> edgar = new EDGr2<>( bayesNet, contrast.get() );

        edgar.setOutFolder( outFolder.get() );

        // Set output folder for edgar
        edgar.run();
    }

    private File outFolder = null;
    private final EDGrLogic<Variable,Value> edgar;

    public EDGr2( BayesNet<Variable,Value> net, Variable contrast ){
        edgar = new EDGrLogic( net, contrast );
    }

    public void setOutFolder(File f) {
        outFolder = f;
    }

    public void run() throws InterruptedException, ExecutionException {

        final int nps = Runtime.getRuntime().availableProcessors();
        System.out.println("Using "+nps+" threads.");
        ExecutorService exec = Executors.newFixedThreadPool( nps );

        List<Callable<Object>> tasks = new LinkedList<>();
        for( Variable node : edgar.network )
            if( edgar.doWeRun( node ) )
                //( new NodeWiseEDGr<Variable,Value>( edgar, node, new File( outFolder, node.toString()+".txt" ) ) ).run();
                //exec.submit( new NodeWiseEDGr<Variable,Value>( edgar, node, new File( outFolder, node.toString()+".txt" ) ) );
                tasks.add( Executors.callable( new NodeWiseEDGr<Variable,Value>( edgar, node, new File( outFolder, node.toString()+".txt" ) ) ) );

        try {
            if (!tasks.isEmpty()){
                System.out.println( tasks.size() );
                outFolder.mkdirs();
                for (Future<Object> f : exec.invokeAll(tasks)) f.get();
            }
        } finally {
            exec.shutdown();
        }
    }
}
