package com.example.lenovo.linehough;

/**
 * Created by lenovo on 2016/6/21.
 */
public class ReturnXY {
   private int[] point=new int[2*100];
    private int count=0;

    public ReturnXY()
    {System.out.println("return x,y");}

    public int[] xyMethod(int x,int y){

       if (count<100)
       { point[2*count]=x;
         point[2*count+1]=y;
           count++;
       }
        return point;
    }

    public double pointsDistance(int x1,int y1,int x2,int y2)
    {
        double d=Math.sqrt(Math.pow( (double)(x1-x2),2.0)+Math.pow( (double)(y1-y2),2.0));
        return d;
    }

}
