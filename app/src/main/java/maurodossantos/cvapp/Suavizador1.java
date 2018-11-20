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

public class Suavizador1 extends AppCompatActivity implements CameraBridgeViewBase.CvCameraViewListener2 {

    private static final String TAG = "Suavizador1";

    private ImageButton ibt_desfazerSuavizador1;
    private TextView tv_TamGaussiana, tv_DesvioGaussianaX, tv_DesvioGaussianaY;
    private SeekBar sb_TamGaussiana, sb_DesvioGaussianaX, sb_DesvioGaussianaY;

    private JavaCameraView JCV_suavizador1;

    private Mat mRgba, imgGaussian;
    private int valor_sb_TamGaussiana;
    private double sigmaX = 0, sigmaY = 0;
    private Size sizeDefault = new Size(3, 3);
    private Size size = sizeDefault;

    private Display display;
    private int screenOrientation;
    private Size sizeRgba;

    BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case BaseLoaderCallback.SUCCESS: {
                    JCV_suavizador1.enableView();
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
        setContentView(R.layout.activity_suavizador1);

        //Recebe a orientação do dispositivo
        display = ((WindowManager) getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
        screenOrientation = display.getOrientation();

        // Link com os objetos da tela:
        JCV_suavizador1 = (JavaCameraView) findViewById(R.id.JCV_suavizador1);
        JCV_suavizador1.setVisibility(SurfaceView.VISIBLE);
        JCV_suavizador1.setCvCameraViewListener(this);

        ibt_desfazerSuavizador1 = (ImageButton) findViewById(R.id.ibt_desfazerSuavizador1);
        sb_TamGaussiana = (SeekBar) findViewById(R.id.sb_TamGaussiana);
        tv_TamGaussiana = (TextView) findViewById(R.id.tv_TamGaussiana);
        sb_DesvioGaussianaX = (SeekBar) findViewById(R.id.sb_DesvioGaussianaX);
        tv_DesvioGaussianaX = (TextView) findViewById(R.id.tv_DesvioGaussianaX);
        sb_DesvioGaussianaY = (SeekBar) findViewById(R.id.sb_DesvioGaussianaY);
        tv_DesvioGaussianaY = (TextView) findViewById(R.id.tv_DesvioGaussianaY);

        //Mudança nos SeekBar
        sb_TamGaussiana.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                valor_sb_TamGaussiana = progress;
                valor_sb_TamGaussiana = 2 * (valor_sb_TamGaussiana + 2) - 1;
                size = new Size(valor_sb_TamGaussiana, valor_sb_TamGaussiana);
                tv_TamGaussiana.setText(Integer.toString(valor_sb_TamGaussiana));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        sb_DesvioGaussianaX.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (progress != 0) {
                    tv_DesvioGaussianaX.setText(Integer.toString(progress));
                } else {
                    tv_DesvioGaussianaX.setText("default");
                }
                sigmaX = 1.0 * progress;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        sb_DesvioGaussianaY.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (progress != 0) {
                    tv_DesvioGaussianaY.setText(Integer.toString(progress));
                } else {
                    tv_DesvioGaussianaY.setText("default");
                }
                sigmaY = 1.0 * progress;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        //Botão Desfazer
        ibt_desfazerSuavizador1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sigmaX = 0;
                sigmaY = 0;
                size = sizeDefault;
                tv_TamGaussiana.setText("3");
                sb_TamGaussiana.setProgress(0);
                tv_DesvioGaussianaX.setText("default");
                sb_DesvioGaussianaX.setProgress(0);
                tv_DesvioGaussianaY.setText("default");
                sb_DesvioGaussianaY.setProgress(0);
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (JCV_suavizador1 != null) {
            JCV_suavizador1.disableView();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (JCV_suavizador1 != null) {
            JCV_suavizador1.disableView();
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
        imgGaussian = new Mat(width, height, CvType.CV_8UC4);
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

        //Aplica o suavizador por gaussiana
        Imgproc.GaussianBlur(mRgba, imgGaussian, size, sigmaX, sigmaY);

        //Retorna a imagem para a tela
        return imgGaussian;
    }

    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        ///Muda a rotação se mudar a orientacao
        display = ((WindowManager) getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
        screenOrientation = display.getOrientation();
    }

}
