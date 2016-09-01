package com.example.lenovo.harris;

import android.graphics.Color;

/**
 * Created by lenovo on 2016/6/20.
 */
public class Mat {
    private int[] imgBuf;
    private int[] neighbor;
    private int[] mark;
    private int markNum1;
    private int markNum2;

    public Mat()
    {System.out.println("MatClass");}

    public boolean matswitch(int w,int h,int[] inputs)
    {
        imgBuf=new int[w*h];
        imgBuf=inputs;
        neighbor=new int[10];
        mark=new int[w*h];
        markNum1=0;

        //第一步
        for(int x=1;x<w-1;x++)
        {
            for(int y=1;y<h-1;y++)
            {
                //条件1：p必须是边界点，值为1， 8邻域内至少有1个像素点值为0
                if(imgBuf[y*w+x]== Color.WHITE) continue;
                int[] detectBlack=new int[10];
                neighbor[2]= ((imgBuf[(y-1)*w+x]& 0x00ff0000)>>16)/255;
                neighbor[3]= ((imgBuf[(y-1)*w+x+1]& 0x00ff0000)>>16)/255;
                neighbor[4]= ((imgBuf[y*w+x+1]& 0x00ff0000)>>16)/255;
                neighbor[5]= ((imgBuf[(y+1)*w+x+1]& 0x00ff0000)>>16)/255;
                neighbor[6]= ((imgBuf[(y+1)*w+x]& 0x00ff0000)>>16)/255;
                neighbor[7]= ((imgBuf[(y+1)*w+x-1]& 0x00ff0000)>>16)/255;
                neighbor[8]= ((imgBuf[(y)*w+x-1]& 0x00ff0000)>>16)/255;
                neighbor[9]= ((imgBuf[(y-1)*w+x-1]& 0x00ff0000)>>16)/255;
                for (int i=2;i<10;i++)
                {
                    if(neighbor[i]==0) detectBlack[i]++;
                }

                if (detectBlack[2]*detectBlack[3]*detectBlack[4]*detectBlack[5]*detectBlack[6]*detectBlack[7]*detectBlack[8]*detectBlack[9]!=0) continue;

                //条件2：2<=N(p）<=6
                int np=(detectBlack[2]+detectBlack[3]+detectBlack[4]+detectBlack[5]+detectBlack[6]+detectBlack[7]+detectBlack[8]+detectBlack[9]);
                if(np<2 || np>6) continue;

                //条件3：T(p）=1
                int tp=0;
                for(int i=3;i<=9;i++)
                {
                       /* if(neighbor[i]-neighbor[i-1]==Color.WHITE-Color.BLACK )*/
                    if(detectBlack[i]-detectBlack[i-1]==1 )
                        tp++;
                }
                if(detectBlack[2]-detectBlack[9]==1)
                    tp++;
                if(tp!=1) continue;

                //条件4：p2*p4*p6=0
                if(detectBlack[2]*detectBlack[4]*detectBlack[6]!=0)
                    continue;
                //条件5：p4*p6*p8=0
                if(detectBlack[4]*detectBlack[6]*detectBlack[8]!=0)
                    continue;

                //标记要被删除的点
                mark[y*w+x]=1;
                markNum1++;
            }
        }

        //将标记删除的点置为背景色
        if(markNum1>0)
        {
            for(int x=1;x<w-1;x++)
            {
                for(int y=1;y<h-1;y++)
                {
                    //删除被标记的点，即置为背景色黑色
                    if(mark[y*w+x]==1)
                    {
                        imgBuf[y*w+x]=Color.WHITE;
                    }
                }
            }
        }



        //第二步
        markNum2=0;
        for(int x=1;x<w-1;x++)
        {
            for(int y=1;y<h-1;y++)
            {
                //条件1：p必须是前景点BLACK
                if(imgBuf[y*w+x]== Color.WHITE) continue;
                int[] detectBlack=new int[10];
                neighbor[2]= ((imgBuf[(y-1)*w+x]& 0x00ff0000)>>16)/255;
                neighbor[3]= ((imgBuf[(y-1)*w+x+1]& 0x00ff0000)>>16)/255;
                neighbor[4]= ((imgBuf[y*w+x+1]& 0x00ff0000)>>16)/255;
                neighbor[5]= ((imgBuf[(y+1)*w+x+1]& 0x00ff0000)>>16)/255;
                neighbor[6]= ((imgBuf[(y+1)*w+x]& 0x00ff0000)>>16)/255;
                neighbor[7]= ((imgBuf[(y+1)*w+x-1]& 0x00ff0000)>>16)/255;
                neighbor[8]= ((imgBuf[(y)*w+x-1]& 0x00ff0000)>>16)/255;
                neighbor[9]= ((imgBuf[(y-1)*w+x-1]& 0x00ff0000)>>16)/255;
                for (int i=2;i<10;i++)
                {
                    if(neighbor[i]==0) detectBlack[i]++;
                }

                if (detectBlack[2]*detectBlack[3]*detectBlack[4]*detectBlack[5]*detectBlack[6]*detectBlack[7]*detectBlack[8]*detectBlack[9]!=0) continue;

                //条件2：2<=N(p）<=6
                int np=(detectBlack[2]+detectBlack[3]+detectBlack[4]+detectBlack[5]+detectBlack[6]+detectBlack[7]+detectBlack[8]+detectBlack[9]);
                if(np<2 || np>6) continue;

                //条件3：T(p）=1
                int tp=0;
                for(int i=3;i<=9;i++)
                {
                       /* if(neighbor[i]-neighbor[i-1]==Color.WHITE-Color.BLACK )*/
                    if(detectBlack[i]-detectBlack[i-1]==1 )
                        tp++;
                }
                if(detectBlack[2]-detectBlack[9]==1)
                    tp++;
                if(tp!=1) continue;

                //条件4：p2*p4*p8==0
                if(detectBlack[2]*detectBlack[4]*detectBlack[8]!=0)
                    continue;
                //条件5：p2*p6*p8==0
                if(detectBlack[2]*detectBlack[6]*detectBlack[8]!=0)
                    continue;

                //标记删除
                mark[y*w+x]=1;
                markNum2++;
            }
        }

        //将标记删除的点置为背景色WHITE
        if (markNum2>0)
        {
            for (int x = 1; x < w-1; x++)
            {
                for (int y = 1; y < h-1; y++)
                {
                    if (mark[y * w + x] == 1)
                    {
                        imgBuf[y * w + x] = Color.WHITE;
                    }
                }
            }
        }
        //一次周期循环后，不再出现标记删除的点时，说明已生成骨架了
        if (markNum1==0&&markNum2==0)  return false;
        else  return true;

    }


    public int[] matresult(boolean s,int w,int h)
    {
        while (s)
        {
            s= matswitch(w, h, imgBuf);
        }
        return imgBuf;
    }

}
