package maurodossantos.cvapp;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import org.opencv.android.OpenCVLoader;

public class MainActivity extends AppCompatActivity {

    private ImageButton ibtn_Fechar, ibtn_Seg, ibtn_Morf, ibtn_Conv, ibtn_Filtro;

    private static final String TAG = "MainActivity";

    private static final int CAMERA_PERMISSAO_REQUEST = 5;

    static {
        //Verificação Conexão OPENCV
        if (!OpenCVLoader.initDebug()) {
            Log.d(TAG, "ERRO");
        } else {
            Log.d(TAG, "OK");
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Solicita que o usuario autorize a escrita pelo app
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA)) {
                Toast.makeText(this, "Permissao para usar a camera", Toast.LENGTH_LONG).show();
            }
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSAO_REQUEST);
        }

        // Link com os objetos da tela:
        ibtn_Seg = (ImageButton) findViewById(R.id.ibtn_Seg);
        ibtn_Morf = (ImageButton) findViewById(R.id.ibtn_Morf);
        ibtn_Conv = (ImageButton) findViewById(R.id.ibtn_Conv);
        ibtn_Fechar = (ImageButton) findViewById(R.id.ibtn_Fechar);
        ibtn_Filtro = (ImageButton) findViewById(R.id.ibtn_Filtro);

        //Operações dos botões
        ibtn_Seg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(MainActivity.this, Segmentar.class);
                startActivity(i);
            }
        });

        ibtn_Morf.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(MainActivity.this, Morfologia.class);
                startActivity(i);
            }
        });

        ibtn_Conv.setOnClickListener(new View.OnClickListener() {//Próxima tarefa da disciplina
            @Override
            public void onClick(View v) {
                Intent i = new Intent(MainActivity.this, Convolucao.class);
                startActivity(i);
            }
        });

        ibtn_Filtro.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(MainActivity.this, Particulas.class);
                startActivity(i);
            }
        });

        ibtn_Fechar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

    }

    //Verifica as permissoes recebidas pelo aplicativo
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        if (requestCode == CAMERA_PERMISSAO_REQUEST) {//Se for permissao de escrita
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                //Toast.makeText(this,"Permissao de usar a camera autorizada!!",Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(this, "Permissao de usar a camera não autorizada!!", Toast.LENGTH_LONG).show();
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }
}
