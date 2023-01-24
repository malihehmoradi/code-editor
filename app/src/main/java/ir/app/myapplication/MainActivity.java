package ir.app.myapplication;

import android.app.*;
import android.net.Uri;
import android.os.*;
import android.content.*;
import android.view.View;
import android.widget.*;
import android.text.*;

import java.io.*;

import android.graphics.*;
import android.util.*;

import java.net.*;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import android.text.style.*;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button btn_open = findViewById(R.id.btn_open);
        btn_open.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openCodeEditor();
            }
        });

    }

    private final Pattern numberPattern = Pattern.compile("[0-9]");
    private final Pattern symbolPattern = Pattern.compile("[\\!\\\"\\#\\$\\%\\&\\'\\(\\)\\*\\+\\,\\-\\.\\/\\:\\;\\<\\>\\=\\?\\@\\[\\]\\{\\}\\\\\\^\\_\\`\\~]+$");

    private boolean isNumber(CharSequence s) {
        return numberPattern.matcher(s).matches();
    }

    private boolean isSymbol(CharSequence s) {
        return symbolPattern.matcher(s).matches();
    }

    public void openCodeEditor() {

        String url = "http://test.ir";
        final String[] code = {null};

        final AlertDialog dialog = new AlertDialog.Builder(MainActivity.this)
                .setTitle("Code Editor")
                .setView(R.layout.codeeditor)
                .setPositiveButton("Send", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        new MyHttpRequestTask().execute(url, code[0]);
                        dialog.dismiss();

                    }
                }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                }).create();
        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface arg0) {
                dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.parseColor("#5296BA"));
                dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(Color.parseColor("#5296BA"));
            }
        });
        dialog.show();


        final EditText editor = dialog.findViewById(R.id.codeeditorEditText);
        editor.setBackgroundColor(Color.TRANSPARENT);
        SpannableStringBuilder mSpannableStringBuilder = new SpannableStringBuilder();
        editor.addTextChangedListener(new TextWatcher() {

            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable s) {

                if (s.length() > 0) {
                    code[0] = s.toString();

                    // unregister and register the listener to avoid infinite loop
                    editor.removeTextChangedListener(this);

                    int start = s.length() - 1;
                    String lastChar = s.toString().substring(start);

                    SpannableString lastSpannableChar = new SpannableString(lastChar);

                    // pick the color based on the last char
                    try {
                        int color = Color.BLACK;
                        if (isNumber(lastChar)) {
                            color = Color.YELLOW;
                        }
                        if (isSymbol(lastChar)) {
                            color = Color.GREEN;
                        }
                        // Span to set char color
                        ForegroundColorSpan fcs = new ForegroundColorSpan(color);

                        // Set the text color for the last character
                        lastSpannableChar.setSpan(fcs, 0, 1, Spannable.SPAN_INCLUSIVE_INCLUSIVE);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }


                    // append the last char to the string builder so you can keep the previous span
                    mSpannableStringBuilder.append(lastSpannableChar);

                    editor.setText(mSpannableStringBuilder);
                    editor.setSelection(editor.getText().length()); //this is to move the cursor position

                    editor.addTextChangedListener(this);

                }
            }
        });

    }

    private class MyHttpRequestTask extends AsyncTask<String, Integer, String> {

        @Override
        protected String doInBackground(String... params) {
            String my_url = params[0];
            String code = params[1];
            HashMap<String, String> postDataParams = new HashMap<>();
            postDataParams.put("code", code);

            try {
                URL url = new URL(my_url);
                HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
                // setting the  Request Method Type
                httpURLConnection.setRequestMethod("POST");
                // adding the headers for request
                httpURLConnection.setRequestProperty("Content-Type", "application/json");
                httpURLConnection.addRequestProperty("code", code);
                try {
                    //to tell the connection object that we will be wrting some data on the server and then will fetch the output result
                    httpURLConnection.setDoOutput(true);
                    // this is used for just in case we don't know about the data size associated with our request
                    httpURLConnection.setChunkedStreamingMode(0);


                    // to write tha data in our request
                    DataOutputStream outputStream = new DataOutputStream(httpURLConnection.getOutputStream());
                    outputStream.writeBytes("Content-Disposition: form-data; code=" + code);
                    OutputStreamWriter outputStreamWriter = new OutputStreamWriter(outputStream);
                    outputStreamWriter.write(getPostDataString(postDataParams));
                    outputStreamWriter.flush();
                    outputStreamWriter.close();


                    // to log the response code of your request
                    Log.d("TAG", "MyHttpRequestTask doInBackground : " + httpURLConnection.getResponseCode());
                    // to log the response message from your server after you have tried the request.
                    Log.d("TAG", "MyHttpRequestTask doInBackground : " + httpURLConnection.getResponseMessage());


                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    // this is done so that there are no open connections left when this task is going to complete
                    httpURLConnection.disconnect();
                }


            } catch (Exception e) {
                e.printStackTrace();
            }

            return null;
        }

        private String getPostDataString(HashMap<String, String> params) throws UnsupportedEncodingException {
            StringBuilder result = new StringBuilder();
            boolean first = true;
            for (Map.Entry<String, String> entry : params.entrySet()) {
                if (first)
                    first = false;
                else
                    result.append("&");

                result.append(URLEncoder.encode(entry.getKey(), "UTF-8"));
                result.append("=");
                result.append(URLEncoder.encode(entry.getValue(), "UTF-8"));
            }

            return result.toString();
        }
    }

}