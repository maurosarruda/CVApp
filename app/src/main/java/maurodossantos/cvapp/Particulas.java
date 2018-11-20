package maurodossantos.cvapp;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import org.opencv.android.Utils;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

public class Particulas extends AppCompatActivity {

    // Variaveis de objetos da interface
    private ImageView imV_particulas;
    private ImageButton ibt_DesfazerParticulas;
    private SeekBar sb_NroParticulas, sb_FPS, sb_FreqAleatoria, sb_DistAleatoria, sb_posicaoXBox, sb_posicaoYBox;
    private TextView tv_NroParticulas, tv_FPS, tv_FreqAleatoria, tv_DistAleatoria, tv_posicaoX, tv_posicaoY;

    // Variaveis de classes externas usadas na execucao do aplicativo
    private Bitmap bitmap;
    private Mat matSaida, rgba, matSaidaCopia;
    private ArrayList<Particula> particulas, particulasNovas;
    private Particula particula;
    private Timer timer;
    private TimerTaskParticulas timerTask;

    // Variaveis de suporte para a execucao
    private int nroParticulas = 50, distAleatoria = 50, freqAleatoria = 1, FPS = 5, posicaoX = 0,
            posicaoY = 0, posicaoAntigaX = 0, posicaoAntigaY = 0, posicaoXEstimada, posicaoYEstimada,
            movimentoX, movimentoY, tamanhoLargura = 0, tamanhoAltura = 0, tamanhoBox, tamanhoParticula,
            maximaDistancia, frameAtual, totalX, totalY, nroParticulasAtuais, passoAleatorioX,
            passoAleatorioY, indice, x, y, i;
    private double[] corBranca = new double[3], corVerde = new double[3], corAzul = new double[3];
    private double pesoMaximo, somaPesos, peso, distancia, escolhaPeso, proporcaoDistancia;
    private boolean criadaInterface = false, alteraMaximoSeekBar = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_particulas);

        // Link com os objetos da tela:
        imV_particulas = (ImageView) findViewById(R.id.imV_particulas);
        ibt_DesfazerParticulas = (ImageButton) findViewById(R.id.ibt_DesfazerParticulas);

        sb_NroParticulas = (SeekBar) findViewById(R.id.sb_NroParticulas);
        sb_FPS = (SeekBar) findViewById(R.id.sb_FPS);
        sb_FreqAleatoria = (SeekBar) findViewById(R.id.sb_FreqAleatoria);
        sb_DistAleatoria = (SeekBar) findViewById(R.id.sb_DistAleatoria);
        sb_posicaoXBox = (SeekBar) findViewById(R.id.sb_posicaoXBox);
        sb_posicaoYBox = (SeekBar) findViewById(R.id.sb_posicaoYBox);

        tv_NroParticulas = (TextView) findViewById(R.id.tv_NroParticulas);
        tv_FPS = (TextView) findViewById(R.id.tv_FPS);
        tv_FreqAleatoria = (TextView) findViewById(R.id.tv_FreqAleatoria);
        tv_DistAleatoria = (TextView) findViewById(R.id.tv_DistAleatoria);
        tv_posicaoX = (TextView) findViewById(R.id.tv_posicaoX);
        tv_posicaoY = (TextView) findViewById(R.id.tv_posicaoY);

        //Acões objetos tela (botoes, seekbar, etc)
        sb_NroParticulas.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                // Atualiza numero de particulas conforme modificacao pelo usuário
                tv_NroParticulas.setText(Integer.toString(progress + 1));
                nroParticulas = progress + 1;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        sb_FPS.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                // Atualiza FPS conforme modificacao pelo usuário
                tv_FPS.setText(Integer.toString(progress + 1));
                FPS = progress + 1;

                // Limpa e reinicia a atualizacao da imagem de tempo em tempo com a taxa alterada
                timer.cancel();
                timerTask.cancel();
                timer = new Timer();
                timerTask = new TimerTaskParticulas();
                timer.scheduleAtFixedRate(timerTask, 0, 1000 / FPS);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        sb_FreqAleatoria.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                // Atualiza frequencia do passo aleatorio conforme modificacao pelo usuário
                tv_FreqAleatoria.setText(Integer.toString(progress));
                freqAleatoria = progress;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        sb_DistAleatoria.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                // Atualiza distancia do passo aleatorio conforme modificacao pelo usuário
                tv_DistAleatoria.setText(Integer.toString(progress));
                distAleatoria = progress;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        sb_posicaoXBox.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (!alteraMaximoSeekBar) {
                    // Atualiza posição x do box
                    posicaoAntigaX = posicaoX;
                    posicaoX = progress;

                    // Calcula quanto foi modificado pela alteração do usuário
                    movimentoX = posicaoX - posicaoAntigaX;

                    // Se a distancia é muito grande ela não é computada para não movimentar muito as particulas
                    if (Math.abs(movimentoX) > distAleatoria)
                        movimentoX = 0;
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        sb_posicaoYBox.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (!alteraMaximoSeekBar) {
                    // Atualiza posição y do box
                    posicaoAntigaY = posicaoY;
                    posicaoY = progress;

                    // Calcula quanto foi modificado pela alteração do usuário
                    movimentoY = posicaoY - posicaoAntigaY;

                    // Se a distancia é muito grande ela não é computada para não movimentar muito as particulas
                    if (Math.abs(movimentoY) > distAleatoria)
                        movimentoY = 0;
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        ibt_DesfazerParticulas.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Reinicia os seekbares com os valores iniciais da aplicacao
                nroParticulas = 50;
                sb_NroParticulas.setProgress(49);
                tv_NroParticulas.setText("50");

                distAleatoria = 50;
                sb_DistAleatoria.setProgress(50);
                tv_DistAleatoria.setText("50");

                FPS = 5;
                sb_FPS.setProgress(4);
                tv_FPS.setText("5");

                freqAleatoria = 1;
                sb_FreqAleatoria.setProgress(1);
                tv_FreqAleatoria.setText("1");
            }
        });
    }

    // Funcao chamada apos a criacao da interface
    @Override
    public void onWindowFocusChanged(boolean focus) {
        super.onWindowFocusChanged(focus);
        if (!criadaInterface) {
            // Cria o Bitmap para ser mostrado na interface
            criarBitmap();
            mostrarSaida(matSaida);

            // Inicializa as variaveis
            inicializaVariaveis();

            // Chama a função para iniciar a atualizacao da imagem na tela com as modificações de tempo em tempo
            timer = new Timer();
            timerTask = new TimerTaskParticulas();
            timer.scheduleAtFixedRate(timerTask, 0, 1000 / FPS);

            // Para executar apenas na criacao da interface
            criadaInterface = true;
        }
    }

    // Funcao para a criacao do Bitmap
    protected void criarBitmap() {
        // Criacao do bitmap a partir de uma imagem
        Bitmap bitmap1 = BitmapFactory.decodeResource(getResources(), R.drawable.tela_preta);

        tamanhoLargura = imV_particulas.getWidth();
        tamanhoAltura = (int) Math.floor((double) bitmap1.getHeight() * ((double) tamanhoLargura / (double) bitmap1.getWidth()));

        // Redimensionamento dinamico dependendo do tamanho da interface do usuario
        bitmap = Bitmap.createScaledBitmap(bitmap1, tamanhoLargura, tamanhoAltura, true);

        rgba = new Mat();
        matSaida = new Mat(rgba.width(), rgba.height(), CvType.CV_8UC3);
        matSaidaCopia = new Mat(rgba.width(), rgba.height(), CvType.CV_8UC3);

        // Armazenamento em matrizes para a modificacao pixel a pixel
        Utils.bitmapToMat(bitmap, rgba);
        Imgproc.cvtColor(rgba, matSaida, Imgproc.COLOR_BGRA2RGB);
        Imgproc.cvtColor(rgba, matSaidaCopia, Imgproc.COLOR_BGRA2RGB);

        // Definicao dos tamanhos da matriz
        tamanhoLargura = matSaida.width();
        tamanhoAltura = matSaida.height();
    }

    // Apresenta na interface uma imagem a partir de uma matriz recebida
    protected void mostrarSaida(Mat mat) {
        //Converte matriz modificada para formato bitmap
        Utils.matToBitmap(mat, bitmap);

        //Mostra imagem na interface
        imV_particulas.setImageBitmap(bitmap);
    }

    // Cria variaveis iniciais para a utilizacao no aplicativo
    protected void inicializaVariaveis() {
        //Inicializa cores que serão usadas
        corBranca[0] = 255;
        corBranca[1] = 255;
        corBranca[2] = 255;

        corAzul[0] = 0;
        corAzul[1] = 0;
        corAzul[2] = 255;

        corVerde[0] = 0;
        corVerde[1] = 255;
        corVerde[2] = 0;

        // Define tamanho do box e particula de forma dinamica dependendo da dimensão da tela
        tamanhoBox = (int) Math.min(tamanhoLargura / 50, tamanhoAltura / 50);
        tamanhoParticula = (int) Math.min(tamanhoLargura / 100, tamanhoAltura / 100);

        // Define a distancia máxima entre dois pontos
        maximaDistancia = (int) Math.floor(Math.sqrt(tamanhoLargura * tamanhoLargura + tamanhoAltura * tamanhoAltura));

        // Define a posicao inicial do box
        posicaoX = (int) Math.floor(Math.random() * tamanhoLargura);
        posicaoY = (int) Math.floor(Math.random() * tamanhoAltura);

        // Inicia outras variaveis que serão usadas no processamento
        movimentoX = 0;
        movimentoY = 0;
        posicaoXEstimada = 0;
        posicaoYEstimada = 0;

        // Inicia valor do frame atual
        frameAtual = -1;

        // Cria o conjunto de particulas
        particulas = new ArrayList<Particula>();
        for (i = 0; i < nroParticulas; i++) {
            particula = new Particula();
            particula.setX((int) Math.floor(Math.random() * tamanhoLargura));
            particula.setY((int) Math.floor(Math.random() * tamanhoAltura));
            particulas.add(i, particula);
        }

        // Define local e limite das posicoes do box nos seekbares
        sb_posicaoXBox.setMax(tamanhoLargura - 1);
        sb_posicaoYBox.setMax(tamanhoAltura - 1);
        alteraMaximoSeekBar = false;
        sb_posicaoXBox.setProgress(posicaoX);
        sb_posicaoYBox.setProgress(posicaoY);
        mostraPosicao();
    }

    // Atualiza imagem na interface
    protected void atualizaImagem() {
        // Atualiza o valor do frame atual
        frameAtual = frameAtual + 1;

        // Executa o algoritmo de filtro de particulas
        particleFilter();

        // Mostra posicao do Box na interface
        mostraPosicao();

        // Limpa matriz de saida
        matSaidaCopia.copyTo(matSaida);

        // Pinta as particulas na imagem
        for (i = 0; i < particulas.size(); i++) {
            pintaQuadrado(particulas.get(i).getX(), particulas.get(i).getY(), corAzul, tamanhoParticula);
        }

        //Pinta o box na imagem
        pintaQuadrado(posicaoX, posicaoY, corBranca, tamanhoBox);

        // Pinta quadrado verde da posicao estimada do box com base nas posicoes das particulas
        pintaQuadradoPredito(posicaoXEstimada, posicaoYEstimada, corVerde, (int) (tamanhoBox * 2.5));

        // Atualiza imagem de saida
        mostrarSaida(matSaida);
    }

    // Apresenta as posicoes do box na interface
    protected void mostraPosicao() {
        tv_posicaoX.setText(Integer.toString(posicaoX));
        tv_posicaoY.setText(Integer.toString(posicaoY));
    }

    // Pinta um quadrado na imagem
    protected void pintaQuadrado(int posX, int posY, double[] cor, int tamanhoQuadrado) {
        for (int i = posY - (tamanhoQuadrado / 2); i < posY + (tamanhoQuadrado / 2); i++) {
            for (int j = posX - (tamanhoQuadrado / 2); j < posX + (tamanhoQuadrado / 2); j++) {
                if (i >= 0 && j >= 0 && i < tamanhoAltura && j < tamanhoLargura) {
                    matSaida.put(i, j, cor);
                }
            }
        }
    }

    //Pinta um quadrado da posicao predita do box na imagem
    protected void pintaQuadradoPredito(int posX, int posY, double[] cor, int tamanhoQuadrado) {
        for (int i = posY - (tamanhoQuadrado / 2); i < posY + (tamanhoQuadrado / 2); i++) {
            for (int j = posX - (tamanhoQuadrado / 2); j < posX + (tamanhoQuadrado / 2); j++) {
                if (i >= 0 && j >= 0 && i < tamanhoAltura && j < tamanhoLargura) {
                    if (i <= posY - (tamanhoQuadrado / 2) + 2 || j <= posX - (tamanhoQuadrado / 2) + 2 || i >= posY + (tamanhoQuadrado / 2) - 3 || j >= posX + (tamanhoQuadrado / 2) - 3)
                        matSaida.put(i, j, cor);
                }
            }
        }
    }

    // Algoritmo principal do filtro de particulas
    protected void particleFilter() {

        // Numero de particulas atuais
        nroParticulasAtuais = particulas.size();

        // Calcula a posicao estimada do box com base nas posicoes das particulas
        totalX = 0;
        totalY = 0;
        for (i = 0; i < nroParticulasAtuais; i++) {
            totalX = totalX + particulas.get(i).getX();
            totalY = totalY + particulas.get(i).getY();
        }
        posicaoXEstimada = (int) Math.floor(totalX / nroParticulasAtuais);
        posicaoYEstimada = (int) Math.floor(totalY / nroParticulasAtuais);

        // Se houve modificacao de posicao do box, as particulas sao atualizadas pela mesma quantidade
        if (movimentoX != 0 || movimentoY != 0) {
            for (i = 0; i < nroParticulasAtuais; i++) {
                x = particulas.get(i).getX();
                y = particulas.get(i).getY();

                // Limita as posicoes das particulas no limite da imagem
                if ((x + movimentoX >= 0) && (x + movimentoX < tamanhoLargura)) {
                    particulas.get(i).setX(x + movimentoX);
                } else if (x + movimentoX < 0) {
                    particulas.get(i).setX(0);
                } else if (x + movimentoX >= tamanhoLargura) {
                    particulas.get(i).setX(tamanhoLargura - 1);
                }

                if (y + movimentoY >= 0 && y + movimentoY < tamanhoAltura) {
                    particulas.get(i).setY(y + movimentoY);
                } else if (y + movimentoY < 0) {
                    particulas.get(i).setY(0);
                } else if (y + movimentoY >= tamanhoAltura) {
                    particulas.get(i).setY(tamanhoAltura - 1);
                }
            }
        }

        // Realiza a movimentacao aleatoria se for um frame aleatorio
        if (freqAleatoria != 0 && (frameAtual % freqAleatoria) == 0) {
            for (i = 0; i < nroParticulasAtuais; i++) {
                // Define um passo aleatorio com base a distancia da caminhada aleatoria
                passoAleatorioX = (int) Math.floor(Math.random() * (distAleatoria + 1)) - distAleatoria / 2;
                passoAleatorioY = (int) Math.floor(Math.random() * (distAleatoria + 1)) - distAleatoria / 2;

                x = particulas.get(i).getX();
                y = particulas.get(i).getY();

                // Modifica a posicao das particulas pelo passo aleatorio, com base nos limites da imagem
                if ((x + passoAleatorioX >= 0) && (x + passoAleatorioX < tamanhoLargura)) {
                    particulas.get(i).setX(x + passoAleatorioX);
                } else if (x + passoAleatorioX < 0) {
                    particulas.get(i).setX(0);
                } else if (x + passoAleatorioX >= tamanhoLargura) {
                    particulas.get(i).setX(tamanhoLargura - 1);
                }

                if (y + passoAleatorioY >= 0 && y + passoAleatorioY < tamanhoAltura) {
                    particulas.get(i).setY(y + passoAleatorioY);
                } else if (y + passoAleatorioY < 0) {
                    particulas.get(i).setY(0);
                } else if (y + passoAleatorioY >= tamanhoAltura) {
                    particulas.get(i).setY(tamanhoAltura - 1);
                }
            }
        }

        // Calcula o peso da particula com base na distancia euclidiana entre cada particula e o box
        pesoMaximo = 0;
        for (i = 0; i < nroParticulasAtuais; i++) {
            // Calcula a distancia euclidiana entre cada particula e o box
            distancia = calculaDistancia(posicaoX, posicaoY, particulas.get(i).getX(), particulas.get(i).getY());

            // Obtem o novo peso baseado a proporcao da distancia pela distancia maxima
            proporcaoDistancia = distancia / maximaDistancia;
            proporcaoDistancia = 1 - proporcaoDistancia;

            peso = particulas.get(i).getPeso();
            peso = peso * proporcaoDistancia;
            particulas.get(i).setPeso(peso);

            // Define o peso maximo para a normalizacao
            if (peso > pesoMaximo)
                pesoMaximo = peso;
        }

        // Normaliza os pesos com base no peso maximo
        somaPesos = 0;
        for (i = 0; i < nroParticulasAtuais; i++) {
            // Normaliza os pesos
            if (pesoMaximo != 0) {
                peso = particulas.get(i).getPeso();
                peso = peso / pesoMaximo;
                particulas.get(i).setPeso(peso);
            }
            // Soma os pesos para a obtencao das novas particulas
            somaPesos = somaPesos + particulas.get(i).getPeso();
        }

        // Realiza a reamostragem das particulas considerando os pesos das particulas
        particulasNovas = new ArrayList<Particula>();
        nroParticulasAtuais = nroParticulas;
        for (i = 0; i < nroParticulasAtuais; i++) {
            // Computa um valor aleatorio com base na soma dos peso,
            // assim, as particulas com maior peso terão maior probabilidade de serem escolhidos
            escolhaPeso = Math.random() * somaPesos;
            indice = -1;
            do {
                indice++;
                escolhaPeso = escolhaPeso - particulas.get(indice).getPeso();
            } while (escolhaPeso > 0);

            // Salva a particula escolhida como nova
            particula = new Particula();
            particula.setX(particulas.get(indice).getX());
            particula.setY(particulas.get(indice).getY());
            particula.setPeso(particulas.get(indice).getPeso());
            particulasNovas.add(i, particula);
        }
        particulas = particulasNovas;

        // Limpa os valores movimentados para a próxima execucao
        movimentoX = 0;
        movimentoY = 0;
    }

    // Retorna a distancia euclidiana de dois pontos
    protected double calculaDistancia(int x1, int y1, int x2, int y2) {
        return Math.floor(Math.sqrt((x1 - x2) * (x1 - x2) + (y1 - y2) * (y1 - y2)));
    }

    // Classe para chamar a funcao de tempo em tempo
    class TimerTaskParticulas extends TimerTask {
        @Override
        public void run() {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    atualizaImagem();
                }
            });
        }
    }
}

// Classe das Particula que é usada para armazenar as informacoes de cada particula
class Particula {
    // Variaveis de cada particula
    private int x = 0;
    private int y = 0;
    private double peso = 1.0;

    // Seta o valor de X
    public void setX(int x) {
        this.x = x;
    }

    // Retorna o valor de X
    public int getX() {
        return this.x;
    }

    // Seta o valor de Y
    public void setY(int y) {
        this.y = y;
    }

    // Retorna o valor de Y
    public int getY() {
        return this.y;
    }

    // Seta o peso de uma particula
    public void setPeso(double peso) {
        this.peso = peso;
    }

    // Retorna o peso da particula
    public double getPeso() {
        return this.peso;
    }
}
