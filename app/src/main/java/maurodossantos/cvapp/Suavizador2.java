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

public class Suavizador2 extends AppCompatActivity implements CameraBridgeViewBase.CvCameraViewListener2 {

    private static final String TAG = "Suavizador2";

    private ImageButton ibt_desfazerSuavizador2;
    private TextView tv_Media;
    private SeekBar sb_Media;

    private JavaCameraView JCV_suavizador2;

    private Mat mRgba, imgMedia;
    private int valor_sb, k_size = 3;

    private Display display;
    private int screenOrientation;
    private Size sizeRgba;

    BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case BaseLoaderCallback.SUCCESS: {
                    JCV_suavizador2.enableView();
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
        setContentView(R.layout.activity_suavizador2);

        //Recebe a orientação do dispositivo
        display = ((WindowManager) getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
        screenOrientation = display.getOrientation();

        // Link com os objetos da tela:
        JCV_suavizador2 = (JavaCameraView) findViewById(R.id.JCV_suavizador2);
        JCV_suavizador2.setVisibility(SurfaceView.VISIBLE);
        JCV_suavizador2.setCvCameraViewListener(this);

        ibt_desfazerSuavizador2 = (ImageButton) findViewById(R.id.ibt_desfazerSuavizador2);
        sb_Media = (SeekBar) findViewById(R.id.sb_Media);
        tv_Media = (TextView) findViewById(R.id.tv_Media);

        //Mudança nos SeekBar
        sb_Media.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                valor_sb = progress;
                valor_sb = 2 * (valor_sb + 2) - 1;
                tv_Media.setText(Integer.toString(valor_sb));
                k_size = valor_sb;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        //Botão Desfazer
        ibt_desfazerSuavizador2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                k_size = 3;
                tv_Media.setText("3");
                sb_Media.setProgress(0);
            }
        });

    }

    @Override
    protected void onPause() {
        super.onPause();
        if (JCV_suavizador2 != null) {
            JCV_suavizador2.disableView();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (JCV_suavizador2 != null) {
            JCV_suavizador2.disableView();
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
        imgMedia = new Mat(width, height, CvType.CV_8UC4);
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

        //Aplica o Suavizador por media
        Imgproc.medianBlur(mRgba, imgMedia, k_size);

        //Retorna a imagem para a tela
        return imgMedia;
    }

    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        //Muda a rotação se mudar a orientacao
        display = ((WindowManager) getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
        screenOrientation = display.getOrientation();
    }
}
