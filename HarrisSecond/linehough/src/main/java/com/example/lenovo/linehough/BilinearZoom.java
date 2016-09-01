package com.example.lenovo.linehough;

import android.graphics.Color;

/**
 * Created by lenovo on 2016/6/26.
 */
public class BilinearZoom {
        private int destW;
        private int destH;
        private int[] inPixels;

        public BilinearZoom(int[] input)
        {
            inPixels=input;
        }

        public void setDestW(int destW)
        {
            this.destW=destW;

        }

        public void setDestH(int destH)
        {
            this.destH=destH;

        }

        public int getDestW()
        {
            return destW;
        }

        public int getDestH()
        {
            return destH;
        }

        public int[] bilinear(int width, int height,int[] inputs) {
            int[] outPixels = new int[destW * destH];
            inPixels=inputs;
            double xRatio =((float) width) / ((float) destW);
            double yRatio = ((float) height) / ((float) destH);
            for (int x = 0; x < destW; x++) {
                int ta = 0, tr = 0, tg = 0, tb = 0;
                double newX = ((float) x) * xRatio;
                //得到newX的整数部分
                double i = Math.floor(newX);
                //得到newX的小数部分
                double t = newX - i;

                for (int y = 0; y < destH; y++) {
                    double newY = ((float) y) * yRatio;
                    double j = Math.floor(newY);
                    double u = newY - j;

                    int[] p1 = getPixel(i, j, width, height);
                    int[] p2 = getPixel(i, j + 1, width, height);
                    int[] p3 = getPixel(i + 1, j, width, height);
                    int[] p4 = getPixel(i + 1, j + 1, width, height);
                    double a = (1.0d - t) * (1.0d - u);
                    double b = (1.0d - t) * u;
                    double c = t * (1.0d - u);
                    double d = t * u;
                    ta = 255;
                    // p1*a+p2*b+p3*c+p4*d
                    tr = (int) (p1[0] * a + p2[0] * b + p3[0] * c + p4[0] * d);
                    tg = (int) (p1[1] * a + p2[1] * b + p3[1] * c + p4[1] * d);
                    tb = (int) (p1[2] * a + p2[2] * b + p3[2] * c + p4[2] * d);

                    outPixels[y * destW + x] = Color.argb(ta, tr, tg, tb);
                }
            }
            return outPixels;
        }
    public int[] xyBlinear(double newX,double newY,int width,int height,int[] countnew)
    {
        int ta = 0, tr = 0, tg = 0, tb = 0;
        //得到newX的整数部分
        double i = Math.floor(newX);
        //得到newX的小数部分
        double t = newX - i;
        double j = Math.floor(newY);
        double u = newY - j;

        int[] p1 = getPixel(i, j, width, height);
        int[] p2 = getPixel(i, j + 1, width, height);
        int[] p3 = getPixel(i + 1, j, width, height);
        int[] p4 = getPixel(i + 1, j + 1, width, height);
        double a = (1.0d - t) * (1.0d - u);
        double b = (1.0d - t) * u;
        double c = t * (1.0d - u);
        double d = t * u;
        ta = 255;
        // p1*a+p2*b+p3*c+p4*d
        tr = (int) (p1[0] * a + p2[0] * b + p3[0] * c + p4[0] * d);
        tg = (int) (p1[1] * a + p2[1] * b + p3[1] * c + p4[1] * d);
        tb = (int) (p1[2] * a + p2[2] * b + p3[2] * c + p4[2] * d);
        int newindex=(int)(newY*width + newX);
        countnew[newindex] = Color.argb(ta, tr, tg, tb);
        return countnew;
    }

        private int[] getPixel(double i, double j, int width, int height) {
            int x = (int) i;
            int y = (int) j;
            if (x < 0) {
                x = 0;
            }
            if (x >= width) {
                x = width - 1;
            }

            if (y < 0) {
                y = 0;
            }
            if (y >= height) {
                y = height - 1;
            }

            int index = y * width + x;
            int[] rgb = new int[3];
            rgb[0] = (inPixels[index] & 0x00ff0000) >> 16;
            rgb[1] = (inPixels[index] & 0x0000ff00)>> 8 ;
            rgb[2] =  inPixels[index] & 0x000000ff;
            return rgb;
        }


    }
