package com.example.lenovo.harris;

import android.graphics.Color;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by lenovo on 2016/6/8.
 */
public class HarrisCorner {
    private GaussianDerivativeFilter filter;
    private List<HarrisMatrix> harrisMatrixList;
    private double lambda = 0.04; // scope : 0.04 ~ 0.06

    // i hard code the window size just keep it' size is same as
    // first order derivation Gaussian window size
    private double sigma = 1; // always
    private double window_radius = 1; // always
    public HarrisCorner() {
        filter = new GaussianDerivativeFilter();
        harrisMatrixList = new ArrayList<HarrisMatrix>();
    }


    public int[] filter(int w,int h,int[] inputs) {
        int width = w;
        int height =h;
        int[] inPixels;
        inPixels=inputs;
        //第一步
        initSettings(height, width);
        //第二步set Ix,Iy
        filter.setDirectionType(GaussianDerivativeFilter.X_DIRECTION);
        sobel(width, height, GaussianDerivativeFilter.X_DIRECTION, inPixels);

        filter.setDirectionType(GaussianDerivativeFilter.Y_DIRECTION);
        sobel(width, height, GaussianDerivativeFilter.Y_DIRECTION, inPixels);

        // 第三步： Ix^2, Iy^2 and Ix*Iy
        //set Ix^2,Iy^2,Ix*Iy
        for(HarrisMatrix hm : harrisMatrixList)
        {
            double Ix = hm.getXGradient();
            double Iy = hm.getYGradient();
            hm.setIxIy(Ix * Iy);
            hm.setXGradient(Ix*Ix);
            hm.setYGradient(Iy*Iy);
        }

        // SumIx2, SumIy2 and SumIxIy
        calculateGaussianBlur(width, height);


        // R= Det(H) - lambda * (Trace(H))^2
        harrisResponse(width, height);

        // non-max suppression
        nonMaxValueSuppression(width, height);

        int[] outPixels = matchToImage(width, height, inPixels);

        // return result image
        return outPixels;
    }

//最后一步：检测R值大小，当R的绝对值很大时，当R值大于0时为角点，当R值小于0时为边缘
    private int[] matchToImage(int width, int height, int[] inputs) {
        int[] outPixels ;
        outPixels=inputs;
        int index = 0;
        double valuemax=(harrisMatrixList.get(0)).getMax();
        double valuemin=(harrisMatrixList.get(0)).getMax();
        for(int row=0; row<height; row++) {
            for(int col=0; col<width; col++) {
                index = row * width + col;
                HarrisMatrix hm = harrisMatrixList.get(index);
                if(hm.getMax() > valuemax)
                 valuemax=hm.getMax();

                if(hm.getMax() < valuemin)
                  valuemin=hm.getMax();
            }
        }

        for(int row=0; row<height; row++) {
            for(int col=0; col<width; col++) {
                index = row * width + col;
                HarrisMatrix hm = harrisMatrixList.get(index);
                if(hm.getMax() > (valuemax+valuemin)/2)
                {  outPixels[index]= Color.GREEN;
                }
            }
        }
        return outPixels;
    }


    //第七步：使用3*3窗口，实现非极大值抑制
    private void nonMaxValueSuppression(int width, int height) {
        int index = 0;
        int radius = (int)window_radius;
        for(int row=0; row<height; row++) {
            for(int col=0; col<width; col++) {
                index = row * width + col;
                HarrisMatrix hm = harrisMatrixList.get(index);
                double maxR = hm.getR();
                boolean isMaxR = true;
                for(int subrow =-radius; subrow<=radius; subrow++)
                {
                    for(int subcol=-radius; subcol<=radius; subcol++)
                    {
                        int nrow = row + subrow;
                        int ncol = col + subcol;
                        if(nrow >= height || nrow < 0)
                        {
                            nrow = 0;
                        }
                        if(ncol >= width || ncol < 0)
                        {
                            ncol = 0;
                        }
                        int index2 = nrow * width + ncol;
                        HarrisMatrix hmr = harrisMatrixList.get(index2);
                        if(hmr.getR() > maxR)
                        {
                            isMaxR = false;
                        }
                    }
                }
                if(isMaxR)
                {
                    hm.setMax(maxR);
                }

            }
        }
    }



    //第六步：计算角点响应值R
    private void harrisResponse(int width, int height) {
        int index = 0;
        for(int row=0; row<height; row++) {
            for(int col=0; col<width; col++) {
                index = row * width + col;
                HarrisMatrix hm = harrisMatrixList.get(index);
                double c =  hm.getIxIy() * hm.getIxIy();
                double ab = hm.getXGradient() * hm.getYGradient();
                double aplusb = hm.getXGradient() + hm.getYGradient();
                double response = (ab -c) - lambda * Math.pow(aplusb, 2);
                hm.setR(response);
            }
        }
    }

//第五步：计算sumxx，sumyy，sumxy，即A，B，C的值
    private void calculateGaussianBlur(int width, int height) {
        int index = 0;
        int radius = (int)window_radius;
        double[][] gw = get2DKernalData(radius, sigma);
        double sumxx = 0, sumyy = 0, sumxy = 0;
        for(int row=0; row<height; row++) {
            for(int col=0; col<width; col++) {
                for(int subrow =-radius; subrow<=radius; subrow++)
                {
                    for(int subcol=-radius; subcol<=radius; subcol++)
                    {
                        int nrow = row + subrow;
                        int ncol = col + subcol;
                        if(nrow >= height || nrow < 0)
                        {
                            nrow = 0;
                        }
                        if(ncol >= width || ncol < 0)
                        {
                            ncol = 0;
                        }
                        int index2 = nrow * width + ncol;
                        HarrisMatrix whm = harrisMatrixList.get(index2);
                        //{e^[-(x^2+y^2)/2sigma^2]}/(2pi*sigma^2) *(Ix^2)
                        //{e^[-(x^2+y^2)/2sigma^2]}/(2pi*sigma^2) *(Iy^2)
                        //{e^[-(x^2+y^2)/2sigma^2]}/(2pi*sigma^2) *(Ix^Iy)
                        sumxx += (gw[subrow + radius][subcol + radius] * whm.getXGradient());
                        sumyy += (gw[subrow + radius][subcol + radius] * whm.getYGradient());
                        sumxy += (gw[subrow + radius][subcol + radius] * whm.getIxIy());
                    }
                }
                index = row * width + col;
                HarrisMatrix hm = harrisMatrixList.get(index);
                hm.setXGradient(sumxx);
                hm.setYGradient(sumyy);
                hm.setIxIy(sumxy);

                // clean up for next loop
                sumxx = 0;
                sumyy = 0;
                sumxy = 0;
            }
        }
    }

    //第四步：计算高斯核参数{e^[-(x^2+y^2)/2sigma^2]}/(2pi*sigma^2)
    public double[][] get2DKernalData(int n, double sigma) {
        int size = 2*n +1;
        double sigma22 = 2*sigma*sigma;
        double sigma22PI = Math.PI * sigma22;
        double[][] kernalData = new double[size][size];
        int row = 0;
        for(int i=-n; i<=n; i++) {
            int column = 0;
            for(int j=-n; j<=n; j++) {
                double xDistance = i*i;
                double yDistance = j*j;
                kernalData[row][column] = Math.exp(-(xDistance + yDistance)/sigma22)/sigma22PI;
                column++;
            }
            row++;
        }

        return kernalData;
    }


    //第一步：对每一个像素点设置一个HarrisMatrix，并把这些HarrisMatrix排成一列
    private void initSettings(int height, int width)
    {
        int index = 0;
        for(int row=0; row<height; row++) {
            for(int col=0; col<width; col++) {
                index = row * width + col;
                HarrisMatrix matrix = new HarrisMatrix();
                harrisMatrixList.add(index, matrix);
            }
        }
    }

    //第二步：set Ix，Iy
    private   void sobel(int width ,int height,int type,int[] inPixels)
    {

        for (int i = 0; i < width; i++) {
            int  tr = 0, tg = 0, tb = 0;
            for (int j = 0; j < height; j++) {
              int  index = j* width + i;
                HarrisMatrix matrix = harrisMatrixList.get(index);
                if (type == GaussianDerivativeFilter.X_DIRECTION) {
                    int[] a1 = getPixel(inPixels, width, height, i-1, j- 1);
                    int[] a2 = getPixel(inPixels, width, height, i-1, j);
                    int[] a3 = getPixel(inPixels, width, height, i-1, j + 1);

                    int[] a7 = getPixel(inPixels, width, height, i+ 1, j - 1);
                    int[] a8 = getPixel(inPixels, width, height, i+1, j);
                    int[] a9 = getPixel(inPixels, width, height, i+1, j + 1);

                    tr = (a7[0] + 2 * a8[0] + a9[0]) - (a1[0] + 2 * a2[0] + a3[0]);
                    tg = (a7[1] + 2 * a8[1] + a9[1]) - (a1[1] + 2 * a2[1] + a3[1]);
                    tb = (a7[2] + 2 * a8[2] + a9[2]) - (a1[2] + 2 * a2[2] + a3[2]);
                    int gray=(int)(0.3*tr+0.59*tg+0.11*tb);
                    matrix.setXGradient(gray);

                }
               else if (type == GaussianDerivativeFilter.Y_DIRECTION)
                {
                    int[] a3 = getPixel(inPixels, width, height, i-1, j + 1);
                    int[] a6 = getPixel(inPixels, width, height, i, j+1);
                    int[] a9 = getPixel(inPixels, width, height, i+1,j+1);

                    int[] a1 = getPixel(inPixels, width, height, i-1, j- 1);
                    int[] a4 = getPixel(inPixels, width, height, i, j-1);
                    int[] a7= getPixel(inPixels, width, height, i+ 1, j - 1);

                    tr = (a3[0] + 2*a6[0] + a9[0]) - (a1[0]+2*a4[0]+a7[0]);
                    tg = (a3[1] + 2*a6[1] + a9[1]) - (a1[1]+2*a4[1]+a7[1]);
                    tb = (a3[2] + 2*a6[2] + a9[2]) - (a1[2]+2*a4[2]+a7[2]);
                    int gray=(int)(0.3*tr+0.59*tg+0.11*tb);
                    matrix.setYGradient(gray);
                }
            }}
    }

    private int[] getPixel(int[] inPixels, int width, int height, int i,
                           int j) {
        if(i < 0 || i>= width)
           i = 0;
        if(j< 0 || j >= height)
            j = 0;
        int index = j * width + i;
        int tr = (inPixels[index]& 0x00ff0000 >> 16) ;
        int tg = (inPixels[index] & 0x0000ff00>> 8) ;
        int tb = inPixels[index] & 0x000000ff;
        return new int[]{tr, tg, tb};
    }

}

