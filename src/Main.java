import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;


public class Main
{    
    public static void main(String[] args) throws IOException{
        int lena = args.length;
        if(lena > 1){
            System.out.println("error");
        }else{
            BufferedReader br = new BufferedReader(new FileReader(new File(args[0])));
            String str = null;
            while((str = br.readLine()) != null){
                System.out.println(str);
            }
        }
        
    }
}
