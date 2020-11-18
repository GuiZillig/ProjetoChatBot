package com.example.classes.MarileneBot;
/**
 * Blibiotecas android para o dev
 */

import android.content.Context;
import android.graphics.Typeface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;
/**
 * Blibiotecas IBM Watson para o dev
 */
import com.ibm.cloud.sdk.core.http.Response;
import com.ibm.cloud.sdk.core.http.ServiceCall;
import com.ibm.cloud.sdk.core.security.IamAuthenticator;
import com.ibm.watson.assistant.v2.model.DialogNodeOutputOptionsElement;
import com.ibm.watson.assistant.v2.model.RuntimeResponseGeneric;
import com.ibm.watson.assistant.v2.Assistant;
import com.ibm.watson.assistant.v2.model.CreateSessionOptions;
import com.ibm.watson.assistant.v2.model.MessageInput;
import com.ibm.watson.assistant.v2.model.MessageOptions;
import com.ibm.watson.assistant.v2.model.MessageResponse;
import com.ibm.watson.assistant.v2.model.SessionResponse;

/**
 * Blibiotecas JAVA para o dev
 */
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {


    private RecyclerView recyclerView; // Modelo de view Para lista
    private AdptadorChat mAdapter;// Adaptador para o uso do chat
    private ArrayList messageArrayList;//uma lista para guardar a troca de mensagens
    private EditText inputMessage;//view android
    private ImageButton btnSend;//view para botao de enviar

    private boolean initialRequest;//verificador de requisao
    private  final static String TAG = "MainActivity";//uma contante para realizar consulta de erros ao log
    private Context mContext;//auxiliar receptor de credenciais da ibm
    private Assistant watsonAssistant;// obejto de assistant para realizar funcionalidade de seus metodos
    private Response<SessionResponse> watsonAssistantSession;//receptor da respsota da api

    /**
     * Metodo responsavel por captar e iniciar as credenciais da api
     */

    private void createServices() {
        watsonAssistant = new Assistant("2019-02-28", new IamAuthenticator(mContext.getString(R.string.assistant_apikey)));
        watsonAssistant.setServiceUrl(mContext.getString(R.string.assistant_url));

    }

    /**
     * OnCreate esta realizando diversas funcionalidades como  capturar od IDS das Views e incoporar suas funcionalidades,
     * assim como sua RecyclerView que eh o responsavel por fazer o refresh e mostrar a troca de mensagens, atraves do seu adpater de
     * arraylist cujo esta sendo responsavel por incorporar a lista onde esta guardado as trocas de mensagens,
     * e por fum esta puxando alguns metodos da classe para o seu funcionamento e fazendo edicao cosmeticas de alguns itens como
     * fonte das letras,cores, e modficando a appBar,
     *
     * @param savedInstanceState
     */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setTitle("Marilene");
        mContext = getApplicationContext();
        inputMessage = findViewById(R.id.message);
        btnSend = findViewById(R.id.btn_send);
        String customFont = "Montserrat-Regular.ttf";
        Typeface typeface = Typeface.createFromAsset(getAssets(), customFont);
        inputMessage.setTypeface(typeface);
        recyclerView = findViewById(R.id.recycler_view);
        messageArrayList = new ArrayList<>();
        mAdapter = new AdptadorChat(messageArrayList);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(mAdapter);
        this.inputMessage.setText("");
        this.initialRequest = true;


        recyclerView.addOnItemTouchListener(new RecyclerTouchListener(getApplicationContext(), recyclerView, new ClickListener() {
            @Override
            public void onClick(View view, final int position) {
                Log.i(TAG, "onClick:  ");
            }

            @Override
            public void onLongClick(View view, int position) {
                sendMessage();

            }
        }));

        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (checkInternetConnection()) {
                    sendMessage();
                }
            }
        });

        createServices();
        sendMessage();
    }

    ;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.refresh:
                finish();
                startActivity(getIntent());

        }
        return (super.onOptionsItemSelected(item));
    }

    private void sendMessage() {

        final String inputmessage = this.inputMessage.getText().toString().trim();
        if (!this.initialRequest) {
            Message inputMessage = new Message();
            inputMessage.setMessage(inputmessage);
            inputMessage.setId("1");
            messageArrayList.add(inputMessage);
        } else {
            Message inputMessage = new Message();
            inputMessage.setMessage(inputmessage);
            inputMessage.setId("100");
            this.initialRequest = false;
        }

        this.inputMessage.setText("");
        mAdapter.notifyDataSetChanged();

        Thread thread = new Thread(new Runnable() {
            public void run() {
                try {
                    if (watsonAssistantSession == null) {
                        ServiceCall<SessionResponse> call = watsonAssistant.createSession(new CreateSessionOptions.Builder().assistantId(mContext.getString(R.string.assistant_id)).build());
                        watsonAssistantSession = call.execute();
                    }

                    MessageInput input = new MessageInput.Builder()
                            .text(inputmessage)
                            .build();
                    MessageOptions options = new MessageOptions.Builder()
                            .assistantId(mContext.getString(R.string.assistant_id))
                            .input(input)
                            .sessionId(watsonAssistantSession.getResult().getSessionId())
                            .build();
                    Response<MessageResponse> response = watsonAssistant.message(options).execute();
                    Log.i(TAG, "run: " + response.getResult());
                    if (response != null &&
                            response.getResult().getOutput() != null &&
                            !response.getResult().getOutput().getGeneric().isEmpty()) {

                        List<RuntimeResponseGeneric> responses = response.getResult().getOutput().getGeneric();

                        for (RuntimeResponseGeneric r : responses) {
                            Message outMessage;
                            switch (r.responseType()) {
                                case "text":
                                    outMessage = new Message();
                                    outMessage.setMessage(r.text());
                                    outMessage.setId("2");

                                    messageArrayList.add(outMessage);
                                    break;

                                case "option":
                                    outMessage = new Message();
                                    String title = r.title();
                                    String OptionsOutput = "";
                                    for (int i = 0; i < r.options().size(); i++) {
                                        DialogNodeOutputOptionsElement option = r.options().get(i);
                                        OptionsOutput = OptionsOutput + option.getLabel() + "\n";

                                    }
                                    outMessage.setMessage(title + "\n" + OptionsOutput);
                                    outMessage.setId("2");

                                    messageArrayList.add(outMessage);

                                    break;

                                case "image":
                                    outMessage = new Message(r);
                                    messageArrayList.add(outMessage);
                                    break;
                                default:
                                    Log.e("Error", "Mensagem nao reconhecida");
                            }
                        }

                        runOnUiThread(new Runnable() {
                            public void run() {
                                mAdapter.notifyDataSetChanged();
                                if (mAdapter.getItemCount() > 1) {
                                    recyclerView.getLayoutManager().smoothScrollToPosition(recyclerView, null, mAdapter.getItemCount() - 1);

                                }

                            }
                        });
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        thread.start();
    }


    private boolean checkInternetConnection() {

        ConnectivityManager cm =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting();

        if (isConnected) {
            return true;
        } else {
            Toast.makeText(this, " Atenção é necessario acesso a internet para utilizar o app ", Toast.LENGTH_LONG).show();
            return false;
        }

    }

}





