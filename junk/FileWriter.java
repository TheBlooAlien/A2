import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.*;
import java.io.*;

public class FileWriter {
    static String timeHold;
    static float runTime;

    FileWriter(String fileName) {
        try {
            File file = new File(fileName);
            FileWriter writer = new FileWriter(fileName);

            writer.write(runTime+"\n");
            writer.close();
            timeHold = "";
            
        } catch (IOException e) {
            System.out.println("Error: File unsuccessfully created/written to.");
            e.printStackTrace();
        }
    }
}