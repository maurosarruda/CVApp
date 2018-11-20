package maurodossantos.cvapp;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import org.opencv.android.Utils;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Morfologia extends AppCompatActivity {

    private static final int ACTIVITY_CAMERA_REQUEST_CODE = 0;
    private static final int ACTIVITY_GALERIA_REQUEST_CODE = 1;
    private static final int WRITE_PERMISSAO_REQUEST = 2;
    private static final int READ_PERMISSAO_REQUEST = 3;

    private File arquivoFoto = null, arquivoSalvar = null;

    private ImageView imV_galeria;
    private Button bt_imGaleria, bt_imExemplo, bt_imCamera;
    private Button bt_Erode, bt_Dilate, bt_Open, bt_Close;
    private ImageButton ibt_salvarImagemMorfologia;

    private Bitmap bitmap;
    private Mat rgba, imgGray;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_morfologia);

        // Permissões do aplicativo:
        // Solicita que o usuario autorize a leitura da galeira pelo app
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
        imV_galeria = (ImageView) findViewById(R.id.imV_galeria);
        bt_imGaleria = (Button) findViewById(R.id.bt_imGaleria);
        bt_imCamera = (Button) findViewById(R.id.bt_imCamera);
        bt_imExemplo = (Button) findViewById(R.id.bt_imExemplo);
        bt_Erode = (Button) findViewById(R.id.bt_Erode);
        bt_Dilate = (Button) findViewById(R.id.bt_Dilate);
        bt_Open = (Button) findViewById(R.id.bt_Open);
        bt_Close = (Button) findViewById(R.id.bt_Close);
        ibt_salvarImagemMorfologia = (ImageButton) findViewById(R.id.ibt_salvarImagemMorfologia);

        // Desativa os botões para que a imagem seja selecionada primeiro
        desativaBotoes();

        // Execução dos botões para a seleção da imagem com a camera, galeria ou imagem de exemplo:
        bt_imCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                    try {
                        // Cria arquivo com nome dinamico para salvar a imagem da camera
                        arquivoFoto = criarArquivo();
                    } catch (IOException ex) {
                        Toast.makeText(Morfologia.this, "Erro ao criar arquivo!!", Toast.LENGTH_LONG).show();
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

        bt_imGaleria.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Chama a Galeria recebendo uma resposta (na funcao onActivityResult)
                Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent, ACTIVITY_GALERIA_REQUEST_CODE);
            }
        });

        bt_imExemplo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Carrega a imagem de exemplo da UCDB
                bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.ucdb);

                // Binariza a imagem recebida
                binarizarBitmap();
            }
        });

        // Execução dos quatro botões das operações morfológicas:
        bt_Erode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Executa a Erosão
                Utils.bitmapToMat(bitmap, rgba);
                Mat erodeImage = new Mat();
                Imgproc.dilate(rgba, erodeImage, new Mat());

                // Mostra a imagem na interface
                mostraImagemInterface(erodeImage);
            }
        });

        bt_Dilate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Executa a Dilatação
                Utils.bitmapToMat(bitmap, rgba);
                Mat dilateImage = new Mat();
                Imgproc.erode(rgba, dilateImage, new Mat());

                // Mostra a imagem na interface
                mostraImagemInterface(dilateImage);
            }
        });

        bt_Open.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Executa a Abertura
                Utils.bitmapToMat(bitmap, rgba);
                Mat openImage = new Mat();
                Imgproc.dilate(rgba, openImage, new Mat());
                Imgproc.erode(openImage, openImage, new Mat());

                // Mostra a imagem na interface
                mostraImagemInterface(openImage);
            }
        });

        bt_Close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Executa o Fechamento
                Utils.bitmapToMat(bitmap, rgba);
                Mat closeImage = new Mat();
                Imgproc.erode(rgba, closeImage, new Mat());
                Imgproc.dilate(closeImage, closeImage, new Mat());

                // Mostra a imagem na interface
                mostraImagemInterface(closeImage);
            }
        });

        ibt_salvarImagemMorfologia.setOnClickListener(new View.OnClickListener() {
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
                Toast.makeText(Morfologia.this, "A imagem foi salva com sucesso!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Processo executado para a resposta da camera ou galeria (Externo ao aplicativo)
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == ACTIVITY_GALERIA_REQUEST_CODE && resultCode == RESULT_OK) {// Resposta positiva da galeria
            //Carrega imagem selecionada da galeria
            Uri selectedImage = data.getData();
            String[] filePath = {MediaStore.Images.Media.DATA};
            Cursor c = getContentResolver().query(selectedImage, filePath, null, null, null);
            c.moveToFirst();
            int columnIndex = c.getColumnIndex(filePath[0]);
            String picturePath = c.getString(columnIndex);
            c.close();

            // Recebe a imagem capturada da galeria de forma redimensionada
            recebeRedimensionada(picturePath);

            // Binariza a imagem recebida
            binarizarBitmap();
        }

        if (requestCode == ACTIVITY_CAMERA_REQUEST_CODE && resultCode == RESULT_OK) {// Resposta positiva da camera
            // Recebe a imagem capturada da camera de forma redimensionada
            recebeRedimensionada(arquivoFoto.getAbsolutePath());

            // Binariza a imagem recebida
            binarizarBitmap();
        }
    }

    private void recebeRedimensionada(String caminho) {
        // Carrega imagem de forma redimensionada (para economizar RAM)
        // Recebe a largura e altura do local em que será mostrado
        int targetW = imV_galeria.getWidth();
        int targetH = imV_galeria.getHeight();

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

    // Binariza uma imagem salva na variavel bitmap
    private void binarizarBitmap() {
        rgba = new Mat();
        Utils.bitmapToMat(bitmap, rgba);

        // Transformação da imagem para níveis de cinza
        imgGray = new Mat(rgba.width(), rgba.height(), CvType.CV_8UC1);
        Imgproc.cvtColor(rgba, imgGray, Imgproc.COLOR_RGB2GRAY);

        // Binarização da imagem em níveis de cinza
        Imgproc.threshold(imgGray, imgGray, 127, 255, Imgproc.THRESH_BINARY);

        // Mostra a imagem na interface
        mostraImagemInterface(imgGray);

        // Ativa botões para permitir operações na imagem
        ativaBotoes();
    }

    private void ativaBotoes() {
        // Ativa os botões para permitir as operações
        bt_Erode.setEnabled(true);
        bt_Dilate.setEnabled(true);
        bt_Open.setEnabled(true);
        bt_Close.setEnabled(true);
        ibt_salvarImagemMorfologia.setEnabled(true);
    }

    private void desativaBotoes() {
        // Desativa os botões para não permitir as operações enquanto a imagem não for carregada
        bt_Erode.setEnabled(false);
        bt_Dilate.setEnabled(false);
        bt_Open.setEnabled(false);
        bt_Close.setEnabled(false);
        ibt_salvarImagemMorfologia.setEnabled(false);
    }

    private void mostraImagemInterface(Mat matEntrada) {
        // Transforma para Bitmap
        Utils.matToBitmap(matEntrada, bitmap);

        // Mostra a imagem na tela do celular
        imV_galeria.setImageBitmap(bitmap);
    }

    // Cria arquivo com nome dinamico para salvar a imagem capturada pela camera
    private File criarArquivo() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        File pasta = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        File imagem = new File(pasta.getPath() + File.separator + "CVApp_" + timeStamp + ".jpg");
        return imagem;
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
