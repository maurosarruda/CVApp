package maurodossantos.cvapp;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfInt4;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.Random;

public class Segmentacao2 extends AppCompatActivity {

    private static final int ACTIVITY_CAMERA_REQUEST_CODE = 0;
    private static final int ACTIVITY_GALERIA_REQUEST_CODE = 1;
    private static final int WRITE_PERMISSAO_REQUEST = 2;
    private static final int READ_PERMISSAO_REQUEST = 3;

    private ImageView imV_galeriaSeg;
    private Button bt_imExemploSeg, bt_imGaleriaSeg, bt_imCameraSeg, bt_executarSeg;
    private ImageButton ibt_desfazerSegmentacao3, ibt_desfazerImagem, ibt_salvarImagem;
    private TextView tv_ThreshouldBinarizacaoSegmentacao, tv_ThreshouldDistancia;
    private SeekBar sb_ThreshouldBinarizacaoSegmentacao, sb_ThreshouldDistancia;

    private File arquivoFoto = null, arquivoSalvar = null;

    private Bitmap bitmap;
    private Mat rgba = new Mat();
    private double valorThreshoulBinarizacao = 127, valorThreshoulDistancia = 0.40;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_segmentacao2);

        // Solicita que o usuario autorize a leitura pelo app
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_EXTERNAL_STORAGE)) {
                Toast.makeText(this, "Permissao para ler imagem da galeria", Toast.LENGTH_LONG).show();
            }
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, READ_PERMISSAO_REQUEST);
        }
        //Solicita que o usuario autorize a escrita na galeria pelo app
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                Toast.makeText(this, "Permissao para gravar imagem na galeria", Toast.LENGTH_LONG).show();
            }
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, WRITE_PERMISSAO_REQUEST);
        }

        // Link com os objetos da tela:
        imV_galeriaSeg = (ImageView) findViewById(R.id.imV_galeriaSeg);
        bt_imExemploSeg = (Button) findViewById(R.id.bt_imExemploSeg);
        bt_imGaleriaSeg = (Button) findViewById(R.id.bt_imGaleriaSeg);
        bt_imCameraSeg = (Button) findViewById(R.id.bt_imCameraSeg);
        bt_executarSeg = (Button) findViewById(R.id.bt_executarSeg);
        ibt_desfazerSegmentacao3 = (ImageButton) findViewById(R.id.ibt_desfazerSegmentacao3);
        ibt_desfazerImagem = (ImageButton) findViewById(R.id.ibt_desfazerImagem);
        ibt_salvarImagem = (ImageButton) findViewById(R.id.ibt_salvarImagem);
        tv_ThreshouldBinarizacaoSegmentacao = (TextView) findViewById(R.id.tv_ThreshouldBinarizacaoSegmentacao);
        tv_ThreshouldDistancia = (TextView) findViewById(R.id.tv_ThreshouldDistancia);
        sb_ThreshouldBinarizacaoSegmentacao = (SeekBar) findViewById(R.id.sb_ThreshouldBinarizacaoSegmentacao);
        sb_ThreshouldDistancia = (SeekBar) findViewById(R.id.sb_ThreshouldDistancia);

        //Desabilita botões, pois a imagem precisa ser carregada
        bt_executarSeg.setEnabled(false);
        ibt_desfazerImagem.setEnabled(false);
        ibt_salvarImagem.setEnabled(false);

        //Mudança nos SeekBar
        sb_ThreshouldBinarizacaoSegmentacao.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                tv_ThreshouldBinarizacaoSegmentacao.setText(Integer.toString(progress));
                valorThreshoulBinarizacao = 1.0 * progress;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        sb_ThreshouldDistancia.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                tv_ThreshouldDistancia.setText(Integer.toString(progress));
                valorThreshoulDistancia = 0.01 * progress;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        //Recebe imagem da Camera
        bt_imCameraSeg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                    try {
                        // Cria arquivo com nome dinamico para salvar a imagem da camera
                        arquivoFoto = criarArquivo();
                    } catch (IOException ex) {
                        Toast.makeText(Segmentacao2.this, "Erro ao criar arquivo!!", Toast.LENGTH_LONG).show();
                    }
                    if (arquivoFoto != null) {
                        // Pega o caminho completo gerado de forma dinâmica (na função criarArquivo())
                        Uri photoURI = FileProvider.getUriForFile(getBaseContext(), getBaseContext().getApplicationContext().getPackageName() + ".provider", arquivoFoto);
                        // Insere na intent o caminho para camera saber onde salvar a imagem capturada
                        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                        // Chama a camera recebendo uma resposta (na funcao onActivityResult)
                        startActivityForResult(takePictureIntent, ACTIVITY_CAMERA_REQUEST_CODE);
                    }
                }
            }
        });

        //Recebe imagem da Galeria
        bt_imGaleriaSeg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent, ACTIVITY_GALERIA_REQUEST_CODE);
            }
        });

        //Carrega a imagem de exemplo
        bt_imExemploSeg.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public void onClick(View v) {
                bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.sparse);
                Utils.bitmapToMat(bitmap, rgba);

                imV_galeriaSeg.setImageBitmap(bitmap);
                bt_executarSeg.setEnabled(true);
                ibt_salvarImagem.setEnabled(true);
            }
        });

        //Executa o Watershed na imagem carregada
        bt_executarSeg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                executarWatershed();
            }
        });

        //Botão de desfazer as modificações nos botões
        ibt_desfazerSegmentacao3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                valorThreshoulBinarizacao = 127;
                valorThreshoulDistancia = 0.4;
                sb_ThreshouldBinarizacaoSegmentacao.setProgress(127);
                sb_ThreshouldDistancia.setProgress(40);
                tv_ThreshouldBinarizacaoSegmentacao.setText("127");
                tv_ThreshouldDistancia.setText("40");
            }
        });

        //Desfaz as alterações na imagem
        ibt_desfazerImagem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Utils.matToBitmap(rgba, bitmap);
                imV_galeriaSeg.setImageBitmap(bitmap);
                bt_executarSeg.setEnabled(true);
            }
        });

        //Salva a imagem da tela
        ibt_salvarImagem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FileOutputStream out = null;
                try {
                    arquivoSalvar = criarArquivo();
                    out = new FileOutputStream(arquivoSalvar.getAbsolutePath());
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    try {
                        if (out != null) {
                            out.close();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                Toast.makeText(Segmentacao2.this, "A imagem foi salva com sucesso!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    protected void executarWatershed() {
        //Obtem a RGB e a imagem Gray
        Mat imgGray = new Mat(rgba.width(), rgba.height(), CvType.CV_8UC1);
        Mat mRGB = new Mat();

        // Transforma para RGB e remove a camada Alfa
        Imgproc.cvtColor(rgba, mRGB, Imgproc.COLOR_BGRA2RGB);
        // Transforma para niveis de cinza
        Imgproc.cvtColor(mRGB, imgGray, Imgproc.COLOR_RGB2GRAY);

        // Binarização da imagem em níveis de cinza
        Imgproc.threshold(imgGray, imgGray, valorThreshoulBinarizacao, 255, Imgproc.THRESH_BINARY_INV);

        //Executa o algoritmo de transformacao de distancia
        Mat tempMask = new Mat();
        Imgproc.distanceTransform(imgGray, tempMask, Imgproc.CV_DIST_L2, 3);

        Core.normalize(tempMask, tempMask, 0, 1.0, Core.NORM_MINMAX);//Normaliza o resultado para limiarizar

        //Obtem os picos que serão os marcadores dos objetos, feito por limiarizacao
        Imgproc.threshold(tempMask, tempMask, valorThreshoulDistancia, 1.0, Imgproc.THRESH_BINARY);

        //Encontra os marcadores
        Mat tempMask_8u = new Mat();
        tempMask.convertTo(tempMask_8u, CvType.CV_8U);

        LinkedList<MatOfPoint> contours = new LinkedList<>();
        MatOfInt4 hierarchy = new MatOfInt4();

        hierarchy.convertTo(hierarchy, CvType.CV_32SC1);

        Imgproc.findContours(tempMask_8u, contours, hierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);

        //Desenha os marcadores
        Mat markers = new Mat(mRGB.rows(), mRGB.cols(), CvType.CV_32SC1);

        for (int idx = 0; idx < contours.size(); idx++)
            Imgproc.drawContours(markers, contours, idx, new Scalar(idx + 1), -1);

        Core.circle(markers, new Point(5, 5), 3, new Scalar(255, 255, 255), -1);

        Imgproc.watershed(mRGB, markers);

        //Gera cores aleatorias para pintar as regiões
        Random rnd = new Random();
        LinkedList<double[]> colors = new LinkedList<>();

        for (int i = 0; i < contours.size(); i++)
            colors.addLast(new double[]{
                    rnd.nextInt(226) + 20, // R: [20, 235]
                    rnd.nextInt(226) + 20, // G: [20, 235]
                    rnd.nextInt(226) + 20} // B: [20, 235]
            );

        Mat result = new Mat(markers.size(), CvType.CV_8UC3);

        double[] w = {255, 255, 255};
        double[] b = {0, 0, 0};

        int index;

        //Preenche os objetos com as cores geradas
        for (int i = 0; i < markers.rows(); i++) {
            for (int j = 0; j < markers.cols(); j++) {
                index = (int) markers.get(i, j)[0];

                if (index > 0 && index < (contours.size() + 1)) {
                    // Pinta os objetos
                    result.put(i, j, colors.get(index - 1));
                } else if (index == -1) {
                    // Pinta as bordas
                    result.put(i, j, b);
                } else {
                    // Pinta os objetos desconhecidos
                    result.put(i, j, w);
                }
            }
        }

        // Mostra o resultado
        Utils.matToBitmap(result, bitmap);
        imV_galeriaSeg.setImageBitmap(bitmap);
        // Desativa o botão de executar watershed
        bt_executarSeg.setEnabled(false);
        // Ativa o botão de desfazer imagem
        ibt_desfazerImagem.setEnabled(true);
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == ACTIVITY_GALERIA_REQUEST_CODE) {
            //Retorno da imagem selecionada na galeria
            Uri selectedImage = data.getData();
            String[] filePath = {MediaStore.Images.Media.DATA};
            Cursor c = getContentResolver().query(selectedImage, filePath, null, null, null);
            c.moveToFirst();
            int columnIndex = c.getColumnIndex(filePath[0]);
            String picturePath = c.getString(columnIndex);
            c.close();

            // Recebe a imagem da galeria de forma redimensionada
            recebeRedimensionada(picturePath);
            Utils.bitmapToMat(bitmap, rgba);

            //Mostra na tela
            imV_galeriaSeg.setImageBitmap(bitmap);

            //Permite a Segmentação
            bt_executarSeg.setEnabled(true);
            ibt_salvarImagem.setEnabled(true);
        }
        if (requestCode == ACTIVITY_CAMERA_REQUEST_CODE && resultCode == RESULT_OK) {// Resposta positiva da camera
            // Recebe a imagem capturada da camera de forma redimensionada
            recebeRedimensionada(arquivoFoto.getAbsolutePath());
            Utils.bitmapToMat(bitmap, rgba);

            //Mostra na tela
            imV_galeriaSeg.setImageBitmap(bitmap);

            //Permite a Segmentação
            bt_executarSeg.setEnabled(true);
            ibt_salvarImagem.setEnabled(true);
        }
    }

    // Cria arquivo com nome dinamico para salvar a imagem capturada pela camera
    private File criarArquivo() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        File pasta = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        File imagem = new File(pasta.getPath() + File.separator + "CVApp_" + timeStamp + ".jpg");
        return imagem;
    }

    private void recebeRedimensionada(String caminho) {
        // Carrega imagem de forma redimensionada (para economizar RAM)
        // Recebe a largura e altura do local em que será mostrado
        int targetW = imV_galeriaSeg.getWidth();
        int targetH = imV_galeriaSeg.getHeight();

        BitmapFactory.Options bmOptions = new BitmapFactory.Options();

        // Obtem a largura e altura da foto tirada
        bmOptions.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(caminho, bmOptions);
        int photoW = bmOptions.outWidth;
        int photoH = bmOptions.outHeight;

        // Encontra o fator de escala para que a foto caiba no local que será apresentado na tela
        int scaleFactor = Math.min(photoW / targetW, photoH / targetH);

        // Recebe a imagem com o tamanho redimensionado
        bmOptions.inJustDecodeBounds = false;
        bmOptions.inSampleSize = scaleFactor;

        bitmap = BitmapFactory.decodeFile(caminho, bmOptions);
    }

    //Verifica as permissoes recebidas pelo aplicativo
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        if (requestCode == READ_PERMISSAO_REQUEST) {//Se for permissao de leitura
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                //Toast.makeText(this,"Permissao de leitura autorizada!!",Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(this, "Permissao de leitura não autorizada!!", Toast.LENGTH_LONG).show();
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
        if (requestCode == WRITE_PERMISSAO_REQUEST) {//Se for permissao de escrita
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                //Toast.makeText(this,"Permissao de escrita autorizada!!",Toast.LENGTH_LONG).show();
            } else {
                //Toast.makeText(this,"Permissao de escrita não autorizada!!",Toast.LENGTH_LONG).show();
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }
}
