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

public class Detector1 extends AppCompatActivity implements CameraBridgeViewBase.CvCameraViewListener2 {

    private static final String TAG = "Detector1";

    private JavaCameraView JCV_detector1;

    private ImageButton ibt_Detector1;
    private TextView tv_Threshould1, tv_Threshould2;
    private SeekBar sb_Threshould1, sb_Threshould2;

    private double valor_threshould1 = 100, valor_threshould2 = 150;
    private Mat mRgba, imgCanny, imgGray, mRgbaT;


    private Display display;
    private int screenOrientation;
    private Size sizeRgba;

    BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case BaseLoaderCallback.SUCCESS: {
                    JCV_detector1.enableView();
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
        setContentView(R.layout.activity_detector1);

        //Recebe a orientação do dispositivo
        display = ((WindowManager) getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
        screenOrientation = display.getOrientation();

        // Link com os objetos da tela:
        JCV_detector1 = (JavaCameraView) findViewById(R.id.JCV_detector1);
        JCV_detector1.setVisibility(SurfaceView.VISIBLE);
        JCV_detector1.setCvCameraViewListener(this);

        ibt_Detector1 = (ImageButton) findViewById(R.id.ibt_Detector1);
        sb_Threshould1 = (SeekBar) findViewById(R.id.sb_Threshould1);
        tv_Threshould1 = (TextView) findViewById(R.id.tv_Threshould1);
        sb_Threshould2 = (SeekBar) findViewById(R.id.sb_Threshould2);
        tv_Threshould2 = (TextView) findViewById(R.id.tv_Threshould2);

        //Mudança nos SeekBar
        sb_Threshould1.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

                tv_Threshould1.setText(Integer.toString(progress));
                valor_threshould1 = progress * 1.0;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        sb_Threshould2.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

                tv_Threshould2.setText(Integer.toString(progress));
                valor_threshould2 = progress * 1.0;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        //Botão Desfazer
        ibt_Detector1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                valor_threshould1 = 100;
                valor_threshould2 = 150;
                tv_Threshould1.setText("100");
                tv_Threshould2.setText("150");
                sb_Threshould1.setProgress(100);
                sb_Threshould2.setProgress(150);
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (JCV_detector1 != null) {
            JCV_detector1.disableView();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (JCV_detector1 != null) {
            JCV_detector1.disableView();
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
        imgCanny = new Mat(width, height, CvType.CV_8UC1);
        imgGray = new Mat(width, height, CvType.CV_8UC1);
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

        //Aplica o detector de borda Canny
        Imgproc.Canny(imgGray, imgCanny, valor_threshould1, valor_threshould2);

        //Retorna a imagem para a tela
        return imgCanny;
    }

    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        //Muda a rotação se mudar a orientacao
        display = ((WindowManager) getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
        screenOrientation = display.getOrientation();
    }

}
