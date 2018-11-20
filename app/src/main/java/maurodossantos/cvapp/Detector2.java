package maurodossantos.cvapp;

import android.content.Context;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Display;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.JavaCameraView;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

public class Detector2 extends AppCompatActivity implements CameraBridgeViewBase.CvCameraViewListener2 {

    private static final String TAG = "Detector2";

    private JavaCameraView JCV_detector2;

    private TextView tv_dx, tv_dy;
    private SeekBar sb_dx, sb_dy;
    private ImageButton ibt_Detector2;

    private Mat mRgba, imgGray, imgSobel;
    private int dx = 1, dy = 1;

    private Display display;
    private int screenOrientation;
    private Size sizeRgba;

    BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case BaseLoaderCallback.SUCCESS: {
                    JCV_detector2.enableView();
                    break;
                }
                default: {
                    super.onManagerConnected(status);
                    break;
                }
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detector2);

        //Recebe a orientação do dispositivo
        display = ((WindowManager) getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
        screenOrientation = display.getOrientation();

        // Link com os objetos da tela:
        JCV_detector2 = (JavaCameraView) findViewById(R.id.JCV_detector2);
        JCV_detector2.setVisibility(SurfaceView.VISIBLE);
        JCV_detector2.setCvCameraViewListener(this);

        ibt_Detector2 = (ImageButton) findViewById(R.id.ibt_Detector2);
        sb_dx = (SeekBar) findViewById(R.id.sb_dx);
        tv_dx = (TextView) findViewById(R.id.tv_dx);
        sb_dy = (SeekBar) findViewById(R.id.sb_dy);
        tv_dy = (TextView) findViewById(R.id.tv_dy);

        //Mudança nos SeekBar
        sb_dx.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (progress == 0) {
                    if (sb_dy.getProgress() == 0) {
                        tv_dy.setText("1");
                        sb_dy.setProgress(1);
                        dy = 1;
                    }
                }
                tv_dx.setText(Integer.toString(progress));
                dx = progress;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        sb_dy.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (progress == 0) {
                    if (sb_dx.getProgress() == 0) {
                        tv_dx.setText("1");
                        sb_dx.setProgress(1);
                        dx = 1;
                    }
                }
                tv_dy.setText(Integer.toString(progress));
                dy = progress;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        //Botão Desfazer
        ibt_Detector2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dx = 1;
                dy = 1;
                tv_dx.setText("1");
                tv_dy.setText("1");
                sb_dx.setProgress(1);
                sb_dy.setProgress(1);
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (JCV_detector2 != null) {
            JCV_detector2.disableView();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (JCV_detector2 != null) {
            JCV_detector2.disableView();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        //Verificação Conexão OPENCV
        if (!OpenCVLoader.initDebug()) {
            Log.d(TAG, "ERRO");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_9, this, mLoaderCallback);
        } else {
            Log.d(TAG, "OK");
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
    }

    @Override
    public void onCameraViewStarted(int width, int height) {
        //Cria matrizes para operações
        mRgba = new Mat(width, height, CvType.CV_8UC4);
        imgGray = new Mat(width, height, CvType.CV_8UC1);
        imgSobel = new Mat(width, height, CvType.CV_8UC1);
    }

    @Override
    public void onCameraViewStopped() {
        mRgba.release();
    }

    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
        //Recebe frame
        mRgba = inputFrame.rgba();

        //Executa a rotacão se necessário
        switch (screenOrientation) {
            case 0: // Portrait
                sizeRgba = mRgba.size();
                mRgba = mRgba.t();
                Core.flip(mRgba, mRgba, 1);
                Imgproc.resize(mRgba, mRgba, sizeRgba);
                break;
            case 3: // Landscape direita
                sizeRgba = mRgba.size();
                mRgba = mRgba.t();
                Core.flip(mRgba, mRgba, 1);
                mRgba = mRgba.t();
                Core.flip(mRgba, mRgba, 1);
                Imgproc.resize(mRgba, mRgba, sizeRgba);
                break;
        }

        //Converte para escala de cinza
        Imgproc.cvtColor(mRgba, imgGray, Imgproc.COLOR_RGB2GRAY);

        //Aplica o detector de borda Sobel
        Imgproc.Sobel(imgGray, imgSobel, CvType.CV_8U, dx, dy);

        //Retorna a imagem para a tela
        return imgSobel;
    }

    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        //Muda a rotação se mudar a orientacao
        display = ((WindowManager) getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
        screenOrientation = display.getOrientation();
    }
}
