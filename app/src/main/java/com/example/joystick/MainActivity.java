package com.example.joystick;

import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private ImageView joystickLeft, joystickRight;
    private RelativeLayout joystickAreaLeft, joystickAreaRight;
    private TextView leftJoystickText, rightJoystickText;
    private WebView webView;
    private float radiusLeft, radiusRight;
    private float centerXLeft, centerYLeft;
    private float centerXRight, centerYRight;

    private static final float MAX_COORDINATE = 100.0f;  // Maksimum koordinat değeri
    private static final float MIN_COORDINATE = -100.0f; // Minimum koordinat değeri

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        joystickLeft = findViewById(R.id.joystick_left);
        joystickRight = findViewById(R.id.joystick_right);
        joystickAreaLeft = findViewById(R.id.joystick_area_left);
        joystickAreaRight = findViewById(R.id.joystick_area_right);
        leftJoystickText = findViewById(R.id.left_joystick_text);
        rightJoystickText = findViewById(R.id.right_joystick_text);
        webView = findViewById(R.id.webview);

        // WebView ayarları

        webView.getSettings().setJavaScriptEnabled(true);
        webView.setWebViewClient(new WebViewClient());
        webView.loadUrl("http://192.168.137.130/capture");

        // Sol Joystick için merkez ve yarıçap ayarları
        joystickAreaLeft.post(() -> {
            centerXLeft = joystickAreaLeft.getWidth() / 2;
            centerYLeft = joystickAreaLeft.getHeight() / 2;
            radiusLeft = (Math.min(joystickAreaLeft.getWidth(), joystickAreaLeft.getHeight()) / 2) - (joystickLeft.getWidth() / 2);
            setupJoystickMovement(joystickLeft, joystickAreaLeft, centerXLeft, centerYLeft, radiusLeft, true);
        });

        // Sağ Joystick için merkez ve yarıçap ayarları
        joystickAreaRight.post(() -> {
            centerXRight = joystickAreaRight.getWidth() / 2;
            centerYRight = joystickAreaRight.getHeight() / 2;
            radiusRight = (Math.min(joystickAreaRight.getWidth(), joystickAreaRight.getHeight()) / 2) - (joystickRight.getWidth() / 2);
            setupJoystickMovement(joystickRight, joystickAreaRight, centerXRight, centerYRight, radiusRight, false);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        resetJoysticks();
        updateJoystickTexts(0, 0); // Koordinatları sıfırla
    }

    private void setupJoystickMovement(ImageView joystick, RelativeLayout joystickArea, float centerX, float centerY, float radius, boolean isLeft) {
        joystickArea.setOnTouchListener((v, event) -> {
            float x = event.getX() - joystickArea.getWidth() / 2;
            float y = event.getY() - joystickArea.getHeight() / 2;

            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                case MotionEvent.ACTION_MOVE:
                    setJoystickPosition(joystick, x, y, radius, centerX, centerY);

                    // Handle'ı merkezden sapmayı normalize ederek -100 ile +100 arasında bir değere getiriyoruz
                    float distance = (float) Math.sqrt(x * x + y * y);
                    float clampedX = distance > radius ? (x / distance) * radius : x;
                    float clampedY = distance > radius ? (y / distance) * radius : y;

                    float normalizedX = (clampedX / radius) * MAX_COORDINATE;
                    float normalizedY = (clampedY / radius) * MAX_COORDINATE;

                    // Güncellenen pozisyonları TextView'a yazdır
                    if (isLeft) {
                        updateLeftJoystickText(normalizedX, normalizedY);
                    } else {
                        updateRightJoystickText(normalizedX, normalizedY);
                    }
                    break;

                case MotionEvent.ACTION_UP:
                    snapBackJoystick(joystick, centerX, centerY);
                    if (isLeft) {
                        updateLeftJoystickText(0, 0);
                    } else {
                        updateRightJoystickText(0, 0);
                    }
                    break;
            }
            return true;
        });
    }

    private void setJoystickPosition(ImageView joystick, float x, float y, float radius, float centerX, float centerY) {
        double distance = Math.sqrt(x * x + y * y);
        if (distance > radius) {
            float ratio = (float) (radius / distance);
            x *= ratio;
            y *= ratio;
        }

        // Joystick'in merkezi alt kısmı referans alınarak ayarlanır
        float handleX = centerX + x - joystick.getWidth() / 2;
        float handleY = centerY + y - joystick.getHeight() / 2;

        // Joystick alanı sınırları içinde kalmasını sağla
        handleX = Math.max(0, Math.min(handleX, joystickAreaLeft.getWidth() - joystick.getWidth()));
        handleY = Math.max(0, Math.min(handleY, joystickAreaLeft.getHeight() - joystick.getHeight()));

        joystick.setX(handleX);
        joystick.setY(handleY);
    }

    private void snapBackJoystick(ImageView joystick, float centerX, float centerY) {
        joystick.animate()
                .x(centerX - joystick.getWidth() / 2)
                .y(centerY - joystick.getHeight() / 2)
                .setDuration(150) // Geri dönüş hızı
                .start();
    }

    private void updateLeftJoystickText(float x, float y) {
        leftJoystickText.setText(String.format("Left Joystick - X: %.0f, Y: %.0f", x, y));
    }

    private void updateRightJoystickText(float x, float y) {
        rightJoystickText.setText(String.format("Right Joystick - X: %.0f, Y: %.0f", x, y));
    }

    private void updateJoystickTexts(float x, float y) {
        updateLeftJoystickText(x, y);
        updateRightJoystickText(x, y);
    }

    private void resetJoysticks() {
        joystickAreaLeft.post(() -> {
            centerXLeft = joystickAreaLeft.getWidth() / 2;
            centerYLeft = joystickAreaLeft.getHeight() / 2;
            snapBackJoystick(joystickLeft, centerXLeft, centerYLeft);
        });

        joystickAreaRight.post(() -> {
            centerXRight = joystickAreaRight.getWidth() / 2;
            centerYRight = joystickAreaRight.getHeight() / 2;
            snapBackJoystick(joystickRight, centerXRight, centerYRight);
        });
    }
}
