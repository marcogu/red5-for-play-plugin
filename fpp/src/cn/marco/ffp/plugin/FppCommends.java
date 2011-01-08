package cn.marco.ffp.plugin;

public class FppCommends {
    public static void main(String[] args) {
        if(args.length == 0){
            System.out.println("args is null");
            return;
        }
        
        String arg0 = args[0];
        if("gen".equals(arg0)){
            gen();
        }else if("regen".equals(arg0)){
            regen();
        }else{
            System.out.println("not known: " + arg0);
        }
    }
    
    public static void gen(){
        System.out.println("this is method gen!!");
    }
    
    public static void regen(){
        System.out.println("this is method regen!!");
    }
}
