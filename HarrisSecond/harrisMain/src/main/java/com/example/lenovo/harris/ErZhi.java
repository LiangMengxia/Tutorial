package com.example.lenovo.harris;

/**
 * Created by lenovo on 2016/6/20.
 */
public class ErZhi {
    public  ErZhi()
    {System.out.println("ErZhiClass");}

    public static int[] ErZhiHua(int w,int h,int[] inputs)
    {
        int[] ray = new int[w*h];
        int[] alpha = new int[w*h];
        int[] newpixel = new int[w*h];
        for (int x = 0; x < w; x++) {
            for (int y = 0; y < h; y++) {
                alpha[y*w+x] = inputs[y*w+x] & 0xFF000000;
                int red = (inputs[y*w+x] & 0x00FF0000) >> 16;
                int green = (inputs[y*w+x] & 0x0000FF00) >> 8;
                int blue = inputs[y*w+x] & 0x000000FF;
                int gray = (int) ((float) red * 0.3 + (float) green * 0.59 + (float) blue * 0.11);
                newpixel[y*w+x] = alpha[y*w+x] | (gray << 16) | (gray << 8) | gray;
                ray[y*w+x] = gray;//将像素点灰度值放在数组ray[][]中
            }
        }
        //求出最大灰度值zmax和最小灰度值zmin
        int zmax=ray[0],zmin=ray[0];
        for (int x = 0; x < w; x++)
        {
            for (int y = 0; y < h; y++)
            {
                if (ray[y*w+x]>zmax)
                {zmax=ray[y*w+x];}
                if (ray[y*w+x]<zmin)
                {zmin=ray[y*w+x];}
            }
        }

        //获取灰度直方图
        int i, j, t, count1 = 0, count2 = 0, sum1 = 0, sum2 = 0;
        int  bp, fp;
        int[] histogram = new int[256];
        for (t =zmin; t <= zmax; t++) {
            for (i = 0; i < w; i++) {
                for (j = 0; j < h; j++) {
                    if (ray[j*w+i] == t)
                        histogram[t]++;
                }
            }
        }

        int  yzt=0;
        int newyzt=(zmax+zmin)/2;//初始阈值
        while (yzt!=newyzt)
        //求出背景和前景的平均灰度值bp和fp
        {
            for (i = 0; i < yzt; i++) {
                count1 += histogram[i];//背景像素点的总个数
                sum1 += histogram[i] * i;//背景像素点的灰度总值
            }
            bp = (count1 == 0) ? 0 : (sum1 / count1);//背景像素点的平均灰度值

            for (j = i; j < histogram.length; j++) {
                count2 += histogram[j];//前景像素点的总个数
                sum2 += histogram[j] * j;//前景像素点的灰度总值
            }
            fp = (count2 == 0) ? 0 : (sum2 / count2);//前景像素点的平均灰度值
            yzt=newyzt;
            newyzt = (bp + fp) / 2;
        }
        int zuijiayzt=newyzt; //最佳阈值zuijiayzt

        //二值化
        for (i=0;i<w;i++)
        {
            for (j=0;j<h;j++)
            {
                if (ray[j*w+i]>zuijiayzt)
                {
                    ray[j*w+i]=255;
                    newpixel[j*w+i]=alpha[j*w+i]|(ray[j*w+i]<<16)|(ray[j*w+i]<<8)|ray[j*w+i];
                }
                else
                {
                    ray[j*w+i]=0;
                    newpixel[j*w+i]=alpha[j*w+i]|(ray[j*w+i]<<16)|(ray[j*w+i]<<8)|ray[j*w+i];

                }
            }
        }
        return newpixel;
    }
}

