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

public class Segmentacao1 extends AppCompatActivity implements CameraBridgeViewBase.CvCameraViewListener2 {

    private static final String TAG = "Segmentacao1";

    private TextView tv_ValorMaximo, tv_ThresholdSegmentacao, tv_TipoThreshold;
    private SeekBar sb_ValorMaximo, sb_ThresholdSegmentacao, sb_TipoThreshold;
    private ImageButton ibt_Segmentacao1;

    private JavaCameraView JCV_segmentacao1;

    private Mat mRgba, imgGray, imgThreshold;
    private double valor_ValorMaximo = 255, valor_ThresholdSegmentacao = 100;
    private int valor_TipoThreshold = Imgproc.THRESH_BINARY;

    private Display display;
    private int screenOrientation;
    private Size sizeRgba;

    BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case BaseLoaderCallback.SUCCESS: {
                    JCV_segmentacao1.enableView();
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
        setContentView(R.layout.activity_segmentacao1);

        //Recebe a orientação do dispositivo
        display = ((WindowManager) getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
        screenOrientation = display.getOrientation();

        // Link com os objetos da tela:
        JCV_segmentacao1 = (JavaCameraView) findViewById(R.id.JCV_segmentacao1);
        JCV_segmentacao1.setVisibility(SurfaceView.VISIBLE);
        JCV_segmentacao1.setCvCameraViewListener(this);

        ibt_Segmentacao1 = (ImageButton) findViewById(R.id.ibt_Segmentacao1);
        sb_ValorMaximo = (SeekBar) findViewById(R.id.sb_ValorMaximo);
        tv_ValorMaximo = (TextView) findViewById(R.id.tv_ValorMaximo);
        sb_ThresholdSegmentacao = (SeekBar) findViewById(R.id.sb_ThresholdSegmentacao);
        tv_ThresholdSegmentacao = (TextView) findViewById(R.id.tv_ThresholdSegmentacao);
        sb_TipoThreshold = (SeekBar) findViewById(R.id.sb_TipoThreshold);
        tv_TipoThreshold = (TextView) findViewById(R.id.tv_TipoThreshold);

        //Mudança nos SeekBar
        sb_ValorMaximo.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

                tv_ValorMaximo.setText(Integer.toString(progress));
                valor_ValorMaximo = progress * 1.0;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        sb_ThresholdSegmentacao.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

                tv_ThresholdSegmentacao.setText(Integer.toString(progress));
                valor_ThresholdSegmentacao = progress * 1.0;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        sb_TipoThreshold.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

                switch (progress) {
                    case 0:
                        tv_TipoThreshold.setText("Binário");
                        valor_TipoThreshold = Imgproc.THRESH_BINARY;
                        break;
                    case 1:
                        tv_TipoThreshold.setText("Binário Invertido");
                        valor_TipoThreshold = Imgproc.THRESH_BINARY_INV;
                        break;
                    case 2:
                        tv_TipoThreshold.setText("Truncado");
                        valor_TipoThreshold = Imgproc.THRESH_TRUNC;
                        break;
                    case 3:
                        tv_TipoThreshold.setText("Para 0");
                        valor_TipoThreshold = Imgproc.THRESH_TOZERO;
                        break;
                    case 4:
                        tv_TipoThreshold.setText("Para 0 Invertido");
                        valor_TipoThreshold = Imgproc.THRESH_TOZERO_INV;
                        break;
                    default:
                        break;

                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        //Botão Desfazer
        ibt_Segmentacao1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                valor_ValorMaximo = 255;
                valor_ThresholdSegmentacao = 100;
                valor_TipoThreshold = Imgproc.THRESH_BINARY;
                tv_TipoThreshold.setText("Binário");
                tv_ThresholdSegmentacao.setText("100");
                tv_ValorMaximo.setText("255");
                sb_TipoThreshold.setProgress(0);
                sb_ThresholdSegmentacao.setProgress(100);
                sb_ValorMaximo.setProgress(255);
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (JCV_segmentacao1 != null) {
            JCV_segmentacao1.disableView();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (JCV_segmentacao1 != null) {
            JCV_segmentacao1.disableView();
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
        imgThreshold = new Mat(width, height, CvType.CV_8UC4);
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

        //Segmentação com Threshold
        Imgproc.threshold(imgGray, imgThreshold, valor_ThresholdSegmentacao, valor_ValorMaximo, valor_TipoThreshold);//THRESH_BINARY, THRESH_BINARY_INV, THRESH_TRUNC, THRESH_TOZERO, THRESH_TOZERO_INV

        //Retorna a imagem para a tela
        return imgThreshold;
    }

    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        //Muda a rotação se mudar a orientacao
        display = ((WindowManager) getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
        screenOrientation = display.getOrientation();
    }
}
