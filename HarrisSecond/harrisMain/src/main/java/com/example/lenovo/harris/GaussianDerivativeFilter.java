package com.example.lenovo.harris;

/**
 * Created by lenovo on 2016/6/8.
 */
public class GaussianDerivativeFilter {

    public final static int X_DIRECTION = 0;
    public final static int Y_DIRECTION = 16;
    public final static int XY_DIRECTION = 2;
    public final static int XX_DIRECTION = 4;
    public final static int YY_DIRECTION = 8;

    // private attribute and settings
    private int DIRECTION_TYPE = 0;
    private int GAUSSIAN_WIN_SIZE = 1;
    private double sigma = 10;

    public GaussianDerivativeFilter()
    {
        System.out.println("");
    }

    public int getGaussianWinSize() {
        return GAUSSIAN_WIN_SIZE;
    }

    public void setGaussianWinSize(int gAUSSIAN_WIN_SIZE) {
        GAUSSIAN_WIN_SIZE = gAUSSIAN_WIN_SIZE;
    }
    public int getDirectionType() {
        return DIRECTION_TYPE;
    }

    public void setDirectionType(int dIRECTION_TYPE) {
        DIRECTION_TYPE = dIRECTION_TYPE;
    }

    public int[] filter(int w,int h,int[] inputs) {
    int width = w;
    int height = h;
    int[] inPixels ;
    int[] outPixels = new int[width*height];
     inPixels=inputs;
    int index = 0, index2 = 0;
    double xred = 0, xgreen = 0, xblue = 0;
    int newRow=0, newCol=0;
    double[][] winDeviationData = getDirectionData();
    for(int row=0; row<height; row++) {
        int ta = 255, tr = 0, tg = 0, tb = 0;
        for(int col=0; col<width; col++) {
            index = row * width + col;
            for(int subrow = -GAUSSIAN_WIN_SIZE; subrow <= GAUSSIAN_WIN_SIZE; subrow++) {
                for(int subcol = -GAUSSIAN_WIN_SIZE; subcol <= GAUSSIAN_WIN_SIZE; subcol++) {
                    newRow = row + subrow;
                    newCol = col + subcol;
                    if(newRow < 0 || newRow >= height) {
                        newRow = row;
                    }
                    if(newCol < 0 || newCol >= width) {
                        newCol = col;
                    }
                    index2 = newRow * width + newCol;
                    tr = (inPixels[index2]& 0x00ff0000 >> 16) ;
                    tg = (inPixels[index2]& 0x0000ff00 >> 8) ;
                    tb = inPixels[index2] & 0x000000ff;
                    xred += (winDeviationData[subrow + GAUSSIAN_WIN_SIZE][subcol + GAUSSIAN_WIN_SIZE] * tr);
                    xgreen +=(winDeviationData[subrow + GAUSSIAN_WIN_SIZE][subcol + GAUSSIAN_WIN_SIZE] * tg);
                    xblue +=(winDeviationData[subrow + GAUSSIAN_WIN_SIZE][subcol + GAUSSIAN_WIN_SIZE] * tb);
                }
            }
            outPixels[index] = (ta << 24) | (clamp((int)xred) << 16) | (clamp((int)xgreen) << 8) | clamp((int)xblue);
            xred = xgreen = xblue = 0;
        }
    }
return outPixels;
}

    private double[][] getDirectionData()
    {
        double[][] winDeviationData = null;
        if(DIRECTION_TYPE == X_DIRECTION)
        {
            winDeviationData = this.getXDirectionDeviation();
        }
        else if(DIRECTION_TYPE == Y_DIRECTION)
        {
            winDeviationData = this.getYDirectionDeviation();
        }
        else if(DIRECTION_TYPE == XY_DIRECTION)
        {
            winDeviationData = this.getXYDirectionDeviation();
        }
        else if(DIRECTION_TYPE == XX_DIRECTION)
        {
            winDeviationData = this.getXXDirectionDeviation();
        }
        else if(DIRECTION_TYPE == YY_DIRECTION)
        {
            winDeviationData = this.getYYDirectionDeviation();
        }
        return winDeviationData;
    }

    public int clamp(int value) {
        // trick, just improve the lightness otherwise image is too darker...
        if(DIRECTION_TYPE == X_DIRECTION || DIRECTION_TYPE == Y_DIRECTION)
        {
            value = value * 10;
        }
        return value < 0 ? 0 : (value > 255 ? 255 : value);
    }

    //高斯窗口函数值e^(-(x*x+y*y)/(2*sigma*sigma))
    public double[][] get2DGaussianData()
    {
        int size = GAUSSIAN_WIN_SIZE * 2 + 1;
        double[][] winData = new double[size][size];
        double sigma2 = this.sigma * sigma;
        for(int x=-GAUSSIAN_WIN_SIZE; x<=GAUSSIAN_WIN_SIZE; x++)
        {
            for(int y=-GAUSSIAN_WIN_SIZE; y<=GAUSSIAN_WIN_SIZE; y++)
            {
                double r = x*x + y*y;
                double sum = -(r/(2*sigma2));
                winData[x + GAUSSIAN_WIN_SIZE][y + GAUSSIAN_WIN_SIZE] = Math.exp(sum);
            }
        }
        return winData;
    }

    //图像X方向的一阶倒数计算
    public double[][] getXDirectionDeviation()
    {
        int size = GAUSSIAN_WIN_SIZE * 2 + 1;
        double[][] data = get2DGaussianData();
        double[][] xDeviation = new double[size][size];
        double sigma2 = this.sigma * sigma;
        for(int x=-GAUSSIAN_WIN_SIZE; x<=GAUSSIAN_WIN_SIZE; x++)
        {
            double c = -(x/sigma2);
            for(int i=0; i<size; i++)
            {
                xDeviation[i][x + GAUSSIAN_WIN_SIZE] = c * data[i][x + GAUSSIAN_WIN_SIZE];
            }
        }
        return xDeviation;
    }

    //图像Y方向的一阶倒数计算
    public double[][] getYDirectionDeviation()
    {
        int size = GAUSSIAN_WIN_SIZE * 2 + 1;
        double[][] data = get2DGaussianData();
        double[][] yDeviation = new double[size][size];
        double sigma2 = this.sigma * sigma;
        for(int y=-GAUSSIAN_WIN_SIZE; y<=GAUSSIAN_WIN_SIZE; y++)
        {
            double c = -(y/sigma2);
            for(int i=0; i<size; i++)
            {
                yDeviation[y + GAUSSIAN_WIN_SIZE][i] = c * data[y + GAUSSIAN_WIN_SIZE][i];
            }
        }
        return yDeviation;
    }

//图像XY方向的导数计算
    public double[][] getXYDirectionDeviation()
    {
        int size = GAUSSIAN_WIN_SIZE * 2 + 1;
        double[][] data = get2DGaussianData();
        double[][] xyDeviation = new double[size][size];
        double sigma2 = sigma * sigma;
        double sigma4 = sigma2 * sigma2;

        for(int x=-GAUSSIAN_WIN_SIZE; x<=GAUSSIAN_WIN_SIZE; x++)
        {
            for(int y=-GAUSSIAN_WIN_SIZE; y<=GAUSSIAN_WIN_SIZE; y++)
            {
                double c = ((x*y)/sigma4);
                xyDeviation[x + GAUSSIAN_WIN_SIZE][y + GAUSSIAN_WIN_SIZE] = c * data[x + GAUSSIAN_WIN_SIZE][y + GAUSSIAN_WIN_SIZE];
            }
        }
        return normalizeData(xyDeviation);
    }

    private double[][] normalizeData(double[][] data)
    {
        // normalization the data
        double min = data[0][0];
        for(int x=-GAUSSIAN_WIN_SIZE; x<=GAUSSIAN_WIN_SIZE; x++)
        {
            for(int y=-GAUSSIAN_WIN_SIZE; y<=GAUSSIAN_WIN_SIZE; y++)
            {
                if(min > data[x + GAUSSIAN_WIN_SIZE][y + GAUSSIAN_WIN_SIZE])
                {
                    min = data[x + GAUSSIAN_WIN_SIZE][y + GAUSSIAN_WIN_SIZE];
                }
            }
        }

        for(int x=-GAUSSIAN_WIN_SIZE; x<=GAUSSIAN_WIN_SIZE; x++)
        {
            for(int y=-GAUSSIAN_WIN_SIZE; y<=GAUSSIAN_WIN_SIZE; y++)
            {
                data[x + GAUSSIAN_WIN_SIZE][y + GAUSSIAN_WIN_SIZE] = (data[x + GAUSSIAN_WIN_SIZE][y + GAUSSIAN_WIN_SIZE] /min);
            }
        }

        return data;
    }
//图像X方向的二阶导数计算
    public double[][] getXXDirectionDeviation()
    {
        int size = GAUSSIAN_WIN_SIZE * 2 + 1;
        double[][] data = get2DGaussianData();
        double[][] xxDeviation = new double[size][size];
        double sigma2 = this.sigma * sigma;
        double sigma4 = sigma2 * sigma2;
        for(int x=-GAUSSIAN_WIN_SIZE; x<=GAUSSIAN_WIN_SIZE; x++)
        {
            double c = ((x*x - sigma2)/sigma4);
            for(int i=0; i<size; i++)
            {
                xxDeviation[i][x + GAUSSIAN_WIN_SIZE] = c * data[i][x + GAUSSIAN_WIN_SIZE];
            }
        }
        return normalizeData(xxDeviation);
    }
//图像Y方向的二阶导数计算
    public double[][] getYYDirectionDeviation()
    {
        int size = GAUSSIAN_WIN_SIZE * 2 + 1;
        double[][] data = get2DGaussianData();
        double[][] yyDeviation = new double[size][size];
        double sigma2 = this.sigma * sigma;
        double sigma4 = sigma2 * sigma2;
        for(int y=-GAUSSIAN_WIN_SIZE; y<=GAUSSIAN_WIN_SIZE; y++)
        {
            double c = ((y*y - sigma2)/sigma4);
            for(int i=0; i<size; i++)
            {
                yyDeviation[y + GAUSSIAN_WIN_SIZE][i] = c * data[y + GAUSSIAN_WIN_SIZE][i];
            }
        }
        return normalizeData(yyDeviation);
    }

}

