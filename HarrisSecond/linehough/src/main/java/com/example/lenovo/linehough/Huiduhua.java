package com.example.lenovo.linehough;

/**
 * Created by lenovo on 2016/6/30.
 */
public class Huiduhua {
  //  private int [] pixels;
    public Huiduhua()
    {
        System.out.println("Huiduhua");
    }
    public static int[] huiDuMethod(int w,int h,int[] inputs)
    {   int[] pixels=new int[w*h];
        for (int i = 0; i < w; i++) {
            for (int j = 0; j < h; j++) {
               int alpha = inputs[j * w + i] & 0xFF000000;
               int red = (inputs[j * w + i] & 0x00FF0000) >> 16;
                int green = (inputs[j * w + i] & 0x0000FF00) >> 8;
               int  blue = inputs[j * w + i] & 0x000000FF;
              int  gray = (int) ((float) red * 0.3 + (float) green * 0.59 + (float) blue * 0.11);
                pixels[j * w + i] = alpha | (gray << 16) | (gray << 8) | gray;
            }
        }
        return pixels;
    }
}
