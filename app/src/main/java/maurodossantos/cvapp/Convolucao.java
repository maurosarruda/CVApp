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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
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

public class Convolucao extends AppCompatActivity {

    private static final int ACTIVITY_CAMERA_REQUEST_CODE = 0;
    private static final int ACTIVITY_GALERIA_REQUEST_CODE = 1;
    private static final int WRITE_PERMISSAO_REQUEST = 2;
    private static final int READ_PERMISSAO_REQUEST = 3;

    private File arquivoFoto = null, arquivoSalvar = null;

    private Button bt_imGaleria, bt_imExemplo, bt_imCamera, bt_executarConv;
    private ImageView imv_entrada, imv_saida;
    private EditText ed00, ed01, ed02, ed10, ed11, ed12, ed20, ed21, ed22;
    private TextView tv00, tv02, tv04, tv10, tv12, tv14, tv30, tv32, tv34, tv40, tv42, tv44, tv60, tv62, tv64, tv70, tv72, tv74, tv92;
    private Spinner sp_kernel;
    private SeekBar sb_posicaoX, sb_posicaoY;
    private TextView tv_seekX, tv_seekY;
    private ImageButton ibt_salvarImagemConvolucao;

    private Bitmap bitmap, bitmapSaidaRGB;
    private Mat rgba, imgGray, aux, matSaida, matSaidaRGB;
    private double[][] kernel = new double[3][3];

    private int i, j, row, col, posicaoX = 0, posicaoY = 0, fator;
    private double[] corRGB = new double[3], corBorda = new double[1], valor = new double[1], valor2 = new double[1];
    private String campo;
    private boolean erro = true;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_convolucao);

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
        imv_entrada = (ImageView) findViewById(R.id.imv_entrada);
        imv_saida = (ImageView) findViewById(R.id.imv_saida);

        bt_imGaleria = (Button) findViewById(R.id.bt_imGaleria);
        bt_imCamera = (Button) findViewById(R.id.bt_imCamera);
        bt_imExemplo = (Button) findViewById(R.id.bt_imExemplo);
        bt_executarConv = (Button) findViewById(R.id.bt_executarConv);

        ed00 = (EditText) findViewById(R.id.ed00);
        ed01 = (EditText) findViewById(R.id.ed01);
        ed02 = (EditText) findViewById(R.id.ed02);
        ed10 = (EditText) findViewById(R.id.ed10);
        ed11 = (EditText) findViewById(R.id.ed11);
        ed12 = (EditText) findViewById(R.id.ed12);
        ed20 = (EditText) findViewById(R.id.ed20);
        ed21 = (EditText) findViewById(R.id.ed21);
        ed22 = (EditText) findViewById(R.id.ed22);

        tv00 = (TextView) findViewById(R.id.tv00);
        tv02 = (TextView) findViewById(R.id.tv02);
        tv04 = (TextView) findViewById(R.id.tv04);
        tv10 = (TextView) findViewById(R.id.tv10);
        tv12 = (TextView) findViewById(R.id.tv12);
        tv14 = (TextView) findViewById(R.id.tv14);
        tv30 = (TextView) findViewById(R.id.tv30);
        tv32 = (TextView) findViewById(R.id.tv32);
        tv34 = (TextView) findViewById(R.id.tv34);
        tv40 = (TextView) findViewById(R.id.tv40);
        tv42 = (TextView) findViewById(R.id.tv42);
        tv44 = (TextView) findViewById(R.id.tv44);
        tv60 = (TextView) findViewById(R.id.tv60);
        tv62 = (TextView) findViewById(R.id.tv62);
        tv64 = (TextView) findViewById(R.id.tv64);
        tv70 = (TextView) findViewById(R.id.tv70);
        tv72 = (TextView) findViewById(R.id.tv72);
        tv74 = (TextView) findViewById(R.id.tv74);
        tv92 = (TextView) findViewById(R.id.tv92);

        sb_posicaoX = (SeekBar) findViewById(R.id.sb_posicaoX);
        sb_posicaoY = (SeekBar) findViewById(R.id.sb_posicaoY);
        tv_seekX = (TextView) findViewById(R.id.tv_seekX);
        tv_seekY = (TextView) findViewById(R.id.tv_seekY);

        ibt_salvarImagemConvolucao = (ImageButton) findViewById(R.id.ibt_salvarImagemConvolucao);

        sp_kernel = (Spinner) findViewById(R.id.sp_kernel);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.kernels, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sp_kernel.setAdapter(adapter);

        //Desativa Botão Convolucao
        bt_executarConv.setEnabled(false);

        //Desativa Botoes Tela
        desativaBotoesTela();

        //Acões objetos tela (botoes, seekbar, etc)
        ibt_salvarImagemConvolucao.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FileOutputStream out = null;
                try {
                    arquivoSalvar = criarArquivo();
                    out = new FileOutputStream(arquivoSalvar.getAbsolutePath());

                    //Recupera imagem limpa (sem quadrado vermelho) para salvar
                    Imgproc.cvtColor(matSaida, matSaidaRGB, Imgproc.COLOR_GRAY2RGB);
                    Utils.matToBitmap(matSaidaRGB, bitmapSaidaRGB);

                    bitmapSaidaRGB.compress(Bitmap.CompressFormat.PNG, 100, out);
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
                Toast.makeText(Convolucao.this, "A imagem foi salva com sucesso!", Toast.LENGTH_SHORT).show();

                //Imprime novamente quadrado vermelho, depois de salvar
                imprimeQuadradoVermelho();
            }
        });

        sb_posicaoX.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

                //Recebe novo progresso
                tv_seekX.setText(Integer.toString(progress));
                posicaoX = progress;

                //Atualiza Resultados na interface
                atualizaResultados();

                //Atualiza posicao quadrado
                imprimeQuadradoVermelho();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        sb_posicaoY.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

                //Recebe novo progresso
                tv_seekY.setText(Integer.toString(progress));
                posicaoY = progress;

                //Atualiza Resultados na interface
                atualizaResultados();

                //Atualiza posicao quadrado
                imprimeQuadradoVermelho();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        sp_kernel.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                //Recebe kernel escolhido no spinner
                String kernelEscolhido = parent.getItemAtPosition(position).toString();

                //Atualiza informacoes do kernel na tela
                if (kernelEscolhido.equals("blur")) {
                    ed00.setText("0.0625");
                    ed01.setText("0.125");
                    ed02.setText("0.0625");
                    ed10.setText("0.125");
                    ed11.setText("0.25");
                    ed12.setText("0.125");
                    ed20.setText("0.0625");
                    ed21.setText("0.125");
                    ed22.setText("0.0625");
                } else if (kernelEscolhido.equals("bottom sobel")) {
                    ed00.setText("-1");
                    ed01.setText("-2");
                    ed02.setText("-1");
                    ed10.setText("0");
                    ed11.setText("0");
                    ed12.setText("0");
                    ed20.setText("1");
                    ed21.setText("2");
                    ed22.setText("1");
                } else if (kernelEscolhido.equals("emboss")) {
                    ed00.setText("-2");
                    ed01.setText("-1");
                    ed02.setText("0");
                    ed10.setText("-1");
                    ed11.setText("1");
                    ed12.setText("1");
                    ed20.setText("0");
                    ed21.setText("1");
                    ed22.setText("2");
                } else if (kernelEscolhido.equals("identity")) {
                    ed00.setText("0");
                    ed01.setText("0");
                    ed02.setText("0");
                    ed10.setText("0");
                    ed11.setText("1");
                    ed12.setText("0");
                    ed20.setText("0");
                    ed21.setText("0");
                    ed22.setText("0");
                } else if (kernelEscolhido.equals("left sobel")) {
                    ed00.setText("1");
                    ed01.setText("0");
                    ed02.setText("-1");
                    ed10.setText("2");
                    ed11.setText("0");
                    ed12.setText("-2");
                    ed20.setText("1");
                    ed21.setText("0");
                    ed22.setText("-1");
                } else if (kernelEscolhido.equals("outline")) {
                    ed00.setText("-1");
                    ed01.setText("-1");
                    ed02.setText("-1");
                    ed10.setText("-1");
                    ed11.setText("8");
                    ed12.setText("-1");
                    ed20.setText("-1");
                    ed21.setText("-1");
                    ed22.setText("-1");
                } else if (kernelEscolhido.equals("right sobel")) {
                    ed00.setText("-1");
                    ed01.setText("0");
                    ed02.setText("1");
                    ed10.setText("-2");
                    ed11.setText("0");
                    ed12.setText("2");
                    ed20.setText("-1");
                    ed21.setText("0");
                    ed22.setText("1");
                } else if (kernelEscolhido.equals("sharpen")) {
                    ed00.setText("0");
                    ed01.setText("-1");
                    ed02.setText("0");
                    ed10.setText("-1");
                    ed11.setText("5");
                    ed12.setText("-1");
                    ed20.setText("0");
                    ed21.setText("-1");
                    ed22.setText("0");
                } else if (kernelEscolhido.equals("top sobel")) {
                    ed00.setText("1");
                    ed01.setText("2");
                    ed02.setText("1");
                    ed10.setText("0");
                    ed11.setText("0");
                    ed12.setText("0");
                    ed20.setText("-1");
                    ed21.setText("-2");
                    ed22.setText("-1");
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

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
                        Toast.makeText(Convolucao.this, "Erro ao criar arquivo!!", Toast.LENGTH_LONG).show();
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
                bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.campogrande);//campogrande

                // Binariza a imagem recebida
                binarizarBitmap();
            }
        });

        bt_executarConv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Verifica Kernel
                if (verificaKernel()) {
                    //Le kernel
                    leKernel();

                    //Atualiza informacoes do kernel nos resultados
                    atualizaKernelResultados();

                    //executa a convolucao
                    executaConvolucao();

                    //Ativa seekbar
                    ativaSeekbar();

                    //Ativa botão para salvar imagem de resultado
                    ibt_salvarImagemConvolucao.setEnabled(true);

                    //Atualiza Resultados na interface
                    atualizaResultados();

                    //Imprime posica analisada na imagem
                    imprimeQuadradoVermelho();
                }

            }
        });

    }

    protected void ativaSeekbar() {
        //Atualiza tamanho do seekbar
        sb_posicaoX.setMax(matSaida.cols() - 1);
        sb_posicaoY.setMax(matSaida.rows() - 1);

        //ativa seekbar
        sb_posicaoX.setEnabled(true);
        sb_posicaoY.setEnabled(true);
    }

    protected void imprimeQuadradoVermelho() {
        //Define cor que será pintada na imagem
        corRGB[0] = 255;
        corRGB[1] = 0;
        corRGB[2] = 0;

        //Recebe imagem limpa em formato RGB
        Imgproc.cvtColor(matSaida, matSaidaRGB, Imgproc.COLOR_GRAY2RGB);

        //Define tamanho do quadrado de forma dinamica
        fator = (int) Math.min(matSaidaRGB.rows() / 50, matSaidaRGB.cols() / 50);
        fator = (int) fator / 2;

        //Pinta quadrado na imagem
        for (i = posicaoY - fator; i < posicaoY + fator; i++) {
            for (j = posicaoX - fator; j < posicaoX + fator; j++) {
                if (i >= 0 && j >= 0 && i < matSaidaRGB.rows() && j < matSaidaRGB.cols()) {
                    matSaidaRGB.put(i, j, corRGB);
                }
            }
        }

        //Atualiza imagem de saida
        mostrarSaida(matSaidaRGB);
    }

    protected void mostrarSaida(Mat mat) {
        //Converte matriz modificada para formato bitmap
        Utils.matToBitmap(mat, bitmapSaidaRGB);

        //Mostra imagem na interface
        imv_saida.setImageBitmap(bitmapSaidaRGB);
    }

    protected void atualizaResultados() {
        //Atualiza campos da inteface
        tv00.setText(String.valueOf(aux.get(posicaoY + 0, posicaoX + 0)[0]));
        tv02.setText(String.valueOf(aux.get(posicaoY + 0, posicaoX + 1)[0]));
        tv04.setText(String.valueOf(aux.get(posicaoY + 0, posicaoX + 2)[0]));
        tv30.setText(String.valueOf(aux.get(posicaoY + 1, posicaoX + 0)[0]));
        tv32.setText(String.valueOf(aux.get(posicaoY + 1, posicaoX + 1)[0]));
        tv34.setText(String.valueOf(aux.get(posicaoY + 1, posicaoX + 2)[0]));
        tv60.setText(String.valueOf(aux.get(posicaoY + 2, posicaoX + 0)[0]));
        tv62.setText(String.valueOf(aux.get(posicaoY + 2, posicaoX + 1)[0]));
        tv64.setText(String.valueOf(aux.get(posicaoY + 2, posicaoX + 2)[0]));

        //Computa valor total, pois a imagem salva aplica corte em 0 e 255
        valor[0] = 0;
        for (i = 0; i < 3; i++) {
            for (j = 0; j < 3; j++) {
                valor[0] = valor[0] + (aux.get(posicaoY + i, posicaoX + j)[0] * kernel[i][j]);
            }
        }
        //Mostra valor total
        tv92.setText(String.valueOf(valor[0]));
    }


    protected void atualizaKernelResultados() {
        //Atualiza informacoes do kernel nos resultados
        tv10.setText("x" + ed00.getText().toString());
        tv12.setText("x" + ed01.getText().toString());
        tv14.setText("x" + ed02.getText().toString());
        tv40.setText("x" + ed10.getText().toString());
        tv42.setText("x" + ed11.getText().toString());
        tv44.setText("x" + ed12.getText().toString());
        tv70.setText("x" + ed20.getText().toString());
        tv72.setText("x" + ed21.getText().toString());
        tv74.setText("x" + ed22.getText().toString());
    }

    protected boolean verificaKernel() {

        erro = true;

        //Verifica todos os campos para detectar entrada incorreta
        campo = ed00.getText().toString();
        if (campo.isEmpty() || campo.equals("") || campo.equals("-") || campo.equals(".") || campo.equals("-.")) {
            ed00.setError("Preencher campo");
            erro = false;
        }

        campo = ed01.getText().toString();
        if (campo.isEmpty() || campo.equals("") || campo.equals("-") || campo.equals(".") || campo.equals("-.")) {
            ed01.setError("Preencher campo");
            erro = false;
        }

        campo = ed02.getText().toString();
        if (campo.isEmpty() || campo.equals("") || campo.equals("-") || campo.equals(".") || campo.equals("-.")) {
            ed02.setError("Preencher campo");
            erro = false;
        }

        campo = ed10.getText().toString();
        if (campo.isEmpty() || campo.equals("") || campo.equals("-") || campo.equals(".") || campo.equals("-.")) {
            ed10.setError("Preencher campo");
            erro = false;
        }

        campo = ed11.getText().toString();
        if (campo.isEmpty() || campo.equals("") || campo.equals("-") || campo.equals(".") || campo.equals("-.")) {
            ed11.setError("Preencher campo");
            erro = false;
        }

        campo = ed12.getText().toString();
        if (campo.isEmpty() || campo.equals("") || campo.equals("-") || campo.equals(".") || campo.equals("-.")) {
            ed12.setError("Preencher campo");
            erro = false;
        }

        campo = ed20.getText().toString();
        if (campo.isEmpty() || campo.equals("") || campo.equals("-") || campo.equals(".") || campo.equals("-.")) {
            ed20.setError("Preencher campo");
            erro = false;
        }

        campo = ed21.getText().toString();
        if (campo.isEmpty() || campo.equals("") || campo.equals("-") || campo.equals(".") || campo.equals("-.")) {
            ed21.setError("Preencher campo");
            erro = false;
        }

        campo = ed22.getText().toString();
        if (campo.isEmpty() || campo.equals("") || campo.equals("-") || campo.equals(".") || campo.equals("-.")) {
            ed22.setError("Preencher campo");
            erro = false;
        }

        return erro;
    }

    protected void leKernel() {
        //Le o kernel da interface
        kernel[0][0] = Double.parseDouble(ed00.getText().toString());
        kernel[0][1] = Double.parseDouble(ed01.getText().toString());
        kernel[0][2] = Double.parseDouble(ed02.getText().toString());
        kernel[1][0] = Double.parseDouble(ed10.getText().toString());
        kernel[1][1] = Double.parseDouble(ed11.getText().toString());
        kernel[1][2] = Double.parseDouble(ed12.getText().toString());
        kernel[2][0] = Double.parseDouble(ed20.getText().toString());
        kernel[2][1] = Double.parseDouble(ed21.getText().toString());
        kernel[2][2] = Double.parseDouble(ed22.getText().toString());
    }

    protected void executaConvolucao() {

        //Cria matriz intermediaria com preenchimento ao redor da imagem
        corBorda[0] = 0.00;
        aux = new Mat(imgGray.rows() + 2, imgGray.cols() + 2, CvType.CV_8UC1);
        for (row = 0; row < aux.rows(); row++) {
            for (col = 0; col < aux.cols(); col++) {
                if (row == 0 || col == 0 || row == aux.rows() - 1 || col == aux.cols() - 1) {
                    aux.put(row, col, corBorda);
                } else {
                    aux.put(row, col, imgGray.get(row - 1, col - 1));
                }
            }
        }

        //Computa matriz de saida da convolucao
        matSaida = new Mat(imgGray.rows(), imgGray.cols(), CvType.CV_8UC1);
        for (row = 0; row < imgGray.rows(); row++) {
            for (col = 0; col < imgGray.cols(); col++) {
                valor[0] = 0.00;
                for (i = 0; i < 3; i++) {
                    for (j = 0; j < 3; j++) {
                        valor2 = aux.get(row + i, col + j);
                        valor[0] = valor[0] + (valor2[0] * kernel[i][j]);
                    }
                }

                if (valor[0] > 255) {
                    valor[0] = 255;
                } else if (valor[0] < 0) {
                    valor[0] = 0;
                }

                matSaida.put(row, col, valor);
            }
        }

        //Converte matriz para RGB (para pintar de vermelho)
        bitmapSaidaRGB = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);
        matSaidaRGB = new Mat(imgGray.rows(), imgGray.cols(), CvType.CV_8UC3);
        Imgproc.cvtColor(matSaida, matSaidaRGB, Imgproc.COLOR_GRAY2RGB);

        //Mostra a imagem final na interface
        mostrarSaida(matSaidaRGB);
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
            //Envia Broadcast para avisar que a imagem foi salva
            sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(arquivoFoto)));

            // Recebe a imagem capturada da camera de forma redimensionada
            recebeRedimensionada(arquivoFoto.getAbsolutePath());

            // Binariza a imagem recebida
            binarizarBitmap();
        }
    }

    private void recebeRedimensionada(String caminho) {
        // Carrega imagem de forma redimensionada (para economizar RAM)
        // Recebe a largura e altura do local em que será mostrado
        int targetW = imv_entrada.getWidth();
        int targetH = imv_entrada.getHeight();

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

        imgGray = new Mat(rgba.width(), rgba.height(), CvType.CV_8UC1);
        Mat mRGB = new Mat();

        // Transforma para RGB e remove a camada Alfa
        Imgproc.cvtColor(rgba, mRGB, Imgproc.COLOR_BGRA2RGB);
        // Transforma para niveis de cinza
        Imgproc.cvtColor(mRGB, imgGray, Imgproc.COLOR_RGB2GRAY);

        Utils.matToBitmap(imgGray, bitmap);

        // Mostra a imagem na interface
        imv_entrada.setImageBitmap(bitmap);

        //Reseta objetos na interface
        resetaObjetosTela();
    }

    protected void resetaObjetosTela() {
        // Ativa botão de execução
        bt_executarConv.setEnabled(true);

        // Reseta SeekBar
        sb_posicaoX.setProgress(0);
        sb_posicaoY.setProgress(0);

        // Limpa imagem Saida
        imv_saida.setImageBitmap(null);

        //Desativa Botoes Tela
        desativaBotoesTela();

        //Reseta resultados
        resetaResultados();
    }

    protected void resetaResultados() {
        //Zera informacoes resultados
        tv00.setText("000");
        tv02.setText("000");
        tv04.setText("000");
        tv30.setText("000");
        tv32.setText("000");
        tv34.setText("000");
        tv60.setText("000");
        tv62.setText("000");
        tv64.setText("000");

        tv10.setText("x0");
        tv12.setText("x0");
        tv14.setText("x0");
        tv40.setText("x0");
        tv42.setText("x0");
        tv44.setText("x0");
        tv70.setText("x0");
        tv72.setText("x0");
        tv74.setText("x0");

        tv92.setText("000");
    }

    protected void desativaBotoesTela() {
        //Desativa SeekBar
        sb_posicaoX.setEnabled(false);
        sb_posicaoY.setEnabled(false);

        // Desativa salvar resultado
        ibt_salvarImagemConvolucao.setEnabled(false);
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
