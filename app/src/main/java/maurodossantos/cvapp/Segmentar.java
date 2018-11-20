package maurodossantos.cvapp;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageButton;

public class Segmentar extends AppCompatActivity {

    private ImageButton ibtn_Suav1, ibtn_Suav2, ibtn_Seg1, ibtn_Seg2, ibtn_Borda1, ibtn_Borda2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_segmentar);

        // Link com os objetos da tela:
        ibtn_Suav1 = (ImageButton) findViewById(R.id.ibtn_Suav1);
        ibtn_Suav2 = (ImageButton) findViewById(R.id.ibtn_Suav2);
        ibtn_Seg1 = (ImageButton) findViewById(R.id.ibtn_Seg1);
        ibtn_Seg2 = (ImageButton) findViewById(R.id.ibtn_Seg2);
        ibtn_Borda1 = (ImageButton) findViewById(R.id.ibtn_Borda1);
        ibtn_Borda2 = (ImageButton) findViewById(R.id.ibtn_Borda2);

        // Botões para cada opção de segmentação
        ibtn_Suav1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(Segmentar.this, Suavizador1.class);
                startActivity(i);
            }
        });

        ibtn_Suav2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(Segmentar.this, Suavizador2.class);
                startActivity(i);
            }
        });

        ibtn_Seg1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(Segmentar.this, Segmentacao1.class);
                startActivity(i);
            }
        });

        ibtn_Seg2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(Segmentar.this, Segmentacao2.class);
                startActivity(i);
            }
        });

        ibtn_Borda1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(Segmentar.this, Detector1.class);
                startActivity(i);
            }
        });

        ibtn_Borda2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(Segmentar.this, Detector2.class);
                startActivity(i);
            }
        });

    }
}
