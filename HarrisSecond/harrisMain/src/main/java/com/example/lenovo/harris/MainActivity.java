package com.example.lenovo.harris;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.lenovo.linehough.BilinearZoom;
import com.example.lenovo.linehough.Canny;
import com.example.lenovo.linehough.Gauss;
import com.example.lenovo.linehough.Huiduhua;
import com.example.lenovo.linehough.LineHough;

import java.util.Arrays;

public class MainActivity extends AppCompatActivity {
    private int w;
    private int h;
    private int[] inputs;
    private int[] inputs2;
    private Bitmap bim2;
    private Bitmap bim3;
    private Bitmap bim4;
    private ImageView image;
    private TextView tv;
    int[][] xx = new int[6][6];
    int[][] yy = new int[6][6];
    int x0 = 0, y0 = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        image = (ImageView) findViewById(R.id.image1);
        Button button1 = (Button) findViewById(R.id.huiduhua);
        Button button2 = (Button) findViewById(R.id.erzhihua);
        Button button3 = (Button) findViewById(R.id.canny);
        Button button4 = (Button) findViewById(R.id.gujia);
        Button button5 = (Button) findViewById(R.id.linehough);
        Button button6 = (Button) findViewById(R.id.harris);
        Button button7 = (Button) findViewById(R.id.distance);
        Button button8 = (Button) findViewById(R.id.gauss);
        Button button9 = (Button) findViewById(R.id.wait);
        Button button10 = (Button) findViewById(R.id.correct);
        // tv = (TextView)findViewById(R.id.textdistance);
        tv = new TextView(this);
        Bitmap bim1 = BitmapFactory.decodeResource(getResources(), R.drawable.blank);
        w = bim1.getWidth();
        h = bim1.getHeight();
        inputs = new int[w * h];
        bim2 = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        bim3 = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        bim4 = bim1.copy(Bitmap.Config.ARGB_8888, true);
        for (int x = 0; x < w; x++) {
            for (int y = 0; y < h; y++) {
                inputs[y * w + x] = bim1.getPixel(x, y);
                bim3.setPixel(x, y, Color.WHITE);
            }
        }
        image.setImageBitmap(bim1);

        if (button1 != null)
            button1.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //灰度化处理
                    //  Huiduhua huiduhua = new Huiduhua();
                    inputs = Huiduhua.huiDuMethod(w, h, inputs);
                    viewImage(w, h, inputs);

                }
            });
        if (button2 != null)
            button2.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //二值化处理
                    //  ErZhi erzhi = new ErZhi();
                    inputs = ErZhi.ErZhiHua(w, h, inputs);
                    viewImage(w, h, inputs);
                }
            });
        if (button3 != null)
            button3.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //Canny边缘检测
                    Canny canny = new Canny();
                    inputs = canny.Cannyfilter(w, h, inputs);
                    viewImage(w, h, inputs);
                }
            });
        if (button4 != null)
            button4.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //骨架提取
                    boolean s;
                    Mat matClass = new Mat();
                    s = matClass.matswitch(w, h, inputs);
                    inputs = matClass.matresult(s, w, h);
                    inputs2=new int[w*h];
                    Arrays.fill(inputs2,Color.WHITE);
                    for (int x = 0; x < w; x++) {
                        for (int y = 0; y < h; y++) {
                            inputs2[y*w+x] = inputs[y*w+x];
                        }
                    }
                    viewImage(w, h, inputs);
                }
            });
        if (button5 != null)
            button5.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //检测直线
                    LineHough lines = new LineHough();
                    lines.init(inputs, w, h);
                    int n = 15;
                    int ww = w / n;
                    int hh = h / n;
                    for (int x = 0; x < w; x = x + ww) {
                        for (int y = 0; y < h; y = y + hh) {
                            inputs = lines.process(x, y, n);
                        }
                    }
                    viewImage(w, h, inputs);
                }
            });
        if (button6 != null)
            button6.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //Harris角点检测直线角点
                    HarrisCorner harrisCorner = new HarrisCorner();
                    inputs = harrisCorner.filter(w, h, inputs);
                  /*  for (int x = 0; x < w; x++) {
                        for (int y = 0; y < h; y++) {
                            bim2.setPixel(x, y, inputs[y * w + x]);
                        }
                    }
                    image.setImageBitmap(bim2);*/
                }
            });

        if (button10 != null)
            button10.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int x = 0, y = 0, count = 0;
                    //找第一个角点的坐标
                    while (count != 1 && y < h) {
                        if (inputs[y * w + x] == Color.GREEN) {
                            x0 = x;
                            y0 = y;
                            count++;
                        }
                        if (++x < w) ;
                        else {
                            x = 0;
                            y++;
                        }
                    }
                    //第一个角点的横坐标加150，纵坐标不变，以该点为中心，检测该点周围的角点，
                    // 作为第二个角点 ，依次类推，找出第三个、第四个。。。角点
                    //周围有多个角点的，取它们的均值

                    for (int i = 1; i <= 5; i++) {
                        int newy = y0 + (i - 1) * 160; //中心点y坐标，每次在y坐标加150作为新中心点的y坐标
                        for (int j = 1; j <= 5; j++) {
                            int newx = x0 + (j - 1) * 160;//中心点x坐标
                            int number = 0, sx = 0, sy = 0;
                            //在中心点30*30的正方形区域类检测角点
                            for (int a = -50; a <= 50; a++) {
                                xx[i][j] = newx + a;
                                if (xx[i][j] < 0) xx[i][j] = 0;
                                if (xx[i][j] > w) xx[i][j] = w - 1;

                                for (int b = -50; b <= 50; b++) {
                                    yy[i][j] = newy + b;
                                    if (yy[i][j] < 0) yy[i][j] = 0;
                                    if (yy[i][j] > h) yy[i][j] = h - 1;

                                    if (inputs[yy[i][j] * w + xx[i][j]] == Color.GREEN) {
                                        number++;
                                        sx += xx[i][j];
                                        sy += yy[i][j];
                                    }
                                }
                            }
                            //中心区域有多个角点的时候，取x，y的均值作为该区域唯一角点的坐标
                            if (number != 0) {
                                xx[i][j] = sx / number;
                                yy[i][j] = sy / number;
                                bim2.setPixel(xx[i][j], yy[i][j], Color.RED);
                            }
                        }
                    }
                    image.setImageBitmap(bim2);
                }
            });
        //u(i)=aij * x^i * y^j  i=0,1,2,3,4;
        //v(i)=bij * x^i * y^j  j=0,1,2,3,4;
        //25个畸变点坐标的x值与25个校正点的x值之间构成含有25个含有25个未知参数的一次方程组；
        //同理，y值构成了25个含有25个未知参数的方程组；
        //下面就是解这两个方程组；
        if (button8 != null)
            button8.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    double[][] ax = new double[26][26];
                    double[][] ay = new double[26][26];
                    double[] bx = new double[26];
                    double[] by = new double[26];
                    double[] kx = new double[26];
                    double[] ky = new double[26];

                    for (int i = 1; i <= 5; i++) {
                        for (int j = 1; j <= 5; j++) {
                            for (int m = 1; m <= 5; m++) {
                                for (int n = 1; n <= 5; n++) {
                                    ax[(i - 1) * 5 + j][(m - 1) * 5 + n] = Math.pow(xx[i][j], (m - 1)) * Math.pow(yy[i][j], (n - 1));
                                    ay[(i - 1) * 5 + j][(m - 1) * 5 + n] = Math.pow(xx[i][j], (m - 1)) * Math.pow(yy[i][j], (n - 1));
                                }
                            }
                            bx[(i - 1) * 5 + j] = x0 + (j - 1) * 162;
                            by[(i - 1) * 5 + j] = y0 + (j - 1) * 162;
                        }
                    }
                    Gauss.setN(25);
                    Gauss.elimination(ax, bx);
                    kx = Gauss.back();

                    Gauss.setN(25);
                    Gauss.elimination(ay, by);
                    ky = Gauss.back();

                    //对原图进行校正

                    int[] countnew = new int[w * h];
                    int[] orginput = new int[w * h];

                    for (int y = 0; y < h; y++) {
                        for (int x = 0; x < w; x++) {
                            countnew[y * w + x] = Color.WHITE;
                            orginput[y * w + x] = bim4.getPixel(x, y);
                        }
                    }
                    BilinearZoom bilinearZoom = new BilinearZoom(orginput);
                    //在原图中进行插值
                    for (int y = 0; y < h; y++) {
                        for (int x = 0; x < w; x++) {
                            //if (inputs[y * w + x] == Color.BLACK || inputs[y * w + x] == Color.GREEN) {
                            if (orginput[y * w + x] != Color.WHITE) {
                                double newx = 0, newy = 0;
                                for (int i = 1; i <= 5; i++) {
                                    for (int j = 1; j <= 5; j++) {
                                        newx += kx[(i - 1) * 5 + j] * Math.pow(x, (i - 1)) * Math.pow(y, (j - 1));
                                        newy += ky[(i - 1) * 5 + j] * Math.pow(x, (i - 1)) * Math.pow(y, (j - 1));
                                    }
                                }
                              /*  if (newx < 0) newx = 0;
                                if (newx >= w) newx = w - 1;
                                if (newy < 0) newy = 0;
                                if (newy >= h) newy = h - 1;*/
                               // countnew[(int)(newy*w+newx)]=Color.BLACK;
                                //双线性插值
                                countnew = bilinearZoom.xyBlinear(newx, newy, w, h, countnew);
                            }
                        }
                    }
                    for (int y = 0; y < h; y++) {
                        for (int x = 0; x < w; x++) {
                            bim3.setPixel(x, y, countnew[y * w + x]);
                        }
                    }
                    image.setImageBitmap(bim3);
                }
            });

        /*      if (button7 != null)
            button7.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    double[][] ax = new double[5][5];
                    double[][] ay = new double[5][5];
                    double[] bx = new double[5];
                    double[] by = new double[5];
                    double[] kx = new double[5];
                    double[] ky = new double[5];
                    int[] pointxy = new int[2 * 100];
                    double[] d = new double[100];
                    ReturnXY returnXY = new ReturnXY();
                    int count = 0;
                    for (int y = 0; y < h; y++) {
                        for (int x = 0; x < w; x++) {
                            if (inputs[y * w + x] == Color.GREEN) {
                                pointxy = returnXY.xyMethod(x, y);
                                count++;
                                if (count <= 4) {
                                    ax[count][1] = 1.0;
                                    ax[count][2] = (double) x;
                                    ax[count][3] = (double) y;
                                    ax[count][4] = (double) (x * y);
                                    ay[count][1] = 1.0;
                                    ay[count][2] = (double) x;
                                    ay[count][3] = (double) y;
                                    ay[count][4] = (double) (x * y);
                                }
                            }
                        }
                    }
                    //这里如果直接用ay=ax给ay数组赋值的话，当gauss1.elimination(ax, bx);执行过后，
                    // ax数组元素值发生了变化，转换为高斯消元后的上三角矩阵形式，而由于ay=ax这条语句，ay元素值也会发生改变，变成了跟ax一样的高斯消元形式
                    //这就导致在执行gauss1.elimination(ay, by);过程中，ay已不用进行消元，于是l总是为0，by元素值不变。
                    // 即ay进行了消元化简，by没有进行消元，故之后得到的方程解是错误的。这也是为什么ax，ay要分别赋值的原因。
                    //疑问：在消元过程中，我是先将ax传给另一个数组a，实际上是数组a进行了消元，可是主程序中的ax元素值为什么也会发生变化？


                    double dis = 119;
                    bx[1] = (double) pointxy[0];
                    bx[2] = (double) pointxy[0] - dis;
                    bx[3] = (double) pointxy[0] - dis;
                    bx[4] = (double) pointxy[0];

                    by[1] = (double) pointxy[1];
                    by[2] = (double) pointxy[1];
                    by[3] = pointxy[1] + dis;
                    by[4] = pointxy[1] + dis;


                    Gauss.setN(4);
                    Gauss.elimination(ax, bx);
                    kx = Gauss.back();

                    Gauss.elimination(ay, by);
                    ky = Gauss.back();

                    //对原图进行校正
                    double newx = 0, newy = 0;
                    int[] countnew = new int[w * h];
                    int[] orginput = new int[w * h];

                    for (int y = 0; y < h; y++) {
                        for (int x = 0; x < w; x++) {
                            countnew[y * w + x] = bim4.getPixel(x, y);
                            orginput[y * w + x] = bim4.getPixel(x, y);
                        }
                    }
                    BilinearZoom bilinearZoom = new BilinearZoom(orginput);
                    //在原图中进行插值
                    for (int y = 0; y < h; y++) {
                        for (int x = 0; x < w; x++) {
                            if (inputs[y * w + x] == Color.BLACK || inputs[y * w + x] == Color.GREEN) {
                                newx = (kx[1] + kx[2] * x + kx[3] * y + kx[4] * x * y);
                                newy = (ky[1] + ky[2] * x + ky[3] * y + ky[4] * x * y);
                                bilinearZoom.xyBlinear(newx, newy, w, h, countnew);
                            }
                        }
                    }

                    for (int y = 0; y < h; y++) {
                        for (int x = 0; x < w; x++) {
                            bim4.setPixel(x, y, countnew[y * w + x]);
                        }
                    }
                    image.setImageBitmap(bim4);


               //对二值图像进行校正
                int newx=0,newy=0;
                int[] countnew=new int[w*h];
                for (int y = 0; y < h; y++) {
                    for (int x = 0; x < w; x++) {
                        if (inputs[y * w + x]==Color.BLACK||inputs[y * w + x]==Color.GREEN)
                        {   newx=(int)(kx[1]+kx[2]*x+kx[3]*y+kx[4]*x*y);
                            newy=(int)(ky[1]+ky[2]*x+ky[3]*y+ky[4]*x*y);
                            countnew[newy*w+newx]=1;//标记所有校正后的像素点，之后再给这些点标为黑色。
                            // 如果校正一个点就给它标为黑色，则在之后的循环检测中，这些点可能就被当成未校正的黑点被再次校正，结果就会出错
                            bim3.setPixel(x,y,Color.BLACK);
                     }
                    }
                }
                for (int y = 0; y < h; y++) {
                    for (int x = 0; x < w; x++) {
                        if (countnew[y*w+x]==1)
                        { inputs[y*w+x]=Color.BLACK;
                            bim3.setPixel(x,y,inputs[y*w+x]);}
                    }
                    }
              image.setImageBitmap(bim3);

                //用Textview显示相邻两角点间距离
                tv.setMaxLines(count / 2 + 1);
                String text = "";
                for (int i = 0; i < 3; i++) {
                    d[i] = returnXY.pointsDistance(pointxy[2 * i], pointxy[2 * i + 1], pointxy[2 * i + 2], pointxy[2 * i + 3]);
                    //if (d[i]>110&&d[i]<160)
                    {
                        text += "角点距离(" + pointxy[2 * i] + "," + pointxy[2 * i + 1] + "),(" + pointxy[2 * i + 2] + "," + pointxy[2 * i + 3] + ")=" + d[i] + "\n";
                        inputs[pointxy[2 * i + 1] * w + pointxy[2 * i]] = Color.RED;
                        inputs[pointxy[2 * i + 3] * w + pointxy[2 * i + 2]] = Color.RED;
                    }
                }
                tv.setText(text);
                setContentView(tv);
                }
            });
        if (button8 != null)
            button8.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    double[][] a = {{0, 0, 0}, {0, 1, -1}, {0, 1, 1}};
                    double[] b = {0, 1, 2};
                    double[] x = new double[3];
                    Gauss gauss = new Gauss();
                    gauss.elimination(a, b);
                    x = gauss.back();
                    tv.setText(x[1] + "," + x[2]);
                    setContentView(tv);

                }
            });
        if (button9!=null)
        button9.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                viewImage(w, h, inputs);
            }
        });*/
    }

    public void viewImage(int w, int h, int[] inputs) {
        for (int x = 0; x < w; x++) {
            for (int y = 0; y < h; y++) {
                bim2.setPixel(x, y, inputs[y * w + x]);
            }
        }
        image.setImageBitmap(bim2);
    }

}


