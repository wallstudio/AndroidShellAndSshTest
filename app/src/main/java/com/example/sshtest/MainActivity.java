package com.example.sshtest;

import androidx.appcompat.app.AppCompatActivity;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.util.Hashtable;

import android.os.Bundle;
import android.os.StrictMode;
import android.view.View;
import android.widget.TextView;
import com.jcraft.jsch.*;

abstract class UserBase
{
    public abstract String getUserName();
    public abstract String getPassword();
}

public class MainActivity extends AppCompatActivity {


    UserBase user = new User();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        try {
            Field f = Packet.class.getDeclaredField("random");
            f.setAccessible(true);
            f.set(null, new Random() {
                @Override
                public void fill(byte[] foo, int start, int len){}
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
        setContentView(R.layout.activity_main);
    }

    @Override
    protected  void onStart()
    {
        super.onStart();

        final TextView oview = findViewById(R.id.Output);
        final TextView iview = findViewById(R.id.Input);
        final TextView hview = findViewById(R.id.Host);

        try {
        } catch (Exception e) {
            oview.setText(e.toString());
        }

        findViewById(R.id.Ssh).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    JSch jsch = new JSch();
                    Hashtable config = new Hashtable();
                    config.put("StrictHostKeyChecking", "no");
                    jsch.setConfig(config);
                    Session session = jsch.getSession(user.getUserName(), hview.getText().toString(), 22);
                    session.setPassword(user.getPassword());
                    // session.setPortForwardingR(hview.getText().toString(), 80, "google.com", 80);
                    session.connect();
                    oview.setText(ExecSSHCommand(session, iview.getText().toString().replace('\n', ' ')));
                    session.disconnect();
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                    oview.setText(e.toString());
                }
            }
        });

        findViewById(R.id.Run).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    ProcessBuilder builder = new ProcessBuilder();
                    builder.command("sh", "-c", iview.getText().toString().replace('\n', ' '));
                    Process process = builder.start();
                    InputStream stream = process.getInputStream();
                    InputStream errorStream = process.getErrorStream();
                    process.waitFor();
                    oview.setText(streamToString(stream) + "\n---\n"+ streamToString(errorStream));
                }
                catch (Exception e)
                {
                    oview.setText(e.toString());
                }
            }
        });


    }

    private String ExecSSHCommand(Session session, String command) throws JSchException, IOException {
        ChannelExec channel=(ChannelExec)session.openChannel("exec");
        channel.setCommand(command);
        channel.connect();
        InputStream output = channel.getInputStream();
        return streamToString((output));
    }


    public static String streamToString(InputStream is) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();

        String line;
        while ((line = br.readLine()) != null) {
            sb.append(line + '\n');
        }
        br.close();

        String string = sb.toString();
        //string = string.substring(0, string.length() - 2);

        return string;
    }
}