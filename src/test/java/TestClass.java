import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * ClassName TestClass
 * Description
 * Author Ymkal
 * Date  1/25/2021
 */
public class TestClass {
    private static List<Integer> m;
    private static List<Integer> a;
    private final static Integer L = 12;
    private static TestClass instance;
    private TestClass(){
        Init();
        //System.out.println(m);
        //System.out.println(a);
    }

    public void change_final_list(final List<Integer> source){
        for(Integer i : source){
            i = 4;
            System.out.println(i);
        }
    }

    private void Init(){
        Random r = new Random();
        m = new ArrayList<>();
        a = new ArrayList<>();
        for (int i = 0; i < L; i++) {
            m.add(r.nextInt(20) + 1);
            a.add(r.nextInt(20) + 1);
        }
    }

    public static TestClass getInstance(){
        if(instance == null){
            synchronized (TestClass.class){
                if(instance == null){
                    instance = new TestClass();
                }
            }
        }
        return instance;
    }

    public static void main(String[] args) {
        TestClass c = TestClass.getInstance();
        List<Integer> l = new ArrayList<Integer>(){{add(0);add(1);}};
        c.change_final_list(l);
        System.out.println(l);
    }
}
