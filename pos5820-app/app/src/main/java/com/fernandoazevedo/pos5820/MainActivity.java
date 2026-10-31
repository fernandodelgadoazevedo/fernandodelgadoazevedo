package com.fernandoazevedo.pos5820;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.text.Layout;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends Activity {
    private static final int REQ_PERMS = 1;
    private static final int REQ_IMAGE = 2;

    private static final int[] FONT_SIZES = {20, 26, 34, 44, 60};
    private static final String[] FONT_NAMES = {"Fonte pequena", "Fonte normal", "Fonte média", "Fonte grande", "Fonte gigante"};
    private static final Layout.Alignment[] ALIGNS = {
            Layout.Alignment.ALIGN_NORMAL, Layout.Alignment.ALIGN_CENTER, Layout.Alignment.ALIGN_OPPOSITE};
    private static final String[] ALIGN_NAMES = {"Esquerda", "Centro", "Direita"};

    private final ExecutorService exec = Executors.newSingleThreadExecutor();
    private final Handler ui = new Handler(Looper.getMainLooper());

    private PrinterSettings s;
    private Bitmap userImage;

    private TextView txtStatus;
    private Spinner spnProtocol, spnMedia, spnFont, spnAlign;
    private EditText edtLabelW, edtLabelH, edtGap, edtDensity, edtText, edtQr, edtBarcode, edtCopies;
    private CheckBox chkInvert, chkNextLabel, chkBold;
    private ImageView imgThumb, imgPreview;
    private ProgressBar progress;
    private View boxLabel;
    private final List<Button> actionButtons = new ArrayList<>();

    private interface PrinterTask {
        void run() throws Exception;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        txtStatus = findViewById(R.id.txtStatus);
        spnProtocol = findViewById(R.id.spnProtocol);
        spnMedia = findViewById(R.id.spnMedia);
        spnFont = findViewById(R.id.spnFont);
        spnAlign = findViewById(R.id.spnAlign);
        edtLabelW = findViewById(R.id.edtLabelW);
        edtLabelH = findViewById(R.id.edtLabelH);
        edtGap = findViewById(R.id.edtGap);
        edtDensity = findViewById(R.id.edtDensity);
        edtText = findViewById(R.id.edtText);
        edtQr = findViewById(R.id.edtQr);
        edtBarcode = findViewById(R.id.edtBarcode);
        edtCopies = findViewById(R.id.edtCopies);
        chkInvert = findViewById(R.id.chkInvert);
        chkNextLabel = findViewById(R.id.chkNextLabel);
        chkBold = findViewById(R.id.chkBold);
        imgThumb = findViewById(R.id.imgThumb);
        imgPreview = findViewById(R.id.imgPreview);
        progress = findViewById(R.id.progress);
        boxLabel = findViewById(R.id.boxLabel);

        setupSpinner(spnProtocol, new String[]{
                "Recibo / cupom (ESC/POS)", "Etiqueta com sensor (TSPL)"});
        setupSpinner(spnMedia, new String[]{
                "Etiqueta com espaço (gap)", "Etiqueta com marca preta", "Contínuo (sem sensor)"});
        setupSpinner(spnFont, FONT_NAMES);
        setupSpinner(spnAlign, ALIGN_NAMES);

        s = PrinterSettings.load(this);
        applySettingsToUi();

        spnProtocol.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                updateModeVisibility();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        findViewById(R.id.btnConnect).setOnClickListener(v -> chooseDevice());
        findViewById(R.id.btnDisconnect).setOnClickListener(v -> {
            exec.execute(() -> {
                PrinterManager.get().disconnect();
                ui.post(this::updateStatus);
            });
        });
        findViewById(R.id.btnPickImage).setOnClickListener(v -> {
            Intent i = new Intent(Intent.ACTION_GET_CONTENT);
            i.setType("image/*");
            startActivityForResult(Intent.createChooser(i, "Escolher imagem"), REQ_IMAGE);
        });
        findViewById(R.id.btnClearImage).setOnClickListener(v -> setUserImage(null));
        findViewById(R.id.btnPreview).setOnClickListener(v -> preview());
        findViewById(R.id.btnPrintSettings).setOnClickListener(v ->
                startActivity(new Intent(Settings.ACTION_PRINT_SETTINGS)));

        Button btnPrint = findViewById(R.id.btnPrint);
        Button btnTest = findViewById(R.id.btnTest);
        Button btnFeed = findViewById(R.id.btnFeed);
        Button btnCalibrate = findViewById(R.id.btnCalibrate);
        actionButtons.add(btnPrint);
        actionButtons.add(btnTest);
        actionButtons.add(btnFeed);
        actionButtons.add(btnCalibrate);

        btnPrint.setOnClickListener(v -> print());
        btnTest.setOnClickListener(v -> {
            readUi();
            runPrinterTask(() -> PrintEngine.testPage(s));
        });
        btnFeed.setOnClickListener(v -> {
            readUi();
            runPrinterTask(() -> PrintEngine.feed(s));
        });
        btnCalibrate.setOnClickListener(v -> calibrate());

        ensurePermissions();
        handleShareIntent(getIntent());
        updateStatus();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        handleShareIntent(intent);
    }

    @Override
    protected void onPause() {
        super.onPause();
        readUi();
    }

    private void setupSpinner(Spinner sp, String[] items) {
        ArrayAdapter<String> a = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, items);
        a.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sp.setAdapter(a);
    }

    private void applySettingsToUi() {
        spnProtocol.setSelection(s.protocol);
        spnMedia.setSelection(s.media);
        spnFont.setSelection(Math.min(s.fontIndex, FONT_SIZES.length - 1));
        spnAlign.setSelection(Math.min(s.alignIndex, ALIGNS.length - 1));
        edtLabelW.setText(String.valueOf(s.labelW));
        edtLabelH.setText(String.valueOf(s.labelH));
        edtGap.setText(trimFloat(s.gap));
        edtDensity.setText(String.valueOf(s.density));
        edtCopies.setText(String.valueOf(s.copies));
        chkInvert.setChecked(s.invert);
        chkNextLabel.setChecked(s.nextLabel);
        chkBold.setChecked(s.bold);
        updateModeVisibility();
    }

    private void updateModeVisibility() {
        boolean label = spnProtocol.getSelectedItemPosition() == PrinterSettings.PROTO_TSPL;
        boxLabel.setVisibility(label ? View.VISIBLE : View.GONE);
        chkNextLabel.setVisibility(label ? View.GONE : View.VISIBLE);
    }

    private static String trimFloat(float f) {
        return f == Math.rint(f) ? String.valueOf((int) f) : String.valueOf(f);
    }

    private static int parseInt(EditText e, int def, int min, int max) {
        try {
            int v = Integer.parseInt(e.getText().toString().trim());
            return Math.max(min, Math.min(max, v));
        } catch (NumberFormatException ex) {
            return def;
        }
    }

    private static float parseFloat(EditText e, float def) {
        try {
            return Math.max(0f, Math.min(20f, Float.parseFloat(e.getText().toString().trim().replace(',', '.'))));
        } catch (NumberFormatException ex) {
            return def;
        }
    }

    private void readUi() {
        s.protocol = spnProtocol.getSelectedItemPosition();
        s.media = spnMedia.getSelectedItemPosition();
        s.fontIndex = spnFont.getSelectedItemPosition();
        s.alignIndex = spnAlign.getSelectedItemPosition();
        s.labelW = parseInt(edtLabelW, s.labelW, 10, 58);
        s.labelH = parseInt(edtLabelH, s.labelH, 5, 300);
        s.gap = parseFloat(edtGap, s.gap);
        s.density = parseInt(edtDensity, s.density, 0, 15);
        s.copies = parseInt(edtCopies, s.copies, 1, 99);
        s.invert = chkInvert.isChecked();
        s.nextLabel = chkNextLabel.isChecked();
        s.bold = chkBold.isChecked();
        s.save(this);
    }

    private LabelRenderer.Content buildContent() {
        LabelRenderer.Content c = new LabelRenderer.Content();
        c.text = edtText.getText().toString();
        c.textSize = FONT_SIZES[Math.max(0, Math.min(s.fontIndex, FONT_SIZES.length - 1))];
        c.align = ALIGNS[Math.max(0, Math.min(s.alignIndex, ALIGNS.length - 1))];
        c.bold = s.bold;
        c.qr = edtQr.getText().toString();
        c.barcode = edtBarcode.getText().toString();
        c.image = userImage;
        return c;
    }

    // ---------- Permissões e Bluetooth ----------

    private boolean hasBtPermission() {
        return Build.VERSION.SDK_INT < 31
                || checkSelfPermission(Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED;
    }

    private void ensurePermissions() {
        if (!hasBtPermission()) {
            requestPermissions(new String[]{Manifest.permission.BLUETOOTH_CONNECT}, REQ_PERMS);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQ_PERMS && !hasBtPermission()) {
            toast("Sem a permissão de Bluetooth o app não consegue falar com a impressora.");
        }
        updateStatus();
    }

    @SuppressLint("MissingPermission")
    private void chooseDevice() {
        if (!hasBtPermission()) {
            ensurePermissions();
            return;
        }
        BluetoothAdapter a = BluetoothAdapter.getDefaultAdapter();
        if (a == null) {
            toast("Este aparelho não tem Bluetooth");
            return;
        }
        if (!a.isEnabled()) {
            startActivity(new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE));
            return;
        }
        final List<BluetoothDevice> devices = new ArrayList<>(a.getBondedDevices());
        final String[] names = new String[devices.size()];
        for (int i = 0; i < names.length; i++) {
            BluetoothDevice d = devices.get(i);
            String n = d.getName() != null ? d.getName() : "(sem nome)";
            names[i] = n + "\n" + d.getAddress();
        }
        AlertDialog.Builder b = new AlertDialog.Builder(this)
                .setTitle("Impressoras pareadas")
                .setNeutralButton("Parear nova", (d, w) ->
                        startActivity(new Intent(Settings.ACTION_BLUETOOTH_SETTINGS)))
                .setNegativeButton("Cancelar", null);
        if (devices.isEmpty()) {
            b.setMessage("Nenhum dispositivo pareado.\n\nLigue a impressora, toque em \"Parear nova\" "
                    + "e procure por POS-5820 / Printer (PIN 0000 ou 1234).");
        } else {
            b.setItems(names, (d, which) -> {
                BluetoothDevice dev = devices.get(which);
                s.address = dev.getAddress();
                s.deviceName = dev.getName() != null ? dev.getName() : dev.getAddress();
                s.save(this);
                runPrinterTask(() -> PrinterManager.get().connect(s.address));
            });
        }
        b.show();
    }

    private void updateStatus() {
        if (s.address == null || s.address.isEmpty()) {
            txtStatus.setText("Nenhuma impressora selecionada. Toque em \"Escolher / conectar\".");
            return;
        }
        PrinterManager pm = PrinterManager.get();
        boolean on = pm.isConnected() && s.address.equals(pm.getAddress());
        txtStatus.setText(s.deviceName + " (" + s.address + ")\n" + (on ? "● Conectada" : "○ Desconectada — conecta ao imprimir"));
    }

    // ---------- Ações ----------

    private void runPrinterTask(final PrinterTask task) {
        if (s.address == null || s.address.isEmpty()) {
            chooseDevice();
            return;
        }
        if (!hasBtPermission()) {
            ensurePermissions();
            return;
        }
        setBusy(true);
        exec.execute(() -> {
            String err = null;
            try {
                task.run();
            } catch (Throwable t) {
                err = t.getMessage() != null ? t.getMessage() : t.toString();
            }
            final String fe = err;
            ui.post(() -> {
                setBusy(false);
                if (fe != null) toast("Erro: " + fe);
                updateStatus();
            });
        });
    }

    private void setBusy(boolean busy) {
        progress.setVisibility(busy ? View.VISIBLE : View.GONE);
        for (Button b : actionButtons) b.setEnabled(!busy);
    }

    private void print() {
        readUi();
        final LabelRenderer.Content c = buildContent();
        if (c.isEmpty()) {
            toast("Digite um texto, QR, código de barras ou escolha uma imagem.");
            return;
        }
        final int copies = s.copies;
        runPrinterTask(() -> {
            final Bitmap bmp = PrintEngine.render(c, s);
            ui.post(() -> showPreview(bmp));
            PrintEngine.printBitmaps(java.util.Collections.singletonList(bmp), s, copies);
        });
    }

    private void preview() {
        readUi();
        final LabelRenderer.Content c = buildContent();
        if (c.isEmpty()) {
            toast("Nada para pré-visualizar.");
            return;
        }
        exec.execute(() -> {
            try {
                final Bitmap bmp = PrintEngine.render(c, s);
                ui.post(() -> showPreview(bmp));
            } catch (final Exception e) {
                ui.post(() -> toast("Erro: " + e.getMessage()));
            }
        });
    }

    private void showPreview(Bitmap bmp) {
        imgPreview.setImageBitmap(bmp);
        imgPreview.setVisibility(View.VISIBLE);
    }

    private void calibrate() {
        readUi();
        if (PrintEngine.isLabel(s)) {
            runPrinterTask(() -> PrinterManager.get().send(s.address, Tspl.calibrate(s)));
            toast("Calibrando: a impressora vai avançar algumas etiquetas.");
        } else {
            new AlertDialog.Builder(this)
                    .setTitle("Calibrar sensor")
                    .setMessage("No modo recibo (ESC/POS) a calibração é feita na própria impressora:\n\n"
                            + "1. Coloque o rolo de etiquetas e feche a tampa.\n"
                            + "2. Com a impressora ligada, segure o botão FEED por 2–3 segundos até ela avançar e parar no início de uma etiqueta.\n\n"
                            + "Depois marque \"avançar até a próxima etiqueta\". Se sua impressora aceitar TSPL, prefira o modo Etiqueta.")
                    .setPositiveButton("OK", null)
                    .show();
        }
    }

    // ---------- Imagens e compartilhamento ----------

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQ_IMAGE && resultCode == RESULT_OK && data != null && data.getData() != null) {
            loadImage(data.getData());
        }
    }

    @SuppressWarnings("deprecation")
    private void handleShareIntent(Intent intent) {
        if (intent == null || !Intent.ACTION_SEND.equals(intent.getAction())) return;
        String type = intent.getType();
        if (type == null) return;
        if (type.startsWith("text/")) {
            String t = intent.getStringExtra(Intent.EXTRA_TEXT);
            if (t != null) edtText.setText(t);
        } else if (type.startsWith("image/")) {
            Uri uri = intent.getParcelableExtra(Intent.EXTRA_STREAM);
            if (uri != null) loadImage(uri);
        }
    }

    private void loadImage(final Uri uri) {
        exec.execute(() -> {
            try {
                BitmapFactory.Options o = new BitmapFactory.Options();
                o.inJustDecodeBounds = true;
                InputStream in = getContentResolver().openInputStream(uri);
                BitmapFactory.decodeStream(in, null, o);
                if (in != null) in.close();
                int sample = 1;
                while (o.outWidth / sample > 1600 || o.outHeight / sample > 4000) sample *= 2;
                BitmapFactory.Options o2 = new BitmapFactory.Options();
                o2.inSampleSize = sample;
                in = getContentResolver().openInputStream(uri);
                final Bitmap b = BitmapFactory.decodeStream(in, null, o2);
                if (in != null) in.close();
                ui.post(() -> {
                    if (b == null) toast("Não foi possível abrir a imagem");
                    else setUserImage(b);
                });
            } catch (final Exception e) {
                ui.post(() -> toast("Erro ao abrir imagem: " + e.getMessage()));
            }
        });
    }

    private void setUserImage(Bitmap b) {
        userImage = b;
        imgThumb.setImageBitmap(b);
        imgThumb.setVisibility(b == null ? View.GONE : View.VISIBLE);
    }

    private void toast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
    }
}
