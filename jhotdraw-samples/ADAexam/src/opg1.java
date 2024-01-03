import java.sql.SQLOutput;

public class opg1 {




    public static void main(String[] args) {
        System.out.println(sumDeleligMedTreOgOtte(26));


    }


     static int sumDeleligMedTreOgOtte(int N){
        if(N<=0){
            return 0;
        }else if(N % 3 ==0 || N % 8==0){
            return N+sumDeleligMedTreOgOtte(N-1);
        }else{
            return sumDeleligMedTreOgOtte(N-1);
        }
    }
}
