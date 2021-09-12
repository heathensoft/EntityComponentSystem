package ecs;
import static java.lang.System.nanoTime;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 *
 * Debugging tool
 *
 * Used to store ECS statistics and output .csv
 *
 * @author Frederik Dahl
 * 08/09/2021
 */



public class DiagnosticsOld extends ECSManager {

    private boolean running;

    private static final String DEFAULT_DIRECTORY = "ecs"+File.separator+"diagnostics";
    private File outputFile = null;


    // should have a load-factor stat
    int componentsInPlay = 0;                       // components currently on entities
    int totalComponentsInPools = 0;                 // components stored in pools
    long totalComponentsObtainedFromPools = 0L;     // components obtained from pools
    long totalComponentsAdded = 0L;                 // components added to entities
    long totalComponentsRemoved = 0L;               // components removed from entities
    long totalComponentsLost = 0L;                  // components removed and lost reference
    long totalComponentsDiscarded = 0L;             // components discarded from pools

    int componentContainerRefits = 0;
    int componentPoolRefits = 0;




    protected DiagnosticsOld() {

    }



    public int componentsInMemory() {
        return totalComponentsInPools + componentsInPlay;
    }




    public void runDiagnostic(String directoryPath) throws IOException{

        final String s = File.separator;

        if (directoryPath == null)
            directoryPath = DEFAULT_DIRECTORY;
        else  {
            directoryPath = directoryPath.replace('/',s.charAt(0));
            if (!directoryPath.endsWith(s))
                directoryPath = directoryPath.concat(s);
        }

        File directory;

        try {
            directory = new File(directoryPath);
            if (!directory.exists()) {
                if (!directory.mkdirs()){
                    throw new IOException("Failed to create directory, trying default path..");
                }
            }
        } catch (IOException e) {
            directory = new File(DEFAULT_DIRECTORY);
            if (!directory.exists()) {
                if (!directory.mkdirs()){
                    throw new IOException("Failed to create directory for default path..");
                }
            }
        }

        final StringBuilder path = new StringBuilder();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyMMddHHmm");
        path.append(directory).append("ECS-D_").append(LocalDateTime.now().format(formatter)).append(".csv");

        outputFile = new File(path.toString());

        new Thread(new Runnable() {


            @Override
            public void run() {

                running = true;

                double startTime = timeSeconds();
                double endTime;
                double deltaTime = 0.0d;
                double accumulator = 0.0d;

                final int timeUnit = 5; // 5 seconds
                final int batchSize = 50; // 50 csv entries
                long tick = 0; // current time unit

                while (running) {
                    accumulator += deltaTime;
                    if (accumulator > timeUnit) {
                        accumulator -= timeUnit;



                    }
                    endTime = timeSeconds();
                    deltaTime = endTime - startTime;
                    startTime = endTime;
                }

                // print out the rest

            }

            private double timeSeconds() {
                return nanoTime() / 1_000_000_000.0d;
            }

        }).start();
    }



    @Override
    protected void terminate() {

    }


}
