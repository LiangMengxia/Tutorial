package com.example.lenovo.linehough;

/**
 * Created by lenovo on 2016/6/29.
 */
public class Canny {
    private float gaussianKernelRadius = 2f;
    private int gaussianKernelWidth = 16;
    private float lowThreshold;
    private float highThreshold;
    // image width, height
    private int width;
    private int height;
    private float[] data;
    private float[] angle;

    public Canny() {
        lowThreshold = 10f;
        highThreshold = 30f;
        gaussianKernelRadius = 2f;
        gaussianKernelWidth = 16;
    }

    public float getGaussianKernelRadius() {
        return gaussianKernelRadius;
    }

    public void setGaussianKernelRadius(float gaussianKernelRadius) {
        this.gaussianKernelRadius = gaussianKernelRadius;
    }

    public int getGaussianKernelWidth() {
        return gaussianKernelWidth;
    }

    public void setGaussianKernelWidth(int gaussianKernelWidth) {
        this.gaussianKernelWidth = gaussianKernelWidth;
    }

    public float getLowThreshold() {
        return lowThreshold;
    }

    public void setLowThreshold(float lowThreshold) {
        this.lowThreshold = lowThreshold;
    }

    public float getHighThreshold() {
        return highThreshold;
    }

    public void setHighThreshold(float highThreshold) {
        this.highThreshold = highThreshold;
    }

    public int[] Cannyfilter(int w,int h,int[] inputs) {
        width = w;
        height = h;
        int[] inPixels;
        int[] outPixels = new int[width * height];
        inPixels=inputs;
        int index = 0;
        for (int x = 0; x < width; x++) {
            int ta = 0, tr = 0, tg = 0, tb = 0;
            for (int y = 0; y < height; y++) {
                index = y * width + x;
                ta = (inPixels[index] & 0xff000000)>> 24 ;
                tr = (inPixels[index] & 0x00ff0000)>> 16 ;
                tg = (inPixels[index] & 0x0000ff00)>> 8 ;
                tb = inPixels[index] & 0x000000ff;
                int gray = (int) (0.299 * tr + 0.587 * tg + 0.114 * tb);
                inPixels[index] = (ta << 24) | (gray << 16) | (gray << 8) | gray;
            }
        }


        float kernel[][] = new float[gaussianKernelWidth][gaussianKernelWidth];
        for(int x=0; x<gaussianKernelWidth; x++)
        {
            for(int y=0; y<gaussianKernelWidth; y++)
            {
                kernel[x][y] = gaussian(x, y, gaussianKernelRadius);
            }
        }

        int krr = (int)gaussianKernelRadius;
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                index = y * width +x;
                double weightSum = 0.0;
                double redSum = 0;
                for(int subRow=-krr; subRow<=krr; subRow++)
                {
                    int nrow =y + subRow;
                    if(nrow >= height || nrow < 0)
                    {
                        nrow = 0;
                    }
                    for(int subCol=-krr; subCol<=krr; subCol++)
                    {
                        int ncol = x + subCol;
                        if(ncol >= width || ncol <=0)
                        {
                            ncol = 0;
                        }
                        int index2 = nrow * width + ncol;
                        int tr1 = (inPixels[index2]  & 0x00ff0000) >> 16;//获取像素点灰度值
                        redSum += tr1*kernel[subRow+krr][subCol+krr];//灰度高斯卷积
                        weightSum += kernel[subRow+krr][subCol+krr];
                    }
                }
                int gray = (int)(redSum / weightSum);//???
                outPixels[index] = gray;//得到高斯平滑后的灰度
            }
        }


        data = new float[width * height];
        angle = new float[width * height];//像素点梯度角度
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                index = y * width + x;
              //求像素点梯度Ix
                float xg = (getPixel(outPixels, width, height, x, y+1) - getPixel(outPixels, width, height, x, y) +
                        getPixel(outPixels, width, height, x+1,y+1) - getPixel(outPixels, width, height,x+1, y))/2.0f;
               // 求像素点梯度Iy
                float yg = (getPixel(outPixels, width, height, x, y)- getPixel(outPixels, width, height, x+1, y) +
                        getPixel(outPixels, width, height,x, y+1) - getPixel(outPixels, width, height, x+1, y+1))/2.0f;
              //求梯度幅值
                data[index] = hypot(xg, yg);
                if(xg == 0)
                {
                    if(yg > 0)
                    {
                        angle[index]=90;
                    }
                    if(yg < 0)
                    {
                        angle[index]=-90;
                    }
                }
                else if(yg == 0)
                {
                    angle[index]=0;
                }
                else
                {
                    angle[index] = (float)((Math.atan(yg/xg) * 180)/Math.PI);
                 //   float m=(float)(Math.atan(Math.sqrt(3.0))*180/Math.PI);
                }
                // make it 0 ~ 180
                angle[index] += 90;
            }
        }

        //非最大值抑制
        for (int x = 0; x <width; x++) {
            for (int y = 0; y < height; y++) {
                index = y * width + x;
                float m0 = data[index];
                if(angle[index] >=0 && angle[index] < 22.5) // angle 0
                {   //getPixel返回梯度幅值大小
                    float m1 = getPixel(data, width, height,x, y+1);
                    float m2 = getPixel(data, width, height,x, y-1);
                    if(m0 < m1 || m0 < m2)
                    {
                        data[index] = 0;
                    }
                }
                else if(angle[index] >= 22.5 && angle[index] < 67.5) // angle +45
                {
                    float m1 = getPixel(data, width, height, x+1,y-1);
                    float m2 = getPixel(data, width, height, x-1,y+1);
                    //非最大值抑制
                    if(m0 < m1 || m0 < m2)
                    {
                        data[index] = 0;
                    }
                }
                else if(angle[index] >= 67.5 && angle[index] < 112.5) // angle 90
                {
                    float m1 = getPixel(data, width, height, x-1, y);
                    float m2 = getPixel(data, width, height, x+1, y);
                    //非最大值抑制
                    if(m0 < m1 || m0 < m2)
                    {
                        data[index] = 0;
                    }
                }
                else if(angle[index] >=112.5 && angle[index] < 157.5) // angle 135 / -45
                {
                    float m1 = getPixel(data, width, height, x-1,y-1);
                    float m2 = getPixel(data, width, height, x+1, y+1);
                    //非最大值抑制
                    if(m0 < m1 || m0 < m2)
                    {
                       data[index] = 0;
                    }
                }
                else if(angle[index] >=157.5 && angle[index] <180) // angle 0
                {   //getPixel返回梯度幅值大小
                    float m1 = getPixel(data, width, height, x, y+1);
                    float m2 = getPixel(data, width, height, x, y-1);
                    if(m0 < m1 || m0 < m2)
                    {
                        data[index] = 0;
                    }
                }
            }
        }
        //求梯度幅值的最大值max和最小值min
        float min = 255;
        float max = 0;
        for(int i=0; i<data.length; i++)
        {
            if(data[i] == 0) continue;
            min = Math.min(min, data[i]);
            max = Math.max(max, data[i]);
        }
        System.out.println("Image Max Gradient = " + max + " Mix Gradient = " + min);

      /*  highThreshold=(max+min)*3/4;
        lowThreshold=(max+min)*1/4;*/

        for (int x = 0; x < width;x++) {
            for (int y = 0;y < height; y++) {
                if (data[y*width+x]<lowThreshold)
                    data[y*width+x]=0;
                if(data[y*width+x]>= highThreshold )
                {
                    follow(x, y, lowThreshold);
                }
            }
        }


        for(int i=0; i<inPixels.length; i++)
        {
            int gray = (int)data[i];
            outPixels[i] = gray > 0 ? 0xff000000 : -1;//梯度幅值大于0像素点置为黑色，否则置为白色
        }
        return outPixels;
    }

    private void follow(int x1, int y1, float threshold) {
        int x0 = (x1 == 0) ? x1 : x1 - 1;
        int x2 = (x1 == width - 1) ? x1 : x1 + 1;
        int y0 = y1 == 0 ? y1 : y1 - 1;
        int y2 = y1 == height -1 ? y1 : y1 + 1;

        for (int x = x0; x <= x2; x++) {
            for (int y = y0; y <= y2; y++) {
                int i2 = x + y * width;
                if ((y != y1 || x != x1) && data[i2] < threshold) {
                     data[i2] = 0;
                }
            }
        }
    }

    private float getPixel(float[] input, int width, int height, int col,
                           int row) {
        if(col < 0 || col >= width)
            col = 0;
        if(row < 0 || row >= height)
            row = 0;
        int index = row * width + col;
        return input[index];
    }

    private float hypot(float x, float y) {
        return (float) Math.hypot(x, y);
    }

    private int getPixel(int[] inPixels, int width, int height, int col,
                         int row) {
        if(col < 0 || col >= width)
            col = 0;
        if(row < 0 || row >= height)
            row = 0;
        int index = row * width + col;
        return inPixels[index];
    }

    private float gaussian(float x, float y, float sigma) {
        float xDistance = x*x;
        float yDistance = y*y;
        float sigma22 = 2*sigma*sigma;
        float sigma22PI = (float)Math.PI * sigma22;
        return (float)Math.exp(-(xDistance + yDistance)/sigma22)/sigma22PI;
    }

}

